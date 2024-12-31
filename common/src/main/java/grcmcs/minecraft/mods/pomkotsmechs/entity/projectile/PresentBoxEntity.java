package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PresentBoxEntity extends LivingEntity implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final int SIZE = 1;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public PresentBoxEntity(EntityType<? extends PresentBoxEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    private int openCount = 0;

    @Override
    public void tick() {
        super.tick();
        // 重力を適用
        if (!this.isNoGravity()) {
            this.addDeltaMovement(new Vec3(0, -0.08, 0));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
        }

        if (openCount > 0) {
            openCount++;
            if (openCount > 80) {
                openCount = -1;
                if (!this.level().isClientSide) {
                    KujiraEntity blockMass = new KujiraEntity(PomkotsMechs.KUJIRA.get(), this.level());
                    blockMass.setPos(this.getX(), this.getY(), this.getZ());
                    this.level().addFreshEntity(blockMass);
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player p, InteractionHand h) {
        openCount = 1;
        return InteractionResult.SUCCESS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            if (openCount > 0) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.presentbox.open1"));
            } else if (openCount < 0) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.presentbox.open2"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.presentbox.idle"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }


    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        // カメラからの距離に関係なくレンダリングする
        double maxDistance = 1000; // 必要に応じて距離を設定
//        return this.distanceToSqr(cameraX, cameraY, cameraZ) < maxDistance * maxDistance;
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }
}
