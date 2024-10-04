package b100.fullscreenfix;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import b100.fullscreenfix.mixin.access.WindowAccess;
import b100.fullscreenfix.util.ConfigUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FullscreenFix {
	
	public static final String MODID = "fullscreenfix";
	public static final boolean OS_WINDOWS = isWindows();
	
	private static Window window;
	
	// Config
	
	private static File configFolder = Paths.get("config").toFile();
	private static File configFile = new File(configFolder, MODID + ".properties");
	
	public static boolean windowNeedsUpdate = false;
	
	private static boolean enableMod = true;
	private static boolean borderlessFullscreen = true;
	private static boolean fullscreenOptimizations = true;
	private static boolean autoMinimize = false;
	
	/**
	 * May be null for current resolution
	 */
	private static VideoMode fullscreenVideoMode;
	
	private static final Map<String, String> translations = new HashMap<>();
	
	static {
		loadConfig();
	}
	
	public static boolean isModEnabled() {
		return enableMod;
	}
	
	public static void setModEnabled(boolean value) {
		enableMod = value;
	}
	
	public static boolean isBorderlessEnabled() {
		return borderlessFullscreen;
	}
	
	public static void setBorderless(boolean value) {
		if(value != borderlessFullscreen) {
			borderlessFullscreen = value;
			updateWindow();
		}
	}
	
	public static boolean isWindowsFullscreenOptimizationsEnabled() {
		return fullscreenOptimizations;
	}
	
	public static void setWindowsFullscreenOptimizations(boolean value) {
		if(value != fullscreenOptimizations) {
			fullscreenOptimizations = value;
			updateWindow();
		}
	}
	
	public static boolean isFullscreenEnabled() {
		return window.isFullscreen();
	}
	
	public static void setFullscreen(boolean value) {
		WindowAccess access = (WindowAccess)(Object)window;
		access.setFullscreen(value);
	}
	
	public static VideoMode getFullscreenVideoMode() {
		return fullscreenVideoMode;
	}
	
	public static void setFullscreenVideoMode(VideoMode value) {
		if(!VideoMode.compare(fullscreenVideoMode, value)) {
			fullscreenVideoMode = value;
			updateWindow();
		}
	}
	
	public static boolean isAutoMinimizeEnabled() {
		return autoMinimize;
	}
	
	public static void setAutoMinimize(boolean value) {
		if(autoMinimize != value) {
			autoMinimize = value;
			updateWindow();
		}
	}
	
	public static void updateWindow() {
		windowNeedsUpdate = true;
	}
	
	public static void loadConfig() {
		ConfigUtil.loadConfig(configFile, (key, value) -> parse(key, value), ':');
	}
	
	public static void saveConfig() {
		StringBuilder str = new StringBuilder();
		str.append("enableMod:" + enableMod + "\n");
		str.append("borderlessFullscreen:" + borderlessFullscreen + "\n");
		str.append("fullscreenOptimizations:" + fullscreenOptimizations + "\n");
		str.append("autoMinimize:" + autoMinimize + "\n");
		
		ConfigUtil.saveStringToFile(str.toString(), configFile);
	}

	public static void parse(String key, String value) {
		if(key.equals("enableMod")) {
			enableMod = value.equalsIgnoreCase("true");
		}else if(key.equals("borderlessFullscreen")) {
			borderlessFullscreen = value.equalsIgnoreCase("true");
		}else if(key.equals("fullscreenOptimizations")) {
			fullscreenOptimizations = value.equalsIgnoreCase("true");
		}else if(key.equals("autoMinimize")) {
			autoMinimize = value.equalsIgnoreCase("true");
		}
	}
	
	public static void setWindow(Window window) {
		FullscreenFix.window = window;
	}
	
	public static void loadTranslations() {
		print("Load Translations");
		
		LanguageManager languageManager = MinecraftClient.getInstance().getLanguageManager();
		if(languageManager == null) {
			print("Language Manager is null!");
			return;
		}
		
		translations.clear();
		String language = languageManager.getLanguage();
		
		print("Language: " + language);

		if(!language.equals("en_us")) {
			loadLanguage("en_us");
		}
		loadLanguage(language);
		
		print(translations.size() + " Translation keys");
	}
	
	private static void loadLanguage(String name) {
		print("Load Language: " + name);
		
		String path = "lang/" + name + ".lang";
		Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of(MODID, path));
		if(!resource.isPresent()) {
			print("Resource not present: " + path);
			return;
		}
		
		try {
			ConfigUtil.loadConfig(resource.get().getInputStream(), (key, value) -> translations.put(key, value), '=');	
		}catch (Exception e) {
			throw new RuntimeException("Loading language: " + name, e);
		}
	}
	
	public static Text translate(String key) {
		String value = translations.get(key);
		if(value != null) {
			return Text.of(value);
		}
		return Text.of(key);
	}
	
	public static String translateIfExists(String key) {
		String value = translations.get(key);
		if(value != null) {
			return value;
		}
		return null;
	}
	
	public static String translateToString(String key) {
		return translations.get(key);
	}
	
	public static boolean translationExists(String key) {
		return translations.containsKey(key);
	}
	
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	public static void print(String str) {
		System.out.print("[FullscreenFix] " + str + "\n");
	}

}
