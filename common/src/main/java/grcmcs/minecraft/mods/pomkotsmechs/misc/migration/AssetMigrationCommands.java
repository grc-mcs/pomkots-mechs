package grcmcs.minecraft.mods.pomkotsmechs.misc.migration;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.migration.AssetAnchorBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class AssetMigrationCommands {

    private AssetMigrationCommands() {}

    // =====================================================
    // Source
    // =====================================================

    private static CommandSourceStack createCommandSource(
            AssetAnchorBlockEntity anchor
    ) {
        if (!(anchor.getLevel() instanceof ServerLevel level)) {
            return null;
        }

        MinecraftServer server =
                level.getServer();

        return server.createCommandSourceStack()
                .withLevel(level)
                .withPosition(
                        Vec3.atCenterOf(
                                anchor.getBlockPos()
                        )
                )
                .withPermission(4)
                .withSuppressedOutput();
    }

    // =====================================================
    // Execute
    // =====================================================

    public static int executeCommand(
            AssetAnchorBlockEntity anchor,
            String command
    ) {
        if (!(anchor.getLevel() instanceof ServerLevel level)) {
            return -1;
        }

        CommandSourceStack source =
                createCommandSource(anchor);

        if (source == null) {
            return -1;
        }

        try {

            int result =
                    level.getServer()
                            .getCommands()
                            .performPrefixedCommand(
                                    source,
                                    command
                            );

            PomkotsMechs.LOGGER.info(
                    "[Asset] Executed: {} (result={})",
                    command,
                    result
            );

            return result;

        } catch (Exception e) {

            PomkotsMechs.LOGGER.error(
                    "[Asset] Command exception: {}",
                    command,
                    e
            );

            return -1;
        }
    }

    // =====================================================
    // Boolean wrapper
    // =====================================================

    public static boolean runCommand(
            AssetAnchorBlockEntity anchor,
            String command
    ) {
        int result =
                executeCommand(
                        anchor,
                        command
                );

        if (result <= 0) {
            PomkotsMechs.LOGGER.error(
                    "[Asset] Command failed: {}",
                    command
            );

            return false;
        }

        return true;
    }

    // =====================================================
    // Function wrapper
    // =====================================================

    public static boolean runFunction(
            AssetAnchorBlockEntity anchor,
            String functionId
    ) {
        return runCommand(
                anchor,
                "function " + functionId
        );
    }
}