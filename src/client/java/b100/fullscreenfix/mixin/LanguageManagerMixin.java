package b100.fullscreenfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import b100.fullscreenfix.FullscreenFix;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.resource.ResourceManager;

@Mixin(value = LanguageManager.class)
public class LanguageManagerMixin {
	
	@Inject(method = "reload", at = @At("HEAD"))
	private void onReload(ResourceManager resourceManager, CallbackInfo ci) {
		FullscreenFix.loadTranslations();
	}

}
