package b100.gui;

import b100.fullscreenfix.mixin.access.IScreen;
import b100.gui.GuiScrollableList.Layout;
import b100.gui.GuiScrollableList.ListLayout;
import net.minecraft.text.Text;

public abstract class GuiScrollListScreen extends GuiScreen {

	public GuiScrollableList scrollList;
	public GuiScrollBar scrollBar;
	public Layout listLayout;
	
	public int headerSize = 32;
	public int footerSize = 32;
	
	public Text title;

	public GuiScrollListScreen(IScreen parentScreen) {
		super(parentScreen);
	}
	
	public abstract void initScrollElements();

	@Override
	protected void onInit() {
		listLayout = getListLayout();
		scrollList = add(new GuiScrollableList(this, listLayout));
		scrollBar = add(new GuiScrollBar(this, scrollList));
		
		initScrollElements();
	}
	
	@Override
	public void draw() {
		super.draw();
		if(title != null) {
			utils.drawCenteredString(title, width / 2, headerSize / 2 - 4, 0xFFFFFFFF, true);
		}
	}
	
	@Override
	public void onResize() {
		final int scrollBarWidth = 6;
		scrollList.setPosition(0, headerSize).setSize(this.width, this.height - (headerSize + footerSize));
		int contentWidth = listLayout.getContentWidth(scrollList);
		scrollBar.setPosition(scrollList.posX + scrollList.width / 2 + contentWidth / 2 + 16, scrollList.posY + 2).setSize(scrollBarWidth, scrollList.height - 4);
		super.onResize();
	}

	public void setDoubleFooterButtonPositions(GuiElement left, GuiElement right) {
		GuiUtils.setDoubleFooterButtonPositions(this, height - footerSize + 4, left, right);
	}
	
	public Layout getListLayout() {
		return new ListLayout();
	}

}
