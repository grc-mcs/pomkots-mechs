package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss.HitBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EarthbreakEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 20;
    private int lifeTicks = 0;
    private LivingEntity shooter;

    public EarthbreakEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.shooter = null;
    }

    public EarthbreakEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        super(entityType, world);
        this.setNoGravity(true);
        this.shooter = shooter;
    }

    @Override
    public void tick() {
        if (this.firstTick && this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), PomkotsMechs.SE_EARTHBREAK_EVENT.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
        }

        this.setNoGravity(true);
        super.tick();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        } else if (this.lifeTicks == 1) {
            atarihantei();
        }
    }

    private void atarihantei() {
        for (var ent : this.level().getEntities(null, this.getBoundingBox())) {
            if (ent.equals(shooter) || ent instanceof HitBoxEntity) {
                continue;
            }

            if (ent instanceof LivingEntity le) {
                if (!(ent instanceof Pmb01Entity) && !(ent.getVehicle() instanceof Pmb01Entity)) {
                    le.addDeltaMovement(new Vec3(0, 5, 0));
                }
                le.invulnerableTime = 20;
                le.hurt(this.damageSources().generic(), BattleBalance.BOSS_EARTHBREAK_DAMAGE);
            }
        }

        if (ProjectileUtil.isDestructionAllowed(this)) {
            breakBlocks(35);
        }
    }

    private void breakBlocks(int radius) {
        BlockPos pos = this.blockPosition(); // ロボットの現在位置

        // 円柱の高さを設定
        int height = 15; // 高さ2ブロックの円柱
        for (int y = 0; y < height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // 半径内のブロックをチェック
                    if (x * x + z * z <= radius * radius) {
                        BlockPos blockPos = pos.offset(x, y, z);
                        BlockState state = this.level().getBlockState(blockPos);
                        // ブロックが空でない場合に破壊
                        if (!state.isAir()) {
                            this.level().setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
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
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.earthbreak.new"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
