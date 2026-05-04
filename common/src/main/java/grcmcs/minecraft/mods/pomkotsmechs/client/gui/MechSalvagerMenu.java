package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.datafixers.util.Either;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechSalvagerBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.save.PomkotsMechsSaveData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MechSalvagerMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerLevelAccess access; // これを保持する！

    public MechSalvagerMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(2), null);
    }

    public MechSalvagerMenu(int id, Inventory playerInv, Container inventory, ContainerLevelAccess access) {
        super(PomkotsMechs.MECH_SALVAGER_GUI.get(), id);
        this.inventory = inventory;
        this.access = access;

        // 仕様アイテムスロット
        this.addSlot(new Slot(inventory, 0, 8, 37));
        this.addSlot(new Slot(inventory, 1, 26, 37));

        int offsetX = 8; // 左寄せ調整（通常44→8）
        int offsetY = 106; // ⚠️ GUIを下にずらす (4行分)

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, offsetX + col * 18, 79 + row * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, offsetX + i * 18, 137));
        }
    }

    public void summon(ServerPlayer player) {
        if (inventory instanceof MechSalvagerBlockEntity msbe) {
            msbe.summon(player);
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

}
