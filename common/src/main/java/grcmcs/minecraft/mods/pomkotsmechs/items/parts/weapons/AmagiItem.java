package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.AmagiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.LinkedList;
import java.util.List;

public class AmagiItem extends BasePartsItem.WeaponArm {
    public AmagiItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);

    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var level = mechInterface.getWorld();

        if (isOnFire && !level.isClientSide) {
            var mech = mechInterface.getMechEntity();
            double forwardX = -Math.sin(Math.toRadians(mech.getYRot()));
            double forwardZ = Math.cos(Math.toRadians(mech.getYRot()));

            // 前方5マスのオフセットを計算
            BlockPos origin = mech.blockPosition().offset(
                    (int) (forwardX * 5),
                    0,
                    (int) (forwardZ * 5)
            );

            // 範囲を指定してブロックを破壊
            int rangeX = 5; // x方向の範囲
            int rangeY = 15; // y方向の範囲
            int rangeZ = 5; // z方向の範囲

            // 張り替えモード
            var driver = mech.getDrivingPassenger();
            if (driver instanceof Player player) {
                BlockState blockState = getMainHandBlock(player);

                if (blockState != null && isAvailableBlock(blockState)) {
                    var blockToPlace = blockState.getBlock();
                    var stacks = Utils.getItemStacks(player.getMainHandItem(), player, rangeX * rangeZ);

                    if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null || player.getAbilities().instabuild) {
                        int consumedBlocks = 0;
                        for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                            for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                                BlockPos targetPos = origin.offset(x, -1, z);
                                BlockPos a = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                var bs = level.getBlockState(a);

                                if (!blockState.equals(bs)) {
                                    Utils.setBlock(level, a, blockToPlace.defaultBlockState(), 3);
                                    consumedBlocks++;
                                }
                            }
                        }
                        if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.getAbilities().instabuild && stacks != null) {
                            Utils.consumeItemStackFromTop(stacks, consumedBlocks);
                        }
                    }
                }
            }
        }
    }

    private BlockState getMainHandBlock(LivingEntity player) {
        if (player != null && player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return null;
    }

    private boolean isAvailableBlock(BlockState bs) {
        return !(bs.getBlock() instanceof ShulkerBoxBlock);
    }

    @Override
    public void startUsing(ActionWeapon.WeaponMechInterface context) {
        if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
            triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel) context.getWorld()), "Activation", "use");
        }
    }

    @Override
    public void endUsing(ActionWeapon.WeaponMechInterface context) {
        if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
            triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel) context.getWorld()), "Activation", "stop");
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
        super.inventoryTick(stack, level, ent, slotIndex, bl);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0, state -> PlayState.STOP)
                .triggerableAnim("use", RawAnimation.begin().thenPlay("animation.weapon.use").thenLoop("animation.weapon.use_loop"))
                .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.weapon.idle"))
        );
    }

    @Override
    public AmagiItemRenderer newRenderer() {
        return new AmagiItemRenderer();
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_ARM;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.MISC;
    }

    @Override
    public Motion getMotion() {
        return Motion.ROAD_ROLLER;
    }

    @Override
    public int getCoolTime() {
        return 60;
    }

    @Override
    public String getPartsSeriesName() {
        return "amagi";
    }
}
