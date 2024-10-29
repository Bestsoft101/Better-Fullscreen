package b100.fullscreenfix.mixin;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Optional;

import org.lwjgl.glfw.GLFWVidMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.MonitorInfo;
import b100.fullscreenfix.VideoMode;
import b100.fullscreenfix.util.GLFWUtil;
import b100.fullscreenfix.util.Win32Util;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;

@Mixin(value = Window.class)
public abstract class WindowMixin {
	
	@Shadow
	private boolean fullscreen;
	@Shadow
	private MonitorTracker monitorTracker;
	
	private boolean wasFullscreen = false;
	private boolean initialized = false;
	
	private boolean fullscreenModeHasChanged = false;
	private net.minecraft.client.util.VideoMode newFullscreenMode;
	
	private int windowPosX;
	private int windowPosY;
	private int windowWidth;
	private int windowHeight;
	
	private boolean firstUpdate = true;
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", ordinal = 0))
	private void onSetupWindowHints(CallbackInfo info) {
		FullscreenFix.debugPrint("Setup Window Hints");
		FullscreenFix.debugPrint("Fullscreen: " + fullscreen);
		
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
		glfwGetWindowSize(getHandle(), i, j);
		int width = i[0];
		int height = j[0];
		MonitorInfo monitor = new MonitorInfo(glfwGetPrimaryMonitor());
		int x = (monitor.width - width) / 2;
		int y = (monitor.height - height) / 2;
		glfwSetWindowPos(getHandle(), x, y);
		this.windowPosX = x;
		this.windowPosY = y;
		this.windowWidth = width;
		this.windowHeight = height;
		
		glfwShowWindow(getHandle());
		initialized = true;
		
	}
	
	@Inject(method = "swapBuffers", at = @At(value = "TAIL"))
	private void onSwapBuffers(CallbackInfo ci) {
		if(FullscreenFix.windowNeedsUpdate) {
			FullscreenFix.windowNeedsUpdate = false;
			updateWindowState();
		}
	}
	
	@Inject(method = "updateWindowRegion", at = @At(value = "HEAD"), cancellable = true)
	private void onUpdateWindowRegion(CallbackInfo ci) {
		FullscreenFix.debugPrint("Update Window Region");
		FullscreenFix.debugPrint("Fullscreen: " + fullscreen);
		
		ci.cancel();
		if(!initialized) {
			FullscreenFix.debugPrint("Not initialized!");
			return;
		}
		
		final long handle = getHandle();
		
		if(!wasFullscreen) {
			int[] i = new int[1];
			int[] j = new int[1];
			
			glfwGetWindowPos(handle, i, j);
			windowPosX = i[0];
			windowPosY = j[0];
			
			glfwGetWindowSize(handle, i, j);
			windowWidth = i[0];
			windowHeight = j[0];
			
			FullscreenFix.debugPrint("Window Size: " + windowWidth + " x " + windowHeight + " at " + windowPosX + ", " + windowPosY);
		}
		
		updateWindowState();
		
		wasFullscreen = fullscreen;
	}
	
	private void updateWindowState() {
		FullscreenFix.debugPrint("Update Window State");
		FullscreenFix.debugPrint("Fullscreen: " + fullscreen);
		
		if(firstUpdate) {
			firstUpdate = false;
			
			if(FullscreenFix.isFullscreenEnabled() && !FullscreenFix.isStartInFullscreenEnabled()) {
				FullscreenFix.print("Start in fullscreen is disabled, turning off fullscreen");
				FullscreenFix.setFullscreen(false);
			}
		}
		
		if(fullscreenModeHasChanged) {
			fullscreenModeHasChanged = false;
			if(newFullscreenMode == null) {
				FullscreenFix.setFullscreenVideoMode(null);
			}else {
				GLFWVidMode glfwVidMode = GLFWUtil.findMatchingVidMode(newFullscreenMode);
				if(glfwVidMode == null) {
					FullscreenFix.print("Could not find matching GLFW VideoMode: " + newFullscreenMode.getWidth() + " x " + newFullscreenMode.getHeight() + " @ " + newFullscreenMode.getRefreshRate() + "hz");
					FullscreenFix.setFullscreenVideoMode(null);
				}else {
					FullscreenFix.setFullscreenVideoMode(new VideoMode(glfwGetPrimaryMonitor(), glfwVidMode));	
				}
			}
		}
		
		final Window window = (Window)(Object)this;
		final long handle = getHandle();
		
		if(FullscreenFix.OS_WINDOWS) {
			Win32Util.updateWindowState(window, windowPosX, windowPosY, windowWidth, windowHeight);
			FullscreenFix.windowNeedsUpdate = false;
			return;
		}
		
		glfwHideWindow(handle);

		VideoMode fullscreenMode = FullscreenFix.getFullscreenVideoMode();
		if(fullscreen && fullscreenMode != null) {
			FullscreenFix.print("Change to Fullscreen with custom resolution");
			
			MonitorInfo monitorInfo = new MonitorInfo(fullscreenMode.monitor);
			glfwSetWindowMonitor(window.getHandle(), 
					fullscreenMode.monitor,
					monitorInfo.posX,
					monitorInfo.posY,
					fullscreenMode.vidMode.width(),
					fullscreenMode.vidMode.height(),
					fullscreenMode.vidMode.refreshRate()
			);
		}else if(fullscreen && !FullscreenFix.isBorderlessEnabled()) {
			FullscreenFix.print("Change to Fullscreen");
			
			glfwSetWindowAttrib(handle, GLFW_AUTO_ICONIFY, FullscreenFix.isAutoMinimizeEnabled() ? 1 : 0);
			
			MonitorInfo monitor = MonitorInfo.getMonitor(window);
			GLFWUtil.enableFullscreen(window, monitor);
		}else {
			if(GLFWUtil.isFullscreen(window)) {
				GLFWUtil.disableFullscreen(window, windowPosX, windowPosY, windowWidth, windowHeight);	
			}
			
			if(fullscreen && FullscreenFix.isBorderlessEnabled()) {
				FullscreenFix.print("Change to Borderless Fullscreen");
				
				MonitorInfo monitor = MonitorInfo.getMonitor(window);
				
				glfwSetWindowAttrib(handle, GLFW_DECORATED, 0);
				glfwSetWindowPos(handle, monitor.posX, monitor.posY);
				glfwSetWindowSize(handle, monitor.width, monitor.height);
			}else {
				FullscreenFix.print("Change to Windowed");
				
				glfwSetWindowAttrib(handle, GLFW_DECORATED, 1);
				glfwSetWindowPos(handle, windowPosX, windowPosY);
				glfwSetWindowSize(handle, windowWidth, windowHeight);
			}
		}

		glfwShowWindow(handle);
		
		FullscreenFix.windowNeedsUpdate = false;
	}
	
	@Inject(method = "setFullscreenVideoMode", at = @At("HEAD"), cancellable = true)
	private void onSetFullscreenVideoMode(Optional<net.minecraft.client.util.VideoMode> optional, CallbackInfo ci) {
		FullscreenFix.print("Set Fullscreen Mode: " + optional);
		
		// This is called when the slider in the options menu is clicked.
		// We don't want to update the resolution until the menu is closed
		newFullscreenMode = optional.isPresent() ? optional.get() : null;
		fullscreenModeHasChanged = true;
	}
	
	@Shadow
	public abstract long getHandle();
	
}
