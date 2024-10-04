package b100.fullscreenfix;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import b100.fullscreenfix.gui.ConfigScreen;
import b100.fullscreenfix.mixin.access.IScreen;
import b100.gui.ScreenWrapper;

public class ModMenuIntegration implements ModMenuApi {
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new ScreenWrapper(new ConfigScreen((IScreen) parent));
	}
	
}
