package net.fabricmc.pricelessmoveset;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

public class CooldownView
extends DrawableHelper
implements HudRenderCallback {
    public static Identifier barTexture = new Identifier("pricelessmoveset", "textures/gui/stamina_bar.png");
	public Dodge dodge;
	public SpinAttack spinAttack;

	public StaminaModel staminaModel;

	public CooldownView(
		Dodge dodge,
		SpinAttack spinAttack) {
		// Scott hates this
		this.dodge = dodge;
		this.spinAttack = spinAttack;
	}

	@Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE ||
			client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) return;

		// The bar background is 81 x 9. This includes a 1 pixel thick outline.
		// But the bar fullness, drawn on top, is 79 x 7.
		int barWidth = 182;
		int barHeight = 5;
		int textureWidth = 203;
		int textureHeight = 81;
		int x = client.getWindow().getScaledWidth() / 2 - barWidth / 2;  // Aligned with the armor bar
		int y = client.getWindow().getScaledHeight() - 19;
		if (client.player.getArmor() > 0) y = y - 10;
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShaderTexture(0, barTexture);

		// Dodge is blue
		{
			int u = 0;
			int v = 50;
			int w = (int)(dodge.getFill() * barWidth);
			drawTexture(matrixStack, x, y, u, v, w, barHeight, textureWidth, textureHeight);
		}

		// Spin attack is red
		{
			int u = 0;
			int v = 45;
			int w = (int)(spinAttack.getFill() * barWidth);
			drawTexture(matrixStack, x, y, u, v, w, barHeight, textureWidth, textureHeight);
		}
	}
}
