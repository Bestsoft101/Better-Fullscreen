package b100.fullscreenfix;

import static b100.fullscreenfix.Global.INDEV;
import static b100.fullscreenfix.Global.MODID;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.blaze3d.platform.Window;
import com.mojang.serialization.Codec;

import b100.fullscreenfix.gui.ConfigScreen;
import b100.fullscreenfix.mixin.access.WindowAccess;
import b100.fullscreenfix.util.GLFWUtil;
import b100.lib.client.gui.util.GuiUtils;
import b100.lib.client.mixin.IScreen;
import b100.lib.config.properties.PropertiesFile;
import b100.lib.config.property.BooleanProperty;
import b100.lib.config.property.BooleanPropertyImpl;
import b100.lib.config.property.Property;
import b100.lib.translate.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.OptionInstance.TooltipSupplier;
import net.minecraft.client.OptionInstance.ValueUpdateListener;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;

public class FullscreenFix {
	
	private static Window window;

	public static boolean windowNeedsUpdate = true;
	public static boolean fullscreenModeWasChanged = false;
	
	// Config
	public static final PropertiesFile CONFIG = new PropertiesFile(Global.CONFIG_FILE);
	
	public static final BooleanProperty ENABLE_NEXT_LAUNCH = BooleanProperty.create(Global.MOD_ENABLED);
	public static final BooleanProperty ENABLE_FULLSCREEN = BooleanProperty.create(false, FullscreenFix::isFullscreenEnabled, FullscreenFix::setFullscreen);
	public static final BooleanProperty EXCLUSIVE_FULLSCREEN = new BooleanPropertyImpl(getExclusiveFullscreenDefaultValue()) {
		@Override
		public void setBoolean(boolean value) {
			if(value != getBoolean()) {
				super.setBoolean(value);
				updateWindow();
			}
		}
	};
	public static final BooleanProperty AUTO_MINIMIZE = new BooleanPropertyImpl(false) {
		@Override
		public void setBoolean(boolean value) {
			if(value != getBoolean()) {
				super.setBoolean(value);
				updateWindow();
			}
		};
	};
	public static final BooleanProperty START_IN_FULLSCREEN = new BooleanPropertyImpl(true);
	public static final BooleanProperty REPLACE_VIDEO_SETTINGS_BUTTON = new BooleanPropertyImpl(true);
	public static final BooleanProperty CAPTURE_CURSOR = new BooleanPropertyImpl(false) {
		@Override
		public void setBoolean(boolean value) {
			super.setBoolean(value);
			GLFWUtil.updateCursorMode();
		};
	};
	public static final BooleanProperty CONFIG_SCREEN_HOTKEY_ENABLED = new BooleanPropertyImpl(true);
	public static Property<VideoMode> FULLSCREEN_VIDEO_MODE = Property.create(null, VideoMode::parse).addValueChangeListener(value -> updateWindow());
	public static Property<MonitorInfo> LAST_FULLSCREEN_MONITOR = Property.create(null, MonitorInfo::fromConfigString).addValueChangeListener(value -> {
		debugPrint("Change fullscreen monitor: " + value.toConfigString());
		CONFIG.save();
	});
	
	static {
		CONFIG.add("enableMod", ENABLE_NEXT_LAUNCH);
		CONFIG.add("exclusiveFullscreen", EXCLUSIVE_FULLSCREEN);
		CONFIG.add("autoMinimize", AUTO_MINIMIZE);
		CONFIG.add("startInFullscreen", START_IN_FULLSCREEN);
		CONFIG.add("replaceVideoSettingsButton", REPLACE_VIDEO_SETTINGS_BUTTON);
		CONFIG.add("captureCursorInFullscreen", CAPTURE_CURSOR);
		CONFIG.add("configScreenHotkeyEnabled", CONFIG_SCREEN_HOTKEY_ENABLED);
		CONFIG.add("fullscreenVideoMode", FULLSCREEN_VIDEO_MODE);
		CONFIG.load();
	}

	/**
	 * Custom option for the vanilla video settings menu
	 */
	public static OptionInstance<Integer> fullscreenOption = createFullscreenOption();
	
	public static boolean getExclusiveFullscreenDefaultValue() {
		if(Util.getPlatform() == OS.LINUX) {
			return true;
		}
		return false;
	}

	////////////////////////////////////
	
