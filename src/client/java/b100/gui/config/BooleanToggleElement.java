package b100.gui.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import b100.fullscreenfix.FullscreenFix;
import b100.gui.ActionListener;
import b100.gui.GuiButton;
import b100.gui.GuiContainer;
import b100.gui.GuiElement;
import b100.gui.GuiScreen;
import b100.gui.ScreenListener;
import net.minecraft.text.Text;

public class BooleanToggleElement extends GuiContainer implements ActionListener, ConfigElement<Boolean>, ScreenListener {
	
	protected GuiScreen screen;
	protected Text name;
	protected boolean initialValue;
	protected boolean value;
	protected boolean defaultValue;
	protected GuiButton button;
	protected Text tooltipText;
	
	private final List<ConfigElementListener> configElementListeners = new ArrayList<>();
	private final List<Consumer<Boolean>> saveConsumers = new ArrayList<>();
	
	public BooleanToggleElement(GuiScreen screen, String key, boolean value) {
		this.screen = screen;
		this.name = FullscreenFix.translate(key);
		this.value = initialValue = defaultValue = value;
		
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
		value = !value;
		
		for(ConfigElementListener configElementListener : configElementListeners) {
			configElementListener.valueChanged(this);
		}
		
		update();
	}
	
	@Override
	public void onResize() {
		button.setPosition(posX + width - button.width - 2, posY + height / 2 - button.height / 2);
		
		super.onResize();
	}
	
	public void update() {
		button.text = Text.of(value ? "\247a" + FullscreenFix.translateToString("value.yes") : "\247c" + FullscreenFix.translateToString("value.no"));
	}
	
	public boolean getValue() {
		return value;
	}
	
	public BooleanToggleElement setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public boolean getDefaultValue() {
		return defaultValue;
	}
	
	public BooleanToggleElement setTooltipText(Text tooltipText) {
		this.tooltipText = tooltipText;
		return this;
	}
	
	public Text getTooltipText() {
		return tooltipText;
	}

	@Override
	public boolean isChanged() {
		return value != initialValue;
	}

	@Override
	public GuiElement addConfigElementListener(ConfigElementListener listener) {
		configElementListeners.add(listener);
		return this;
	}

	@Override
	public boolean removeConfigElementListener(ConfigElementListener listener) {
		return configElementListeners.remove(listener);
	}

	@Override
	public void resetToInitialValue() {
		value = initialValue;
	}

	@Override
	public void resetToDefaultValue() {
		value = defaultValue;
	}

	@Override
	public boolean isDefaultValue() {
		return value == defaultValue;
	}

	@Override
	public void save() {
		if(initialValue != value) {
			initialValue = value;
		}
		for(Consumer<Boolean> saveConsumer : saveConsumers) {
			saveConsumer.accept(value);
		}
	}

	@Override
	public GuiElement addSaveConsumer(Consumer<Boolean> saveListener) {
		saveConsumers.add(saveListener);
		return this;
	}

	@Override
	public boolean removeSaveConsumer(Consumer<Boolean> saveListener) {
		return saveConsumers.remove(saveListener);
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
