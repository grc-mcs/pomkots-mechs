package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

import java.util.LinkedList;
import java.util.List;

public class Pmv03pEntity extends PomkotsVehicleBase {
    @Override
    protected String getMechName() {
        return "pmv02";
    }
    
    public static final float DEFAULT_SCALE = 1f;

    public Pmv03pEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected static final int ACT_GATLING = 2;
    protected static final int ACT_MISSILE = 3;
    protected static final int ACT_HUMMER = 4;
    protected static final int ACT_ROLLER = 5;
    protected static final int ACT_BLOCKINJECT = 6;
    protected static final int ACT_VACUME = 7;
    protected static final int ACT_PLACE = 8;
    protected static final int ACT_LIFT_BLOCK = 9;
    protected static final int ACT_THROW = 10;

    @Override
    protected void registerActions() {
        super.registerActions();
        this.actionController.registerAction(ACT_GATLING, new Action(0, 7, 2), ActionController.ActionType.R_ARM_MAIN);
        this.actionController.registerAction(ACT_MISSILE, new Action(60, 2, 18), ActionController.ActionType.L_ARM_MAIN);
        this.actionController.registerAction(ACT_HUMMER, new Action(20, 40, 20), ActionController.ActionType.R_SHL_MAIN);
        this.actionController.registerAction(ACT_ROLLER, new Action(0, 20, 2), ActionController.ActionType.L_SHL_MAIN);
        this.actionController.registerAction(ACT_BLOCKINJECT, new Action(0, 5, 5), ActionController.ActionType.R_ARM_SUB);
        this.actionController.registerAction(ACT_VACUME, new Action(0, 5, 5), ActionController.ActionType.L_ARM_SUB);
        this.actionController.registerAction(ACT_LIFT_BLOCK, new Action(20, 8, 2), ActionController.ActionType.R_SHL_SUB);
        this.actionController.registerAction(ACT_THROW, new Action(20, 3, 7), ActionController.ActionType.R_SHL_SUB);
        this.actionController.registerAction(ACT_PLACE, new Action(0, 20, 2), ActionController.ActionType.L_SHL_SUB);

    }

    protected boolean useEnergy(int dec) {
        return true;
    }

    @Override
    public void tick() {
        if (this.isMainMode()) {
            super.tick();

        } else {
            setNoGravity(true);
            super.tick();
            this.moveForward();
        }

    }


    private void moveForward() {
        // ピッチ（xRot）とヨー（yRot）に基づいて移動量を計算
        double xRotRadians = Math.toRadians(this.getXRot());
        double yRotRadians = Math.toRadians(this.getYRot());

        double speed = this.getSpeed();
        double vx = -speed * Math.cos(xRotRadians) * Math.sin(yRotRadians);
        double vy = -speed * Math.sin(xRotRadians);
        double vz = speed * Math.cos(xRotRadians) * Math.cos(yRotRadians);

        // エンティティを移動
        this.setDeltaMovement(vx, vy, vz);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }


    @Override
    public void travel(Vec3 pos) {
        if (this.isMainMode()) {
            super.travel(pos);
        } else {
//            this.setSpeed(1);
//            pos = this.calculateNextPosition(pos, this.getXRot(), this.getYRot(), this.getSpeed());
            this.travel2(pos);
        }
    }

    public Vec3 calculateNextPosition(Vec3 cur, double xRot, double yRot, double speed) {
        // 度をラジアンに変換
        double xRotRadians = Math.toRadians(xRot);
        double yRotRadians = Math.toRadians(yRot);

        // 速度ベクトルの計算
        double vx = -speed * Math.cos(xRotRadians) * Math.sin(yRotRadians);
        double vy = -speed * Math.sin(xRotRadians);
        double vz = speed * Math.cos(xRotRadians) * Math.cos(yRotRadians);

        // 次の座標
        double nextX = cur.x + vx;
        double nextY = cur.y + vy;
        double nextZ = cur.z + vz;

        // 結果をVec3として返す
        return new Vec3(nextX, nextY, nextZ);
    }

    @Override
    protected void applyPlayerInput(DriverInput driverInput) {
        if (this.isMainMode()) {
            this.getUserIntentionForDirectionFromKey(driverInput);

            this.applyPlayerInputWeapons(driverInput);
            this.applyPlayerInputBoost(driverInput);
            this.applyPlayerInputEvasion(driverInput);
            this.applyPlayerInputJump(driverInput);
            this.applyPlayerInputInAirActions(driverInput);
        } else {
            this.getUserIntentionForDirectionFromKey(driverInput);
            this.applyPlayerInputWeapons(driverInput);

            if (driverInput.isForwardPressed()) {
                this.setXRot(this.getXRot() - 5);
            }
            if (driverInput.isBackPressed()) {
                this.setXRot(this.getXRot() + 5);
            }
            if (driverInput.isLeftPressed()) {
                this.setYRot(this.getYRot() - 5);
            }
            if (driverInput.isRightPressed()) {
                this.setYRot(this.getYRot() + 5);
            }

//            this.setXRot(Mth.clamp(this.getXRot(), -90, 90)); // ピッチは上下90度まで

            this.yRotO = this.getYRot();
            this.setYBodyRot(this.getYRot());
            this.setYHeadRot(this.getYRot());
//            this.setRot(this.getYRot(), this.getXRot());

            this.hasImpulse = true;
        }
    }

