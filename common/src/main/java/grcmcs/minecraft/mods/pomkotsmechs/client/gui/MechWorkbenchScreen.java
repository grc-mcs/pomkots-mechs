package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class MechWorkbenchScreen extends AbstractContainerScreen<MechWorkbenchMenu> {
    private static final ResourceLocation V_GUI_TEXTURE = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench_bg.png");

    private float xMouse;
    private float yMouse;
    private Pmvc01Entity mech = null;

    public MechWorkbenchScreen(MechWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 256;
        
        this.titleLabelX = 44;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private Button generateCardButton;
    private Button repairMechButton;
    private Button textureColorButton;

    private boolean isDropdownOpen = false;
    private List<Button> optionButtons = new ArrayList<>();
    private List<String> options = BasePartsItemModel.BASE_COLORS;
    private String selectedOption = "Color";



    @Override
    protected void init() {
        super.init();

        // 認証カード生成ボタン
        generateCardButton = this.addRenderableWidget(Button.builder(
                Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.getcard}"),
                btn -> sendSecurityAction(PomkotsMechs.PACKET_SECURITY_GENCARD)
        ).bounds(this.leftPos + 7, this.topPos + 91, 70, 14).build());

        // リペアボタン
        repairMechButton = this.addRenderableWidget(Button.builder(
                Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.repair}"),
                btn -> sendSecurityAction(PomkotsMechs.PACKET_REPAIR_MECH)
        ).bounds(this.leftPos + 90, this.topPos + 91, 70, 14).build());

        // テクスチャ選択ボタン
        this.textureColorButton = addRenderableWidget(
                Button.builder(Component.literal(selectedOption), button -> toggleDropdown()).pos(this.leftPos + 114, this.topPos + 3).size(50, 15).build());

        // 選択肢のボタン
        for (int i = 0; i < options.size(); i++) {
            int y = this.topPos + 3 + 15 + (i * 15);
            Button optionButton = Button.builder(Component.literal(options.get(i)), button -> selectOption(button)).pos(this.leftPos + 114, y).size(50, 15).build();
            optionButton.visible = false; // 初期状態では非表示
            optionButtons.add(optionButton);
            addRenderableWidget(optionButton);
        }

        this.mech = null;
    }

    private void toggleDropdown() {
        isDropdownOpen = !isDropdownOpen;
        for (Button button : optionButtons) {
            button.visible = isDropdownOpen;
        }
    }

    private void selectOption(Button button) {
        this.selectedOption = button.getMessage().getString();
        this.textureColorButton.setMessage(Component.literal(selectedOption));

        sendTextureColor2Server(BasePartsItemModel.getColorIndex(button.getMessage().getString()));

        toggleDropdown();
    }

    private void sendTextureColor2Server(int color) {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(mech.getUUID());
            buf.writeInt(color);
            NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_CHANGE_TEXTURE), buf);
        }
    }

    private void sendSecurityAction(String message) {
        if (mech != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUUID(mech.getUUID());
            NetworkManager.sendToServer(PomkotsMechs.id(message), buf);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        if (menu.getMode() == MechWorkbenchMenu.MODE_VIEW) {
            generateCardButton.visible = false;
            repairMechButton.visible = false;
            textureColorButton.visible = false;
        }

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

        if (this.minecraft != null && this.minecraft.level != null && this.mech == null) {
            for (Entity entity : this.minecraft.level.entitiesForRendering()) {
                if (entity instanceof Pmvc01Entity m) {
                    if ((short)(entity.getUUID().hashCode()) == this.menu.getEntityId()) {
                        this.mech = m;
                    }
                }
            }
        }

        if (this.mech != null) {
            renderEntityInInventoryFollowsMouse(guiGraphics, offsetX + 42, offsetY + 82, 10, offsetX + 42 - this.xMouse, offsetY + 82 - this.yMouse, mech);
//            renderEntity(guiGraphics, offsetX + 42, offsetY + 82, 10, offsetX + 42 - this.xMouse, offsetY + 82 - this.yMouse, mech);

            drawParams(0, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.hp}", mech.getDurability() + "", false, guiGraphics);
            drawParams(1, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.weight}", mech.getWeight() + "/" + mech.getMaxWeight(), mech.getWeight() > mech.getMaxWeight(), guiGraphics);
            drawParams(2, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.speed}", String.format("%.1f", mech.getSpeedModifier()) + "/" + String.format("%.1f",mech.getJumpModifier()), false, guiGraphics);
            drawParams(3, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.boost}", String.format("%.1f", mech.getSpeedModifierEvasion()) + "/" + String.format("%.1f", mech.getSpeedModifierVertical()), false, guiGraphics);
            drawParams(4, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.energy}", String.format("%d", mech.getMaxEnergy()) + "/" + String.format("%d", mech.getEnergyChargePerTick()), false, guiGraphics);

        }
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int i, int j, int k, float f, float g, LivingEntity livingEntity) {
        float h = (float)Math.atan((double)(f / 40.0F));
        float l = (float)Math.atan((double)(g / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(l * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);
        float m = livingEntity.yBodyRot;
        float n = livingEntity.getYRot();
        float o = livingEntity.getXRot();
        float p = livingEntity.yHeadRotO;
        float q = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + h * 20.0F;
        livingEntity.setYRot(180.0F + h * 40.0F);
        livingEntity.setXRot(-l * 20.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        renderEntityInInventory(guiGraphics, i, j, k, quaternionf, quaternionf2, livingEntity);
        livingEntity.yBodyRot = m;
        livingEntity.setYRot(n);
        livingEntity.setXRot(o);
        livingEntity.yHeadRotO = p;
        livingEntity.yHeadRot = q;
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, int i, int j, int k, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((double)i, (double)j, 50.0);
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

    private void renderEntity(
            GuiGraphics gg,
            int x, int y,
            int size,
            float mouseX, float mouseY,
            LivingEntity entity
    ) {

        float savedYaw = entity.getYRot();
        float savedPrevYaw = entity.yRotO;
        float savedBodyYaw = entity.yBodyRot;
        float savedHeadYaw = entity.yHeadRot;
        float savedPrevHeadYaw = entity.yHeadRotO;

        Lighting.setupForEntityInInventory();
        PoseStack pose = gg.pose();
        pose.pushPose();

        try {
            entity.setYRot(0.0F);
            entity.yRotO = 0.0F;
            entity.yBodyRot = 0.0F;
            entity.yHeadRot = 0.0F;
            entity.yHeadRotO = 0.0F;

            pose.translate(x, y, 50);
            pose.scale(size, size, size);

            float yaw = mouseX * 0.01f;
            float pitch = mouseY * -0.001f;

            Quaternionf rot = new Quaternionf()
                        .rotateZ((float) Math.PI)
                        .rotateX(pitch)
                        .rotateY(yaw);

            pose.mulPose(rot);

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.setRenderShadow(false);

            MultiBufferSource.BufferSource buffer =
                    Minecraft.getInstance().renderBuffers().bufferSource();

            dispatcher.render(
                    entity,
                    0, 0, 0,
                    0,
                    1.0f,
                    pose,
                    buffer,
                    0xF000F0
            );

            buffer.endBatch();
            dispatcher.setRenderShadow(true);
        } finally {
            entity.setYRot(savedYaw);
            entity.yRotO = savedPrevYaw;
            entity.yBodyRot = savedBodyYaw;
            entity.yHeadRot = savedHeadYaw;
            entity.yHeadRotO = savedPrevHeadYaw;

            pose.popPose();
            Lighting.setupFor3DItems();
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
        guiGraphics.drawString(this.font, Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.parts}").getString(), offX + 118, offY + 110, 0x000000, false);
        guiGraphics.drawString(this.font, Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.weapons}").getString(), offX + 118, offY + 128, 0x000000, false);
        guiGraphics.drawString(this.font, Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.ammo}").getString(), offX + 118, offY + 146, 0x000000, false);
    }

    protected void drawParams(int row, int offX, int offY, String paramName, String paramValue, boolean alert, GuiGraphics guiGraphics) {
        int xoff1 = offX + 81;
        int xoff2 = offX + 112;
        int yoff = offY + 22;
        int color = alert?0xFF0000:0x000000;

        Component c = Utils.string2Component(paramName);

        guiGraphics.drawString(this.font, c.getString(), xoff1 + 1, yoff + 14 * row, color, false);
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
