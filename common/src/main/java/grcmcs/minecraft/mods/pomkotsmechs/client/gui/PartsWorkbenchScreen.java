package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.BuilderUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.RailSliderItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class PartsWorkbenchScreen  extends AbstractContainerScreen<PartsWorkbenchMenu> {
    private static final ResourceLocation TEXTURE_PROD =
            new ResourceLocation(PomkotsMechs.MODID, "textures/gui/partsworkbench_bg.png");
    private static final ResourceLocation TEXTURE_ENH =
            new ResourceLocation(PomkotsMechs.MODID, "textures/gui/partsworkbench_bg_upgrade.png");

    private PartsWorkbenchMenu.Tab currentTab;

    private final List<PartEntry> craftablePartsList = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedIndex = -1;

    private final List<Material> requiredMaterials = new ArrayList<>();

    private ItemStack previewItem = ItemStack.EMPTY;
    private int previewX = 9;
    private int previewY = 45;
    private int previewSize = 64;

    private final int previewSectionWidth;
    private final int materialSectionWidth;
    private final int partsSelectSectionWidth;
    private final int inventorySectionWidth;

    private Button craftButton;
    private Button upgradeButton;

    public PartsWorkbenchScreen(PartsWorkbenchMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 400;
        this.imageHeight = 180;

        this.previewSectionWidth = 80;
        this.materialSectionWidth = 80;
        this.partsSelectSectionWidth = 120;
        this.inventorySectionWidth = imageWidth - (previewSectionWidth + materialSectionWidth + partsSelectSectionWidth);

        this.titleLabelX = 90;
        this.inventoryLabelY = 60;
    }

    @Override
    protected void init() {
        super.init();
        currentTab = menu.getCurrentTab();

        craftablePartsList.clear();
        registerAllParts();

        craftButton = this.addRenderableWidget(Button.builder(
                Utils.string2Component("{text.pomkotsmechs.gui.partsworkbench.btn.craft}"),
                this::onCraftButtonPressed
        ).bounds(this.leftPos + previewX, this.topPos + 120, 70, 14).build());

        upgradeButton = this.addRenderableWidget(Button.builder(
                Utils.string2Component("{text.pomkotsmechs.gui.partsworkbench.btn.upgrade}"),
                this::onUpgradeButtonPressed
        ).bounds(this.leftPos + previewX, this.topPos + 120, 70, 14).build());

        setupButtonVisibility(currentTab);
    }

    private void registerAllParts() {
        // 仮のパーツデータ
        this.registerParts(PomkotsMechs.ALTAIR_HEAD);
        this.registerParts(PomkotsMechs.ALTAIR_BODY);
        this.registerParts(PomkotsMechs.ALTAIR_ARM);
        this.registerParts(PomkotsMechs.ALTAIR_LEGS);
        this.registerParts(PomkotsMechs.DENEB_HEAD);
        this.registerParts(PomkotsMechs.DENEB_BODY);
        this.registerParts(PomkotsMechs.DENEB_ARM);
        this.registerParts(PomkotsMechs.DENEB_LEGS);
        this.registerParts(PomkotsMechs.VEGA_HEAD);
        this.registerParts(PomkotsMechs.VEGA_BODY);
        this.registerParts(PomkotsMechs.VEGA_ARM);
        this.registerParts(PomkotsMechs.VEGA_LEGS);
        this.registerParts(PomkotsMechs.SIRIUS_HEAD);
        this.registerParts(PomkotsMechs.SIRIUS_BODY);
        this.registerParts(PomkotsMechs.SIRIUS_ARM);
        this.registerParts(PomkotsMechs.SIRIUS_LEGS);
        this.registerParts(PomkotsMechs.ALDEBARAN_HEAD);
        this.registerParts(PomkotsMechs.ALDEBARAN_BODY);
        this.registerParts(PomkotsMechs.ALDEBARAN_ARM);
        this.registerParts(PomkotsMechs.ALDEBARAN_LEGS);
        this.registerParts(PomkotsMechs.MUKNVALI_HEAD);
        this.registerParts(PomkotsMechs.MUKNVALI_BODY);
        this.registerParts(PomkotsMechs.MUKNVALI_ARM);
        this.registerParts(PomkotsMechs.MUKNVALI_LEGS);
        this.registerParts(PomkotsMechs.SHAKUJI_WEAPON);
        this.registerParts(PomkotsMechs.SHINOBAZU_WEAPON);
        this.registerParts(PomkotsMechs.SENZOKU_WEAPON);
        this.registerParts(PomkotsMechs.KASUMI_WEAPON);
        this.registerParts(PomkotsMechs.KAGAMI_WEAPON);
        this.registerParts(PomkotsMechs.UGUISU_WEAPON);
        this.registerParts(PomkotsMechs.MASHU_WEAPON);
        this.registerParts(PomkotsMechs.TENPOU_WEAPON);
        this.registerParts(PomkotsMechs.TSURUGI_WEAPON);
        this.registerParts(PomkotsMechs.MITAKE_WEAPON);
        this.registerParts(PomkotsMechs.KAGENOBU_WEAPON);
        this.registerParts(PomkotsMechs.TAKAO_WEAPON);
        this.registerParts(PomkotsMechs.JINBA_WEAPON);
        this.registerParts(PomkotsMechs.AMAGI_WEAPON);
        this.registerParts(PomkotsMechs.DAIGOMARU_WEAPON);
        this.registerParts(PomkotsMechs.SHOUTOU_WEAPON);
        this.registerParts(PomkotsMechs.WADA_WEAPON);
        this.registerParts(PomkotsMechs.BIWA_WEAPON);
        this.registerParts(PomkotsMechs.SUWA_WEAPON);
        this.registerParts(PomkotsMechs.KAWASEMI_WEAPON);
        this.registerParts(PomkotsMechs.NOSURI_WEAPON);
        this.registerParts(PomkotsMechs.TSUBAME_WEAPON);
        this.registerParts(PomkotsMechs.MUKUDORI_WEAPON);
        this.registerParts(PomkotsMechs.DODO_WEAPON);
        this.registerParts(PomkotsMechs.SAGA_GENERATOR);
        this.registerParts(PomkotsMechs.SHIGA_GENERATOR);
        this.registerParts(PomkotsMechs.CHIBA_GENERATOR);
        this.registerParts(PomkotsMechs.KANSAI_BOOSTER);
        this.registerParts(PomkotsMechs.NARITA_BOOSTER);
        this.registerParts(PomkotsMechs.HANEDA_BOOSTER);
        this.registerParts(PomkotsMechs.SB_PROTO);
        this.registerParts(PomkotsMechs.BUILDER_UNIT);
        this.registerParts(PomkotsMechs.SOFT_LOCK_CIRCUIT);
        this.registerParts(PomkotsMechs.HARD_LOCK_CIRCUIT);
        this.registerParts(PomkotsMechs.HOVER_UNIT);
        this.registerParts(PomkotsMechs.RAIL_SLIDER);
    }

    private void registerParts(RegistrySupplier<Item> partsSupplier) {
        var dataPack = PomkotsDataPackManager.getInstance().getDataPack();
        var partsData = dataPack.getPartsData(partsSupplier.getId().getPath());

        if (partsData == null) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isCreative = mc.player.isCreative();
        boolean hasBluePrint = hasBluePrint(mc.player.getInventory(), partsSupplier.getId());

        // クリエイティブ or BluePrintがある場合のみ追加
        if (isCreative || hasBluePrint) {
            craftablePartsList.add(new PartEntry(
                    partsSupplier.getId(),
                    partsData,
                    new ItemStack(partsSupplier.get())
            ));
        }
    }

    private boolean hasBluePrint(Inventory inventory, ResourceLocation partsId) {
        String partsName = partsId.getPath();
        if (partsName.endsWith("head") || partsName.endsWith("body") || partsName.endsWith("arm") || partsName.endsWith("legs")) {
            partsName = partsName.replaceAll("head", "").replaceAll("body", "").replaceAll("arm", "").replaceAll("legs", "");
        }

        ResourceLocation bluePrintId = getBluePrintId(partsId.getNamespace(), partsName);

        Item bluePrint = BuiltInRegistries.ITEM.get(bluePrintId);
        if (bluePrint == Items.AIR) return false; // BluePrintアイテム自体が存在しない

        return inventory.countItem(bluePrint) > 0;
    }

    private ResourceLocation getBluePrintId(String nameSpace, String partsName) {
        return switch (partsName) {
            case "hoverunit" -> new ResourceLocation(nameSpace, "blue_print_hover_unit");
            case "railslider" -> new ResourceLocation(nameSpace, "blue_print_rail_slider_unit");
            case "protosbunit" -> new ResourceLocation(nameSpace, "blue_print_proto_super_boost_unit");
            case "builderunit" -> new ResourceLocation(nameSpace, "blue_print_builder_unit");
            default -> new ResourceLocation(
                    nameSpace,
                    "blue_print_" + partsName
            );
        };
    }

    // == ボタン関連処理 ====================================================================================

    private void onCraftButtonPressed(Button btn) {
        if (!menu.getSlot(0).getItem().isEmpty()) {
            displayMessage("{text.pomkotsmechs.messages.partsworkbench.01}");

        } else if (previewItem.isEmpty()) {
            displayMessage("{text.pomkotsmechs.messages.partsworkbench.08}");

        } else if (previewItem.getItem() instanceof BasePartsItem parts) {
            var partsData = PartsWorkbenchMenu.getPartsData(parts);

            if (partsData == null || partsData.recipes.isEmpty() || partsData.recipes.get(0).isEmpty()) {
                displayMessage("{text.pomkotsmechs.messages.partsworkbench.02}");
                return;
            }

            if (PartsWorkbenchMenu.isCraftable(Minecraft.getInstance().player.getInventory(), partsData.recipes.get(0))) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeId(BuiltInRegistries.ITEM, parts);
                NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_PARTS_WKBNCH_CRAFT), buf);

            } else {
                displayMessage("{text.pomkotsmechs.messages.partsworkbench.03}");
            }
        } else {
            displayMessage("{text.pomkotsmechs.messages.partsworkbench.09}");
        }
    }

    private void onUpgradeButtonPressed(Button btn) {
        var inputItemStack = menu.getSlot(1).getItem();

        if (inputItemStack.isEmpty()) {
            displayMessage("{text.pomkotsmechs.messages.partsworkbench.10}");

        } else if (inputItemStack.getItem() instanceof BasePartsItem parts) {
            var partsData = PartsWorkbenchMenu.getPartsData(parts);
            var curLevel = parts.getLevel(inputItemStack);

            if (partsData == null) {
                displayMessage("{text.pomkotsmechs.messages.partsworkbench.05}");
                return;
            } else if (parts.getMaxLevel() <= curLevel) {
                displayMessage("{text.pomkotsmechs.messages.partsworkbench.06}");
                return;
            } else if (partsData.recipes.isEmpty() || partsData.recipes.size() < curLevel || partsData.recipes.get(curLevel).isEmpty()) {
                displayMessage("{text.pomkotsmechs.messages.partsworkbench.02}");
                return;
            }

            if (PartsWorkbenchMenu.isCraftable(Minecraft.getInstance().player.getInventory(), partsData.recipes.get(curLevel))) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeId(BuiltInRegistries.ITEM, parts);
                NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_PARTS_WKBNCH_UPGRADE), buf);

            } else {
                displayMessage("{text.pomkotsmechs.messages.partsworkbench.03}");
            }
        } else {
            displayMessage("{text.pomkotsmechs.messages.partsworkbench.07}");
        }
    }

    // == 共通の描画処理 ====================================================================================

    private ItemStack prevUpgradeTarget = ItemStack.EMPTY;
    private ItemStack upgradeTarget = ItemStack.EMPTY;
    private List<ItemStack> upgradeTargetLevels = new ArrayList<>();

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        this.renderTooltip(guiGraphics, i, j);

        if (PartsWorkbenchMenu.Tab.UPGRADE == currentTab) {
            prevUpgradeTarget = upgradeTarget;
            upgradeTarget = menu.getSlot(1).getItem();

            if (prevUpgradeTarget != upgradeTarget) {
                setupUpgradeTarget(upgradeTarget);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        if (currentTab == PartsWorkbenchMenu.Tab.CRAFT) {
            renderBgTexture(TEXTURE_PROD, gui);

            int left = leftPos;
            int top = topPos;

            // === セクション境界 ===

            // 区切り線
            gui.fill(left + previewSectionWidth, top + 35, left + previewSectionWidth + 1, top + imageHeight - 22, 0xFF606060);
            gui.fill(left + previewSectionWidth + materialSectionWidth, top + 35, left + previewSectionWidth + materialSectionWidth + 1, top + imageHeight - 22, 0xFF606060);
            gui.fill(left + previewSectionWidth + materialSectionWidth + partsSelectSectionWidth, top + 35, left + previewSectionWidth + materialSectionWidth + partsSelectSectionWidth + 1, top + imageHeight - 22, 0xFF606060);

            // --- プレビューセクション ---
            renderPreview(gui, previewItem, left, top, previewSectionWidth, previewSize, partialTick);

            // --- 素材リストセクション ---
            renderMaterialSection(gui, left + previewSectionWidth + 4, top, materialSectionWidth - 8, mouseX, mouseY);

            // --- パーツリストセクション ---
            renderPartList(gui, left + previewSectionWidth + materialSectionWidth + 4, top, partsSelectSectionWidth - 8, mouseX, mouseY);

            // --- インベントリセクション
            renderInventory(gui, left + previewSectionWidth + materialSectionWidth + partsSelectSectionWidth + 4, top, inventorySectionWidth - 8, mouseX, mouseY);

        } else {
            renderBgTexture(TEXTURE_ENH, gui);

            int left = leftPos;
            int top = topPos;

            // === セクション境界 ===

            // 区切り線
            gui.fill(left + previewSectionWidth, top + 35, left + previewSectionWidth + 1, top + imageHeight - 22, 0xFF606060);
            gui.fill(left + previewSectionWidth + materialSectionWidth, top + 35, left + previewSectionWidth + materialSectionWidth + 1, top + imageHeight - 22, 0xFF606060);
            gui.fill(left + previewSectionWidth + materialSectionWidth + partsSelectSectionWidth, top + 35, left + previewSectionWidth + materialSectionWidth + partsSelectSectionWidth + 1, top + imageHeight - 22, 0xFF606060);

            // --- プレビューセクション ---
            renderPreview(gui, previewItem, left, top, previewSectionWidth, previewSize, partialTick);

            // --- 素材リストセクション ---
            renderMaterialSection(gui, left + previewSectionWidth + 4, top, materialSectionWidth - 8, mouseX, mouseY);

            // --- パーツのレベル毎の性能を表示するセクション ---
            renderPartsLevels(gui, left + previewSectionWidth + materialSectionWidth + 4, top, partsSelectSectionWidth - 8, mouseX, mouseY);

            // --- インベントリセクション
            renderInventory(gui, left + previewSectionWidth + materialSectionWidth + partsSelectSectionWidth + 4, top, inventorySectionWidth - 8, mouseX, mouseY);
        }
    }

    private void renderBgTexture(ResourceLocation texture, GuiGraphics gfx) {
        RenderSystem.setShaderTexture(0, texture);

        int x = this.leftPos;
        int y = this.topPos;
        gfx.blit(texture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
        guiGraphics.drawCenteredString(font, "Parts Workbench: Level " + 1, imageWidth / 2, this.titleLabelY, 0xFFFFFF);

        if (currentTab == PartsWorkbenchMenu.Tab.CRAFT) {
            guiGraphics.drawCenteredString(this.font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.craft}"), 30, 18, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.upgrade}"),81, 18, 0x404040);
            guiGraphics.drawString(this.font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.output}"), 30, 145, 0xFFFFFF, false);

        } else if (currentTab == PartsWorkbenchMenu.Tab.UPGRADE) {
            guiGraphics.drawCenteredString(this.font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.craft}"), 30, 18, 0x404040);
            guiGraphics.drawCenteredString(this.font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.upgrade}"),81, 18, 0xFFFFFF);
            guiGraphics.drawString(this.font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.target}"), 30, 145, 0xFFFFFF, false);
        }
    }

    // == セクション毎の描画処理[プレビュー] ====================================================================================

    private void renderPreview(GuiGraphics gui, ItemStack stack, int left, int top, int width, int size, float partialTicks) {
        Lighting.setupForEntityInInventory();

        gui.drawCenteredString(font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.preview}"), left + width / 2, top + 33, 0xFFFFFF);

        left += previewX;
        top += previewY;

        gui.fill(left, top, left + size, top + size, 0xFF404040);

        if (previewItem.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = gui.pose();

        pose.pushPose();

        // 座標：中央に配置
        pose.translate(left + size / 2.0, top + size / 2.0 + 8, 150);

        // GUI空間のY軸は逆なので反転
        pose.scale(1.0F, -1.0F, 1.0F);

        // 実際のアイテムを小さくスケーリングして枠内に収まるようにする
        float scale = size / 8F;
        pose.scale(scale, scale, scale);

        // 回転（ゆっくり回る）
        double time = (System.currentTimeMillis() % 8000L) / 8000.0;
        float rotation = (float) (time * 360.0);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        pose.mulPose(Axis.XP.rotationDegrees(-25.0F)); // 少し傾ける

        // ライトを適用して描画
        mc.getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                15728880, // ライト
                0, // オーバーレイ
                pose,
                gui.bufferSource(),
                mc.level,
                0
        );

        gui.bufferSource().endBatch();
        pose.popPose();

        Lighting.setupFor3DItems();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (PartsWorkbenchMenu.Tab.CRAFT == currentTab) {
            // プレビュー上にマウスがある場合、ツールチップ表示
            int px = leftPos + previewX;
            int py = topPos + previewY;
            if (mouseX >= px && mouseX <= px + previewSize && mouseY >= py && mouseY <= py + previewSize) {
                if (!previewItem.isEmpty()) {
                    graphics.renderTooltip(font, previewItem, mouseX, mouseY);
                }
            }
        }
    }

    // == セクション毎の描画処理[必要素材] ====================================================================================

    private void renderMaterialSection(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
        gui.drawCenteredString(font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.requirements}"), left + width / 2, top + 33, 0xFFFFFF);

        if (previewItem.isEmpty()) {
            return;
        }

        int x = left + 2;
        int y = top + 45;
        int slotSize = 18;
        int index = 0;

        for (Material mat : requiredMaterials) {
            Item item = mat.getFirst();
            int required = mat.getSecond();
            int has = Minecraft.getInstance().player.getInventory().countItem(item);

            // アイテムスロット枠
            gui.fill(x - 1, y - 1, x + slotSize + 1, y + slotSize + 1, 0xFF404040);

            // アイテムアイコン
            ItemStack stack = new ItemStack(item);
            gui.renderItem(stack, x + 1, y + 1);

            // 所持数 / 必要数 のテキストを右下に描画
            String countText = has + "/" + required;
            int color = has >= required ? 0x80FF80 : 0xFF8080;
            drawScaledTextCentered(gui, font, Component.literal(countText), x + slotSize/2, y + slotSize + 2, slotSize, color);

            // ホバー判定
            if (mouseX >= x && mouseX <= x + slotSize && mouseY >= y && mouseY <= y + slotSize) {
                gui.renderTooltip(font, stack, mouseX, mouseY);
            }

            // グリッド配置（3列ごとに折り返し）
            index++;
            x += slotSize + 8;
            if (index % 3 == 0) {
                x = left + 2;
                y += slotSize + 16;
            }
        }
    }

    // == セクション毎の描画処理[生成パーツ選択] ====================================================================================

    // 1つのタイル（アイコン）のサイズ
    private static final int ENTRY_SIZE = 24; // 1アイコン24px（16pxアイテム＋余白）
    private static final int ENTRIES_PER_ROW = 4;

    private void renderPartList(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
        gui.drawCenteredString(font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.selectparts}"), left + width / 2, top + 33, 0xFFFFFF);

        int listTop = top + 45;
        int listHeight = imageHeight - 55;

        int entriesPerRow = ENTRIES_PER_ROW;
        int visibleRows = listHeight / (ENTRY_SIZE + 2);
        int totalRows = (int) Math.ceil((float) craftablePartsList.size() / entriesPerRow);

        // 現在のスクロール位置から表示範囲を計算
        int startRow = scrollOffset;
        int endRow = Math.min(totalRows, startRow + visibleRows);

        // ==== 描画 ====
        for (int row = startRow; row < endRow; row++) {
            for (int col = 0; col < entriesPerRow; col++) {
                int index = row * entriesPerRow + col;
                if (index >= craftablePartsList.size()) break;

                PartEntry entry = craftablePartsList.get(index);

                int x = left + col * (ENTRY_SIZE + 2);
                int y = listTop + (row - startRow) * (ENTRY_SIZE + 2);

                boolean hovered = isInside(mouseX, mouseY, x, y, ENTRY_SIZE, ENTRY_SIZE);
                boolean selected = (index == selectedIndex);

                int bg = hovered ? 0xFF505050 : (selected ? 0xFF6060A0 : 0xFF303030);
                gui.fill(x, y, x + ENTRY_SIZE, y + ENTRY_SIZE, bg);

                gui.renderItem(entry.getItemStack(), x + 4, y + 4);
            }
        }

        // ==== スクロールバー ====
        int barHeight = Math.max(10, (int)((float)listHeight * visibleRows / Math.max(1, totalRows)));
        int barPos = listTop + (int)((float)scrollOffset / Math.max(1, (totalRows - visibleRows)) * (listHeight - barHeight));
        gui.fill(left + width - 6, barPos, left + width - 2, barPos + barHeight, 0xFF505050);

        // ==== ツールチップ ====
        for (int row = startRow; row < endRow; row++) {
            for (int col = 0; col < entriesPerRow; col++) {
                int index = row * entriesPerRow + col;
                if (index >= craftablePartsList.size()) break;

                int x = left + col * (ENTRY_SIZE + 2);
                int y = listTop + (row - startRow) * (ENTRY_SIZE + 2);

                if (isInside(mouseX, mouseY, x, y, ENTRY_SIZE, ENTRY_SIZE)) {
                    PartEntry entry = craftablePartsList.get(index);
//                    gui.renderTooltip(font, List.of(entry.getName(), Component.literal(entry.itemParams.description)), Optional.empty(), mouseX, mouseY);
                    gui.renderTooltip(font, entry.getItemStack(), mouseX, mouseY);

                    return;
                }
            }
        }
    }

    // == セクション毎の描画処理[強化時のレベルごと性能] ====================================================================================

    private static int LEVEL_ROW_WIDTH = 110;
    private static int LEVEL_ROW_HEIGHT = 20;
    private void renderPartsLevels(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
        gui.drawCenteredString(font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.partslevels}"), left + width / 2, top + 33, 0xFFFFFF);

        if (upgradeTargetLevels.isEmpty() || !(upgradeTarget.getItem() instanceof BasePartsItem parts)) {
            return;
        }

        int listTop = top + 45;

        // 現在のスクロール位置から表示範囲を計算
        int startRow = 0;

        // ==== 描画 ====
        for (int row = startRow; row < upgradeTargetLevels.size(); row++) {
            int x = left;
            int y = listTop + (row - startRow) * (LEVEL_ROW_HEIGHT + 2);

            boolean isCurrent = parts.getLevel(upgradeTarget) - 1 == row;

            int bg = isCurrent ? 0xFF6060A0 : 0xFF303030;

            gui.fill(x, y, x + LEVEL_ROW_WIDTH, y + LEVEL_ROW_HEIGHT, bg);
            gui.drawString(font, "Level " + (row + 1) + (isCurrent ? ": Current" : ""), left + 24, y + 6, 0xFFFFFF, false);

            var itemStack = upgradeTargetLevels.get(row);
            gui.renderItem(itemStack, x + 4, y + 4);
        }

        // ==== ツールチップ ====
        for (int row = startRow; row < upgradeTargetLevels.size(); row++) {
            int x = left;
            int y = listTop + (row - startRow) * (LEVEL_ROW_HEIGHT + 2);

            boolean hovered = isInside(mouseX, mouseY, x, y, LEVEL_ROW_WIDTH, LEVEL_ROW_HEIGHT);
            var itemStack = upgradeTargetLevels.get(row);
            if (hovered) {
                gui.renderTooltip(font, itemStack, mouseX, mouseY);
            }
        }
    }

    private void setupUpgradeTarget(ItemStack target) {
        setPreviewItem(target);
        upgradeTargetLevels.clear();
        requiredMaterials.clear();

        if (target.getItem() instanceof BasePartsItem partsItem) {
            int maxLevel = partsItem.getMaxLevel();
            for (int l = 0; l < maxLevel; l++) {
                ItemStack stack = new ItemStack(partsItem);
                partsItem.setLevel(stack, l + 1);
                upgradeTargetLevels.add(stack);
            }

            PomkotsDataPack.PartsData pd = PartsWorkbenchMenu.getPartsData(partsItem);

            if (pd != null) {
                setupRequiredMaterials(partsItem.getLevel(target), pd);
            }
        }
    }

    private void renderInventory(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
        gui.drawCenteredString(font, getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.inventory}"), left + width / 2, top + 33, 0xFFFFFF);
    }

    // == マウス関連処理 ====================================================================================

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (PartsWorkbenchMenu.Tab.CRAFT == currentTab) {
            int listHeight = imageHeight - 55;
            int visibleRows = listHeight / (ENTRY_SIZE + 2);
            int totalRows = (int) Math.ceil((float) craftablePartsList.size() / ENTRIES_PER_ROW);
            int maxScroll = Math.max(0, totalRows - visibleRows);

            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll);
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 左のタブ
        if (isInside(mouseX, mouseY, leftPos + 5, topPos + 19, 48, 10)) {
            switchTab(PartsWorkbenchMenu.Tab.CRAFT);
            return true;
        }

        // 右のタブ
        if (isInside(mouseX, mouseY, leftPos + 55, topPos + 19, 48, 10)) {
            switchTab(PartsWorkbenchMenu.Tab.UPGRADE);
            return true;
        }

        if (PartsWorkbenchMenu.Tab.CRAFT == currentTab && button == 0) {
            int listLeft = leftPos + previewSectionWidth + materialSectionWidth + 4;
            int listTop = topPos + 48;
            int listHeight = imageHeight - 55;

            int entriesPerRow = ENTRIES_PER_ROW;
            int visibleRows = listHeight / ENTRY_SIZE;
            int startRow = scrollOffset;
            int endRow = Math.min((int)Math.ceil((float) craftablePartsList.size() / entriesPerRow), startRow + visibleRows);

            for (int row = startRow; row < endRow; row++) {
                for (int col = 0; col < entriesPerRow; col++) {
                    int index = row * entriesPerRow + col;
                    if (index >= craftablePartsList.size()) break;

                    int x = listLeft + col * (ENTRY_SIZE + 2);
                    int y = listTop + (row - startRow) * (ENTRY_SIZE + 2);

                    if (isInside(mouseX, mouseY, x, y, ENTRY_SIZE, ENTRY_SIZE)) {
                        partsForCraftSelected(index);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void partsForCraftSelected(int i) {
        setupCraftTarget(i);

        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("Selected: " + craftablePartsList.get(i).getName().getString()), true);
    }

    private void setupCraftTarget(int i) {
        selectedIndex = i;

        if (i < 0) {
            setPreviewItem(ItemStack.EMPTY);
            requiredMaterials.clear();

        } else {
            setPreviewItem(craftablePartsList.get(selectedIndex).getItemStack());
            setupRequiredMaterials(0, craftablePartsList.get(selectedIndex).itemParams);
        }
    }

    public void setPreviewItem(ItemStack stack) {
        this.previewItem = stack;
    }

    private boolean setupRequiredMaterials(int level, PomkotsDataPack.PartsData partsData) {
        requiredMaterials.clear();

        if (partsData.recipes.size() <= level) {
            return false;
        }

        var recipe = partsData.recipes.get(level);

        for (var material: recipe) {
            var key = material.getFirst();
            var num = material.getSecond();

            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(key));

            if (Items.AIR.equals(item)) {
                return false;
            }

            requiredMaterials.add(new Material(item, num));
        }

        return true;
    }

    // == タブ関連処理 ====================================================================================

    private void switchTab(PartsWorkbenchMenu.Tab tab) {
        if (tab != currentTab) {
            currentTab = tab;
            menu.setTab(tab);
            sendTabChange(tab);

            if (tab == PartsWorkbenchMenu.Tab.CRAFT) {
                setupCraftTarget(selectedIndex);
            } else if (tab == PartsWorkbenchMenu.Tab.UPGRADE) {
                setupUpgradeTarget(menu.getSlot(1).getItem());
            }
        }

        setupButtonVisibility(currentTab);
    }

    private void sendTabChange(PartsWorkbenchMenu.Tab tab) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(tab.getInt());
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_PARTS_WKBNCH_TAB_CHANGE), buf);
    }

    private boolean isInside(double x, double y, int bx, int by, int bw, int bh) {
        return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
    }

    // == その他 ====================================================================================

    private void setupButtonVisibility(PartsWorkbenchMenu.Tab tab) {
        craftButton.visible = PartsWorkbenchMenu.Tab.CRAFT == tab;
        upgradeButton.visible = PartsWorkbenchMenu.Tab.UPGRADE == tab;
    }

    private void displayMessage(String msg) {
        Minecraft.getInstance().player.displayClientMessage(
                Utils.string2Component(msg), false
        );
    }

    private String getLocalizedString(String id) {
        return Utils.string2Component(id).getString();
    }

    public static void drawScaledTextCentered(
            GuiGraphics graphics,
            Font font,
            Component text,
            float centerX,
            float y,
            float maxWidth,
            int color) {

        float textWidth = font.width(text);
        float scale = Math.min(1.0F, maxWidth / textWidth);

        // スケール後の実際の幅
        float scaledWidth = textWidth * scale;

        // 中央揃えのためのX座標
        float x = centerX - scaledWidth / 2.0F;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0F);

        graphics.drawString(font, text, 0, 0, color, true);

        poseStack.popPose();
    }

    public static class Material extends Pair<Item, Integer> {
        public Material(Item first, Integer second) {
            super(first, second);
        }
    }

    public static class PartEntry {
        private final ResourceLocation id;
        private final PomkotsDataPack.PartsData itemParams;
        private final ItemStack itemStack;
        private final Component name;

        public PartEntry(ResourceLocation id, PomkotsDataPack.PartsData itemParams, ItemStack itemStack) {
            this.id = id;
            this.itemParams = itemParams;
            this.itemStack = itemStack;
            this.name = itemStack.getItem().getName(itemStack);
        }

        public ResourceLocation getId() {
            return id;
        }

        public PomkotsDataPack.PartsData getItemParams() {
            return itemParams;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public Component getName() {
            return this.name;
        }
    }
}
