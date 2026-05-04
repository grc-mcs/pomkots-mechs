package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class IntroNarrationScreen extends Screen {
    private static final List<String> NARRATION = List.of(
            "──遠い遠い未来。けれど、どこか身近な星で──",
            "",
            "人類はかつて、宇宙の果てへと手を伸ばし、",
            "飽くなき欲望と無限の技術で、星々の時代を築いていった",
            "",
            "しかし、どれほど文明が進化しようと、争いが無くなる事はなかった",
            "理解し合えるはずの人々は、より深く傷つけ合い、",
            "そして、終わりの日が訪れた",
            "国家は滅び、企業は崩壊し、大地は焼かれた",
            "",
            "それから百年が経ち、緑が廃墟を覆いつくした頃",
            "遺された無人兵器は今なお稼働し、人類を“敵”として行動している",
            "人々はその影に怯えながら、それでも強かに生き延びていた",
            "",
            "そして今日、ひとりの若者がシェルターで成人を迎える"
    );

    private int tickCounter = 0;
    private int lineIndex = 0;
    private int alpha = 0;

    private static final ResourceLocation[] BACKGROUNDS = new ResourceLocation[]{
            new ResourceLocation("mymod:textures/gui/intro/scene1.png"),
            new ResourceLocation("mymod:textures/gui/intro/scene2.png"),
            // …順次
    };

    public IntroNarrationScreen() {
        super(Component.empty());
    }

    @Override
    public void tick() {
        tickCounter++;

        if (tickCounter < 20) {
            if (alpha < 255) alpha += 10;
        } else if (tickCounter >= 100 && tickCounter < 120) {
            if (alpha >= 10) alpha -= 10;
        } else if (tickCounter >= 120){
            nextLine();
        }
    }

    private void nextLine() {
        alpha = 0;
        tickCounter = 0;
        lineIndex++;
        if (lineIndex >= NARRATION.size()) {
            this.minecraft.setScreen(null); // 閉じる
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        // 背景黒
        gfx.fill(0, 0, this.width, this.height, 0xFF000000);
        // 背景画像
        if (lineIndex / 4 < BACKGROUNDS.length)
            gfx.blit(BACKGROUNDS[lineIndex / 4], 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        // テキスト
        if (lineIndex < NARRATION.size() && alpha != 0) {
            int color = ((int)(alpha) << 24) | 0xFFFFFF;
            gfx.drawCenteredString(this.font, NARRATION.get(lineIndex), this.width / 2, this.height / 2, color);
        }
        super.render(gfx, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
