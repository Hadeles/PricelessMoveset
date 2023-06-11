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
	public static Identifier barTexture = new Identifier("pricelessmoveset", "textures/gui/moveset_bars.png");
	public Dodge dodge;
	public SpinAttack spinAttack;

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
				client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
			return;

		// The bar background is 81 x 9. This includes a 1 pixel thick outline.
		// But the bar fullness, drawn on top, is 79 x 7.
		int textureWidth = 131;
		int textureHeight = 81;
		{
			int barWidth = 9;
			int barHeight = 27;
			int x = client.getWindow().getScaledWidth() / 2 - barWidth / 2; // Aligned with the armor bar
			int y = client.getWindow().getScaledHeight() - 19;
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.setShaderTexture(0, barTexture);

			// Dodge is blue
			{ // Background
				int u = 27;
				int v = 0;
				drawTexture(matrixStack, x - 108, y + 18 - barHeight, u, v, barWidth, barHeight, textureWidth,
						textureHeight);
			}
			{ // Foreground
				int h = (int) (dodge.getFill() * barHeight);
				int u = 18;
				int v = barHeight - h;
				drawTexture(matrixStack, x - 108, y + 18 - h, u, v, barWidth, h, textureWidth, textureHeight);
			}

			// Spin attack is red
			{ // Background
				int u = 9;
				int v = 0;
				drawTexture(matrixStack, x - 98, y + 18 - barHeight, u, v, barWidth, barHeight, textureWidth,
						textureHeight);
			}
			{ // Foreground
				int h = (int) (spinAttack.getFill() * barHeight);
				int u = 0;
				int v = barHeight - h;
				drawTexture(matrixStack, x - 98, y + 18 - h, u, v, barWidth, h, textureWidth, textureHeight);
			}
		}

		// While any cooldown is active, draw an icon over the XP level.
		/*
		 * if (dodge.getFill() > 0.0f || spinAttack.getFill() > 0.0f) {
		 * int u = 124;
		 * int v = 25;
		 * int w = 13;
		 * int h = 15;
		 * int x = client.getWindow().getScaledWidth() / 2 - (w - 1) / 2;
		 * int y = client.getWindow().getScaledHeight() - 42;
		 * drawTexture(matrixStack, x, y, u, v, w, h, textureWidth, textureHeight);
		 * }
		 */
	}
}
