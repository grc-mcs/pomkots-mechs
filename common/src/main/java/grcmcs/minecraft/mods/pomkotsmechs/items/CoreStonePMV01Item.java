package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CoreStonePMV01Item extends Item {
    // 使用済みフラグのキー
    private static final String USED_KEY = PomkotsMechs.MODID + ":corestone:used";

    public CoreStonePMV01Item(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag nbt = stack.getOrCreateTag();

        // 1回目の使用：ロボット召喚
        if (!nbt.getBoolean(USED_KEY)) {
            if (!world.isClientSide) { // サーバー側のみで実行
                summonRobot(world, player);
            }
            nbt.putBoolean(USED_KEY, true); // 使用済みに設定
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
        } else {
            return InteractionResultHolder.fail(stack); // ロボットがいない場合は失敗
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof Pmv01Entity robot) {
            // ロボットの体力を回復
            robot.heal(20.0F);
            robot.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1, 3)); // 回復エフェクト

            return false;
        }
        return super.hurtEnemy(stack, target, attacker);
    }
    // ロボットを召喚するメソッド
    private void summonRobot(Level world, Player player) {
        Pmv01Entity robot = createInstance(world);
        robot.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        world.addFreshEntity(robot);
    }

    protected Pmv01Entity createInstance(Level world) {
        return new Pmv01Entity(PomkotsMechs.PMV01.get(), world);
    }
}
