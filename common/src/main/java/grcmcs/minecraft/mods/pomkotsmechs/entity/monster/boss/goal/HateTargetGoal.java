package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

// HateTargetGoal.java
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HateTargetGoal extends Goal {
    private final BaseBossEntity mob;
    private final double maxHateRange;
    private final double proximityRange;
    private final Map<UUID, HateData> hateMap;
    private LivingEntity currentTarget;
    private int recheckDelay;

    // ヘイト管理の設定値
    private static final float HATE_DECAY_RATE = 100f; // 毎秒のヘイト減衰量
    private static final float PROXIMITY_HATE_RATE = 1.0f; // 近接時の毎秒ヘイト増加量
    private static final float DISTANCE_DECAY_MULTIPLIER = 10.0f; // 距離による減衰倍率
    private static final int RECHECK_INTERVAL = 20; // ターゲット再選択間隔（tick）

    public HateTargetGoal(BaseBossEntity mob, double maxHateRange, double proximityRange) {
        this.mob = mob;
        this.maxHateRange = maxHateRange;
        this.proximityRange = proximityRange;
        this.hateMap = new ConcurrentHashMap<>();
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        updateHateValues();
        LivingEntity newTarget = selectHighestHateTarget();

        if (newTarget != currentTarget) {
            currentTarget = newTarget;
            mob.setTarget(currentTarget);
        }

        return currentTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (--recheckDelay <= 0) {
            recheckDelay = RECHECK_INTERVAL;
            updateHateValues();

            LivingEntity newTarget = selectHighestHateTarget();
            if (newTarget != currentTarget) {
                currentTarget = newTarget;
                mob.setTarget(currentTarget);
            }
        }
        return currentTarget != null && currentTarget.isAlive() &&
                mob.distanceToSqr(currentTarget) <= maxHateRange * maxHateRange;
    }

    @Override
    public void start() {
        recheckDelay = RECHECK_INTERVAL;
    }

    @Override
    public void stop() {
        currentTarget = null;
        mob.setTarget(null);
    }

    /**
     * エンティティからダメージを受けた時のヘイト追加
     */
    public void addDamageHate(LivingEntity entity, float damage) {
        if (entity == null || !isValidTarget(entity)) return;

        UUID entityId = entity.getUUID();
        HateData hateData = hateMap.computeIfAbsent(entityId, k -> new HateData());

        // ダメージ量に応じてヘイト追加（ダメージ×2倍）
        hateData.addHate(damage * 2.0f);
    }

    /**
     * カスタムヘイト追加（スキル使用など）
     */
    public void addCustomHate(LivingEntity entity, float hate) {
        if (entity == null || !isValidTarget(entity)) return;

        UUID entityId = entity.getUUID();
        HateData hateData = hateMap.computeIfAbsent(entityId, k -> new HateData());
        hateData.addHate(hate);
    }

    /**
     * ヘイト値を強制設定
     */
    public void setHate(LivingEntity entity, float hate) {
        if (entity == null || !isValidTarget(entity)) return;

        UUID entityId = entity.getUUID();
        HateData hateData = hateMap.computeIfAbsent(entityId, k -> new HateData());
        hateData.setHate(hate);
    }

    /**
     * エンティティのヘイト値を取得
     */
    public float getHate(LivingEntity entity) {
        if (entity == null) return 0;
        HateData hateData = hateMap.get(entity.getUUID());
        return hateData != null ? hateData.getHate() : 0;
    }

    /**
     * 有効なターゲットかどうかを確認
     */
    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive()) return false;

        // 自分自身は対象外
        if (mob.isSelf(entity)) return false;

        // チーム判定
        Team mobTeam = mob.getTeam();
        Team entityTeam = entity.getTeam();

        // 両方ともチームに所属している場合
        if (mobTeam != null && entityTeam != null) {
            // 同じチームの場合は対象外
            if (mobTeam.equals(entityTeam)) return false;

            // チーム間の関係をチェック（友好関係の場合は対象外）
            if (mobTeam.isAlliedTo(entityTeam)) return false;
        }

        return true;
    }

    /**
     * ヘイト値を更新（減衰・近接増加・距離減衰）
     */
    private void updateHateValues() {
        // 既存のヘイトデータを更新
        Iterator<Map.Entry<UUID, HateData>> iterator = hateMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, HateData> entry = iterator.next();
            UUID entityId = entry.getKey();
            HateData hateData = entry.getValue();

            // エンティティを検索
            Entity entity = findEntityById(entityId);

            if (entity instanceof LivingEntity le) {
                double distance = mob.distanceToSqr(entity);

                if (!le.isAlive()) {
                    iterator.remove();

                } else if (distance > maxHateRange * maxHateRange) {
                    // エンティティが範囲外の場合は一気にヘイト減少
                    hateData.decay(HATE_DECAY_RATE * 2.0f / 20.0f);

                } else if (isValidTarget(le)) {
                    // 近接ヘイト増加
                    if (distance <= proximityRange * proximityRange) {
                        hateData.addHate(PROXIMITY_HATE_RATE / 20.0f); // tick単位で計算
                    }

                    // 時間経過による減衰
                    float decayRate = HATE_DECAY_RATE / 20.0f; // tick単位

                    // 距離による減衰倍率
                    if (distance > proximityRange * proximityRange) {
                        double distanceRatio = Math.sqrt(distance) / maxHateRange;
                        decayRate *= (1.0f + DISTANCE_DECAY_MULTIPLIER * distanceRatio);
                    }

                    hateData.decay(decayRate);

                    // ヘイトが0以下になったら削除
                    if (hateData.getHate() <= 0) {
                        iterator.remove();
                    }
                } else {
                    hateData.decay(HATE_DECAY_RATE * 2.0f / 20.0f);
                }
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * 最もヘイトの高いターゲットを選択
     */
    private LivingEntity selectHighestHateTarget() {
        LivingEntity bestTarget = null;
        float highestHate = 0;

        for (Map.Entry<UUID, HateData> entry : hateMap.entrySet()) {
            if (entry.getValue().getHate() > highestHate) {
                Entity entity = findEntityById(entry.getKey());

                if (entity instanceof LivingEntity le && le.isAlive() && isValidTarget(le) &&
                        mob.distanceToSqr(le) <= maxHateRange * maxHateRange) {
                    bestTarget = le;
                    highestHate = entry.getValue().getHate();
                }
            }
        }

        return bestTarget;
    }

    /**
     * 近くのLivingEntityを取得
     */
    private List<LivingEntity> getNearbyEntities() {
        AABB searchBox = new AABB(
                mob.getX() - maxHateRange, mob.getY() - maxHateRange, mob.getZ() - maxHateRange,
                mob.getX() + maxHateRange, mob.getY() + maxHateRange, mob.getZ() + maxHateRange
        );

        return mob.level().getEntitiesOfClass(LivingEntity.class, searchBox, this::isValidTarget);
    }

    /**
     * UUIDでエンティティを検索
     */
    private Entity findEntityById(UUID entityId) {
        return ((ServerLevel)mob.level()).getEntity(entityId);
    }

    /**
     * ヘイトデータを格納するクラス
     */
    private static class HateData {
        private float hate;

        public HateData() {
            this.hate = 0;
        }

        public void addHate(float amount) {
            this.hate = Math.min(1000, this.hate + amount);
        }

        public void setHate(float hate) {
            this.hate = Math.min(1000, hate);
        }

        public void decay(float amount) {
            this.hate = Math.max(1, this.hate - amount);
        }

        public float getHate() {
            return hate;
        }

        @Override
        public String toString() {
            return "" + hate;
        }
    }

    // デバッグ用メソッド
    public Map<UUID, Float> getHateMap() {
        Map<UUID, Float> result = new HashMap<>();
        for (Map.Entry<UUID, HateData> entry : hateMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getHate());
        }
        return result;
    }

    public void clearHate() {
        hateMap.clear();
        currentTarget = null;
        mob.setTarget(null);
    }

    public void clearHate(LivingEntity entity) {
        if (entity != null) {
            hateMap.remove(entity.getUUID());
            if (currentTarget == entity) {
                currentTarget = null;
                mob.setTarget(null);
            }
        }
    }
}