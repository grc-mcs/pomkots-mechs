package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MechSalvagerScreen extends AbstractContainerScreen<MechSalvagerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechsalvager_bg.png");

    private Button summonButton;

    public MechSalvagerScreen(MechSalvagerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;

        this.titleLabelX = 44;
        this.inventoryLabelY = this.imageHeight - 99;
    }

    @Override
    protected void init() {
        super.init();

        summonButton = this.addRenderableWidget(Button.builder(
                Utils.string2Component("{text.pomkotsmechs.gui.mechsalvager.summon}"),
                btn -> sendSecurityAction(PomkotsMechs.PACKET_SUMMON_MECH)
        ).bounds(this.leftPos + 100, this.topPos + 38, 70, 14).build());
    }

    private void sendSecurityAction(String message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(0);
        NetworkManager.sendToServer(PomkotsMechs.id(message), buf);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        gfx.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);
    }
}
