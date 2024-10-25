package b100.gui;

import b100.gui.Textures.GuiTextures;
import net.minecraft.util.math.MathHelper;

public class GuiScrollableList extends GuiContainer implements FocusListener {
	
	public GuiScreen screen;
	public Layout layout;
	
	private double scrollAmount = 0.0;
	private double maxScrollAmount = 0.0;
	private int contentHeight;
	private int scrollRegionHeight;
	
	private boolean scrollAmountChanged = false;
	
	private Focusable lastFocusedElement = null;
	
	public GuiScrollableList(GuiScreen screen, Layout layout) {
		this.screen = screen;
		this.layout = layout;
		this.isList = true;
		
		screen.addFocusListener(this);
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
		
		utils.drawContext.drawTexture(utils.getRenderLayer, textures.menuListBackground, 0, this.posY + separatorSize, 0, 0, this.width, this.height - 2 * separatorSize, 32, 32);
		utils.drawContext.drawTexture(utils.getRenderLayer, textures.headerSeparator, 0, this.posY, 0, 0, this.width, separatorSize, 32, separatorSize);
		utils.drawContext.drawTexture(utils.getRenderLayer, textures.footerSeparator, 0, this.posY + this.height - separatorSize, 0, 0, this.width, 2, 32, separatorSize);
		
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
			scroll(verticalAmount * 16.0);
			return true;
		}
		return false;
	}
	
	public void scroll(double amount) {
		setScrollAmount(scrollAmount - amount);
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
	
	@Override
	public Focusable getFirstFocusableElement(FocusDirection direction) {
		if(lastFocusedElement != null && direction.isTab()) {
			return lastFocusedElement;
		}
		return super.getFirstFocusableElement(direction);
	}
	
	@Override
	public Focusable getNextFocusable(GuiElement element, FocusDirection direction) {
		return super.getNextFocusable(element, direction);
	}

	@Override
	public void focusChanged(Focusable focusable) {
		GuiElement element = (GuiElement) focusable;
		if(contains(element)) {
			lastFocusedElement = focusable;
			
			int offset = 0;
			if(element.posY < posY) {
				offset = posY - element.posY + 4;
			}
			if(element.posY + element.height > posY + height) {
				offset = (posY + height) - element.posY - element.height - 4;
			}
			if(offset != 0) {
				scroll(offset);
			}
		}
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
