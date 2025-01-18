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
	public static final OperatingSystem OS;
	public static final boolean MOD_ENABLED;
	
	static {
		final MutableObject<Boolean> enabled = new MutableObject<>(true);
		final MutableObject<Boolean> ignoreOS = new MutableObject<>(false);
		
		ConfigUtil.loadConfig(Global.CONFIG_FILE, (key, value) -> {
			if(key.equals("enableMod")) {
				enabled.setValue(value.equalsIgnoreCase("true"));
			}
			if(key.equals("ignoreOS")) {
				ignoreOS.setValue(value.equalsIgnoreCase("true"));
			}
		}, ':');
		
		MOD_ENABLED = enabled.getValue();
		if(!MOD_ENABLED) {
			print("Mod disabled in config!");
		}
		
		if(ignoreOS.getValue()) {
			print("Ignore OS is enabled. Any operating system specific fixes or optimizations will be disabled!");
			OS = OperatingSystem.OTHER;
		}else {
			OS = getOS();
		}
		
		debugPrint("OS: " + OS);
	}
	
	private static OperatingSystem getOS() {
		String osname = System.getProperty("os.name").toLowerCase();
		if(osname.contains("windows")) {
			return OperatingSystem.WINDOWS;
		}else if(osname.contains("mac")) {
			return OperatingSystem.MAC;
		}
		return OperatingSystem.OTHER;
	}
	
	public static boolean isWindows() {
		return OS == OperatingSystem.WINDOWS;
	}
	
	public static boolean isMac() {
		return OS == OperatingSystem.MAC;
	}
	
	static void print(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFix] " + string + "\n");
		}else {
			LOGGER.info("[FullscreenFix] " + string);	
		}
	}
	
	static void debugPrint(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFixDebug] " + string + "\n");
		}
	}

}
