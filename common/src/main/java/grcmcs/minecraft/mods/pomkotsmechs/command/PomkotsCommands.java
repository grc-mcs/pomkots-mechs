package grcmcs.minecraft.mods.pomkotsmechs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.CoreStoneBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.UUID;

public class PomkotsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("pomkots:raidstart")
                        .then(Commands.argument("raid_type", StringArgumentType.string())
                                .then(Commands.argument("command_success", StringArgumentType.string())
                                        .then(Commands.argument("command_fail", StringArgumentType.string())
                                        .executes(PomkotsCommands::executeRaid))))
        );

        dispatcher.register(
                Commands.literal("pomkots:register_radartarget")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("label", StringArgumentType.string())
                                        .then(Commands.literal("coord")
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(ctx -> executeCoord(ctx))
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("pomkots:reset_boss_pos")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> resetBossPosition(ctx))
                                )));
    }

    private static int resetBossPosition(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();

            UUID uuid = UuidArgument.getUuid(ctx, "uuid");
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

            ServerLevel level = source.getLevel();

            Entity entity = level.getEntity(uuid);

            if (!(entity instanceof BaseBossEntity boss)) {
                source.sendFailure(Component.literal("Entity not found"));
                return 0;
            }

            boss.teleportTo(
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5
            );

            boss.setActivated(false);
            boss.setAiMode(BaseBossEntity.AI_MODE_INACTIVE);

            source.sendSuccess(() -> Component.literal("Boss moved!"), true);
            return 0;

        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Fail", e);
            return 0;
        }
    }

    private static int executeCoord(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player  = EntityArgument.getPlayer(ctx, "player");
            String label         = StringArgumentType.getString(ctx, "label");
            BlockPos pos         = BlockPosArgument.getBlockPos(ctx, "pos");
            ResourceLocation dim = player.level().dimension().location();

            // インベントリからPomkotsRadarItemを探す
            ItemStack radarStack = findRadarItem(player);
            if (radarStack == null) {
                ctx.getSource().sendFailure(Component.literal(
                        player.getName().getString() + " does not have a Pomkots Data Pad"));
                return 0;
            }

            PomkotsDatapadItem.addTarget(radarStack, new RadarTarget.CoordTarget(pos, label, dim));

            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Registered radar target: " + label + " at " + pos), true);
            return 1;

        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Fail", e);
            return 0;
        }
    }

    private static ItemStack findRadarItem(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PomkotsDatapadItem) return stack;
        }
        return null;
    }

    private static int executeRaid(CommandContext<CommandSourceStack> ctx) {
        String raid_type = StringArgumentType.getString(ctx, "raid_type");
        String command_success = StringArgumentType.getString(ctx, "command_success");
        String command_fail = StringArgumentType.getString(ctx, "command_fail");

        CommandSourceStack source = ctx.getSource();
        Level level = source.getLevel();

        var raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(raid_type);

        if (raidData == null) {
            source.sendFailure(Component.literal("Undefined raid: " + raid_type));

        } else if ("defense".equals(raidData.type)){
            BlockPos origin = BlockPos.containing(source.getPosition());

            int radius = 100;
            double closestDist = Double.MAX_VALUE;
            BlockPos closest = null;
            Block targetBlock = PomkotsMechs.CORE_STONE_BLOCK.get();

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                    origin.offset(radius, radius, radius))) {
                if (level.getBlockState(pos).is(targetBlock)) {
                    double dist = pos.distToCenterSqr(origin.getX(), origin.getY(), origin.getZ());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = pos.immutable();
                    }
                }
            }

            if (closest != null) {
                BlockEntity be = level.getBlockEntity(closest);
                if (be instanceof CoreStoneBlockEntity csbe) {
                    var p = source.getPlayer();
                    if (!csbe.isRaidActive() && p != null) {
                        csbe.startRaid(raid_type, p.getUUID(), command_success, command_fail);
                        csbe.setChanged();
                        source.sendSuccess(() ->
                                Component.literal("Raid Started"), true);
                    } else {
                        source.sendFailure(Component.literal("Another raid is active now."));
                    }
                } else {
                    source.sendFailure(Component.literal("Block has no BlockEntity."));
                }
            } else {
                source.sendFailure(Component.literal("No nearby block of type " + targetBlock + " found."));
            }
        } else if ("survive".equals(raidData.type)) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                BlockPos pos = BlockPos.containing(source.getPosition());
                rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raid_type, source.getPlayer().getUUID(), command_success, command_fail
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);

                source.sendSuccess(() ->
                        Component.literal("Raid Started"), true);
            } else {
                source.sendFailure(Component.literal("Cant spawn raid controller"));
            }
        } else if ("sweep".equals(raidData.type)) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                BlockPos pos = BlockPos.containing(source.getPosition());
                rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raid_type, source.getPlayer().getUUID(), command_success, command_fail
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);

                source.sendSuccess(() ->
                        Component.literal("Raid Started"), true);
            } else {
                source.sendFailure(Component.literal("Cant spawn raid controller"));
            }
        } else if ("activate".equals(raidData.type)) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                BlockPos pos = BlockPos.containing(source.getPosition());
                rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raid_type, source.getPlayer().getUUID(), command_success, command_fail
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);

                source.sendSuccess(() ->
                        Component.literal("Raid Started"), true);
            } else {
                source.sendFailure(Component.literal("Cant spawn raid controller"));
            }
        }  else {
            source.sendFailure(Component.literal("Undefined raid type: " + raidData.type));
        }

        return 1;
    }

}
