package b100.gui;

import org.lwjgl.glfw.GLFW;

public enum FocusDirection {
	
	NEXT_ELEMENT(true, true, false, false),
	PREV_ELEMENT(false, true, false, false),
	UP(false, false, false, true),
	DOWN(true, false, false, true),
	LEFT(false, false, false, true),
	RIGHT(true, false, false, true),
	HOME(false, false, true, false),
	END(true, false, true, false);
	
	private boolean forwards;
	private boolean tab;
	private boolean listNavigation;
	private boolean isArrowKey;
	
	private FocusDirection(boolean forwards, boolean tab, boolean listNavigation, boolean isArrowKey) {
		this.forwards = forwards;
		this.tab = tab;
		this.listNavigation = listNavigation;
		this.isArrowKey = isArrowKey;
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
	
	public boolean isArrowKey() {
		return isArrowKey;
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
