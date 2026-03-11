package b100.fullscreenfix;

import static b100.fullscreenfix.Global.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OperatingSystem;
import net.minecraft.util.math.MathHelper;

public class FullscreenFix {
	
	private static Window window;

	public static boolean windowNeedsUpdate = true;
	public static boolean fullscreenModeWasChanged = false;
	
	// Config
	public static final PropertiesFile CONFIG = new PropertiesFile(Global.CONFIG_FILE);
	
	public static final BooleanProperty ENABLE_NEXT_LAUNCH = BooleanProperty.create(Global.MOD_ENABLED);
	public static final BooleanProperty FULLSCREEN = BooleanProperty.create(false, FullscreenFix::isFullscreenEnabled, FullscreenFix::setFullscreen);
	public static final BooleanProperty BORDERLESS_FULLSCREEN = new BooleanPropertyImpl(getBorderlessFullscreenDefaultValue()) {
		@Override
		public void setBoolean(boolean value) {
			if(value != getBoolean()) {
				super.setBoolean(value);
				updateWindow();
			}
		}
	};
	public static final BooleanProperty FULLSCREEN_OPTIMIZATIONS = new BooleanPropertyImpl(true) {
		@Override
		public void setBoolean(boolean value) {
			if(value != getBoolean()) {
				super.setBoolean(value);
				updateWindow();
			}
		};
	};
	public static final BooleanProperty AUTO_MINIMIZE = new BooleanPropertyImpl(true) {
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
		CONFIG.add("borderlessFullscreen", BORDERLESS_FULLSCREEN);
		CONFIG.add("fullscreenOptimizations", FULLSCREEN_OPTIMIZATIONS);
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
	public static SimpleOption<Integer> fullscreenOption = createFullscreenOption();
	
	public static boolean getBorderlessFullscreenDefaultValue() {
		if(Util.getOperatingSystem() == OperatingSystem.LINUX) {
			return false;
		}
		return true;
	}

	////////////////////////////////////
	
	public static final Translations TRANS = Translations.get(null);
	
	static {
		Translations.loadFromNamespace(MODID);
	}

	////////////////////////////////////
	
	@SuppressWarnings("resource")
	public static void openConfigScreen() {
		IScreen currentScreen = (IScreen) MinecraftClient.getInstance().currentScreen;
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

		SimpleOption<Boolean> fullscreenOption = getVanillaFullscreenOption();
		if(fullscreenOption != null) {
			fullscreenOption.setValue(value);
		}
	}
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
								setFullscreenMode(getCurrentFullscreenMode().next());
								pressedButton.setMessage(getFullscreenModeDisplayText());
							}).build();
							button.setX(x);
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
		FullscreenMode mode = getCurrentFullscreenMode();
		StringBuilder str = new StringBuilder();
		str.append(TRANS.asString("option.fullscreen")).append(": ");
		if(mode == FullscreenMode.BORDERLESS) {
			str.append(TRANS.asString("option.fullscreen.borderless"));
		}else if(mode == FullscreenMode.ON) {
			str.append(TRANS.asString("option.fullscreen.on"));
		}else {
			str.append(TRANS.asString("option.fullscreen.off"));
		}
		return Text.of(str.toString());
	}
	
	public static FullscreenMode getCurrentFullscreenMode() {
		if(isFullscreenEnabled()) {
			if(BORDERLESS_FULLSCREEN.getBoolean()) {
				return FullscreenMode.BORDERLESS;
			}
			return FullscreenMode.ON;
		}
		return FullscreenMode.OFF;
	}
	
	public static void setFullscreenMode(FullscreenMode mode) {
		if(mode == FullscreenMode.OFF) {
			setFullscreen(false);
		}else {
			setFullscreen(true);
			if(mode == FullscreenMode.BORDERLESS) {
				BORDERLESS_FULLSCREEN.setBoolean(true);
			}else {
				BORDERLESS_FULLSCREEN.setBoolean(false);
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
	
	public static void debugPrint(String string) {
		if(INDEV) {
			System.out.print("[FullscreenFixDebug] " + string + "\n");
		}
	}
	
	public static void print(String string) {
		Global.print(string);
	}
}
