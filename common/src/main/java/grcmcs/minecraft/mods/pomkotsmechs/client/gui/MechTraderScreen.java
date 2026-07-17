package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.TraderInventory;
import grcmcs.minecraft.mods.pomkotsmechs.items.coin.AbstractPomCoinItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MechTraderScreen extends AbstractContainerScreen<MechTraderMenu> {

    // =========================================================
    // TEXTURES
    // =========================================================

    private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_hangar.png");
    private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_gradient.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_normal.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
    private static final ResourceLocation TEXTURE_OFFERS_PANEL = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_trader_offers.png");
    private static final ResourceLocation TEXTURE_CARD = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_trader_card.png");
    private static final ResourceLocation TEXTURE_CARD_SELECTED = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_trader_card_selected.png");

    // =========================================================
    // Component Settings
    // =========================================================

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;

    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 14;

    private static final int CARD_WIDTH = 58;
    private static final int CARD_HEIGHT = 18;

    private static final int OFFER_WIDTH = 264;
    private static final int OFFER_HEIGHT = 198;

    private int offersOffsetX = 0;
    private int offersOffsetY = 0;

    private int panelWidth = 108;
    private int panelHeight = 160;

    private int playerInvX = 0;
    private int playerInvY = 0;
    private int playerInvWidth = 164;
    private int playerInvHeight = 94;

    // =========================================================
    // Other variables
    // =========================================================

    private int selectedOfferSlot = -1;
    private Button buyButton;
    private MechTraderEntity trader = null;

    public MechTraderScreen(
            MechTraderMenu menu,
            Inventory inventory,
            Component component
    ) {
        super(menu, inventory, component);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // =====================================================
        // INIT POSITIONS
        // =====================================================

        this.offersOffsetX = leftPos + 20;
        this.offersOffsetY = topPos + 35;

        this.playerInvX = leftPos + 310;
        this.playerInvY = topPos + 35;

        // =====================================================
        // INIT BUTTONS
        // =====================================================

        buyButton = addRenderableWidget(
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
                        Component.literal("Buy"),
                        btn -> {
                            buySelected();
                        }
                )
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        if (this.minecraft != null && this.minecraft.level != null && this.trader == null) {
            for (Entity entity : this.minecraft.level.entitiesForRendering()) {
                if (entity instanceof MechTraderEntity m) {
                    if ((short)(entity.getUUID().hashCode()) == this.menu.getEntityId()) {
                        this.trader = m;
                    }
                }
            }
        }

        renderBackground(guiGraphics);

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                guiGraphics,
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
        guiGraphics.drawString(font, "Time Left: " + getRemainingTimeString(), offersOffsetX, offersOffsetY - 15, 0xFFFFFFFF, false);
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

    private void renderOfferPanel(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY) {

        renderBgTexture(TEXTURE_OFFERS_PANEL, offersOffsetX, offersOffsetY, OFFER_WIDTH, OFFER_HEIGHT, guiGraphics);

        for (MechTraderEntity.TraderCategory category
                : MechTraderEntity.TraderCategory.values()) {
            int col = category.ordinal();

            drawCenteredStringNoShadow(
                    guiGraphics,
                    font,
                    getCategoryString(category),
                    offersOffsetX + 5 + col * 65 + 31,
                    offersOffsetY + 4,
                    0xFFFFFFFF
            );

            for (int row = 0;
                 row < TraderInventory.OFFERS_PER_CATEGORY;
                 row++) {

                int offerSlot =
                        TraderInventory.getOfferSlot(
                                category,
                                row
                        );

                int priceSlot =
                        TraderInventory.getPriceSlot(
                                category,
                                row
                        );

                renderOfferCard(
                        guiGraphics,
                        offerSlot,
                        priceSlot,
                        col,
                        row,
                        mouseX,
                        mouseY
                );
            }
        }
    }

    private String getCategoryString(MechTraderEntity.TraderCategory category) {
        return switch (category) {
            case BLUEPRINT -> "Blue Print";
            case MATERIAL -> "Material";
            case CONSUMABLE -> "Consumable";
            case FOOD -> "Food";
        };
    }

    private void renderOfferCard(
            GuiGraphics gg,
            int offerSlot,
            int priceSlot,
            int col,
            int row,
            int mouseX,
            int mouseY
    ) {
        Slot offer = menu.getSlot(offerSlot);
        Slot price = menu.getSlot(priceSlot);

        ItemStack offerStack =
                offer.getItem();

        ItemStack priceStack =
                price.getItem();

        if (offerStack.isEmpty()) {
            return;
        }

        int x = offersOffsetX + 5 + col * 65;
        int y = offersOffsetY + 17 + row * 22;

        boolean selected = selectedOfferSlot == offerSlot;

        if (selected) {
            renderBgTexture(TEXTURE_CARD_SELECTED, x, y, CARD_WIDTH, CARD_HEIGHT, gg);
        } else {
            renderBgTexture(TEXTURE_CARD, x, y, CARD_WIDTH, CARD_HEIGHT, gg);
        }

        drawCenteredStringNoShadow(
                gg,
                font,
                "=",
                x + 30,
                y + 6,
                0xFFFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        for (MechTraderEntity.TraderCategory category
                : MechTraderEntity.TraderCategory.values()) {

            int col = category.ordinal();

            for (int row = 0;
                 row < TraderInventory.OFFERS_PER_CATEGORY;
                 row++) {

                int offerSlot =
                        TraderInventory.getOfferSlot(
                                category,
                                row
                        );

                int x = offersOffsetX + 5 + col * 65;
                int y = offersOffsetY + 17 + row * 22;

                if (isPointInside(
                        x,
                        y,
                        CARD_WIDTH,
                        CARD_HEIGHT,
                        mouseX,
                        mouseY
                )) {
                    if (!menu.getSlot(offerSlot)
                            .getItem()
                            .isEmpty()) {

                        selectedOfferSlot = offerSlot;
                    }

                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isPointInside(
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    private void buySelected() {
        if (selectedOfferSlot < 0) {
            return;
        } else if (menu.getSlot(selectedOfferSlot).getItem().is(PomkotsMechs.SOLD_ITEM.get())) {
            return;
        }

        minecraft.gameMode
                .handleInventoryButtonClick(
                        menu.containerId,
                        selectedOfferSlot
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

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem() && !(this.hoveredSlot.getItem().getItem() instanceof AbstractPomCoinItem)) {
            super.renderTooltip(guiGraphics, i, j);
        }
    }

    public static void drawCenteredStringNoShadow(
            GuiGraphics guiGraphics,
            Font font,
            String text,
            int centerX,
            int y,
            int color
    ) {
        int width = font.width(text);

        guiGraphics.drawString(
                font,
                text,
                centerX - width / 2,
                y,
                color,
                false
        );
    }

    public String getRemainingTimeString() {
        if (trader == null || trader.getDespawnTime() < 0) {
            return "--:--";
        }

        long despawnDayTime = trader.getDespawnTime();
        long currentDayTime = trader.level().getDayTime();

        long remainingTicks =
                Math.max(
                        0,
                        despawnDayTime - currentDayTime
                );

        int totalSeconds =
                (int)(remainingTicks / 20L);

        int minutes =
                totalSeconds / 60;

        int seconds =
                totalSeconds % 60;

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }
}
