package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.components.SummonMechList;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
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
import java.util.UUID;

public class MechSalvagerScreen extends AbstractContainerScreen<MechSalvagerMenu> {

    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
    private static final ResourceLocation TEXTURE_MECH_SELECTOR = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_selector.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_normal.png");

    // =========================================================
    // Component Settings
    // =========================================================

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;

    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 14;

    private static final int CARD_WIDTH = 164;
    private static final int CARD_HEIGHT = 130;

    private int leftCardOffsetX = 0;
    private int leftCardOffsetY = 0;

    private int rightCardOffsetX = 0;
    private int rightCardOffsetY = 0;

    private boolean isSlotInited = false;

    private SummonMechList targetList;
    private UUID mechUUID = null;

    private Button summonButton;

    public MechSalvagerScreen(MechSalvagerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

    }

    @Override
    protected void init() {
        super.init();

        int offsetX = 20;
        int offsetY = 40;

        this.leftCardOffsetX = offsetX + 28;
        this.leftCardOffsetY = offsetY;
        this.rightCardOffsetX = offsetX + 28 + CARD_WIDTH + 28 + 28;
        this.rightCardOffsetY = offsetY;

        targetList =
                new SummonMechList(
                        this,
                        minecraft
                );

        addRenderableWidget(
                targetList
        );

        summonButton = this.addRenderableWidget(
                buildButton(
                    "Summon",
                        rightCardOffsetX,
                        rightCardOffsetY + 130,
                        btn -> {
                            sendSummonPacket2Server();
                        }
                )
        );
    }

    public void setSelected(UUID slot) {
        this.mechUUID = slot;
    }

    private void sendSummonPacket2Server() {
        if (this.mechUUID != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(this.mechUUID);
            NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_SUMMON_MECH), buf);
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (!isSlotInited && menu.getDataPad() != null && menu.getDataPad().getItem() instanceof PomkotsDatapadItem) {
            isSlotInited = true;

            var dataPad = menu.getDataPad();

            if (dataPad.getItem() instanceof PomkotsDatapadItem) {
                List<Pair<UUID, String>> targets = new ArrayList<>();

                for (var keyCardSlot: PomkotsDatapadItem.createKeyCardContainer(dataPad).items) {
                    if (keyCardSlot.getItem() instanceof KeycardItem
                            && KeycardItem.getMechUuid(keyCardSlot) != null
                    ) {
                        targets.add(new Pair<>(
                                KeycardItem.getMechUuid(keyCardSlot),
                                KeycardItem.getMechName(keyCardSlot)
                        ));
                    }
                }
                targetList.addTargets(targets);
            }
        }

        summonButton.active = mechUUID != null;

        renderBackground(graphics);

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
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        renderBgTexture(
                TEXTURE_MECH_SELECTOR,
                leftCardOffsetX,
                leftCardOffsetY,
                164,
                170,
                guiGraphics
        );

        guiGraphics.drawString(
                font,
                "Select a mech to summon",
                leftCardOffsetX + 18,
                leftCardOffsetY + 4,
                0xFFFFFFFF
        );

        if (menu.getDataPad().isEmpty()) {
            guiGraphics.drawString(
                    font,
                    "No data pad in your inventory",
                    leftCardOffsetX + 10,
                    leftCardOffsetY + 70,
                    0xFFFF2222
            );
        } else if (targetList.getSize() == 0) {
            guiGraphics.drawString(
                    font,
                    "No key card in your data pad",
                    leftCardOffsetX + 10,
                    leftCardOffsetY + 70,
                    0xFFFF2222
            );
        }

        renderBgTexture(
                TEXTURE_PANEL_INV_PLAYER,
                rightCardOffsetX,
                leftCardOffsetY,
                164,
                94,
                guiGraphics
        );

        guiGraphics.drawString(
                font,
                "Player Inventory",
                rightCardOffsetX + 18,
                leftCardOffsetY + 4,
                0xFFFFFFFF
        );
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
