package b100.fullscreenfix.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.util.InputUtil;

@Mixin(value = InputUtil.class)
public class InputUtilMixin {
	
//	@ModifyArg(
//		method = "setCursorParameters",
//		at = @At(
//			value = "INVOKE",
//			target = "Lorg/lwjgl/glfw/GLFW;glfwSetInputMode(JII)V"
//		),
//		index = 2
//	)
//	private static int modifyCursorParameter(int cursorMode) {
//		final int prevCursorMode = cursorMode;
//		cursorMode = GLFWUtil.getUpdatedCursorMode(cursorMode);
//		if(cursorMode != prevCursorMode) {
//			FullscreenFix.debugPrint("Modified Cursor Mode: " + GLFWUtil.getCursorModeString(prevCursorMode) + " -> " + GLFWUtil.getCursorModeString(cursorMode));	
//		}
//		return cursorMode;
//	}
}
