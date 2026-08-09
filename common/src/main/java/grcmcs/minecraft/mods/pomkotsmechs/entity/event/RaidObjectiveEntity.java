package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RaidObjectiveEntity extends LivingEntity {
    private static final String FIXED_X_TAG = "pomkotsmechsFixedX";
    private static final String FIXED_Y_TAG = "pomkotsmechsFixedY";
    private static final String FIXED_Z_TAG = "pomkotsmechsFixedZ";

    protected final BaseBossEntity parentEntity;
    private Vec3 fixedPosition;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MAX_HEALTH, 500)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.ARMOR_TOUGHNESS, 1);
    }

    public RaidObjectiveEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        this(entityType, world, null);
    }

    public RaidObjectiveEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
        super(entityType, world);
        this.parentEntity = parent;
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        if (fixedPosition == null) {
            fixedPosition = this.position();
        }

        // Knockback and other LivingEntity impulses must never move a defense objective.
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(fixedPosition.x, fixedPosition.y, fixedPosition.z);
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(fixedPosition.x, fixedPosition.y, fixedPosition.z);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Stationary mission objective: damage is accepted, displacement is not.
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        Vec3 position = fixedPosition != null ? fixedPosition : this.position();
        tag.putDouble(FIXED_X_TAG, position.x);
        tag.putDouble(FIXED_Y_TAG, position.y);
        tag.putDouble(FIXED_Z_TAG, position.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(FIXED_X_TAG) && tag.contains(FIXED_Y_TAG) && tag.contains(FIXED_Z_TAG)) {
            fixedPosition = new Vec3(
                    tag.getDouble(FIXED_X_TAG),
                    tag.getDouble(FIXED_Y_TAG),
                    tag.getDouble(FIXED_Z_TAG));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount/5);
    }

    @Override
    public boolean displayFireAnimation() {
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
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

}
