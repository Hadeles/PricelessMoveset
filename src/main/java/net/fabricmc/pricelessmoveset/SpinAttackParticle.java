// Forked from ShriekParticle.

package net.fabricmc.pricelessmoveset;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

@Environment(value = EnvType.CLIENT)
public class SpinAttackParticle
        extends SpriteBillboardParticle {
    private SpriteProvider spriteProvider;
    private static final Vec3f field_38334 = Util.make(new Vec3f(0.5f, 0.5f, 0.5f), Vec3f::normalize);
    private static final Vec3f field_38335 = new Vec3f(-1.0f, -1.0f, 0.0f);

    SpinAttackParticle(ClientWorld clientWorld, double d, double e, double f, SpriteProvider spriteProvider) {
        super(clientWorld, d, e, f, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;
        this.scale = 1.75f;
        this.maxAge = 7;
        this.alpha = 1.0f;
        this.gravityStrength = 0.0f;
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        this.method_42583(vertexConsumer, camera, tickDelta, quaternion -> {
            quaternion.hamiltonProduct(Vec3f.POSITIVE_Y.getRadialQuaternion(0.0f));
            quaternion.hamiltonProduct(Vec3f.POSITIVE_X.getRadialQuaternion((float) (-Math.PI / 2.0)));
        });
        this.method_42583(vertexConsumer, camera, tickDelta, quaternion -> {
            quaternion.hamiltonProduct(Vec3f.POSITIVE_Y.getRadialQuaternion((float) (-Math.PI)));
            quaternion.hamiltonProduct(Vec3f.POSITIVE_X.getRadialQuaternion((float) (Math.PI / 2.0)));
        });
    }

    private void method_42583(VertexConsumer vertexConsumer, Camera camera, float f, Consumer<Quaternion> consumer) {
        int k;
        Vec3d vec3d = camera.getPos();
        float g = (float) (MathHelper.lerp((double) f, this.prevPosX, this.x) - vec3d.getX());
        float h = (float) (MathHelper.lerp((double) f, this.prevPosY, this.y) - vec3d.getY());
        float i = (float) (MathHelper.lerp((double) f, this.prevPosZ, this.z) - vec3d.getZ());
        Quaternion quaternion = new Quaternion(field_38334, 0.0f, true);
        consumer.accept(quaternion);
        field_38335.rotate(quaternion);
        Vec3f[] vec3fs = new Vec3f[] { new Vec3f(-1.0f, -1.0f, 0.0f), new Vec3f(-1.0f, 1.0f, 0.0f),
                new Vec3f(1.0f, 1.0f, 0.0f), new Vec3f(1.0f, -1.0f, 0.0f) };
        float j = this.getSize(f);
        for (k = 0; k < 4; ++k) {
            Vec3f vec3f = vec3fs[k];
            vec3f.rotate(quaternion);
            vec3f.scale(j);
            vec3f.add(g, h, i);
        }
        k = this.getBrightness(f);
        this.method_42584(vertexConsumer, vec3fs[0], this.getMaxU(), this.getMaxV(), k);
        this.method_42584(vertexConsumer, vec3fs[1], this.getMaxU(), this.getMinV(), k);
        this.method_42584(vertexConsumer, vec3fs[2], this.getMinU(), this.getMinV(), k);
        this.method_42584(vertexConsumer, vec3fs[3], this.getMinU(), this.getMaxV(), k);
    }

    private void method_42584(VertexConsumer vertexConsumer, Vec3f vec3f, float f, float g, int i) {
        vertexConsumer.vertex(vec3f.getX(), vec3f.getY(), vec3f.getZ()).texture(f, g)
                .color(this.red, this.green, this.blue, this.alpha).light(i).next();
    }

    @Override
    public int getBrightness(float tint) {
        return 240;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        this.setSpriteForAge(this.spriteProvider); //// SKIPS FRAMES FROM ANIMATION

        // super.tick();
    }

    @Environment(value = EnvType.CLIENT)
    public static class Factory
            implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d,
                double e, double f, double g, double h, double i) {
            SpinAttackParticle spinAttackParticle = new SpinAttackParticle(clientWorld, d, e, f, this.spriteProvider);
            spinAttackParticle.setSprite(this.spriteProvider);
            spinAttackParticle.setAlpha(1.0f);
            return spinAttackParticle;
        }
    }
}
