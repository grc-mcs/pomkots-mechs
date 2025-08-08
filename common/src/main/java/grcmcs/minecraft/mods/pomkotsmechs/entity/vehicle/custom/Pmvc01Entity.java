package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.CachedBoneFinder;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CircuitHardLockItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CircuitSoftLockItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.RailSliderItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

import java.util.*;
import java.util.function.Supplier;

public class Pmvc01Entity extends PomkotsVehicleBase implements HasCustomInventoryScreen, Container, MenuProvider {
    public static final float DEFAULT_SCALE = 1f;
    public static final int CONTAINER_SIZE = 27;

    @Override
    protected String getMechName() {
        return "pmv01";
    }

    private NonNullList<ItemStack> itemStacks;

    private int fuel = 0;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, BattleBalance.MECH_HEALTH)
                .add(Attributes.ARMOR, 20);
    }

    public Pmvc01Entity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
        this.itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    }

    private final CachedBoneFinder boneFinder = new CachedBoneFinder();

    public CachedBoneFinder getBoneFinder() {
        return boneFinder;
    }

    public void setupBoneFinder(BakedGeoModel model) {
        boneFinder.setModel(model);
    }

    /******************************************************************************************
     * エンティティ基本関係の処理
     ******************************************************************************************/

    @Override
    public void tick() {
        if (areWeaponsChanged()) {
            registerWeapons();
        }

        if (this.isServerSide()) {
            if (Double.isNaN(this.getSpeedModifier())) {
                this.syncAllParameter2Client();
                this.initializeAmmoManager(false);
            }

            if (this.isAlive() && this.isVehicle() && tickCount % 200 == 0) {
                syncFuels();
            }

            if (isHovering()) {
                this.setNoGravity(true);
            }
        }

        this.tickAmmos();
        super.tick();

        if (this.isServerSide()) {
            if (this.isBoundToRail()) {
                Vec3 currentVelocity = this.getDeltaMovement();
                railBindComponent.moveRobotOnRail(this, currentVelocity);
            }
        }
    }

    // レール系機能ここから
    private final RailBindComponent railBindComponent = new RailBindComponent();

    public boolean isBoundToRail() {
        return this.entityData.get(IS_BOUND_TO_RAIL);
    }

    public boolean tryBindToRail() {
        var bp = railBindComponent.findRailBlockBelow(this.level(), this.blockPosition());
        this.entityData.set(IS_BOUND_TO_RAIL, bp != null);

        return bp != null;
    }

    public void unbindFromRail() {
        this.entityData.set(IS_BOUND_TO_RAIL, false);
    }
    // レール系機能おわり

    @Override
    public void setDeltaMovement(Vec3 movement) {
        if (Double.isNaN(movement.x) || Double.isNaN(movement.y) || Double.isNaN(movement.z)) {
            movement = new Vec3(0,0,0);
        }
        super.setDeltaMovement(movement);
    }

    @Override
    public boolean canWork() {
        var res = !this.getHeadParts().isEmpty()
                && !this.getBodyParts().isEmpty()
                && !this.getArmParts().isEmpty()
                && !this.getLegsParts().isEmpty()
                && !this.getBooster().isEmpty()
                && !this.getGenerator().isEmpty()
                && this.consumeFuel();

        if (!res) {
            resetExtensionUnitStatus();
        }

        return res;
    }

    public void resetExtensionUnitStatus() {
        this.isHovering = false;
        this.unbindFromRail();
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    /******************************************************************************************
     * アクション/武器関係の処理
     ******************************************************************************************/

    private boolean areWeaponsChanged() {
        boolean b = false;

        b = checkWeaponChanged(INV_WEAPON_RIGHT_HAND, this.getRightArmWeapon().getItem());
        b = b || checkWeaponChanged(INV_WEAPON_LEFT_HAND, this.getLeftArmWeapon().getItem());
        b = b || checkWeaponChanged(INV_WEAPON_RIGHT_SHOULDER, this.getRightShoulderWeapon().getItem());
        b = b || checkWeaponChanged(INV_WEAPON_LEFT_SHOULDER, this.getLeftShoulderWeapon().getItem());

        return b;
    }

    private final Map<Integer, Item> weaponMap = new HashMap<>();
    private boolean checkWeaponChanged(int num, Item latest) {
        var old = weaponMap.get(num);
        if (old == latest) {
            return false;
        } else {
            weaponMap.put(num, latest);
            return true;
        }
    }

    protected static final int ACT_RIGHT_HAND = 2;
    protected static final int ACT_LEFT_HAND = 3;
    protected static final int ACT_RIGHT_SHOULDER = 4;
    protected static final int ACT_LEFT_SHOULDER = 5;
    protected static final int ACT_BIND_RAIL = 6;

    @Override
    protected void registerActions() {
        super.registerActions();
        this.actionController.registerAction(ACT_RIGHT_HAND, new ActionWeapon(this, INV_WEAPON_RIGHT_HAND), ActionController.ActionType.R_ARM_MAIN);
        this.actionController.registerAction(ACT_LEFT_HAND, new ActionWeapon(this, INV_WEAPON_LEFT_HAND), ActionController.ActionType.L_ARM_MAIN);
        this.actionController.registerAction(ACT_RIGHT_SHOULDER, new ActionWeapon(this, INV_WEAPON_RIGHT_SHOULDER), ActionController.ActionType.R_SHL_MAIN);
        this.actionController.registerAction(ACT_LEFT_SHOULDER, new ActionWeapon(this, INV_WEAPON_LEFT_SHOULDER), ActionController.ActionType.L_SHL_MAIN);

        this.actionController.registerAction(ACT_BIND_RAIL, new Action(10, 0, 10), ActionController.ActionType.BASE);

        this.registerWeapons();
    }

    protected void registerWeapons() {
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND), this.getRightArmWeapon());
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND), this.getLeftArmWeapon());
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER), this.getRightShoulderWeapon());
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER), this.getLeftShoulderWeapon());
    }

    protected void registerWeapon(ActionWeapon act, ItemStack itemStack) {
        BasePartsItem.WeaponInterface wpn = null;

        if (itemStack.getItem() instanceof BasePartsItem.Weapon bw){
            wpn = bw;
        } else {
            wpn = new ActionWeapon.DefaultDummyWeapon();
        }

        act.setWeapon(wpn, itemStack);
    }

    @Override
    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        applyPlayerInputWeapon((ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND),
                driverInput.isWeaponRightHandPressed(), driverInput.isWeaponRightHandReleased());
        applyPlayerInputWeapon((ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND),
                driverInput.isWeaponLeftHandPressed(), driverInput.isWeaponLeftHandReleased());
        applyPlayerInputWeapon((ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER),
                driverInput.isWeaponRightShoulderPressed(), driverInput.isWeaponRightShoulderReleased());
        applyPlayerInputWeapon((ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER),
                driverInput.isWeaponLeftShoulderPressed(), driverInput.isWeaponLeftShoulderReleased());

        if (driverInput.isExtension1Released()) {
            applyPlayerInputExtension(getExtension1Weapon());
        }
        if (driverInput.isExtension2Released()) {
            applyPlayerInputExtension(getExtension2Weapon());
        }
    }

    protected void applyPlayerInputExtension(ItemStack extensionStack) {
        if (!extensionStack.isEmpty()) {
            var extensionItem = extensionStack.getItem();
            if (extensionItem instanceof HoverUnitItem) {
                this.isHovering = !this.isHovering;
            } else if (extensionItem instanceof RailSliderItem) {
                if (this.isBoundToRail()) {
                    this.unbindFromRail();
                } else if (this.tryBindToRail()) {
                    this.actionController.getAction(ACT_BIND_RAIL).startAction();
                }
            }
        }
    }

    protected void applyPlayerInputWeapon(ActionWeapon act, boolean isPressed, boolean isReleased) {
        if (act.getMotion().getType().equals(Motion.MotionType.MULTI_LOCK)) {
            if (isReleased) {
                act.startAction();
            }
        } else if (act.getMotion().getType().equals(Motion.MotionType.CONTINUOUS)) {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingWeapons()) {
                    act.startAction();
                }
            } else if (act.isInAction() && isReleased) {
                act.reset();
            }
        } else if (act.getMotion().getType().equals(Motion.MotionType.CHARGE)) {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingWeapons()) {
                    act.startAction();
                }
            } else if (act.isInAction() && isReleased && !act.isInFire()) {
                act.fireAction();
            }
        } else {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingWeapons()) {
                    act.startAction();
                }
            }
        }
    }

    private boolean isUsingWeapons() {
        return this.actionController.getAction(ACT_RIGHT_HAND).isInAction()
                || this.actionController.getAction(ACT_LEFT_HAND).isInAction()
                || this.actionController.getAction(ACT_RIGHT_SHOULDER).isInAction()
                || this.actionController.getAction(ACT_LEFT_SHOULDER).isInAction();
    }

    @Override
    protected void fireWeapons() {
    }

    public Vec3 getShootingOffset() {
        return this.posHistory.getFirst();
    }

    public float[] getShootingAngle(Entity bullet, boolean useTarget, boolean useDeviation) {
        var targetPos = getTargetPos(useDeviation);
        var bulletPos = bullet.position();

        Vec3 bulletDir = targetPos.subtract(bulletPos).normalize();

        float yaw = (float) (Math.atan2(-bulletDir.x, bulletDir.z) * (180.0 / Math.PI));
        float pitch = (float) (Math.asin(-bulletDir.y) * (180.0 / Math.PI));

        return new float[]{pitch, yaw};
    }

    protected Vec3 getTargetPos(boolean useDeviation) {
        Vec3 targetPos;
        //仮
        useDeviation = true;

        Entity lockTarget = this.lockTargets.getLockTargetHard();
        if (lockTarget == null) {
            lockTarget = this.lockTargets.getLockTargetSoft();
        }

        if (lockTarget != null) {
            if (useDeviation) {
                targetPos = lockTarget.getBoundingBox().getCenter().add(lockTarget.getDeltaMovement()).add(lockTarget.getDeltaMovement());
            } else {
                targetPos = lockTarget.getBoundingBox().getCenter();
            }
        } else {
            targetPos = getCameraTargetPosition(this.getDrivingPassenger());
            targetPos = new Vec3(targetPos.x, targetPos.y + this.getPassengersRidingOffset()/2, targetPos.z);
        }

        return targetPos;
    }

    protected Vec3 getCameraTargetPosition(Entity cameraEntity) {
        if (cameraEntity == null) {
            cameraEntity = this;
        } else if (cameraEntity instanceof ServerPlayer sp) {
            cameraEntity = sp.getCamera();
        }

        HitResult hitResult = cameraEntity.pick(100, 0, true);

        if (hitResult.getType() == HitResult.Type.MISS) {
            Vec3 cameraPos = cameraEntity.position();
            Vec3 cameraDirection = cameraEntity.getLookAngle().normalize();

            return cameraPos.add(cameraDirection.scale(60));
        } else {
            return hitResult.getLocation();
        }
    }

    public BlockHitResult performBlockRaycast(LivingEntity player, double maxDistance) {
        Vec3 start = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 end = start.add(lookVec.scale(maxDistance));

        return player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }


    public void addHitParticles(Entity target) {
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

    /******************************************************************************************
     * 移動/速度関係の処理
     ******************************************************************************************/

    @Override
    protected float getWalkSpeed(){
        return 0.5F * this.getSpeedModifier();
    }

    @Override
    protected float getRunSpeed() {
        return 1.5F * this.getSpeedModifier();
    }

    @Override
    protected float getFlyingSpeed() {
        if (isNoGravity()) {
            if (this.actionController.isBoost()) {
                return 0.4f * this.getSpeedModifier();
            } else {
                return 0.2f * this.getSpeedModifier();
            }
        } else {
            return 0.02F;
        }
    }

    @Override
    protected float getJumpSpeed() {
        return 1.5F * this.getJumpModifier() ;
    }

    @Override
    protected float getHorizontalBoostAcceleration() {
        if (onGround()) {
            return 0.475F * 12F  * this.getSpeedModifier() * this.getSpeedModifierEvasion();
        } else if (isHovering()) {
            return 0.475F * 10F  * this.getSpeedModifier() * this.getSpeedModifierEvasion();
        } else {
            return 0.475F * 7F  * this.getSpeedModifier() * this.getSpeedModifierEvasion();
        }
    }

    @Override
    protected float getVerticalBoostAcceleration() {
        return 0.3F * this.getJumpModifier() * this.getSpeedModifierVertical();
    }

    @Override
    protected float getVerticalBoostMaxSpeed() {
        return 0.7F;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 3.75F;
    }

    @Override
    protected void applyPlayerInputEvasion(DriverInput driverInput) {
        if (driverInput.isEvasionPressed()
                && !this.actionController.getAction(ACT_EVASION).isInAction()
                && !this.actionController.getAction(ACT_EVASION).isInCooltime()
                && useEnergy(this.getEnergyConsumeEvasion())
                && this.actionController.getAction(ACT_EVASION).startAction()) {
            this.startEvasion();
            this.actionController.setBoost(true);
        }
    }

    @Override
    protected void applyPlayerInputInAirActions(DriverInput driverInput) {
        if (!this.onGround()) {
            if (driverInput.isJumpPressed()) {
                this.setNoGravity(true);

                if (!tryVerticalBoost()) {
                    if (isServerSide()) {
                        this.push(0, -0.18 * 0.9800000190734863D, 0);
                    }
                }
            } else if (isServerSide()){
                if (isHovering()) {
                    this.handleHovering();
                } else if (!isBoundToRail()) {
                    this.push(0, -0.18 * 0.9800000190734863D, 0);
                }
            }
        } else {
            if (isHovering()) {
                this.handleHovering();
            }
            this.setNoGravity(false);
        }
    }

    @Override
    protected boolean tryVerticalBoost() {
        if (useEnergy(this.getEnergyConsumeVertical())) {
            if (isServerSide() && this.getDeltaMovement().y() < getVerticalBoostMaxSpeed()) {
                this.push(0, getVerticalBoostAcceleration(), 0);
                return true;
            }
        }
        return false;
    }

    protected boolean isHovering() {
        return isHovering;
    }

    private boolean isHovering = false;
    private final float hoverHeight = 3.0f; // 最低維持高度

    private void handleHovering() {
        // 地面または液体面までの距離を計算
        float distanceToGround = getDistanceToGround();

        if (distanceToGround <= hoverHeight) {
            // 浮上力を適用
            Vec3 velocity = getDeltaMovement();
            double upwardForce = (hoverHeight - distanceToGround) * 0.05; // 調整可能
            setDeltaMovement(velocity.x, velocity.y + upwardForce, velocity.z);
        } else if (distanceToGround > hoverHeight + 1){
            this.push(0, -0.18 * 0.9800000190734863D, 0);
        }
    }

    private float getDistanceToGround() {
        Level world = level();
        double entityY = getY();

        // エンティティの足元から下方向にレイキャスト
        for (int i = 0; i < 10; i++) {
            BlockPos checkPos = new BlockPos((int)getX(), (int)Math.floor(entityY - i), (int)getZ());
            BlockState blockState = world.getBlockState(checkPos);

            // 固体ブロックまたは液体をチェック
            if (!blockState.isAir() && (blockState.isSolid() ||
                    blockState.getFluidState().is(FluidTags.WATER) ||
                    blockState.getFluidState().is(FluidTags.LAVA))) {

                // エンティティの足元から地面までの正確な距離を計算
                double groundY = checkPos.getY() + 1.0; // ブロックの上面
                return (float)(entityY - groundY);
            }
        }

        return Float.MAX_VALUE; // 地面が見つからない場合
    }

    /**************************************************************************************
     * エネルギー関係の処理
     **************************************************************************************/

    private int energy = 0;

    @Override
    public int getEnergy() {
        return this.energy;
    }

    @Override
    protected void chargeEnergy() {
        if (this.energy + this.getEnergyChargePerTick() > this.getMaxEnergy()) {
            this.energy = this.getMaxEnergy();
        } else {
            this.energy += this.getEnergyChargePerTick();
        }
    }

    @Override
    protected boolean useEnergy(int dec) {
        if (this.energy - dec < 0) {
            return false;
        } else {
            this.energy -= dec;
            return true;
        }
    }

    public boolean consumeEnergy(int dec) {
        return this.useEnergy(dec);
    }

    @Override
    public int getMaxEnergy() {
        return this.entityData.get(Pmvc01Entity.MAX_ENERGY);
    }

    protected boolean consumeFuel() {
        if (this.isServerSide()) {
            if (fuel > 0) {
                fuel--;
                return true;
            } else {
                var fuels = this.getFuelFromInventory();

                if (fuels.getCount() > 0) {
                    var g = this.getGeneratorFromInventory().getItem();

                    if (g instanceof BasePartsItem.Generator gen) {
                        fuel = gen.getWorkSecPerFuel(this.getGeneratorFromInventory()) * 20 * 2;
                        fuels.shrink(1);

                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return true;
        }
    }

    private void syncFuels() {
        int maxFuel = 0;
        int nowFuel = 0;

        if (this.getGeneratorFromInventory().getItem() instanceof BasePartsItem.Generator gen) {
            var tpf = gen.getWorkSecPerFuel(this.getGeneratorFromInventory()) * 20 * 2;
            maxFuel = tpf * this.getFuelFromInventory().getMaxStackSize();
            nowFuel =  tpf * this.getFuelFromInventory().getCount() + this.fuel;
        }

        this.entityData.set(FUEL_MAX, maxFuel);
        this.entityData.set(FUEL_NOW, nowFuel);
    }

    /**************************************************************************************
     * ターゲットロック関係の処理
     **************************************************************************************/

    @Override
    public boolean shouldLockMulti(DriverInput driverInput) {
        boolean res = false;

        if (driverInput.isWeaponRightHandPressed()) {
            res |= isMultiLockEnabledWeapon(this.getRightArmWeapon());
        }
        if (driverInput.isWeaponLeftHandPressed()) {
            res |= isMultiLockEnabledWeapon(this.getLeftArmWeapon());
        }
        if (driverInput.isWeaponRightShoulderPressed()) {
            res |= isMultiLockEnabledWeapon(this.getRightShoulderWeapon());
        }
        if (driverInput.isWeaponLeftShoulderPressed()) {
            res |= isMultiLockEnabledWeapon(this.getLeftShoulderWeapon());
        }

        return res;
    }

    public static boolean isMultiLockEnabledWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else if (stack.getItem() instanceof BasePartsItem.Weapon w) {
            return w.maxMultiLockNum() > 0;
        } else {
            return false;
        }
    }

    public static int getMultiLockTargetNum(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else if (stack.getItem() instanceof BasePartsItem.Weapon w) {
            return w.maxMultiLockNum();
        } else {
            return 0;
        }
    }

    @Override
    public boolean shouldLockWeak(DriverInput driverInput) {
        boolean hasWeaponInput =
                driverInput.isWeaponRightHandPressed()
                        || driverInput.isWeaponLeftHandPressed();
        return hasWeaponInput && hasSoftLockCircuit();
    }

    private boolean hasSoftLockCircuit() {
        var i1 = this.getExtension1Weapon().getItem();
        var i2 = this.getExtension2Weapon().getItem();

        return i1 instanceof CircuitSoftLockItem || i2 instanceof  CircuitSoftLockItem;
    }

    @Override
    public boolean shouldLockStrong(DriverInput driverInput) {
        return driverInput.isLockPressed()
            && hasHardLockCircuit();
    }

    private boolean hasHardLockCircuit() {
        var i1 = this.getExtension1Weapon().getItem();
        var i2 = this.getExtension2Weapon().getItem();

        return i1 instanceof CircuitHardLockItem || i2 instanceof  CircuitHardLockItem;
    }

    /******************************************************************************************
     * モーション/サウンド関係の処理
     ******************************************************************************************/

    @Override
    protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
        if (this.actionController.getAction(ACT_JUMP).isInAction()) {
            if (this.actionController.getAction(ACT_JUMP).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".jump"));

        } else if (this.actionController.getAction(ACT_BIND_RAIL).isInAction()) {
            if (this.actionController.getAction(ACT_BIND_RAIL).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".bindrail"));
        }

        if (event.isMoving()) {
            if (this.isBoundToRail() && this.getDeltaMovement().length() > 2) {
                var legPos1 = new Vec3(-1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                var legPos2 = new Vec3(1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

                ParticleUtil.addSparkParticlesSmall(legPos1, this.level());
                ParticleUtil.addSparkParticlesSmall(legPos2, this.level());
            }

            if (this.actionController.getAction(ACT_EVASION).isInAction()) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".evasion"));
            } else if (this.actionController.isBoost()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".dash"));
            } else {
                if (this.isBoundToRail()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".dash"));
                } else if (this.isNoGravity()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".walk"));
                }
            }
        } else {
            if (this.isBoundToRail()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".dash"));
            } else if (this.yRotO != this.getYRot()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".walk"));
            } else {
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
            }
        }
    }

    @Override
    protected PlayState controllAnimationFlyingMotion(AnimationState<PomkotsVehicleBase> event) {
        if (justLanded(event.getAnimatable())) {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".onground"));
        } else if (event.getAnimatable().onGround() || this.isBoundToRail()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".flylegs"));
        }
    }

    @Override
    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
        return null;
    }

    @Override
    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "right_hand", 0, event -> {
            var act = (ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "right")));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "left_hand", 0, event -> {
            var act = (ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "left")));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "right_shoulder", 1, event -> {
            var act = (ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "right")));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "left_shoulder", 1, event -> {
            var act = (ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "left")));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
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
        } else if ("se_gashan".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GASHAN.get());
            var legPos1 = new Vec3(-1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
            var legPos2 = new Vec3(1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

            ParticleUtil.addSparkParticles(legPos1, this.level());
            ParticleUtil.addSparkParticles(legPos2, this.level());
        }
    }

    /**************************************************************************************
     * インベントリ関係の処理
     **************************************************************************************/

    public static final int INV_PARTS_HEAD = 0;
    public static final int INV_PARTS_BODY = 1;
    public static final int INV_PARTS_ARMS = 2;
    public static final int INV_PARTS_LEGS = 3;
    public static final int INV_PARTS_GENERATOR = 4;
    public static final int INV_PARTS_BOOSTER = 5;
    public static final int INV_WEAPON_RIGHT_HAND = 6;
    public static final int INV_WEAPON_LEFT_HAND = 7;
    public static final int INV_WEAPON_RIGHT_SHOULDER = 8;
    public static final int INV_WEAPON_LEFT_SHOULDER = 9;
    public static final int INV_WEAPON_EXT1 = 10;
    public static final int INV_WEAPON_EXT2 = 11;
    public static final int INV_AMMO_RA = 12;
    public static final int INV_AMMO_LA = 13;
    public static final int INV_AMMO_RS = 14;
    public static final int INV_AMMO_LS = 15;
    public static final int INV_FUEL = 16;

    protected ItemStack getHeadPartsFromInventory() {
        return getItem(INV_PARTS_HEAD);
    }

    protected ItemStack getBodyPartsFromInventory() {
        return getItem(INV_PARTS_BODY);
    }

    protected ItemStack getArmPartsFromInventory() {
        return getItem(INV_PARTS_ARMS);
    }

    protected ItemStack getLegsPartsFromInventory() {
        return getItem(INV_PARTS_LEGS);
    }

    protected ItemStack getRightArmWeaponFromInventory() {
        return getItem(INV_WEAPON_RIGHT_HAND);
    }

    protected ItemStack getLeftArmWeaponFromInventory() {
        return getItem(INV_WEAPON_LEFT_HAND);
    }

    protected ItemStack getRightShoulderWeaponFromInventory() {
        return getItem(INV_WEAPON_RIGHT_SHOULDER);
    }

    protected ItemStack getLeftShoulderWeaponFromInventory() {
        return getItem(INV_WEAPON_LEFT_SHOULDER);
    }

    protected ItemStack getGeneratorFromInventory() {
        return getItem(INV_PARTS_GENERATOR);
    }

    protected ItemStack getBoosterFromInventory() {
        return getItem(INV_PARTS_BOOSTER);
    }

    protected ItemStack getExtension1FromInventory() {
        return getItem(INV_WEAPON_EXT1);
    }

    protected ItemStack getExtension2FromInventory() {
        return getItem(INV_WEAPON_EXT2);
    }

    protected ItemStack getAmmoRAFromInventory() {
        return getItem(INV_AMMO_RA);
    }

    protected ItemStack getAmmoLAFromInventory() {
        return getItem(INV_AMMO_LA);
    }

    protected ItemStack getAmmoRSFromInventory() {
        return getItem(INV_AMMO_RS);
    }

    protected ItemStack getAmmoLSFromInventory() {
        return getItem(INV_AMMO_LS);
    }

    protected ItemStack getFuelFromInventory() {
        return getItem(INV_FUEL);
    }

    /**************************************************************************************
     * インベントリ関係/NBT周りの処理
     **************************************************************************************/

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.addChestVehicleSaveData(compound);
        this.addAmmoSaveData(compound);
        compound.putInt(PomkotsMechs.nbtName("TextureColor"), this.getTextureColor());

        compound.putInt(PomkotsMechs.nbtName("FuelNow"), fuel);
    }

    private void addChestVehicleSaveData(CompoundTag compoundTag) {
        if (this.getLootTable() != null) {
            compoundTag.putString("LootTable", this.getLootTable().toString());
            if (this.getLootTableSeed() != 0L) {
                compoundTag.putLong("LootTableSeed", this.getLootTableSeed());
            }
        } else {
            ContainerHelper.saveAllItems(compoundTag, this.getItemStacks());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readChestVehicleSaveData(compound);

        this.setTextureColor(compound.getInt(PomkotsMechs.nbtName("TextureColor")));

        this.syncAllParameter2Client(false);
        this.readAmmoSaveData(compound);

        this.fuel = compound.getInt(PomkotsMechs.nbtName("FuelNow"));
    }

    void readChestVehicleSaveData(CompoundTag compoundTag) {
        this.clearItemStacks();
        if (compoundTag.contains("LootTable", 8)) {
            this.setLootTable(new ResourceLocation(compoundTag.getString("LootTable")));
            this.setLootTableSeed(compoundTag.getLong("LootTableSeed"));
        } else {
            ContainerHelper.loadAllItems(compoundTag, this.getItemStacks());
        }
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide && removalReason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(removalReason);
    }

    /**************************************************************************************
     * インベントリ関係/メニュー周りの処理
     **************************************************************************************/

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
            return super.interact(player, interactionHand);
        }
