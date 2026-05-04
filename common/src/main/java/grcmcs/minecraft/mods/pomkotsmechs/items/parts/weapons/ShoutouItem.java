package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.DaigomaruItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.ShoutouItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ShoutouItem extends BasePartsItem.WeaponArm {
    public ShoutouItem(Properties properties) {
        super(properties);
    }

    @Override
    public ShoutouItemRenderer newRenderer() {
        return new ShoutouItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!world.isClientSide()) {
                var player = mechInterface.getPlayer();
                if (player != null) {
                    var blockState = getMainHandBlock(player);

                    if (blockState != null) {
                        var hit = player.pick(100, 0, true);

                        if (hit.getType() == HitResult.Type.BLOCK) {
                            BlockHitResult bh = (BlockHitResult) hit;
                            BlockPos pos = bh.getBlockPos().relative(bh.getDirection());

                            Utils.setBlock(world, pos, blockState, 3);

                            if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.getAbilities().instabuild) {
                                player.getMainHandItem().shrink(1);
                            }
                        }
                    }
                }
            } else {
                mechInterface.playSoundEffect(PomkotsMechs.SE_PLACE.get());
            }
        }
    }

    private BlockState getMainHandBlock(LivingEntity player) {
        if (player != null && player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return null;
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_ARM;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.RIFLE;
    }

    @Override
    public Motion getMotion() {
        return Motion.RIFLE;
    }

    @Override
    public int getCoolTime() {
        return 11;
    }

    @Override
    public String getPartsSeriesName() {
        return "shoutou";
    }
}
