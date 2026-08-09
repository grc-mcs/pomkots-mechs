package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb06Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.SearchDroneGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RaidTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

// Flying Mob
public class Pms10Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 0.5F;

    @Override
    public String getMechName() {
        return "pms10";
    }

    public Pms10Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    private LivingEntity prevTarget = null;

    private int alertTick = 0;

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        if (this.isServerSide() && this.getMode() != MODE_BLUE) {
            if (alertTick > 0) {
                alertTick++;
                if (alertTick == 50) {
                    if (this.getTarget() != null) {
                        var level = this.level();

                        if (this.getMode() == MODE_RED || this.raidName != null && !this.raidName.isEmpty()) {
                            startRaid(this.raidName, level);
                        } else {
                            forceTarget(level);
                        }

                        this.kill();
                    } else {
                        alertTick = 0;
                    }
                }
            } else if (this.getTarget() != null) {
                var level = this.level();
                AlertEntity e;

                if (this.getMode() == MODE_RED) {
                    sendTitle(
                            "Detected!",
                            "Mech Dropship Summoned",
                            this.getTarget()
                    );
                    e = new AlertEntity(PomkotsMechs.ALERTRED.get(), level);
                } else {
                    sendTitle(
                            "Detected!",
                            "Nearby Enemies Converging",
                            this.getTarget()
                    );
                    e = new AlertEntity(PomkotsMechs.ALERT.get(), level);
                }

                e.setPos(this.position());
                level.addFreshEntity(e);

                alertTick = 1;
            }
        }
    }

    private void sendTitle(String title, String subTitle, LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            ClientboundSetTitlesAnimationPacket timesPacket =
                    new ClientboundSetTitlesAnimationPacket(20, 60, 20);

            ClientboundSetTitleTextPacket titlePacket =
                    new ClientboundSetTitleTextPacket(Component.literal(title));

            ClientboundSetSubtitleTextPacket subtitlePacket =
                    new ClientboundSetSubtitleTextPacket(Component.literal(subTitle));

            player.connection.send(timesPacket);
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
    }

    private void startRaid(String raidName, Level level) {
        var raidDef = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(raidName);
        if (raidDef != null) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                rce.setPos(this.getTarget().position());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raidName, this.getTarget().getUUID(), "say mission complete", "say mission failed"
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);
            }
        }
    }

    private void forceTarget(Level level) {
        List<BaseSmallMonsterEntity> list = level.getEntitiesOfClass(
                BaseSmallMonsterEntity.class,
                this.getBoundingBox().inflate(200, 40, 200),
                e -> !(e instanceof Pms10Entity)
        );

        for (var mob: list) {
            mob.setTarget(this.getTarget());
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.removeAllGoals((goal)->true);

        this.targetSelector.addGoal(0, new RaidTargetGoal<>(
                this,
                RaidObjectiveEntity.class,
                this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue(),
                200,
                this::isInRaid));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true, this::predicate));

        this.goalSelector.addGoal(1, new SearchDroneGoal(
                this,
                getMechData().speed * 2,
                100F,
                80F,
                20F,
                40
        ));
    }

    public boolean predicate(Object target) {
        if (target instanceof Player p) {
            boolean ridingMech = p.getVehicle() instanceof PomkotsVehicleBase;

            if (ridingMech) {
                return true;

            } else if (p.distanceTo(this) <= this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() * 0.5) {
                if (p.getMainHandItem().is(PomkotsMechs.CARTON.get())) {
                    return p.isSprinting();
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    };

    @Override
    public void doAttack() {
        if (this.isServerSide() && this.getTarget() != null) {
            this.rotateToTarget(this.getTarget());

            BulletMiddleEntity be = new BulletMiddleEntity(PomkotsMechs.BULLETMIDDLE.get(), this.level(), this, getMechData().bulletDamage);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(0, 0, 5);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));
            be.setYRot(this.getYRot());
            be.setXRot(this.getXRot());
            be.yRotO = this.getYRot();
            be.xRotO = this.getXRot();

            float[] angle = Utils.getShootingAngle(be, this.getTarget(), true);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

            this.level().addFreshEntity(be);
        }
    }

    public static int MODE_BLUE = 0;
    public static int MODE_YELLOW = 1;
    public static int MODE_RED = 2;

    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(Pms10Entity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, MODE_YELLOW);
    }

    private void setMode(int id) {
        this.entityData.set(MODE, id);

    }

    public int getMode() {
        return this.entityData.get(MODE);
    }

    private String raidName = "";

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains(PomkotsMechs.nbtName("DroneSearchMode"))) {
            setMode(tag.getInt(PomkotsMechs.nbtName("DroneSearchMode")));
        } else {
            setMode(MODE_YELLOW);
        }

        if (tag.contains(PomkotsMechs.nbtName("DroneStartRaidName"))) {
            raidName = tag.getString(PomkotsMechs.nbtName("DroneStartRaidName"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt(PomkotsMechs.nbtName("DroneSearchMode"), getMode());

        if (this.raidName != null) {
            tag.putString(PomkotsMechs.nbtName("DroneStartRaidName"), raidName);
        } else {
            tag.remove(PomkotsMechs.nbtName("DroneStartRaidName"));
        }
    }

    @Override
    public int getMaxAttackCooltime() {
        return 30;
    }

    @Override protected float getFlyingSpeed() {
        return getMechData().speed;
    }

    @Override
    public boolean onGround() {
        return false; // モンスターが地上にいると認識されないようにする
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (isClosed()) {
                return PlayState.STOP;
            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms.idle"))
                .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
                .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
        );
    }

    @Override
    protected void fireOpenAnimation() {
        this.triggerAnim("shoot_controller", "open");
    }

    @Override
    protected void fireCloseAnimation() {
        this.triggerAnim("shoot_controller", "close");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static boolean canSpawn(EntityType<Pms10Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }
}
