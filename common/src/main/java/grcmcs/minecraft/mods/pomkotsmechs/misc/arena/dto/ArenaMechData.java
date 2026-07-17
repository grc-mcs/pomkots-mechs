package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ArenaMechData {

    private String mechName;

    private int textureColor;

    public String getMechName() {
        return mechName;
    }

    public void setMechName(String mechName) {
        this.mechName = mechName;
    }

    public NonNullList<ItemStack> getParts() {
        return parts;
    }

    public void setParts(NonNullList<ItemStack> parts) {
        this.parts = parts;
    }

    public int getTextureColor() {
        return textureColor;
    }

    public void setTextureColor(int textureColor) {
        this.textureColor = textureColor;
    }

    private NonNullList<ItemStack> parts = NonNullList.withSize(18, ItemStack.EMPTY);

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("MechName", mechName);

        tag.putInt("TextureColor", textureColor);

        ListTag partsList = new ListTag();

        for (int i = 0; i < parts.size(); i++) {
            ItemStack stack = parts.get(i);

            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag stackTag = new CompoundTag();

            stackTag.putInt("Slot", i);

            if (stack.getItem() instanceof BasePartsItem basePartsItem) {
                stackTag.putInt(PomkotsMechs.nbtName("Level"), basePartsItem.getLevel(stack));
            }

            stack.save(stackTag);

            partsList.add(stackTag);
        }

        tag.put("Parts", partsList);

        return tag;
    }

    public static ArenaMechData load(CompoundTag tag) {
        ArenaMechData data = new ArenaMechData();

        data.mechName = tag.getString("MechName");

        data.textureColor = tag.getInt("TextureColor");

        ListTag partsList =
                tag.getList("Parts", Tag.TAG_COMPOUND);

        NonNullList<ItemStack> parts =
                NonNullList.withSize(18, ItemStack.EMPTY);

        for (Tag element : partsList) {
            CompoundTag stackTag = (CompoundTag) element;
            int slot = stackTag.getInt("Slot");
            parts.set(
                    slot,
                    ItemStack.of(stackTag)
            );
        }

        data.parts = parts;

        return data;
    }

    public static ArenaMechData fromMechInstance(Pmvc01Entity mech) {
        ArenaMechData data = new ArenaMechData();

        var cName = mech.getCustomName();

        if (cName == null) {
            data.setMechName("No Name");
        } else {
            data.setMechName(cName.getString());
        }
        data.setTextureColor(mech.getTextureColor());

        var parts = data.parts;

        parts.set(0, mech.getHeadParts());
        parts.set(1, mech.getBodyParts());
        parts.set(2, mech.getArmParts());
        parts.set(3, mech.getLegsParts());
        parts.set(4, mech.getGenerator());
        parts.set(5, mech.getBooster());
        parts.set(6, mech.getRightArmWeapon());
        parts.set(7, mech.getLeftArmWeapon());
        parts.set(8, mech.getRightShoulderWeapon());
        parts.set(9, mech.getLeftShoulderWeapon());
        parts.set(10, mech.getExtension1Weapon());
        parts.set(11, mech.getExtension1Weapon());

        return data;
    }
}
