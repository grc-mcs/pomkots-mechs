package grcmcs.minecraft.mods.pomkotsmechs.client.gui.narration;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class Cut {
    public abstract void tick(IntroNarrationScreen screen);
    public abstract void render(GuiGraphics gfx, IntroNarrationScreen screen, Font font, float partialTicks);
    public abstract boolean isFinished();

    public static class ImageCut extends Cut {
        private final ResourceLocation background;
        private final List<String> lines;
        private final int fadeInTicks;
        private final int stayTicks;
        private final int fadeOutTicks;

        private int tick = 0;
        private int lineIndex = 0;
        private float alpha = 0f;
        private boolean fadingIn = true;
        private boolean fadingOut = false;

        public ImageCut(String backgroundPath, List<String> lines, int fadeIn, int stay, int fadeOut) {
            if (backgroundPath != null) {
                this.background = new ResourceLocation(backgroundPath);
            } else {
                this.background = null;
            }
            this.lines = lines;
            this.fadeInTicks = fadeIn;
            this.stayTicks = stay;
            this.fadeOutTicks = fadeOut;
        }

        @Override
        public void tick(IntroNarrationScreen screen) {
            tick++;

            if (fadingIn) {
                alpha += 1f / fadeInTicks;
                if (alpha >= 1f) {
                    alpha = 1f;
                    fadingIn = false;
                    tick = 0;
                }
            } else {
                // stay → fadeOut
                if (tick >= stayTicks) {
                    alpha -= 1f / fadeOutTicks;
                    fadingOut = true;
                    if (alpha <= 0f) {
                        alpha = 0f;
                        nextLine();
                    }
                }
            }
        }

        private void nextLine() {
            tick = 0;
            fadingIn = true;
            fadingOut = false;
            lineIndex++;
        }

        private static final int IMG_WIDTH = 571;
        private static final int IMG_HEIGHT = 300;

        private boolean shouldFadeImage() {
            return (lineIndex == 0 && fadingIn) || (lineIndex == lines.size() - 1 && fadingOut);
        }

        @Override
        public void render(GuiGraphics gfx, IntroNarrationScreen screen, Font font, float partialTicks) {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            if (background != null) {

                // 背景画像をスケーリングして画面に合わせる
                if (shouldFadeImage()) {
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                }

                gfx.blit(
                        background,
                        0, 0,                       // 描画開始位置
                        0.0f, 0.0f,                 // UV
                        screenWidth, screenHeight,  // 表示サイズ
                        screenWidth, screenHeight   // 元テクスチャサイズ（比率合わせにする）
                );

                if (shouldFadeImage()) {
                    RenderSystem.disableBlend();
                }

                if (lineIndex < lines.size() && alpha != 0) {
                    int color = ((int)(alpha * 255) << 24) | 0xFFFFFF;
                    gfx.drawCenteredString(font, getLocalizedString(lines.get(lineIndex)), screen.width / 2, screen.height / 2, color);
                }
            } else {
                if (lineIndex < lines.size() && alpha != 0) {
                    int color = ((int)(alpha * 255) << 24) | 0xFFFFFF;
                    gfx.drawCenteredString(font, getLocalizedString(lines.get(lineIndex)), screen.width / 2, screen.height / 2, color);
                }
            }
        }

        private String getLocalizedString(String id) {
            if (id == null || id.isEmpty()) {
                return "";
            }

            return Utils.string2Component(id).getString();
        }

        @Override
        public boolean isFinished() {
            return lineIndex >= lines.size();
        }
    }

    public static class DarkCut extends Cut {
        private final int duration;
        private int tick = 0;

        public DarkCut(int duration) {
            this.duration = duration;
        }

        @Override
        public void tick(IntroNarrationScreen screen) {
            tick++;
        }

        @Override
        public void render(GuiGraphics gfx, IntroNarrationScreen screen, Font font, float partialTicks) {
            gfx.fill(0, 0, screen.width, screen.height, 0xFF000000);
        }

        @Override
        public boolean isFinished() {
            return tick >= duration;
        }
    }
}

