package b100.fullscreenfix.mixin;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Optional;

import org.lwjgl.glfw.GLFWVidMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.Global;
import b100.fullscreenfix.MonitorInfo;
import b100.fullscreenfix.VideoMode;
import b100.fullscreenfix.util.GLFWUtil;
import b100.fullscreenfix.util.Win32Util;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.GpuBackend;

@Mixin(value = Window.class)
public abstract class WindowMixin {
	
	@Shadow
	private boolean fullscreen;
	
	private boolean wasFullscreen = false;
	private boolean initialized = false;
	
	private boolean fullscreenModeHasChanged = false;
	private com.mojang.blaze3d.platform.VideoMode newFullscreenMode;

	private boolean isMaximized = false;
	
	private int windowPosX;
	private int windowPosY;
	private int windowWidth;
	private int windowHeight;
	private boolean windowMaximized;
	
	private boolean firstUpdate = true;
	
	@Inject(method = "createGlfwWindow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuBackend;setWindowHints()V", ordinal = 0))
	private static void onSetupWindowHints(int width, int height, String title, long monitor, GpuBackend backend, CallbackInfoReturnable<Long> info) {
		FullscreenFix.debugPrint("Setup Window Hints");
		
		glfwWindowHint(GLFW_AUTO_ICONIFY, 1);
		glfwWindowHint(GLFW_RESIZABLE, 1);
		glfwWindowHint(GLFW_DECORATED, 1);
		glfwWindowHint(GLFW_VISIBLE, 0);
	}
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetFramebufferSizeCallback(JLorg/lwjgl/glfw/GLFWFramebufferSizeCallbackI;)Lorg/lwjgl/glfw/GLFWFramebufferSizeCallback;"))
	private void postInit(CallbackInfo info) {
		FullscreenFix.debugPrint("PostInit");
		FullscreenFix.debugPrint("Fullscreen: " + fullscreen);
		
		FullscreenFix.setWindow((Window)(Object)this);
		
		// Center Window
		int[] i = new int[1];
		int[] j = new int[1];
		glfwGetWindowSize(handle(), i, j);
		int width = i[0];
		int height = j[0];
		MonitorInfo monitor = new MonitorInfo(glfwGetPrimaryMonitor());
		int x = (monitor.width - width) / 2;
		int y = (monitor.height - height) / 2;
		glfwSetWindowPos(handle(), x, y);
		this.windowPosX = x;
		this.windowPosY = y;
		this.windowWidth = width;
		this.windowHeight = height;
		
		glfwShowWindow(handle());
		initialized = true;
		
		glfwSetWindowMaximizeCallback(handle(), (window, maximized) -> {
			isMaximized = maximized;
		});
	}
	
	@Inject(method = "updateFullscreenIfChanged", at = @At(value = "TAIL"))
	private void onSwapBuffers(CallbackInfo ci) {
		if(FullscreenFix.windowNeedsUpdate) {
			FullscreenFix.windowNeedsUpdate = false;
			updateWindowState();
		}
	}
	
	@Inject(method = "setMode", at = @At(value = "HEAD"), cancellable = true)
	private void onUpdateWindowRegion(CallbackInfo ci) {
		FullscreenFix.debugPrint("Update Window Region");
		
		ci.cancel();
		if(!initialized) {
			FullscreenFix.debugPrint("Not initialized!");
			return;
		}
		
		updateWindowState();
		
		wasFullscreen = fullscreen;
	}
	
	private void updateWindowState() {
		final boolean firstUpdate = this.firstUpdate;
		
		FullscreenFix.debugPrint("Update Window State");
		if(firstUpdate) {
			FullscreenFix.debugPrint("First update!");
		}
		
		if(!wasFullscreen) {
			windowMaximized = isMaximized;
			
			if(!isMaximized) {
				final long handle = handle();
				
				int[] i = new int[1];
				int[] j = new int[1];
				
				glfwGetWindowPos(handle, i, j);
				windowPosX = i[0];
				windowPosY = j[0];
				
				glfwGetWindowSize(handle, i, j);
				windowWidth = i[0];
				windowHeight = j[0];
				
				FullscreenFix.debugPrint("Window Size: " + windowWidth + " x " + windowHeight + " at " + windowPosX + ", " + windowPosY + ", max: " + windowMaximized);
			}else {
				FullscreenFix.debugPrint("Window maximized: " + windowMaximized);
			}
		}
		
		if(firstUpdate) {
			this.firstUpdate = false;
			
			if(FullscreenFix.isFullscreenEnabled() && !FullscreenFix.START_IN_FULLSCREEN.getBoolean()) {
				FullscreenFix.print("Start in fullscreen is disabled, turning off fullscreen");
				FullscreenFix.setFullscreen(false);
			}
		}
		
		if(fullscreenModeHasChanged) {
			fullscreenModeHasChanged = false;
			if(newFullscreenMode == null) {
				FullscreenFix.FULLSCREEN_VIDEO_MODE.set(null);
			}else {
				GLFWVidMode glfwVidMode = GLFWUtil.findMatchingVidMode(newFullscreenMode);
				if(glfwVidMode == null) {
					FullscreenFix.print("Could not find matching GLFW VideoMode: " + newFullscreenMode.getWidth() + " x " + newFullscreenMode.getHeight() + " @ " + newFullscreenMode.getRefreshRate() + "hz");
					FullscreenFix.FULLSCREEN_VIDEO_MODE.set(null);
				}else {
					FullscreenFix.FULLSCREEN_VIDEO_MODE.set(new VideoMode(glfwGetPrimaryMonitor(), glfwVidMode));	
				}
			}
		}
		
		final Window window = (Window)(Object)this;
		final long handle = handle();
		
		if(Global.OS_WINDOWS) {
			Win32Util.updateWindowState(window, windowPosX, windowPosY, windowWidth, windowHeight, windowMaximized, firstUpdate);
			FullscreenFix.windowNeedsUpdate = false;
			return;
		}
		
		glfwHideWindow(handle);

		final VideoMode fullscreenMode = FullscreenFix.FULLSCREEN_VIDEO_MODE.get();
		final boolean exclusive = FullscreenFix.EXCLUSIVE_FULLSCREEN.getBoolean();
		final boolean autoMinimize = FullscreenFix.AUTO_MINIMIZE.getBoolean();
		
		if(fullscreen && fullscreenMode != null) {
			FullscreenFix.print("Change to Fullscreen with custom resolution");
			
			MonitorInfo monitorInfo = new MonitorInfo(fullscreenMode.monitor);
			glfwSetWindowMonitor(window.handle(), 
					fullscreenMode.monitor,
					monitorInfo.posX,
					monitorInfo.posY,
					fullscreenMode.vidMode.width(),
					fullscreenMode.vidMode.height(),
					fullscreenMode.vidMode.refreshRate()
			);
		}else if(fullscreen && exclusive) {
			FullscreenFix.print("Change to GLFW Fullscreen");
			
			glfwSetWindowAttrib(handle, GLFW_AUTO_ICONIFY, autoMinimize ? 1 : 0);
			
			MonitorInfo monitor = MonitorInfo.getMonitor(window, firstUpdate);
			GLFWUtil.enableFullscreen(window, monitor);

			FullscreenFix.LAST_FULLSCREEN_MONITOR.set(monitor);
		}else {
			if(GLFWUtil.isFullscreen(window)) {
				GLFWUtil.disableFullscreen(window, windowPosX, windowPosY, windowWidth, windowHeight);	
			}
			
			if(fullscreen) {
				FullscreenFix.print("Change to Borderless Fullscreen");
				
				MonitorInfo monitor = MonitorInfo.getMonitor(window, firstUpdate);
				
				glfwSetWindowAttrib(handle, GLFW_DECORATED, 0);
				glfwSetWindowPos(handle, monitor.posX, monitor.posY);
				glfwSetWindowSize(handle, monitor.width, monitor.height);

				FullscreenFix.LAST_FULLSCREEN_MONITOR.set(monitor);
			}else {
				FullscreenFix.print("Change to Windowed");
				
				glfwSetWindowAttrib(handle, GLFW_DECORATED, 1);
				glfwSetWindowPos(handle, windowPosX, windowPosY);
				glfwSetWindowSize(handle, windowWidth, windowHeight);
			}
		}

		glfwShowWindow(handle);
		
		FullscreenFix.windowNeedsUpdate = false;
		GLFWUtil.updateCursorMode();
	}
	
	@Inject(method = "setPreferredFullscreenVideoMode", at = @At("HEAD"), cancellable = true)
	private void onSetFullscreenVideoMode(Optional<com.mojang.blaze3d.platform.VideoMode> optional, CallbackInfo ci) {
		FullscreenFix.print("Set Fullscreen Mode: " + optional);
		
		// This is called when the slider in the options menu is clicked.
		// We don't want to update the resolution until the menu is closed
		newFullscreenMode = optional.isPresent() ? optional.get() : null;
		fullscreenModeHasChanged = true;
	}
	
	@Shadow
	public abstract long handle();
	
}
