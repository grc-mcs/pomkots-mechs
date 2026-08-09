package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.StraightRushGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnContext;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnSource;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionSpawnTrackingService;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pmc01Entity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable, MissionSpawnSource {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final int MAX_LIVING_TICK = 600;
    private static final int MAX_LIVING_TICK_AFTER_DROPPED = 60;

    public static final float DEFAULT_SCALE = 1.0f;
    public RaidControllerEntity raidControllerEntity = null;
    public UUID raidControllerEntityUUID = null;
    private int countDownForDiscard = -1;

    private float dropDistance = 100;
    private MissionSpawnContext missionSpawnContext;

    public Pmc01Entity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.alwaysLookAtTarget = false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new StraightRushGoal(this, 2));
        this.targetSelector.addGoal(1, new NearestEntityTargetGoal(this, RaidObjectiveEntity.class, false));
        this.targetSelector.addGoal(2, new NearestEntityTargetGoal(this, Player.class, false));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmc01.flying"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmc01.idle"));
            }
        }));
    }

    protected void updateRotationBasedOnVelocity() {
        Vec3 vel = this.getDeltaMovement();

        if (vel.lengthSqr() > 0.0001) {
            float yaw = (float) Math.toDegrees(Math.atan2(vel.x, vel.z));
//            float yaw = (float) (Math.atan2(vel.z, vel.x) * 180.0 / Math.PI) - 90.0F;
            this.setYRot(yaw);        // エンティティ基本角度
            this.yRotO = yaw;         // 前フレーム角度を同期

            this.setYBodyRot(yaw);    // モブの体
            this.yBodyRotO = yaw;

            this.setYHeadRot(yaw);    // 頭
            this.yHeadRotO = yaw;
        }
    }

    @Override
    public void tick() {
        this.setNoGravity(true);

        super.tick();

        if (this.isServerSide()) {
            if (tickCount > MAX_LIVING_TICK) {
                if (this.container != null) {
                    MissionSpawnTrackingService.discardSpawnedChild(this, this.container);
                }
                this.discard();
            }

            if (raidControllerEntityUUID != null && raidControllerEntity == null) {
                raidControllerEntity = (RaidControllerEntity) ((ServerLevel)this.level()).getEntity(raidControllerEntityUUID);
            }

            if (!"".equals(containerType) && containerUUID == null) {
                var type = Utils.getEntityType(containerType);
                if (type != null) {
                    Entity entity = type.get().create(this.level());

                    entity.setPos(this.position());

                    if (!this.level().addFreshEntity(entity)) {
                        entity.discard();
                        return;
                    }
                    this.containerUUID = entity.getUUID();
                    this.container = entity;

                    if (entity instanceof GenericPomkotsMonster pomkots) {
                        pomkots.setInRaid(this.isInRaid);
                        pomkots.setInEvent(this.isInEvent());
                        pomkots.setModifiers(healthModifier, attackModifier);
                    }

                    if (raidControllerEntity != null) {
                        raidControllerEntity.addSpawnedEntity(entity);

                        if (entity instanceof Pmc02Entity pmc02) {
                            pmc02.raidControllerEntityUUID = raidControllerEntity.getUUID();
                            pmc02.raidControllerEntity = raidControllerEntity;
                        }
                    }
                    MissionSpawnTrackingService.registerSpawnedChild(this, entity);
                }
                containerType = "";
            }

            if (containerUUID != null && container == null) {
                var c = ((ServerLevel)this.level()).getEntity(containerUUID);
                if (c != null) {
                    this.container = c;
                }
            }

            if (this.container != null) {
                if (this.container instanceof BaseBossEntity boss) {
                    boss.isCarrying = true;
                } else if (this.container instanceof Pmc02Entity cont) {
                    cont.isCarrying = true;
                    cont.setSpawnTargetMobs(spawnTargetMobs);
                }

                var pos = this.position();
                this.container.setPos(pos.x, pos.y - 10, pos.z);

                var yaw = this.getYRot();
                container.setYRot(yaw);
                container.yRotO = yaw;
                if (container instanceof LivingEntity living) {
                    living.setYBodyRot(yaw);
                    living.yBodyRotO = yaw;
                    living.setYHeadRot(yaw);
                    living.yHeadRotO = yaw;
                }

                if (this.getTarget() != null
                        && this.container != null
                        && !isFartherThanXZ(this, this.getTarget(), this.dropDistance)
                        && isBelowGroundNotWater(this.level(), this.blockPosition())
                ) {
                    if (this.container instanceof Mob mob) {
                        mob.setTarget(this.getTarget());
                    }
                    this.container = null;
                    this.containerUUID = null;
                    this.countDownForDiscard = MAX_LIVING_TICK_AFTER_DROPPED;
                }
            }

            if (countDownForDiscard > 0) {
                countDownForDiscard--;
            } else if (countDownForDiscard == 0) {
                this.discard();
            }
        }
    }

    public static boolean isFartherThanXZ(Entity a, Entity b, double n) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        double distSqr = dx * dx + dz * dz;
        return distSqr >= n * n; // 距離n以上ならtrue
    }

    public static boolean isBelowGroundNotWater(Level level, BlockPos startPos) {
        // 下方向にスキャン
        for (int y = startPos.getY(); y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir()) {
                // 最初にぶつかったブロックが水じゃなければtrue
                return !state.getFluidState().is(FluidTags.WATER);
            }
        }
        // 全部空気 or ワールド下限まで何もなかった
        return false;
    }

    protected Entity container = null;
    public String containerType = "";
    protected UUID containerUUID = null;

    private List<String> spawnTargetMobs = new ArrayList<>();

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(PomkotsMechs.nbtName("ContainerType"), containerType);
        compound.putFloat(PomkotsMechs.nbtName("DropDistance"), dropDistance);

        if (containerUUID != null) {
            compound.putUUID(PomkotsMechs.nbtName("ContainerUUID"), containerUUID);
        } else {
            compound.remove(PomkotsMechs.nbtName("ContainerUUID"));
        }

        if (raidControllerEntityUUID != null) {
            compound.putUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"), raidControllerEntityUUID);
        } else {
            compound.remove(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
        }

        ListTag list = new ListTag();
        for (String type : spawnTargetMobs) {
            list.add(StringTag.valueOf(type));
        }
        compound.put(PomkotsMechs.nbtName("SpawnTargetMobs"), list);
        if (missionSpawnContext != null) {
            compound.put(PomkotsMechs.nbtName("MissionSpawnContext"), missionSpawnContext.save());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(PomkotsMechs.nbtName("ContainerType"))) {
            containerType = compound.getString(PomkotsMechs.nbtName("ContainerType"));
        } else {
            containerType = "";
        }

        if (compound.contains(PomkotsMechs.nbtName("ContainerUUID"))) {
            containerUUID = compound.getUUID(PomkotsMechs.nbtName("ContainerUUID"));
        } else {
            containerUUID = null;
        }

        if (compound.contains(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
            raidControllerEntityUUID = compound.getUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
        } else {
            raidControllerEntityUUID = null;
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

        if (compound.contains(PomkotsMechs.nbtName("DropDistance"))) {
            dropDistance = compound.getFloat(PomkotsMechs.nbtName("DropDistance"));
        } else {
            dropDistance = 100F;
        }

        if (compound.contains(PomkotsMechs.nbtName("MonsterHealthModifier"))) {
            healthModifier = compound.getFloat(PomkotsMechs.nbtName("MonsterHealthModifier"));
        } else {
            healthModifier = 1.0F;
        }
        if (compound.contains(PomkotsMechs.nbtName("MissionSpawnContext"), Tag.TAG_COMPOUND)) {
            missionSpawnContext = MissionSpawnContext.load(
                    compound.getCompound(PomkotsMechs.nbtName("MissionSpawnContext")));
        } else {
            missionSpawnContext = null;
        }
    }

    @Override
    public boolean hasPendingMissionSpawns() {
        return !containerType.isEmpty();
    }

    @Override
    public MissionSpawnContext getMissionSpawnContext() {
        return missionSpawnContext;
    }

    @Override
    public void setMissionSpawnContext(MissionSpawnContext context) {
        missionSpawnContext = context;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy()) MissionSpawnTrackingService.completeSource(this);
        super.remove(reason);
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 10) {
            if (isServerSide()) {
                var level = this.level();
                ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level);
                e.setPos(this.position());
                level.addFreshEntity(e);

                if (this.container != null) {
                    MissionSpawnTrackingService.discardSpawnedChild(this, this.container);
                    this.container = null;
                    this.containerUUID = null;
                }
            }

            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public String getMechName() {
        return "pmc01";
    }

    @Override
    protected void doAttack() {

    }

    @Override
    protected int getMaxAttackCooltime() {
        return 20;
    }
}
