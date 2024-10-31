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
import net.minecraft.text.Text;

public class ScreenResolutionsMenu extends GuiScreen {

	public GuiButton doneButton;
	
	public Text title;

	public int headerSize = 32;
	public int footerSize = 32;
	
	public GuiScrollableList monitorList;
	public GuiScrollableList resolutionList;
	public GuiScrollableList refreshRateList;
	
	public RefreshRate selectedMode;
	
	private boolean contentChanged = false;
	
	public ScreenResolutionsMenu(IScreen parentScreen) {
		super(parentScreen);
		
		title = FullscreenFix.translate("screen.fullscreenResolution.title");
	}
	
	@Override
	protected void onInit() {
		doneButton = add(new GuiButton(this, FullscreenFix.translate("button.done")).addActionListener((e) -> {
			if(selectedMode == null) {
				FullscreenFix.print("No mode selected!");
				return;
			}
			
			FullscreenFix.setFullscreenVideoMode(new VideoMode(selectedMode.monitor.info.handle, selectedMode.vidMode));
			FullscreenFix.setFullscreen(true);
			FullscreenFix.saveConfig();
			back();	
		}));
		
		ListLayout layout = new ListLayout();
		layout.innerPadding = 0;
		layout.outerPadding = 2;
		
		monitorList = add(new GuiScrollableList(this, layout));
		resolutionList = add(new GuiScrollableList(this, layout));
		refreshRateList = add(new GuiScrollableList(this, layout));
		
		List<Monitor> monitors = new ArrayList<>();
		Map<Long, Monitor> monitorMap = new HashMap<>();
		
		int monitorNumber = 0;
		for(long monitorHandle : GLFWUtil.getMonitors()) {
			MonitorInfo monitorInfo = new MonitorInfo(monitorHandle);
			
			Monitor monitor = monitorMap.get(monitorHandle);
			if(monitor == null) {
				monitor = new Monitor(monitorNumber++, monitorInfo);
				monitorMap.put(monitorHandle, monitor);
				monitors.add(monitor);
			}
			
			for(GLFWVidMode vidMode : GLFWUtil.getVideoModes(monitorHandle)) {
				Resolution resolution = monitor.get(vidMode.width(), vidMode.height());
				resolution.refreshRates.add(new RefreshRate(monitor, resolution, vidMode));
			}
		}
		
		for(int i=0; i < monitors.size(); i++) {
			Monitor monitor = monitors.get(i);
			Util.reverseList(monitor.resolutions);
			
			monitorList.add(new MonitorElement(this, monitor));
			
			for(Resolution resolution : monitor.resolutions) {
				Util.reverseList(resolution.refreshRates);
			}
		}
		
		VideoMode videoMode = FullscreenFix.getFullscreenVideoMode();
		if(videoMode != null) {
			Monitor monitor = monitorMap.get(videoMode.monitor);
			setMonitor(monitor);
		}else {
			setMonitor(monitors.get(0));
		}
	}
	
	@Override
	public void draw() {
		if(contentChanged) {
			onResize();
		}
		super.draw();
	}
	
	public boolean setMonitor(Monitor monitor) {
		if(selectedMode == null || selectedMode.monitor != monitor) {
			FullscreenFix.debugPrint("Set Monitor: " + monitor.id);
			
			// Update Resolution List
			resolutionList.removeAll();
			for(Resolution resolution : monitor.resolutions) {
				ResolutionElement element = new ResolutionElement(this, resolution);
				resolutionList.add(element);
			}
			contentChanged = true;
			
			// Set to first resolution
			setResolution(monitor.resolutions.get(0));	
			
			return true;
		}
		return false;
	}
	
	public boolean setResolution(Resolution resolution) {
		if(selectedMode == null || selectedMode.resolution != resolution) {
			FullscreenFix.debugPrint("Set Resolution: " + resolution.width + " x " + resolution.height);
			
			// Update Refresh Rate List
			refreshRateList.removeAll();
			for(RefreshRate refreshRate : resolution.refreshRates) {
				RefreshRateElement element = new RefreshRateElement(this, refreshRate);
				refreshRateList.add(element);
			}
			contentChanged = true;
			
			// Set to first refresh rate
			setRefreshRate(resolution.refreshRates.get(0));
			
			return true;
		}
		return false;
	}
	
	public boolean setRefreshRate(RefreshRate refreshRate) {
		if(selectedMode != refreshRate) {
			FullscreenFix.debugPrint("Set Refresh Rate: " + refreshRate.get() + "hz");
			selectedMode = refreshRate;
			
			return true;
		}
		return false;
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
		
		doneButton.setPosition(this.width / 2 - 100, this.height - footerSize + 4);
		
		super.onResize();
	}
	
	@Override
	public Focusable getNextFocusable(GuiElement element, FocusDirection direction) {
		return super.getNextFocusable(element, direction);
	}
	
	class Monitor {
		
		public int id;
		public MonitorInfo info;
		public String name;
		public List<Resolution> resolutions = new ArrayList<>();
		
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
			Resolution resolution = new Resolution(this, w, h);
			resolutions.add(resolution);
			return resolution;
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
	}
	
	class RefreshRate {
		
		public final Monitor monitor;
		public final Resolution resolution;
		public final GLFWVidMode vidMode;
		
		public RefreshRate(Monitor monitor, Resolution resolution, GLFWVidMode vidMode) {
			this.monitor = monitor;
			this.resolution = resolution;
			this.vidMode = vidMode;
		}
		
		public int get() {
			return vidMode.refreshRate();
		}
	}
	
	class MonitorElement extends GuiListButton {

		public Monitor monitor;
		
		public MonitorElement(GuiScreen screen, Monitor monitor) {
			super(screen);
			this.monitor = monitor;
			this.text = Text.of("Monitor " + (monitor.id + 1));
		}
		
		@Override
		public void onFocusChanged() {
			if(isFocused()) {
				setMonitor(monitor);	
			}
			super.onFocusChanged();
		}
		
		@Override
		public int getHighlightColor() {
			int color = super.getHighlightColor();
			if((color & 0xFF000000) == 0) {
				if(selectedMode != null && selectedMode.monitor == monitor) {
					return outlineColor;
				}
			}
			return color;
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
		
		@Override
		public int getHighlightColor() {
			int color = super.getHighlightColor();
			if((color & 0xFF000000) == 0) {
				if(selectedMode != null && selectedMode.resolution == resolution) {
					return outlineColor;
				}
			}
			return color;
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
		
		@Override
		public int getHighlightColor() {
			int color = super.getHighlightColor();
			if(color == 0) {
				if(selectedMode != null && selectedMode == refreshRate) {
					return outlineColor;
				}
			}
			return color;
		}
	}
}
