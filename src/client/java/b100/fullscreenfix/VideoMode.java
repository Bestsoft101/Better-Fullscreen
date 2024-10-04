package b100.fullscreenfix;

import org.lwjgl.glfw.GLFWVidMode;

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
		throw new RuntimeException("Not Implemented!");
	}
	
	@Override
	public String toString() {
		return vidMode.width() + " x " + vidMode.height() + " @ " + vidMode.refreshRate() + "hz";
	}
	
	public void toConfigString() {
		throw new RuntimeException("Not Implemented!");
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
