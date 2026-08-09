package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class MissionPresentationHud {
    public static final MissionPresentationHud INSTANCE = new MissionPresentationHud();
    public static final int ENTER_TICKS = 6;
    public static final int EXIT_TICKS = 6;
    private MissionPresentationHud() { }

    public void render(GuiGraphics graphics, float partialTick) {
        var snapshot = MissionHudManager.presentation(partialTick);
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || snapshot == null || snapshot.flicker()) return;
        float alpha = Mth.clamp(snapshot.alpha(), 0, 1);
        int width = Math.min(430, graphics.guiWidth() - 24);
        int height = 64;
        int x = (graphics.guiWidth() - width) / 2;
        int y = (graphics.guiHeight() - height) / 2;
        int openHeight = Math.max(1, Math.round(height * alpha));
        int top = y + (height - openHeight) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 1200);
        graphics.enableScissor(x, top, x + width, top + openHeight);
        graphics.fill(x, y, x + width, y + height, Mth.clamp(Math.round(alpha * 175), 0, 255) << 24 | 0x08151D);
        int titleWidth = mc.font.width(snapshot.value().title());
        graphics.drawString(mc.font, snapshot.value().title(), x + (width - titleWidth) / 2, y + 17,
                Mth.clamp(Math.round(alpha * 255), 0, 255) << 24 | 0x55DDF5, false);
        int messageWidth = mc.font.width(snapshot.value().message());
        graphics.drawString(mc.font, snapshot.value().message(), x + (width - messageWidth) / 2, y + 35,
                Mth.clamp(Math.round(alpha * 255), 0, 255) << 24 | 0xFFFFFF, false);
        graphics.disableScissor();
        graphics.pose().popPose();
    }
}