//        else if (this.getPassengers().isEmpty()) {
//            InteractionResult interactionResult = this.interactWithContainerVehicle(player);
//            if (interactionResult.consumesAction()) {
//                this.gameEvent(GameEvent.CONTAINER_OPEN, player);
//            }
//            return interactionResult;
//        }
        else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (passenger instanceof Player) {
            this.resetExtensionUnitStatus();
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    private InteractionResult interactWithContainerVehicle(Player player) {
        player.openMenu(this);
        return !player.level().isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
        if (!player.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
        }
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (this.lootTable != null && player.isSpectator()) {
            return null;
        } else {
            this.unpackLootTable(inventory.player);

            int mode = MechWorkbenchMenu.MODE_VIEW;
            if (this.getPassengers().isEmpty()) {
                mode = MechWorkbenchMenu.MODE_ASSEMBLE;
            }

            var menu = new MechWorkbenchMenu(i, inventory, this, this, mode);
            menu.setEntityId(this.getId());
            menu.setTextureColor(this.getTextureColor());

            return menu;
        }
    }

    @Override
    public void setChanged() {
        this.syncAllParameter2Client();
        this.initializeAmmoManager(true);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isChestVehicleStillValid(player);
    }

    boolean isChestVehicleStillValid(Player player) {
        return !this.isRemoved() && this.position().closerThan(player.position(), 8.0);
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable((Player)null);
        this.getItemStacks().clear();
    }

    void unpackChestVehicleLootTable(@Nullable Player player) {
        MinecraftServer minecraftServer = this.level().getServer();
        if (this.getLootTable() != null && minecraftServer != null) {
            LootTable lootTable = minecraftServer.getLootData().getLootTable(this.getLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
            }

            this.setLootTable((ResourceLocation)null);
            LootParams.Builder builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }

            lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }

    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    boolean isChestVehicleEmpty() {
        Iterator var1 = this.getItemStacks().iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return this.getChestVehicleItem(i);
    }

    ItemStack getChestVehicleItem(int i) {
        this.unpackChestVehicleLootTable((Player)null);
        return (ItemStack)this.getItemStacks().get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return this.removeChestVehicleItem(i, j);
    }

    ItemStack removeChestVehicleItem(int i, int j) {
        this.unpackChestVehicleLootTable((Player)null);
        return ContainerHelper.removeItem(this.getItemStacks(), i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return this.removeChestVehicleItemNoUpdate(i);
    }

    ItemStack removeChestVehicleItemNoUpdate(int i) {
        this.unpackChestVehicleLootTable((Player)null);
        ItemStack itemStack = (ItemStack)this.getItemStacks().get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.getItemStacks().set(i, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.setChestVehicleItem(i, itemStack);
    }

    @Override
    public SlotAccess getSlot(int i) {
        return this.getChestVehicleSlot(i);
    }

    SlotAccess getChestVehicleSlot(final int i) {
        return i >= 0 && i < this.getContainerSize() ? new SlotAccess() {
            public ItemStack get() {
                return Pmvc01Entity.this.getChestVehicleItem(i);
            }

            public boolean set(ItemStack itemStack) {
                Pmvc01Entity.this.setChestVehicleItem(i, itemStack);
                return true;
            }
        } : SlotAccess.NULL;
    }

    void setChestVehicleItem(int i, ItemStack itemStack) {
        this.unpackChestVehicleLootTable((Player)null);
        this.getItemStacks().set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

    }

    public void unpackLootTable(Player player) {
        this.unpackChestVehicleLootTable(player);
    }

    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setLootTable(ResourceLocation resourceLocation) {
        this.lootTable = resourceLocation;
    }

    public void setLootTableSeed(long l) {
        this.lootTableSeed = l;
    }

    private ResourceLocation lootTable;
    private long lootTableSeed;

    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));

    }

    /**************************************************************************************
     * 弾薬関係
     **************************************************************************************/

    private final List<AmmoManager> ammoManagers = List.of(
            new AmmoManager(this::getAmmoRAFromInventory, this::getRightArmWeaponFromInventory, RELOAD_RA, this),
            new AmmoManager(this::getAmmoLAFromInventory, this::getLeftArmWeaponFromInventory, RELOAD_LA, this),
            new AmmoManager(this::getAmmoRSFromInventory, this::getRightShoulderWeaponFromInventory, RELOAD_RS, this),
            new AmmoManager(this::getAmmoLSFromInventory, this::getLeftShoulderWeaponFromInventory, RELOAD_LS, this));

    protected void tickAmmos() {
        for (var ammoData: ammoManagers) {
            ammoData.tick();
        }
    }

    protected void addAmmoSaveData(CompoundTag compound) {
        compound.putInt(PomkotsMechs.nbtName("BulletNum1"), this.ammoManagers.get(0).bulletNum);
        compound.putInt(PomkotsMechs.nbtName("BulletNum2"), this.ammoManagers.get(1).bulletNum);
        compound.putInt(PomkotsMechs.nbtName("BulletNum3"), this.ammoManagers.get(2).bulletNum);
        compound.putInt(PomkotsMechs.nbtName("BulletNum4"), this.ammoManagers.get(3).bulletNum);
    }

    protected void readAmmoSaveData(CompoundTag compound) {
        this.ammoManagers.get(0).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum1"));
        this.ammoManagers.get(1).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum2"));
        this.ammoManagers.get(2).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum3"));
        this.ammoManagers.get(3).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum4"));

        initializeAmmoManager(false);
    }

    protected void initializeAmmoManager(boolean resetBulletNum) {
        if (this.isServerSide()) {
            for (var ammoData: ammoManagers) {
                ammoData.initialize(resetBulletNum);
            }
        }
    }

    public boolean consumeBullet(int num, ItemStack weaponStack, int weaponItemSlot) {
        var ammoManager = getAmmoManager(weaponItemSlot);
        return ammoManager.consumeBullet(num);

    }

    public boolean consumeBulletFromServerSide(int num, ItemStack weaponStack, int weaponItemSlot) {
        var ammoManager = getAmmoManager(weaponItemSlot);
        return ammoManager.consumeBulletFromServerSide(num);

    }

    public AmmoManager getAmmoManager(int weaponItemSlot) {
        return ammoManagers.get(weaponItemSlot - INV_WEAPON_RIGHT_HAND);
    }

    public static class AmmoManager {
        private int bulletNum;
        private int bulletNumPerMagazine;
        private int magazineNum;
        private final Pmvc01Entity owner;

        private int prevState;
        private int reloadTicks;

        private final Supplier<ItemStack> ammoStackSupplier;
        private final Supplier<ItemStack> weaponStackSupplier;
        private final EntityDataAccessor<Integer> reloadStateAccessor;

        AmmoManager(Supplier<ItemStack> ammoStackSupplier, Supplier<ItemStack> weaponStackSupplier, EntityDataAccessor<Integer> r, Pmvc01Entity owner) {
            this.ammoStackSupplier = ammoStackSupplier;
            this.weaponStackSupplier = weaponStackSupplier;
            this.reloadStateAccessor = r;
            this.owner = owner;
        }

        private void initialize(boolean resetBulletNum) {
            if (owner.isServerSide()) {
                // TODO 流石に汚すぎるのでなんとかしたい
                int tmpBN = this.bulletNum;
                int tmpBNPM = this.bulletNumPerMagazine;

                this.bulletNum = 0;
                this.bulletNumPerMagazine = 0;
                this.magazineNum = 0;

                var ammoStack = this.ammoStackSupplier.get();
                var weaponStack = this.weaponStackSupplier.get();

                if (ammoStack != null && weaponStack != null) {
                    if (ammoStack.getItem() instanceof BasePartsItem.Magazine mag
                            && weaponStack.getItem() instanceof BasePartsItem.Weapon weapon) {
                        if (weapon.isMatchAmmo(mag)) {
                            this.bulletNumPerMagazine = mag.getBulletsPerMagazine(ammoStack);
                            this.magazineNum = ammoStack.getCount();

                            if (resetBulletNum) {
                                if (this.bulletNumPerMagazine != tmpBNPM) {
                                    this.bulletNum = Math.min(bulletNumPerMagazine, tmpBN);
                                } else {
                                    this.bulletNum = tmpBN;
                                }
                            } else {
                                this.bulletNum = tmpBN;
                            }
                        }
                    }
                }
                syncClient();
            }
        }

        protected void tick() {
            if (owner.isClientSide()) {
                int state = owner.entityData.get(this.reloadStateAccessor);
                if (prevState != state) {
                    deserialize(state);
                    prevState = state;
                }
            } else {
                if (this.reloadTicks > 0) {
                    this.reloadTicks--;

                    if (this.reloadTicks == 0) {
                        doActualReload();
                    }
                }
            }
        }

        private void doActualReload() {
            var ammoStack = this.ammoStackSupplier.get();

            if (ammoStack.getCount() > 0 && ammoStack.getItem() instanceof BasePartsItem.Magazine mag) {
                ammoStack.shrink(1);
                this.bulletNum = this.bulletNumPerMagazine = mag.getBulletsPerMagazine(ammoStack);
                this.magazineNum = ammoStack.getCount();
                syncClient();
            }
        }

        protected void syncClient() {
            owner.entityData.set(this.reloadStateAccessor, this.serialize());
        }

        protected boolean consumeBullet(int num) {
            if (this.bulletNumPerMagazine > 0) {
                if (this.bulletNum - num >= 0) {
                    this.bulletNum -= num;

                    if (this.owner.isServerSide() && this.bulletNum == 0 && this.reloadTicks == 0){
                        this.reloadTicks = 59;
                    }

                    return true;
                } else {
                    if (this.owner.isServerSide() && this.reloadTicks == 0){
                        this.reloadTicks = 29;
                    }
                    return false;
                }
            } else {
                return false;
            }
        }

        protected boolean consumeBulletFromServerSide(int num) {
            boolean b = this.consumeBullet(num);
            syncClient();
            return b;
        }

        public int getBulletNum() {
            return bulletNum;
        }

        public int getBulletNumPerMagazine() {
            return bulletNumPerMagazine;
        }

        public int getMagazineNum() {
            return magazineNum;
        }

        private int serialize() {
            int result = 0;

            result |= (bulletNumPerMagazine & 0x3FF) << 22;
            result |= (bulletNum & 0x3FF) << 12;
            result |= (magazineNum & 0x3F) << 6;

            return result;
        }

        private void deserialize(int serialized) {
            bulletNumPerMagazine = (serialized >> 22) & 0x3FF;
            bulletNum = (serialized >> 12) & 0x3FF;
            magazineNum = (serialized >> 6) & 0x3F;
        }
    }

    /**************************************************************************************
     * ロボのデータでサーバ→クライアントに同期したいでーたは全部ここに入れとく
     **************************************************************************************/

    protected static final EntityDataAccessor<ItemStack> P_HEAD = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_ARMS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_BODY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_LEGS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<ItemStack> P_GENERATOR = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_BOOSTER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<ItemStack> W_RIGHT_ARM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_LEFT_ARM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_RIGHT_SHOULDER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_LEFT_SHOULDER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<ItemStack> W_EXTENSION1 = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_EXTENSION2 = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<Integer> DURABILITY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> WEIGHT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> MAX_WEIGHT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> JUMP = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);

    protected static final EntityDataAccessor<Integer> MAX_ENERGY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> ENERGY_CHARGE_PER_TICK = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> WORK_SEC_PER_FUEL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Float> SPEED_MODIFIER_EVASION = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> SPEED_MODIFIER_VERTICAL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> ENERGY_CONSUME_EVASION = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> ENERGY_CONSUME_VERTICAL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Integer> RELOAD_RA = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> RELOAD_LA = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> RELOAD_RS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> RELOAD_LS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Integer> FUEL_MAX = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> FUEL_NOW = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Boolean> IS_BOUND_TO_RAIL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Integer> TEXTURE_COLOR = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(P_HEAD, ItemStack.EMPTY);
        this.entityData.define(P_ARMS, ItemStack.EMPTY);
        this.entityData.define(P_BODY, ItemStack.EMPTY);
        this.entityData.define(P_LEGS, ItemStack.EMPTY);

        this.entityData.define(P_GENERATOR, ItemStack.EMPTY);
        this.entityData.define(P_BOOSTER, ItemStack.EMPTY);

        this.entityData.define(W_RIGHT_ARM, ItemStack.EMPTY);
        this.entityData.define(W_LEFT_ARM, ItemStack.EMPTY);
        this.entityData.define(W_RIGHT_SHOULDER, ItemStack.EMPTY);
        this.entityData.define(W_LEFT_SHOULDER, ItemStack.EMPTY);

        this.entityData.define(W_EXTENSION1, ItemStack.EMPTY);
        this.entityData.define(W_EXTENSION2, ItemStack.EMPTY);

        this.entityData.define(DURABILITY, 0);
        this.entityData.define(WEIGHT, 0);
        this.entityData.define(MAX_WEIGHT, 0);
        this.entityData.define(SPEED, 0F);
        this.entityData.define(JUMP, 0F);
        this.entityData.define(MAX_ENERGY, 0);
        this.entityData.define(ENERGY_CHARGE_PER_TICK, 0);
        this.entityData.define(WORK_SEC_PER_FUEL, 0);
        this.entityData.define(SPEED_MODIFIER_EVASION, 0F);
        this.entityData.define(SPEED_MODIFIER_VERTICAL, 0F);
        this.entityData.define(ENERGY_CONSUME_EVASION, 0);
        this.entityData.define(ENERGY_CONSUME_VERTICAL, 0);

        this.entityData.define(RELOAD_RA, 0);
        this.entityData.define(RELOAD_LA, 0);
        this.entityData.define(RELOAD_RS, 0);
        this.entityData.define(RELOAD_LS, 0);

        this.entityData.define(FUEL_MAX, 0);
        this.entityData.define(FUEL_NOW, 0);

        this.entityData.define(IS_BOUND_TO_RAIL, false);

        this.entityData.define(TEXTURE_COLOR, 0);
    }

    protected void syncAllParameter2Client() {
        syncAllParameter2Client(true);
    }

    protected void syncAllParameter2Client(boolean updateHealth) {
        this.entityData.set(P_HEAD, this.getHeadPartsFromInventory());
        this.entityData.set(P_ARMS, this.getArmPartsFromInventory());
        this.entityData.set(P_BODY, this.getBodyPartsFromInventory());
        this.entityData.set(P_LEGS, this.getLegsPartsFromInventory());

        this.entityData.set(P_GENERATOR, this.getGeneratorFromInventory());
        this.entityData.set(P_BOOSTER, this.getBoosterFromInventory());

        this.entityData.set(W_RIGHT_ARM, this.getRightArmWeaponFromInventory());
        this.entityData.set(W_LEFT_ARM, this.getLeftArmWeaponFromInventory());
        this.entityData.set(W_RIGHT_SHOULDER, this.getRightShoulderWeaponFromInventory());
        this.entityData.set(W_LEFT_SHOULDER, this.getLeftShoulderWeaponFromInventory());

        this.entityData.set(W_EXTENSION1, this.getExtension1FromInventory());
        this.entityData.set(W_EXTENSION2, this.getExtension2FromInventory());

        this.updateMechParams(updateHealth);
        this.registerWeapons();
    }

    private void updateMechParams(boolean updateHealth) {
        MechParam param = new MechParam();

        sumPartsParameter(this.getHeadPartsFromInventory(), param);
        sumPartsParameter(this.getArmPartsFromInventory(), param);
        sumPartsParameter(this.getBodyPartsFromInventory(), param);
        sumPartsParameter(this.getLegsPartsFromInventory(), param);
        sumPartsParameter(this.getRightArmWeaponFromInventory(), param);
        sumPartsParameter(this.getLeftArmWeaponFromInventory(), param);
        sumPartsParameter(this.getRightShoulderWeaponFromInventory(), param);
        sumPartsParameter(this.getLeftShoulderWeaponFromInventory(), param);
        sumPartsParameter(this.getGeneratorFromInventory(), param);
        sumPartsParameter(this.getBoosterFromInventory(), param);
        sumPartsParameter(this.getExtension1FromInventory(), param);
        sumPartsParameter(this.getExtension2FromInventory(), param);

        int newHealth = param.durability + (int) BattleBalance.MECH_HEALTH/2;
        this.entityData.set(DURABILITY, newHealth);
        this.entityData.set(WEIGHT, param.weight);
        this.entityData.set(MAX_WEIGHT, param.maxWeight);
        this.entityData.set(SPEED, calculateSpeedModifier(param.maxWeight, param.weight) * param.speed);
        this.entityData.set(JUMP, calculateSpeedModifier(param.maxWeight, param.weight) * param.jump);
        this.entityData.set(MAX_ENERGY, param.maxEnergy);
        this.entityData.set(ENERGY_CHARGE_PER_TICK, param.energyChargePerTick);
        this.entityData.set(WORK_SEC_PER_FUEL, param.workSecPerFuel);
        this.entityData.set(SPEED_MODIFIER_EVASION, param.speedModifierEvasion);
        this.entityData.set(SPEED_MODIFIER_VERTICAL, param.speedModifierVertical);
        this.entityData.set(ENERGY_CONSUME_EVASION, param.energyConsumeEvasion);
        this.entityData.set(ENERGY_CONSUME_VERTICAL, param.energyConsumeVertical);

        this.syncFuels();

        if (updateHealth) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newHealth);
            this.setHealth(Math.min(this.getHealth(), newHealth));
        }
    }

