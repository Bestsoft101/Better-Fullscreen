package b100.gui;

import java.util.function.Function;

public interface Focusable {
	
	public static final Function<GuiElement, Boolean> FOCUSABLE_CONDITION = (element) -> isFocusable(element);
	
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
	
	public GuiContainer getContainer();
	
	public static boolean isFocusable(GuiElement element) {
		if(element instanceof Focusable) {
			Focusable focusable = (Focusable) element;
			return focusable.isFocusable();
		}
		return false;
	}
	
	/**
	 * Recursively search containers for the next focusable element
	 */
	public static Focusable findNextFocusableElement(GuiElement element, FocusDirection direction) {
		GuiContainer container = element.getContainer();
		if(container == null) {
			return null;
		}
		
		// Check for next focusable in container
		Focusable next = container.getNextFocusable(element, direction);
		if(next != null) {
			return next;
		}
		
		return findNextFocusableElement(container, direction);
	}

}
