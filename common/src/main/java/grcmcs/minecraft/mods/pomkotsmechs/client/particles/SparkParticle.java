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
        this.lifetime = 20; // パーティクルの寿命（20ティック＝1秒）
        this.gravity = 0.9F; // 重力の影響を受けない
        this.quadSize *= 2F; // パーティクルサイズの調整
        this.setSpriteFromAge(spriteSet); // スプライトシートのアニメーション
        this.hasPhysics = false; // パーティクルが物理エンティティに影響されない
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
//        float g = ((float)this.age + f) / (float)this.lifetime;
        float g = (float)this.lifetime / ((float)this.age + f);
        g = Mth.clamp(g, 0.0F, 1.0F);
        int i = super.getLightColor(f);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(g * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
        // return 15728880;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Particle p = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
//            p.scale(4.0F);
            p.setLifetime(20);
            return p;
        }
    }
}
