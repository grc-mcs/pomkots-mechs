package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.components.RadarTargetList;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
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

import java.util.ArrayList;
import java.util.List;

public class RadarTargetSelectScreen extends AbstractContainerScreen<RadarTargetSelectMenu> {
    private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/radar_menu.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_large.png");

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;

    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 28;

    private int buttonOffsetX = 0;
    private int buttonOffsetY = 0;

    private RadarTargetList targetList;

    public RadarTargetSelectScreen(RadarTargetSelectMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        buttonOffsetX = (GUI_WIDTH - BUTTON_WIDTH) / 2;
        buttonOffsetY = 50;


        targetList =
                new RadarTargetList(
                        this,
                        minecraft
                );

        addRenderableWidget(
                targetList
        );

        targetList.addTargets(
                loadTargets().getSecond()
        );
    }

    private Pair<Integer, List<RadarTarget>> loadTargets() {
        List<RadarTarget> targets = null;
        int selectedIdx = 0;

        var radarStack = menu.getRadarStack();
        if (radarStack.getItem() instanceof PomkotsRadarItem) {
            targets = PomkotsRadarItem.getTargets(radarStack);
            selectedIdx = PomkotsRadarItem.getSelectedIndex(radarStack);

        } else if (radarStack.getItem() instanceof PomkotsDatapadItem) {
            targets = PomkotsDatapadItem.getTargets(radarStack);
            selectedIdx = PomkotsDatapadItem.getSelectedIndex(radarStack);

        } else {
            targets = new ArrayList<>();
        }

        return new Pair<>(selectedIdx, targets);
    }

    public void onTargetSelected(int idx) {
        // サーバーにパケット送信
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(idx);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_RADAR_SELECT_TARGET), buf);
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        renderBackground(graphics);

        renderBgTexture(
                TEXTURE_BG_1,
                leftPos, topPos,
                leftPos + GUI_WIDTH, topPos + GUI_HEIGHT,
                graphics);

        graphics.drawCenteredString(font, "Select Radar Target", leftPos + imageWidth / 2, buttonOffsetY - 10, 0xFFFFFF);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {

    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        // 上書きして何も描画しない（ラベル不要）
    }

    private ImageButton buildButton(String text, int x, int y, Button.OnPress onPress) {
        return buildButton(text, x, y, onPress, true);
    }

    private ImageButton buildButton(String text, int x, int y, Button.OnPress onPress, boolean active) {
        var b = new ImageButton(
                x,
                y,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                TEXTURE_BUTTON,
                0,
                0,
                BUTTON_WIDTH,
                BUTTON_HEIGHT * 4,
                Component.literal(text),
                onPress
        );
        b.active = active;

        return b;
    }

    private void renderBgTexture(
            ResourceLocation texture,
            int x, int y,
            int width, int height,
            GuiGraphics guiGraphics) {
        renderBgTexture(
                texture,
                x, y,
                width, height,
                0.3F,
                guiGraphics);
    }

    private void renderBgTexture(
            ResourceLocation texture,
            int x, int y,
            int width, int height,
            float alpha,
            GuiGraphics guiGraphics) {

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                alpha // alpha
        );
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, texture);
        guiGraphics.blit(
                texture,
                x,
                y,
                0,
                0,
                width,
                height,
                width,
                height
        );

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
