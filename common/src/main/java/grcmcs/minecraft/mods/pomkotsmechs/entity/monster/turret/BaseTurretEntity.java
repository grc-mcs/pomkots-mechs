package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

// Charging Mob
public abstract class BaseTurretEntity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable {

    public BaseTurretEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(getMechData().maxStepUp);
        this.setSpeed(getMechData().speed);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;
    }

    public abstract void startActionAnimation();
    public abstract void stopActionAnimation();

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public void travel(Vec3 pos) {
    }

    @Override
    public int getMaxAttackCooltime() {
        return 10;
    }

    public static boolean canSpawn(EntityType<BaseTurretEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide && player.isCreative() && player.isShiftKeyDown()) {
            this.rotateToTarget(player);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        if ("small_canon".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_SHOTGUN.get(), this);
        } else if ("missile".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get(), this);
        } else if ("missile_large".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get(), this);
        } else if ("large_canon".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get(), this);
        }
    }
}
