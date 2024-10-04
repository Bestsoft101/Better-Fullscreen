package b100.gui;

import net.minecraft.util.Identifier;

public class Textures {
	
	public static final Textures INSTANCE = new Textures();
	
	private Textures() {
		
	}
	
	public final Identifier buttonHover = Identifier.ofVanilla("widget/button_highlighted");
	public final Identifier buttonNormal = Identifier.ofVanilla("widget/button");
	public final Identifier buttonDisabled = Identifier.ofVanilla("widget/button_disabled");

	public final Identifier scroller = Identifier.ofVanilla("widget/scroller");
	public final Identifier scrollerBackground = Identifier.ofVanilla("widget/scroller_background");
	
	public final GuiTextures guiTextures = new GuiTextures(false);
	public final GuiTextures guiTexturesInWorld = new GuiTextures(true);
	
	public GuiTextures getCurrentGuiTextures() {
		return GuiUtils.instance.isInWorld() ? guiTexturesInWorld : guiTextures;
	}
	
	public static class GuiTextures {
		
		public final Identifier menuBackground;
		public final Identifier menuListBackground;
		public final Identifier headerSeparator;
		public final Identifier footerSeparator;
		
		public GuiTextures(boolean inWorld) {
			menuBackground = Identifier.ofVanilla(inWorld ? "textures/gui/inworld_menu_background.png" : "textures/gui/menu_background.png");
			menuListBackground = Identifier.ofVanilla(inWorld ? "textures/gui/inworld_menu_list_background.png" : "textures/gui/menu_list_background.png");
			headerSeparator = Identifier.ofVanilla(inWorld ? "textures/gui/inworld_header_separator.png" : "textures/gui/header_separator.png");
			footerSeparator = Identifier.ofVanilla(inWorld ? "textures/gui/inworld_footer_separator.png" : "textures/gui/footer_separator.png");
		}
	}

}
