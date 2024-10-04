package b100.gui.config;

import java.util.function.Consumer;

import b100.gui.GuiElement;

public interface ConfigElement<E> {
	
	public boolean isChanged();
	
	public boolean isDefaultValue();
	
	public void resetToInitialValue();
	
	public void resetToDefaultValue();
	
	public void save();
	
	public GuiElement addConfigElementListener(ConfigElementListener listener);
	
	public boolean removeConfigElementListener(ConfigElementListener listener);
	
	public GuiElement addSaveConsumer(Consumer<E> saveListener);
	
	public boolean removeSaveConsumer(Consumer<E> saveListener);

}
