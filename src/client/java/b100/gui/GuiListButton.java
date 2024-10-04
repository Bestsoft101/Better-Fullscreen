package b100.gui;

import java.util.ArrayList;
import java.util.List;

public class GuiListButton extends GuiElement implements Focusable {
	
	public GuiScreen screen;
	
	private boolean focused = false;
	
	private final List<FocusListener> focusListeners = new ArrayList<>();
	
	public int outlineColor = 0xFF808080;
	public int fillColor = 0xFF000000;
	
	public GuiListButton(GuiScreen screen) {
		this.screen = screen;
		
		this.width = 200;
		this.height = 20;
	}
	
	@Override
	public void draw() {
		if(isFocused()) {
			utils.drawRectangle(posX, posY, width, height, outlineColor);
			utils.drawRectangle(posX + 1, posY + 1, width - 2, height - 2, fillColor);	
		}
	}
	
	@Override
	public boolean mouseEvent(int button, boolean pressed, double mouseX, double mouseY) {
		if(pressed && screen.isMouseOver(this)) {
			setFocused(true);
			return true;
		}
		
		return super.mouseEvent(button, pressed, mouseX, mouseY);
	}
	
	@Override
	public void setFocused(boolean focused) {
		if(focused != this.focused) {
			this.focused = focused;
			for(FocusListener focusListener : focusListeners) {
				focusListener.focusChanged(this);
			}
		}
	}

	@Override
	public boolean isFocused() {
		return focused;
	}
	
	@Override
	public boolean isFocusable() {
		return true;
	}

	@Override
	public GuiListButton addFocusListener(FocusListener actionListener) {
		focusListeners.add(actionListener);
		return this;
	}

	@Override
	public boolean removeFocusListener(FocusListener actionListener) {
		return focusListeners.remove(actionListener);
	}

}
