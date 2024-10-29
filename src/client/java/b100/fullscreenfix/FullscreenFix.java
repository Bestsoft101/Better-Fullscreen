package b100.fullscreenfix;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;

import b100.fullscreenfix.mixin.access.WindowAccess;
import b100.fullscreenfix.util.ConfigUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.util.Window;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class FullscreenFix {

	public static final boolean INDEV = FabricLoader.getInstance().isDevelopmentEnvironment();
	public static final String MODID = "fullscreenfix";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final boolean OS_WINDOWS = isWindows();
	
	private static Window window;
	
	// Config
	
	private static File configFolder = Paths.get("config").toFile();
	private static File configFile = new File(configFolder, MODID + ".properties");
	
	public static boolean windowNeedsUpdate = true;
	public static boolean fullscreenModeWasChanged = false;
	
	private static final boolean enableMod;
	
	private static boolean enableModNextLaunch = true;
	private static boolean borderlessFullscreen = true;
	private static boolean fullscreenOptimizations = true;
	private static boolean autoMinimize = false;
	private static boolean startInFullscreen = true;
	private static boolean replaceVideoSettingsButton = true;
	
	/**
	 * May be null for current resolution
	 */
	private static VideoMode fullscreenVideoMode;
	
	private static final Map<String, String> translations = new HashMap<>();
	
	/**
	 * Custom option for the vanilla video settings menu
	 */
	public static SimpleOption<Integer> fullscreenOption = createFullscreenOption();
	
	static {
		loadConfig();
		
		enableMod = enableModNextLaunch;
	}
	
	////////////////////////////////////
	
	public static boolean isModEnabled() {
		return enableMod;
	}
	
	public static boolean isModEnabledNextLaunch() {
		return enableModNextLaunch;
	}
	
	public static void setModEnabled(boolean value) {
		enableModNextLaunch = value;
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
		if(window == null) {
			return false;
		}
		return window.isFullscreen();
	}
	
	public static void setFullscreen(boolean value) {
		WindowAccess access = (WindowAccess)(Object)window;
		access.setFullscreen(value);

		SimpleOption<Boolean> fullscreenOption = getVanillaFullscreenOption();
		if(fullscreenOption != null) {
			fullscreenOption.setValue(value);
		}
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
	
	public static boolean isStartInFullscreenEnabled() {
		return startInFullscreen;
	}
	
	public static void setStartInFullscreen(boolean value) {
		startInFullscreen = value;
	}
	
	public static void setReplaceVideoSettingsButton(boolean value) {
		replaceVideoSettingsButton = value;
	}
	
	public static boolean shouldReplaceVideoSettingsButton() {
		return replaceVideoSettingsButton;
	}
	
	////////////////////////////////////
	
	public static void updateWindow() {
		windowNeedsUpdate = true;
	}
	
	public static void setWindow(Window window) {
		FullscreenFix.window = window;
	}
	
	////////////////////////////////////
	
	private static SimpleOption<Integer> createFullscreenOption() {
		return new SimpleOption<Integer>("fullscreenoverride",
				SimpleOption.emptyTooltip(),
				(text, value) -> Text.of("idkwhatthisdoes"),
				new SimpleOption.Callbacks<>() {
					@Override
					public Function<SimpleOption<Integer>, ClickableWidget> getWidgetCreator(TooltipFactory<Integer> tooltipFactory, GameOptions gameOptions, int x, int y, int width, Consumer<Integer> changeCallback) {
						return option -> {
							final ButtonWidget button = ButtonWidget.builder(getFullscreenModeDisplayText(), (pressedButton) -> {
								int newMode = (getCurrentFullscreenModeInt() + 1) % 3;
								setFullscreenMode(newMode);
								pressedButton.setMessage(getFullscreenModeDisplayText());
							}).build();
							return button;
						};
					}
					@Override
					public Optional<Integer> validate(Integer value) {
						return Optional.of(MathHelper.clamp(value, 0, 2));
					}
					@Override
					public Codec<Integer> codec() {
						return Codec.INT;
					}
				}, 0, (newValue) -> {}
		);
	}
	
	private static Text getFullscreenModeDisplayText() {
		int mode = getCurrentFullscreenModeInt();
		StringBuilder str = new StringBuilder();
		str.append(translateToString("option.fullscreen")).append(": ");
		if(mode == 2) {
			str.append(translateToString("option.fullscreen.borderless"));
		}else if(mode == 1) {
			str.append(translateToString("option.fullscreen.on"));
		}else {
			str.append(translateToString("option.fullscreen.off"));
		}
		return Text.of(str.toString());
	}
	
	private static int getCurrentFullscreenModeInt() {
		if(isFullscreenEnabled()) {
			if(isBorderlessEnabled()) {
				return 2;
			}
			return 1;
		}
		return 0;
	}
	
	private static void setFullscreenMode(int mode) {
		if(mode == 0) {
			setFullscreen(false);
		}else {
			setFullscreen(true);
			if(mode == 2) {
				setBorderless(true);
			}else {
				setBorderless(false);
			}
		}
		fullscreenModeWasChanged = true;
	}
	
	@SuppressWarnings("resource")
	public static SimpleOption<Boolean> getVanillaFullscreenOption() {
		try {
			return MinecraftClient.getInstance().options.getFullscreen();	
		}catch (NullPointerException e) {
			return null;
		}
	}
	
	////////////////////////////////////
	
	public static void loadConfig() {
		ConfigUtil.loadConfig(configFile, (key, value) -> parse(key, value), ':');
	}
	
	public static void saveConfig() {
		StringBuilder str = new StringBuilder();
		str.append("enableMod:" + enableModNextLaunch + "\n");
		str.append("borderlessFullscreen:" + borderlessFullscreen + "\n");
		str.append("fullscreenOptimizations:" + fullscreenOptimizations + "\n");
		str.append("autoMinimize:" + autoMinimize + "\n");
		str.append("startInFullscreen:" + startInFullscreen + "\n");
		if(fullscreenVideoMode != null) {
			str.append("fullscreenMode:" + fullscreenVideoMode.toConfigString() + "\n");	
		}
		str.append("replaceVideoSettingsButton:" + replaceVideoSettingsButton + "\n");
		
		ConfigUtil.saveStringToFile(str.toString(), configFile);
	}

	public static void parse(String key, String value) {
		if(key.equals("enableMod")) {
			enableModNextLaunch = value.equalsIgnoreCase("true");
		}else if(key.equals("borderlessFullscreen")) {
			borderlessFullscreen = value.equalsIgnoreCase("true");
		}else if(key.equals("fullscreenOptimizations")) {
			fullscreenOptimizations = value.equalsIgnoreCase("true");
		}else if(key.equals("autoMinimize")) {
			autoMinimize = value.equalsIgnoreCase("true");
		}else if(key.equals("startInFullscreen")) {
			startInFullscreen = value.equalsIgnoreCase("true");
		}else if(key.equals("fullscreenMode")) {
			fullscreenVideoMode = VideoMode.parse(value);
		}else if(key.equals("replaceVideoSettingsButton")) {
			replaceVideoSettingsButton = value.equalsIgnoreCase("true");
		}
	}
	
	////////////////////////////////////
	
	public static void loadTranslations() {
		print("Load Translations");
		
		LanguageManager languageManager = MinecraftClient.getInstance().getLanguageManager();
		if(languageManager == null) {
			print("Language Manager is null!");
			return;
		}
		
		translations.clear();
		String language = languageManager.getLanguage();
		
		if(!language.equals("en_us")) {
			loadLanguage("en_us");
		}
		loadLanguage(language);
		
		print(translations.size() + " Translation keys");
	}
	
	private static void loadLanguage(String name) {
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
	
	////////////////////////////////////
	
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	public static void debugPrint(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFixDebug] " + string + "\n");
		}
	}
	
	public static void print(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFix] " + string + "\n");
		}else {
			LOGGER.info("[FullscreenFix] " + string);	
		}
	}

}
