package grcmcs.minecraft.mods.pomkotsmechs.items.radar;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public sealed interface RadarTarget permits RadarTarget.EntityTarget, RadarTarget.CoordTarget {

    record EntityTarget(
            UUID uuid,           // 解決済みUUID（未解決ならnull）
            String selector,     // セレクタ文字列
            String label,
            ResourceLocation dimension // ④ディメンション
    ) implements RadarTarget {}

    record CoordTarget(
            BlockPos pos,
            String label,
            ResourceLocation dimension // ④ディメンション
    ) implements RadarTarget {}

    // --- NBT serialize/deserialize ---

    static RadarTarget fromNbt(CompoundTag tag) {
        String type = tag.getString("type");
        String selector = tag.getString("selector");
        String label = tag.getString("label");
        String level = tag.getString("level");

        return switch (type) {
            case "entity" -> new EntityTarget(tag.getUUID("uuid"), selector, label, new ResourceLocation(level));
            case "coord"  -> new CoordTarget(NbtUtils.readBlockPos(tag.getCompound("pos")), label, new ResourceLocation(level));
            default -> throw new IllegalArgumentException("Unknown target type: " + type);
        };
    }

    static CompoundTag toNbt(RadarTarget target) {
        CompoundTag tag = new CompoundTag();
        tag.putString("label", target.label());
        tag.putString("level", target.dimension().toString());

        if (target instanceof EntityTarget e){
            tag.putString("type", "entity");
            tag.putUUID("uuid", e.uuid());
            tag.putString("selector", e.selector);
        } else if (target instanceof CoordTarget c){
            tag.putString("type", "coord");
            tag.put("pos", NbtUtils.writeBlockPos(c.pos()));
        }

        return tag;
    }

    String label();
    ResourceLocation dimension();
}