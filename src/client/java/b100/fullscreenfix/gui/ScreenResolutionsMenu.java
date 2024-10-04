package b100.fullscreenfix.gui;

import org.lwjgl.glfw.GLFWVidMode;

import b100.fullscreenfix.FullscreenFix;
import b100.fullscreenfix.MonitorInfo;
import b100.fullscreenfix.VideoMode;
import b100.fullscreenfix.mixin.access.IScreen;
import b100.fullscreenfix.util.GLFWUtil;
import b100.gui.Focusable;
import b100.gui.GuiButton;
import b100.gui.GuiListButton;
import b100.gui.GuiScreen;
import b100.gui.GuiScrollListScreen;
import b100.gui.GuiTextElement;
import net.minecraft.text.Text;

public class ScreenResolutionsMenu extends GuiScrollListScreen {

	public GuiButton doneButton;
	
	public VideoMode selectedVideoMode = FullscreenFix.getFullscreenVideoMode();
	
	public ScreenResolutionsMenu(IScreen parentScreen) {
		super(parentScreen);
		
		title = FullscreenFix.translate("screen.fullscreenResolution.title");
	}
	
	@Override
	protected void onInit() {
		super.onInit();
		doneButton = add(new GuiButton(this, FullscreenFix.translate("button.done")).addActionListener((e) -> {
			FullscreenFix.setFullscreenVideoMode(selectedVideoMode);
			FullscreenFix.setFullscreen(true);
			back();	
		}));
	}

	@Override
	public void initScrollElements() {
		addVideoMode(null);

		int monitorNumber = 0;
		for(Long monitor : GLFWUtil.getMonitors()) {
			MonitorInfo monitorInfo = new MonitorInfo(monitor);
			
			GuiTextElement monitorInfoElement = scrollList.add(new GuiTextElement(Text.of("Monitor " + (monitorNumber + 1) + ": " + monitorInfo.width + " x " + monitorInfo.height + " at " + monitorInfo.posX + ", " + monitorInfo.posY), true, 0xFF808080));
			monitorInfoElement.setSize(200, 20);
			
			for(GLFWVidMode vidMode : GLFWUtil.getVideoModes(monitor)) {
				addVideoMode(new VideoMode(monitor, vidMode));
				
			}
			monitorNumber++;
		}
	}
	
	private void addVideoMode(VideoMode videoMode) {
		VideoModeElement videoModeElement = scrollList.add(new VideoModeElement(this, videoMode));
		if(VideoMode.compare(videoMode, selectedVideoMode)) {
			videoModeElement.setFocused(true);
		}
	}
	
	@Override
	public void focusChanged(Focusable focusable) {
		if(focusable.isFocused() && focusable instanceof VideoModeElement) {
			VideoModeElement videoModeElement = (VideoModeElement) focusable;
			selectedVideoMode = videoModeElement.videoMode;
		}
		super.focusChanged(focusable);
	}
	
	@Override
	public void onResize() {
		doneButton.setPosition(this.width / 2 - 100, this.height - footerSize + 4);
		super.onResize();
	}
	
	class VideoModeElement extends GuiListButton {
		
		public VideoMode videoMode;
		public Text text;
		
		public VideoModeElement(GuiScreen screen, VideoMode videoMode) {
			super(screen);
			this.videoMode = videoMode;
			
			if(videoMode != null) {
				GLFWVidMode vidmode = videoMode.vidMode;
				text = Text.of(vidmode.width() + " x " + vidmode.height() + " @ " + vidmode.refreshRate() + "hz");	
			}else {
				text = FullscreenFix.translate("value.fullscreenResolution.default");
			}
		}
		
		@Override
		public void draw() {
			super.draw();
			
			utils.drawCenteredString(text, posX + width / 2, posY + height / 2 - 4, 0xFFFFFF, true);
		}
	}

}
