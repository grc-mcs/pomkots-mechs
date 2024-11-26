package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ExplosionCore extends TextureSheetParticle {

    private final SpriteSet spriteSet;

    public ExplosionCore(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
        super(level, x, y, z);

        this.spriteSet = spriteSet;
        this.lifetime = 20; // パーティクルの寿命（20ティック＝1秒）

        this.gravity = 0.0F; // 重力の影響を受けない
        this.quadSize = 10F; // パーティクルサイズの調整

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.setSpriteFromAge(spriteSet); // スプライトシートのアニメーション
        this.hasPhysics = false; // パーティクルが物理エンティティに影響されない

        // 初期カラーを設定（中心部：黄色）
        this.setColor(1.0F, 1.0F, 0.0F); // RGB（黄色）
    }

    @Override
    public void tick() {
        super.tick();
        // 寿命に応じてパーティクルのスプライトを変更する
        this.setSpriteFromAge(this.spriteSet);

        // 半透明効果を加える（消えていくように）
        float lifeRatio = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0F - lifeRatio; // 時間が経つにつれ徐々に透明に

        // パーティクルの寿命に応じて色を変える（中心部は黄色、外縁部は薄いオレンジ）

        // 寿命に応じて黄色から薄いオレンジへグラデーション
        float red = Mth.lerp(lifeRatio, 1.0F, 1.0F); // 赤成分（1.0から1.0）
        float green = Mth.lerp(lifeRatio, 1.0F, 0.5F); // 緑成分（1.0から0.5）
        float blue = Mth.lerp(lifeRatio, 0.0F, 0.0F); // 青成分（常に0.0、黄色とオレンジの範囲）

        // 色を変更する
        this.setColor(red, green, blue);

    }

    @Override
    public int getLightColor(float f) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; // 半透明のパーティクル
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            ExplosionCore particle = new ExplosionCore(world, x, y, z, velocityX, velocityY, velocityZ, spriteSet);
            return particle;
        }
    }
}

