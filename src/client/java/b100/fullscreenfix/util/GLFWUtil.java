package b100.fullscreenfix.util;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.MonitorInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;

public class GLFWUtil {
	
	public static void enableFullscreen(Window window, MonitorInfo monitor) {
		glfwSetWindowMonitor(window.getHandle(), monitor.handle, monitor.posX, monitor.posY, monitor.width, monitor.height, monitor.refreshRate);
	}
	
	public static void disableFullscreen(Window window, int x, int y, int w, int h) {
		glfwSetWindowMonitor(window.getHandle(), 0L, x, y, w, h, 0);
	}
	
	public static boolean isFullscreen(Window window) {
		return glfwGetWindowMonitor(window.getHandle()) != 0L;
	}
	
	public static List<Long> getMonitors() {
		List<Long> list = new ArrayList<>();
		PointerBuffer buffer = glfwGetMonitors();
		while(buffer.position() < buffer.limit()) {
			list.add(buffer.get());
		}
		return list;
	}
	
	public static List<GLFWVidMode> getVideoModes(long monitor) {
		List<GLFWVidMode> list = new ArrayList<>();
		GLFWVidMode.Buffer buffer = glfwGetVideoModes(monitor);
		while(buffer.hasRemaining()) {
			list.add(buffer.get());
		}
		return list;
	}
	
	public static GLFWVidMode findMatchingVidMode(VideoMode videoMode) {
		for(GLFWVidMode vidMode : GLFWUtil.getVideoModes(glfwGetPrimaryMonitor())) {
			if(vidMode.width() == videoMode.getWidth() && vidMode.height() == videoMode.getHeight() && vidMode.refreshRate() == videoMode.getRefreshRate()) {
				return vidMode;
			}
		}
		return null;
	}
	
	public static long findMonitor(int x, int y, int width, int height) {
		for(long monitor : getMonitors()) {
			MonitorInfo monitorInfo = new MonitorInfo(monitor);
			if(monitorInfo.posX == x
					&& monitorInfo.posY == y
					&& monitorInfo.width == width
					&& monitorInfo.height == height) {
				return monitor;
			}
		}
		return 0;
	}
	
	public static GLFWVidMode getMonitorVidMode(long monitor, int width, int height, int refreshRate) {
		for(GLFWVidMode vidMode : getVideoModes(monitor)) {
			if(vidMode.width() == width
					&& vidMode.height() == height
					&& vidMode.refreshRate() == refreshRate) {
				return vidMode;
			}
		}
		return null;
	}
	
	public static int getUpdatedCursorMode(int mode) {
//		if(mode == GLFW_CURSOR_NORMAL || mode == GLFW_CURSOR_CAPTURED) {
//			boolean isCaptured = mode == GLFW_CURSOR_CAPTURED;
//			boolean shouldBeCaptured = FullscreenFix.CAPTURE_CURSOR.getBoolean() && FullscreenFix.isFullscreenEnabled();
//			if(isCaptured != shouldBeCaptured) {
//				if(shouldBeCaptured) {
//					mode = GLFW_CURSOR_CAPTURED;
//				}else {
//					mode = GLFW_CURSOR_NORMAL;
//				}
//			}
//		}
//		return mode;
		return GLFW_CURSOR_NORMAL;
	}
	
	public static void updateCursorMode() {
		Window window = MinecraftClient.getInstance().getWindow();
		if(window == null) {
			return;
		}
		long windowHandle = window.getHandle();
		
		int cursorMode = glfwGetInputMode(windowHandle, GLFW_CURSOR);
		final int prevCursorMode = cursorMode;
		cursorMode = getUpdatedCursorMode(cursorMode);
		
		if(cursorMode != prevCursorMode) {
			FullscreenFix.debugPrint("Updated Cursor Mode: " + GLFWUtil.getCursorModeString(prevCursorMode) + " -> " + GLFWUtil.getCursorModeString(cursorMode));	
			glfwSetInputMode(windowHandle, GLFW_CURSOR, cursorMode);
		}
	}
	
	public static String getCursorModeString(int cursorMode) {
//		if(cursorMode == GLFW_CURSOR_CAPTURED) {
//			return "CAPTURED";
//		}else
		if(cursorMode == GLFW_CURSOR_NORMAL) {
			return "NORMAL";
		}else if(cursorMode == GLFW_CURSOR_HIDDEN) {
			return "HIDDEN";
		}else if(cursorMode == GLFW_CURSOR_DISABLED) {
			return "DISABLED";
		}
		return String.valueOf(cursorMode);
	}

}
