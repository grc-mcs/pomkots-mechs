package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMenuData;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaRankingEntry;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMechData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class DataPadMenu
        extends AbstractContainerMenu {

    private final SimpleContainer dataPadContainer = new SimpleContainer(1);
    public Player player;
    private boolean stillVallid = true;
    private ItemStack dataPadStack;

    /**
     * MenuType用
     */
    public DataPadMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                null,
                null
        );
    }

    public DataPadMenu(
            int containerId,
            Inventory playerInventory,
            Player p,
            ItemStack datapad
    ) {
        super(
                PomkotsMechs.POMKOTS_DATAPAD_GUI.get(),
                containerId
        );

        if (p == null) {
            player = playerInventory.player;
        } else {
            player = p;
        }

        this.dataPadStack = datapad;

        this.addSlot(new Slot(dataPadContainer, 0, 0, 0) {
            public boolean isActive() {
                return false;
            }
        });

        if (player instanceof ServerPlayer sp && this.dataPadStack != null) {
            for (var req: PomkotsDatapadItem.getArenaRequests(this.dataPadStack)) {
                var match = ArenaManager.getActiveMatch(req.arenaId(), req.requestUuid(), sp.getServer());
                if (match == null) {
                    PomkotsDatapadItem.removeArenaRequest(datapad, req.requestUuid());
                }
            }
            this.getSlot(0).set(this.dataPadStack);
        }
    }

    public ItemStack getDataPad() {
        return this.getSlot(0).getItem();
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return stillVallid;
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slot
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(
            Player player,
            int id
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            switch (id) {
                case 0 -> {
                    var mode = MechWorkbenchMenu.MODE_VIEW;

                    if (serverPlayer.getVehicle() instanceof Pmvc01Entity mech) {
                        player.openMenu(
                                new SimpleMenuProvider(
                                        (i, inventory, p)-> {
                                            var mechMenu = new MechWorkbenchMenu(i, inventory, mech, mech, mode, null, true);

                                            mechMenu.setEntityId(mech.getUUID().hashCode());
                                            mechMenu.setTextureColor(mech.getTextureColor());
                                            mechMenu.setMode(mode);
                                            mechMenu.sendAllDataToRemote();

                                            return mechMenu;
                                        },
                                        Component.literal("Mech WorkBench")
                                )
                        );
                    }
                }
                case 1 -> {
                    if (dataPadStack != null) {
                        player.openMenu(
                                new SimpleMenuProvider(
                                        (i, inv, p) ->
                                                new DataPadKeyCardMenu(
                                                        i,
                                                        inv,
                                                        dataPadStack),
                                        Component.literal("Pomkots Datapad")
                                )
                        );
                    }
                }
                case 2 -> {
                    var extendedData = new ArenaMenuData.Entry(
                            "default",
                            ArenaReceptionistMenu.MODE_DATAPAD,
                            dataPadStack,
                            ArenaManager.buildRanking(
                                    serverPlayer.server,
                                    "default"
                            )
                    );

                    MenuRegistry.openExtendedMenu(
                            serverPlayer,

                            new SimpleMenuProvider(
                                    (i, container, p)-> new ArenaReceptionistMenu(
                                            i,
                                            container,
                                            extendedData
                                    ),
                                    Component.literal(
                                            "Arena Reception"
                                    )
                            ),

                            buf -> {
                                ArenaMenuData.write(
                                        buf,
                                        extendedData
                                );

                            }
                    );
                }
                case 3 -> {
                    player.openMenu(
                            new SimpleMenuProvider(
                                    (i, container, p)-> new RadarTargetSelectMenu(
                                            i,
                                            container,
                                            p,
                                            true
                                    ),
                                    Component.literal("Pomkots Radar")
                            )
                    );
                }
                case 4 -> {
                    serverPlayer.closeContainer();
                    serverPlayer.server.execute(
                            () -> serverPlayer.server.getCommands()
                                    .performPrefixedCommand(
                                            serverPlayer.createCommandSourceStack(),
                                            "ftbquests open_book"
                                    )
                    );
                }
                case 5 -> {
//                    MinecraftServer server = serverPlayer.getServer();
//                    CommandSourceStack source = player.createCommandSourceStack()
//                            .withPermission(2);
//                    server.getCommands().performPrefixedCommand(source, "ftbquests open_book");
                }
            }
        }

        return true;
    }

    private void sendMessage(String msg, Player player) {
        player.sendSystemMessage(
                Component.literal(
                        msg
                )
        );
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);
    }
}