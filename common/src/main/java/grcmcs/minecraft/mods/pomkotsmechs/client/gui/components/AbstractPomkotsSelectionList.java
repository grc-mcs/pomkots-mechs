package grcmcs.minecraft.mods.pomkotsmechs.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPomkotsSelectionList<T extends ObjectSelectionList.Entry<T>>
        extends ObjectSelectionList<T> {
    public static final ResourceLocation TEXTURE_LIST_BG_1 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/arena_fighter_card.png");;
    public static final ResourceLocation TEXTURE_LIST_SELECTION = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/arena_fighter_card_selected.png");;;
    public static final ResourceLocation TEXTURE_SCROLLBAR_BG = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/scroll_bar_background.png");;;
    public static final ResourceLocation TEXTURE_SCROLLBAR_THUMB = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/scroll_bar.png");;;

    private static final int SCROLLBAR_WIDTH = 2;
    private static final int THUMB_MIN_HEIGHT = 16;

    protected final AbstractContainerScreen<?> screen;

    protected final int listX;
    protected final int listY;

    protected final int listWidth;
    protected final int listHeight;

    protected T hovered;

    protected AbstractPomkotsSelectionList(
            AbstractContainerScreen<?> screen,
            Minecraft minecraft,
            int listX,
            int listY,
            int listWidth,
            int listHeight,
            int rowHeight
    ) {
        super(
                minecraft,
                listWidth,
                listHeight,
                listY,
                listY + listHeight,
                rowHeight
        );

        this.screen = screen;

        this.listX = listX;
        this.listY = listY;
        this.listWidth = listWidth;
        this.listHeight = listHeight;

        this.setLeftPos(listX);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(guiGraphics);
        this.hovered = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPosition((double)mouseX, (double)mouseY) : null;

        this.enableScissor(guiGraphics);

        this.renderList(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();

        this.renderDecorations(guiGraphics, mouseX, mouseY);
        RenderSystem.disableBlend();


        renderCustomScrollbar(
                guiGraphics
        );
    }

    protected T getHovered() {
        return this.hovered;
    }

    private void renderCustomScrollbar(
            GuiGraphics graphics
    ) {
        if (!hasScrollbar()) {
            return;
        }

        int barX =
                getScrollbarPosition();

        int barY =
                listY;

        int barHeight =
                listHeight;

        renderBgTexture(
                TEXTURE_SCROLLBAR_BG,
                barX,
                barY,
                SCROLLBAR_WIDTH,
                barHeight,
                graphics
        );

        int maxScroll =
                getMaxScroll();

        if (maxScroll <= 0) {
            return;
        }

        int thumbHeight =
                Math.max(
                        THUMB_MIN_HEIGHT,
                        (int)(
                                (float) barHeight
                                        * barHeight
                                        / getMaxPosition()
                        )
                );

        int thumbY =
                (int)(
                        getScrollAmount()
                                * (barHeight - thumbHeight)
                                / maxScroll
                );

        renderBgTexture(
                TEXTURE_SCROLLBAR_THUMB,
                barX - 1,
                barY + thumbY,
                SCROLLBAR_WIDTH + 2,
                thumbHeight,
                graphics
        );
    }

    private boolean hasScrollbar() {
        return getMaxScroll() > 0;
    }

    @Override
    protected void renderSelection(
            GuiGraphics graphics,
            int top,
            int width,
            int height,
            int outerColor,
            int innerColor
    ) {
        renderBgTexture(
                TEXTURE_LIST_SELECTION,
                getRowLeft(),
                top,
                getRowWidth(),
                height,
                graphics
        );
    }

    @Override
    public int getRowLeft() {
        return listX;
    }

    @Override
    public int getRowWidth() {
        return listWidth;
    }

    @Override
    protected int getScrollbarPosition() {
        return getRowLeft()
                + listWidth
                - SCROLLBAR_WIDTH;
    }

    public static abstract class PomkotsEntry<E extends PomkotsEntry<E>>
            extends ObjectSelectionList.Entry<E> {
        public PomkotsEntry(
        ) {
        }

        @Override
        public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {

        }

        @Override
        public void renderBack(
                GuiGraphics graphics,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovered,
                float partialTick
        ) {
            renderBgTexture(
                    TEXTURE_LIST_BG_1,
                    left,
                    top,
                    width,
                    height,
                    graphics
            );
        }

        @Override
        public Component getNarration() {
            return Component.literal("");
        }
    }


    private static void renderBgTexture(
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

    private static void renderBgTexture(
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
