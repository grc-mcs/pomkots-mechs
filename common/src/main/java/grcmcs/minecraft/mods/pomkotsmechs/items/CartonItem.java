package grcmcs.minecraft.mods.pomkotsmechs.items;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.CartonItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CartonItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Supplier<Object> renderProvider;

    public CartonItem(Properties properties) {
        super(properties);
        if (Platform.isFabric()) {
            renderProvider = GeoItem.makeRenderer(this);
        }
//        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    public static BiConsumer<Consumer<Object>, CartonItem> regForForge;

    public void initializeClient(Consumer<Object> consumer) {
        regForForge.accept(consumer, this);
    }
//
//    @Override
//    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
//        if (!level.isClientSide)
//            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(interactionHand), (ServerLevel) level), "Activation", "use");
//
//        player.startUsingItem(interactionHand);
//
//        return super.use(level, player, interactionHand);
//    }
//
//    @Override
//    public int getUseDuration(ItemStack stack) {
//        // 例えば 72000 (バニラの弓と同じ、事実上「無限に押せる」)
//        return 72000;
//    }
//
//    @Override
//    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
//        if (!level.isClientSide) {
//        }
//    }
//
//    @Override
//    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
//        if (!level.isClientSide)
//            triggerAnim(livingEntity, GeoItem.getOrAssignId(itemStack, (ServerLevel) level), "Activation", "stop");
//
//        super.releaseUsing(itemStack, level, livingEntity, i);
//    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = (BlockEntityWithoutLevelRenderer)newRenderer();

                return this.renderer;
            }
        });
    }

    public Object newRenderer() {
        return new CartonItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0, state -> PlayState.STOP)
                .triggerableAnim("use", RawAnimation.begin().thenPlayAndHold("animation.carton.use"))
                .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.carton.stopUsing"))
        );

        controllers.add(new AnimationController<>(this, 0, state -> {
                    return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.carton.use"));

//            if (entity instanceof Player p) {
//
//                System.out.println(p + ":" +p.isShiftKeyDown());
//
//                if (p.isShiftKeyDown()) {
//                    state.getController().forceAnimationReset();
//                    return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.carton.use"));
//
//                } else {
//                    return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.carton.idle"));
//                }
//            } else {
//                System.out.println("hoge");
//                return PlayState.STOP;
//            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
