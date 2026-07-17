package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MechWorkbenchScreen extends AbstractContainerScreen<MechWorkbenchMenu> {

    // =========================================================
    // TEXTURES
    // =========================================================

    private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_hangar.png");
    private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_gradient.png");

    private static final ResourceLocation TEXTURE_PANEL_PARTS_SELECTOR = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/panel_parts_selector_background.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_MECH = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_cargo.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
    private static final ResourceLocation TEXTURE_PANEL_SPEC = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/stats_panel.png");
    private static final ResourceLocation TEXTURE_PANEL_CIRCUIT = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_circuits.png");

    private static final ResourceLocation TEXTURE_ICON_AMMO = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/icon_ammo.png");
    private static final ResourceLocation TEXTURE_ICON_FUEL = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/icon_fuel.png");
    private static final ResourceLocation TEXTURE_ICON_REPAIR = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/icon_repair.png");

    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_normal.png");

    private static final ResourceLocation TEXTURE_TAB_UNSELECTED_TOP = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/tab_unselected_top.png");
    private static final ResourceLocation TEXTURE_TAB_UNSELECTED_BOTTOM = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/tab_unselected_bottom.png");

    private static final ResourceLocation TEXTURE_TAB_SELECTED_TOP = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/tab_selected_top.png");
    private static final ResourceLocation TEXTURE_TAB_SELECTED_BOTTOM = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/tab_selected_bottom.png");

    private static final ResourceLocation TEXTURE_CARD_PARTS_BG = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/part_box_unselected.png");
    private static final ResourceLocation TEXTURE_CARD_PARTS_SELECTED_BG = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/part_box_selected.png");

    private static final ResourceLocation TEXTURE_WINDOW_TITLE = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/window_name.png");

    private static final ResourceLocation TEXTURE_SCROLL_BAR_BG = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/scroll_bar_background.png");
    private static final ResourceLocation TEXTURE_SCROLL_BAR = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/scroll_bar.png");

    private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/item_container.png");

    private static final ResourceLocation TEXTURE_ROAD_CROSSHAIR = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/road/crosshair.png");
    private static final ResourceLocation TEXTURE_ROAD_PART_INDICATOR_01 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/road/part_indicator_01.png");
    private static final ResourceLocation TEXTURE_ROAD_PART_INDICATOR_02 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/road/part_indicator_02.png");
    private static final ResourceLocation TEXTURE_ROAD_STAT_PANEL_SMALL = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/road/stats_panel_small.png");

    // =========================================================
    // Component Settings
    // =========================================================

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;

    private int panelX = 0;
    private int panelY = 0;

    private int panelWidth = 108;
    private int panelHeight = 160;

    private int tabTopOffsetY = -16;
    private int tabButtomOffsetY = 0;

    private final int cardWidth = 48;
    private final int cardHeight = 48;
    private final int cardModelHeight = 36;

    private final int cardSpacingX = 3;
    private final int cardSpacingY = 3;

    private final int columns = 2;

    private int cargoX = 0;
    private int cargoY = 0;
    private int cargoWidth = 164;
    private int cargoHeight = 90;

    private int playerInvX = 0;
    private int playerInvY = 0;
    private int playerInvWidth = 164;
    private int playerInvHeight = 94;

    private int specsX = 0;
    private int specsY = 0;
    private int specsWidth = 180;
    private int specsHeight = 56;

    private int slotSize = 18;
    private int slotSpacing = 20;

    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 14;

    private static final int DROPDOWN_WIDTH = BUTTON_WIDTH;
    private static final int DROPDOWN_HEIGHT = BUTTON_HEIGHT;
    private static final int DROPDOWN_OFFSET_X = 400;
    private static final int DROPDOWN_OFFSET_Y = 15;

    private float modelYaw = -20f;
    private float modelPitch = -10f;

    private boolean draggingModel = false;

    private double lastMouseX;
    private double lastMouseY;

    private int modelAreaX = 112;
    private int modelAreaY = 25;

    private int modelAreaWidth = 200;
    private int modelAreaHeight = 150;

    private int modelScale = 20;

    // =========================================================
    // Scroll
    // =========================================================

    private int scrollOffset = 0;
    private int maxScroll = 0;

    // =========================================================
    // Tabs
    // =========================================================

    public enum PartsTab {
        HEAD(0, "textures/gui/mechworkbench/icon_head.png"),
        BODY(1, "textures/gui/mechworkbench/icon_body.png"),
        ARM(2, "textures/gui/mechworkbench/icon_arms.png"),
        LEGS(3, "textures/gui/mechworkbench/icon_legs.png"),

        GENERATOR(4, "textures/gui/mechworkbench/icon_generator.png"),
        BOOSTER(5, "textures/gui/mechworkbench/icon_booster.png"),

        RIGHT_ARM(6, "textures/gui/mechworkbench/icon_w_a_r.png"),
        LEFT_ARM(7, "textures/gui/mechworkbench/icon_w_a_l.png"),

        RIGHT_SHOULDER(8, "textures/gui/mechworkbench/icon_w_s_r.png"),
        LEFT_SHOULDER(9, "textures/gui/mechworkbench/icon_w_s_l.png"),

        EXT1(10, "textures/gui/mechworkbench/icon_ext_1.png"),
        EXT2(11, "textures/gui/mechworkbench/icon_ext_2.png");

        public final int mechSlot;

        public final ResourceLocation icon;

        PartsTab(int mechSlot, String texture) {
            this.mechSlot = mechSlot;
            this.icon = new ResourceLocation(PomkotsMechs.MODID, texture);
        }
    }

    private static final PartsTab[] TOP_TABS = {
            PartsTab.HEAD,
            PartsTab.BODY,
            PartsTab.ARM,
            PartsTab.LEGS,
            PartsTab.GENERATOR,
            PartsTab.BOOSTER
    };

    private static final PartsTab[] BOTTOM_TABS = {
            PartsTab.RIGHT_ARM,
            PartsTab.LEFT_ARM,
            PartsTab.RIGHT_SHOULDER,
            PartsTab.LEFT_SHOULDER,
            PartsTab.EXT1,
            PartsTab.EXT2
    };

    private PartsTab currentTab = PartsTab.HEAD;

    // =========================================================
    // Visible Items
    // =========================================================

    private final List<Integer> visiblePartSlots = new ArrayList<>();

    // =========================================================
    // Other variables
    // =========================================================

    private Pmvc01Entity mech = null;

    private EditBox nameField;
    private boolean nameFieldInited = false;

    private Button renameButton;
    private Button generateCardButton;
    private Button repairMechButton;
    private Button autoSupply;
    private Button textureColorButton;
    private Button circuitButton;

    private boolean isDropdownOpen = false;
    private List<String> options = BasePartsItemModel.BASE_COLORS;
    private String selectedOption = "Color";

    private boolean circuitInvActive = false;

    public MechWorkbenchScreen(MechWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        this.titleLabelX = 44;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        // =====================================================
        // INIT POSITIONS
        // =====================================================

        this.panelX = leftPos + 8;
        this.panelY = topPos + 50;

        this.cargoX = leftPos + 310;
        this.cargoY = topPos + 35;

        this.playerInvX = leftPos + 310;
        this.playerInvY = topPos + 135;

        this.specsX = panelX + panelWidth + 7;
        this.specsY = panelY + panelHeight + slotSize * 2 + 5 - specsHeight;

        // =====================================================
        // INIT TEXT FIELDS
        // =====================================================

        nameField =
                new EditBox(
                        font,
                        panelX + 120,
                        this.topPos + DROPDOWN_OFFSET_Y,
                        80,
                        BUTTON_HEIGHT,
                        Component.literal("Name")
                );

        nameField.setMaxLength(32);

        addRenderableWidget(nameField);

        // =====================================================
        // INIT BUTTONS
        // =====================================================

        circuitButton = addRenderableWidget(
                new ImageButton(
                        specsX + specsWidth - BUTTON_WIDTH - 5,
                        specsY + 2,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT * 4,
                        Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.circuit}"),
                        btn -> {
                            circuitInvActive = !circuitInvActive;
                            menu.setActivationForCircuitSlots(circuitInvActive);
                        }
                )
        );

        renameButton = addRenderableWidget(
                new ImageButton(
                        panelX+ 120 + 80 + 5,
                        this.topPos + DROPDOWN_OFFSET_Y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT * 4,
                        Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.rename}"),
                        btn -> {
                            sendMechName(nameField.getValue());
                        }
                )
        );

        generateCardButton = addRenderableWidget(
                new ImageButton(
                        playerInvX,
                        this.topPos + DROPDOWN_OFFSET_Y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT * 4,
                        Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.getcard}"),
                        btn -> {
                            sendMechOperationAction(PomkotsMechs.PACKET_SECURITY_GENCARD);
                        }
                )
        );

        textureColorButton = addRenderableWidget(
                new ImageButton(
                        this.leftPos + DROPDOWN_OFFSET_X,
                        this.topPos + DROPDOWN_OFFSET_Y,
                        DROPDOWN_WIDTH,
                        DROPDOWN_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        DROPDOWN_WIDTH,
                        DROPDOWN_HEIGHT * 4,
                        Component.literal(selectedOption),
                        btn -> {
                            isDropdownOpen = !isDropdownOpen;
                        }
                )
        );

        var buttonBottom = panelY + panelHeight + slotSize + 16 + 4;

        repairMechButton = addRenderableWidget(
                new ImageButton(
                        playerInvX,
                        buttonBottom - BUTTON_HEIGHT,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT * 4,
                        Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.repair}"),
                        btn -> {
                            sendMechOperationAction(PomkotsMechs.PACKET_REPAIR_MECH);
                        }
                )
        );

        autoSupply = addRenderableWidget(
                new ImageButton(
                        playerInvX + 90,
                        buttonBottom - BUTTON_HEIGHT,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        TEXTURE_BUTTON,
                        0,
                        0,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT * 4,
                        Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.autosupply}"),
                        btn -> {
                            sendMechOperationAction(PomkotsMechs.PACKET_AUTO_SUPPLY);
                        }
                )
        );

        this.mech = null;

        this.menu.setActivationForCircuitSlots(false);
        rebuildVisibleParts();
    }

    // =========================================================
    // Render Background
    // =========================================================

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY) {

        if (!nameFieldInited && this.mech != null && this.mech.hasCustomName()) {
            nameField.setValue(
                    this.mech.getCustomName().getString()
            );
            nameFieldInited = true;
        }

        if (menu.getMode() == MechWorkbenchMenu.MODE_VIEW) {
            renameButton.visible = false;
            generateCardButton.visible = false;
            repairMechButton.visible = false;
            textureColorButton.visible = false;
            nameField.active = false;
            // @TODO ROAD UI
//            modelAreaX = 10;
//            modelAreaY = 120;
//            modelAreaWidth = 200;
//            modelAreaHeight = 130;
//            modelScale = 30;
        }

        // =====================================================
        // Background
        // =====================================================

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0);

        if (menu.getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
            RenderSystem.setShaderTexture(0, TEXTURE_BG_1);
            guiGraphics.blit(
                    TEXTURE_BG_1,
                    0, 0,
                    this.width, this.height,
                    0F, 0F,
                    960, 540,
                    960, 540
            );
        }

        String title;
        if (menu.getMode() == MechWorkbenchMenu.MODE_VIEW) {
            title = "Mech Resupply";

            // @TODO ROAD UI
//            RenderSystem.setShaderTexture(0, TEXTURE_ROAD_CROSSHAIR);
//            RenderSystem.enableBlend();
//            guiGraphics.blit(
//                    TEXTURE_ROAD_CROSSHAIR,
//                    -15,30,
//                    GUI_WIDTH / 2,GUI_WIDTH / 2,
//                    0F,0F,
//                    640,647,
//                    640,647
//            );
//            RenderSystem.disableBlend();
        } else {
            title = "Mech Assemble";
        }

        guiGraphics.drawString(
                font,
                title,
                panelX,
                topPos + 15,
                0xFFFFFFFF,
                false
        );

