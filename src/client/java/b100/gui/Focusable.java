package b100.gui;

public interface Focusable {
	
	/**
	 * Set focused or unfocused and notify listeners if the state changed
	 */
	public void setFocused(boolean focused);
	
	/**
	 * Is the element focused
	 */
	public boolean isFocused();
	
	/**
	 * Elements like buttons should not be focusable when they are disabled
	 */
	public boolean isFocusable();
	
	/**
	 * Add a FocusListener, returns itself
	 */
	public GuiElement addFocusListener(FocusListener focusListener);
	
	/**
	 * Remove a FocusListener, returns if the listener was removed
	 */
	public boolean removeFocusListener(FocusListener focusListener);

}
