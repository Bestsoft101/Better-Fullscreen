package b100.fullscreenfix.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.util.Window;

@Mixin(Window.class)
public interface WindowAccess {
	
	@Accessor("fullscreen")
	public void setFullscreen(boolean fullscreen);
	
	@Invoker("updateWindowRegion")
	public void invokeUpdateWindowRegion();
	
}
