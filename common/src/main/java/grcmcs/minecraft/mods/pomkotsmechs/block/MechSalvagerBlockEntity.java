package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.save.PomkotsMechsSaveData;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MechSalvagerBlockEntity extends BlockEntity implements MenuProvider, Container {
    private static final int PAYMENT_AMOUNT = 10;
    private static final int MAX_SUMMON_TICK = 60;
    private int summonTick = 0;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public MechSalvagerBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.MECH_SALVAGER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Mech Salvager");
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int index) { return items.get(index); }
    @Override public ItemStack removeItem(int index, int count) { return ContainerHelper.removeItem(items, index, count); }
    @Override public ItemStack removeItemNoUpdate(int index) { return ContainerHelper.takeItem(items, index); }
    @Override public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }
    @Override public void clearContent() { items.clear(); }
    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64.0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new MechSalvagerMenu(id, playerInv, this, ContainerLevelAccess.create(level, this.getBlockPos()));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MechSalvagerBlockEntity be) {
        be.tick();
    }

    public void tick() {
        if (this.level instanceof ServerLevel) {
            if (this.summonTick == 0) {
                return ;
            } else {
                this.checkSummonMech();
                this.summonTick--;
            }
        }
    }

    private LoadTicket ticket = null;
    private ServerLevel sourceLevel = null;
    private ServerPlayer targetPlayer = null;
    private Vec3 targetPosition = null;
    private UUID targetUUID = null;

    public void checkSummonMech() {
        ItemStack payment = this.getItem(1);
        var e = sourceLevel.getEntity(targetUUID);

        if (e != null) {
            if (payment.getCount() > PAYMENT_AMOUNT && payment.is(PomkotsMechs.POM_COIN.get())) {
                sendMessage(targetPlayer, "{text.pomkotsmechs.messages.mechsalvager.01}");
                if (teleportMech(targetPlayer, e, (ServerLevel)this.level, targetPosition)) {
                    payment.shrink(PAYMENT_AMOUNT);
                }

            } else {
                sendMessage(targetPlayer, "{text.pomkotsmechs.messages.mechsalvager.02}");
            }

            summonTick = 1;
        }

        if (summonTick == 1) {
            sourceLevel.getChunkSource().removeRegionTicket(
                    ticket.ticketType, ticket.chunkPos, ticket.i, ticket.chunkPos
            );
            clearContext();
        }
    }

    private void clearContext() {
        ticket = null;
        sourceLevel = null;
        targetPosition = null;
        targetUUID = null;
        targetPlayer = null;
    }

    public void summon(ServerPlayer player) {
        if (this.summonTick > 0) {
            return;
        }

        ItemStack keyCard = this.getItem(0);
        ItemStack payment = this.getItem(1);

        if (!keyCard.hasTag() || !keyCard.getTag().contains(KeycardItem.NBT_MECH_UUID)) {
            sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.03}");
            return;
        }

        // 代金チェック
        if (payment.getCount() < PAYMENT_AMOUNT || !payment.is(PomkotsMechs.POM_COIN.get())) {
            sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.02}");
            return;
        }

        // 引数展開
        UUID uuid = keyCard.getTag().getUUID(KeycardItem.NBT_MECH_UUID);
        ServerLevel level = player.serverLevel();
        MinecraftServer server = level.getServer();
        Vec3 targetPos = getTargetPos();

        // Mech召喚
        summonMech(player, server, uuid, level, targetPos, payment);
    }

    public void summonMech(ServerPlayer player, MinecraftServer server, UUID mechId, ServerLevel targetLevel, Vec3 summonPos, ItemStack payment) {
        // --- チャンクにロードされてる場合は即召喚 ---
        for (ServerLevel lvl : server.getAllLevels()) {
            Entity e = lvl.getEntity(mechId);
            if (e != null) {
                if (teleportMech(player, e, targetLevel, summonPos)) {
                    payment.shrink(PAYMENT_AMOUNT);
                }
                return;
            }
        }

        // --- チャンクにロードされてない場合、セーブデータをみて座標を確認する ---
        PomkotsMechsSaveData data = PomkotsMechsSaveData.get(server.overworld());
        PomkotsMechsSaveData.MechLocation info = data.get(mechId);
        if (info == null) {
            sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.04}");
            return;
        }

        // ディメンションも確認する
        ServerLevel sourceLevel = server.getLevel(info.dimension());
        if (sourceLevel == null) {
            sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.05}");
            return;
        }

        // 対象チャンクのロードを依頼
        this.ticket = createTicket(info.chunkPos());
        sourceLevel.getChunkSource().addRegionTicket(
                ticket.ticketType, ticket.chunkPos, ticket.i, ticket.chunkPos
        );

        sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.06}");

        this.targetPosition = summonPos;
        this.sourceLevel = sourceLevel;
        this.summonTick = MAX_SUMMON_TICK;
        this.targetUUID = mechId;
        this.targetPlayer = player;
    }

    public Vec3 getTargetPos() {
        BlockPos pos = this.getBlockPos();
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        var offset = switch (facing) {
            case NORTH -> new Vec3(0, 0, -2);
            case SOUTH -> new Vec3(0, 0, 2);
            case WEST -> new Vec3(-2, 0, 0);
            case EAST -> new Vec3(2, 0, 0);
            default -> Vec3.ZERO;
        };

        return Vec3.atCenterOf(pos).add(offset);
    }

    private boolean teleportMech(ServerPlayer player, Entity mech, ServerLevel targetLevel, Vec3 pos) {
        if (mech.level() != targetLevel) {
            sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.07}");
            return false;
        }
        mech.teleportTo(pos.x() + 0.5, pos.y(), pos.z() + 0.5);
        mech.setDeltaMovement(Vec3.ZERO);

        if (mech instanceof Pmvc01Entity pmvc) {
            pmvc.warpEffect(targetLevel);
        }

        sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.08}");

        return true;
    }

    public void sendMessage(ServerPlayer player, String msg) {
        player.sendSystemMessage(Utils.string2Component(msg));
    }

    private LoadTicket createTicket(ChunkPos pos) {
        var ticket = new LoadTicket();
        ticket.ticketType = TicketType.FORCED;
        ticket.chunkPos = pos;
        ticket.i = 3;

        return ticket;
    }

    private static class LoadTicket {
        TicketType<ChunkPos> ticketType;
        ChunkPos chunkPos;
        int i;
    }
}