//    public static void updateMaxHealth(MechEntity mech) {
//        int newMaxHealth = calculateMaxHealth(mech);
//
//        if (PlatformHelper.isForge()) {
//            mech.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
//        } else if (PlatformHelper.isFabric()) {
//            mech.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
//        }
//
//        mech.setHealth(Math.min(mech.getHealth(), newMaxHealth));
//    }

    private void sumPartsParameter(ItemStack stack, MechParam param) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (stack.getItem() instanceof BasePartsItem partsItem) {
            param.durability += partsItem.getDurability(stack);
            param.weight += partsItem.getWeight(stack);

            if (partsItem instanceof BasePartsItem.Legs legs) {
                param.maxWeight += legs.getMaxWeight(stack);
                param.speed += legs.getSpeedModifier(stack);
                param.jump += legs.getJumpModifier(stack);
            } else if (partsItem instanceof BasePartsItem.Generator g) {
                param.maxEnergy += g.getMaxEnergy(stack);
                param.energyChargePerTick += g.getEnergyChargePerTick(stack);
                param.workSecPerFuel += g.getWorkSecPerFuel(stack);
            } else if (partsItem instanceof BasePartsItem.Booster b) {
                param.speedModifierEvasion += b.getSpeedModifierEvasion(stack);
                param.speedModifierVertical += b.getSpeedModifierVertical(stack);
                param.energyConsumeEvasion += b.getEnergyConsumeEvasion(stack);
                param.energyConsumeVertical += b.getEnergyConsumeVertical(stack);
            }
        }
    }

    public float calculateSpeedModifier(float maxLoad, float currentLoad) {
        double ratio = currentLoad / maxLoad;

        if (ratio <= 1.0) {
            // 最大積載量以下（線形補間）
            return (float)(1.0 - 0.4 * Math.max(0, ratio - 0.5));
        } else {
            // 最大積載量超過（二次減衰）
            double excess = ratio - 1.0;
            return (float)(0.2 / (1 + excess * excess * excess * excess));
        }
    }

    public int getDurability() {
        return this.entityData.get(DURABILITY);
    }

    public int getWeight() {
        return this.entityData.get(WEIGHT);
    }

    public int getMaxWeight() {
        return this.entityData.get(MAX_WEIGHT);
    }

    public float getSpeedModifier() {
        return this.entityData.get(SPEED);
    }

    public float getJumpModifier() {
        return this.entityData.get(JUMP);
    }

    public int getEnergyChargePerTick() {
        return this.entityData.get(ENERGY_CHARGE_PER_TICK);
    }

    public float getWorkSecPerFuel() {
        return this.entityData.get(WORK_SEC_PER_FUEL);
    }

    public float getSpeedModifierEvasion() {
        return this.entityData.get(SPEED_MODIFIER_EVASION);
    }

    public float getSpeedModifierVertical() {
        return this.entityData.get(SPEED_MODIFIER_VERTICAL);
    }

    public int getEnergyConsumeEvasion() {
        return this.entityData.get(ENERGY_CONSUME_EVASION);
    }

    public int getEnergyConsumeVertical() {
        return this.entityData.get(ENERGY_CONSUME_VERTICAL);
    }

    public ItemStack getHeadParts() {
        return this.entityData.get(P_HEAD);
    }

    public ItemStack getBodyParts() {
        return this.entityData.get(P_BODY);
    }

    public ItemStack getArmParts() {
        return this.entityData.get(P_ARMS);
    }

    public ItemStack getLegsParts() {
        return this.entityData.get(P_LEGS);
    }

    public ItemStack getBooster() {
        return this.entityData.get(P_BOOSTER);
    }

    public ItemStack getGenerator() {
        return this.entityData.get(P_GENERATOR);
    }

    public ItemStack getRightArmWeapon() {
        return this.entityData.get(W_RIGHT_ARM);
    }

    public ItemStack getLeftArmWeapon() {
        return this.entityData.get(W_LEFT_ARM);
    }

    public ItemStack getRightShoulderWeapon() {
        return this.entityData.get(W_RIGHT_SHOULDER);
    }

    public ItemStack getLeftShoulderWeapon() {
        return this.entityData.get(W_LEFT_SHOULDER);
    }

    public ItemStack getExtension1Weapon() {
        return this.entityData.get(W_EXTENSION1);
    }

    public ItemStack getExtension2Weapon() {
        return this.entityData.get(W_EXTENSION2);
    }

    public void setTextureColor(int i) {
        this.entityData.set(TEXTURE_COLOR, i);
    }

    public int getTextureColor() {
        return this.entityData.get(TEXTURE_COLOR);
    }

    public int getMaxFuel() {
        return this.entityData.get(FUEL_MAX);
    }

    public int getFuelNow() {
        return this.entityData.get(FUEL_NOW);
    }

    private static class MechParam {
        int durability;
        int weight;
        int maxWeight;
        float speed;
        float jump;
        public int maxEnergy;
        public int energyChargePerTick;
        public int workSecPerFuel;
        public int energyConsumeEvasion;
        public int energyConsumeVertical;
        public float speedModifierEvasion;
        public float speedModifierVertical;
    }

    /**************************************************************************************
     * そのほか
     **************************************************************************************/

    public boolean isUsingWeapon(BasePartsItem.Weapon w) {
        String attchPoint = w.getWeaponAttachPoint();
        String side = w.getSide();

        if ("right".equals(side)) {
            if ((BasePartsItem.WeaponInterface.ATTACH_POINT_ARM.equals(attchPoint) || BasePartsItem.WeaponInterface.ATTACH_POINT_HAND.equals(attchPoint))) {
                return this.actionController.getAction(ACT_RIGHT_HAND).isInAction();
            } else if (BasePartsItem.WeaponInterface.ATTACH_POINT_SHOULDER.equals(attchPoint)) {
                return this.actionController.getAction(ACT_RIGHT_SHOULDER).isInAction();
            }
        } else if ("left".equals(side)) {
            if ((BasePartsItem.WeaponInterface.ATTACH_POINT_ARM.equals(attchPoint) || BasePartsItem.WeaponInterface.ATTACH_POINT_HAND.equals(attchPoint))) {
                return this.actionController.getAction(ACT_LEFT_HAND).isInAction();
            } else if (BasePartsItem.WeaponInterface.ATTACH_POINT_SHOULDER.equals(attchPoint)) {
                return this.actionController.getAction(ACT_LEFT_SHOULDER).isInAction();
            }
        }

        return false;
    }

    public boolean isSelfPublic(Entity ent) {
        return isSelf(ent);
    }

    protected Vec3 mainCameraPosition = null;

    public Vec3 getMainCameraPosition() {
        if (mainCameraPosition == null) {
            return Vec3.ZERO;
        } else {
            return mainCameraPosition;
        }
    }

    public void setMainCameraPosition(Vec3 pos) {
        this.mainCameraPosition = pos;
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.deathTime == 10) {
            if (isServerSide()) {
                var level = this.level();
                ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level);
                e.setPos(this.position());
                level.addFreshEntity(e);
            }
        }
    }

    @Override
    protected void checkInsideBlocks() {
    }
}
