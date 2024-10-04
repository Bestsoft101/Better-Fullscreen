package b100.gui;

import b100.fullscreenfix.mixin.access.IScreen;
import b100.gui.GuiScrollableList.ListLayout;
import net.minecraft.text.Text;

public abstract class GuiScrollListScreen extends GuiScreen {

	public GuiScrollableList scrollList;
	public GuiScrollBar scrollBar;
	public ListLayout listLayout;
	
	public int headerSize = 32;
	public int footerSize = 32;
	
	public Text title;

	public GuiScrollListScreen(IScreen parentScreen) {
		super(parentScreen);
	}

	@Override
	protected void onInit() {
		listLayout = new ListLayout();
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
		int p = 2;
		int w = 150;
		int center = width / 2;
		int x0 = center - w - p;
		int x1 = center + p;
		int y = height - footerSize + 4;
		left.setPosition(x0, y).setSize(w, 20);
		right.setPosition(x1, y).setSize(w, 20);
	}
	
	public abstract void initScrollElements();

}
