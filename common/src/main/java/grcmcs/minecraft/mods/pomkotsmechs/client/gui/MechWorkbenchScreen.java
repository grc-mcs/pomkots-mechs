package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MechWorkbenchScreen extends AbstractContainerScreen<MechWorkbenchMenu> {
    private static final ResourceLocation V_GUI_TEXTURE = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench_bg.png");

    private float xMouse;
    private float yMouse;

    public MechWorkbenchScreen(MechWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 256;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private Button selectButton;
    private boolean isDropdownOpen = false;
    private List<Button> optionButtons = new ArrayList<>();
    private List<String> options = BasePartsItemModel.BASE_COLORS;
    private String selectedOption = "Color";

    @Override
    protected void init() {
        super.init();

        // メインの選択ボタン
        this.selectButton = addRenderableWidget(
                Button.builder(Component.literal(selectedOption), button -> toggleDropdown()).pos(this.leftPos + 114, this.topPos + 3).size(50, 15).build());

        // 選択肢のボタン
        for (int i = 0; i < options.size(); i++) {
            int y = this.topPos + 3 + 15 + (i * 15);
            Button optionButton = Button.builder(Component.literal(options.get(i)), button -> selectOption(button)).pos(this.leftPos + 114, y).size(50, 15).build();
            optionButton.visible = false; // 初期状態では非表示
            optionButtons.add(optionButton);
            addRenderableWidget(optionButton);
        }
    }

    private void toggleDropdown() {
        isDropdownOpen = !isDropdownOpen;
        for (Button button : optionButtons) {
            button.visible = isDropdownOpen;
        }
    }

    private void selectOption(Button button) {
        this.selectedOption = button.getMessage().getString();
        this.selectButton.setMessage(Component.literal(selectedOption));

        sendTextureColor2Server(BasePartsItemModel.getColorIndex(button.getMessage().getString()));

        toggleDropdown();
    }

    private void sendTextureColor2Server(int color) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(menu.getEntityId());
        buf.writeInt(color);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_CHANGE_TEXTURE), buf);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int offsetX = (this.width - this.imageWidth) / 2;
        int offsetY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(V_GUI_TEXTURE, offsetX, offsetY, 0, 0, this.imageWidth, this.imageHeight);

        ItemStack carried = this.menu.getCarried();
        for (int slot = 0; slot < 18; slot++) {
            var pos = MechWorkbenchMenu.getItemPosition(slot, offsetX + 7, offsetY + 105);
            var s = this.menu.getSlot(slot);

            if (s.hasItem()) {
                if (s.getItem().getItem() instanceof BasePartsItem.Magazine mag) {
                    var weaponSlot = this.menu.getSlot(slot - 6);
                    if (weaponSlot.hasItem() && weaponSlot.getItem().getItem() instanceof BasePartsItem.Weapon w && !w.isMatchAmmo(mag)) {
                        guiGraphics.fill((int)pos.x + 2, (int)pos.y + 2, (int)pos.x + 16, (int)pos.y + 16,
                                0xFFAA0000);
                    } else {
                        guiGraphics.fill((int)pos.x + 2, (int)pos.y + 2, (int)pos.x + 16, (int)pos.y + 16,
                                0xFFAEC1D6);
                    }
                } else {
                    guiGraphics.fill((int)pos.x + 2, (int)pos.y + 2, (int)pos.x + 16, (int)pos.y + 16,
                            0xFFAEC1D6);
                }
            }

            if (s.mayPlace(carried)) {
                if (carried.getItem() instanceof BasePartsItem.Magazine mag) {
                    var weaponSlot = this.menu.getSlot(slot - 6);
                    if (weaponSlot.hasItem() && weaponSlot.getItem().getItem() instanceof BasePartsItem.Weapon w && w.isMatchAmmo(mag)) {
                        drawRectangle((int)pos.x, (int)pos.y, (int)pos.x + 16, (int)pos.y + 16, 0xFF1B43B5, guiGraphics);
                    }
                } else {
                    drawRectangle((int)pos.x, (int)pos.y, (int)pos.x + 16, (int)pos.y + 16, 0xFF1B43B5, guiGraphics);
                }
            }
        }

        drawTexts(offsetX, offsetY, guiGraphics);

        if (this.minecraft != null && this.minecraft.level != null) {
            var entity = this.minecraft.level.getEntity(this.menu.getEntityId());
            if (entity instanceof Pmvc01Entity mech) {
                InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, offsetX + 42, offsetY + 82, 10, offsetX + 42 - this.xMouse, offsetY + 82 - this.yMouse, mech);

                drawParams(0, offsetX, offsetY, "Health", mech.getDurability() + "", false, guiGraphics);
                drawParams(1, offsetX, offsetY, "Weight", mech.getWeight() + "/" + mech.getMaxWeight(), mech.getWeight() > mech.getMaxWeight(), guiGraphics);
                drawParams(2, offsetX, offsetY, "Speed", String.format("%.1f", mech.getSpeedModifier()) + "/" + String.format("%.1f",mech.getJumpModifier()), false, guiGraphics);
                drawParams(3, offsetX, offsetY, "Boost", String.format("%.1f", mech.getSpeedModifierEvasion()) + "/" + String.format("%.1f", mech.getSpeedModifierVertical()), false, guiGraphics);
                drawParams(4, offsetX, offsetY, "EN", String.format("%d", mech.getMaxEnergy()) + "/" + String.format("%d", mech.getEnergyChargePerTick()), false, guiGraphics);
            }
        }
    }

    protected void drawRectangle(int x1, int y1, int x2, int y2, int color, GuiGraphics guiGraphics) {
        guiGraphics.hLine(x1 + 1, x2 - 1, y1 + 1, color);
        guiGraphics.hLine(x1 + 1, x2 - 1, y1 + 2, color);

        guiGraphics.hLine(x1 + 1, x2 - 1, y2 - 1, color);
        guiGraphics.hLine(x1 + 1, x2 - 1, y2 - 2, color);

        guiGraphics.vLine(x1 + 1, y1 + 1, y2 - 1, color);
        guiGraphics.vLine(x1 + 2, y1 + 1, y2 - 1, color);

        guiGraphics.vLine(x2 - 1, y1 + 1, y2 - 1, color);
        guiGraphics.vLine(x2 - 2, y1 + 1, y2 - 1, color);
    }

    protected void drawTexts(int offX, int offY, GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, "Parts", offX + 118, offY + 110, 0x000000, false);
        guiGraphics.drawString(this.font, "Weapons", offX + 118, offY + 128, 0x000000, false);
        guiGraphics.drawString(this.font, "Ammo/Fuel", offX + 118, offY + 146, 0x000000, false);
    }

    protected void drawParams(int row, int offX, int offY, String paramName, String paramValue, boolean alert, GuiGraphics guiGraphics) {
        int xoff1 = offX + 81;
        int xoff2 = offX + 112;
        int yoff = offY + 22;
        int color = alert?0xFF0000:0x000000;

        guiGraphics.drawString(this.font, paramName, xoff1 + 1, yoff + 14 * row, color, false);
        guiGraphics.drawString(this.font, paramValue, xoff2 + 4, yoff + 14 * row, color, false);
    }

    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.xMouse = (float)i;
        this.yMouse = (float)j;

        this.renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);
    }
}
