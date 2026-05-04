package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class DustHeavyParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public DustHeavyParticle(ClientLevel level, double x, double y, double z,
                             double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);

        // 見た目設定
        this.rCol = 0.1f;
        this.gCol = 0.1f;
        this.bCol = 0.1f;
        this.alpha = 1f;
        this.lifetime = 10;
        this.hasPhysics = false;
        this.gravity = 0.5f;
        this.spriteSet = spriteSet;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Particle p = new DustHeavyParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            p.setLifetime((int)(10 * Math.random()) + 10);
            p.scale(0.5F);
            return p;
        }
    }
}
