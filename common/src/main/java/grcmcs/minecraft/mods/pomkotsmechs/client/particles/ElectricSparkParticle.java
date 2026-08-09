package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class ElectricSparkParticle extends TextureSheetParticle {
    private ElectricSparkParticle(ClientLevel level, double x, double y, double z,
                                  double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.lifetime = 6;
        this.gravity = 0.0F;
        this.friction = 0.86F;
        this.hasPhysics = false;
        this.quadSize = 2F + this.random.nextFloat() * 0.35F;

        this.rCol = 0.5F;
        this.gCol = 0.5F;
        this.bCol = 1F;

        this.alpha = 0.95F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.setSprite(sprites.get(this.random));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed && this.age >= this.lifetime - 2) {
            this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 2.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new ElectricSparkParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
