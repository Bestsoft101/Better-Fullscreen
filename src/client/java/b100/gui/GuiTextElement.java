package b100.gui;

import net.minecraft.text.Text;

public class GuiTextElement extends GuiElement {
	
	public Text text;
	public boolean shadow;
	public int color;
	
	public GuiTextElement(Text text, boolean shadow, int color) {
		this.text = text;
		this.shadow = shadow;
		this.color = color;
	}
	
	@Override
	public void draw() {
		utils.drawCenteredString(text, posX + width / 2, posY + height / 2 - 4, color, shadow);
	}
	
}