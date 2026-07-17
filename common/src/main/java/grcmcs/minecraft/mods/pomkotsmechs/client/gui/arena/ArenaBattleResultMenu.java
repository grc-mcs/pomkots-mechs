package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitRewardRoller;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ArenaBattleResultMenu extends AbstractContainerMenu {

    private final SimpleContainer rewardsContainer = new SimpleContainer(3);

    private ArenaMatchResultData.FighterResult challengerResult;
    private ArenaMatchResultData.FighterResult opponentResult;

    public ArenaBattleResultMenu(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf buf
    ) {
        this(
                containerId,
                inventory,
                null,
                ArenaMatchResultData.read(
                        buf
                )
        );
    }

    public ArenaBattleResultMenu(
            int containerId,
            Inventory inventory,
            Player player,
            ArenaMatchResultData.Entry entry
    ) {
        super(
                PomkotsMechs.ARENA_RESULT_MENU.get(),
                containerId
        );

        if (entry != null) {
            this.challengerResult =
                    entry.getChallengerResult();

            this.opponentResult =
                    entry.getOpponentResult();
        }

        addSlots(inventory);

        if (player instanceof ServerPlayer) {
            ArenaMatchResultData.FighterResult selfResult = null;
            ArenaMatchResultData.FighterResult targetResult = null;

            var cId = challengerResult.getFighterId();
            var oId = opponentResult.getFighterId();

            if (cId != null && player.getUUID().equals(cId)) {
                selfResult = challengerResult;
                targetResult = opponentResult;
            } else if (oId != null && player.getUUID().equals(oId)) {
                selfResult = opponentResult;
                targetResult = challengerResult;
            }

            if (selfResult == null || targetResult == null) {
                return;
            }

            int baseReward = selfResult.getRatingAfter() - selfResult.getRatingBefore();
            baseReward = Mth.clamp(baseReward,1,64);

            int rankReward = getRankReward(ArenaRank.getRank(selfResult.getRankAfter()));

            this.slots.get(0).set(new ItemStack(PomkotsMechs.POM_COIN.get(), baseReward));
            this.slots.get(1).set(new ItemStack(PomkotsMechs.POM_COIN.get(), rankReward));

            CircuitRewardRoller.WeightedEntry<CircuitRarity>[] cRarity = switch (ArenaRank.getRank(targetResult.getRankBefore())) {
                case E, D, C -> CircuitRewardRoller.RARITY_WEIGHTS_ARENA_C_D;
                case B, A -> CircuitRewardRoller.RARITY_WEIGHTS_ARENA_A_B;
                case S, SS -> CircuitRewardRoller.RARITY_WEIGHTS_ARENA_SS_S;
            };

            if (selfResult.getWinsAfter() > selfResult.getWinsBefore()) {
                var circuit = CircuitRewardRoller.roll(CircuitRewardRoller.PREFIX_WEIGHTS_ARENA, cRarity, 1, player.getRandom());
                if (!circuit.isEmpty()) {
                    this.slots.get(2).set(circuit);
                }
            }
        }
    }

    private int getRankReward(ArenaRank rank) {
        return switch (rank) {
            case E -> 0;
            case D -> 0;
            case C -> 4;
            case B -> 8;
            case A -> 16;
            case S -> 24;
            case SS -> 32;
        };
    }

    private void addSlots(Inventory playerInventory) {
        addRewardSlots();
        addPlayerInventory(playerInventory);
    }

    private void addRewardSlots() {
        int offsetX = 20 + 28 + 25;
        int offsetY = 20 + 130 + 30;

        for (int i = 0; i < rewardsContainer.items.size(); i++) {
            this.addSlot(new Slot(rewardsContainer, i, offsetX, offsetY + i * 18));
        }
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {
        int offsetX = 20 + 28 + 164 + 28 + 28 + 2;
        int offsetY = 20 + 130 + 26;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 3 * 18 + 4));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return true;
    }

    public ArenaMatchResultData.FighterResult getChallengerResult() {
        return challengerResult;
    }

    public ArenaMatchResultData.FighterResult getOpponentResult() {
        return opponentResult;
    }
}
