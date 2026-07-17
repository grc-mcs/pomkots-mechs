package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.TraderInventory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PilotScreen extends AbstractContainerScreen<PilotMenu> {
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_normal.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_MOB = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_mob.png");
    private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/item_container.png");

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;

    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 14;

    private static final int OFFER_WIDTH = 164;
    private static final int OFFER_HEIGHT = 120;

    private int offersOffsetX = 0;
    private int offersOffsetY = 0;

    private int playerInvX = 0;
    private int playerInvY = 0;
    private int playerInvWidth = 164;
    private int playerInvHeight = 94;

    public PilotScreen(
            PilotMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(
                menu,
                inventory,
                title
        );

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // =====================================================
        // INIT POSITIONS
        // =====================================================

        this.offersOffsetX = leftPos + 60;
        this.offersOffsetY = topPos + 35;

        this.playerInvX = leftPos + 260;
        this.playerInvY = topPos + 35;

        // =====================================================
        // INIT BUTTONS
        // =====================================================

        addRenderableWidget(
                new ImageButton(
                        playerInvX,
                        playerInvY + playerInvHeight + 10,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT * 4,
                        Component.literal("Ride Mech"),
                        btn -> {
                            rideMech();
                        }
                )
        );
    }

    private void rideMech() {
        minecraft.gameMode
                .handleInventoryButtonClick(
                        menu.containerId,
                        0
                );
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
        renderOfferPanel(guiGraphics, partialTick, mouseX, mouseY);
        renderPlayerInventoryPanel(guiGraphics);
    }

    private void renderOfferPanel(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY) {

        renderBgTexture(TEXTURE_PANEL_INV_MOB, offersOffsetX, offersOffsetY, OFFER_WIDTH, OFFER_HEIGHT, guiGraphics);
        guiGraphics.drawString(
                font,
                "Mob Equipments",
                offersOffsetX + 18,
                offersOffsetY + 4,
                0xFFFFFFFF
        );

        int offsetX = offersOffsetX + 10;
        int offsetY = offersOffsetY + 15;

        renderMobEquipment("Pilot License", offsetX, offsetY, guiGraphics);

        offsetY += 18;
        renderMobEquipment("Pilot Role", offsetX, offsetY, guiGraphics);

        offsetY += 18;
        renderMobEquipment("Head", offsetX, offsetY, guiGraphics);

        offsetY += 18;
        renderMobEquipment("Chest", offsetX, offsetY, guiGraphics);

        offsetY += 18;
        renderMobEquipment("Legs", offsetX, offsetY, guiGraphics);

        offsetY += 18;
        renderMobEquipment("Feet", offsetX, offsetY, guiGraphics);
    }

    private void renderMobEquipment(String text, int x, int y, GuiGraphics guiGraphics) {
        guiGraphics.drawString(
                font,
                text,
                x + 24,
                y + 5,
                0xFFFFFFFF
        );

        renderBgTexture(
                TEXTURE_ITEM_CONTAINER,
                x,
                y,
                16,
                16,
                guiGraphics);
    }

    private void renderPlayerInventoryPanel(
            GuiGraphics guiGraphics) {
        renderBgTexture(TEXTURE_PANEL_INV_PLAYER, playerInvX, playerInvY, playerInvWidth, playerInvHeight, guiGraphics);
        guiGraphics.drawString(
                font,
                "Player Inventory",
                playerInvX + 18,
                playerInvY + 4,
                0xFFFFFFFF
        );
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

    @Override
    protected void renderLabels(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
    }
}