    @Override
    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        if (driverInput.isWeaponRightHandPressed()) {
            this.actionController.getAction(ACT_GATLING).startAction();
        } else if (driverInput.isWeaponLeftHandPressed()) {
            this.actionController.getAction(ACT_MISSILE).startAction();
        }
    }

    @Override
    protected void fireWeapons() {
        Level level = level();

        if (actionController.getAction(ACT_GATLING).isOnFire()) {
            this.fireGatling(level);
        } else if (actionController.getAction(ACT_MISSILE).isOnFire()) {
            this.fireMissile(level);
        }
    }

    public void fireGatling(Level world) {
        if (!world.isClientSide()) {
            for (int i = 0; i < 2; i++) {
                BulletEntity be = new BulletEntity(PomkotsMechs.BULLET.get(), world, this);

                // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
                // ので、3tick前の座標をオフセットにする
                // なんかaddVelocity周りが悪さしてる…？
                var offset = posHistory.getFirst();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(0.5 - i, 3.3F, 6.5F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
                be.setPos(offset.add(muzzlPos));

                be.shootFromRotation(be, this.getXRot(), this.getYRot(), this.getFallFlyingTicks(), 0.9F, 2F);

                world.addFreshEntity(be);
            }
        } else {
            if (tickCount % 7 == 0) {
                playSoundEffect(PomkotsMechs.SE_GATLING_EVENT.get());
            }
        }
    }

    private void fireMissile(Level world) {
        if (!world.isClientSide()) {
            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-1.0, 3.5F, 0.8F);
            var worldMuzzlPos = muzzlPos.add(0, 0, 3).yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_HORIZONTAL.get(), world, this);

            be.setPos(offset.add(worldMuzzlPos));

            be.shootFromRotation(be, this.getXRot(), this.getYRot(), this.getFallFlyingTicks(), 1F, 0F);

            world.addFreshEntity(be);
        } else {
            playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
            lockTargets.clearLockTargetsMulti();
        }
    }


    @Override
    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
