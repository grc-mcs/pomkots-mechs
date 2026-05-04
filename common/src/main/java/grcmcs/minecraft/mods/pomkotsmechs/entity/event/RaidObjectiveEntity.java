package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RaidObjectiveEntity extends LivingEntity {
    protected final BaseBossEntity parentEntity;

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
        super.tick();
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
