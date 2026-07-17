package grcmcs.minecraft.mods.pomkotsmechs.block.migration;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.migration.AssetMigrationManager;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AssetAnchorBlockEntity extends BlockEntity {

    private static final String NBT_ID = PomkotsMechs.nbtName("AssetId");
    private static final String NBT_VERSION = PomkotsMechs.nbtName("AssetVersion");

    private String assetId = "";

    private int assetVersion = 0;

    private boolean migrationChecked = false;

    public AssetAnchorBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                PomkotsMechs.ASSET_ANCHOR_BE.get(),
                pos,
                state
        );
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            AssetAnchorBlockEntity be
    ) {
        if (level.isClientSide) {
            return;
        }

        if (!be.migrationChecked && !Utils.isMapEditingMode(level)) {
            be.migrationChecked = true;

            AssetMigrationManager.queueCheck(
                    be
            );
        }
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(
            String assetId
    ) {
        this.assetId = assetId;
        setChanged();
    }

    public int getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(
            int assetVersion
    ) {
        this.assetVersion = assetVersion;
        setChanged();
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag
    ) {
        super.saveAdditional(tag);

        tag.putString(
                NBT_ID,
                assetId
        );

        tag.putInt(
                NBT_VERSION,
                assetVersion
        );
    }

    @Override
    public void load(
            CompoundTag tag
    ) {
        super.load(tag);

        assetId =
                tag.getString(
                        NBT_ID
                );

        assetVersion =
                tag.getInt(
                        NBT_VERSION
                );
    }
}
