package b100.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiButton extends GuiElement implements Focusable {
	
	/**
	 * The screen that is button is in
	 */
	public GuiScreen screen;
	
	/**
	 * The text of this button. Can be null.
	 */
	public Text text;
	
	/**
	 * Should the button be clickable or not. When it is not clickable, its still visible but grayed out
	 */
	private boolean clickable = true;
	
	/**
	 * When the button is focused it can be clicked with Space and Enter
	 */
	private boolean focused = false;
	
	private final List<ActionListener> actionListeners = new ArrayList<>();
	private final List<FocusListener> focusListeners = new ArrayList<>();
	
	public GuiButton(GuiScreen screen, Text text) {
		this.screen = screen;
		this.text = text;
		
		this.width = 200;
		this.height = 20;
	}
	
	@Override
	public void draw() {
		Identifier texture;
		
		if(clickable) {
			if(focused || screen.isMouseOver(this)) {
				texture = Textures.INSTANCE.buttonHover;
			}else {
				texture = Textures.INSTANCE.buttonNormal;
			}
		}else {
			texture = Textures.INSTANCE.buttonDisabled;
		}
		
		utils.drawGuiTexture(texture, posX, posY, width, height);
		
		if(text != null) {
			int textWidth = utils.textRenderer.getWidth(text);
			int textX = posX + (width - textWidth) / 2;
			int textY = posY + height / 2 - 4;
			utils.drawString(text, textX, textY, 0xFFFFFF, true);
		}
	}
	
	@Override
	public boolean keyEvent(int key, int scancode, int modifiers, boolean pressed) {
		if(pressed && focused && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE)) {
			clickButton();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseEvent(int button, boolean pressed, double mouseX, double mouseY) {
		if(clickable && pressed && screen.isMouseOver(this)) {
			clickButton();
			return true;
		}
		
		return super.mouseEvent(button, pressed, mouseX, mouseY);
	}
	
	public void clickButton() {
		utils.playSound(SoundEvents.UI_BUTTON_CLICK);
		for(ActionListener actionListener : actionListeners) {
			actionListener.actionPerformed(this);
		}
	}
	
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}
	
	public boolean isClickable() {
		return clickable;
	}
	
	public GuiButton addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
		return this;
	}
	
	public boolean removeActionListener(ActionListener actionListener) {
		return actionListeners.remove(actionListener);
	}

	@Override
	public GuiButton addFocusListener(FocusListener actionListener) {
		focusListeners.add(actionListener);
		return this;
	}

	@Override
	public boolean removeFocusListener(FocusListener actionListener) {
		return focusListeners.remove(actionListener);
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
		return clickable;
	}

}
