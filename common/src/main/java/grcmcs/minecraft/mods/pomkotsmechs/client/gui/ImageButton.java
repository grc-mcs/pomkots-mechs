package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class ImageButton extends Button {

    private final ResourceLocation texture;

    private final int texU;
    private final int texV;

    private final int texWidth;
    private final int texHeight;
    private final Component label;

    public ImageButton(
            int x,
            int y,
            int width,
            int height,
            ResourceLocation texture,
            int texU,
            int texV,
            int texWidth,
            int texHeight,

            Component label,

            OnPress onPress
    ) {
        super(
                x,
                y,
                width,
                height,
                label,
                onPress,
                DEFAULT_NARRATION
        );

        this.texture = texture;

        this.texU = texU;
        this.texV = texV;

        this.texWidth = texWidth;
        this.texHeight = texHeight;

        this.label = label;
    }

    @Override
    protected void renderWidget(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        RenderSystem.enableBlend();

        boolean pressed =
                isHovered
                        && GLFW.glfwGetMouseButton(
                        Minecraft.getInstance()
                                .getWindow()
                                .getWindow(),
                        GLFW.GLFW_MOUSE_BUTTON_LEFT
                ) == GLFW.GLFW_PRESS;

        int v = texV;

        if (!this.active) {
            v += height * 3;
        } else if (pressed) {
            v += height * 2;
        } else if (isHovered) {
            v += height;
        }

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                0.9F
        );
        guiGraphics.blit(
                texture,
                getX(),
                getY(),
                texU,
                v,
                width,
                height,
                texWidth,
                texHeight
        );
        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        // =========================
        // Text
        // =========================

        Font font = Minecraft.getInstance().font;

        int textWidth =
                font.width(label);

        int textX =
                getX()
                        + (width / 2)
                        - (textWidth / 2);

        int textY =
                getY()
                        + (height - 8) / 2;

        int color = 0xFFFFFFFF;

        if (!this.active) {
            color = 0xFF222222;

        } else if (pressed) {
            // 押下時に少しずらす
            textX += 1;
            textY += 1;
        }

        guiGraphics.drawString(
                font,
                label,
                textX,
                textY,
                color,
                false
        );

        renderBadge(guiGraphics);
    }

    private void renderBadge(
            GuiGraphics graphics
    ) {
        if (badgeCount <= 0) {
            return;
        }

        int badgeX =
                getX()
                        + width
                        - 10;

        int badgeY =
                getY()
                        - 2;

        graphics.fill(
                badgeX,
                badgeY,
                badgeX + 10,
                badgeY + 10,
                0xFFFF0000
        );

        String text =
                badgeCount > 99
                        ? "!"
                        : String.valueOf(
                        badgeCount
                );

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                text,
                badgeX + 5,
                badgeY + 1,
                0xFFFFFFFF
        );
    }

    private int badgeCount = 0;

    public void setBadgeCount(
            int badgeCount
    ) {
        this.badgeCount = badgeCount;
    }

    public int getBadgeCount() {
        return badgeCount;
    }
}
