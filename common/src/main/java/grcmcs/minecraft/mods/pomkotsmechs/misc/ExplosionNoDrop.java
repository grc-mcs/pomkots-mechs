package grcmcs.minecraft.mods.pomkotsmechs.misc;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ExplosionNoDrop extends Explosion {

    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    private final BlockInteraction blockInteraction;

    public ExplosionNoDrop(Level level, @Nullable Entity entity, double x, double y, double z, float scale, boolean fire, BlockInteraction blockInteraction) {
        super(level, entity, x, y, z, scale, fire, blockInteraction);
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockInteraction = blockInteraction;
    }

    @Override
    public void finalizeExplosion(boolean bl) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean bl2 = this.interactsWithBlocks();
        if (bl) {
            if (!(this.radius < 2.0F) && bl2) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }

        if (bl2) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList();
            ObjectArrayList<BlockPos> toBlow = (ObjectArrayList<BlockPos>)this.getToBlow();
            Util.shuffle(toBlow, this.level.random);
            ObjectListIterator var5 = toBlow.iterator();

            while(var5.hasNext()) {
                BlockPos blockPos = (BlockPos)var5.next();
                BlockState blockState = this.level.getBlockState(blockPos);
                net.minecraft.world.level.block.Block block = blockState.getBlock();
                if (!blockState.isAir()) {
                    this.level.getProfiler().push("explosion_blocks");
                    this.level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    block.wasExploded(this.level, blockPos, this);
                    this.level.getProfiler().pop();
                }
            }

            var5 = objectArrayList.iterator();

            while(var5.hasNext()) {
                Pair<ItemStack, BlockPos> pair = (Pair)var5.next();
                net.minecraft.world.level.block.Block.popResource(this.level, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
            }
        }
    }
}
