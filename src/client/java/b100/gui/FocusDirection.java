package b100.gui;

import org.lwjgl.glfw.GLFW;

public enum FocusDirection {
	
	NEXT_ELEMENT(true, true, false),
	PREV_ELEMENT(false, true, false),
	UP(false, false, false),
	DOWN(true, false, false),
	LEFT(false, false, false),
	RIGHT(true, false, false),
	HOME(false, false, true),
	END(true, false, true);
	
	private boolean forwards;
	private boolean tab;
	private boolean listNavigation;
	
	private FocusDirection(boolean forwards, boolean tab, boolean listNavigation) {
		this.forwards = forwards;
		this.tab = tab;
		this.listNavigation = listNavigation;
	}
	
	public boolean isForwards() {
		return forwards;
	}
	
	public boolean isTab() {
		return tab;
	}
	
	public boolean isListNavigation() {
		return listNavigation;
	}
	
	public static FocusDirection get(int keyCode, int modifiers) {
		boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) > 0;
		if(keyCode == GLFW.GLFW_KEY_TAB) return shift ? PREV_ELEMENT : NEXT_ELEMENT;
		if(keyCode == GLFW.GLFW_KEY_UP) return UP;
		if(keyCode == GLFW.GLFW_KEY_DOWN) return DOWN;
		if(keyCode == GLFW.GLFW_KEY_LEFT) return LEFT;
		if(keyCode == GLFW.GLFW_KEY_RIGHT) return RIGHT;
		if(keyCode == GLFW.GLFW_KEY_HOME) return HOME;
		if(keyCode == GLFW.GLFW_KEY_END) return END;
		return null;
	}
	
}
