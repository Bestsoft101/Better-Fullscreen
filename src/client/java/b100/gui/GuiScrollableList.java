package b100.gui;

import b100.gui.Textures.GuiTextures;
import net.minecraft.util.math.MathHelper;

public class GuiScrollableList extends GuiContainer {
	
	public GuiScreen screen;
	public Layout layout;
	
	private double scrollAmount = 0.0;
	private double maxScrollAmount = 0.0;
	private int contentHeight;
	private int scrollRegionHeight;
	
	private boolean scrollAmountChanged = false;
	
	public GuiScrollableList(GuiScreen screen, Layout layout) {
		this.screen = screen;
		this.layout = layout;
	}
	
	@Override
	public void draw() {
		if(scrollAmountChanged) {
			scrollAmountChanged = false;
			layout.moveElements(this);
			super.onResize();
		}
		
		GuiTextures textures = Textures.INSTANCE.getCurrentGuiTextures();
		
		final int separatorSize = 2;
		
		utils.drawContext.drawTexture(textures.menuListBackground, 0, this.posY + separatorSize, 0, 0, this.width, this.height - 2 * separatorSize, 32, 32);
		utils.drawContext.drawTexture(textures.headerSeparator, 0, this.posY, 0, 0, this.width, separatorSize, 32, separatorSize);
		utils.drawContext.drawTexture(textures.footerSeparator, 0, this.posY + this.height - separatorSize, 0, 0, this.width, 2, 32, separatorSize);
		
		utils.drawContext.enableScissor(this.posX, this.posY + separatorSize, this.width, this.posY + this.height - separatorSize);
		super.draw();
		utils.drawContext.disableScissor();
	}
	
	@Override
	public void onResize() {
		scrollRegionHeight = getScrollRegionHeight();
		contentHeight = layout.getContentHeight(this);
		maxScrollAmount = Math.max(0.0, contentHeight - scrollRegionHeight);
		scrollAmount = Math.clamp(scrollAmount, 0.0, maxScrollAmount);
		layout.moveElements(this);
		
		super.onResize();
	}
	
	@Override
	public boolean scrollEvent(double horizontalAmount, double verticalAmount, double mouseX, double mouseY) {
		if(screen.isInside(mouseX, mouseY)) {
			scroll(verticalAmount);
			return true;
		}
		return false;
	}
	
	public void scroll(double amount) {
		setScrollAmount(scrollAmount - amount * 16.0);
	}
	
	public void setScrollAmount(double newScrollAmount) {
		newScrollAmount = Math.clamp(newScrollAmount, 0.0, maxScrollAmount);
		if(newScrollAmount == scrollAmount) {
			return;
		}
		scrollAmount = newScrollAmount;
		scrollAmountChanged = true;
	}
	
	@Override
	public boolean isSolid() {
		return true;
	}
	
	public int getScrollRegionHeight() {
		return height;
	}
	
	public int getContentHeight() {
		return contentHeight;
	}
	
	public int getScrollOffset() {
		return -MathHelper.floor(scrollAmount);
	}
	
	public double getScrollAmount() {
		return scrollAmount;
	}
	
	public double getMaxScrollAmount() {
		return maxScrollAmount;
	}
	
	@Override
	public GuiElement getClickElementAt(double x, double y) {
		if(!isInside(x, y)) {
			return null;
		}
		return super.getClickElementAt(x, y);
	}
	
	public static interface Layout {
		
		public void moveElements(GuiScrollableList list);
		
		public int getContentHeight(GuiScrollableList list);
		
		public int getContentWidth(GuiScrollableList list);
		
	}
	
	public static class ListLayout implements Layout {
		
		public int outerPadding = 5;
		public int innerPadding = 0;
		public Align align = Align.CENTER;
		
		public ListLayout() {
		}
		
		@Override
		public void moveElements(GuiScrollableList list) {
			int offset = list.getScrollOffset();
			offset += outerPadding;
			
			for(int i=0; i < list.elements.size(); i++) {
				GuiElement element = list.elements.get(i);
				
				int x;
				if(align == Align.LEFT) {
					x = list.posX + outerPadding;
				}else if(align == Align.CENTER) {
					x = list.posX + list.width / 2 - element.width / 2;
				}else {
					x = list.posX + list.width - element.width - outerPadding;
				}
				
				element.setPosition(x, list.posY + offset);
				offset += element.height;
				offset += innerPadding;
			}
		}

		@Override
		public int getContentHeight(GuiScrollableList list) {
			int contentHeight = 0;
			
			for(int i=0; i < list.elements.size(); i++) {
				contentHeight += list.elements.get(i).height;
			}
			
			contentHeight += 2 * outerPadding;
			contentHeight += (list.elements.size() - 1) * innerPadding;
			
			return contentHeight;
		}

		@Override
		public int getContentWidth(GuiScrollableList list) {
			int contentWidth = 0;
			
			for(int i=0; i < list.elements.size(); i++) {
				contentWidth = Math.max(contentWidth, list.elements.get(i).width);
			}
			
			contentWidth += 2 * outerPadding;
			
			return contentWidth;
		}
		
		public static enum Align {
			
			LEFT, CENTER, RIGHT;
			
		}

	}

}
