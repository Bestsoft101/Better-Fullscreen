package b100.fullscreenfix;

import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.text.Text;

public class SodiumCompat {
	
	public static OptionStorageImpl optionStorage = new OptionStorageImpl();
	
	public static OptionImpl<Object, FullscreenMode> getCustomFullscreenButton() {
		return OptionImpl.createBuilder(FullscreenMode.class, optionStorage)
				.setName(FullscreenFix.translate("option.fullscreen"))
				.setTooltip(Text.translatable("sodium.options.fullscreen.tooltip"))
				.setControl(option1 -> new CyclingControl<>(option1, FullscreenMode.class, new Text[] {
						FullscreenFix.translate("option.fullscreen.off"),
						FullscreenFix.translate("option.fullscreen.on"),
						FullscreenFix.translate("option.fullscreen.borderless")
				}))
				.setBinding((options, value) -> FullscreenFix.setFullscreenMode(value), (options) -> FullscreenFix.getCurrentFullscreenMode())
				.build();
	}
	
	public static class OptionStorageImpl implements OptionStorage<Object> {

		@Override
		public Object getData() {
			return null;
		}

		@Override
		public void save() {
			FullscreenFix.saveConfig();
		}
		
	}
}
