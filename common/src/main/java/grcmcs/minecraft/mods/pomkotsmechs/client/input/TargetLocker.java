package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetLocker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);
    private static final float COSINE_THRESHOLD = Mth.cos((float)Math.toRadians(30));
    private static final float COSINE_THRESHOLD2 = Mth.cos((float)Math.toRadians(10));

    private static final TargetLocker singleton = new TargetLocker();

    public static TargetLocker getInstance() {
        return singleton;
    }

    private Minecraft minecraft = Minecraft.getInstance();
    private Entity targetSoft = null;
    private Entity targetHard = null;
    private Map<Integer, Entity> targetMulti = new HashMap<>();

    public void clearLockTargets() {
        this.targetSoft = null;
        this.targetHard = null;
        if (targetMulti != null) {
            targetMulti.clear();
        }
    }

    public void tick(DriverInput driverInput, PomkotsVehicle bot) {
        if (targetSoft != null && !targetSoft.isAlive()) {
            unlockTargetSoft();
        }
        if (targetHard != null && !targetHard.isAlive()) {
            unlockTargetHard();
        }
        if (!targetMulti.isEmpty()) {
            for (Map.Entry<Integer, Entity> entry: targetMulti.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isAlive()) {
                    targetMulti.remove(entry.getKey());
                }
            }
        }

        if (isLockingHard()) {
            if (bot.shouldLockStrong(driverInput)) {
                unlockTargetHard();
            }
        } else {
            // ソフトロックを試す
            if (bot.shouldLockWeak(driverInput)) {
                if (targetSoft == null) {
                    lockOnTargetSoft(getCrossHairTarget());
                } else {
                    if (!isInLockonTraceRange(targetSoft, COSINE_THRESHOLD)) {
                        unlockTargetSoft();
                    }
                }
            } else {
                unlockTargetSoft();
            }

            // ハードロックを試す
            if (bot.shouldLockStrong(driverInput)) {
                var tgt = findTargetHard();
                if (tgt != null) {
                    lockOnTargetHard(tgt);
                }
            }
        }

        if (lockOnMultiCooltime > 0) {
            lockOnMultiCooltime--;

        } else {
            if (bot.shouldLockMulti(driverInput)) {
                if (targetHard != null) {
                    addTargetMulti(targetHard);

                } else if (targetSoft != null) {
                    addTargetMulti(targetSoft);

                } else {
                    var ent = getCrossHairTarget();
                    if (ent != null) {
                        addTargetMulti(ent);
                    }
                }
            }
        }

        if (!driverInput.isWeaponRightShoulderPressed() && !targetMulti.isEmpty()) {
            releaseTargetMulti(bot);
        }
    }

    private int lockOnMultiCooltime = 0;

    private void addTargetMulti(Entity ent) {
        if (targetMulti.size() < 6) {
            targetMulti.put(ent.getId(), ent);
            lockOnMultiCooltime = 10;
            sendServerLockMulti(ent.getId());
            minecraft.player.playSound(PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
        }
    }

    private void releaseTargetMulti(PomkotsVehicle bot) {
        targetMulti.clear();
        bot.getLockTargets().unlockTargetMulti();
        sendServerUnlockMulti();
    }

    private void sendServerLockMulti(int entityId) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_LOCK_MULTI), buf);
    }

    private void sendServerUnlockMulti() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_UNLOCK_MULTI), buf);
    }

    private Entity findTargetHard() {
        Entity res;

        if (targetSoft != null) {
            res = targetSoft;
        } else if (getCrossHairTarget() != null) {
            res = getCrossHairTarget();
        } else  {
            Player p = minecraft.player;
            var list = getEntitiesAroundPlayer(p, 50);
            res = getClosestEntityInLookDirection(p, list);
        }

        return res;
    }

    public List<LivingEntity> getEntitiesAroundPlayer(LivingEntity player, double range) {
        Level world = player.level();
        Vec3 playerPos = player.position();

        AABB searchBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> {
            return entity != player && player.hasLineOfSight(entity) && !isSelf(entity, player);
        });

        return entities;
    }

    private boolean isSelf(LivingEntity entity, LivingEntity player) {
        if (entity instanceof PomkotsVehicleBase) {
            var driver = ((PomkotsVehicleBase) entity).getDrivingPassenger();
            if (driver == null) {
                return false;
            } else {
                return driver.equals(player);
            }
        } else {
            return false;
        }
    }

    private Entity getClosestEntityInLookDirection(LivingEntity player, List<LivingEntity> entities) {
        double maxCosineSimilarity = -1.0;
        LivingEntity closestEntity = null;

        Vec3 lookVector = player.getLookAngle().normalize();

        Vec3 playerPos = player.position();

        for (LivingEntity entity : entities) {
            Vec3 entityVector = entity.position().subtract(playerPos).normalize();

            double cosineSimilarity = lookVector.dot(entityVector);

            if (cosineSimilarity > maxCosineSimilarity && !isSelf(entity, player)) {
                maxCosineSimilarity = cosineSimilarity;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    public boolean isLockingHard() {
        return targetHard != null;
    }

    private void lockOnTargetHard(Entity ent) {
        if (ent != null) {
            sendServerLockHard(ent.getId());
            targetHard = ent;
            minecraft.player.playSound(PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
        }
    }

    private void unlockTargetHard() {
        if (targetHard != null) {
            sendServerUnlockHard();
        }
        targetHard = null;
    }

    private void sendServerLockHard(int entityId) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_LOCK_HARD), buf);
    }

    private void sendServerUnlockHard() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_UNLOCK_HARD), buf);
    }

    public boolean turn(LocalPlayer player) {
        if (Utils.isRidingPomkotsVehicle(player)) {
            if (targetHard != null) {
                if (!targetHard.isAlive()) {
                    this.unlockTargetHard();
                    return false;
                }

                Vec3 targetPos = targetHard.position().add(0, targetHard.getBbHeight() - targetHard.getBbHeight() / 2, 0);
                Vec3 targetVec = targetPos.subtract(player.position().add(0, player.getEyeHeight(), 0)).normalize();

                double angleX = Mth.wrapDegrees(Math.atan2(-targetVec.x, targetVec.z) * 180 / Math.PI);
                double angleY = Math.atan2(targetVec.y, targetVec.horizontalDistance()) * 180 / Math.PI;

                double xRot = Mth.wrapDegrees(player.getXRot());
                double yRot = Mth.wrapDegrees(player.getYRot());

                double toTurnX = Mth.wrapDegrees(yRot - angleX);
                double toTurnY = Mth.wrapDegrees(xRot + angleY);

                player.turn(-toTurnX, -toTurnY);

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isLockingSoft() {
        return targetHard != null;
    }

    private void lockOnTargetSoft(Entity ent) {
        if (ent != null) {
            sendServerLockSoft(ent.getId());
            targetSoft = ent;
        }
    }

    private void unlockTargetSoft() {
        if (targetSoft != null) {
            sendServerUnlocksoft();
        }
        targetSoft = null;
    }

    private boolean isInLockonTraceRange(Entity ent, float cosineThreshold) {
        Camera cam = minecraft.gameRenderer.getMainCamera();

        Vec3 playerToEnemy = ent.position().subtract(cam.getPosition()).normalize();
        Vec3 playerLookDirection = new Vec3(cam.getLookVector().normalize());

        double dotProduct = playerToEnemy.dot(playerLookDirection);

        return dotProduct >= cosineThreshold;
    }

    public Entity getCrossHairTarget() {
        var list = getEntitiesAroundPlayer(minecraft.player, 80);

        for (var ent: list) {
            if (isInLockonTraceRange(ent, COSINE_THRESHOLD2)) {
                return ent;
            }
        }
        return null;
    }

    private void sendServerLockSoft(int entityId) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_LOCK_SOFT), buf);
    }

    private void sendServerUnlocksoft() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_UNLOCK_SOFT), buf);
    }

    public static final int SOFT = 0;
    public static final int HARD = 1;
    public static final int MULTI = 2;
    public static final int NONE = 3;

    public int isEntityLocked(Entity ent) {
        if (targetSoft == ent) {
            return SOFT;
        } else if (targetHard == ent) {
            return HARD;
        } else if (targetMulti.containsKey(ent.getId())) {
            return MULTI;
        } else {
            return NONE;
        }
    }

    // ロックオンターゲットの取得を色々試した残骸。いつか使うかもしんないので取っておく

    private Entity getCrossHairTargetFromPick() {
        HitResult hit = minecraft.gameRenderer.getMainCamera().getEntity().pick(1000, 1.0F, true);
        if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            Entity entity = entityHit.getEntity();

            return entity;
        } else {
            return null;
        }
    }

    private Entity getCrossHairTargetFromCameraDirection() {
        Entity entity = this.minecraft.getCameraEntity();
        Entity crosshairPickEntity = null;

        double maxDistance = 1000;
        HitResult hitResult = entity.pick(maxDistance, 1, false);
        Vec3 vec3 = entity.getEyePosition(1);
        boolean bl = false;
        double e = maxDistance;

        if (true) {
            e = 6.0;
            maxDistance = e;
        } else {
            if (e > 3.0) {
                bl = true;
            }

            maxDistance = e;
        }

        e *= e;
        if (hitResult != null) {
            e = hitResult.getLocation().distanceToSqr(vec3);
        }


        Vec3 vec32 = entity.getViewVector(1.0F);
        Vec3 vec33 = vec3.add(vec32.x * maxDistance, vec32.y * maxDistance, vec32.z * maxDistance);
        float g = 1.0F;
        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(maxDistance)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, (entityx) -> {
            return !entityx.isSpectator() && entityx.isPickable();
        }, e);

        if (entityHitResult != null) {
            Entity entity2 = entityHitResult.getEntity();
            Vec3 vec34 = entityHitResult.getLocation();

            double h = vec3.distanceToSqr(vec34);
            if (bl && h > 9.0) {
                hitResult = BlockHitResult.miss(vec34, Direction.getNearest(vec32.x, vec32.y, vec32.z), BlockPos.containing(vec34));
            } else if (h < e || hitResult == null) {
                hitResult = entityHitResult;
                if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrame) {
                    crosshairPickEntity = entity2;
                }
            }
        }

        return crosshairPickEntity;
    }

    public Entity getCrossHairTargetFromLookAtDirection(Player player, double maxDistance) {
        Vec3 startVec = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F).scale(maxDistance);
        Vec3 endVec = startVec.add(lookVec);

        ClipContext context = new ClipContext(startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        HitResult hitResult = player.level().clip(context);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            if (entityHitResult != null && entityHitResult.getEntity() != player.getVehicle()) {
                return entityHitResult.getEntity();
            }
        }

        return null;
    }
}
