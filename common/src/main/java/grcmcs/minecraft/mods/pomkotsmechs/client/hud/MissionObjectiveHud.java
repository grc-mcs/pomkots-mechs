package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class MissionObjectiveHud {
    public static final MissionObjectiveHud INSTANCE = new MissionObjectiveHud();
    private MissionObjectiveHud() { }

    public void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        MissionHudManager.Objective data = MissionHudManager.objective();
        if (mc.options.hideGui || data == null) return;
        float scale = 0.80F;
        int logicalScreenWidth = Math.round(graphics.guiWidth() / scale);
        int width = Math.min(190, logicalScreenWidth - 16);
        int height = 46;
        int x = logicalScreenWidth - width - 10;
        int y = 12;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 800);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.fill(x, y, x + width, y + height, 0x9908151D);
        graphics.drawString(mc.font, Component.literal("MISSION  ").append(data.mission()),
                x + 7, y + 6, 0x55DDF5, false);
        int lineY = y + 19;
        for (FormattedCharSequence line : mc.font.split(data.text(), width - 16)) {
            graphics.drawString(mc.font, line, x + 8, lineY, 0xD8E8EC, false);
            if ((lineY += 10) > y + 27) break;
        }
        String suffix = data.remainingTicks() >= 0 ? "  " + ((data.remainingTicks() + 19) / 20) + "s" : "";
        graphics.drawString(mc.font, data.progress().copy().append(suffix), x + 8, y + 34, 0x72F4E5, false);
        graphics.pose().popPose();
    }
}
