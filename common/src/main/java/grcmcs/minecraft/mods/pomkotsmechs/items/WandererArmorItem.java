package grcmcs.minecraft.mods.pomkotsmechs.items;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsArmorItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.WandererArmorItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WandererArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Supplier<Object> renderProvider;

    public WandererArmorItem(ArmorMaterial armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties);
        if (Platform.isFabric()) {
            renderProvider = GeoItem.makeRenderer(this);
        }
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    public static BiConsumer<Consumer<Object>, WandererArmorItem> regForForge;

    public void initializeClient(Consumer<Object> consumer) {
        regForForge.accept(consumer, this);
    }

    // Create our armor model/renderer for Fabric and return it
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original) {
                if(this.renderer == null)
                    this.renderer = new WandererArmorItemRenderer();

                // This prepares our GeoArmorRenderer for the current render frame.
                // These parameters may be null however, so we don't do anything further with them
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }

    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, 20, state -> {
//            // Apply our generic idle animation.
//            // Whether it plays or not is decided down below.
//            state.getController().setAnimation(DefaultAnimations.IDLE);
//
//            // Let's gather some data from the state to use below
//            // This is the entity that is currently wearing/holding the item
//            Entity entity = state.getData(DataTickets.ENTITY);
//
//            // We'll just have ArmorStands always animate, so we can return here
//            if (entity instanceof ArmorStand)
//                return PlayState.CONTINUE;
//
//            // For this example, we only want the animation to play if the entity is wearing all pieces of the armor
//            // Let's collect the armor pieces the entity is currently wearing
//            Set<Item> wornArmor = new ObjectOpenHashSet<>();
//
//            for (ItemStack stack : entity.getArmorSlots()) {
//                // We can stop immediately if any of the slots are empty
//                if (stack.isEmpty())
//                    return PlayState.STOP;
//
//                wornArmor.add(stack.getItem());
//            }
//
//            // Check each of the pieces match our set
//            boolean isFullSet = wornArmor.containsAll(ObjectArrayList.of(
//                    PomkotsMechs.POMKOTS_ARMOR_HELMET,
//                    PomkotsMechs.POMKOTS_ARMOR_CHESTPLATE,
//                    PomkotsMechs.POMKOTS_ARMOR_LEGGINGS,
//                    PomkotsMechs.POMKOTS_ARMOR_BOOTS));
//
//            // Play the animation if the full set is being worn, otherwise stop
//            return isFullSet ? PlayState.CONTINUE : PlayState.STOP;
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
