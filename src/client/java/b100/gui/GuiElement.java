package b100.gui;

public abstract class GuiElement {
	
	public GuiUtils utils = GuiUtils.instance;
	
	public int posX;
	public int posY;
	public int width;
	public int height;
	
	public abstract void draw();
	
	private GuiContainer container;
	
	public boolean keyEvent(int key, int scancode, int modifiers, boolean pressed) {
		return false;
	}
	
	public boolean mouseEvent(int button, boolean pressed, double mouseX, double mouseY) {
		return false;
	}
	
	public boolean scrollEvent(double horizontalAmount, double verticalAmount, double mouseX, double mouseY) {
		return false;
	}
	
	public void onResize() {
		
	}
	
	public GuiElement setPosition(int x, int y) {
		posX = x;
		posY = y;
		return this;
	}
	
	public GuiElement setSize(int w, int h) {
		width = w;
		height = h;
		return this;
	}
	
	public boolean isInside(double x, double y) {
		return x >= posX && y >= posY && x < posX + width && y < posY + height;
	}
	
	public boolean isSolid() {
		return true;
	}
	
	public void onAddedToContainer(GuiContainer container) {
		if(this.container != null) {
			throw new RuntimeException("Cannot add element " + this + "to container " + container + " because element is already added to container " + this.container + "!");
		}
		this.container = container;
	}
	
	public void onRemovedFromContainer(GuiContainer container) {
		this.container = null;
	}

	public GuiContainer getContainer() {
		return container;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[x=" + posX + ",y=" + posY + ",w=" + width + ",h=" + height + "]";
	}

}
