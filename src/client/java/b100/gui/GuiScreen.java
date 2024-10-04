package b100.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import b100.fullscreenfix.mixin.access.IScreen;
import net.minecraft.text.Text;

public abstract class GuiScreen extends GuiContainer implements IScreen, FocusListener, ContainerListener {
	
	private boolean initialized = false;
	
	public double mouseX = -1;
	public double mouseY = -1;
	
	public IScreen parentScreen;
	
	/**
	 * Only one element per screen should be focused
	 */
	protected Focusable focusedElement;
	
	private final List<ScreenListener> screenListeners = new ArrayList<>();
	
	private ScreenWrapper wrapper;
	
	public GuiScreen(IScreen parentScreen) {
		this.parentScreen = parentScreen;
		
		addContainerListener(this);
	}
	
	public void setWrapper(ScreenWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public final void init() {
		if(initialized) {
			return;
		}
		
		onInit();
		
		initialized = true;
	}
	
	/**
	 *      Called one time when the screen is first created.
	 * <br> Should only be used to create GUI elements, positioning is done in {@link GuiScreen#onResize()}
	 */
	protected abstract void onInit();
	
	/**
	 * Called once when the screen is created and every time the size of the game window changed, should be used to position GUI Elements
	 */
	@Override
	public void onResize() {
		super.onResize();
	}
	
	@Override
	public boolean keyEvent(int key, int scancode, int modifiers, boolean pressed) {
		if(pressed && key == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		
		if(super.keyEvent(key, scancode, modifiers, pressed)) {
			return true;
		}
		
		if(pressed && key == GLFW.GLFW_KEY_BACKSPACE) {
			back();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void elementAdded(GuiContainer parent, GuiElement element) {
		// Add a FocusListener to all elements added to this screen
		if(element instanceof Focusable) {
			Focusable focusable = (Focusable) element;
			focusable.addFocusListener(this);
		}
		// Add a ScreenListener to all elements added to this screen
		if(element instanceof ScreenListener) {
			ScreenListener screenListener = (ScreenListener) element;
			addScreenListener(screenListener);
		}
		
		// Add a ContainerListener to all containers added to this screen,
		// to make sure that Focusable elements in subcontainers also get a FocusListener
		if(element instanceof GuiContainer) {
			GuiContainer container = (GuiContainer) element;
			
			// Make sure elements that were already added also get a FocusListener
			container.elements.forEach((e) -> elementAdded(container, e));
			
			container.addContainerListener(this);
		}
	}
	
	@Override
	public void focusChanged(Focusable focusable) {
		if(focusable.isFocused()) {
			if(focusedElement != null) {
				focusedElement.setFocused(false);
			}
			this.focusedElement = focusable;
		}
	}
	
	public GuiElement getMouseOver() {
		return getClickElementAt(mouseX, mouseY);
	}
	
	public boolean isMouseOver(GuiElement element) {
		return getClickElementAt(mouseX, mouseY) == element;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void back() {
		utils.setScreen(parentScreen);
	}
	
	public void close() {
		utils.setScreen(null);
	}
	
	public void onScreenOpened() {
		for(ScreenListener screenListener : screenListeners) {
			screenListener.onScreenOpened(this);
		}
	}
	
	public GuiElement addScreenListener(ScreenListener screenListener) {
		screenListeners.add(screenListener);
		return this;
	}
	
	public boolean removeScreenListener(ScreenListener screenListener) {
		return screenListeners.remove(screenListener);
	}
	
	public void setTooltip(Text tooltip) {
		wrapper.setTooltip(tooltip);
	}

}
