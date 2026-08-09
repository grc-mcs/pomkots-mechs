package grcmcs.minecraft.mods.pomkotsmechs.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record RadioPortrait(
        RadioPortraitType type,
        ResourceLocation texture,
        ResourceLocation entityType,
        float yaw,
        float pitch,
        float scale,
        float offsetX,
        float offsetY,
        boolean bustUp,
        boolean slimPlayerModel
) {
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeResourceLocation(texture);
        buf.writeResourceLocation(entityType);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeFloat(scale);
        buf.writeFloat(offsetX);
        buf.writeFloat(offsetY);
        buf.writeBoolean(bustUp);
        buf.writeBoolean(slimPlayerModel);
    }

    public static RadioPortrait read(FriendlyByteBuf buf) {
        return new RadioPortrait(
                buf.readEnum(RadioPortraitType.class),
                buf.readResourceLocation(),
                buf.readResourceLocation(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }
}
