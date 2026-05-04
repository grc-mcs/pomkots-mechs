package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.PlacementPreviewEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlacementPreviewEntityManager {
    private static PlacementPreviewEntity preview;

    public static void enable(Level level) {
        if (preview != null) return;

        preview = new PlacementPreviewEntity(
                PomkotsMechs.PLACEMENT_PREVIEW.get(),
                level
        );
        preview.setPos(Minecraft.getInstance().cameraEntity.position());
        level.addFreshEntity(preview);
    }

    public static void disable() {
        if (preview != null) {
            preview.discard();
            preview = null;
        }
    }

    public static void tick(Minecraft mc) {
        if (preview == null || mc.player == null) return;

        HitResult hit = mc.player.pick(30, 0, true);
        if (!(hit instanceof BlockHitResult bhr)) return;


        Vec3 pos = bhr.getBlockPos().getCenter();
        preview.setPos(pos.x, pos.y, pos.z);

        // 視点に追従
        preview.setYRot(mc.player.getYRot());
        preview.yRotO = preview.getYRot();
    }
}
