package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PersonalPomkotsCubeBlock extends ChestBlock implements PomkotsUnbreakableBlock {
    public PersonalPomkotsCubeBlock() {
        this(Properties.of()
                .strength(-1.0F, 3600000.0F)
                .pushReaction(PushReaction.BLOCK)
                .sound(SoundType.METAL), PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY::get);
    }

    public PersonalPomkotsCubeBlock(Properties properties,
                                    Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TYPE, ChestType.SINGLE)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity) {
            blockEntity.setOwnerIfAbsent(player.getUUID());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        // Block interaction runs before Item#useOn when the player is not sneaking.
        // Let the spanner handle this click instead of opening the chest menu.
        if (player.getItemInHand(hand).is(PomkotsMechs.SPANNER_ITEM.get())) {
            return InteractionResult.PASS;
        }

        if (level.getBlockEntity(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity
                && !blockEntity.isOwner(player)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                        "text.pomkotsmechs.messages.personal_pomkots_cube.not_owner"));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PersonalPomkotsCubeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, this.blockEntityType(), ChestBlockEntity::lidAnimateTick)
                : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
