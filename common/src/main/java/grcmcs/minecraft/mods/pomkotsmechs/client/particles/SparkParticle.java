package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class SparkParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    protected SparkParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(level, x, y, z, vx, vy, vz);
        this.spriteSet = spriteSet;
        this.lifetime = 20;
        this.gravity = 0.9F;
        this.quadSize *= 2F;
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);
    @Override
    public void tick() {
        super.tick();
    }


    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Particle p = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);

            if (typeIn == PomkotsMechs.SPARK_RED.get()) {
                p.setColor(1.0f, 0.1f, 0.0f);
            } else if (typeIn == PomkotsMechs.SPARK_ORANGE.get()) {
                p.setColor(1.0f, 0.5f, 0.0f);
            } else {
                p.setColor(1.0f, 1.0f, 0.0f);
            }

            p.setLifetime(20);
            return p;
        }
    }
}
