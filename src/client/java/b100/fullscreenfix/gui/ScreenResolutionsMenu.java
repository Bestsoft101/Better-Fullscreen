package b100.fullscreenfix.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.MonitorInfo;
import b100.fullscreenfix.VideoMode;
import b100.fullscreenfix.mixin.access.IScreen;
import b100.fullscreenfix.util.GLFWUtil;
import b100.fullscreenfix.util.Util;
import b100.gui.FocusDirection;
import b100.gui.Focusable;
import b100.gui.GuiButton;
import b100.gui.GuiElement;
import b100.gui.GuiListButton;
import b100.gui.GuiScreen;
import b100.gui.GuiScrollableList;
import b100.gui.GuiScrollableList.ListLayout;
import b100.gui.GuiUtils;
import net.minecraft.text.Text;

public class ScreenResolutionsMenu extends GuiScreen {
	
	protected Text title;

	protected GuiButton cancelButton;
	protected GuiButton applyButton;

	protected int headerSize = 32;
	protected int footerSize = 32;
	
	protected GuiScrollableList monitorList;
	protected GuiScrollableList resolutionList;
	protected GuiScrollableList refreshRateList;
	
	protected RefreshRate previousMode;
	protected RefreshRate selectedMode;
	
	/**
	 * Contains all monitors
	 */
	protected final Map<Long, Monitor> monitorMap = new HashMap<>();
	
	/**
	 * Contains the resolution and refresh rate each monitor is set to
	 */
	protected final Map<Monitor, RefreshRate> monitorSettings = new HashMap<>();
	
	/**
	 * Contains which resolution was last selected for each monitor
	 */
	protected final Map<Monitor, Resolution> focusCacheResolution = new HashMap<>();
	
	/**
	 * Contains which refresh rate was last selected for each resolution
	 */
	protected final Map<Resolution, RefreshRate> focusCacheRefreshRate = new HashMap<>();
	
	protected Monitor primaryMonitor;
	
	protected boolean contentChanged = false;
	protected boolean initialized = false;
	
	public ScreenResolutionsMenu(IScreen parentScreen) {
		super(parentScreen);
		
		title = FullscreenFix.translate("screen.fullscreenResolution.title");
	}
	
	@Override
	protected void onInit() {
		applyButton = new GuiButton(this, FullscreenFix.translate("button.apply"));
		cancelButton = new GuiButton(this, null);
		
		applyButton.addActionListener((e) -> apply());
		cancelButton.addActionListener((e) -> back());
		
		// Setup Lists
		ListLayout layout = new ListLayout();
		layout.innerPadding = 0;
		layout.outerPadding = 2;
		
		monitorList = add(new GuiScrollableList(this, layout));
		resolutionList = add(new GuiScrollableList(this, layout));
		refreshRateList = add(new GuiScrollableList(this, layout));
		
		List<Monitor> monitors = new ArrayList<>();
		
		int monitorNumber = 0;
		for(long monitorHandle : GLFWUtil.getMonitors()) {
			MonitorInfo monitorInfo = new MonitorInfo(monitorHandle);
			
			Monitor monitor = monitorMap.get(monitorHandle);
			if(monitor == null) {
				monitor = new Monitor(++monitorNumber, monitorInfo);
				monitorMap.put(monitorHandle, monitor);
				monitors.add(monitor);
			}
			
			for(GLFWVidMode vidMode : GLFWUtil.getVideoModes(monitorHandle)) {
				monitor.getOrAdd(vidMode.width(), vidMode.height()).getOrAdd(vidMode);
			}
		}
		
		monitorList.add(new MonitorElement(this, null));
		
		for(int i=0; i < monitors.size(); i++) {
			Monitor monitor = monitors.get(i);
			Util.reverseList(monitor.resolutions);
			
			monitorList.add(new MonitorElement(this, monitor));
			
			for(Resolution resolution : monitor.resolutions) {
				Util.reverseList(resolution.refreshRates);
			}
		}
		
		// Cache monitor settings
		for(Monitor monitor : monitors) {
			Resolution resolution = monitor.get(monitor.info.width, monitor.info.height);
			RefreshRate refreshRate = resolution.get(monitor.info.refreshRate);
			monitorSettings.put(monitor, refreshRate);
		}
		
		RefreshRate fullscreenMode = getRefreshRate(FullscreenFix.getFullscreenVideoMode());
		
		this.primaryMonitor = monitorMap.get(GLFW.glfwGetPrimaryMonitor());
		this.previousMode = fullscreenMode;
		
		// Focus currently selected resolution
		setFocused(fullscreenMode);
		initialized = true;
		
		// Add buttons after everything else
		add(applyButton);
		add(cancelButton);
	}
	
