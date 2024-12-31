package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class BigMobNavigation extends GroundPathNavigation {

    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);
    private static final double BASE_SIZE = 0.3;

    public BigMobNavigation(Mob entity, Level level) {
        super(entity, level);
    }

    @Override
    protected Path createPath(Set<BlockPos> set, int i, boolean bl, int j, float f) {
        // パスを追従する前にAABBを縮小
        AABB originalBoundingBox = adjustBoundingBoxForPathfinding();

        var res = super.createPath(set, i, bl, j, f);// 通常のパス追従を実行

        // パス追従後にAABBを元に戻す
        restoreOriginalBoundingBox(originalBoundingBox);

        return res;
    }

    @Override
    public void tick() {
        // パスを追従する前にAABBを縮小
        AABB originalBoundingBox = adjustBoundingBoxForPathfinding();

        super.tick(); // 通常のパス追従を実行

        // パス追従後にAABBを元に戻す
        restoreOriginalBoundingBox(originalBoundingBox);
    }

    private AABB adjustBoundingBoxForPathfinding() {
        // パスファインディング中、一時的にエンティティのAABBを縮小
        AABB originalBoundingBox = this.mob.getBoundingBox();

        double sizeDiff = Math.abs(originalBoundingBox.getXsize() / 2 - BASE_SIZE);

//        AABB smallerBoundingBox = new AABB(-0.3, 0, -0.3, 0.3, originalBoundingBox.getYsize(), 0.3);
        AABB smallerBoundingBox = originalBoundingBox.inflate(-sizeDiff);

        this.mob.setBoundingBox(smallerBoundingBox);

        return originalBoundingBox;
    }

    private void restoreOriginalBoundingBox(AABB originalBoundingBox) {
        // 元のAABBに戻す
        this.mob.setBoundingBox(originalBoundingBox);
    }

//
//    @Override
//    protected boolean canUpdatePath() {
//        // 通常の位置が有効かを確認する
//        if (this.mob.getBoundingBox().getSize() > 1.0) {
//            // エンティティのサイズが大きい場合、カスタムの位置判定を行う
//            BlockPos pos = this.mob.blockPosition();
//            // カスタムロジックで広い範囲を調べる、または他の判定を追加
//            return this.level.noCollision(this.mob, new AABB(pos).inflate(0.5));
//        }
//        return super.canUpdatePath();
//    }
//
//    // カスタムパス計算を追加して、ヒットボックスの大きなエンティティに最適化する
//    @Override
//    protected boolean canStartAt(BlockPos pos) {
//        // エンティティの大きさを考慮して、パスが通れるかの判定をカスタマイズ
//        if (this.entity.getBoundingBox().getAverageEdgeLength() > 1.0) {
//            // 大きなヒットボックスの場合の処理
//            return this.world.isSpaceEmpty(this.entity, new Box(pos).expand(0.5));
//        }
//        WalkNodeEvaluator
//        return super.canPathThrough(pos);
//    }
}