//        if (this.actionController.getAction(ACT_LIFT_BLOCK).isInAction()) {
//            if (this.actionController.getAction(ACT_LIFT_BLOCK).isOnStart()) {
//                event.getController().forceAnimationReset();
//            }
//            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".raise"));
//        } else if (this.actionController.getAction(ACT_THROW).isInAction()) {
//            if (this.actionController.getAction(ACT_THROW).isOnStart()) {
//                event.getController().forceAnimationReset();
//            }
//            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".throw"));
//
//        }

        return null;
    }

    protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
        if (this.isMainMode()) {
            return super.controllAnimationBasicMove(event);
        } else {
            return null;
        }
    }

    protected PlayState controllAnimationFlyingMotion(AnimationState<PomkotsVehicleBase> event) {
        if (this.isMainMode()) {
            return super.controllAnimationFlyingMotion(event);
        } else {
            return null;
        }
    }

    protected PlayState controllAnimationRotation(AnimationState<PomkotsVehicleBase> event) {
        if (this.isMainMode()) {
            return super.controllAnimationRotation(event);
        } else {
            return null;
        }
    }

    @Override
    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "props", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".prop"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".prop"));
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "tires", 0, event -> {
            if (event.isMoving() && event.getAnimatable().onGround()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".tire"));
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));
    }

    @Override
    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        if ("se_jump".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_booster".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get());
        } else if ("se_onground".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_drill1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_DRILL1.get());
        } else if ("se_drill2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_DRILL2.get());
        } else if ("se_hummer1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_HUMMER1.get());
        } else if ("se_hummer2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_HUMMER2.get());
        } else if ("se_lift".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_LIFT.get());
        } else if ("se_needle".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_NEEDLE.get());
        } else if ("se_roller1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER1.get());
        } else if ("se_roller2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER2.get());
        } else if ("se_step".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_STEP.get());
        } else if ("se_throw".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_THROW.get());
        } else if ("se_water".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_WATER.get());
        }
    }

    @Override
    public boolean shouldRenderDefaultHud(String hudName) {
        return "renderHotbar".equals(hudName);
    }

    public void travel2(Vec3 vec3) {
        if (this.isControlledByLocalInstance()) {
            double d = 0.08;
            boolean bl = this.getDeltaMovement().y <= 0.0;
            if (bl && this.hasEffect(MobEffects.SLOW_FALLING)) {
                d = 0.01;
            }

            FluidState fluidState = this.level().getFluidState(this.blockPosition());
            float f;
            double e;
            if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
                e = this.getY();
                f = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float g = 0.02F;
                float h = (float) EnchantmentHelper.getDepthStrider(this);
                if (h > 3.0F) {
                    h = 3.0F;
                }

                if (!this.onGround()) {
                    h *= 0.5F;
                }

                if (h > 0.0F) {
                    f += (0.54600006F - f) * h / 3.0F;
                    g += (this.getSpeed() - g) * h / 3.0F;
                }

                if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                    f = 0.96F;
                }

                this.moveRelative(g, vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 vec32 = this.getDeltaMovement();
                if (this.horizontalCollision && this.onClimbable()) {
                    vec32 = new Vec3(vec32.x, 0.2, vec32.z);
                }

                this.setDeltaMovement(vec32.multiply((double)f, 0.800000011920929, (double)f));
                Vec3 vec33 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
                this.setDeltaMovement(vec33);
                if (this.horizontalCollision && this.isFree(vec33.x, vec33.y + 0.6000000238418579 - this.getY() + e, vec33.z)) {
                    this.setDeltaMovement(vec33.x, 0.30000001192092896, vec33.z);
                }
            } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
                e = this.getY();
                this.moveRelative(0.02F, vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 vec34;
                if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.800000011920929, 0.5));
                    vec34 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
                    this.setDeltaMovement(vec34);
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
                }

                if (!this.isNoGravity()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d / 4.0, 0.0));
                }

                vec34 = this.getDeltaMovement();
                if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6000000238418579 - this.getY() + e, vec34.z)) {
                    this.setDeltaMovement(vec34.x, 0.30000001192092896, vec34.z);
                }
            } else if (this.isFallFlying()) {
                this.checkSlowFallDistance();
                Vec3 vec35 = this.getDeltaMovement();
                Vec3 vec36 = this.getLookAngle();
                f = this.getXRot() * 0.017453292F;
                double i = Math.sqrt(vec36.x * vec36.x + vec36.z * vec36.z);
                double j = vec35.horizontalDistance();
                double k = vec36.length();
                double l = Math.cos((double)f);
                l = l * l * Math.min(1.0, k / 0.4);
                vec35 = this.getDeltaMovement().add(0.0, d * (-1.0 + l * 0.75), 0.0);
                double m;
                if (vec35.y < 0.0 && i > 0.0) {
                    m = vec35.y * -0.1 * l;
                    vec35 = vec35.add(vec36.x * m / i, m, vec36.z * m / i);
                }

                if (f < 0.0F && i > 0.0) {
                    m = j * (double)(-Mth.sin(f)) * 0.04;
                    vec35 = vec35.add(-vec36.x * m / i, m * 3.2, -vec36.z * m / i);
                }

                if (i > 0.0) {
                    vec35 = vec35.add((vec36.x / i * j - vec35.x) * 0.1, 0.0, (vec36.z / i * j - vec35.z) * 0.1);
                }

                this.setDeltaMovement(vec35.multiply(0.9900000095367432, 0.9800000190734863, 0.9900000095367432));
                this.move(MoverType.SELF, this.getDeltaMovement());
                if (this.horizontalCollision && !this.level().isClientSide) {
                    m = this.getDeltaMovement().horizontalDistance();
                    double n = j - m;
                    float o = (float)(n * 10.0 - 3.0);
                    if (o > 0.0F) {
                        this.hurt(this.damageSources().flyIntoWall(), o);
                    }
                }

                if (this.onGround() && !this.level().isClientSide) {
                    this.setSharedFlag(7, false);
                }
            } else {
                BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
                float p = this.level().getBlockState(blockPos).getBlock().getFriction();
                f = this.onGround() ? p * 0.91F : 0.91F;
                Vec3 vec37 = this.handleRelativeFrictionAndCalculateMovement(vec3, p);
                double q = vec37.y;
                if (this.hasEffect(MobEffects.LEVITATION)) {
                    q += (0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec37.y) * 0.2;
                } else if (this.level().isClientSide && !this.level().hasChunkAt(blockPos)) {
                    if (this.getY() > (double)this.level().getMinBuildHeight()) {
                        q = -0.1;
                    } else {
                        q = 0.0;
                    }
                } else if (!this.isNoGravity()) {
                    q -= d;
                }

                if (this.shouldDiscardFriction()) {
                    this.setDeltaMovement(vec37.x, q, vec37.z);
                } else {
                    this.setDeltaMovement(vec37.x * (double)f, q * 0.9800000190734863, vec37.z * (double)f);
                }
            }
        }

        this.calculateEntityAnimation(this instanceof FlyingAnimal);
    }

    private Vec3 seatPos1 = Vec3.ZERO;
    private Vec3 seatPos2 = Vec3.ZERO;

    public void setClientSeatPos1(Vec3 seatPos) {
        this.seatPos1 = seatPos;
    }

    public void setClientSeatPos2(Vec3 seatPos) {
        this.seatPos2 = seatPos;
    }

    @Override
    public Vec3 getClientSeatPos(Entity passenger) {
        var ps = this.getPassengers();

        int num = 1;
        for (Entity p: ps) {
            if (p == passenger) {
                if (num == 1) {
                    return this.seatPos1;
                } else if (num == 2) {
                    return this.seatPos2;
                }
            }
            num++;
        }
        return Vec3.ZERO;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isServerSide()) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        }

        this.rideCoolTick = 3;
        this.actionController.reset();

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2;
    }
}
