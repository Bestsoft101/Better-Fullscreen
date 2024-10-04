package b100.fullscreenfix.util;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import b100.fullscreenfix.MonitorInfo;
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

}
