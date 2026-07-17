package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DataPadKeyCardScreen
        extends AbstractContainerScreen<DataPadKeyCardMenu> {
    // =========================================================
    // TEXTURES
    // =========================================================

    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_ITEM_CONTAINER_KEYCARD = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_mech_keycard.png");

    // =========================================================
    // Component Settings
    // =========================================================

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;


    private int leftCardOffsetX = 0;
    private int leftCardOffsetY = 0;

    private int rightCardOffsetX = 0;
    private int rightCardOffsetY = 0;

    private static final int CARD_WIDTH = 164;
    private static final int CARD_HEIGHT = 130;

    public DataPadKeyCardScreen(
            DataPadKeyCardMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

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

    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
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
                TEXTURE_ITEM_CONTAINER_KEYCARD,
                leftCardOffsetX,
                leftCardOffsetY,
                164,
                170,
                guiGraphics
        );

        guiGraphics.drawString(
                font,
                "Put mech Key Cards here",
                leftCardOffsetX + 18,
                leftCardOffsetY + 4,
                0xFFFFFFFF
        );

        for (int i = 0; i < PomkotsDatapadItem.MAX_KEYCARD_SLOTS; i++) {
            if (i >= menu.slots.size()) {
                break;
            }

            if (menu.slots.get(i).getItem().getItem() instanceof KeycardItem) {
                var stack = menu.slots.get(i).getItem();
                String name = KeycardItem.getMechName(stack);
                if (!name.isEmpty()) {
                    guiGraphics.drawString(
                            font,
                            name,
                            leftCardOffsetX + 36,
                            leftCardOffsetY + 30 + i * 18 - 4,
                            0xFFFFFFFF
                    );
                }
            }
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
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
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
                0.5F,
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
