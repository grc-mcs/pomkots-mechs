package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PartsWorkbenchMenu extends AbstractContainerMenu {
    private Tab currentTab = Tab.CRAFT;
    private final Container container;

    public PartsWorkbenchMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(4));
    }

    public PartsWorkbenchMenu(int id, Inventory playerInv, Container inventory) {
        super(PomkotsMechs.PARTS_WORKBENCH_GUI.get(), id);
        this.container = inventory;

        setupSlotsForCurrentTab(playerInv);
    }

    private void setupSlotsForCurrentTab(Inventory playerInv) {
        // クラフト時の出力スロット
        this.addSlot(new ConditionalSlot(container, 0, 10, 142, () -> currentTab == Tab.CRAFT){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // プレイヤーは置けない
            }
        });

        // 強化時の入出力スロット
        this.addSlot(new ConditionalSlot(container, 1, 10, 142, () -> currentTab == Tab.UPGRADE));

        // プレイヤーインベントリのスロット
        int offsetX = 284;
        int offsetY = 45;

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                this.addSlot(new ConditionalSlot(playerInv, col + row * 6, offsetX + col * 18, offsetY + row * 18, () -> true));
            }
        }
    }

    public void craftItem(Item target, ServerPlayer player) {
        if (target instanceof BasePartsItem parts && getSlot(0).getItem().isEmpty()) {
            if (!getSlot(0).getItem().isEmpty()) {
                sendMessage(player,"{text.pomkotsmechs.messages.partsworkbench.01}");
                return;
            }

            var partsData = getPartsData(parts);

            if (partsData == null || partsData.recipes.isEmpty() || partsData.recipes.get(0).isEmpty()) {
                sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.02}");
                return;
            }

            if (PartsWorkbenchMenu.isCraftable(player.getInventory(), partsData.recipes.get(0))) {
                ItemStack crafted = new ItemStack(parts);
                parts.setLevel(crafted, 1);

                consumeMaterials(player.getInventory(), partsData.recipes.get(0));

                this.getSlot(0).set(crafted);

            } else {
                sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.03}");

            }
        }
    }

    public void sendMessage(ServerPlayer player, String msg) {
        player.sendSystemMessage(Utils.string2Component(msg));
    }

    public void setTab(Tab tab) {
        if (tab != this.currentTab) {
            this.currentTab = tab;
        }
    }

    public Tab getCurrentTab() {
        return currentTab;
    }

    public void upgradeParts(ServerPlayer player) {
        var inputItemStack = this.getSlot(1).getItem();

        if (inputItemStack.isEmpty()) {
            sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.04}");

        } else if (inputItemStack.getItem() instanceof BasePartsItem parts) {
            var partsData = PartsWorkbenchMenu.getPartsData(parts);
            var curLevel = parts.getLevel(inputItemStack);

            if (partsData == null) {
                sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.05}");
                return;
            } else if (parts.getMaxLevel() <= curLevel) {
                sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.06}");
                return;
            } else if (partsData.recipes.isEmpty() || partsData.recipes.size() < curLevel || partsData.recipes.get(curLevel).isEmpty()) {
                sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.02}");
                return;
            }

            var recipe = partsData.recipes.get(curLevel);
            if (PartsWorkbenchMenu.isCraftable(player.getInventory(), recipe)) {
                var newItemStack = inputItemStack.copy();
                parts.setLevel(newItemStack, curLevel + 1);

                consumeMaterials(player.getInventory(), recipe);

                this.getSlot(1).getItem().shrink(1);
                this.getSlot(1).set(newItemStack);

            } else {
                sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.03}");
            }
        } else {
            sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.07}");
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public enum Tab {
        CRAFT(0),
        UPGRADE(1);

        private final int id;

        private Tab(final int id) {
            this.id = id;
        }

        public int getInt() {
            return this.id;
        }
    }

    public static Tab getTab(final int id) {
        Tab[] types = Tab.values();
        for (Tab tab : types) {
            if (tab.getInt() == id) {
                return tab;
            }
        }
        return null;
    }

    class ConditionalSlot extends Slot {
        private final Supplier<Boolean> activeCondition;

        public ConditionalSlot(Container container, int index, int x, int y, Supplier<Boolean> condition) {
            super(container, index, x, y);
            this.activeCondition = condition;
        }

        @Override
        public boolean mayPickup(Player player) {
            return activeCondition.get() && super.mayPickup(player);
        }

        @Override
        public boolean isActive() {
            return activeCondition.get();
        }
    }

    public static PomkotsDataPack.PartsData getPartsData(BasePartsItem partsItem) {
        var dataPack = PomkotsDataPackManager.getInstance().getDataPack();
        if (partsItem instanceof BasePartsItem.Arm
                || partsItem instanceof BasePartsItem.Head
                || partsItem instanceof BasePartsItem.Body
                || partsItem instanceof BasePartsItem.Legs) {
            return dataPack.getPartsData(partsItem.getPartsSeriesName() + partsItem.getPartsCategory().getString());
        } else {
            return dataPack.getPartsData(partsItem.getPartsSeriesName());
        }
    }

    public static boolean isCraftable(Inventory inv, List<PomkotsDataPack.SerializablePair<String, Integer>> recipe) {
        for (var entry : recipe) {
            ResourceLocation id = new ResourceLocation(entry.getFirst());
            Item requiredItem = BuiltInRegistries.ITEM.get(id);

            if (requiredItem == Items.AIR) {
                return false;
            }

            if (inv.countItem(requiredItem) < entry.getSecond()) {
                return false;
            }
        }
        return true;
    }

    public static boolean consumeMaterials(Inventory inv, List<PomkotsDataPack.SerializablePair<String, Integer>> recipe) {
        for (var entry : recipe) {
            ResourceLocation id = new ResourceLocation(entry.getFirst());
            Item requiredItem = BuiltInRegistries.ITEM.get(id);

            if (requiredItem == Items.AIR) {
                return false;
            }

            int remaining = entry.getSecond();

            for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(requiredItem)) {
                    int take = Math.min(stack.getCount(), remaining);
                    stack.shrink(take);
                    remaining -= take;
                }
            }
        }

        return true;
    }
}
