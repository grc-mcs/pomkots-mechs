package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BlockPlacementPreviewEntity extends Entity {
    public BlockPlacementPreviewEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public BlockPlacementPreviewEntity(Level level, Vec3 position) {
        this(PomkotsMechs.BLOCK_PLACEMENT_PREVIEW.get(), level);
        this.setPos(position);
    }

    @Override
    public void tick() {
        setNoGravity(true);
        super.tick();
        this.hasImpulse = true;
//        this.setDeltaMovement(Vec3.ZERO);
    }

    public void placeBlocks(ServerLevel level, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        var stacks = Utils.getItemStacks(player.getMainHandItem(), player, this.getCurrentPattern().size());

        if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.getAbilities().instabuild && stacks == null) {
            return;
        }

        BlockState state = blockItem.getBlock().defaultBlockState();

        int q = this.yawToQuarter(this.getYRot());
        var offset = this.blockPosition();
        int consumedBlockNum = 0;

        for (var bPos: this.getCurrentPattern()){
            var localPos = this.rotate(bPos, q);
            var worldPos = new BlockPos(offset.getX() + localPos.getX(), offset.getY() + localPos.getY(), + offset.getZ() + localPos.getZ());

            // 既に埋まってたら置けない
            if (!level.getBlockState(worldPos).canBeReplaced()) {
                continue;
            }

            // 実際に設置
            if (level.setBlock(worldPos, state, Block.UPDATE_ALL)) {
                consumedBlockNum++;
            }
        }

        if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.getAbilities().instabuild && stacks != null) {
            Utils.consumeItemStackFromTop(stacks, consumedBlockNum);
        }
    }

    protected static final EntityDataAccessor<Integer> PATTERN_INDEX = SynchedEntityData.defineId(BlockPlacementPreviewEntity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PATTERN_INDEX, 0);
    }

    public void incrementPatternIndex() {
        var val = (getPatternIndex() + 1) % BLOCK_TEMPLATES.length;
        setPatternIndex(val);
    }

    public void setPatternIndex(int num) {
        this.entityData.set(PATTERN_INDEX, num);
    }

    public int getPatternIndex() {
        return this.entityData.get(PATTERN_INDEX);
    }

    public List<BlockPos> getCurrentPattern() {
        return BLOCK_TEMPLATES[getPatternIndex()];
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true; // 常に表示
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public int yawToQuarter(float yaw) {
        // 0..360 に正規化
        float rot = (yaw % 360 + 360) % 360;

        // 90度刻みに丸める
        return Mth.floor((rot + 45.0f) / 90.0f) & 3;
    }

    public BlockPos rotate(BlockPos pos, int quarter) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return switch (quarter & 3) {
            case 1 -> new BlockPos(-z, y, x);   // 90°
            case 2 -> new BlockPos(-x, y, -z);  // 180°
            case 3 -> new BlockPos(z, y, -x);   // 270°
            default -> pos;                     // 0°
        };
    }

    public static List<BlockPos> LINE_5X1X1 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int x = -2; x <= 2; x++) {
            BlockPos pos = center
                    .relative(right, x)
                    .relative(back, 0)
                    .relative(up, 0);
            LINE_5X1X1.add(pos);
        }
    }

    public static List<BlockPos> PILLAR_1X5X1 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int y = 0; y <= 4; y++) {
            BlockPos pos = center
                    .relative(right, 0)
                    .relative(back, 0)
                    .relative(up, y);
            PILLAR_1X5X1.add(pos);
        }
    }

    public static List<BlockPos> PILLAR_3X5X3 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 4; y++) {
                    BlockPos pos = center
                            .relative(right, x)
                            .relative(back, z)
                            .relative(up, y);
                    PILLAR_3X5X3.add(pos);
                }
            }
        }
    }

    public static List<BlockPos> WALL_5X5X1 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 4; y++) {
                BlockPos pos = center
                        .relative(right, x)
                        .relative(up, y);
                WALL_5X5X1.add(pos);
            }
        }
    }

    public static List<BlockPos> WALL_NANAME = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 4; y++) {
                    if (x == z) {
                        BlockPos pos = center
                                .relative(right, x)
                                .relative(back, z)
                                .relative(up, y);
                        WALL_NANAME.add(pos);
                    }
                }
            }
        }
    }

    public static List<BlockPos> PLATE_5X1X5 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 0; y++) {
                    BlockPos pos = center
                            .relative(right, x)
                            .relative(back, z)
                            .relative(up, y);
                    PLATE_5X1X5.add(pos);
                }
            }
        }
    }

    public static List<BlockPos> STAIR_3X3X3 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int y = 0; y <= 2; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1 - y; z++) {
                    BlockPos pos = center
                            .relative(right, x)
                            .relative(back, z)
                            .relative(up, y);
                    STAIR_3X3X3.add(pos);
                }
            }
        }
    }

    public static List<BlockPos> CUBE_3X3X3 = new ArrayList<>();
    static {
        Direction right = Direction.EAST;
        Direction back = Direction.SOUTH;
        Direction up = Direction.UP;
        var center = new BlockPos(0,0,0);

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = center
                            .relative(right, x)
                            .relative(back, z)
                            .relative(up, y);
                    CUBE_3X3X3.add(pos);
                }
            }
        }
    }

    public static List<BlockPos>[] BLOCK_TEMPLATES = new List[]{
            LINE_5X1X1,
            PILLAR_1X5X1,
            PILLAR_3X5X3,
            WALL_5X5X1,
            WALL_NANAME,
            PLATE_5X1X5,
            STAIR_3X3X3,
            CUBE_3X3X3
    };
}
