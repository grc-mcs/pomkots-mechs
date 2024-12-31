package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlayerDummyEntity extends PathfinderMob implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public PlayerDummyEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(1.0F);
    }

    @Override
    public boolean hurt(DamageSource s, float d) {
       if (s.getMsgId().equals("inWall")) {
           return false;
       }
       return super.hurt(s, d);
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public void tick() {
        if (this.getVehicle() == null) {
            this.kill();
        }
        super.tick();
    }

    @Override
    public void travel(Vec3 pos) {
        super.travel(pos);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.ossan.sit"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