	@Override
	public void draw() {
		utils.drawCenteredString(title, width / 2, headerSize / 2 - 4, 0xFFFFFF, true);
		if(contentChanged) {
			onResize();
		}
		super.draw();
	}
	
	public void apply() {
		if(selectedMode != null) {
			FullscreenFix.setFullscreenVideoMode(new VideoMode(selectedMode.monitor.info.handle, selectedMode.vidMode));
		}else {
			FullscreenFix.setFullscreenVideoMode(null);
		}
		FullscreenFix.setFullscreen(true);
		FullscreenFix.saveConfig();
		previousMode = selectedMode;
		updateButtons();
	}
	
	public void updateButtons() {
		if(selectedMode != previousMode) {
			applyButton.setClickable(true);
			cancelButton.text = FullscreenFix.translate("button.cancel");
		}else {
			applyButton.setClickable(false);
			cancelButton.text = FullscreenFix.translate("button.done");
		}
	}
	
	@Override
	public boolean keyEvent(int key, int scancode, int modifiers, boolean pressed) {
		if(pressed && key == GLFW.GLFW_KEY_ENTER) {
			apply();
			back();
		}
		return super.keyEvent(key, scancode, modifiers, pressed);
	}
	
	@Override
	public Focusable getNextScreenFocusableElement(Focusable focusedElement, FocusDirection direction) {
		GuiElement element = (GuiElement) focusedElement;
		if(monitorList.contains(element)) {
			if(direction == FocusDirection.LEFT) return null;
			if(direction == FocusDirection.RIGHT) return resolutionList.getLastFocusedElement();
		}
		if(resolutionList.contains(element)) {
			if(direction == FocusDirection.LEFT) return monitorList.getLastFocusedElement();
			if(direction == FocusDirection.RIGHT) return refreshRateList.getLastFocusedElement();
		}
		if(refreshRateList.contains(element)) {
			if(direction == FocusDirection.LEFT) return resolutionList.getLastFocusedElement();
			if(direction == FocusDirection.RIGHT) return null;
		}
		return super.getNextScreenFocusableElement(focusedElement, direction);
	}
	
	public RefreshRate getRefreshRate(VideoMode videoMode) {
		if(videoMode == null) {
			return null;
		}
		FullscreenFix.debugPrint("Get RefreshRate: " + videoMode.toConfigString());
		
		Monitor monitor = monitorMap.get(videoMode.monitor);
		FullscreenFix.debugPrint("  Monitor: " + monitor);
		if(monitor == null) {
			return null;
		}
		
		Resolution resolution = monitor.get(videoMode.vidMode.width(), videoMode.vidMode.height());
		FullscreenFix.debugPrint("  Resolution: " + resolution);
		if(resolution == null) {
			return null;
		}
		
		RefreshRate refreshRate = resolution.get(videoMode.vidMode);
		FullscreenFix.debugPrint("  Refresh Rate: " + refreshRate);
		
		return refreshRate;
	}
	
	public void setFocused(RefreshRate refreshRate) {
		FullscreenFix.debugPrint("Focus: " + refreshRate);
		
		if(refreshRate == null) {
			getMonitorElement(null).setFocused(true);
			return;
		}
		
		if(!isRefreshRateSet(refreshRate)) {
			if(!isResolutionSet(refreshRate.resolution)) {
				if(!isMonitorSet(refreshRate.monitor)) {
					getMonitorElement(refreshRate.monitor).setFocused(true);
				}
				getResolutionElement(refreshRate.resolution).setFocused(true);
			}
			getRefreshRateElement(refreshRate).setFocused(true);
		}
	}
	
	public boolean setMonitor(Monitor monitor) {
		if(isMonitorSet(monitor)) {
			return false;
		}
		
		FullscreenFix.debugPrint("Set Monitor: " + monitor);
		resolutionList.removeAll();
		
		if(monitor != null) {

			// Build menu
			for(Resolution resolution : monitor.resolutions) {
				resolutionList.add(new ResolutionElement(this, resolution));
			}
			
			// Select resolution
			Resolution resolution = focusCacheResolution.get(monitor);
			if(resolution == null) {
				resolution = monitorSettings.get(monitor).resolution;
			}
			ResolutionElement resolutionElement = getResolutionElement(resolution);
			if(resolutionElement != null) {
				resolutionElement.setFocused(true);
			}else {
				FullscreenFix.debugPrint("No resolution element to focus!");	
			}
		}else {
			setResolution(null);
		}
		contentChanged = true;
		
		return true;
	}
	
