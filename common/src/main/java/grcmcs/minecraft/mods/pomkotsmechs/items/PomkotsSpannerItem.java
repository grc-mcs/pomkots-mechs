package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.block.MechSalvagerBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.PartsWorkbenchBlock;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PomkotsSpannerItem extends Item {

    public PomkotsSpannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {

        Level level = ctx.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = ctx.getPlayer();

        // 対象ブロック判定
        var block = state.getBlock();
        if (block instanceof PersonalPomkotsCubeBlock) {
            return retrievePersonalCube(ctx, level, pos, state, player);
        }

        if (!(
                block instanceof MechSalvagerBlock
                || block instanceof MechWorkbenchBlock
                || block instanceof PartsWorkbenchBlock
        )) {
            return InteractionResult.PASS;
        }

        // ===== コンテナ中身ドロップ =====

        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }

        // ===== ブロックアイテム化 =====

        Block.popResource(level, pos,
                new ItemStack(state.getBlock()));

        // ===== ブロック削除 =====

        level.removeBlock(pos, false);

        // ===== 効果音 =====

        level.playSound(
                null,
                pos,
                SoundEvents.ANVIL_BREAK,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        // ===== 耐久消費 =====

        if (player != null && !player.getAbilities().instabuild) {

            ctx.getItemInHand().hurtAndBreak(
                    1,
                    player,
                    p -> p.broadcastBreakEvent(ctx.getHand())
            );
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult retrievePersonalCube(UseOnContext ctx, Level level, BlockPos pos,
                                                    BlockState state, Player player) {
        if (!(level.getBlockEntity(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity)) {
            return InteractionResult.FAIL;
        }
        if (player == null || !blockEntity.isOwner(player)) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable(
                        "text.pomkotsmechs.messages.personal_pomkots_cube.not_owner"));
            }
            return InteractionResult.CONSUME;
        }

        ItemStack retrievedCube = new ItemStack(PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ITEM.get());
        BlockItem.setBlockEntityData(
                retrievedCube,
                PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(),
                blockEntity.saveWithoutMetadata()
        );

        // The saved copy is now authoritative. Clear the source before removal so its contents
        // cannot also be dropped by the block removal lifecycle.
        blockEntity.clearContent();
        Block.popResource(level, pos, retrievedCube);
        level.removeBlock(pos, false);

        level.playSound(null, pos, SoundEvents.ANVIL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            ctx.getItemInHand().hurtAndBreak(
                    1,
                    player,
                    p -> p.broadcastBreakEvent(ctx.getHand())
            );
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(
                "tooltip.pomkotsmechs.pomkots_spanner"
        ).withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
