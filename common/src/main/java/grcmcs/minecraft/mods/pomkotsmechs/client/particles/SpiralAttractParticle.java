package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SpiralAttractParticle extends TextureSheetParticle {
    private final double baseRadius;
    private final double spiralSpeed;
    private final double attractionStrength;
    private float angle; // 回転角度
    private Entity target = null;

    protected SpiralAttractParticle(ClientLevel level, double x, double y, double z,
                                    Entity target, SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);
        this.target = target;
        this.baseRadius = 1.5 + this.random.nextDouble() * 0.5;
        this.spiralSpeed = 0.3 + this.random.nextDouble() * 0.2;
        this.attractionStrength = 0.05;
        this.angle = this.random.nextFloat() * (float) Math.PI * 2F;

        this.lifetime = 30 + this.random.nextInt(10);
        this.gravity = 0;
        this.setSpriteFromAge(spriteSet);
        this.quadSize *= 2F; // パーティクルサイズの調整
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (target == null) {
            this.remove();
            return;
        }

        // ターゲットの現在位置（ブースター中心など）
        var targetPos = new Vec3(0, 3.4, -1.5);
        targetPos = target.position().add(targetPos.yRot((float) Math.toRadians((-1.0) * target.getYRot())));

        // 螺旋角度を進める
        angle += spiralSpeed;

        // 現在半径を少しずつ縮める
        double radius = baseRadius * (1.0 - (double) age / lifetime);

        // 螺旋の回転成分
        double offsetX = Math.cos(angle) * radius;
        double offsetZ = Math.sin(angle) * radius;
        double offsetY = Math.sin(angle * 0.3) * 0.5 * radius; // 少し上下動

        // ターゲット中心からの吸引方向ベクトル
        Vec3 currentPos = new Vec3(x, y, z);
        Vec3 attractDir = targetPos.subtract(currentPos).normalize().scale(attractionStrength);

        // 螺旋運動 + 吸引
        this.xd = attractDir.x + (offsetX - (x - targetPos.x)) * 0.05;
        this.yd = attractDir.y + (offsetY - (y - targetPos.y)) * 0.05;
        this.zd = attractDir.z + (offsetZ - (z - targetPos.z)) * 0.05;

        this.move(xd, yd, zd);

        // サイズ・透明度の変化
//        float t = (float) this.age / this.lifetime;
//        this.alpha = 1.0F - t;
//        this.quadSize = 0.05F + (1.0F - t) * 0.05F;
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

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double dx, double dy, double dz) {
            // dxにエンティティIDを入れる
            Entity target = Minecraft.getInstance().level.getEntity((int)dx);
            return new SpiralAttractParticle(level, x, y, z, target, spriteSet);
        }
    }
}