	public boolean setResolution(Resolution resolution) {
		if(isResolutionSet(resolution)) {
			return false;
		}
		
		FullscreenFix.debugPrint("Set Resolution: " + resolution);
		refreshRateList.removeAll();
		
		if(resolution != null) {
			// Build menu
			for(RefreshRate refreshRate : resolution.refreshRates) {
				refreshRateList.add(new RefreshRateElement(this, refreshRate));
			}
			
			// Put in cache
			focusCacheResolution.put(resolution.monitor, resolution);
			
			// Focus refresh rate
			focusCacheRefreshRate.clear();
			RefreshRate refreshRate = focusCacheRefreshRate.get(resolution);
			if(refreshRate == null) {
				RefreshRate defaultRefreshRate = monitorSettings.get(resolution.monitor);
				if(defaultRefreshRate != null && defaultRefreshRate.resolution == resolution) {
					refreshRate = defaultRefreshRate;
				}
			}
			if(refreshRate == null) {
				refreshRate = resolution.refreshRates.get(0);
			}
			RefreshRateElement refreshRateElement = getRefreshRateElement(refreshRate);
			if(refreshRateElement != null) {
				refreshRateElement.setFocused(true);
			}else {
				FullscreenFix.debugPrint("No element to focus for " + refreshRate + "!");	
			}
		}else {
			setRefreshRate(null);
		}
		contentChanged = true;
		
		return true;
	}
	
	public boolean setRefreshRate(RefreshRate refreshRate) {
		if(isRefreshRateSet(refreshRate)) {
			return false;
		}
		
		FullscreenFix.debugPrint("Set Refresh Rate: " + refreshRate);
		
		if(refreshRate != null) {
			// Put in cache
			focusCacheRefreshRate.put(refreshRate.resolution, refreshRate);
		}
		selectedMode = refreshRate;
		
		updateButtons();
		
		return true;
	}
	
	public boolean isMonitorSet(Monitor monitor) {
		if(!initialized) {
			return false;
		}
		if(selectedMode == null) {
			return monitor == null;
		}
		return selectedMode.monitor == monitor;
	}
	
	public boolean isResolutionSet(Resolution resolution) {
		if(!initialized) {
			return false;
		}
		if(selectedMode == null) {
			return resolution == null;
		}
		return selectedMode.resolution == resolution;
	}
	
	public boolean isRefreshRateSet(RefreshRate refreshRate) {
		if(!initialized) {
			return false;
		}
		return selectedMode == refreshRate;
	}
	
	public MonitorElement getMonitorElement(Monitor monitor) {
		for(GuiElement element : monitorList.elements) {
			if(element instanceof MonitorElement) {
				MonitorElement monitorElement = (MonitorElement) element;
				if(monitorElement.monitor == monitor) {
					return monitorElement;
				}
			}
		}
		return null;
	}
	
	public ResolutionElement getResolutionElement(Resolution resolution) {
		for(GuiElement element : resolutionList.elements) {
			if(element instanceof ResolutionElement) {
				ResolutionElement resolutionElement = (ResolutionElement) element;
				if(resolutionElement.resolution == resolution) {
					return resolutionElement;
				}
			}
		}
		return null;
	}
	
	public RefreshRateElement getRefreshRateElement(RefreshRate refreshRate) {
		for(GuiElement element : refreshRateList.elements) {
			if(element instanceof RefreshRateElement) {
				RefreshRateElement resolutionElement = (RefreshRateElement) element;
				if(resolutionElement.refreshRate == refreshRate) {
					return resolutionElement;
				}
			}
		}
		return null;
	}
	
	@Override
	public void onResize() {
		int p = 5;
		
		int resolutionListWidth = Math.min(220, width / 2);
		int smallListsWidth = Math.min(100, width / 4 - 2 * p);
		
		int x0 = width / 2 - resolutionListWidth / 2;
		int y0 = headerSize;
		int y1 = y0 + 16;
		int h1 = height - headerSize - footerSize;
		int h2 = h1 / 2;
		
		resolutionList.setPosition(x0, y0).setSize(resolutionListWidth, h1);
		monitorList.setPosition(x0 - p - smallListsWidth, y1).setSize(smallListsWidth, h2);
		refreshRateList.setPosition(x0 + resolutionListWidth + p, y1).setSize(smallListsWidth, h2);
		
		for(GuiElement element : resolutionList.elements) {
			element.setSize(resolutionListWidth, 20);
		}
		for(GuiElement element : monitorList.elements) {
			element.setSize(smallListsWidth, 20);
		}
		for(GuiElement element : refreshRateList.elements) {
			element.setSize(smallListsWidth, 20);
		}
		
		GuiUtils.setDoubleFooterButtonPositions(this, height - footerSize + 4, applyButton, cancelButton);
		
		super.onResize();
	}
	
