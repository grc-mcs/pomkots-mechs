package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class PomkotsCubeBlock extends ChestBlock implements PomkotsUnbreakableBlock {

    public PomkotsCubeBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.METAL), PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY::get);
    }

    public PomkotsCubeBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(properties, supplier);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext arg) {
        ChestType chesttype = ChestType.SINGLE;
        Direction direction = arg.getHorizontalDirection().getOpposite();
        FluidState fluidstate = arg.getLevel().getFluidState(arg.getClickedPos());
        boolean flag = arg.isSecondaryUseActive();
        Direction direction1 = arg.getClickedFace();
        if (direction1.getAxis().isHorizontal() && flag) {
            Direction direction2 = this.candidatePartnerFacing(arg, direction1.getOpposite());
            if (direction2 != null && direction2.getAxis() != direction1.getAxis()) {
                direction = direction2;
                chesttype = direction2.getCounterClockWise() == direction1.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }

        if (chesttype == ChestType.SINGLE && !flag) {
            if (direction == this.candidatePartnerFacing(arg, direction.getClockWise())) {
                chesttype = ChestType.LEFT;
            } else if (direction == this.candidatePartnerFacing(arg, direction.getCounterClockWise())) {
                chesttype = ChestType.RIGHT;
            }
        }

        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(TYPE, chesttype)).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    private Direction candidatePartnerFacing(BlockPlaceContext arg, Direction arg2) {
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PomkotsCubeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) :
                createTickerHelper(blockEntityType, PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), PomkotsCubeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof PomkotsCubeBlockEntity cubeBe) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    cubeBe.incrementMode();
                }
                return InteractionResult.SUCCESS;
            } else {
                switch (cubeBe.getMode()) {
                    case PomkotsCubeBlockEntity.MODE_BLUE:
                        cubeBe.openBox(player);
                        return super.use(state, level, pos, player, hand, hit);
                    default:
                        cubeBe.openBox(player);
                        return InteractionResult.CONSUME;
                }
            }
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
