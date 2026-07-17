package grcmcs.minecraft.mods.pomkotsmechs.items.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotMenuProvider;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PilotConfiguratorItem extends Item {

    public PilotConfiguratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity entity,
            InteractionHand hand
    ) {
        if (!(entity instanceof Mob mob) && !(entity instanceof Pmvc01Entity mech)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            if (entity instanceof Pmvc01Entity mech && mech.getDrivingPassenger() instanceof Mob mob) {
                mob.stopRiding();
                mob.setNoAi(false);
            } else if (entity instanceof Mob mob) {
                player.openMenu(new PilotMenuProvider(mob));
            }
        }

        return InteractionResult.sidedSuccess(
                player.level().isClientSide()
        );
    }
}