	@Override
	public Focusable getNextFocusable(GuiElement element, FocusDirection direction) {
		return super.getNextFocusable(element, direction);
	}
	
	class Monitor {
		
		public final int id;
		public final MonitorInfo info;
		public final String name;
		public final List<Resolution> resolutions = new ArrayList<>();
		
		public Monitor(int id, MonitorInfo monitorInfo) {
			this.id = id;
			this.info = monitorInfo;
			this.name = GLFW.glfwGetMonitorName(monitorInfo.handle);
		}
		
		public Resolution get(int w, int h) {
			for(Resolution resolution : resolutions) {
				if(resolution.width == w && resolution.height == h) {
					return resolution;
				}
			}
			return null;
		}
		
		public Resolution getOrAdd(int w, int h) {
			Resolution resolution = get(w, h);
			if(resolution == null) {
				resolution = new Resolution(this, w, h);
				resolutions.add(resolution);
			}
			return resolution;
		}
		
		@Override
		public String toString() {
			return "Monitor[" + id + "]";
		}
	}
	
	class Resolution {
		
		public final Monitor monitor;
		public final int width;
		public final int height;
		public final List<RefreshRate> refreshRates = new ArrayList<>();
		
		public Resolution(Monitor monitor, int width, int height) {
			this.monitor = monitor;
			this.width = width;
			this.height = height;
		}
		
		public RefreshRate getOrAdd(GLFWVidMode vidMode) {
			RefreshRate refreshRate = get(vidMode);
			if(refreshRate == null) {
				refreshRate = new RefreshRate(this, vidMode);
				refreshRates.add(refreshRate);
			}
			return refreshRate;
		}
		
		public RefreshRate get(GLFWVidMode vidMode) {
			for(RefreshRate refreshRate : refreshRates) {
				if(refreshRate.vidMode.equals(vidMode)) {
					return refreshRate;
				}
			}
			return null;
		}
		
		public RefreshRate get(int rate) {
			for(RefreshRate refreshRate : refreshRates) {
				if(refreshRate.vidMode.refreshRate() == rate) {
					return refreshRate;
				}
			}
			return null;
		}
		
		@Override
		public String toString() {
			return "Resolution["
					+ monitor + ","
					+ width + "x" + height
					+ "]";
		}
	}
	
	class RefreshRate {
		
		public final Monitor monitor;
		public final Resolution resolution;
		public final GLFWVidMode vidMode;
		
		public RefreshRate(Resolution resolution, GLFWVidMode vidMode) {
			this.monitor = resolution.monitor;
			this.resolution = resolution;
			this.vidMode = vidMode;
		}
		
		public int get() {
			return vidMode.refreshRate();
		}
		
		@Override
		public String toString() {
			return "RefreshRate["
					+ resolution + ","
					+ vidMode.refreshRate() + "hz"
					+ "]";
		}
	}
	
	class MonitorElement extends GuiListButton {

		public Monitor monitor;
		
		public MonitorElement(GuiScreen screen, Monitor monitor) {
			super(screen);
			this.monitor = monitor;
			if(monitor != null) {
				this.text = Text.of("Monitor " + monitor.id);
			}else {
				this.text = FullscreenFix.translate("option.fullscreenResolution.default");
			}
		}
		
		@Override
		public void onFocusChanged() {
			if(isFocused()) {
				setMonitor(monitor);	
			}
			super.onFocusChanged();
		}
	}
	
	class ResolutionElement extends GuiListButton {

		public Resolution resolution;
		
		public ResolutionElement(GuiScreen screen, Resolution resolution) {
			super(screen);
			this.resolution = resolution;
			this.text = Text.of(resolution.width + " x " + resolution.height);
		}

		@Override
		public void onFocusChanged() {
			if(isFocused()) {
				setResolution(resolution);
			}
			super.onFocusChanged();
		}
	}
	
	class RefreshRateElement extends GuiListButton {

		public RefreshRate refreshRate;
		
		public RefreshRateElement(GuiScreen screen, RefreshRate refreshRate) {
			super(screen);
			this.refreshRate = refreshRate;
			this.text = Text.of(refreshRate.get() + " hz");
		}

		@Override
		public void onFocusChanged() {
			if(isFocused()) {
				setRefreshRate(refreshRate);
			}
			super.onFocusChanged();
		}
	}
}
