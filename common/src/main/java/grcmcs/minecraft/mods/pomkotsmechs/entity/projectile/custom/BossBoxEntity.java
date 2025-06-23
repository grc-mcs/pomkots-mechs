package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BossBoxEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 100;
    private int lifeTicks = 0;
    public float scale = 2f;

    public BossBoxEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.noCulling = true;
        this.noPhysics = true;
    }

    @Override
    public boolean shouldRender(double a, double b, double c) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double e) {
        return true;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);

        super.tick();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            if (lifeTicks < 10) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bossbox.idle"));
            } else if (lifeTicks == 10) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.bossbox.start").thenPlayAndHold("animation.bossbox.open"));
            } else {
                return PlayState.CONTINUE;
            }
        }).setSoundKeyframeHandler(this::registerAnimationSoundHandlers));
    }

    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        if ("raise1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
            this.spawnDirtParticlesSquare(this.level(), (int)(1 * scale), this.blockPosition());
        } else if ("raise2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_EARTHBREAK_EVENT.get());
            this.spawnDirtParticlesSquare(this.level(), (int)(10 * scale), this.blockPosition());
        } else if ("close".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOSSBOXOPEN.get());
        }
    }

    public void spawnDirtParticlesSquare(Level level, int ypos, BlockPos centerPos) {
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        RandomSource random = level.getRandom();

        int range = (int)(7 * scale);

        for (int dx = -range; dx < range; dx++) {
            for (int dz = -range; dz < range; dz++) {
                double px = centerPos.getX() + 0.5 + dx;
                double py = centerPos.getY() + 1.0 + random.nextDouble() * 2.0 + ypos;
                double pz = centerPos.getZ() + 0.5 + dz;

                double vx = (random.nextDouble() - 0.5) * 0.1;
                double vy = random.nextDouble() * 0.1;
                double vz = (random.nextDouble() - 0.5) * 0.1;

                this.level().addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, dirt),
                        px, py, pz,
                        vx, vy, vz
                );
            }
        }
    }


    protected void playSoundEffect(SoundEvent event) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
