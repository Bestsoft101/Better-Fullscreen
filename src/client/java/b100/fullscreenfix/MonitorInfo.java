package b100.fullscreenfix;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import net.minecraft.client.util.Window;

public class MonitorInfo {
	
	public static MonitorInfo getMonitor(Window window) {
		return getMonitor(window.getHandle());
	}

	public static MonitorInfo getMonitor(long window) {
		int[] x = new int[1];
		int[] y = new int[1];
		glfwGetWindowPos(window, x, y);
		
		int[] w = new int[1];
		int[] h = new int[1];
		glfwGetWindowSize(window, w, h);
		
		return getMonitorAt(x[0] + w[0] / 2, y[0] + h[0] / 2);
	}
	
	public static MonitorInfo getMonitorAt(int x, int y) {
		PointerBuffer monitorPointers = glfwGetMonitors();
		
		for(int i = monitorPointers.position(); i < monitorPointers.limit(); i++) {
			long monitor = monitorPointers.get(i);
			
			MonitorInfo info = new MonitorInfo(monitor);
			if(x >= info.posX && y >= info.posY && x < info.posX + info.width && y < info.posY + info.height) {
				return info;
			}
		}
		
		return new MonitorInfo(glfwGetPrimaryMonitor());
	}
	
	public final long handle;
	public final int posX;
	public final int posY;
	public final int width;
	public final int height;
	public final int refreshRate;
	
	public MonitorInfo(long monitor) {
		this.handle = monitor;
		
		GLFWVidMode vidmode = glfwGetVideoMode(monitor);
		
		int[] x = new int[1];
		int[] y = new int[1];
		glfwGetMonitorPos(monitor, x, y);
		posX = x[0];
		posY = y[0];
		
		width = vidmode.width();
		height = vidmode.height();
		refreshRate = vidmode.refreshRate();
	}

}
