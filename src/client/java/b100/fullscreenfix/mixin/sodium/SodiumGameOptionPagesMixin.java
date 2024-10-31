package b100.fullscreenfix.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.SodiumCompat;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public class SodiumGameOptionPagesMixin {
	
	@ModifyArg(
			method = "general",
			at = @At(
					value = "INVOKE", ordinal = 0,
					target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;add(Lnet/caffeinemc/mods/sodium/client/gui/options/Option;)Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;"
			),
			slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.fullscreen"))
	)
	private static Option<?> replaceFullscreenButton(Option<?> button) {
		if(FullscreenFix.shouldReplaceVideoSettingsButton()) {
			return SodiumCompat.getCustomFullscreenButton();	
		}
		return button;
	}
	
	@WrapOperation(
			method = "general",
			at = @At(
					value = "INVOKE", ordinal = 0,
					target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;add(Lnet/caffeinemc/mods/sodium/client/gui/options/Option;)Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;"
			),
			slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=options.fullscreen.resolution"))
	)
	private static OptionGroup.Builder removeFullscreenResolutionButton(OptionGroup.Builder instance, Option<?> option, Operation<OptionGroup.Builder> operation) {
		return instance;
	}
	
}
