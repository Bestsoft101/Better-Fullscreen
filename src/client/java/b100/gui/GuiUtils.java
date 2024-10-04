package b100.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import b100.fullscreenfix.mixin.access.IScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiUtils {
	
	public static GuiUtils instance = new GuiUtils();
	
	public DrawContext drawContext;
	public TextRenderer textRenderer;
	
	private GuiUtils() {
		
	}
	
	public void setScreen(IScreen screen) {
		if(screen instanceof GuiScreen) {
			GuiScreen screen1 = (GuiScreen) screen;
			MinecraftClient.getInstance().setScreen(new ScreenWrapper(screen1));	
		}else if(screen instanceof Screen) {
			Screen screen1 = (Screen) screen;
			MinecraftClient.getInstance().setScreen(screen1);
		}else if(screen == null) {
			MinecraftClient.getInstance().setScreen(null);
		}
	}
	
	public void drawString(String string, int x, int y, int color, boolean shadow) {
		drawContext.drawText(textRenderer, string, x, y, color, shadow);
		RenderSystem.enableBlend();
		RenderSystem.disableDepthTest();
	}
	
	public void drawString(Text text, int x, int y, int color, boolean shadow) {
		drawString(text.getString(), x, y, color, shadow);
	}

	public void drawCenteredString(Text text, int x, int y, int color, boolean shadow) {
		int width = textRenderer.getWidth(text);
		drawString(text.getString(), x - width / 2, y, color, shadow);
	}
	
	public void drawGuiTexture(Identifier texture, int x, int y, int width, int height) {
		drawContext.drawGuiTexture(texture, x, y, width, height);
	}
	
	public void drawRectangle(int x, int y, int w, int h, int color) {
		drawContext.fill(x, y, x + w, y + h, color);
	}
	
	public void playSound(SoundEvent sound) {
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0f));
	}
	
	public void playSound(RegistryEntry.Reference<SoundEvent> sound) {
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0f));
	}
	
	@SuppressWarnings("resource")
	public boolean isInWorld() {
		return MinecraftClient.getInstance().world != null;
	}

}
