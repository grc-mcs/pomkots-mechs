package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.BaseTurretEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt01Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Turret01Item extends Item {
    // 使用済みフラグのキー
    private static final String USED_KEY = PomkotsMechs.MODID + ":corestone:used";

    public Turret01Item(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1回目の使用：ロボット召喚
        if (!world.isClientSide) { // サーバー側のみで実行
            summonRobot(stack, world, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    private void summonRobot(ItemStack stack, Level world, Player player) {
        double distance = 20.0D;

        // サーバ側でもクライアント側でもOK
        HitResult result = player.pick(distance, 0.0F, false);

        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) result;
            BlockPos hitPos = blockHit.getBlockPos();

            BlockState state = player.level().getBlockState(hitPos);
            Block block = state.getBlock();

            // ネザライトブロック判定
            if (block == Blocks.NETHERITE_BLOCK) {
                Pmt01Entity robot = createInstance(world);
                robot.moveTo(hitPos.getX() + 0.5, hitPos.getY() + 1, hitPos.getZ() + 0.5, player.getYRot(), player.getXRot());
                world.addFreshEntity(robot);
                robot.setTargetEntityType(BaseTurretEntity.TargetEntityType.POMKOTS_MONSTERS);
                stack.shrink(1);
            }
        }
    }

    protected Pmt01Entity createInstance(Level world) {
        return new Pmt01Entity(PomkotsMechs.PMT01.get(), world);
    }
}
