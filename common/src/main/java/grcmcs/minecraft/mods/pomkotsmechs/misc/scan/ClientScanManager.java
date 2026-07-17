package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import grcmcs.minecraft.mods.pomkotsmechs.block.ScanTargetBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.tags.ModEntityTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientScanManager {

    /**
     * ScanPulseEntity ID → クライアント側の走査状態。
     */
    private static final Map<Integer, ClientScanPulseState>
            ACTIVE_PULSES = new HashMap<>();

    /**
     * Entity ID → ハイライト終了時刻。
     */
    private static final Map<Integer, Long>
            ENTITY_HIGHLIGHT_END = new HashMap<>();

    /**
     * BlockEntity位置 → ハイライト終了時刻。
     */
    private static final Map<BlockPos, Long>
            BLOCK_ENTITY_HIGHLIGHT_END = new HashMap<>();

    /**
     * BlockEntity候補を再収集する間隔。
     *
     * ScanPulseEntity同期後に周辺チャンクが遅れて読み込まれた場合の
     * 取りこぼし防止として、定期的に再走査する。
     */
    private static final int BLOCK_ENTITY_REFRESH_INTERVAL = 10;

    private ClientScanManager() {
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player localPlayer = minecraft.player;

        if (level == null || localPlayer == null) {
            clear();
            return;
        }

        long gameTime = level.getGameTime();

        ENTITY_HIGHLIGHT_END.entrySet().removeIf(
                entry -> entry.getValue() <= gameTime
        );

        BLOCK_ENTITY_HIGHLIGHT_END.entrySet().removeIf(
                entry -> entry.getValue() <= gameTime
        );

        Map<Integer, ScanPulseEntity> currentPulses =
                new HashMap<>();

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ScanPulseEntity pulse)) {
                continue;
            }

            /*
             * 自分に公開されていないスキャンは、
             * 描画も走査もしない。
             */
            if (!pulse.isVisibleTo(localPlayer)) {
                continue;
            }

            currentPulses.put(pulse.getId(), pulse);

            tickPulse(
                    level,
                    pulse
            );
        }

        /*
         * 消滅したScanPulseEntityのクライアント状態を削除。
         */
        ACTIVE_PULSES.keySet().removeIf(
                id -> !currentPulses.containsKey(id)
        );
    }

    private static void tickPulse(
            ClientLevel level,
            ScanPulseEntity pulse
    ) {
        ClientScanPulseState state =
                ACTIVE_PULSES.computeIfAbsent(
                        pulse.getId(),
                        ignored -> new ClientScanPulseState()
                );

        float currentRadius =
                pulse.getRadius(0.0F);

        float previousRadius =
                state.getLastRadius();

        /*
         * 初回と、その後10tickごとに対象BlockEntityを再収集。
         */
        if (state.getAge() % BLOCK_ENTITY_REFRESH_INTERVAL == 0) {
            collectBlockEntityCandidates(
                    level,
                    pulse,
                    state,
                    currentRadius
            );
        }

        scanEntities(
                level,
                pulse,
                state,
                previousRadius,
                currentRadius
        );

        scanBlockEntities(
                level,
                pulse,
                state,
                previousRadius,
                currentRadius
        );

        state.setLastRadius(currentRadius);
        state.tickAge();
    }

    // =========================================================
    // Entity走査
    // =========================================================

    private static void scanEntities(
            ClientLevel level,
            ScanPulseEntity pulse,
            ClientScanPulseState state,
            float previousRadius,
            float currentRadius
    ) {
        if (currentRadius <= 0.0F) {
            return;
        }

        Vec3 center = pulse.position();

        AABB searchBox = new AABB(
                center.x - currentRadius,
                center.y - currentRadius,
                center.z - currentRadius,
                center.x + currentRadius,
                center.y + currentRadius,
                center.z + currentRadius
        );

        for (Entity entity : level.getEntities(
                pulse,
                searchBox,
                ClientScanManager::isEntityScanTarget
        )) {
            if (state.getDetectedEntityIds().contains(entity.getId())) {
                continue;
            }

            double distance =
                    distanceToAabb(
                            center,
                            entity.getBoundingBox()
                    );

            if (didWaveReachTarget(
                    previousRadius,
                    currentRadius,
                    distance
            )) {
                state.getDetectedEntityIds().add(
                        entity.getId()
                );

                ENTITY_HIGHLIGHT_END.put(
                        entity.getId(),
                        level.getGameTime()
                                + pulse.getHighlightTicks()
                );
            }
        }
    }

    private static boolean isEntityScanTarget(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        return entity instanceof Pmvc01Entity
                || entity.getType().is(ModEntityTags.SCAN_TARGETS);
    }

    private static double distanceToAabb(
            Vec3 center,
            AABB box
    ) {
        double nearestX =
                Mth.clamp(center.x, box.minX, box.maxX);

        double nearestY =
                Mth.clamp(center.y, box.minY, box.maxY);

        double nearestZ =
                Mth.clamp(center.z, box.minZ, box.maxZ);

        return center.distanceTo(
                new Vec3(
                        nearestX,
                        nearestY,
                        nearestZ
                )
        );
    }

    // =========================================================
    // BlockEntity候補収集
    // =========================================================

    private static void collectBlockEntityCandidates(
            ClientLevel level,
            ScanPulseEntity pulse,
            ClientScanPulseState state,
            float currentRadius
    ) {
        Vec3 center = pulse.position();
        float maxRadius = pulse.getMaxRadius();

        int minChunkX =
                SectionPos.blockToSectionCoord(
                        Mth.floor(center.x - maxRadius)
                );

        int maxChunkX =
                SectionPos.blockToSectionCoord(
                        Mth.floor(center.x + maxRadius)
                );

        int minChunkZ =
                SectionPos.blockToSectionCoord(
                        Mth.floor(center.z - maxRadius)
                );

        int maxChunkZ =
                SectionPos.blockToSectionCoord(
                        Mth.floor(center.z + maxRadius)
                );

        double maxRadiusSqr =
                maxRadius * maxRadius;

        for (int chunkX = minChunkX;
             chunkX <= maxChunkX;
             chunkX++) {

            for (int chunkZ = minChunkZ;
                 chunkZ <= maxChunkZ;
                 chunkZ++) {

                /*
                 * getChunkNowを使い、未ロードチャンクを
                 * クライアント側から強制ロードしない。
                 */
                LevelChunk chunk =
                        level.getChunkSource()
                                .getChunkNow(
                                        chunkX,
                                        chunkZ
                                );

                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity
                        : chunk.getBlockEntities().values()) {

                    if (!(blockEntity
                            instanceof ScanTargetBlockEntity)) {
                        continue;
                    }

                    BlockPos pos =
                            blockEntity.getBlockPos().immutable();

                    double distanceSqr =
                            Vec3.atCenterOf(pos)
                                    .distanceToSqr(center);

                    if (distanceSqr > maxRadiusSqr) {
                        continue;
                    }

                    boolean newlyAdded =
                            state.getBlockEntityCandidates()
                                    .add(pos);

                    /*
                     * 後からクライアントへ同期されたBlockEntityが、
                     * すでに波面の内側にいた場合の補完処理。
                     */
                    if (newlyAdded) {
                        detectLateLoadedBlockEntity(
                                level,
                                pulse,
                                state,
                                pos,
                                currentRadius
                        );
                    }
                }
            }
        }
    }

    private static void detectLateLoadedBlockEntity(
            ClientLevel level,
            ScanPulseEntity pulse,
            ClientScanPulseState state,
            BlockPos pos,
            float currentRadius
    ) {
        if (state.getDetectedBlockEntities().contains(pos)) {
            return;
        }

        double distance =
                pulse.position().distanceTo(
                        Vec3.atCenterOf(pos)
                );

        if (distance > currentRadius) {
            return;
        }

        state.getDetectedBlockEntities().add(pos);

        BLOCK_ENTITY_HIGHLIGHT_END.put(
                pos,
                level.getGameTime()
                        + pulse.getHighlightTicks()
        );
    }

    // =========================================================
    // BlockEntity波面判定
    // =========================================================

    private static void scanBlockEntities(
            ClientLevel level,
            ScanPulseEntity pulse,
            ClientScanPulseState state,
            float previousRadius,
            float currentRadius
    ) {
        Vec3 center = pulse.position();

        Iterator<BlockPos> iterator =
                state.getBlockEntityCandidates().iterator();

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            BlockEntity blockEntity =
                    level.getBlockEntity(pos);

            /*
             * チャンクアンロード、破壊、別ブロックへの置換など。
             */
            if (!(blockEntity
                    instanceof ScanTargetBlockEntity)) {

                iterator.remove();

                state.getDetectedBlockEntities()
                        .remove(pos);

                continue;
            }

            if (state.getDetectedBlockEntities().contains(pos)) {
                continue;
            }

            double distance =
                    center.distanceTo(
                            Vec3.atCenterOf(pos)
                    );

            if (!didWaveReachTarget(
                    previousRadius,
                    currentRadius,
                    distance
            )) {
                continue;
            }

            state.getDetectedBlockEntities().add(pos);

            BLOCK_ENTITY_HIGHLIGHT_END.put(
                    pos,
                    level.getGameTime()
                            + pulse.getHighlightTicks()
            );
        }
    }

    /**
     * 前回半径と現在半径の間に対象距離があるか。
     */
    private static boolean didWaveReachTarget(
            double previousRadius,
            double currentRadius,
            double targetDistance
    ) {
        return previousRadius < targetDistance
                && targetDistance <= currentRadius;
    }

    // =========================================================
    // 描画側から参照するAPI
    // =========================================================

    public static boolean isEntityHighlighted(int entityId) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return false;
        }

        Long endTime =
                ENTITY_HIGHLIGHT_END.get(entityId);

        return endTime != null
                && endTime > minecraft.level.getGameTime();
    }

    public static boolean isBlockEntityHighlighted(
            BlockPos pos
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return false;
        }

        Long endTime =
                BLOCK_ENTITY_HIGHLIGHT_END.get(pos);

        return endTime != null
                && endTime > minecraft.level.getGameTime();
    }

    public static Iterable<BlockPos>
    getHighlightedBlockEntities() {
        return BLOCK_ENTITY_HIGHLIGHT_END.keySet();
    }

    public static boolean isBlockHighlightEmpty() {
        return BLOCK_ENTITY_HIGHLIGHT_END.isEmpty();
    }

    public static void clear() {
        ACTIVE_PULSES.clear();
        ENTITY_HIGHLIGHT_END.clear();
        BLOCK_ENTITY_HIGHLIGHT_END.clear();
    }
}