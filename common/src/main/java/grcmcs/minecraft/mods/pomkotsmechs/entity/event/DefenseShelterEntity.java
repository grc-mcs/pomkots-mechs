package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Large stationary shelter used to physically block a base entrance.
 *
 * EntityDimensions only supports a square horizontal footprint, so this entity
 * supplies its own yaw-aware 10 x 11 x 2 bounding box.
 */
public class DefenseShelterEntity extends LivingEntity implements GeoEntity {
    public static final float SHELTER_WIDTH = 10.0F;
    public static final float SHELTER_HEIGHT = 11.0F;
    public static final float SHELTER_DEPTH = 2.0F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public DefenseShelterEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        this.noCulling = true;
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 256.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 18);
    }

    @Override
    public void tick() {
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        setBoundingBox(makeBoundingBox());
    }

    @Override
    protected AABB makeBoundingBox() {
        double radians = Math.toRadians(getYRot());
        double absCos = Math.abs(Math.cos(radians));
        double absSin = Math.abs(Math.sin(radians));
        double halfX = (SHELTER_WIDTH * absCos + SHELTER_DEPTH * absSin) * 0.5D;
        double halfZ = (SHELTER_WIDTH * absSin + SHELTER_DEPTH * absCos) * 0.5D;

        return new AABB(
                getX() - halfX,
                getY(),
                getZ() - halfZ,
                getX() + halfX,
                getY() + SHELTER_HEIGHT,
                getZ() + halfZ
        );
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        // This is a fixed defensive structure, not a movable mob.
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    protected void checkInsideBlocks() {
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.dead && !level().isClientSide()) {
            triggerAnim("action_controller", "break");
        }
        super.die(damageSource);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<DefenseShelterEntity>(
                        this,
                        "action_controller",
                        state -> PlayState.STOP
                ).triggerableAnim(
                        "break",
                        RawAnimation.begin().thenPlayAndHold("animation.bossbox.break")
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
