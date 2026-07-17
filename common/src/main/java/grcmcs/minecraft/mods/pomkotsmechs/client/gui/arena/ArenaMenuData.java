package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArenaMenuData {

    public static class Entry {
        private String arenaId;
        private List<ArenaRankingEntry> rankings;
        private int mode;
        private ItemStack mechDataPadStack;

        public Entry(String arenaId, int mode, ItemStack mechDataPadStack, List<ArenaRankingEntry> rankings) {
            this.arenaId = arenaId;
            this.mode = mode;
            this.mechDataPadStack = mechDataPadStack;
            this.rankings = rankings;
        }

        public String getArenaId() {
            return arenaId;
        }

        public ItemStack getMechDataPadStack() {
            return mechDataPadStack;
        }

        public int getMode() {
            return mode;
        }

        public List<ArenaRankingEntry> getRankings() {
            return rankings;
        }
    }

    public static void write(
            FriendlyByteBuf buf,
            Entry entry
    ){
        buf.writeUtf(entry.arenaId);
        buf.writeInt(entry.mode);
        buf.writeItem(Objects.requireNonNullElse(entry.mechDataPadStack, ItemStack.EMPTY));

        writeRankings(buf, entry.rankings);
    }

    private static void writeRankings(
            FriendlyByteBuf buf,
            List<ArenaRankingEntry> rankings
    ) {
        buf.writeInt(rankings.size());

        for (ArenaRankingEntry entry : rankings) {
            buf.writeUUID(
                    entry.getFighterId()
            );

            buf.writeUtf(
                    entry.getDisplayName()
            );

            buf.writeInt(
                    entry.getRating()
            );

            buf.writeUtf(
                    entry.getComment()
            );

            buf.writeUtf(
                    entry.getMechName()
            );

            buf.writeInt(
                    entry.getWins()
            );

            buf.writeInt(
                    entry.getLosses()
            );

            buf.writeInt(
                    entry.getType()
            );

            buf.writeBoolean(
                    entry.isOnline()
            );
        }
    }

    public static Entry read(
            FriendlyByteBuf buf
    ) {
        String arenaId = buf.readUtf();
        int mode = buf.readInt();
        ItemStack mechDataPackStack = buf.readItem();

        int size = buf.readInt();

        List<ArenaRankingEntry> result =
                new ArrayList<>();

        for (int i = 0; i < size; i++) {

            ArenaRankingEntry entry =
                    new ArenaRankingEntry();

            entry.setFighterId(
                    buf.readUUID()
            );

            entry.setDisplayName(
                    buf.readUtf()
            );

            entry.setRating(
                    buf.readInt()
            );

            entry.setComment(
                    buf.readUtf()
            );

            entry.setMechName(
                    buf.readUtf()
            );

            entry.setWins(
                    buf.readInt()
            );

            entry.setLosses(
                    buf.readInt()
            );

            entry.setType(
                    buf.readInt()
            );

            entry.setOnline(
                    buf.readBoolean()
            );

            result.add(entry);
        }

        return new Entry(arenaId, mode, mechDataPackStack, result);
    }
}
