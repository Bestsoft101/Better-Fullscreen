package b100.fullscreenfix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class MixinPlugin implements IMixinConfigPlugin {

	public Set<String> mixinsThatRequireModEnabled = new HashSet<>();
	public Set<String> sodiumMixins = new HashSet<>();
	
	public MixinPlugin() {
		mixinsThatRequireModEnabled.add("VideoOptionsScreenMixin");
		mixinsThatRequireModEnabled.add("WindowMixin");
		
		sodiumMixins.add("sodium.SodiumGameOptionPagesMixin");
	}
	
	@Override
	public void onLoad(String mixinPackage) {
		
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(mixinsThatRequireModEnabled.contains(Global.MIXIN_PACKAGE + "." + mixinClassName)) {
			return Global.MOD_ENABLED;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		
	}

	@Override
	public List<String> getMixins() {
		List<String> mixins = new ArrayList<>();
		if(FabricLoader.getInstance().isModLoaded("sodium") && Global.MOD_ENABLED) {
			mixins.addAll(sodiumMixins);
		}
		return mixins;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		
	}
}
