package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.Utils;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileVerticalEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.controller.ActionController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
import software.bernie.geckolib.network.SerializableDataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Pmv01Entity extends LivingEntity implements GeoEntity, GeoAnimatable {
    public static final float DEFAULT_SCALE = 0.5f;
    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // ロボ君のアクションの状態（サーバと他クライアントにも同期する）
    public ActionController actionController = new ActionController();

    // 搭乗してから操作開始するまでの間のティック
    private short rideCoolTick = 0;

    // 操作しているドライバーのキー入力（サーバと他クライアントにも同期する）
    private DriverInput driverInput = null;

    public void setDriverInput(DriverInput di) {
        this.driverInput = di;

        if (!level().isClientSide) {
            setAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET, driverInput.getStatus());
            if (di.isModeChangePressed()) {
                this.setMainMode(!this.isMainMode());
            }
        }
    }

    public DriverInput getDriverInput() {
        return this.driverInput;
    }

    public static final SerializableDataTicket<Short> DRIVER_INPUT_SERIALIZABLE_DATA_TICKET =
            GeckoLibUtil.addDataTicket(
                    new SerializableDataTicket<Short>(PomkotsMechs.MODID.toString(), Short.class) {
                        public void encode(Short data, FriendlyByteBuf buffer) {
                            try {
                                buffer.writeShort((int)data);
                            } catch (Exception e) {
                                LOGGER.error(e.toString());
                            }
                        }

                        public Short decode(FriendlyByteBuf buffer) {
                            Object o = null;
                            try {
                                o = buffer.readShort();
                            } catch (Exception e) {
                                LOGGER.error(e.toString());
                            }
                            return (Short) o;
                        }
                    }
            );

    // ユーザが縦に移動しようとしてるか横に移動しようとしてるか（速度じゃないので注意）
    private float forwardIntention = 0;
    private float sidewayIntention = 0;

    // よくわからんけどサーバとクライアントで位置情報に3tick分ぐらい差があるっぽい。その辺を無理やり対処するためにposの履歴を取っとく
    private LinkedList<Vec3> posHistory = new LinkedList<Vec3>();

    private Entity lockTargetS = null;
    private Entity lockTargetH = null;
    private List<Entity> lockTargetM = new ArrayList<>();

    public void lockTargetSoft(Entity ent) {
        this.lockTargetS = ent;
    }

    public void unlockTargetSoft() {
        this.lockTargetS = null;
    }
    public void lockTargetHard(Entity ent) {
        this.lockTargetH = ent;
    }

    public void unlockTargetHard() {
        this.lockTargetH = null;
    }

    public void lockTargetMulti(Entity ent) {
        if (lockTargetM.size() < 6) {
            lockTargetM.add(ent);
        }
    }

    private boolean fireMissile = false;
    public void unlockTargetMulti() {
        fireMissile = true;
    }

    private void clearLockTargets() {
        unlockTargetSoft();
        unlockTargetHard();

        if (!lockTargetM.isEmpty()) {
            lockTargetM.clear();
        }
    }

    private static final int MAX_ENERGY = 100;
    private int energy = MAX_ENERGY;

    public int getEnergy() {
        return this.energy;
    }

    private void chargeEnergy() {
        if (this.energy + 2 > MAX_ENERGY) {
            this.energy = MAX_ENERGY;
        } else {
            this.energy += 2;
        }
    }

    private boolean useEnergy(int dec) {
        if (this.energy - dec < 0) {
            this.energy = 0;
            return false;
        } else {
            this.energy -= dec;
            return true;
        }
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, BattleBalance.MECH_HEALTH);
    }

    private boolean isServerSide() {
        return !isClientSide();
    }

    private boolean isClientSide() {
        return this.level().isClientSide();
    }

    public Pmv01Entity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(2.0F);
        this.setYRot(0F);
        this.setNoGravity(false);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        this.updatePosHistory(this.position());

        super.tick();

        this.actionController.tick();

        if (this.isAlive() && this.isVehicle()) {
            if (rideCoolTick > 0) {
                rideCoolTick--;

            } else {
                chargeEnergy();

                if (isClientSide()) {
                    Short b = getAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET);

                    if (b == null) {
                        b = 0;
                    }

                    driverInput = new DriverInput(b);
                }

                if (driverInput != null) {
                    this.updateStatus(driverInput);
                }

                this.fireActions();
            }

        } else {
            this.setNoGravity(false);

        }
    }

    private void updatePosHistory(Vec3 v) {
        var vec = new Vec3(v.x, v.y, v.z);

        if (posHistory.size() > 3) {
            posHistory.remove(0);
        }

        posHistory.add(vec);
    }

    private void updateStatus (DriverInput driverInput) {
        getUserIntentionForDirectionFromKey(driverInput);

        // USING EQUIPMENT
        LivingEntity l = this.getDrivingPassenger();

        // WEAPONS
        if (this.isMainMode()) {
            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.gatring.tryAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                this.actionController.pile.tryAction();
                this.boostPileBunker();
            } else if (driverInput.isWeaponLeftShoulderPressed()) {
                this.actionController.grenade.tryAction();
            } else if (fireMissile) {
                this.fireMissile = false;
                this.actionController.missile.tryAction();
            }
        } else {
            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.hummer.tryAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                this.actionController.scop.tryAction();
            }
        }

        // BOOST
        if (this.getDeltaMovement().z == 0 && this.getDeltaMovement().x == 0) {
            this.actionController.setBoost(false);
        }

        // EVASION
        if (driverInput.isEvasionPressed() && this.actionController.evasion.tryAction()) {
            this.doEvasion();
            this.actionController.setBoost(true);
        }

        // JUMP
        if (driverInput.isJumpPressed() && this.onGround()) {
            this.actionController.jump.tryAction();
        }
        if (this.actionController.jump.isOnFire()) {
            if (isServerSide()) {
                this.push(0, 2, 0);
            } else {
                this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
            }
        }

        // IN AIR
        if (!this.onGround()) {
            if (driverInput.isJumpPressed()) {
                this.setNoGravity(true);

//                if (useEnergy(0)) {
                if (useEnergy(5)) {
                    if (isServerSide() && this.getDeltaMovement().y() < 0.7) {
                        this.push(0, 0.3, 0);
                    }
                } else {
                    if (isServerSide()) {
                        this.push(0, -0.18 * 0.9800000190734863D, 0);
                    }
                }
            } else if (isServerSide()){
                this.push(0, -0.18 * 0.9800000190734863D, 0);
            }
        } else {
            if (this.isClientSide() && justLanded(this)) {
                this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
            }
            this.setNoGravity(false);
        }
    }

    private boolean justLanded(Pmv01Entity ent) {
        return ent.getDeltaMovement().y == 0 && (ent.yOld - ent.getY()) > 0.4;
    }

    private void getUserIntentionForDirectionFromKey(DriverInput driverInput) {
        if (driverInput.isForwardPressed()) {
            forwardIntention = 1;
        } else if (driverInput.isBackPressed()) {
            forwardIntention = -1;
        } else {
            forwardIntention = 0;
        }

        if (driverInput.isRightPressed()) {
            sidewayIntention = -1;
        } else if (driverInput.isLeftPressed()) {
            sidewayIntention = 1;
        } else {
            sidewayIntention = 0;
        }
    }

    private void doEvasion() {
        if (isServerSide()) {
            Vec3 vel;
            if (forwardIntention == 0 && sidewayIntention == 0) {
                vel = new Vec3(0, 0, 1);
            } else {
                vel = new Vec3(sidewayIntention, 0, forwardIntention).normalize();
            }

            vel = vel.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            var distance = 0.475 * 15;
            vel = vel.scale(distance);

            this.push(vel.x, vel.y, vel.z);
        } else {
            playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get());
        }
    }

    private void boostPileBunker() {
        if (isServerSide()) {
            Vec3 vel;

            if (this.lockTargetH != null) {
                vel = this.position().vectorTo(lockTargetH.position()).normalize();
            } else if (this.lockTargetS != null) {
                vel = this.position().vectorTo(lockTargetS.position()).normalize();
            } else {
                vel = new Vec3(0, 0, 1);
                vel = vel.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            }

            var distance = 0.475 * 3;
            if (!this.onGround()) {
                distance = 0.475;
            }

            vel = vel.scale(distance);
            this.push(vel.x, vel.y, vel.z);
        }
    }

    private void fireActions() {
        Level world = level();

        if (actionController.hummer.isOnFire()) {
            this.fireHummer(world);
        } else if (actionController.scop.isOnFire()) {
            this.fireScop(world);
        } else if (actionController.gatring.isOnFire()) {
            this.fireGatring(world);
        } else if (actionController.pile.isOnFire()) {
            this.firePilebunker(world);
        } else if (actionController.grenade.isOnFire()) {
            this.fireGrenade(world);
        } else if (actionController.missile.isInFire()) {
            if (actionController.missile.currentFireTime % 3 == 0) {
                this.fireMissile(world, actionController.missile.currentFireTime / 3 - 1);
            }
        }
    }

    private void fireHummer(Level world) {
        if (!world.isClientSide()) {
            BlockPos curBP = this.blockPosition();
            Vec3 vec = new Vec3(0, 0, 5);
            vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            BlockPos tgtOffset = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

            for (int i = 0; i < 4; i++) {
                for (var v : Utils.circlePosRad9) {
                    world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() + v.getY() + i, tgtOffset.getZ() + v.getZ()), false);
                }
            }

            for (var v : Utils.circlePosRad7) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() + v.getY() + 4, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad5) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() + v.getY() + 5, tgtOffset.getZ() + v.getZ()), false);
            }
        }
    }

    private void fireScop(Level world) {
        if (!world.isClientSide()) {
            BlockPos curBP = this.blockPosition();
            Vec3 vec = new Vec3(0, 0, 8);
            vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            BlockPos tgtOffset = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

            for (var v : Utils.circlePosRad9) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 0, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad9) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 1, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad7) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 2, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad5) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 3, tgtOffset.getZ() + v.getZ()), false);
            }
        }
    }

    private void fireGatring(Level world) {
        if (!world.isClientSide()) {
            BulletEntity be = new BulletEntity(PomkotsMechs.BULLET.get(), world, this);

            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-1.65, 2.0F, 3.5F);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            be.setPos(offset.add(muzzlPos));

            float[] angle = getShootingAngle(be, true);

            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 0.9F, 2F);

            world.addFreshEntity(be);
        } else {
            if (tickCount % 7 == 0) {
                playSoundEffect(PomkotsMechs.SE_GATLING_EVENT.get());
            }
        }
    }

    private boolean isSelf(Entity ent) {
        return ent.equals(this) || ent.equals(this.getDrivingPassenger());
    }

    private void firePilebunker(Level world) {
        Entity driver = this.getDrivingPassenger();
        var pilePos1 = new Vec3(6.5, 3.0F, 18F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
        var pilePos2 = new Vec3(-2, -3F, -2F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

        var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
            if (isSelf(ent)) {
                continue;
            }

            if (ent instanceof LivingEntity le) {
                if (!world.isClientSide()) {
                    le.knockback(2, kbVel.x, kbVel.z);

                    DamageSource ds;
                    if (driver instanceof Player p) {
                        ds = this.damageSources().playerAttack(p);
                    } else {
                        ds = this.damageSources().generic();
                    }
                    le.hurt(ds, BattleBalance.MECH_PILE_DAMAGE);
                } else {
                    addHitParticles(le);
                }
            }
        }

        if (this.level().isClientSide) {
            playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
        }
    }

    private void addHitParticles(Entity target) {
        var offset = new Vec3(target.position().x, target.getBoundingBox().getCenter().y, target.position().z);

        for (int i = 0; i < 40; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 4.3 - 1;
            double velocityY = random.nextDouble() * 4.3 - 1;
            double velocityZ = random.nextDouble() * 4.3 - 1;

            // パーティクルをクライアント側で発生させる
            this.level().addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(),
                    true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
    }

    private void fireMissile(Level world, int slot) {
        if (!world.isClientSide()) {
            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-1.0, 3.5F, 0.8F);
            var worldMuzzlPos = muzzlPos.add(0, 0, -slot * 0.3).yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            LivingEntity target = null;

            if (!lockTargetM.isEmpty()) {
                var lockNum = lockTargetM.size();
                int idx = slot;
                if (lockNum <= slot) {
                    idx = slot % lockNum;
                }
                target = (LivingEntity)lockTargetM.get(idx);
            }

            MissileVerticalEntity be = new MissileVerticalEntity(PomkotsMechs.MISSILE_VERTICAL.get(), world, this, target);

            be.setPos(offset.add(worldMuzzlPos));

            be.shootFromRotation(be, -89, this.getYRot(), this.getFallFlyingTicks(), 1F, 0F);

            world.addFreshEntity(be);

            if (slot == 5) {
                lockTargetM.clear();
            }
        } else {
            if (slot == 0) {
                playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
            }

            lockTargetM.clear();
        }
    }

    private void fireGrenade(Level world) {
        if (!world.isClientSide()) {
            GrenadeEntity be = new GrenadeEntity(PomkotsMechs.GRENADE.get(), world, this, BattleBalance.MECH_GRENADE_EXPLOSION);

            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(1.65, 3.0F, 3.5F);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            float[] angle = getShootingAngle(be, false);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 0.9F, 0F);

            world.addFreshEntity(be);

            var knockBackvel = new Vec3(0, 0, -3F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            this.addDeltaMovement(knockBackvel);
        } else {
            playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        }
    }

    private float[] getShootingAngle(Entity bullet, boolean useLockTarget) {

        double xRot = 0;
        double yRot = 0;

        var lockTarget = (lockTargetH != null)?lockTargetH:lockTargetS;
        if (useLockTarget && lockTarget != null) {
            Vec3 positionA = bullet.position();
            // エンティティBの位置を取得
            Vec3 positionB = lockTarget.getBoundingBox().getCenter();

            // エンティティAからエンティティBへの相対ベクトル
            double deltaX = positionB.x - positionA.x;
            double deltaY = positionB.y - positionA.y;
            double deltaZ = positionB.z - positionA.z;

            // 水平角度 (Yaw) の計算 (XZ平面上の角度)
            yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0; // 90度引いて北基準に

            // 垂直角度 (Pitch) の計算 (Y軸方向の角度)
            double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ); // 水平方向の距離
            xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ)); // Y

        } else {
            var driver = this.getDrivingPassenger();
            var lookAngle = driver.getLookAngle();

            xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 7.5;
            yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
        }

        return new float[]{(float)xRot, (float)yRot};
    }

    @Override
    public void travel(Vec3 pos) {
        if (this.isAlive() && this.isVehicle()) {
            var pilot = this.getDrivingPassenger();

            // ROTATE Vehicle
            this.setYRot(pilot.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(pilot.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.setYBodyRot(this.getYRot());
            this.setYHeadRot(this.getYRot());

            float f = pilot.xxa * 0.5F;
            float f1 = pilot.zza * 0.5F;

            // BOOST
            if (isServerSide()) {
                if (this.actionController.isBoost()) {
                    this.setSpeed(1.5F);
                } else {
                    this.setSpeed(0.5F);
                }
            }

            super.travel(new Vec3(f, pos.y, f1));
        } else {
            super.travel(pos);
        }
    }

    @Override protected float getFlyingSpeed() {
        if (isNoGravity() && isServerSide()) {
            if (this.actionController.isBoost()) {
                return 0.4f;
            } else {
                return 0.2f;
            }
        } else {
            return super.getFlyingSpeed();
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (passenger instanceof Player player) {
            if (player.isDeadOrDying()) {
                player.getAbilities().mayfly = true;
            }
        }

        this.clearLockTargets();

        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());        // -1.0 for less head bonk.
    }

    @Override
    public boolean hurt(DamageSource ds, float a) {
        if (ds.getEntity() != null && ds.getEntity().equals(this.getDrivingPassenger())) {
            return false;
        } else {
            if (this.isClientSide()) {
                this.playSoundEffect(PomkotsMechs.SE_HIT_EVENT.get());
                ParticleUtil.addParticles(ds,this);
            }
            return super.hurt(ds, a);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.getDrivingPassenger() == null) {
            if (isServerSide()) {
                player.setYRot(this.getYRot());
                player.setXRot(this.getXRot());
                player.startRiding(this);
            }

            this.rideCoolTick = 3;
            this.actionController.reset();
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            if (this.getDrivingPassenger() == null) {
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.idle"));
            }

            if (this.actionController.pile.isInAction()) {
                if (this.actionController.pile.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.pilebunker"));

            } else if (this.actionController.grenade.isInAction()) {
                if (this.actionController.grenade.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.grenade"));

            } else if (this.actionController.hummer.isInAction()) {
                if (this.actionController.hummer.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.hummer"));

            } else if (this.actionController.scop.isInAction()) {
                if (this.actionController.scop.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.scop"));

            } else if (this.actionController.gigadrill.isInAction()) {
                if (this.actionController.gigadrill.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.gigadrillbreak"));

            } else if (this.actionController.jump.isInAction()) {
                if (this.actionController.jump.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.jump"));

            }

            if (event.isMoving()) {
                if (this.actionController.evasion.isInAction()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.evasion"));
                } else if (this.actionController.isBoost()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.dash"));
                } else {
                    if (this.isNoGravity()) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.idle"));
                    } else {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmv01.walk"));
                    }
                }
            } else {
                if (this.yRotO != this.getYRot()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmv01.walk"));
                } else {
                    event.getController().forceAnimationReset();
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.idle"));
                }
            }
        }));

        controllers.add(new AnimationController<>(this, "fly", 0, event -> {
            if (event.getAnimatable().onGround()) {
                if (justLanded(event.getAnimatable())) {
                    event.getController().forceAnimationReset();
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.onground"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.nop"));
                }
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.flylegs"));
            }
        }));

        controllers.add(new AnimationController<>(this, "rotation", 3, event -> {
            if (event.isMoving()) {
                if (this.isNoGravity() && !this.actionController.isBoost() && !this.actionController.evasion.isInAction()) {
                    if (this.forwardIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.frontfly"));
                    } else if (forwardIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.backfly"));
                    } else if (this.sidewayIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.rightfly"));
                    } else if (sidewayIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.leftfly"));
                    } else {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.nop"));
                    }
                } else {
                    if (this.sidewayIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.right"));
                    } else if (sidewayIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.left"));
                    } else {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.nop"));
                    }
                }
            } else {
                if (!this.actionController.isInActionAll()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "gatling", 0, event -> {
            if (this.actionController.gatring.isInAction()) {
                if (this.actionController.gatring.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.gatring.isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.gatringgun1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmv01.gatringgun2"));
                }
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "weapons", 0, event -> {
            if (this.actionController.grenade.isInAction()) {
                if (this.actionController.grenade.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.weapon_grenade"));
            } else if (this.actionController.missile.isInAction()) {
                if (this.actionController.grenade.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.weapon_missile"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmv01.nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "boosters", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pmv01.booster"));
        }));
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    public LivingEntity getDrivingPassenger() {
        return this.getPassengers().isEmpty() ? null : (LivingEntity) this.getPassengers().get(0);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2.5F;
    }


    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private Vec3 clientSeatPos = Vec3.ZERO;

    public void setClientSeatPos(Vec3 seatPos) {
        this.clientSeatPos = seatPos;
    }

    public Vec3 getClientSeatPos() {
        return this.clientSeatPos;
    }

    private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(Pmv01Entity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, true); // 初期値を設定
    }

    private void setMainMode(boolean value) {
        this.entityData.set(MODE, value);
    }

    public boolean isMainMode() {
        return this.entityData.get(MODE);
    }

    public boolean isSubMode() {
        return !this.entityData.get(MODE);
    }

    private void playSoundEffect(SoundEvent event) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
//        if (this.isClientSide() && this.tickCount % 10 == 0) {
//            this.playSoundEffect(PomkotsMechs.SE_WALK_EVENT);
//        }
    }
}
