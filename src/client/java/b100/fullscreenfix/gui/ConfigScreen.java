package b100.fullscreenfix.gui;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.Global;
import b100.fullscreenfix.VideoMode;
import b100.lib.client.gui.config.SaveConfigButton;
import b100.lib.client.gui.config.base.ConfigElement;
import b100.lib.client.gui.config.element.BooleanToggleElement;
import b100.lib.client.gui.config.element.CustomOptionElement;
import b100.lib.client.gui.element.GuiButton;
import b100.lib.client.gui.element.GuiContainer;
import b100.lib.client.gui.element.GuiElement;
import b100.lib.client.gui.screen.GuiScrollListScreen;
import b100.lib.client.mixin.IScreen;
import b100.lib.client.util.UpdateMode;
import net.minecraft.text.Text;

public class ConfigScreen extends GuiScrollListScreen {
	
	public GuiButton cancelButton;
	public SaveConfigButton saveConfigButton;

	private CustomOptionElement<VideoMode> fullscreenResolutionButton;
	
	public ConfigScreen(IScreen parentScreen) {
		super(parentScreen);
		
		title = FullscreenFix.TRANS.asText("screen.fullscreenSettings.title");
	}
	
	@Override
	public void onInit() {
		saveConfigButton = new SaveConfigButton(this);
		saveConfigButton.addActionListener((e) -> {
			FullscreenFix.CONFIG.save();
			back();
		});
		
		super.onInit();
		
		cancelButton = add(new GuiButton(this, FullscreenFix.TRANS.asText("button.cancel")).addActionListener((e) -> back()));
		add(saveConfigButton);
	}
	
	@Override
	public void initScrollElements() {
		scrollList.add(BooleanToggleElement.create(this, "option.enableMod", FullscreenFix.ENABLE_NEXT_LAUNCH, UpdateMode.ON_SAVE));
		
		if(Global.MOD_ENABLED) {
			scrollList.add(BooleanToggleElement.create(this, "option.fullscreen", FullscreenFix.FULLSCREEN, UpdateMode.ON_SAVE));
			scrollList.add(BooleanToggleElement.create(this, "option.borderlessFullscreen", FullscreenFix.BORDERLESS_FULLSCREEN, UpdateMode.ON_SAVE));
			
			if(Global.OS_WINDOWS) {
				scrollList.add(BooleanToggleElement.create(this, "option.windowsFullscreenOptimizations", FullscreenFix.FULLSCREEN_OPTIMIZATIONS, UpdateMode.ON_SAVE));
			}

//			scrollList.add(BooleanToggleElement.create(this, "option.captureCursorInFullscreen", FullscreenFix.CAPTURE_CURSOR, UpdateMode.ON_SAVE));
			scrollList.add(BooleanToggleElement.create(this, "option.autoMinimize", FullscreenFix.AUTO_MINIMIZE, UpdateMode.ON_SAVE));
			scrollList.add(BooleanToggleElement.create(this, "option.startInFullscreen", FullscreenFix.START_IN_FULLSCREEN, UpdateMode.ON_SAVE));
			
			fullscreenResolutionButton = scrollList.add(new CustomOptionElement<>(this, "option.fullscreenResolution", FullscreenFix.FULLSCREEN_VIDEO_MODE.get())
			.addActionListener((e) -> utils.setScreen(new ScreenResolutionsMenu(this)))
			.setToTextFunction((videoMode) -> videoMode != null ? Text.of(videoMode.toString()) : FullscreenFix.TRANS.asText("value.fullscreenResolution.default")));

			scrollList.add(BooleanToggleElement.create(this, "option.replaceVideoSettings", FullscreenFix.REPLACE_VIDEO_SETTINGS_BUTTON, UpdateMode.ON_SAVE));
			scrollList.add(BooleanToggleElement.create(this, "option.configScreenHotkeyEnabled", FullscreenFix.CONFIG_SCREEN_HOTKEY_ENABLED, UpdateMode.ON_SAVE));
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
		setDoubleFooterButtonPositions(saveConfigButton, cancelButton);
		super.onResize();
	}
	
	@Override
	public void onScreenOpened() {
		if(fullscreenResolutionButton != null) {
			fullscreenResolutionButton.setValue(FullscreenFix.FULLSCREEN_VIDEO_MODE.get());
		}
		super.onScreenOpened();
	}
}
