package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class ActionWeapon extends Action {
    private BasePartsItem.WeaponInterface weapon;
    private Motion motion;
    private int maxActionTick;
    private int fireStartTick;
    private int currentActionTick = -1;
    private Pmvc01Entity owner;
    private int weaponItemSlot;
    private ItemStack itemStack;

    public ActionWeapon(Pmvc01Entity owner, int weaponItemSlot) {
        super(1,1,1);
        this.owner = owner;
        this.weaponItemSlot = weaponItemSlot;
    }

    public void setWeapon(BasePartsItem.WeaponInterface w, ItemStack itemStack) {
        this.weapon = w;
        this.motion = w.getMotion();

        this.maxActionTick = motion.getMaxActionTick();
        this.fireStartTick = getSmallest(motion.getActualFireTick());
        this.maxCoolTime = w.getCoolTime();
        this.itemStack = itemStack;

        this.reset();
    }

    private int getSmallest(Set<Integer> set) {
        int val = Integer.MAX_VALUE;
        for (int i: set) {
            if (i < val) {
                val = i;
            }
        }
        return val;
    }

    public Motion getMotion() {
        return this.motion;
    }

    public void tick() {
        if (currentCoolTime > 0) {
            currentCoolTime--;
        }

        if (isInAction()) {
            weapon.tickWeaponInAction(new WeaponMechInterface(this.owner, this), currentActionTick, this.isOnFire());

            if (canContinue()) {
                currentActionTick++;
            } else {
                // end
                reset();
            }
        }
    }

    public int getFireStartTick() {
        return fireStartTick;
    }

    public int getCurrentActionTick() {
        return currentActionTick;
    }

    public int getWeaponItemSlot() {
        return this.weaponItemSlot;
    }

    protected boolean concurrentAvailable() {
        return motion.concurrentAvailable();
    }

    protected boolean canContinue() {
        if (motion.getType().equals(Motion.MotionType.CONTINUOUS)) {
            return true;
        } else if (motion.getType().equals(Motion.MotionType.CHARGE)) {
            return currentActionTick <= fireStartTick + maxActionTick;
        } else {
            return currentActionTick <= maxActionTick;
        }
    }

    public boolean canStartAction() {
        return !isInCooltime() && !isInAction();
    }

    public boolean startAction() {
        currentActionTick = 0;
        currentCoolTime = maxCoolTime;

        weapon.startUsing(new WeaponMechInterface(this.owner, this));

        return true;
    }

    public void fireAction() {
        if (motion.getType().equals(Motion.MotionType.CHARGE)) {
            fireStartTick = currentActionTick;
        }
    }

    public boolean isOnStart() {
        return currentActionTick == 0;
    }

    public boolean isInAction() {
        return currentActionTick >= 0;
    }

    public boolean isInCooltime() {
        return currentCoolTime > 0;
    }

    public boolean isCharging() {
        return currentActionTick < fireStartTick;
    }

    public boolean isInFire() {
        return currentActionTick >= fireStartTick;
    }

    public boolean isOnFire() {
        if (motion.getType().equals(Motion.MotionType.CONTINUOUS) && isInFire()
                && (currentActionTick - fireStartTick) % motion.getInterval() == 0) {
            return true;
        } else if (motion.getType().equals(Motion.MotionType.CHARGE)) {
            return currentActionTick == fireStartTick + 5;

        } else {
            if (motion.getActualFireTick().contains(currentActionTick)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void reset() {
        currentChargeTime = 0;
        currentFireTime = 0;

        currentActionTick = -1;
        if (motion.getType().equals(Motion.MotionType.CHARGE)) {
            fireStartTick = getSmallest(motion.getActualFireTick());
        }
        weapon.endUsing(new WeaponMechInterface(this.owner, this));
    }

    public static class WeaponMechInterface {
        private Pmvc01Entity owner;
        private ActionWeapon action;

        private WeaponMechInterface(Pmvc01Entity owner, ActionWeapon action) {
            this.owner = owner;
            this.action = action;
        }

        public Level getWorld() {
            return owner.level();
        }

        public Vec3 getOffset() {
            return owner.getShootingOffset();
        }

        public float getYRot() {
            return owner.getYRot();

        }

        public Player getPlayer() {
            var v = owner.getDrivingPassenger();

            if (v instanceof Player p) {
                return p;
            } else {
                return null;
            }
        }

        public ItemStack getItemStack() {
            return action.itemStack;
        }

        public int isRight() {
            if (action.weaponItemSlot == Pmvc01Entity.INV_WEAPON_RIGHT_HAND || action.weaponItemSlot == Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER) {
                return -1;
            } else {
                return 1;
            }
        }

        public boolean consumeEnergy(int num) {
            return owner.consumeEnergy(num);
        }

        public boolean consumeAmmo(int num) {
            return owner.consumeBullet(num, action.itemStack, getWeaponItemSlot());
        }

        public boolean consumeAmmoFromServerSide(int num) {
            return owner.consumeBulletFromServerSide(num, action.itemStack, getWeaponItemSlot());
        }

        public int getWeaponItemSlot() {
            return action.weaponItemSlot;
        }

        public LivingEntity getMechEntity() {
            return owner;
        }

        public float[] getShootingAngle(Entity ent, boolean useTarget) {
            return owner.getShootingAngle(ent, useTarget, true);
        }

        public float getFallFlyingTicks() {
            return owner.getFallFlyingTicks();
        }

        public List<Entity> getMultiLockTargets() {
            return owner.getLockTargets().getLockTargetMulti(action.getWeaponItemSlot());
        }

        public List<Entity> consumeMultiLockTargets() {
            return owner.getLockTargets().consumeTargetMulti(action.getWeaponItemSlot());
        }

        public Entity consumeMultiLockTargetsSingle() {
            return owner.getLockTargets().consumeTargetMultiSingle(action.getWeaponItemSlot());
        }

        public void playSoundEffect(SoundEvent event) {
            owner.playSoundPublic(event);
        }

        public void addHitParticles(Entity e) {
            owner.addHitParticles(e);
        }

        public Vec3 position() {
            return owner.position();
        }

        public DamageSources damageSources() {
            return owner.damageSources();
        }

        public LivingEntity getDrivingPassenger() {
            return owner.getDrivingPassenger();
        }

        public boolean isSelf(Entity ent) {
            return owner.isSelfPublic(ent);
        }

        public boolean onGround() {
            return owner.onGround();
        }

        public void knockBack(float strength) {
            var knockBackvel = new Vec3(0, 0, -strength).yRot((float) Math.toRadians((-1.0) * owner.getYRot()));
            owner.addDeltaMovement(knockBackvel);
        }

        public void addVelocity(float pitch, float yaw, float strength) {
            float x = -Mth.sin(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);
            float y = -Mth.sin((pitch) * 0.017453292F);
            float z = Mth.cos(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);

            if (!this.owner.onGround()) {
                strength *= 0.5F;
            }
            Vec3 acceleration = new Vec3(x, y, z).scale(strength);

            this.owner.setDeltaMovement(acceleration);
        }

        public Vec3 getOffsetByLookingDirection(int distance) {
            var entity = owner.getDrivingPassenger();

            if (entity != null) {
                Vec3 lookDirection = entity.getLookAngle();
                Vec3 playerPosition = new Vec3(0, 6, 0);

                return playerPosition.add(
                        0,
                        lookDirection.y * distance,
                        (1 - Math.abs(lookDirection.y)) * distance
                );
            } else {
                return null;
            }
        }

        public void breakBlocksCube(int radius) {
            BlockPos curBP = owner.blockPosition();
            Vec3 vec = new Vec3(0, 0, 5);
            vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            BlockPos pos = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

            // 円柱の高さを設定
            int height = 8; // 高さ2ブロックの円柱
            for (int y = 0; y < height; y++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos blockPos = pos.offset(x, y, z);
                        BlockState state = owner.level().getBlockState(blockPos);
                        if (!state.isAir()) {
                            if (x * x + z * z <= radius * radius) {
                                Utils.destroyBlock(owner.level(), blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                            }
                        }
                    }
                }
            }
        }

        public void breakBlockSphere(int radius, Vec3 offset) {
            // 半径の2乗を計算（球体判定に利用）
            int radiusSquared = radius * radius;

            BlockPos curBP = owner.blockPosition();
            Vec3 vec = offset;
            vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            BlockPos pos = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

            // 範囲を指定してループ処理
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z <= radiusSquared) {
                            BlockPos targetPos = pos.offset(x, y, z);
                            BlockState state = owner.level().getBlockState(targetPos);
                            // ブロックが空でない場合に破壊
                            if (!state.isAir()) {
                                Utils.destroyBlock(owner.level(), targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class DefaultDummyWeapon implements BasePartsItem.WeaponInterface {
        public void tickWeaponInAction(WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
            var world = mechInterface.getWorld();

            if (isOnFire) {
                Entity driver = mechInterface.getDrivingPassenger();
                var pilePos1 = new Vec3(4.5 * mechInterface.isRight(), 4.0F, 10F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());
                var pilePos2 = new Vec3(-2 * mechInterface.isRight(), -4F, -4F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());

                var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
                    if (mechInterface.isSelf(ent)) {
                        continue;
                    }

                    if (ent instanceof LivingEntity le) {
                        if (!world.isClientSide()) {
                            le.knockback(2, kbVel.x, kbVel.z);

                            DamageSource ds;
                            if (driver instanceof Player p) {
                                ds = mechInterface.damageSources().playerAttack(p);
                            } else {
                                ds = mechInterface.damageSources().generic();
                            }
                            le.hurt(ds, 20F);
                        } else {
                            mechInterface.addHitParticles(le);
                        }
                    }
                }

                if (world.isClientSide) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
                }
            }
        }

        public String getWeaponAttachPoint() {
            return "";
        }

        public Motion getMotion() {
            return Motion.PUNCH;
        }

        public int maxMultiLockNum() {
            return 0;
        }

        public boolean isSoftLockEnabled() {
            return false;
        }

        public int getCoolTime() {
            return 100;
        }
    }
}
