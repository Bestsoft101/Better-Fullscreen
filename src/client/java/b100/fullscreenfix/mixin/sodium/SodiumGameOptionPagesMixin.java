package b100.fullscreenfix.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.SodiumCompat;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.Option;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class SodiumGameOptionPagesMixin {
	
	@ModifyArg(
			method = "general",
			at = @At(
					value = "INVOKE", ordinal = 0,
					target = "Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;add(Lme/jellysquid/mods/sodium/client/gui/options/Option;)Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;"
			),
			slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.fullscreen"))
	)
	private static Option<?> replaceFullscreenButton(Option<?> button) {
		if(FullscreenFix.REPLACE_VIDEO_SETTINGS_BUTTON.getBoolean()) {
			return SodiumCompat.getCustomFullscreenButton();	
		}
		return button;
	}
	
}
