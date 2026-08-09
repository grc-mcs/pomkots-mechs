package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Large white spray that keeps its launch velocity and falls in an arc.
 */
public class WaterSplashParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final float initialSize;

    protected WaterSplashParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            SpriteSet spriteSet,
            float minimumSize,
            float sizeVariation,
            double horizontalVelocityScale,
            double upwardVelocity,
            int minimumLifetime,
            int lifetimeVariation) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;

        // The vanilla dust particle damps the supplied velocity heavily.
        // Assign it directly so the spray visibly jumps up and away.
        this.xd = vx * horizontalVelocityScale;
        this.yd = Math.abs(vy) + upwardVelocity;
        this.zd = vz * horizontalVelocityScale;
        this.gravity = 0.75F;
        this.friction = 0.94F;
        this.hasPhysics = false;
        this.lifetime = minimumLifetime + this.random.nextInt(lifetimeVariation);
        this.initialSize = minimumSize + this.random.nextFloat() * sizeVariation;
        this.quadSize = this.initialSize;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 0.95F;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        float progress = (float) this.age / (float) this.lifetime;
        this.quadSize = this.initialSize * (1.0F - progress * 0.55F);
        float fadeProgress = Math.max(0.0F, (progress - 0.65F) / 0.35F);
        this.alpha = 0.95F * (1.0F - fadeProgress);
        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        private final float minimumSize;
        private final float sizeVariation;
        private final double horizontalVelocityScale;
        private final double upwardVelocity;
        private final int minimumLifetime;
        private final int lifetimeVariation;

        public Provider(SpriteSet spriteSet) {
            this(spriteSet, 0.11F, 0.08F, 0.25D, 0.48D, 13, 7);
        }

        protected Provider(
                SpriteSet spriteSet,
                float minimumSize,
                float sizeVariation,
                double horizontalVelocityScale,
                double upwardVelocity,
                int minimumLifetime,
                int lifetimeVariation) {
            this.spriteSet = spriteSet;
            this.minimumSize = minimumSize;
            this.sizeVariation = sizeVariation;
            this.horizontalVelocityScale = horizontalVelocityScale;
            this.upwardVelocity = upwardVelocity;
            this.minimumLifetime = minimumLifetime;
            this.lifetimeVariation = lifetimeVariation;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed) {
            return new WaterSplashParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet,
                    this.minimumSize, this.sizeVariation,
                    this.horizontalVelocityScale, this.upwardVelocity,
                    this.minimumLifetime, this.lifetimeVariation);
        }
    }

    public static class WakeProvider extends Provider {
        public WakeProvider(SpriteSet spriteSet) {
            super(spriteSet, 0.12F, 0.09F, 1.0D, 0.22D, 7, 4);
        }
    }
}
