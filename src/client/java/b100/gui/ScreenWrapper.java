package b100.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class ScreenWrapper extends Screen {

	public GuiScreen screen;
	
	private GuiUtils utils = GuiUtils.instance;
	private boolean screenOpened = true;
	
	public ScreenWrapper(GuiScreen screen) {
		super(null);
		
		this.screen = screen;
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		
		utils.drawContext = context;
		utils.textRenderer = textRenderer;
		
		screen.mouseX = mouseX;
		screen.mouseY = mouseY;
		
		if(!screen.isInitialized()) {
			screen.init();
		}
		
		if(screen.width != this.width || screen.height != this.height) {
			screen.setSize(width, height);
			screen.onResize();
		}
		
		if(screenOpened) {
			screenOpened = false;
			screen.onScreenOpened();
		}
		
		screen.draw();
	}
	
	@Override
	public boolean keyPressed(KeyInput input) {
		return screen.keyEvent(input.key(), input.scancode(), input.modifiers(), true);
	}
	
	@Override
	public boolean keyReleased(KeyInput input) {
		return screen.keyEvent(input.key(), input.scancode(), input.modifiers(), false);
	}
	
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		return screen.mouseEvent(click.button(), true, click.x(), click.y());
	}
	
	@Override
	public boolean mouseReleased(Click click) {
		return screen.mouseEvent(click.button(), false, click.x(), click.y());
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return screen.scrollEvent(horizontalAmount, verticalAmount, mouseX, mouseY);
	}
	
	@Override
	protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
		// TODO
	}
	
	@Override
	public void onDisplayed() {
		screenOpened = true;
	}
	
	@Override
	public Text getTitle() {
		return Text.of("");
	}
	
}
