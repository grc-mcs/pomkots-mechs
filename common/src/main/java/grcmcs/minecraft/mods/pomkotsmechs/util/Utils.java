package grcmcs.minecraft.mods.pomkotsmechs.util;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Utils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public static boolean isRidingPomkotsControllable(Entity entity) {
        return entity != null && entity.getVehicle() instanceof PomkotsControllable;
    }

    public static boolean isRidingPomkotsVehicle(Entity entity) {
        return entity != null && entity.getVehicle() instanceof PomkotsVehicle;
    }

    public static void logInfo(String message) {
        LOGGER.info(message);
    }

    public static void logError(String message, Throwable t) {
        LOGGER.error(message, t);
    }


    private static Vec3 createEvasionVelocityFromLocalVelocity(Entity ent) {
        Vec3 localVelocity = ent.getDeltaMovement().yRot((float) Math.toRadians((1.0) * ent.getYRot()));
        var forward = localVelocity.z;
        var sideways = localVelocity.x;

        Vec3 vel;
        if (forward == 0 && sideways == 0) {
            vel = new Vec3(0, 0, 1);
        } else if (Math.abs(sideways) >= 0.1) {
            vel = new Vec3(sideways, 0, 0).normalize();
        } else  {
            vel = new Vec3(sideways, 0, forward).normalize();
        }

        return vel;
    }

    // TODO くそかっこ悪いのでなんとかしたい…。半径指定したら計算して出してくれるようにする
    public static final ArrayList<Vec3i> circlePosRad9 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, 0));}
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, 1));}
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, -1));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 1, 0, 2));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 1, 0, -2));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 2, 0, 3));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 2, 0, -3));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 3, 0, 4));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 3, 0, -4));}
    }

    public static final ArrayList<Vec3i> circlePosRad7 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, -1));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, 0));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, 1));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 2, 0, 2));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 2, 0, -2));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 3, 0, 3));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 3, 0, -3));}
    }

    public static final ArrayList<Vec3i> circlePosRad5 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, 1));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, 0));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, -1));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 3, 0, 2));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 3, 0, -2));}
    }

    public static boolean isBlockDestructionAllowed(Entity ent) {
        if (ent == null) {
            return false;
        } else if (ent instanceof PomkotsVehicleBase) {
            return !ent.level().isClientSide && PomkotsMechs.CONFIG.enablePlayerVehicleBlockDestruction;
        } else {
            return !ent.level().isClientSide && PomkotsMechs.CONFIG.enableEntityBlockDestruction;
        }
    }

    public static boolean isDestructiveBLock(String blockID) {
        updateDestConfig();
        return !nonDestructiveBlocks.contains(blockID);
    }

    public static void destroyBlock(Level level, BlockPos blockPos, boolean dropItem) {
        var blockID = getBlockId(level.getBlockState(blockPos).getBlock());
        if (isDestructiveBLock(blockID)) {
            level.destroyBlock(blockPos, dropItem);
        }
    }

    public static void setBlock(Level level, BlockPos blockPos, BlockState blockState, int num) {
        var blockID = getBlockId(level.getBlockState(blockPos).getBlock());
        if (isDestructiveBLock(blockID)) {
            level.setBlock(blockPos, blockState, num);
        }
    }

    private static String getBlockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    private static String prevDestConfig = "hoge";
    private static final Set<String> nonDestructiveBlocks = new HashSet<>();

    private static void updateDestConfig() {
        if (!prevDestConfig.equals(PomkotsMechs.CONFIG.nonDestructiveBlocks)) {
            prevDestConfig = PomkotsMechs.CONFIG.nonDestructiveBlocks;

            if (prevDestConfig != null && !prevDestConfig.isEmpty()) {
                resetNonDestructiveBlocks();
                for (var blockId: prevDestConfig.split(",")) {
                    Utils.addNonDestructiveBlock(blockId);
                }
            }
        }
    }

    public static void resetNonDestructiveBlocks() {
        nonDestructiveBlocks.clear();
    }

    public static void addNonDestructiveBlock(String blockID) {
        nonDestructiveBlocks.add(blockID);
    }

    public static boolean shouldRenderCockpit(PomkotsVehicleBase vehicle) {
        if (vehicle instanceof Pmv03Entity) {
            return true;
        } else if (vehicle instanceof Pmvc01Entity pmvc01) {
            ItemStack headStack = pmvc01.getHeadParts();
            if (headStack != null && !headStack.isEmpty() && headStack.getItem() instanceof BasePartsItem.Head headParts) {
                if (headParts.isFullCovered()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static float[] getShootingAngle(Entity bullet, Entity target, boolean useDeviation) {
        var targetPos = getTargetPos(target, useDeviation);
        var bulletPos = bullet.position();

        Vec3 bulletDir = targetPos.subtract(bulletPos).normalize();

        float yaw = (float) (Math.atan2(-bulletDir.x, bulletDir.z) * (180.0 / Math.PI));
        float pitch = (float) (Math.asin(-bulletDir.y) * (180.0 / Math.PI));

        return new float[]{pitch, yaw};
    }

    public static Vec3 getTargetPos(Entity target, boolean useDeviation) {
        Vec3 targetPos;
        useDeviation = true;

        if (useDeviation) {
            targetPos = target.getBoundingBox().getCenter().add(target.getDeltaMovement()).add(target.getDeltaMovement());
        } else {
            targetPos = target.getBoundingBox().getCenter();
        }

        return targetPos;
    }

    public static boolean isObstructed(Level level, Entity from, Entity to) {
        Vec3 fromPos = from.getEyePosition(1.0f);
        Vec3 toPos = to.getEyePosition(1.0f);

        BlockHitResult result = level.clip(new ClipContext(
                fromPos,
                toPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                from
        ));

        // ブロックにヒットしている場合、遮蔽あり
        return result.getType() == HitResult.Type.BLOCK;
    }

    public static void playSoundEffect(SoundEvent event, Entity src) {
        src.level().playLocalSound(src.getX(), src.getY(), src.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    public static void playSoundEffect(SoundEvent event, Entity src, float volume) {
        src.level().playLocalSound(src.getX(), src.getY(), src.getZ(), event, SoundSource.PLAYERS, volume, 1.0F, false);
    }

    public static boolean isWithinHorizontalDistance(Entity entityA, Entity entityB, double distance) {
        Vec3 posA = entityA.position();
        Vec3 posB = entityB.position();

        double dx = posA.x - posB.x;
        double dz = posA.z - posB.z;
        double horizontalDistSqr = dx * dx + dz * dz;

        return horizontalDistSqr <= distance * distance;
    }

    public static LivingEntity getProjectileOwner(Projectile projectile) {
        if (projectile instanceof PomkotsThrowableProjectile ptp) {
            var shooter = ptp.getShooter();

            if (shooter == null) {
                return null;
            } else if (shooter instanceof PomkotsVehicleBase v) {
                var d = v.getDrivingPassenger();
                return Objects.requireNonNullElse(d, v);
            } else {
                return shooter;
            }
        } else {
            if (projectile.getOwner() instanceof LivingEntity l) {
                return l;
            } else {
                return null;
            }
        }
    }

    public static boolean isSystemicDamage(DamageSource source) {
        return "genericKill".equals(source.getMsgId());
    }

    public static boolean isInRangeOnAxisXZ(Entity a, Entity b, float distance) {
        if (a == null || b == null) {
            return false;
        }

        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        double distanceXZSquared = dx * dx + dz * dz;

        return distanceXZSquared <= distance * distance;
    }
}
