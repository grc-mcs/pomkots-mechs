package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArenaReceptionistScreen
        extends AbstractContainerScreen<ArenaReceptionistMenu> {
    // =========================================================
    // TEXTURES
    // =========================================================

    private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_hangar.png");
    private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/background_gradient.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/button_normal.png");
    private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/inventory_player.png");
    private static final ResourceLocation TEXTURE_OFFERS_PANEL = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/mech_trader_offers.png");
    private static final ResourceLocation TEXTURE_CARD = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/arena_fighter_card.png");
    private static final ResourceLocation TEXTURE_CARD_SELECTED = new ResourceLocation(PomkotsMechs.MODID, "textures/gui/mechworkbench/arena_fighter_card_selected.png");
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

    private int tabSelectorOffsetX = 0;
    private int tabSelectorOffsetY = 0;

    private int rankingOffsetX = 0;
    private int rankingOffsetY = 0;

    private int playerInvX = 0;
    private int playerInvY = 0;
    private int playerInvWidth = 164;
    private int playerInvHeight = 94;

    private static final int RANKING_AREA_WIDTH = 440;
    private static final int RANKING_AREA_HEIGHT = 190;

    private static final int CARD_WIDTH = 430;
    private static final int CARD_HEIGHT = 60;
    private static final int CARD_MARGIN = 4;

    private static final int PORTRAIT_SIZE = 48;

    private static final int SCROLL_SPEED = 12;

    // =========================================================
    // Tabs
    // =========================================================

    public enum ArenaTab {
        RANKED,
        TOURNAMENT,
        PROFILE
    }

    private ArenaTab currentTab = ArenaTab.RANKED;

    private int scrollOffset;

    private int maxScroll;

    private int contentHeight;

    private UUID challengerUuid = null;

    // Profile Inupts

    private EditBox nameField;

    private EditBox commentField;

    private String mechName = "-";

    private UUID selectedFighterId;
    private ArenaRankingEntry selectedEntry;

    private Button rankMatchButton;
    private Button freeMatchButton;

    private ImageButton acceptButton;
    private ImageButton declineButton;
    private ImageButton returnButton;

    private Button saveProfileButton;
    private Button removeProfileButton;

    private ArenaRankingEntry selfProfile = null;

    // =========================================================
    // 初期化関連
    // =========================================================

    public ArenaReceptionistScreen(
            ArenaReceptionistMenu menu,
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

        this.tabSelectorOffsetX = leftPos + 20;
        this.tabSelectorOffsetY = topPos + 35;

        this.rankingOffsetX = tabSelectorOffsetX;
        this.rankingOffsetY = tabSelectorOffsetY + BUTTON_HEIGHT;

        this.playerInvX = leftPos + 310;
        this.playerInvY = topPos + 35;

        // Tab Selector
        addRenderableWidget(
                buildButton(
                        "Ranking", tabSelectorOffsetX, tabSelectorOffsetY,
                        btn -> currentTab = ArenaTab.RANKED
                )
        );

        if (menu.getMode() == ArenaReceptionistMenu.MODE_RECEPTION) {
//            addRenderableWidget(
//                    buildButton(
//                            "Tournament", tabSelectorOffsetX + BUTTON_WIDTH, tabSelectorOffsetY,
//                            btn -> currentTab = ArenaTab.TOURNAMENT
//                    )
//            );

            addRenderableWidget(
                    buildButton(
                            "Profile", tabSelectorOffsetX + BUTTON_WIDTH, tabSelectorOffsetY,
                            btn -> currentTab = ArenaTab.PROFILE
                    )
            );
        }

        // Match Button
        rankMatchButton =
                addRenderableWidget(
                        buildButton(
                                "Rank Match", tabSelectorOffsetX + BUTTON_WIDTH * 4, rankingOffsetY + RANKING_AREA_HEIGHT + 5,
                                btn -> startMatch(true)
                        )
                );

        freeMatchButton =
                addRenderableWidget(
                        buildButton(
                                "Free Match", tabSelectorOffsetX + BUTTON_WIDTH * 5 + 15, rankingOffsetY + RANKING_AREA_HEIGHT + 5,
                                btn -> startMatch(false)
                        )
                );

        if (menu.getMode() == ArenaReceptionistMenu.MODE_DATAPAD) {
            acceptButton = addRenderableWidget(
                    buildButton(
                            "Teleport", tabSelectorOffsetX + BUTTON_WIDTH * 3 - 15, rankingOffsetY - 18,
                            btn -> btnClicked(ArenaReceptionistMenu.BTN_ID_ACCEPT_MATCH),
                            isAcceptEnable()
                    )
            );

            declineButton = addRenderableWidget(
                    buildButton(
                            "Decline", tabSelectorOffsetX + BUTTON_WIDTH * 4, rankingOffsetY - 18,
                            btn -> btnClicked(ArenaReceptionistMenu.BTN_ID_DECLINE_MATCH),
                            isDeclineEnable()
                    )
            );

            returnButton = addRenderableWidget(
                    buildButton(
                            "Return", tabSelectorOffsetX + BUTTON_WIDTH * 5 + 15, rankingOffsetY - 18,
                            btn -> btnClicked(ArenaReceptionistMenu.BTN_ID_RETURN_FROM_MATCH),
                            isReturnEnable()
                    )
            );

            if (isAcceptEnable()) {
                acceptButton.setBadgeCount(100);
            }
            if (isDeclineEnable()) {
                declineButton.setBadgeCount(100);
            }
            if (isReturnEnable()) {
                returnButton.setBadgeCount(100);
            }

            var dataPad = getDataPad();
            if (dataPad != null) {
                var reqs = PomkotsDatapadItem.getArenaRequests(dataPad);
                if (!reqs.isEmpty()) {
                    challengerUuid = reqs.get(0).challengerUuid();
                }
            }
        }

        // Profile Tab
        nameField =
                new EditBox(
                        font,
                        rankingOffsetX + 20,
                        rankingOffsetY + 10 + 15,
                        180,
                        18,
                        Component.literal("Display Name")
                );
        nameField.setMaxLength(32);
        addRenderableWidget(nameField);

        commentField =
                new EditBox(
                        font,
                        rankingOffsetX + 20,
                        rankingOffsetY + 10 + 45 + 15,
                        180,
                        18,
                        Component.literal("Comment")
                );
        commentField.setMaxLength(64);
        addRenderableWidget(commentField);

        saveProfileButton = addRenderableWidget(
                buildButton(
                        "Save", rankingOffsetX, rankingOffsetY + 145 + 20,
                        btn -> savePlayerProfile()
                )
        );

        removeProfileButton = addRenderableWidget(
                buildButton(
                        "Remove", rankingOffsetX + BUTTON_WIDTH + 20, rankingOffsetY + 145 + 20,
                        btn -> removePlayerPlofile()
                )
        );

        updateScrollLimits();
        updateComponentsVisibility();
        refleshData();
    }

    // =========================================================
    // ボタン実行処理
    // =========================================================

    private void btnClicked(int btnId) {
        minecraft.gameMode
                .handleInventoryButtonClick(
                        menu.containerId,
                        btnId
                );
        this.onClose();
    }

    private void startMatch(boolean isFree) {
        if (selectedEntry == null) {
            return;
        }

        if (!isFree) {
            // ランク戦の時は条件を確認する
        }

        FriendlyByteBuf buf =
                new FriendlyByteBuf(
                        Unpooled.buffer()
                );

        buf.writeBoolean(isFree);

        buf.writeUtf(
                menu.getArenaId()
        );

        buf.writeUUID(
                selectedEntry.getFighterId()
        );

        NetworkManager.sendToServer(
                PomkotsMechs.id(
                        PomkotsMechs.PACKET_ARENA_START_MATCH
                ),
                buf
        );

        this.onClose();
    }

    private void savePlayerProfile() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(nameField.getValue());
        buf.writeUtf(commentField.getValue());
        buf.writeUtf(menu.getArenaId());

        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_ARENA_SAVE_PROFILE), buf);
    }

    private void removePlayerPlofile() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(menu.getArenaId());

        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_ARENA_REMOVE_PROFILE), buf);
    }

    // =========================================================
    // データ/表示更新系
    // =========================================================

    public void refleshData() {
        selfProfile = null;
        var player = Minecraft.getInstance().player;

        if (menu.getRankings() != null && player != null) {
            for (var fighter: menu.getRankings()) {
                if (fighter.getFighterId().equals(player.getUUID())) {
                    if (nameField != null) {
                        nameField.setValue(fighter.getDisplayName());
                    }
                    if (commentField != null) {
                        commentField.setValue(fighter.getComment());
                    }
                    mechName = fighter.getMechName();

                    selfProfile = fighter;
                }
            }
        }
    }

    private void updateMatchButtons() {
        boolean enable =
                currentTab == ArenaTab.RANKED
                        && selectedEntry != null
                        && menu.getMode() == ArenaReceptionistMenu.MODE_RECEPTION
                        && selfProfile != null
                ;

        rankMatchButton.active = enable;
        freeMatchButton.active = enable;
    }

    private void updateComponentsVisibility() {
        boolean visible = currentTab == ArenaTab.PROFILE;

        if (nameField != null) {
            nameField.visible = visible;
            nameField.setEditable(visible);
        }

        if (commentField != null) {
            commentField.visible = visible;
            commentField.setEditable(visible);
        }

        if (saveProfileButton != null) {
            saveProfileButton.visible = visible;
            saveProfileButton.active = visible;
        }

        if (removeProfileButton != null) {
            removeProfileButton.visible = visible;
            removeProfileButton.active = visible;
        }

        for (int i = 0; i < menu.slots.size(); i++) {
            if (menu.getSlot(i) instanceof ArenaReceptionistMenu.ToggleableSlot ts) {
                ts.setActive(visible);
            }
        }
    }

    private void updateScrollLimits() {
        contentHeight =
                menu.getRankings().size()
                        * (CARD_HEIGHT + CARD_MARGIN);

        maxScroll = Math.max(
                0,
                contentHeight - RANKING_AREA_HEIGHT
        );

        scrollOffset = Mth.clamp(
                scrollOffset,
                0,
                maxScroll
        );
    }

    // =========================================================
    // 描画系
    // =========================================================

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updateMatchButtons();
        updateComponentsVisibility();
        updateScrollLimits();

        renderBackground(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        if (currentTab == ArenaTab.RANKED) {
            renderRanking(graphics);
        } else if (currentTab == ArenaTab.PROFILE) {
            renderProfileTab(graphics);
        }

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );

        if (acceptButton != null && acceptButton.isHovered()) {
            graphics.renderTooltip(
                    font,
                    List.of(
                            Component.literal("Accept the challenge and teleport to the arena"),
                            Component.literal("After the match, you can return to your original location using the \"Return\" button"),
                            Component.literal("* You must be piloting a Mech").withStyle(ChatFormatting.RED)
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
        }

        if (declineButton != null && declineButton.isHovered()) {
            graphics.renderTooltip(
                    font,
                    List.of(
                            Component.literal("Decline the challenge"),
                            Component.literal("If the request times out, it will be automatically declined"),
                            Component.literal("No ranking changes will occur").withStyle(ChatFormatting.RED)
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
        }

        if (returnButton != null && returnButton.isHovered()) {
            graphics.renderTooltip(
                    font,
                    List.of(
                            Component.literal("Teleport to your pre-teleport location"),
                            Component.literal("* You must be piloting a Mech").withStyle(ChatFormatting.RED)
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
        }
    }

    // =========================================================
    // 描画系：Profileタブ
    // =========================================================

    private void renderProfileTab(
            GuiGraphics graphics
    ) {
        int x = rankingOffsetX + 10;
        int y = rankingOffsetY + 10;

        graphics.drawString(
                font,
                "Display Name",
                x,
                y,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Comment",
                x,
                y + 45,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                "Registered Mech",
                x,
                y + 90,
                0xFFFFFF
        );

        renderBgTexture(
                TEXTURE_ITEM_CONTAINER,
                x + 10,
                y + 105,
                16,
                16,
                graphics);

        graphics.drawString(
                font,
                "Put your Data Pad here",
                x + 34,
                y + 109,
                0xAAAAAA
        );

        graphics.drawString(
                font,
                "Registered Mech:",
                x,
                y + 135,
                0xFFFFFF
        );

        graphics.drawString(
                font,
                mechName,
                x + 100,
                y + 135,
                0x55FF55
        );

        renderPlayerInventoryPanel(graphics);
    }

    private void renderPlayerInventoryPanel(
            GuiGraphics guiGraphics) {
        renderBgTexture(TEXTURE_PANEL_INV_PLAYER, playerInvX, playerInvY, playerInvWidth, playerInvHeight, guiGraphics);
        guiGraphics.drawString(
                font,
                "Player Inventory",
                playerInvX + 18,
                playerInvY + 4,
                0xFFFFFFFF
        );
    }

    // =========================================================
    // 描画系：Rankタブ
    // =========================================================

    private void renderRanking(
            GuiGraphics graphics
    ) {

        List<ArenaRankingEntry> rankings =
                menu.getRankings();

        int areaX = rankingOffsetX;
        int areaY = rankingOffsetY;

        // Scroll Area BG
        graphics.fill(
                areaX,
                areaY,
                areaX + RANKING_AREA_WIDTH,
                areaY + RANKING_AREA_HEIGHT,
                0x66000000
        );

        // Clip
        graphics.enableScissor(
                areaX,
                areaY,
                areaX + RANKING_AREA_WIDTH,
                areaY + RANKING_AREA_HEIGHT
        );

        // Card Render
        int cardY =
                areaY + 4 - scrollOffset;

        for (int i = 0;
             i < rankings.size();
             i++) {

            ArenaRankingEntry entry =
                    rankings.get(i);

            // 上側に完全に消えてる
            if (cardY + CARD_HEIGHT < areaY) {

                cardY +=
                        CARD_HEIGHT
                                + CARD_MARGIN;

                continue;
            }

            // 下側に完全に出た
            if (cardY >
                    areaY + RANKING_AREA_HEIGHT) {
                break;
            }

            renderRankingCard(
                    graphics,
                    entry,
                    i,
                    areaX + 4,
                    cardY
            );

            cardY +=
                    CARD_HEIGHT
                            + CARD_MARGIN;
        }

        graphics.disableScissor();

        // Scroll Bar
        renderScrollbar(graphics);
    }

    private void renderRankingCard(
            GuiGraphics graphics,
            ArenaRankingEntry entry,
            int rank,
            int x,
            int y
    ) {
        boolean selected =
                entry.getFighterId()
                        .equals(
                                selectedFighterId
                        );

        if (selected) {
            renderBgTexture(
                    TEXTURE_CARD_SELECTED,
                    x - 2,
                    y - 2,
                    CARD_WIDTH + 4,
                    CARD_HEIGHT + 4,
                    graphics
            );
        } else {
            renderBgTexture(TEXTURE_CARD, x, y, CARD_WIDTH, CARD_HEIGHT, graphics);
        }

        graphics.fill(
                x + 6,
                y + 6,
                x + 6 + PORTRAIT_SIZE,
                y + 6 + PORTRAIT_SIZE,
                0xFF404040
        );

        if (menu.getMode() == ArenaReceptionistMenu.MODE_DATAPAD
                && challengerUuid != null
                && challengerUuid.equals(entry.getFighterId())
        ) {
            renderBadge("C",x + 12, y + 12, 0xFFFF0000, graphics);
        }

        graphics.drawCenteredString(
                font,
                "#"+(rank + 1),
                x + 30,
                y + 24,
                0xFFFFFF
        );

        String text = entry.isOnline() ? "ONLINE": "OFFLINE";
        int color = entry.isOnline() ? 0xFF55FF55: 0xFF555555;
        graphics.drawCenteredString(
                font,
                text,
                x + 30,
                y + 40,
                color
        );

        graphics.drawString(
                font,
                entry.getDisplayName() + (entry.getType() == 0 ? "" : " (NPC)"),
                x + 62,
                y + 6,
                0xFFFFFF,
                false
        );

        graphics.drawString(
                font,
                "Rating : " + entry.getRating(),
                x + 62,
                y + 18,
                0xC0C0C0,
                false
        );

        graphics.drawString(
                font,
                "Win "
                        + entry.getWins()
                        + " / Lose "
                        + entry.getLosses(),
                x + 62,
                y + 30,
                0xC0C0C0,
                false
        );

        var curRank = ArenaRank.getRank(entry.getRating());
        graphics.drawString(
                font,
                "Rank : " + curRank.getName(),
                x + 62,
                y + 45,
                curRank.getColor(),
                false
        );

        graphics.drawString(
                font,
                "Mech : " + entry.getMechName(),
                x + 240,
                y + 6,
                0x80FFFF,
                false
        );

        String comment =
                font.plainSubstrByWidth(
                        entry.getComment(),
                        170
                );

        graphics.drawString(
                font,
                comment,
                x + 240,
                y + 20,
                0xC0C0C0,
                false
        );
    }

    private void renderBadge(
            String text,
            int x,
            int y,
            int bgcolor,
            GuiGraphics graphics
    ) {

        int badgeX = x - 2;
        int badgeY = y - 2;

        graphics.fill(
                badgeX,
                badgeY,
                badgeX  + font.width(text),
                badgeY + 10,
                bgcolor
        );

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                text,
                badgeX + font.width(text)/2,
                badgeY + 1,
                0xFFFFFFFF
        );
    }

    private void renderScrollbar(
            GuiGraphics graphics
    ) {

        if (maxScroll <= 0) {
            return;
        }

        int barX =
                rankingOffsetX
                        + RANKING_AREA_WIDTH
                        + 2;

        int barY =
                rankingOffsetY;

        int barHeight =
                RANKING_AREA_HEIGHT;

        float visibleRatio =
                RANKING_AREA_HEIGHT
                        / (float)contentHeight;

        int thumbHeight =
                Math.max(
                        16,
                        (int)(barHeight
                                * visibleRatio)
                );

        float progress =
                scrollOffset
                        / (float)maxScroll;

        int movable =
                barHeight
                        - thumbHeight;

        int thumbY =
                barY
                        + (int)(progress
                        * movable);

        renderBgTexture(
                TEXTURE_SCROLL_BAR_BG,
                barX,
                barY,
                2,
                barHeight,
                graphics
        );

        renderBgTexture(
                TEXTURE_SCROLL_BAR,
                barX,
                thumbY,
                4,
                thumbHeight,
                graphics
        );
    }

    // =========================================================
    // マウス/キー押下系
    // =========================================================

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        scrollOffset -=
                (int)(delta * SCROLL_SPEED);

        scrollOffset =
                Mth.clamp(
                        scrollOffset,
                        0,
                        maxScroll
                );

        return true;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        boolean result =
                super.mouseClicked(
                        mouseX,
                        mouseY,
                        button
                );

        if (!nameField.isMouseOver(
                mouseX,
                mouseY
        )) {
            nameField.setFocused(false);
        }

        if (!commentField.isMouseOver(
                mouseX,
                mouseY
        )) {
            commentField.setFocused(false);
        }

        if (currentTab == ArenaTab.RANKED) {

            if (mouseX < rankingOffsetX
                    || mouseX > rankingOffsetX + RANKING_AREA_WIDTH
                    || mouseY < rankingOffsetY
                    || mouseY > rankingOffsetY + RANKING_AREA_HEIGHT) {
                return result;
            }

            int cardY = rankingOffsetY + 4 - scrollOffset;

            for (ArenaRankingEntry entry: menu.getRankings()) {

                int cardX = rankingOffsetX + 4;

                if (mouseX >= cardX
                        && mouseX <= cardX + CARD_WIDTH
                        && mouseY >= cardY
                        && mouseY <= cardY + CARD_HEIGHT) {

                    if (!entry.isOnline() ||
                            (this.minecraft != null
                                    && this.minecraft.player != null
                                    && entry.getFighterId() != null
                                    && this.minecraft.player.getUUID().equals(entry.getFighterId()))
                    ) {
                        continue;
                    }

                    selectedFighterId = entry.getFighterId();
                    selectedEntry = entry;

                    return true;
                }

                cardY += CARD_HEIGHT + CARD_MARGIN;
            }
        }

        return result;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (nameField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                nameField.setFocused(false);
                return true;
            }

            if (nameField.keyPressed(
                    keyCode,
                    scanCode,
                    modifiers
            )) {
                return true;
            }

            if (minecraft.options.keyInventory.matches(
                    keyCode,
                    scanCode
            )) {
                return true;
            }
        }

        if (commentField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                commentField.setFocused(false);
                return true;
            }

            if (commentField.keyPressed(
                    keyCode,
                    scanCode,
                    modifiers
            )) {
                return true;
            }

            if (minecraft.options.keyInventory.matches(
                    keyCode,
                    scanCode
            )) {
                return true;
            }
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    // =========================================================
    // その他
    // =========================================================

    private boolean isAcceptEnable() {
        var dataPadStack = getDataPad();
        if (dataPadStack == null) {
            return false;
        }
        return PomkotsDatapadItem.hasArenaRequests(dataPadStack);
    }

    private boolean isDeclineEnable() {
        var dataPadStack = getDataPad();
        if (dataPadStack == null) {
            return false;
        }
        return PomkotsDatapadItem.hasArenaRequests(dataPadStack);
    }

    private boolean isReturnEnable() {
        var dataPadStack = getDataPad();
        if (dataPadStack == null) {
            return false;
        }
        return PomkotsDatapadItem.hasReturnLocation(dataPadStack);
    }

    private ItemStack getDataPad() {
        var p = menu.getDataPadStack();
        if (p.getItem() instanceof PomkotsDatapadItem) {
            return p;
        }

        return null;
    }

    private ImageButton buildButton(String text, int x, int y, Button.OnPress onPress) {
        return buildButton(text, x, y, onPress, true);
    }

    private ImageButton buildButton(String text, int x, int y, Button.OnPress onPress, boolean active) {
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
