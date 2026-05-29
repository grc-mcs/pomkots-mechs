package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.GliderUnitItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class GliderUnitItem extends BasePartsItem.Extension {
    public GliderUnitItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void startUsing(Level level, ItemStack stack, Entity entity) {
        if (!level.isClientSide) {
            triggerAnim(entity, GeoItem.getOrAssignId(stack, (ServerLevel) level), "Activation", "use");
        }
    }

    public void endUsing(Level level, ItemStack stack, Entity entity) {
        if (!level.isClientSide) {
            triggerAnim(entity, GeoItem.getOrAssignId(stack, (ServerLevel) level), "Activation", "stop");
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
                .triggerableAnim("use", RawAnimation.begin().thenPlay("animation.gliderunit.open"))
                .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.gliderunit.close"))
        );
    }

    @Override
    public GliderUnitItemRenderer newRenderer() {
        return new GliderUnitItemRenderer();
    }

    @Override
    public String getPartsSeriesName() {
        return "gliderunit";
    }
}
