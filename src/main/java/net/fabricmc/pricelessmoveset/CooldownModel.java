package net.fabricmc.pricelessmoveset;

import net.minecraft.client.MinecraftClient;

public class CooldownModel {
    public long lastUseTime = 0L;
    public long cooldownTime;

    public CooldownModel(long cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public boolean canUse() {
        return getTime() > lastUseTime + cooldownTime;
    }

    public void use() {
        lastUseTime = getTime();
    }

    public long getTime() {
       return MinecraftClient.getInstance().player.getEntityWorld().getTime();
    }

    public float getFill() {
        float fill = (float)(getTime() - lastUseTime) / (float)(cooldownTime);
        if (fill < 0.0f) fill = 0.0f;
        if (fill > 1.0f) fill = 1.0f;
        return fill;
    }
}