	public static final Translations TRANS = Translations.get(null);
	
	static {
		Translations.loadFromNamespace(MODID);
	}

	////////////////////////////////////
	
	public static void openConfigScreen() {
		IScreen currentScreen = (IScreen) Minecraft.getInstance().gui.screen();
		if(!(currentScreen instanceof ConfigScreen)) {
			GuiUtils.instance.setScreen(new ConfigScreen(null));	
		}
	}
	
	////////////////////////////////////
	
	public static boolean isFullscreenEnabled() {
		if(window == null) {
			return false;
		}
		return window.isFullscreen();
	}
	
	public static void setFullscreen(boolean value) {
		WindowAccess access = (WindowAccess)(Object)window;
		access.setFullscreen(value);

		OptionInstance<Boolean> fullscreenOption = getVanillaFullscreenOption();
		if(fullscreenOption != null) {
			fullscreenOption.set(value);
		}
	}
	public static void updateWindow() {
		windowNeedsUpdate = true;
	}
	
	public static void setWindow(Window window) {
		FullscreenFix.window = window;
	}
	
	////////////////////////////////////
	
	private static OptionInstance<Integer> createFullscreenOption() {
		return new OptionInstance<Integer>("fullscreenoverride",
				OptionInstance.noTooltip(),
				(text, value) -> Component.nullToEmpty("idkwhatthisdoes"),
				new OptionInstance.ValueSet<>() {
					@Override
					public Function<OptionInstance<Integer>, AbstractWidget> createButton(TooltipSupplier<Integer> tooltipFactory, Options gameOptions, int x, int y, int width, ValueUpdateListener<? super Integer> onValueChanged) {
						return option -> {
							final Button button = Button.builder(getFullscreenModeDisplayText(), (pressedButton) -> {
								setFullscreenMode(getCurrentFullscreenMode().next());
								pressedButton.setMessage(getFullscreenModeDisplayText());
							}).build();
							return button;
						};
					}
					@Override
					public Optional<Integer> validateValue(Integer value) {
						return Optional.of(Mth.clamp(value, 0, 2));
					}
					@Override
					public Codec<Integer> codec() {
						return Codec.INT;
					}
				}, 0, (newValue) -> {}
		);
	}
	
	private static Component getFullscreenModeDisplayText() {
		FullscreenMode mode = getCurrentFullscreenMode();
		
		StringBuilder str = new StringBuilder();
		str.append(TRANS.asString("option.fullscreen")).append(": ");
		if(mode == FullscreenMode.BORDERLESS) {
			str.append(TRANS.asString("option.fullscreen.borderless"));
		}else if(mode == FullscreenMode.EXCLUSIVE) {
			str.append(TRANS.asString("option.fullscreen.exclusive"));
		}else {
			str.append(TRANS.asString("option.fullscreen.off"));
		}
		
		return Component.literal(str.toString());
	}
	
	public static FullscreenMode getCurrentFullscreenMode() {
		if(isFullscreenEnabled()) {
			if(EXCLUSIVE_FULLSCREEN.getBoolean()) {
				return FullscreenMode.EXCLUSIVE;
			}
			return FullscreenMode.BORDERLESS;
		}
		return FullscreenMode.OFF;
	}
	
	public static void setFullscreenMode(FullscreenMode mode) {
		if(mode == FullscreenMode.OFF) {
			setFullscreen(false);
		}else {
			setFullscreen(true);
			if(mode == FullscreenMode.EXCLUSIVE) {
				EXCLUSIVE_FULLSCREEN.setBoolean(true);
			}else {
				EXCLUSIVE_FULLSCREEN.setBoolean(false);
			}
		}
		fullscreenModeWasChanged = true;
	}
	
	public static OptionInstance<Boolean> getVanillaExclusiveFullscreenOption() {
		try {
			return Minecraft.getInstance().options.exclusiveFullscreen();	
		}catch (NullPointerException e) {
			return null;
		}
	}
	
	public static OptionInstance<Boolean> getVanillaFullscreenOption() {
		try {
			return Minecraft.getInstance().options.fullscreen();	
		}catch (NullPointerException e) {
			return null;
		}
	}
	
	////////////////////////////////////
	
	public static void debugPrint(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFixDebug] " + string + "\n");
		}
	}
	
	public static void print(String string) {
		Global.print(string);
	}
}
