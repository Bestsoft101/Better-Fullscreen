package b100.fullscreenfix.gui;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.VideoMode;
import b100.fullscreenfix.mixin.access.IScreen;
import b100.gui.GuiButton;
import b100.gui.GuiContainer;
import b100.gui.GuiElement;
import b100.gui.GuiScrollListScreen;
import b100.gui.config.BooleanToggleElement;
import b100.gui.config.ConfigElement;
import b100.gui.config.CustomOptionElement;
import b100.gui.config.SaveConfigButton;
import net.minecraft.text.Text;

public class ConfigScreen extends GuiScrollListScreen {
	
	public GuiButton cancelButton;
	public SaveConfigButton saveConfigButton;

	private CustomOptionElement<VideoMode> fullscreenResolutionButton;
	
	public ConfigScreen(IScreen parentScreen) {
		super(parentScreen);
		
		title = FullscreenFix.translate("screen.fullscreenSettings.title");
	}
	
	@Override
	public void onInit() {
		saveConfigButton = new SaveConfigButton(this);
		saveConfigButton.addActionListener((e) -> {
			FullscreenFix.saveConfig();
			back();
		});
		
		super.onInit();
		
		cancelButton = add(new GuiButton(this, FullscreenFix.translate("button.cancel")).addActionListener((e) -> back()));
		add(saveConfigButton);
	}
	
	@Override
	public void initScrollElements() {
		scrollList.add(new BooleanToggleElement(this, "option.enableMod", FullscreenFix.isModEnabled()).addSaveConsumer(newValue -> FullscreenFix.setModEnabled(newValue)));
		
		if(FullscreenFix.isModEnabled()) {
			scrollList.add(new BooleanToggleElement(this, "option.fullscreen", FullscreenFix.isFullscreenEnabled()).addSaveConsumer(newValue -> FullscreenFix.setFullscreen(newValue)));
			scrollList.add(new BooleanToggleElement(this, "option.borderlessFullscreen", FullscreenFix.isBorderlessEnabled()).addSaveConsumer(newValue -> FullscreenFix.setBorderless(newValue)));
			
			if(FullscreenFix.OS_WINDOWS) {
				scrollList.add(new BooleanToggleElement(this, "option.windowsFullscreenOptimizations", FullscreenFix.isWindowsFullscreenOptimizationsEnabled()).addSaveConsumer(newValue -> FullscreenFix.setWindowsFullscreenOptimizations(newValue)));
			}
			
			scrollList.add(new BooleanToggleElement(this, "option.autoMinimize", FullscreenFix.isAutoMinimizeEnabled()).addSaveConsumer(newValue -> FullscreenFix.setAutoMinimize(newValue)));
			
			fullscreenResolutionButton = scrollList.add(new CustomOptionElement<>(this, "option.fullscreenResolution", FullscreenFix.getFullscreenVideoMode())
			.addActionListener((e) -> utils.setScreen(new ScreenResolutionsMenu(this)))
			.setToTextFunction((videoMode) -> videoMode != null ? Text.of(videoMode.toString()) : FullscreenFix.translate("value.fullscreenResolution.default")));
		}
	}
	
	@Override
	public void elementAdded(GuiContainer parent, GuiElement element) {
		if(element instanceof ConfigElement) {
			ConfigElement<?> configElement = (ConfigElement<?>) element;
			configElement.addConfigElementListener(saveConfigButton);
		}
		super.elementAdded(parent, element);
	}
	
	@Override
	public void onResize() {
		setDoubleFooterButtonPositions(cancelButton, saveConfigButton);
		super.onResize();
	}
	
	@Override
	public void onScreenOpened() {
		fullscreenResolutionButton.setValue(FullscreenFix.getFullscreenVideoMode());
		super.onScreenOpened();
	}
}
