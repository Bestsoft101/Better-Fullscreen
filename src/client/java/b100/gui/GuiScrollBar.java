package b100.gui;

public class GuiScrollBar extends GuiElement {

	public GuiScreen screen;
	public GuiScrollableList list;
	
	protected boolean dragging = false;
	protected int dragButton = 0;
	protected double previousScrollAmount;
	
	protected double clickPosX;
	protected double clickPosY;
	
	public GuiScrollBar(GuiScreen screen, GuiScrollableList list) {
		this.screen = screen;
		this.list = list;
	}
	
	@Override
	public void draw() {
		int scrollRegionHeight = list.getScrollRegionHeight();
		int contentHeight = list.getContentHeight();
		
		if(contentHeight < scrollRegionHeight) {
			return;
		}
		
		float f = scrollRegionHeight / (float) contentHeight;
		int scrollerHeight = (int) (this.height * f);
		scrollerHeight = Math.max(scrollerHeight, 32);
		scrollerHeight = Math.min(scrollerHeight, this.height - this.width);
		
		if(dragging) {
			double offset = clickPosY - screen.mouseY;
			
			int scrollableAreaLength = height - scrollerHeight;
			
			double d = offset / (double) scrollableAreaLength;
			d *= (contentHeight - scrollRegionHeight);
			
			list.setScrollAmount(previousScrollAmount - d);
		}

		float scrollFactor = (float) (list.getScrollAmount() / list.getMaxScrollAmount());
		int scrollerOffset = (int) (scrollFactor * (height - scrollerHeight));
		
		utils.drawGuiTexture(Textures.INSTANCE.scrollerBackground, posX, posY, width, height);
		utils.drawGuiTexture(Textures.INSTANCE.scroller, posX, posY + scrollerOffset, width, scrollerHeight);
	}
	
	@Override
	public boolean mouseEvent(int button, boolean pressed, double mouseX, double mouseY) {
		if(!pressed && dragging && button == dragButton) {
			dragging = false;
		}
		if(pressed && !dragging && screen.isMouseOver(this)) {
			dragging = true;
			dragButton = button;
			previousScrollAmount = list.getScrollAmount();
			clickPosX = mouseX;
			clickPosY = mouseY;
		}
		return super.mouseEvent(button, pressed, mouseX, mouseY);
	}
	
	@Override
	public boolean isSolid() {
		return list.getContentHeight() > list.getScrollRegionHeight();
	}

}
