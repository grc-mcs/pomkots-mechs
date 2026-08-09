package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class DustParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public DustParticle(ClientLevel level, double x, double y, double z,
                        double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);

        // 見た目設定
        this.rCol = 0.6f;
        this.gCol = 0.5f;
        this.bCol = 0.4f;
        this.alpha = 0.8f;
        this.lifetime = 10;
        this.hasPhysics = true;
        this.friction = 0.9f;
        this.gravity = 0.05f;
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
            Particle p = new DustParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            p.setLifetime((int)(5 * Math.random()) + 5);
            return p;
        }
    }
}
