package b100.fullscreenfix;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import b100.fullscreenfix.util.ConfigUtil;
import net.fabricmc.loader.api.FabricLoader;

/**
 * This class is used in the Mixin Plugin and should not load any Minecraft classes
 */
public class Global {
	
	public static final boolean INDEV = FabricLoader.getInstance().isDevelopmentEnvironment();
	public static final String MODID = "fullscreenfix";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final File CONFIG_FOLDER = Paths.get("config").toFile();
	public static final File CONFIG_FILE = new File(CONFIG_FOLDER, MODID + ".properties");
	public static final String MIXIN_PACKAGE = "b100." + MODID + ".mixin";
	public static final boolean OS_WINDOWS = isWindows();
	public static final boolean MOD_ENABLED = isModEnabled();
	
	static {
		if(!MOD_ENABLED) {
			print("Mod disabled in config!");	
		}
	}
	
	private static boolean isModEnabled() {
		final MutableObject<Boolean> enabled = new MutableObject<>(true);
		ConfigUtil.loadConfig(Global.CONFIG_FILE, (key, value) -> {
			if(key.equals("enableMod")) {
				enabled.setValue(value.equalsIgnoreCase("true"));
			}
		}, ':');
		return enabled.getValue();
	}
	
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	static void print(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFix] " + string + "\n");
		}else {
			LOGGER.info("[FullscreenFix] " + string);	
		}
	}

}
