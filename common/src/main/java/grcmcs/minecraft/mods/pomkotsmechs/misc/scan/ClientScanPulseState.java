package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public final class ClientScanPulseState {

    private int age;
    private float lastRadius;

    /**
     * スキャン範囲内に存在する対象BlockEntity候補。
     */
    private final Set<BlockPos> blockEntityCandidates =
            new HashSet<>();

    /**
     * すでに波面が接触したBlockEntity。
     */
    private final Set<BlockPos> detectedBlockEntities =
            new HashSet<>();

    /**
     * すでに波面が接触したEntity。
     */
    private final Set<Integer> detectedEntityIds =
            new HashSet<>();

    public void tickAge() {
        age++;
    }

    public int getAge() {
        return age;
    }

    public float getLastRadius() {
        return lastRadius;
    }

    public void setLastRadius(float lastRadius) {
        this.lastRadius = lastRadius;
    }

    public Set<BlockPos> getBlockEntityCandidates() {
        return blockEntityCandidates;
    }

    public Set<BlockPos> getDetectedBlockEntities() {
        return detectedBlockEntities;
    }

    public Set<Integer> getDetectedEntityIds() {
        return detectedEntityIds;
    }
}
