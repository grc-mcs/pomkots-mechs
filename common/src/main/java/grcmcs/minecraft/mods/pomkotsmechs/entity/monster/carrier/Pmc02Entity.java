package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb01mk2Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pmc02Entity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 4000;
    private int lifeTicks = 0;
    private int ongroundTicks = 80;
    public float scale = 2f;

    public boolean isCarrying = false;
    private List<String> spawnTargetMobs = new ArrayList<>();

    public RaidControllerEntity raidControllerEntity = null;
    public UUID raidControllerEntityUUID = null;

    @Override
    public String getMechName() {
        return "pmc02";
    }

    public Pmc02Entity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.alwaysLookAtTarget = false;
    }

    @Override
    public void tick() {
        this.setNoGravity(false);

        super.tick();

        if (this.isServerSide()) {
            if (raidControllerEntityUUID != null && raidControllerEntity == null) {
                raidControllerEntity = (RaidControllerEntity) ((ServerLevel)this.level()).getEntity(raidControllerEntityUUID);
            }

            if (this.lifeTicks++ >= MAX_LIFE_TICKS) {
                this.discard();

            } else {
                if (this.isCarrying && this.onGround()) {
                    this.isCarrying = false;
                    this.triggerAnim("action_controller", "onground");
                }

                if (!this.isCarrying) {
                    ongroundTicks--;
                }

                if (ongroundTicks == 50) {
                    this.triggerAnim("action_controller", "open");

                    for (String typeString: spawnTargetMobs) {
                        var type = Utils.getEntityType(typeString);
                        if (type != null) {
                            var ent = type.get().create(this.level());

                            if (ent != null) {
                                var pos = this.position();
                                ent.setPos(
                                        pos.x + this.random.nextInt(8) - 4,
                                        pos.y + 5,
                                        pos.z + this.random.nextInt(8) - 4
                                );
                                if (ent instanceof BaseSmallMonsterEntity mons) {
                                    mons.setPersistenceRequired();

                                    if (this.getTarget() != null) {
                                        mons.setTarget(this.getTarget());
                                    }
                                    mons.setInRaid(this.isInRaid);
                                    mons.setInEvent(this.isInEvent());
                                }
                                if (raidControllerEntity != null) {
                                    raidControllerEntity.addSpawnedEntity(ent);
                                }

                                this.level().addFreshEntity(ent);
                            }
                        }
                    }
                    spawnTargetMobs.clear();

                } else if (ongroundTicks < 0) {
                    this.discard();
                }

            }
        }
    }

    public void setSpawnTargetMobs(@NotNull List<String> list) {
        this.spawnTargetMobs = list;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean(PomkotsMechs.nbtName("IsCarrying"), isCarrying);

        ListTag list = new ListTag();
        for (String type : spawnTargetMobs) {
            list.add(StringTag.valueOf(type));
        }
        compound.put(PomkotsMechs.nbtName("SpawnTargetMobs"), list);

        if (raidControllerEntityUUID != null) {
            compound.putUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"), raidControllerEntityUUID);
        } else {
            compound.remove(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(PomkotsMechs.nbtName("IsCarrying"))) {
            isCarrying = compound.getBoolean(PomkotsMechs.nbtName("IsCarrying"));
        } else {
            isCarrying = false;
        }

        if (compound.contains(PomkotsMechs.nbtName("SpawnTargetMobs"))) {
            spawnTargetMobs.clear();
            ListTag list = (ListTag) compound.get(PomkotsMechs.nbtName("SpawnTargetMobs"));
            if (list != null) {
                for (Tag t : list) {
                    spawnTargetMobs.add(t.getAsString());
                }
            }
        }

        if (compound.contains(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
            raidControllerEntityUUID = compound.getUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
        } else {
            raidControllerEntityUUID = null;
        }
    }

    private final AnimationController<Pmc02Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("appearFromGround", RawAnimation.begin().thenPlayAndHold("animation.bossbox.start"))
            .triggerableAnim("open", RawAnimation.begin().thenPlayAndHold("animation.bossbox.open"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlayAndHold("animation.bossbox.onground"))
            .setSoundKeyframeHandler(this::registerAnimationSoundHandlers);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(trigger);
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
        } else if ("se_bossdown".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOSSDOWN_EVENT.get());
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

    @Override
    public boolean shouldRender(double a, double b, double c) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double e) {
        return true;
    }

    @Override
    protected void doAttack() {

    }

    @Override
    protected int getMaxAttackCooltime() {
        return 0;
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }
}
