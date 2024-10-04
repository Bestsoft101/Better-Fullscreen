package b100.gui.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import b100.fullscreenfix.FullscreenFix;
import b100.gui.ActionListener;
import b100.gui.GuiButton;
import b100.gui.GuiContainer;
import b100.gui.GuiElement;
import b100.gui.GuiScreen;
import b100.gui.ScreenListener;
import net.minecraft.text.Text;

public class CustomOptionElement<E> extends GuiContainer implements ActionListener, ScreenListener {

	protected GuiScreen screen;
	protected Text name;
	protected E value;
	protected GuiButton button;
	protected Function<E, Text> toTextFunction;
	protected Text tooltipText;
	
	private final List<ActionListener> actionListeners = new ArrayList<>();
	
	public CustomOptionElement(GuiScreen screen, String key, E value) {
		this.screen = screen;
		this.name = FullscreenFix.translate(key);
		this.value = value;

		String tooltipText = FullscreenFix.translateIfExists(key + ".tooltip");
		if(tooltipText != null) {
			this.tooltipText = Text.of(tooltipText);
		}
		
		button = add(new GuiButton(screen, null).addActionListener(this));
		button.setSize(112, 20);
		
		setSize(320, 24);
		
		update();
	}
	
	@Override
	public void draw() {
		GuiElement mouseOver = button.screen.getMouseOver();
		if(mouseOver == this || contains(mouseOver)) {
			utils.drawRectangle(posX, posY, width, height, 0x20FFFFFF);
			
			if(tooltipText != null) {
				screen.setTooltip(tooltipText);
			}
		}
		
		super.draw();
		
		utils.drawString(name, posX + 8, posY + height / 2 - 4, 0xFFFFFF, true);
	}

	@Override
	public void actionPerformed(GuiElement source) {
		for(ActionListener actionListener : actionListeners) {
			actionListener.actionPerformed(this);
		}
	}
	
	@Override
	public void onResize() {
		button.setPosition(posX + width - button.width - 2, posY + height / 2 - button.height / 2);
		
		super.onResize();
	}
	
	public void update() {
		if(toTextFunction != null) {
			button.text = toTextFunction.apply(value);	
		}else {
			button.text = Text.of(String.valueOf(value));
		}
	}
	
	public CustomOptionElement<E> setValue(E value) {
		this.value = value;
		return this;
	}
	
	public E getValue() {
		return value;
	}
	
	public CustomOptionElement<E> setTooltipText(Text tooltipText) {
		this.tooltipText = tooltipText;
		return this;
	}
	
	public Text getTooltipText() {
		return tooltipText;
	}
	
	public CustomOptionElement<E> setToTextFunction(Function<E, Text> toTextFunction) {
		this.toTextFunction = toTextFunction;
		update();
		return this;
	}
	
	public Function<E, Text> getToTextFunction() {
		return toTextFunction;
	}
	
	public CustomOptionElement<E> addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
		return this;
	}
	
	public boolean removeActionListener(ActionListener actionListener) {
		return actionListeners.remove(actionListener);
	}

	@Override
	public void onScreenOpened(GuiScreen screen) {
		update();
	}
	
	@Override
	public boolean isSolid() {
		return true;
	}

}
