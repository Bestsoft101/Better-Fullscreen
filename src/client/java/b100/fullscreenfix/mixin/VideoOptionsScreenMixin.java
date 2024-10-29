package b100.fullscreenfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import b100.fullscreenfix.FullscreenFix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

@Mixin(value = VideoOptionsScreen.class)
public abstract class VideoOptionsScreenMixin extends GameOptionsScreen {

	// no
	private VideoOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
		super(parent, gameOptions, title);
	}
	
	@SuppressWarnings("resource")
	@ModifyArg(method = "addOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/OptionListWidget;addAll([Lnet/minecraft/client/option/SimpleOption;)V"), index = 0)
	private SimpleOption<?>[] replaceFullscreenOption(SimpleOption<?>[] options) {
		if(FullscreenFix.shouldReplaceVideoSettingsButton()) {
			SimpleOption<?> fullscreenOption = MinecraftClient.getInstance().options.getFullscreen();
			for(int i=0; i < options.length; i++) {
				SimpleOption<?> option = options[i];
				if(option == fullscreenOption) {
					options[i] = FullscreenFix.fullscreenOption;
				}
			}	
		}
		return options;
	}
	
	@Inject(method = "close", at = @At(value = "TAIL"))
	private void onClose(CallbackInfo ci) {
		if(FullscreenFix.fullscreenModeWasChanged) {
			FullscreenFix.print("Save Config");
			FullscreenFix.saveConfig();
			FullscreenFix.fullscreenModeWasChanged = false;
		}
	}
}
