package net.fabricmc.pricelessmoveset;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StaminaRenderer
extends DrawableHelper
implements HudRenderCallback {
    public static Identifier barTexture = new Identifier("pricelessmoveset", "textures/gui/stamina_bar.png");
	public float fillFraction;

	@Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
		int barWidth = 81;
		int barHeight = 9;
		int textureWidth = 131;
		int textureHeight = 81;
		MinecraftClient client = MinecraftClient.getInstance();
		int x = client.getWindow().getScaledWidth() / 2 - barWidth - 10;  // Aligned with the armor bar
		int y = client.getWindow().getScaledHeight() - 59;  // Just above the armor bar
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShaderTexture(0, barTexture);
		// Draw the empty bar below
		int u = 0;
		int v = 10;
		drawTexture(matrixStack, x, y, u, v, barWidth, barHeight, textureWidth, textureHeight);
		// Overdraw the full bar
		u = 0;
		v = 0;
		int w = (int)(fillFraction * barWidth);
		drawTexture(matrixStack, x, y, u, v, w, barHeight, textureWidth, textureHeight);
	}

	public void setFillFraction(float fillFraction) {
		this.fillFraction = fillFraction;
	}
}
