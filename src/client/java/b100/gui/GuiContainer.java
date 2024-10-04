package b100.gui;

import java.util.ArrayList;
import java.util.List;

public class GuiContainer extends GuiElement {
	
	public final List<GuiElement> elements = new ArrayList<>();

	private final List<ContainerListener> containerListeners = new ArrayList<>();
	
	@Override
	public void draw() {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).draw();
		}
	}
	
	@Override
	public boolean keyEvent(int key, int scancode, int modifiers, boolean pressed) {
		for(int i=0; i < elements.size(); i++) {
			if(elements.get(i).keyEvent(key, scancode, modifiers, pressed)) {
				return true;
			}
		}
		return super.keyEvent(key, scancode, modifiers, pressed);
	}
	
	@Override
	public boolean mouseEvent(int button, boolean pressed, double mouseX, double mouseY) {
		for(int i=0; i < elements.size(); i++) {
			if(elements.get(i).mouseEvent(button, pressed, mouseX, mouseY)) {
				return true;
			}
		}
		return super.mouseEvent(button, pressed, mouseX, mouseY);
	}
	
	@Override
	public boolean scrollEvent(double horizontalAmount, double verticalAmount, double mouseX, double mouseY) {
		for(int i=0; i < elements.size(); i++) {
			if(elements.get(i).scrollEvent(horizontalAmount, verticalAmount, mouseX, mouseY)) {
				return true;
			}
		}
		return super.scrollEvent(horizontalAmount, verticalAmount, mouseX, mouseY);
	}
	
	@Override
	public void onResize() {
		for(int i=0; i < elements.size(); i++) {
			elements.get(i).onResize();
		}
		super.onResize();
	}
	
	public <E extends GuiElement> E add(E element) {
		elements.add(element);
		for(ContainerListener containerListener : containerListeners) {
			containerListener.elementAdded(this, element);
		}
		return element;
	}
	
	public boolean contains(GuiElement element) {
		for(int i=0; i < elements.size(); i++) {
			GuiElement e = elements.get(i);
			if(e == element) {
				return true;
			}
			if(e instanceof GuiContainer) {
				GuiContainer container = (GuiContainer) e;
				if(container.contains(element)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public GuiElement getClickElementAt(double x, double y) {
		for(int i = elements.size() - 1; i >= 0; i--) {
			GuiElement element = elements.get(i);
			if(element instanceof GuiContainer) {
				GuiContainer container = (GuiContainer) element;
				GuiElement clickElement = container.getClickElementAt(x, y);
				if(clickElement != null) {
					return clickElement;
				}
			}
			if(element.isSolid() && element.isInside(x, y)) {
				return element;
			}
		}
		return null;
	}
	
	@Override
	public boolean isSolid() {
		return false;
	}
	
	public GuiContainer addContainerListener(ContainerListener containerListener) {
		containerListeners.add(containerListener);
		return this;
	}
	
	public boolean removeContainerListener(ContainerListener containerListener) {
		return containerListeners.remove(containerListener);
	}

}
