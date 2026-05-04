package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class CommandExecutorBlockEntity extends BlockEntity {
    private boolean executed = false;;
    private String command = ""; // デフォルト

    public CommandExecutorBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.ENTITY_COMMAND_EXECUTOR_BLOCK.get(), pos, state);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, CommandExecutorBlockEntity be) {
        if (be.executed || be.command == null || be.command.isEmpty() || Utils.isMapEditingMode(level)) return;

        // コマンド実行
        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                (ServerLevel) level,
                2, // 権限レベル
                "command_block_once",
                Component.literal("command_block_once"),
                level.getServer(),
                null
        );

        try {
            level.getServer().getCommands().performPrefixedCommand(source, be.command);
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to execute command: {}", be.command, e);
        }

        // 一度だけ実行した印としてフラグを立てる
        be.executed = true;
        be.setChanged();

        // 自壊
        level.removeBlock(pos, false);
    }

    // --- NBT保存・読み込み ---
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(PomkotsMechs.nbtName("CommandExecutorExecuted"), executed);
        if (!command.isEmpty()) tag.putString(PomkotsMechs.nbtName("CommandExecutorCommand"), command);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        executed = tag.getBoolean(PomkotsMechs.nbtName("CommandExecutorExecuted"));
        if (tag.contains(PomkotsMechs.nbtName("CommandExecutorCommand"))) {
            command = tag.getString(PomkotsMechs.nbtName("CommandExecutorCommand"));
        }
    }
}