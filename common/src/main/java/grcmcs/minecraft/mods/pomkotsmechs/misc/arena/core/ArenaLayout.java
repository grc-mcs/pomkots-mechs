package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ArenaLayout {
    private List<BlockPos> gates = new ArrayList<>();
    private BlockPos battleAnchor;
    private BlockPos teleportPos;

    public BlockPos getBattleAnchor() {
        return battleAnchor;
    }

    public void setBattleAnchor(BlockPos battleAnchor) {
        this.battleAnchor = battleAnchor;
    }

    public List<BlockPos> getGates() {
        return gates;
    }

    public void setGates(List<BlockPos> gates) {
        this.gates = gates;
    }

    public BlockPos getTeleportPos() {
        return teleportPos;
    }

    public void setTeleportPos(BlockPos teleportPos) {
        this.teleportPos = teleportPos;
    }
}
