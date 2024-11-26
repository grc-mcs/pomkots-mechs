package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss.HitBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
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

public class EarthraiseEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 20;
    private int lifeTicks = 0;
    private int count;
    private LivingEntity shooter;
    private Vec3 vec;
    private Vec3 knockbackVec;

    public EarthraiseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, Vec3.ZERO, null, 8);
    }

    public EarthraiseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, Vec3 vec, LivingEntity shooter, int count) {
        super(entityType, world);
        this.setNoGravity(true);
        this.shooter = shooter;
        this.vec = vec;
        var tmp = vec.normalize();
        this.knockbackVec = new Vec3(-tmp.x, tmp.y, -tmp.z);
        this.count = count;
    }

    public float getScaleHeight() {
        return 2.0F;
    }

    public float getScaleWidth() {
        return 2.0F;
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);
    @Override
    public void tick() {
        if (this.firstTick && this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), PomkotsMechs.SE_EARTHRAISE_EVENT.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
        }

        this.setNoGravity(true);
        super.tick();

        if (this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        } else if (this.lifeTicks == 3) {
            if (!this.level().isClientSide && count > 0) {
                var pos = this.position();
                EarthraiseEntity e = new EarthraiseEntity(PomkotsMechs.EARTHRAISE.get(), this.level(), vec, shooter, count - 1);
                e.setPos(pos.add(vec));

                e.setYRot((float)(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0));
                this.level().addFreshEntity(e);
            } else {
                generateBlockParticles();
            }

            this.atarihantei();

        }
    }

    private void atarihantei() {
        for (var ent : this.level().getEntities(null, this.getBoundingBox())) {
            if (ent.equals(shooter) || ent instanceof HitBoxEntity) {
                continue;
            }

            if (ent instanceof LivingEntity le) {
                if (!(ent instanceof Pmb01Entity) && !(ent.getVehicle() instanceof Pmb01Entity)) {
                    le.knockback(3, knockbackVec.x, knockbackVec.z);
                }
                le.invulnerableTime = 20;
                le.hurt(this.damageSources().generic(), BattleBalance.BOSS_EARTHRAISE_DAMAGE);
            }
        }

        if (ProjectileUtil.isDestructionAllowed(this)) {
            breakBlocks();
        }
    }

    private void breakBlocks() {
        AABB area = this.getBoundingBox();
        for (BlockPos blockPos : BlockPos.betweenClosed((int)area.minX, (int)area.minY, (int)area.minZ, (int)area.maxX, (int)area.maxY + 10, (int)area.maxZ)) {
            BlockState state = this.level().getBlockState(blockPos);
            // ブロックが空でない場合に破壊
            if (!state.isAir()) {
                this.level().setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void generateBlockParticles() {
        if (this.level().isClientSide) {
            RandomSource random = this.level().getRandom();

            // 100個のLavaパーティクルをバラまく
            for (int i = 0; i < 30; i++) {
                // ランダムな速度を生成
                double velocityX = random.nextDouble() * 2.0 - 1;
                double velocityY = random.nextDouble() * 2.0 - 1;
                double velocityZ = random.nextDouble() * 2.0 - 1;

                // パーティクルをクライアント側で発生させる
                this.level().addAlwaysVisibleParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), true,
                        this.getX() + velocityX, this.getY() + 8, this.getZ() + velocityZ, // 位置
                        velocityX, velocityY, velocityZ // 速度
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        // なんかしらんけどプレイヤーにうまく機能しないので自前で当たり判定やる
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {

    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.earthraise.new"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
