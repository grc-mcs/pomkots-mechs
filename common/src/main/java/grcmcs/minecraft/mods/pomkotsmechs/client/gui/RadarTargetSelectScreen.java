package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class RadarTargetSelectScreen extends AbstractContainerScreen<RadarTargetSelectMenu> {

    private static final int ENTRY_HEIGHT = 20;
    private static final int SCREEN_W     = 200;
    private static final int SCREEN_H     = 180;

    public RadarTargetSelectScreen(RadarTargetSelectMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = SCREEN_W;
        this.imageHeight = SCREEN_H;
    }

    @Override
    protected void init() {
        super.init();

        List<RadarTarget> targets = PomkotsRadarItem.getTargets(menu.getRadarStack());
        int selectedIdx = PomkotsRadarItem.getSelectedIndex(menu.getRadarStack());

        for (int i = 0; i < targets.size(); i++) {
            RadarTarget target = targets.get(i);
            final int idx      = i;
            boolean selected   = (i == selectedIdx);

            this.addRenderableWidget(Button.builder(
                    Component.literal((selected ? "▶ " : "  ") + target.label()
                            + " [" + dimensionShortName(target.dimension()) + "]"),
                    btn -> onTargetSelected(idx)
            ).bounds(this.leftPos + 10, this.topPos + 30 + i * (ENTRY_HEIGHT + 2), SCREEN_W - 20, ENTRY_HEIGHT).build());
        }
    }

    private void onTargetSelected(int idx) {
        // サーバーにパケット送信
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(idx);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_RADAR_SELECT_TARGET), buf);
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC000000);
        gui.drawCenteredString(font, "Select Radar Target", leftPos + imageWidth / 2, topPos + 10, 0xFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        // 上書きして何も描画しない（ラベル不要）
    }

    private String dimensionShortName(ResourceLocation dim) {
        return switch (dim.getPath()) {
            case "overworld" -> "OW";
            case "the_nether" -> "NE";
            case "the_end"   -> "EN";
            default          -> dim.getPath();
        };
    }
}
