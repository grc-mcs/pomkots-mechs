package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

import java.util.function.Predicate;
import java.util.Locale;

// Charging Mob
public abstract class BaseTurretEntity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable {

    public enum TargetEntityType {
        PLAYER(0, "player"),
        POMKOTS_MONSTERS(1, "hostile_mechs"),
        HOSTILE_MOBS(2, "hostile_mobs");

        private final int num;
        private final String serializedName;

        TargetEntityType(int num, String serializedName) {
            this.num = num;
            this.serializedName = serializedName;
        }

        public static TargetEntityType of(int num) {
            if (num < 0) return PLAYER;
            for (TargetEntityType type : values()) {
                if (type.num == num) return type;
            }
            return PLAYER;
        }

        public int getNum() {
            return num;
        }

        public String getSerializedName() {
            return serializedName;
        }

        public static TargetEntityType of(String value) {
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "player", "players" -> PLAYER;
                case "hostile_mechs", "enemy_mechs", "pomkots_monsters" -> POMKOTS_MONSTERS;
                case "hostile_mobs" -> HOSTILE_MOBS;
                default -> {
                    PomkotsMechs.LOGGER.warn(
                            "Unknown turret target entity type '{}'; falling back to player", value);
                    yield PLAYER;
                }
            };
        }
    }

    protected TargetEntityType targetEntityType = TargetEntityType.PLAYER;

    public void setTargetEntityType(TargetEntityType type) {
        this.targetEntityType = type;
    }

    public BaseTurretEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(getMechData().maxStepUp);
        this.setSpeed(getMechData().speed);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
//        this.noCulling = true;
    }

    public void tick() {
        if (firstTick) {
            registerTargetSelectorGoals();

            if (targetEntityType == TargetEntityType.POMKOTS_MONSTERS) {
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mechData.health * 1.5);
                this.setHealth(mechData.health * 1.5F);
            }
        }
        super.tick();
    }

    protected void registerTargetSelectorGoals() {
        if (this.targetEntityType == TargetEntityType.POMKOTS_MONSTERS) {
            this.targetSelector.addGoal(1, new NearestEntityTargetGoal<>(this, GenericPomkotsMonster.class, false,
                    livingEntity -> !(livingEntity instanceof BaseTurretEntity)));
        } else {
            this.targetSelector.addGoal(1, new NearestEntityTargetGoal<>(this, Player.class, false, false));
        }
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
            this.playSoundEffect(PomkotsMechs.SE_SHOTGUN.get());
        } else if ("missile".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
        } else if ("missile_large".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
        } else if ("large_canon".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        String key = PomkotsMechs.nbtName("TargetEntityType");
        if (compound.contains(key, Tag.TAG_STRING)) {
            this.targetEntityType = TargetEntityType.of(compound.getString(key));
        } else if (compound.contains(key, Tag.TAG_ANY_NUMERIC)) {
            this.targetEntityType = TargetEntityType.of(compound.getInt(key));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(
                PomkotsMechs.nbtName("TargetEntityType"),
                this.targetEntityType.getSerializedName());
    }
}
