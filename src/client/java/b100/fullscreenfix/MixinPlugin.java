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
	
	public MixinPlugin() {
		mixinsThatRequireModEnabled.add("b100.fullscreenfix.mixin.VideoOptionsScreenMixin");
		mixinsThatRequireModEnabled.add("b100.fullscreenfix.mixin.WindowMixin");
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
		if(mixinsThatRequireModEnabled.contains(mixinClassName)) {
			return FullscreenFix.isModEnabled();
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		
	}

	@Override
	public List<String> getMixins() {
		List<String> mixins = new ArrayList<>();
		if(FabricLoader.getInstance().isModLoaded("sodium") && FullscreenFix.isModEnabled()) {
			mixins.add("sodium.SodiumGameOptionPagesMixin");	
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
