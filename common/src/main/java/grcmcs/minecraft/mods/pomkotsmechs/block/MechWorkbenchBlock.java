package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.PomkotsSpannerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class MechWorkbenchBlock extends HorizontalDirectionalBlock implements EntityBlock, PomkotsUnbreakableBlock {

    public MechWorkbenchBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(100.0F, 3600000.0F)
                .sound(SoundType.METAL));
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public  BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechWorkbenchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof MechWorkbenchBlockEntity station) {
                MechWorkbenchBlockEntity.serverTick(lvl, pos, st, station);
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected static final VoxelShape SHAPE = Block.box(0.1, 0.1, 0.1, 15.9, 15.9, 15.9);
    public VoxelShape getShape(BlockState bs, BlockGetter bg, BlockPos bp, CollisionContext cc) {
        // 謎にこっちのシェイプの形状で周囲のブロックがカリングされちゃうのでざっくり置いとく
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PomkotsSpannerItem) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            List<Pmvc01Entity> mechs = getMechsBehindWorkbench(level, pos, state);
            if (!mechs.isEmpty()) {
                BlockEntity be = level.getBlockEntity(pos);

                if (be instanceof MechWorkbenchBlockEntity mwbe) {
                    Pmvc01Entity targetMech = mechs.get(0);

                    if (targetMech.isLocked(player)) {
                        return InteractionResult.FAIL;
                    }

                    targetMech.setConsoleAccessor(mwbe);
                    targetMech.openCustomInventoryScreen(player);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
    public List<Pmvc01Entity> getMechsBehindWorkbench(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        AABB searchBox = getBackArea(pos, facing);

        return level.getEntitiesOfClass(Pmvc01Entity.class, searchBox);
    }

    private static AABB getBackArea(BlockPos pos, Direction facing) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        switch (facing) {
            case NORTH:
                return new AABB(x - 5, y - 3, z, x + 5, y + 3, z + 10);
            case SOUTH:
                return new AABB(x - 5, y - 3, z - 10, x + 5, y + 3, z);
            case WEST:
                return new AABB(x, y - 3, z - 5, x + 10, y + 3, z + 5);
            case EAST:
                return new AABB(x - 10, y - 3, z - 5, x, y + 3, z + 5);
            default:
                return new AABB(pos);
        }
    }
}
