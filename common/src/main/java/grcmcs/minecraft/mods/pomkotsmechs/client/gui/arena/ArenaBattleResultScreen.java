package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ArenaBattleResultScreen extends AbstractContainerScreen<ArenaBattleResultMenu> {

    // =========================================================
    // TEXTURES
    // =========================================================

    private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_hangar.png");
    private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_gradient.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_normal.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_REWARDS = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_arena_rewards.png");

    private static final ResourceLocation TEXTURE_OFFERS_PANEL = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_trader_offers.png");
    private static final ResourceLocation TEXTURE_CARD = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/arena_fighter_card.png");
    private static final ResourceLocation TEXTURE_CARD_SELECTED = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_trader_card_selected.png");
    private static final ResourceLocation TEXTURE_SCROLL_BAR_BG = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/scroll_bar_background.png");
    private static final ResourceLocation TEXTURE_SCROLL_BAR = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/scroll_bar.png");
    private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/item_container.png");

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

    private final ArenaMatchResultData.FighterResult challenger;
    private final ArenaMatchResultData.FighterResult opponent;

    public ArenaBattleResultScreen(
            ArenaBattleResultMenu menu,
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

        challenger = menu.getChallengerResult();
        opponent = menu.getOpponentResult();
    }

    @Override
    protected void init() {

        super.init();

        int offsetX = 20;
        int offsetY = 10;

        this.leftCardOffsetX = offsetX + 28;
        this.leftCardOffsetY = offsetY;
        this.rightCardOffsetX = offsetX + 28 + CARD_WIDTH + 28 + 28;
        this.rightCardOffsetY = offsetY;
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        renderBackground(
                guiGraphics
        );

        renderFighterCard(
                guiGraphics,
                challenger,
                leftCardOffsetX,
                leftCardOffsetY,
                true
        );

        renderFighterCard(
                guiGraphics,
                opponent,
                rightCardOffsetX,
                leftCardOffsetY,
                false
        );

        renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        renderBgTexture(
                TEXTURE_PANEL_INV_REWARDS,
                leftCardOffsetX,
                leftCardOffsetY + 10 + CARD_HEIGHT,
                164,
                94,
                guiGraphics
        );

        guiGraphics.drawString(
                font,
                "Rewards",
                leftCardOffsetX + 18,
                leftCardOffsetY + 10 + CARD_HEIGHT + 4,
                0xFFFFFFFF
        );

        int offsetX = leftCardOffsetX + 25 + 26;
        int offsetY = leftCardOffsetY + 10 + CARD_HEIGHT + 26;

        guiGraphics.drawString(
                font,
                "Match Reward",
                offsetX,
                offsetY,
                0xFFFFFFFF
        );

        guiGraphics.drawString(
                font,
                "Rank Reward",
                offsetX,
                offsetY + 18,
                0xFFFFFFFF
        );

        guiGraphics.drawString(
                font,
                "Special Reward",
                offsetX,
                offsetY + 36,
                0xFFFFFFFF
        );

        renderBgTexture(
                TEXTURE_PANEL_INV_PLAYER,
                rightCardOffsetX,
                leftCardOffsetY + 10 + CARD_HEIGHT,
                164,
                94,
                guiGraphics
        );

        guiGraphics.drawString(
                font,
                "Player Inventory",
                rightCardOffsetX + 18,
                leftCardOffsetY + 10 + CARD_HEIGHT + 4,
                0xFFFFFFFF
        );

    }

    private void renderHeader(
            GuiGraphics gg
    ) {

        String title;

        switch (challenger.getResultType()) {

            case ArenaMatchResultData.FighterResult.RESULT_WIN ->
                    title = "VICTORY";

            case ArenaMatchResultData.FighterResult.RESULT_LOSE ->
                    title = "DEFEAT";

            default ->
                    title = "DRAW";
        }

        int color =
                switch (challenger.getResultType()) {

                    case ArenaMatchResultData.FighterResult.RESULT_WIN ->
                            0x55FF55;

                    case ArenaMatchResultData.FighterResult.RESULT_LOSE ->
                            0xFF5555;

                    default ->
                            0xFFFF55;
                };

        gg.drawCenteredString(
                font,
                title,
                leftPos + imageWidth / 2,
                topPos + 10,
                color
        );
    }

    private void renderFighterCard(
            GuiGraphics gg,
            ArenaMatchResultData.FighterResult result,
            int x,
            int y,
            boolean challenger
    ) {
        gg.fill(
                x,
                y,
                x + CARD_WIDTH,
                y + CARD_HEIGHT,
                0x66000000
        );

        int line = y + 8;

        gg.drawCenteredString(
                font,
                result.getDisplayName(),
                x + CARD_WIDTH / 2,
                line,
                0xFFFFFF
        );

        line += 16;

        renderResultBadge(
                gg,
                result,
                x + 8,
                line
        );

        line += 18;

        drawChangeLine(
                gg,
                "Rating",
                result.getRatingBefore(),
                result.getRatingAfter(),
                x,
                line
        );

        line += 14;

        drawChangeLine(
                gg,
                "Wins",
                result.getWinsBefore(),
                result.getWinsAfter(),
                x,
                line
        );

        line += 14;

        drawChangeLine(
                gg,
                "Losses",
                result.getLossesBefore(),
                result.getLossesAfter(),
                x,
                line
        );

        line += 14;

        drawChangeLine(
                gg,
                "Draws",
                result.getDrawsBefore(),
                result.getDrawsAfter(),
                x,
                line
        );

        line += 18;

        gg.drawString(
                font,
                "Rank",
                x + 8,
                line,
                0xAAAAAA
        );

        gg.drawString(
                font,
                result.getRankBefore()
                        + " -> "
                        + result.getRankAfter(),
                x + 60,
                line,
                getRankColor(
                        result.getRankAfter()
                )
        );
    }

    private void renderResultBadge(
            GuiGraphics gg,
            ArenaMatchResultData.FighterResult result,
            int x,
            int y
    ) {

        String text;
        int color;

        switch (result.getResultType()) {

            case ArenaMatchResultData.FighterResult.RESULT_WIN -> {
                text = "WIN";
                color = 0x55FF55;
            }

            case ArenaMatchResultData.FighterResult.RESULT_LOSE -> {
                text = "LOSE";
                color = 0xFF5555;
            }

            default -> {
                text = "DRAW";
                color = 0xFFFF55;
            }
        }

        gg.drawString(
                font,
                text,
                x,
                y,
                color
        );
    }

    private void drawChangeLine(
            GuiGraphics gg,
            String label,
            int before,
            int after,
            int x,
            int y
    ) {

        gg.drawString(
                font,
                label,
                x + 8,
                y,
                0xAAAAAA
        );

        int color;

        if (after > before) {
            color = 0x55FF55;
        }
        else if (after < before) {
            color = 0xFF5555;
        }
        else {
            color = 0xFFFFFF;
        }

        gg.drawString(
                font,
                before + " -> " + after,
                x + 60,
                y,
                color
        );
    }

    private int getRankColor(
            String rank
    ) {
        var r = ArenaRank.getRank(rank);
        return r.getColor();
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
