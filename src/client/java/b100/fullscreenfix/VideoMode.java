package b100.fullscreenfix;

import org.lwjgl.glfw.GLFWVidMode;

import b100.fullscreenfix.util.GLFWUtil;

public class VideoMode {
	
	public final long monitor;
	public final GLFWVidMode vidMode;
	
	public VideoMode(long monitor, GLFWVidMode vidMode) {
		this.monitor = monitor;
		this.vidMode = vidMode;
	}
	
	public static VideoMode parse(String string) {
		string = string.trim();
		if(string.length() == 0) {
			return null;
		}
		
		int x = 0;
		int y = 0;
		int monitorWidth = 0;
		int monitorHeight = 0;
		int width = 0;
		int height = 0;
		int refreshRate = 0;
		
		String[] str = string.split(";");
		for(int i=0; i < str.length; i++) {
			String entry = str[i];
			int j = entry.indexOf(':');
			String key = entry.substring(0, j);
			int value = Integer.parseInt(entry.substring(j + 1));
			
			if(key.equals("x")) {
				x = value;
			}else if(key.equals("y")) {
				y = value;
			}else if(key.equals("monitorWidth")) {
				monitorWidth = value;
			}else if(key.equals("monitorHeight")) {
				monitorHeight = value;
			}else if(key.equals("width")) {
				width = value;
			}else if(key.equals("height")) {
				height = value;
			}else if(key.equals("refreshRate")) {
				refreshRate = value;
			}
		}
		
		long monitor = GLFWUtil.findMonitor(x, y, monitorWidth, monitorHeight);
		if(monitor == 0) {
			FullscreenFix.print("Could not find monitor " + monitorWidth + " x " + monitorHeight + " at " + x + ", " + y);
			FullscreenFix.print("Available monitors: ");
			for(long m : GLFWUtil.getMonitors()) {
				MonitorInfo info = new MonitorInfo(m);
				FullscreenFix.print("    " + info.width + " x " + info.height + " at " + info.posX + ", " + info.posY);
			}
			
			return null;
		}
		
		GLFWVidMode vidMode = GLFWUtil.getMonitorVidMode(monitor, width, height, refreshRate);
		if(vidMode == null) {
			FullscreenFix.print("Could not find matching vidmode: " + width + " x " + height + " @ " + refreshRate + "hz");
			return null;
		}
		
		return new VideoMode(monitor, vidMode);
	}
	
	@Override
	public String toString() {
		return vidMode.width() + " x " + vidMode.height() + " @ " + vidMode.refreshRate() + "hz";
	}
	
	public String toConfigString() {
		MonitorInfo monitorInfo = new MonitorInfo(monitor);
		return "x:" + monitorInfo.posX
				+ ";y:" + monitorInfo.posY
				+ ";monitorWidth:" + monitorInfo.width
				+ ";monitorHeight:" + monitorInfo.height
				+ ";width:" + vidMode.width()
				+ ";height:" + vidMode.height()
				+ ";refreshRate:" + vidMode.refreshRate();
	}
	
	public static boolean compare(VideoMode o1, VideoMode o2) {
		if(o1 == null && o2 == null) {
			return true;
		}
		if(o1 == null || o2 == null) {
			return false;
		}
		return o1.monitor == o2.monitor && o1.vidMode.equals(o2.vidMode);
	}
	
}
