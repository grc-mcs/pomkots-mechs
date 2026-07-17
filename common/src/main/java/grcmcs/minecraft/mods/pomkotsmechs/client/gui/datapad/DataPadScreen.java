package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DataPadScreen
        extends AbstractContainerScreen<DataPadMenu> {
    // =========================================================
    // TEXTURES
    // =========================================================

   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_large.png");

    // =========================================================
    // Component Settings
    // =========================================================

    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 270;

    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 28;

    private int menuButtonsOffsetX = 0;
    private int menuButtonsOffsetY = 0;

    private ImageButton btnMech;
    private ImageButton btnMechKeycards;
    private ImageButton btnArena;
    private ImageButton btnRadar;
    private ImageButton btnQuests;
    private ImageButton btnTeams;

    private boolean isSlotInited = false;

    public DataPadScreen(
            DataPadMenu menu,
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

        this.menuButtonsOffsetX = leftPos + 100;
        this.menuButtonsOffsetY = topPos + 75;

        btnMech = addRenderableWidget(
            buildMenuButton(
                    "Mech",
                    menuButtonsOffsetX,
                    menuButtonsOffsetY,
                    btn -> {
                        this.btnClicked(0);
                    },
                    (menu.player != null && menu.player.getVehicle() instanceof Pmvc01Entity)
            )
        );

        btnMechKeycards = addRenderableWidget(
                buildMenuButton(
                        "Mech Key Cards",
                        menuButtonsOffsetX + BUTTON_WIDTH + 20,
                        menuButtonsOffsetY,
                        btn -> {
                            this.btnClicked(1);
                        },
                        true
                )
        );

        btnArena = addRenderableWidget(
                buildMenuButton(
                        "Arena",
                        menuButtonsOffsetX,
                        menuButtonsOffsetY + (BUTTON_HEIGHT + 10),
                        btn -> {
                            this.btnClicked(2);
                        },
                        true
                )
        );

        btnRadar = addRenderableWidget(
                buildMenuButton(
                        "Radar",
                        menuButtonsOffsetX + BUTTON_WIDTH + 20,
                        menuButtonsOffsetY + (BUTTON_HEIGHT + 10),
                        btn -> {
                            this.btnClicked(3);
                        },
                        true
                )
        );

        btnQuests = addRenderableWidget(
                buildMenuButton(
                        "Quests",
                        menuButtonsOffsetX,
                        menuButtonsOffsetY + (BUTTON_HEIGHT + 10) * 2 + 10,
                        btn -> {
                            this.btnClicked(4);
                        },
                        isFtbQuestLoaded()
                )
        );

        btnTeams =addRenderableWidget(
                buildMenuButton(
                        "Teams",
                        menuButtonsOffsetX + BUTTON_WIDTH + 20,
                        menuButtonsOffsetY + (BUTTON_HEIGHT + 10) * 2 + 10,
                        btn -> {
                            this.btnClicked(5);
                        },
                        isFtbTeamsLoaded()
                )
        );
    }

    private boolean isFtbQuestLoaded() {
        return Platform.isModLoaded("ftbquests");
    }

    private boolean isFtbTeamsLoaded() {
//        return Platform.isModLoaded("ftbteams");
        return false;
    }

    private ImageButton buildMenuButton(String text, int x, int y, Button.OnPress onPress, boolean active) {
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

    private void btnClicked(int btnId) {
        minecraft.gameMode
                .handleInventoryButtonClick(
                        menu.containerId,
                        btnId
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

        if (!isSlotInited && menu.getDataPad() != null && menu.getDataPad().getItem() instanceof PomkotsDatapadItem) {
            isSlotInited = true;

            if (PomkotsDatapadItem.getRegisteredArenaIds(menu.getDataPad()).isEmpty()) {
                btnArena.active = false;
            } else {
                btnArena.setBadgeCount(PomkotsDatapadItem.getArenaRequests(menu.getDataPad()).size());
            }
        }

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
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
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