//        RenderSystem.setShaderTexture(0, TEXTURE_WINDOW_TITLE);
//        guiGraphics.blit(
//                TEXTURE_WINDOW_TITLE,
//                0,
//                0,
//                0,
//                0,
//                176,
//                80,
//                176,
//                80
//        );

        renderCircuitSlotHighlights(guiGraphics);

        guiGraphics.pose().popPose();
    }

    private void renderCircuitSlotHighlights(
            GuiGraphics guiGraphics
    ) {
        ItemStack carried =
                this.menu.getCarried();

        if (!(carried.getItem() instanceof CircuitItem)) {
            return;
        }

        CircuitPrefix carriedPrefix =
                CircuitItemStackHelper.getPrefixOrDefault(carried);

        for (Slot slot : this.menu.slots) {
            if (!(slot instanceof MechWorkbenchMenu.MechCircuitSlot circuitSlot)) {
                continue;
            }

            if (circuitSlot.isDisablePlace() || !circuitSlot.acceptsCircuitPrefix(carriedPrefix)) {
                continue;
            }

            /*
             * 空スロットだけ光らせたい場合。
             * 既に埋まっているスロットも「対応スロット」として光らせたいなら、
             * このifは消してOK。
             */
            if (slot.hasItem()) {
                continue;
            }

            int x =
                    this.leftPos + slot.x;

            int y =
                    this.topPos + slot.y;

            renderSlotHighlight(
                    guiGraphics,
                    x,
                    y
            );
        }
    }

    private static void renderSlotHighlight(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {
        /*
         * ARGB
         * 0x80 = 半透明
         * 0x55FFFF00 = 黄色寄り
         */
        int fillColor =
                0x550000FF;

        int borderColor =
                0xCC5555FF;

        /*
         * スロット内側
         */
        guiGraphics.fill(
                x,
                y,
                x + 16,
                y + 16,
                fillColor
        );

        /*
         * 枠線
         */
        guiGraphics.fill(x - 1, y - 1, x + 17, y, borderColor);
        guiGraphics.fill(x - 1, y + 16, x + 17, y + 17, borderColor);
        guiGraphics.fill(x - 1, y, x, y + 16, borderColor);
        guiGraphics.fill(x + 16, y, x + 17, y + 16, borderColor);
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

    // =========================================================
    // Render
    // =========================================================

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick) {

        if (this.minecraft != null && this.minecraft.level != null && this.mech == null) {
            for (Entity entity : this.minecraft.level.entitiesForRendering()) {
                if (entity instanceof Pmvc01Entity m) {
                    if ((short)(entity.getUUID().hashCode()) == this.menu.getEntityId()) {
                        this.mech = m;
                        this.rebuildVisibleParts();
                    }
                }
            }
        }

        if (mech == null) {
            return;
        }

        renderBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // =====================================================
        // Mech 3D Model
        // =====================================================

        if (!circuitInvActive) {
            renderMech3DModel(guiGraphics);
        }

        // =====================================================
        // Main Panel
        // =====================================================

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);

        // @TODO ROAD UI
//        if (menu.getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
            renderPartsSelectPanel(guiGraphics, mouseX, mouseY);
//        }

        // =====================================================
        // Inventory
        // =====================================================


        if (true) {
            renderConsumablePanel(
                    guiGraphics,
                    panelX,
                    panelY + panelHeight + slotSize + 4
            );
        } else {
            renderConsumablePanel(
                    guiGraphics,
                    playerInvX + 1,
                    topPos + 12
            );
        }

        renderCargoPanel(guiGraphics);

        renderPlayerInventoryPanel(guiGraphics);

        renderCircuitInventoryPanel(guiGraphics);

        guiGraphics.pose().popPose();

        // @TODO ROAD UI
//        if (menu.getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 300);


            renderSpecsPanel(guiGraphics);

            guiGraphics.pose().popPose();
//        }

        if (menu.getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
            nameField.render(
                    guiGraphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
        }

        if (!isDropdownOpen) {
            renderTooltip(guiGraphics, mouseX, mouseY);
//            renderCustomTooltip(guiGraphics, mouseX, mouseY);
        }

        if (isDropdownOpen) {
            renderDropdown(guiGraphics, mouseX, mouseY);
        }
    }

    // =========================================================
    // Render Parts Select Panel
    // =========================================================


    private void renderPartsSelectPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderBgTexture(
                TEXTURE_PANEL_PARTS_SELECTOR,
                panelX, panelY,
                panelWidth, panelHeight,
                0.3F,
                guiGraphics);

        renderTabs(
                guiGraphics,
                TOP_TABS,
                panelX,
                panelY + tabTopOffsetY,
                true
        );

        renderTabs(
                guiGraphics,
                BOTTOM_TABS,
                panelX,
                panelY + panelHeight + tabButtomOffsetY,
                false
        );

        renderPartList(guiGraphics, mouseX, mouseY);

        renderScrollbar(guiGraphics);
    }

    // =========================================================
    // Build Tabs
    // =========================================================

    private void rebuildVisibleParts() {

        visiblePartSlots.clear();

        for (int i = 0; i < menu.slots.size(); i++) {

            Slot slot = menu.getSlot(i);

            ItemStack stack = slot.getItem();

            if (stack.isEmpty()) continue;

            if (matchesCurrentTab(stack)) {
                visiblePartSlots.add(i);
            }
        }

        visiblePartSlots.add(Integer.MIN_VALUE);

        updateScrollLimits();
    }

    private boolean matchesCurrentTab(ItemStack stack) {
        Item item = stack.getItem();

        return switch (currentTab) {
            case HEAD ->
                    item instanceof BasePartsItem.Head;
            case BODY ->
                    item instanceof BasePartsItem.Body;
            case ARM ->
                    item instanceof BasePartsItem.Arm;
            case LEGS ->
                    item instanceof BasePartsItem.Legs;
            case GENERATOR ->
                    item instanceof BasePartsItem.Generator;
            case BOOSTER ->
                    item instanceof BasePartsItem.Booster;
            case RIGHT_ARM, LEFT_ARM ->
                    item instanceof BasePartsItem.WeaponArm;
            case RIGHT_SHOULDER, LEFT_SHOULDER ->
                    item instanceof BasePartsItem.WeaponShoulder;
            case EXT1, EXT2 ->
                    item instanceof BasePartsItem.Extension;
        };
    }

    private int contentHeight;

    private void updateScrollLimits() {

        int rows = (int)Math.ceil(
                visiblePartSlots.size() / (double)columns
        );

        if (rows <= 0) {
            maxScroll = 0;
            contentHeight = 0;
            return;
        }

        contentHeight =
                rows * cardHeight
                        + (rows - 1) * cardSpacingY;

        maxScroll = Math.max(
                0,
                contentHeight - panelHeight
        );
    }

    // =========================================================
    // Render Parts Select Tabs
    // =========================================================

    private void renderTabs(
            GuiGraphics guiGraphics,
            PartsTab[] tabs,
            int startX,
            int y,
            boolean top) {
        ResourceLocation sel = top? TEXTURE_TAB_SELECTED_TOP: TEXTURE_TAB_SELECTED_BOTTOM;
        ResourceLocation unsel = top? TEXTURE_TAB_UNSELECTED_TOP: TEXTURE_TAB_UNSELECTED_BOTTOM;

        for (int i = 0; i < tabs.length; i++) {

            PartsTab tab = tabs[i];

            int x = startX + i * slotSize;

            boolean selected = currentTab == tab;

            ResourceLocation bg;
            if (selected) {
                bg = sel;
            } else {
                bg = unsel;
            }


            int topAdj = top?-3:0;

            renderBgTexture(
                    bg,
                    x,
                    y + topAdj,
                    slotSize,
                    19,
                    0.3F,
                    guiGraphics);

            renderBgTexture(
                    tab.icon,
                    x + 1,
                    y + 1,
                    16,
                    16,
                    1.0F,
                    guiGraphics);
        }
    }

    private final int rowHeight = 22;

    private void renderPartList(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        int startX = panelX + 4;
        int startY = panelY + 4;

        guiGraphics.enableScissor(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight
        );

        ItemStack hoverItem = null;

        for (int index = 0; index < visiblePartSlots.size(); index++) {

            // ====================================
            // GRID
            // ====================================

            int col = index % columns;
            int row = index / columns;

            int x =
                    startX
                            + col * (cardWidth + cardSpacingX);

            int y =
                    startY
                            + row * (cardHeight + cardSpacingY)
                            - scrollOffset;

            // ====================================
            // CLIP
            // ====================================

            if (y + cardHeight < panelY) continue;

            if (y > panelY + panelHeight) continue;

            // ====================================
            // HOVER
            // ====================================

            boolean hover =
                    mouseX >= x
                            && mouseX < x + cardWidth
                            && mouseY >= y
                            && mouseY < y + cardHeight;

            // ====================================
            // CARD BG
            // ====================================

            renderBgTexture(
                    TEXTURE_CARD_PARTS_BG,
                    x,
                    y,
                    cardWidth,
                    cardHeight,
                    0.7F,
                    guiGraphics);

            if (currentTab.mechSlot == visiblePartSlots.get(index)) {
                renderBgTexture(
                        TEXTURE_CARD_PARTS_SELECTED_BG,
                        x,
                        y,
                        cardWidth,
                        cardHeight,
                        0.7F,
                        guiGraphics);
            }


            // ====================================
            // MODEL PREVIEW
            // ====================================

            var slotIndex = visiblePartSlots.get(index);

            ItemStack stack = ItemStack.EMPTY;
            if (slotIndex >= 0) {
                stack = menu.getSlot(slotIndex).getItem();
            }

            if (stack.isEmpty()) {
                guiGraphics.drawString(
                        font,
                        "Empty",
                        x + 10,
                        y + cardModelHeight / 2,
                        0xFFFFFFFF,
                        false
                );
            } else {
                renderPartPreview(
                        guiGraphics,
                        stack,
                        x + cardWidth / 2,
                        y + cardModelHeight / 2,
                        2
                );
            }

            // ====================================
            // NAME
            // ====================================

            if (stack.isEmpty()) {
                guiGraphics.drawString(
                        font,
                        "-",
                        x + 4,
                        y + cardModelHeight + 2,
                        0xFFFFFFFF,
                        false
                );

            } else {
                String name = stack.getHoverName().getString();

                if (name.contains("(")) {
                    name = name.substring(0, name.indexOf('('));
                }

                FormattedText seq =
                        font.substrByWidth(
                                Component.literal(name),
                                cardWidth - 4
                        );
                guiGraphics.drawString(
                        font,
                        seq.getString(),
                        x + 4,
                        y + cardModelHeight + 2,
                        0xFFFFFFFF,
                        false
                );
            }

            // ====================================
            // TOOLTIP
            // ====================================

            if (hover) {
                hoverItem = stack;
            }
        }

        guiGraphics.disableScissor();

        if (hoverItem != null
                && !hoverItem.isEmpty()
                && mouseInRange(
                        mouseX, mouseY,
                    panelX,
                    panelY,
                panelX + panelWidth,
                panelY + panelHeight)) {
            guiGraphics.renderTooltip(
                    font,
                    hoverItem,
                    mouseX,
                    mouseY
            );
        }

    }

    private boolean mouseInRange(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2
                && my >= y1 && my <= y2;
    }

    private void renderPartPreview(
            GuiGraphics guiGraphics,
            ItemStack stack,
            int x,
            int y,
            int scale
    ) {

        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(
                x,
                y,
                50
        );

//        guiGraphics.pose().scale(
//                scale,
//                scale,
//                scale
//        );

//        guiGraphics.renderItem(
//                stack,
//                -8,
//                -8
//        );

        renderParts(guiGraphics, stack);

        guiGraphics.pose().popPose();
    }

    private void renderParts(GuiGraphics guiGraphics, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();

        ItemRenderer itemRenderer = mc.getItemRenderer();

        PoseStack pose = guiGraphics.pose();

        pose.pushPose();

        if (stack.getItem() instanceof BasePartsItem.Weapon) {
        } else if (stack.getItem() instanceof BasePartsItem.Extension) {
        } else {
            pose.mulPose(Axis.XP.rotationDegrees(-25F));
            pose.mulPose(Axis.YP.rotationDegrees(45F));
        }

        pose.scale(36F, -36F, 36F);

        BakedModel model = itemRenderer.getModel(stack, null, null, 0);

        itemRenderer.render(
                stack,
                ItemDisplayContext.GUI,
                false,
                pose,
                guiGraphics.bufferSource(),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                model
        );

        pose.popPose();
    }


    private void renderPartList2(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY) {

        int startX = panelX + 6;
        int startY = panelY + 6;

        for (int i = 0; i < visiblePartSlots.size(); i++) {

            int y = startY + i * rowHeight - scrollOffset;

            if (y < panelY) continue;
            if (y > panelY + panelHeight - rowHeight) continue;

            Slot slot = menu.getSlot(visiblePartSlots.get(i));
            ItemStack stack = slot.getItem();

            boolean hover =
                    mouseX >= startX &&
                            mouseX < startX + panelWidth - 12 &&
                            mouseY >= y &&
                            mouseY < y + rowHeight;

            if (hover) {
                guiGraphics.renderTooltip(
                        font,
                        stack,
                        mouseX,
                        mouseY
                );
            }

            // Row BG
            boolean equippedNow = currentTab.mechSlot == visiblePartSlots.get(i);

            int bg;
            if (equippedNow) {
                bg = 0xAA44FF44;
            }
            else if (hover) {
                bg = 0x88FFFFFF;
            }
            else {
                bg = 0xAA222222;
            }

            guiGraphics.fill(
                    startX,
                    y,
                    startX + panelWidth - 12,
                    y + rowHeight,
                    bg
            );

            // Item Icon

            guiGraphics.renderItem(
                    stack,
                    startX + 3,
                    y + 3
            );

            // Name
            String name = stack.getHoverName().getString();

            if (name.contains("(")) {
                name = name.substring(0, name.indexOf('('));
            }

            guiGraphics.drawString(
                    font,
                    Component.literal(name),
                    startX + 24,
                    y + 7,
                    0xFFFFFFFF,
                    false
            );
        }
    }

    // =========================================================
    // Scrollbar
    // =========================================================
    private void renderScrollbar(
            GuiGraphics guiGraphics
    ) {

        if (maxScroll <= 0) {
            return;
        }

        int barX =
                panelX + panelWidth + 1;

        int barY =
                panelY + 4;

        int barHeight =
                panelHeight - 8;

        // =====================================
        // thumb size
        // =====================================

        float visibleRatio =
                panelHeight / (float)contentHeight;

        int thumbHeight = Math.max(
                16,
                (int)(barHeight * visibleRatio)
        );

        // =====================================
        // progress
        // =====================================

        float progress =
                scrollOffset / (float)maxScroll;

        int movable =
                barHeight - thumbHeight;

        int thumbY =
                barY + (int)(progress * movable);

        // =====================================
        // BG
        // =====================================

        renderBgTexture(
                TEXTURE_SCROLL_BAR_BG,
                panelX + panelWidth + 2,
                panelY,
                2,
                panelHeight,
                guiGraphics
        );

        // =====================================
        // Thumb
        // =====================================

        renderBgTexture(
                TEXTURE_SCROLL_BAR,
                barX,
                thumbY,
                4,
                thumbHeight,
                guiGraphics
        );
    }

    private void renderConsumablePanel(
            GuiGraphics guiGraphics,
            int startX,
            int y) {

        for (int i = 0; i < 6; i++) {
            int x = startX + i * slotSize;

            renderBgTexture(
                    TEXTURE_ITEM_CONTAINER,
                    x + 1,
                    y + 1,
                    16,
                    16,
                    guiGraphics);

            if (!menu.getSlot(12 + i).hasItem()) {
                ResourceLocation icon;

                if (i == 4) {
                    icon = TEXTURE_ICON_FUEL;
                } else if (i == 5) {
                    icon = TEXTURE_ICON_REPAIR;
                } else {
                    icon = TEXTURE_ICON_AMMO;
                }

                renderBgTexture(
                        icon,
                        x + 1,
                        y + 1,
                        16,
                        16,
                        1.0F,
                        guiGraphics);
            }
        }
    }

    private void renderCargoPanel(GuiGraphics guiGraphics) {
        renderBgTexture(TEXTURE_PANEL_INV_MECH, cargoX, cargoY, cargoWidth, cargoHeight, guiGraphics);

        // Title
        guiGraphics.drawString(
                font,
                "Mech Cargo",
                cargoX + 18,
                cargoY + 4,
                0xFFFFFFFF
        );
    }

    private void renderPlayerInventoryPanel(
            GuiGraphics guiGraphics) {
        renderBgTexture(TEXTURE_PANEL_INV_PLAYER, playerInvX, playerInvY, playerInvWidth, playerInvHeight, guiGraphics);

        // Title
        guiGraphics.drawString(
                font,
                "Player Inventory",
                playerInvX + 18,
                playerInvY + 4,
                0xFFFFFFFF
        );
    }

    private void renderCircuitInventoryPanel(GuiGraphics guiGraphics) {
        if (circuitInvActive) {
            renderBgTexture(TEXTURE_PANEL_CIRCUIT, specsX, cargoY, 180, 132, guiGraphics);

            guiGraphics.drawString(
                    font,
                    "Additional Circuits",
                    specsX + 18,
                    cargoY + 4,
                    0xFFFFFFFF
            );

            int offX = specsX + 22;
            int offY = cargoY + 3;
            int rowH = 19;

            guiGraphics.drawString(
                    font,
                    "- Balance",
                    offX,
                    offY + rowH,
                    0xFFFFFFFF
            );

            guiGraphics.drawString(
                    font,
                    "- Offence",
                    offX,
                    offY + rowH * 2,
                    0xFFFFFFFF
            );

            guiGraphics.drawString(
                    font,
                    "- Defence",
                    offX,
                    offY + rowH * 3,
                    0xFFFFFFFF
            );

            guiGraphics.drawString(
                    font,
                    "- Mobility",
                    offX,
                    offY + rowH * 4,
                    0xFFFFFFFF
            );

            guiGraphics.drawString(
                    font,
                    "- Energy",
                    offX,
                    offY + rowH * 5,
                    0xFFFFFFFF
            );

            guiGraphics.drawString(
                    font,
                    "- Utility",
                    offX,
                    offY + rowH * 6,
                    0xFFFFFFFF
            );
        }
    }

    private void renderSpecsPanel(
            GuiGraphics guiGraphics) {

        renderBgTexture(TEXTURE_PANEL_SPEC, specsX, specsY, specsWidth, specsHeight, guiGraphics);

        guiGraphics.drawString(
                font,
                "Specs",
                specsX + 18,
                specsY + 4,
                0xFFFFFFFF
        );

        int offsetX = specsX + 4;
        int offsetY = specsY + 20;

        drawParams(0, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.hp}",  (int)mech.getHealth() + "/" + mech.getDurability(), false, guiGraphics);
        drawParams(1, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.energy}", String.format("%d", mech.getMaxEnergy()) + "/" + String.format("%.1f", mech.getEnergyChargePerTick()), false, guiGraphics);
        drawParams(2, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.weight}", mech.getWeight() + "/" + mech.getMaxWeight(), mech.getWeight() > mech.getMaxWeight(), guiGraphics);
        drawParams(3, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.speed}", String.format("%.1f", mech.getSpeedModifier()), false, guiGraphics);
        drawParams(4, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.jump}", String.format("%.1f",mech.getJumpModifier()), false, guiGraphics);
        drawParams(5, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.boost}", String.format("%.1f", mech.getSpeedModifierEvasion()), false, guiGraphics);

    }

    protected void drawParams(int row, int offX, int offY, String paramName, String paramValue, boolean alert, GuiGraphics guiGraphics) {
        if (row > 2) {
            offX += specsWidth / 2 + 2;
        }

        int xoff1 = offX;
        int xoff2 = offX + specsWidth / 4;

        if (row <= 2) {
            xoff2 -= 16;
        }

        int yoff = offY;
        int color = alert ? 0xFF0000 : 0xFFFFFF;

        Component c = Utils.string2Component(paramName);

        guiGraphics.drawString(this.font, c.getString(), xoff1 + 1, yoff + 12 * (row % 3), color, false);
        guiGraphics.drawString(this.font, paramValue, xoff2 + 4, yoff + 12 * (row % 3), color, false);
    }

    // =========================================================
    // Mouse Click
    // =========================================================

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button) {

        if (!nameField.isMouseOver(
                mouseX,
                mouseY
        )) {
            nameField.setFocused(false);
        }
        // @TODO ROAD UI
//        if (menu.getMode() == MechWorkbenchMenu.MODE_VIEW) {
//            return super.mouseClicked(mouseX, mouseY, button);
//        }

        if (isDropdownOpen) {
            int x = this.leftPos + DROPDOWN_OFFSET_X;
            int startY = this.topPos + DROPDOWN_OFFSET_Y + DROPDOWN_HEIGHT;

            for (int i = 0; i < options.size(); i++) {
                int y = startY + (i * DROPDOWN_HEIGHT);

                if (mouseX >= x && mouseX <= x + DROPDOWN_WIDTH &&
                        mouseY >= y && mouseY <= y + DROPDOWN_HEIGHT) {

                    selectedOption = options.get(i);
                    textureColorButton.setMessage(Component.literal(selectedOption));

                    sendTextureColor2Server(i);

                    isDropdownOpen = false;
                    return true;
                }
            }

            // 外クリックで閉じる
            isDropdownOpen = false;
        }

        // =====================================================
        // TAB SETTINGS
        // =====================================================

        int tabSize = 18;
        int tabSpacing = 18;

        // =====================================================
        // TOP TABS
        // =====================================================

        int topTabX = panelX;
        int topTabY = panelY + tabTopOffsetY;

        for (int i = 0; i < TOP_TABS.length; i++) {

            PartsTab tab = TOP_TABS[i];

            int x = topTabX + i * tabSpacing;
            int y = topTabY;

            if (mouseX >= x &&
                    mouseX < x + tabSize &&
                    mouseY >= y &&
                    mouseY < y + tabSize) {

                currentTab = tab;

                scrollOffset = 0;

                rebuildVisibleParts();

                return true;
            }
        }

        // =====================================================
        // BOTTOM TABS
        // =====================================================

        int bottomTabX = panelX;
        int bottomTabY = panelY + panelHeight + tabButtomOffsetY;

        for (int i = 0; i < BOTTOM_TABS.length; i++) {

            PartsTab tab = BOTTOM_TABS[i];

            int x = bottomTabX + i * tabSpacing;
            int y = bottomTabY;

            if (mouseX >= x &&
                    mouseX < x + tabSize &&
                    mouseY >= y &&
                    mouseY < y + tabSize) {

                currentTab = tab;

                scrollOffset = 0;

                rebuildVisibleParts();

                return true;
            }
        }

        // =====================================================
        // PART LIST
        // =====================================================

        if (menu.getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
            int startX = panelX + 6;
            int startY = panelY + 6;

            for (int index = 0; index < visiblePartSlots.size(); index++) {

                int col = index % columns;
                int row = index / columns;

                int x =
                        startX
                                + col * (cardWidth + cardSpacingX);

                int y =
                        startY
                                + row * (cardHeight + cardSpacingY)
                                - scrollOffset;

                if (mouseX >= x
                        && mouseX < x + cardWidth
                        && mouseY >= y
                        && mouseY < y + cardHeight
                        && mouseInRange((int) mouseX, (int) mouseY, panelX, panelY, panelX + panelWidth, panelY + panelHeight)
                ) {

                    int idx = visiblePartSlots.get(index);

                    if (idx < 0) {
                        if (!hasInventorySpace()) {
                            return true;
                        }

                        sendServerUnequip();

                        return true;
                    } else {
                        sendServerEquip(idx);
                    }

                    return true;
                }
            }
        }

        // =====================================================
        // Mech 3D Model
        // =====================================================

        if (button == 0 && !circuitInvActive) {
            if (mouseX >= modelAreaLeft()
                    && mouseX <= modelAreaRight()
                    && mouseY >= modelAreaTop()
                    && mouseY <= modelAreaBottom()) {

                draggingModel = true;

                lastMouseX = mouseX;
                lastMouseY = mouseY;

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean hasInventorySpace() {

        Inventory inv = minecraft.player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {

            if (inv.getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {

        if (draggingModel) {

            float dx = (float)(mouseX - lastMouseX);
            float dy = (float)(mouseY - lastMouseY);

            modelYaw -= dx * 1.2f;

            modelPitch -= dy * 0.7f;

            modelPitch = Mth.clamp(
                    modelPitch,
                    -40f,
                    40f
            );

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {

        draggingModel = false;

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta) {

        scrollOffset -= (int)(delta * 12);

        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        return true;
    }

    private void renderMech3DModel(GuiGraphics guiGraphics) {
        if (this.mech != null) {
            int centerX =
                    modelAreaLeft()
                            + modelAreaWidth / 2;

            int centerY =
                    modelAreaTop()
                            + modelAreaHeight;

            this.mech.setShowCustomHealthBar(false);
            renderEntityInInventoryRotation(
                    guiGraphics,
                    centerX,
                    centerY,
                    modelScale,
                    modelYaw,
                    modelPitch,
                    this.mech
            );
            this.mech.setShowCustomHealthBar(true);
        }
    }

    public static void renderEntityInInventoryRotation(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int scale,
            float yaw,
            float pitch,
            LivingEntity entity
    ) {

        Quaternionf baseRot =
                new Quaternionf()
                        .rotateZ((float)Math.PI);

        Quaternionf pitchRot =
                new Quaternionf()
                        .rotateX(pitch * 0.017453292F);

        baseRot.mul(pitchRot);

        float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;

        entity.yBodyRot = 180.0F + yaw;
        entity.setYRot(180.0F + yaw);
        entity.setXRot(-pitch);

        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        renderEntityInInventory(
                guiGraphics,
                x,
                y,
                scale,
                baseRot,
                null,
                entity
        );

        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, int i, int j, int k, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((double)i, (double)j, 50);
        guiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)k, (float)k, (float)(-k)));
        guiGraphics.pose().mulPose(quaternionf);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880);
        });
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    private int modelAreaLeft() {
        return leftPos + modelAreaX;
    }

    private int modelAreaTop() {
        return topPos + modelAreaY;
    }

    private int modelAreaRight() {
        return modelAreaLeft() + modelAreaWidth;
    }

    private int modelAreaBottom() {
        return modelAreaTop() + modelAreaHeight;
    }

    private void renderDropdown(GuiGraphics g, int mouseX, int mouseY) {
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);

        int x = this.leftPos + DROPDOWN_OFFSET_X;
        int startY = this.topPos + DROPDOWN_OFFSET_Y + DROPDOWN_HEIGHT;

        for (int i = 0; i < options.size(); i++) {
            int y = startY + (i * DROPDOWN_HEIGHT);

            boolean hovered =
                    mouseX >= x && mouseX <= x + DROPDOWN_WIDTH &&
                            mouseY >= y && mouseY <= y + DROPDOWN_HEIGHT;

            int bgColor = hovered ? 0xFF666666 : 0xFF000000;

            // 背景
            g.fill(x, y, x + DROPDOWN_WIDTH, y + DROPDOWN_HEIGHT, bgColor);

            // テキスト
            g.drawString(this.font, options.get(i), x + 3, y + 4, 0xFFFFFF, false);
        }

        g.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
    }

    private void sendServerEquip(int sourceSlot) {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(menu.containerId);
            buf.writeInt(currentTab.mechSlot);
            buf.writeInt(sourceSlot);
            buf.writeInt(MechWorkbenchMenu.CMD_EQUIP);

            NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_MECH_CHANGE_PARTS), buf);
        }
    }

    private void sendServerUnequip() {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(menu.containerId);
            buf.writeInt(currentTab.mechSlot);
            buf.writeInt(0);
            buf.writeInt(MechWorkbenchMenu.CMD_UNEQUIP);

            NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_MECH_CHANGE_PARTS), buf);
        }
    }

    private void sendTextureColor2Server(int color) {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(mech.getUUID());
            buf.writeInt(color);
            NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_CHANGE_TEXTURE), buf);
        }
    }

    private void sendMechOperationAction(String message) {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(mech.getUUID());
            NetworkManager.sendToServer(PomkotsMechs.id(message), buf);
        }
    }

    private void sendMechName(String mechName) {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(mech.getUUID());
            buf.writeUtf(mechName);

            NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_CHANGE_CUSTOM_NAME), buf);
        }
    }

    private int lastInventoryRevision = -1;

    @Override
    protected void containerTick() {
        super.containerTick();

        int rev = menu.getInventoryRevision();

        if (rev != lastInventoryRevision) {

            lastInventoryRevision = rev;

            rebuildVisibleParts();
        }
    }

    // @TODO ROAD
    private void renderCustomTooltip(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY
    ) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {

            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);

            ItemStack itemStack = this.hoveredSlot.getItem();

            List<Component> lines =
                    Screen.getTooltipFromItem(
                            minecraft,
                            itemStack
                    );

            int padding = 4;

            int width = 0;

            for (Component line : lines) {

                width = Math.max(
                        width,
                        font.width(line)
                );
            }

            int lineHeight = 12;

            int height =
                    lines.size() * lineHeight
                            + padding * 2;

            // =====================================
            // Initial position
            // =====================================

            int x = mouseX + 12;
            int y = mouseY - 12;

            // =====================================
            // Clamp inside screen
            // =====================================

            int screenW = this.width;
            int screenH = this.height;

            // Right edge

            if (x + width + padding * 2 > screenW) {

                x =
                        screenW
                                - width
                                - padding * 2
                                - 4;
            }

            // Bottom edge

            if (y + height > screenH) {
                y =
                        screenH
                                - height
                                - 4;
            }

            // Left edge

            if (x < 4) {
                x = 4;
            }

            // Top edge

            if (y < 4) {
                y = 4;
            }

            // =====================================
            // Background
            // =====================================

            guiGraphics.fillGradient(
                    x - padding,
                    y - padding,
                    x + width + padding,
                    y + height,
                    0xEE101820,
                    0xEE202A38
            );

            guiGraphics.renderOutline(
                    x - padding,
                    y - padding,
                    width + padding * 2,
                    height + padding,
                    0xFF66CCFF
            );

            // =====================================
            // Text
            // =====================================

            for (int i = 0; i < lines.size(); i++) {

                guiGraphics.drawString(
                        font,
                        lines.get(i),
                        x,
                        y + i * lineHeight,
                        0xFFFFFFFF,
                        false
                );
            }

            guiGraphics.pose().popPose();
        }
    }

//    @Override
//    public boolean keyPressed(
//            int keyCode,
//            int scanCode,
//            int modifiers
//    ) {
//        if (nameField.isFocused()) {
//            return true;
//        }
//
//        return super.keyPressed(
//                keyCode,
//                scanCode,
//                modifiers
//        );
//    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (nameField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                nameField.setFocused(false);
                return true;
            }

            if (nameField.keyPressed(
                    keyCode,
                    scanCode,
                    modifiers
            )) {
                return true;
            }

            if (minecraft.options.keyInventory.matches(
                    keyCode,
                    scanCode
            )) {
                return true;
            }
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    @Override
    public boolean charTyped(
            char codePoint,
            int modifiers
    ) {
        if (nameField.isFocused()
                && nameField.charTyped(
                codePoint,
                modifiers
        )) {

            return true;
        }

        return super.charTyped(
                codePoint,
                modifiers
        );
    }
}
