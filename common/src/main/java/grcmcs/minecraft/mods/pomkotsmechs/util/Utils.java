package grcmcs.minecraft.mods.pomkotsmechs.util;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsUnbreakableBlock;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai.MechAutoController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BluePrintItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.ExplosionNoDrop;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    public static void destroyBlockSphere(int radius, Vec3 offset, BlockPos ownerPos, Float ownerYRot, Level level) {
        destroyBlockSphere(radius, offset, ownerPos, ownerYRot, level, false);
    }

    public static void destroyBlockSphere(int radius, Vec3 offset, BlockPos ownerPos, Float ownerYRot, Level level, boolean erase) {
        // 半径の2乗を計算（球体判定に利用）
        int radiusSquared = radius * radius;

        Vec3 vec = offset;
        vec = vec.yRot((float) Math.toRadians((-1.0) * ownerYRot));
        BlockPos pos = new BlockPos(ownerPos.getX() + (int) vec.x, ownerPos.getY() + (int) vec.y, +ownerPos.getZ() + (int) vec.z);

        // 範囲を指定してループ処理
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radiusSquared) {
                        BlockPos targetPos = pos.offset(x, y, z);
                        BlockState state = level.getBlockState(targetPos);
                        // ブロックが空でない場合に破壊
                        if (!state.isAir()) {
                            if (erase) {
                                Utils.eraseBlock(level, targetPos);
                            } else {
                                Utils.destroyBlock(level, targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void destroyBlock(Level level, BlockPos blockPos, boolean dropItem) {
        var state = level.getBlockState(blockPos);
        if (isDestructiveBLock(state)) {
            level.destroyBlock(blockPos, dropItem && Utils.isDroppableBLock(state));
        }
    }

    public static void eraseBlock(Level level, BlockPos blockPos) {
        if (isDestructiveBLock(level.getBlockState(blockPos))) {
            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
        }
    }

    public static void setBlock(Level level, BlockPos blockPos, BlockState blockState, int num) {
        if (isDestructiveBLock(level.getBlockState(blockPos))) {
            level.setBlock(blockPos, blockState, num);
        }
    }

    private static String prevDestConfig = "hoge";
    private static final Set<Block> nonDestructiveBlocks = new HashSet<>();

    public static boolean isDestructiveBLock(BlockState blockState) {
        return isDestructiveBLock(blockState.getBlock());
    }

    public static boolean isDestructiveBLock(Block block) {
        return !(block instanceof PomkotsUnbreakableBlock) && !nonDestructiveBlocks.contains(block);
    }

    public static void updateDestConfig(PomkotsConfig config) {
        if (!prevDestConfig.equals(config.nonDestructiveBlocks)) {
            prevDestConfig = config.nonDestructiveBlocks;

            if (prevDestConfig != null && !prevDestConfig.isEmpty()) {
                resetNonDestructiveBlocks();
                for (var blockId: prevDestConfig.split(",")) {
                    Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));
                    if (!Blocks.AIR.equals(block)) {
                        Utils.addNonDestructiveBlock(block);
                    }
                }
            }
        }
    }

    public static void resetNonDestructiveBlocks() {
        nonDestructiveBlocks.clear();
    }

    public static void addNonDestructiveBlock(Block block) {
        nonDestructiveBlocks.add(block);
    }


    private static String prevDropConfig = "hoge";
    private static final Set<Block> nonDroppableBlocks = new HashSet<>();

    public static boolean isDroppableBLock(BlockState blockState) {
        return isDroppableBLock(blockState.getBlock());
    }

    public static boolean isDroppableBLock(Block block) {
        return !(block instanceof PomkotsUnbreakableBlock) && !nonDroppableBlocks.contains(block);
    }

    public static void updateDropConfig(PomkotsConfig config) {
        if (!prevDropConfig.equals(config.nonDropBlocks)) {
            prevDropConfig = config.nonDropBlocks;

            if (prevDropConfig != null && !prevDropConfig.isEmpty()) {
                resetNonDroppableBlocks();
                for (var blockId: prevDropConfig.split(",")) {
                    Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));
                    if (!Blocks.AIR.equals(block)) {
                        Utils.addNonDroppableBlock(block);
                    }
                }
            }
        }
    }

    public static void resetNonDroppableBlocks() {
        nonDroppableBlocks.clear();
    }

    public static void addNonDroppableBlock(Block block) {
        nonDroppableBlocks.add(block);
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
        return getShootingAngle(bullet, target, useDeviation, false);
    }

    public static float[] getShootingAngle(Entity bullet, Entity target, boolean useDeviation, boolean aimFoot) {
        var targetPos = getTargetPos(target, useDeviation, aimFoot);
        var bulletPos = bullet.position();

        Vec3 bulletDir = targetPos.subtract(bulletPos);
        double horizontalDistance = Math.sqrt(bulletDir.x * bulletDir.x + bulletDir.z * bulletDir.z);

        float yaw = (float) (Math.atan2(-bulletDir.x, bulletDir.z) * (180.0 / Math.PI));
        float pitch = (float) Math.toDegrees(Math.atan2(-bulletDir.y, horizontalDistance));

        return new float[]{pitch, yaw};
    }

    public static Vec3 getTargetPos(Entity target, boolean useDeviation, boolean aimFoot) {
        Vec3 targetPos;

        if (target.getVehicle() != null) {
            target = target.getVehicle();
        }

        if (target instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity hitBox) {
            targetPos = hitBox.getStableAimPosition(aimFoot);
            return useDeviation ? applyDeviation(2, targetPos, hitBox.getStableAimVelocity()) : targetPos;
        }

        if (aimFoot) {
            targetPos = target.position();
        } else {
            targetPos = target.getBoundingBox().getCenter();
        }

        if (useDeviation) {
                targetPos = applyDeviation(2, targetPos, target.getDeltaMovement());
        }

        return targetPos;
    }

    private static Vec3 applyDeviation(int num, Vec3 originalPos, Vec3 targetDelta) {
        for (int i = 0; i < num; i++) {
            originalPos = originalPos.add(targetDelta);
        }
        return originalPos;
    }

    private static final Random RANDOM = new Random();

    public static float[] getShootingAngle2(Entity bullet, Entity target,
                                           boolean useDeviation, boolean aimFoot,
                                           float bulletSpeed,
                                           float inaccuracy) {
        target = getTarget2(target);
        Vec3 bulletPos    = bullet.position();
        Vec3 targetPos    = getTargetPos2(target, aimFoot);
        Vec3 targetVelocity = target.getDeltaMovement();

        if (target instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity hitBox) {
            targetPos = hitBox.getStableAimPosition(aimFoot);
            targetVelocity = hitBox.getStableAimVelocity();
        }

        if (useDeviation) {
            return getShootingAngle(bulletPos, bulletSpeed, targetPos, targetVelocity, RANDOM, inaccuracy);
        } else {
            return aimDirect(bulletPos, targetPos);
        }
    }

    public static Entity getTarget2(Entity target) {
        if (target.getVehicle() != null) {
            target = target.getVehicle();
        }

        return target;
    }

    public static Vec3 getTargetPos2(Entity target, boolean aimFoot) {
        Vec3 targetPos;

        if (target instanceof Pmvc01Entity pmvc01) {
            targetPos = pmvc01.getPosHistory(2);
        } else {
            targetPos = target.position();
        }

        if (!aimFoot) {
            targetPos = targetPos.add(0, target.getBoundingBox().getYsize() / 2,0);
        }

        return targetPos;
    }

    public static float[] getShootingAngle(
            Vec3 bulletPos,
            float bulletSpeed,
            Vec3 targetPos,
            Vec3 targetVelocity,
            Random random,
            float inaccuracy
    ) {
        Vec3 relativePos = targetPos.subtract(bulletPos);

        double a = targetVelocity.dot(targetVelocity) - (bulletSpeed * bulletSpeed);
        double b = 2.0 * relativePos.dot(targetVelocity);
        double c = relativePos.dot(relativePos);

        double t = solveQuadratic(a, b, c);

        Vec3 predictedPos = t >= 0
                ? targetPos.add(targetVelocity.scale(t))
                : targetPos;

        // 精度に応じてランダムオフセットを加える
        if (inaccuracy > 0) {
            double dist = relativePos.length();
            double offsetScale = dist * inaccuracy * 0.1; // 距離に比例してずれる

            predictedPos = predictedPos.add(
                    (random.nextDouble() - 0.5) * 2 * offsetScale,
                    (random.nextDouble() - 0.5) * 2 * offsetScale,
                    (random.nextDouble() - 0.5) * 2 * offsetScale
            );
        }

        Vec3 dir = predictedPos.subtract(bulletPos).normalize();

        float yaw   = (float)(Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
        float pitch = (float)(Math.asin(-dir.y)          * (180.0 / Math.PI));

        return new float[]{pitch, yaw};
    }

    /**
     * 二次方程式 at^2 + bt + c = 0 の正の最小解を返す
     * 解なしの場合は -1 を返す
     */
    private static double solveQuadratic(double a, double b, double c) {
        if (Math.abs(a) < 1e-6) {
            // 一次方程式 bt + c = 0
            if (Math.abs(b) < 1e-6) return -1;
            double t = -c / b;
            return t >= 0 ? t : -1;
        }

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return -1; // 解なし

        double sqrtD = Math.sqrt(discriminant);
        double t1 = (-b - sqrtD) / (2 * a);
        double t2 = (-b + sqrtD) / (2 * a);

        // 正の最小値を返す
        if (t1 >= 0 && t2 >= 0) return Math.min(t1, t2);
        if (t1 >= 0) return t1;
        if (t2 >= 0) return t2;
        return -1;
    }

    private static float[] aimDirect(Vec3 bulletPos, Vec3 targetPos) {
        Vec3 dir = targetPos.subtract(bulletPos).normalize();
        float yaw   = (float)(Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
        float pitch = (float)(Math.asin(-dir.y)          * (180.0 / Math.PI));
        return new float[]{pitch, yaw};
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

    private static void playSoundEffect(SoundEvent event, Entity src) {
        src.level().playLocalSound(src.getX(), src.getY(), src.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    private static void playSoundEffect(SoundEvent event, Entity src, float volume) {
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
//
//    public enum PROGRESS {
//        CHAPTER_1("1"),
//        CHAPTER_2("2"),
//        CHAPTER_3("3"),
//        CHAPTER_4("4"),
//        NO_PROGRESS("n");
//
//        private final String text;
//
//        private PROGRESS(final String text) {
//            this.text = text;
//        }
//
//        public String getString() {
//            return this.text;
//        }
//    };
//
//    public static PROGRESS getCurrentProgress(Player p) {
//        Scoreboard scoreBoard = p.getScoreboard();
//        Objective o = scoreBoard.getObjective(PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS);
//        if (o == null) {
//            return PROGRESS.NO_PROGRESS;
//        } else {
//            String playerName = p.getScoreboardName(); // プレイヤー名を取得
//            Score score = scoreBoard.getOrCreatePlayerScore(playerName, o);
//
//            int s = score.getScore();
//
//            if (s < 100) {
//                return PROGRESS.CHAPTER_1;
//            } else if (s < 200) {
//                return PROGRESS.CHAPTER_2;
//            } else if (s < 300) {
//                return PROGRESS.CHAPTER_3;
//            } else if (s < 400) {
//                return PROGRESS.CHAPTER_4;
//            } else {
//                return PROGRESS.NO_PROGRESS;
//            }
//        }
//    }

    public static boolean isMapEditingMode(Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) return true;

        return level.getGameRules().getBoolean(PomkotsMechs.RULE_MAP_EDIT);
    }

    public static RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
        ResourceLocation rl = new ResourceLocation(typeId);

        RegistrySupplier<EntityType<?>> et = null;
        for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
            if (entityType.getId().equals(rl)) {
                et = entityType;
            }
        }

        return et;
    }


    public static List<ItemStack> getItemStacks(ItemStack mainHandStack, Player player, int totalAmountToConsume) {
        List<ItemStack> stacks = new LinkedList<>();

        int remainingAmount = totalAmountToConsume;
        Item mainHandItem = mainHandStack.getItem();

        // プレイヤーのインベントリ内を走査
        for (ItemStack stack : player.getInventory().items) {
            if (stack != mainHandStack && stack.getItem() == mainHandItem) {
                stacks.add(stack);

                int stackCount = stack.getCount();

                if (stackCount <= remainingAmount) {
                    remainingAmount -= stackCount;
                } else {
                    return stacks;
                }
            }
        }

        // メインハンドのアイテムを消費
        if (remainingAmount > 0) {
            stacks.add(mainHandStack);

            int mainHandCount = mainHandStack.getCount();

            if (mainHandCount <= remainingAmount) {
                remainingAmount -= mainHandCount;
            } else {
                remainingAmount = 0;
            }
        }

        if (remainingAmount > 0) {
            return null;
        } else {
            return stacks;
        }
    }

    public static void consumeItemStackFromTop(List<ItemStack> stacks, int num) {
        for (var stack: stacks) {
            if (stack.getCount() > num) {
                stack.setCount(stack.getCount() - num);
                return;
            } else {
                num -= stack.getCount();
                stack.setCount(0);
            }
        }
    }

    public static void doCircleAoeDamageAndKnockback(
            Level level,
            Entity owner,
            Vec3 center,
            double radius,
            float damage,
            double knockbackStrength,
            double lift
    ) {
        AABB box = new AABB(
                center.x - radius, center.y - 2, center.z - radius,
                center.x + radius, center.y + 2, center.z + radius
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != owner && !(e instanceof BossHitBoxEntity) && e.isAlive()
        );

        for (LivingEntity target : targets) {
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;

            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            // ダメージ
            target.hurt(
                    owner.damageSources().generic(),
                    damage
            );

            // ノックバック
            Vec3 kb = computeRadialKnockback(
                    center,
                    target,
                    radius,
                    knockbackStrength,
                    lift
            );

            target.setDeltaMovement(
                    target.getDeltaMovement().add(kb)
            );
            target.hasImpulse = true;
        }
    }

    public static Vec3 computeRadialKnockback(
            Vec3 impactCenter,
            LivingEntity target,
            double radius,
            double strength,
            double lift
    ) {
        // 中心 → エンティティ
        Vec3 dir = target.position().subtract(impactCenter);

        // 水平方向のみ（円AoEなのでXZ）
        Vec3 horizontal = new Vec3(dir.x, 0, dir.z);
        double dist = horizontal.length();

        if (dist < 0.0001) {
            // 中心ドンピシャ回避
            return Vec3.ZERO;
        }

        // 正規化
        Vec3 norm = horizontal.scale(1.0 / dist);

        // 距離減衰（中心強・端弱）
        double falloff = Mth.clamp(1.0 - (dist / radius), 0.0, 1.0);

        // 最終ベクトル
        return new Vec3(
                norm.x * strength * falloff,
                lift * falloff,
                norm.z * strength * falloff
        );
    }

    public static void spawnAoeDustParticles(
            Level level,
            Vec3 center,
            double radius,
            int countPerTick
    ) {
        RandomSource rand = level.random;

        for (int i = 0; i < countPerTick; i++) {
            Vec3 pos = randomPointInCircle(rand, center, radius);
            level.addParticle(
                    new BlockParticleOption(
                            ParticleTypes.BLOCK,
                            Blocks.DIRT.defaultBlockState()
                    ),
                    pos.x,
                    pos.y + 0.2,
                    pos.z,
                    rand.nextGaussian() * 0.03,
                    2,
                    rand.nextGaussian() * 0.03
            );
        }
    }

    public static void spawnAoeDustParticles2(
            Level level,
            Vec3 center,
            double radius,
            int countPerTick
    ) {
        RandomSource rand = level.random;

        for (int i = 0; i < countPerTick; i++) {
            Vec3 pos = randomPointInCircle(rand, center, radius);
            level.addAlwaysVisibleParticle(
                    new BlockParticleOption(
                            ParticleTypes.BLOCK,
                            Blocks.DIRT.defaultBlockState()
                    ),
                    pos.x,
                    pos.y + 0.2,
                    pos.z,
                    rand.nextGaussian() * 0.03,
                    2,
                    rand.nextGaussian() * 0.03
            );
        }
    }

    private static Vec3 randomPointInCircle(RandomSource rand, Vec3 center, double radius) {
        double r = radius * Math.sqrt(rand.nextDouble());
        double theta = rand.nextDouble() * Math.PI * 2;

        double x = center.x + r * Math.cos(theta);
        double z = center.z + r * Math.sin(theta);
        double y = center.y;

        return new Vec3(x, y, z);
    }

    public static Vec3 computeAoeCenter(Entity owner, Vec3 offsetLocal) {
        var offset = owner.position();
        offsetLocal = offsetLocal.yRot((float) Math.toRadians((-1.0) * owner.getYRot()));
        return offset.add(offsetLocal);
    }

    public static boolean isRidingPomkotsMechs(Player p) {
        if (p == null) {
            return false;
        }

        return p.getVehicle() instanceof PomkotsVehicle;
    }

    public static void explode(Entity entity, double x, double y, double z, float power, Level.ExplosionInteraction explosionInteraction, Level level) {
        explode(entity, x, y, z, power, false, explosionInteraction, level);
    }

    public static void explode(Entity entity, double x, double y, double z, float power, boolean fire, Level.ExplosionInteraction explosionInteraction, Level level) {
        Explosion.BlockInteraction blockInteraction;

        switch (explosionInteraction) {
            case NONE, MOB -> blockInteraction = Explosion.BlockInteraction.KEEP;
            case BLOCK -> blockInteraction = Explosion.BlockInteraction.DESTROY_WITH_DECAY;
            default -> {
                return;
            }
        }

        Explosion explosion = new ExplosionNoDrop(
                level,
                entity,
                x, y, z,
                power,
                fire,
                blockInteraction
        );

        explosion.explode();
        explosion.finalizeExplosion(true);
    }

    public static boolean completeAdvancement(ResourceLocation advancementId, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Advancement adv = player.getServer().getAdvancements().getAdvancement(advancementId);
            if (adv == null) {
                return false;
            }
            serverPlayer.getAdvancements().award(adv, "complete");
            return true;
        }

        return false;
    }

    public static Component string2Component(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        if (input.startsWith("{") && input.endsWith("}")) {
            String key = input.substring(1, input.length() - 1);
            return Component.translatable(key);
        }

        return Component.literal(input);
    }

    public static Item getPartsClassFromBluePrint(ItemStack bluePrint) {
        var bluePrintItem = bluePrint.getItem();

        if (!(bluePrintItem instanceof BluePrintItem)) {
            return null;
        }

        ResourceLocation id =
                BuiltInRegistries.ITEM.getKey(
                        bluePrintItem
                );

        String partsName = id.getPath().replaceAll("blue_print_", "");

        if (partsName.equals("hover_unit")) {
            return PomkotsMechs.HOVER_UNIT.get();
        } else if (partsName.equals("rail_slider_unit")) {
            return PomkotsMechs.RAIL_SLIDER.get();
        } else if (partsName.equals("proto_super_boost_unit")) {
            return PomkotsMechs.SB_PROTO.get();
        } else if (partsName.equals("builder_unit")) {
            return PomkotsMechs.BUILDER_UNIT.get();
        } else if (partsName.equals("glider_unit")) {
            return PomkotsMechs.GLIDER_UNIT.get();
        } else if (partsName.equals("deneb")) {
            return PomkotsMechs.DENEB_BODY.get();
        } else if (partsName.equals("altair")) {
            return PomkotsMechs.ALTAIR_BODY.get();
        } else if (partsName.equals("vega")) {
            return PomkotsMechs.VEGA_BODY.get();
        } else if (partsName.equals("sirius")) {
            return PomkotsMechs.SIRIUS_BODY.get();
        } else if (partsName.equals("aldebaran")) {
            return PomkotsMechs.ALDEBARAN_BODY.get();
        } else if (partsName.equals("muknvali")) {
            return PomkotsMechs.MUKNVALI_BODY.get();
        }

        Item item = BuiltInRegistries.ITEM.get(PomkotsMechs.id(partsName));

        if (item.equals(Items.AIR)) {
            return null;
        }

        return item;
    }

    public static MechAutoController createMechAutoController(LivingEntity pilot, Pmvc01Entity mech) {
        return new MechAutoController(pilot, mech);
    }

    public static void saveBlockPos(CompoundTag tag, String namePrefix, BlockPos blockPos) {
        tag.putInt(
                PomkotsMechs.nbtName(namePrefix + "X"),
                blockPos.getX()
        );

        tag.putInt(
                PomkotsMechs.nbtName(namePrefix + "Y"),
                blockPos.getY()
        );

        tag.putInt(
                PomkotsMechs.nbtName(namePrefix + "Z"),
                blockPos.getZ()
        );
    }

    public static BlockPos loadBlockPos(String namePrefix, CompoundTag tag) {
        return new BlockPos(
                tag.getInt(PomkotsMechs.nbtName(namePrefix + "X")),
                tag.getInt(PomkotsMechs.nbtName(namePrefix + "Y")),
                tag.getInt(PomkotsMechs.nbtName(namePrefix + "Z"))
        );
    }
}
