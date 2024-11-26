package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class MissileSmokeParticle extends TextureSheetParticle {

    protected MissileSmokeParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);

        this.gravity = -0.0F;
        this.friction = 0.9F;
        this.xd = vx + (Math.random() * 2.0 - 1.0) * 0.01000000074505806;
        this.yd = vy + (Math.random() * 2.0 - 1.0) * 0.01000000074505806;
        this.zd = vz + (Math.random() * 2.0 - 1.0) * 0.01000000074505806;

        this.pickSprite(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Particle p = new MissileSmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            p.scale(1F);
            p.setLifetime((int)(40 * Math.random()) + 20);
            return p;
        }
    }
}
