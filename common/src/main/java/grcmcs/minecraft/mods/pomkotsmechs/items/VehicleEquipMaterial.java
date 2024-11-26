package grcmcs.minecraft.mods.pomkotsmechs.items;


import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class VehicleEquipMaterial implements Tier {
    public static final VehicleEquipMaterial INSTANCE = new VehicleEquipMaterial();

    @Override
    public int getUses() {
        return 0;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return null;
    }
}
