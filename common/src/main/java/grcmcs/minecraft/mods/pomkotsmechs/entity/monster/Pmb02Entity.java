package grcmcs.minecraft.mods.pomkotsmechs.entity.monster;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmb02Entity extends PathfinderMob implements GeoEntity, GeoAnimatable, Enemy {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    private int lifeTick = 0;

    public Pmb02Entity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);

        this.setHealth(100);
        this.setMaxUpStep(1.0F);
        this.setNoGravity(true);

        this.setPersistenceRequired();
        this.noCulling = true;
    }


    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);

        super.tick();

        GameType gameMode = null;
        if (this.level().isClientSide) {
            gameMode = Minecraft.getInstance().gameMode.getPlayerMode();
        } else {
            var list = this.level().getServer().getPlayerList().getPlayers();
            if (list == null || list.isEmpty()) {
                gameMode = GameType.CREATIVE;
            } else {
                gameMode = this.level().getServer().getPlayerList().getPlayers().get(0).gameMode.getGameModeForPlayer();
            }
        }

        if (gameMode.equals(GameType.SURVIVAL)) {
            lifeTick++;

            if (lifeTick < 10) {
                this.moveTo(this.position().add(new Vec3(0,1,0)));
            }

            int cnt = lifeTick % 300;
            if (cnt == 100) {
                for (int i = 0; i < 5; i++) {
                    MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                    be.setPos(this.position().add(new Vec3(-10, i, 0)));
                    be.shootFromRotation(be, 0, this.getYRot() + 20, this.getFallFlyingTicks(), 1.5F, 0F);
                    this.level().addFreshEntity(be);
                }

                for (int i = 0; i < 5; i++) {
                    MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                    be.setPos(this.position().add(new Vec3(10, i, 0)));
                    be.shootFromRotation(be, 0, this.getYRot() - 20, this.getFallFlyingTicks(), 1.5F, 0F);
                    this.level().addFreshEntity(be);
                }
            }

            if (cnt == 200) {
                for (int i = 0; i < 5; i++) {
                    MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                    be.setPos(this.position().add(new Vec3(3-i, 0, 10)));
                    be.shootFromRotation(be, 10, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0F);
                    this.level().addFreshEntity(be);
                }

                for (int i = 0; i < 5; i++) {
                    MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                    be.setPos(this.position().add(new Vec3(3-i, -1, 10)));
                    be.shootFromRotation(be, -10, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0F);
                    this.level().addFreshEntity(be);
                }
            }


            if (cnt == 299) {
                int mNum = 20;

                for (int i = 0; i < mNum; i++) {
                    for (int j = 0; j < mNum; j++) {
                        MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                        be.setPos(this.position());
                        be.shootFromRotation(be, -i * (90/mNum), -90 + (j * (180/mNum)), this.getFallFlyingTicks(), 1.5F, 0F);
                        this.level().addFreshEntity(be);

                    }
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        // カメラからの距離に関係なくレンダリングする
        double maxDistance = 1000; // 必要に応じて距離を設定
//        return this.distanceToSqr(cameraX, cameraY, cameraZ) < maxDistance * maxDistance;
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    public boolean isPersistenceRequired() {
        // デスポーンを防ぐために常にtrueを返す
        return true;
    }

    @Override
    public void checkDespawn() {
        // デスポーン処理を無効にするために空にする
        // これにより、通常のデスポーン条件が適用されなくなる
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(5.0); // 必要に応じて調整
    }
}
