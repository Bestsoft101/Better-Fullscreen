package b100.fullscreenfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;

import b100.fullscreenfix.FullscreenFix;
import net.minecraft.client.Minecraft;

@Mixin(value = Minecraft.class)
public class MinecraftMixin {
	
	@WrapOperation(
		method = "handleGlobalKeyPress",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;toggleFullscreen()V"
		)
	)
	private void openMenuInsteadOfTogglingFullscreen(Minecraft instance, Operation<Void> original, @Local(argsOnly = true) InputConstants.Key key, @Local(argsOnly = true) boolean controlDown) {
		if(controlDown && FullscreenFix.CONFIG_SCREEN_HOTKEY_ENABLED.getBoolean()) {
			FullscreenFix.openConfigScreen();
			return;
		}
		original.call(instance);
	}
	
}
