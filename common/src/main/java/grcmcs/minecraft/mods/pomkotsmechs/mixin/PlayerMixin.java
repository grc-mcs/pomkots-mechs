package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.google.common.collect.ImmutableList;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract Inventory getInventory();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "dropEquipment", cancellable = true, at = @At("HEAD"))
    private void onDropEquipment(CallbackInfo ci) {
        super.dropEquipment();

        Player entity = (Player) (Object) this;

        if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            var inv = getInventory();
            this.destroyVanishingCursedItems(inv);

            List<NonNullList<ItemStack>> invItems = ImmutableList.of(inv.items, inv.armor, inv.offhand);

            for (List<ItemStack> invItem : invItems) {
                for (int i = 0; i < invItem.size(); i++) {
                    ItemStack itemStack = invItem.get(i);
                    if (!itemStack.isEmpty() && isDropTarget(itemStack)) {
                        inv.player.drop(itemStack, true, false);
                        invItem.set(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        ci.cancel();
    }

    protected boolean isDropTarget(ItemStack stack) {
        return !stack.is(PomkotsMechs.KEYCARD_ITEM.get()) && !stack.is(PomkotsMechs.POMKOTS_RADAR_ITEM.get());
    }

    protected void destroyVanishingCursedItems(Inventory inventory) {
        for(int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemStack)) {
                inventory.removeItemNoUpdate(i);
            }
        }
    }
}
