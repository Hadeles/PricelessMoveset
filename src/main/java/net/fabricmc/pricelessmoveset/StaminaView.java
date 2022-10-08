package net.fabricmc.pricelessmoveset;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

public class StaminaView
extends DrawableHelper
implements HudRenderCallback {
    public static Identifier barTexture = new Identifier("pricelessmoveset", "textures/gui/stamina_bar.png");
	public StaminaModel staminaModel;

	public StaminaView(StaminaModel staminaModel) {
		this.staminaModel = staminaModel;
	}

	@Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		float fillFraction = (float)(staminaModel.stamina) / (float)(StaminaModel.MAX_STAMINA);

		if (client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE ||
			client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) return;

		// The bar background is 81 x 9. This includes a 1 pixel thick outline.
		// But the bar fullness, drawn on top, is 79 x 7.
		int barWidth = 81;
		int barHeight = 9;
		int textureWidth = 131;
		int textureHeight = 81;
		int x = client.getWindow().getScaledWidth() / 2 - barWidth - 10;  // Aligned with the armor bar
		int y = client.getWindow().getScaledHeight() - 49;  // Just above the armor bar
		if (client.player.getArmor() > 0) y = y - 10;
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShaderTexture(0, barTexture);

		// Draw the empty bar below
		int u = 0;
		int v = 10;
		drawTexture(matrixStack, x, y, u, v, barWidth, barHeight, textureWidth, textureHeight);

		// Overdraw the full bar.
		u = 0;
		v = 0;
		int w = (int)(fillFraction * (barWidth - 2) + 1); // Remove the outline
		drawTexture(matrixStack, x, y, u, v, w, barHeight, textureWidth, textureHeight);
	}
}
