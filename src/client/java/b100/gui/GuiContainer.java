package b100.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiContainer extends GuiElement implements FocusListener {
	
	private final List<GuiElement> elementsMutable = new ArrayList<>();
	public final List<GuiElement> elements = Collections.unmodifiableList(elementsMutable);

	public final ListenerList<ContainerListener> containerListeners = new ListenerList<>(this);
	
	protected boolean isList = false;
	private Focusable lastFocusedElement = null;
	
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
		if(element == null) {
			throw new NullPointerException("Added element is null!");
		}
		elementsMutable.add(element);
		onElementAdded(element);
		element.onAddedToContainer(this);
		return element;
	}
	
	public void onElementAdded(GuiElement element) {
		containerListeners.forEach((e) -> e.elementAdded(this, element));
	}
	
	public boolean remove(GuiElement element) {
		if(elementsMutable.remove(element)) {
			onElementRemoved(element);
			element.onRemovedFromContainer(this);
			return true;
		}
		return false;
	}
	
	public void onElementRemoved(GuiElement element) {
		
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
	
	public void removeAll() {
		for(GuiElement element : new ArrayList<>(elements)) {
			remove(element);
		}
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
	
	/**
	 * Get the first focusable element in this container
	 * <br> Containers in different shapes should override this method
	 * <br> If there is no focusable element, null is returned
	 */
	public Focusable getFirstFocusableElement(FocusDirection direction) {
		int start, dir;
		boolean backwards;
		if(direction.isListNavigation()) {
			backwards = direction.isForwards();
		}else {
			backwards = !direction.isForwards();
		}
		if(!backwards) {
			start = 0;
			dir = 1;
		}else {
			start = elements.size() - 1;
			dir = -1;
		}
		return getNextFocusable(start, dir, direction);
	}
	
	/**
	 * Search for the next focusable element in this container, starting at the given element.
	 * <br> Containers in different shapes should override this method
	 * <br> If there is no next focusable element, null is returned
	 */
	public Focusable getNextFocusable(GuiElement element, FocusDirection direction) {
		if(isList && direction.isTab()) {
			// Next container
			return null;
		}
		if(direction.isListNavigation()) {
			return getFirstFocusableElement(direction);
		}
		int start = elements.indexOf(element);
		if(start == -1) {
			return getFirstFocusableElement(direction);
		}
		int dir = direction.isForwards() ? 1 : -1;
		return getNextFocusable(start + dir, dir, direction);
	}
	
	protected final Focusable getNextFocusable(int start, int dir, FocusDirection direction) {
		for(int i = start; i >= 0 && i < elements.size(); i += dir) {
			GuiElement element = elements.get(i);
			if(element instanceof GuiContainer) {
				GuiContainer container = (GuiContainer) element;
				Focusable focusable = container.getFirstFocusableElement(direction);
				if(focusable != null) {
					return focusable;
				}
			}
			if(Focusable.isFocusable(element)) {
				return (Focusable) element;
			}
		}
		return null;
	}
	
	@Override
	public boolean isSolid() {
		return false;
	}
	
	public Focusable getLastFocusedElement() {
		if(!contains((GuiElement) lastFocusedElement)) {
			lastFocusedElement = null;
		}
		return lastFocusedElement;
	}

	@Override
	public void focusChanged(Focusable focusable) {
		if(focusable.isFocused()) {
			GuiElement element = (GuiElement) focusable;
			if(contains(element)) {
				lastFocusedElement = focusable;
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[x=" + posX + ",y=" + posY + ",w=" + width + ",h=" + height + ",elements=" + elements.size() + "]";
	}

}
