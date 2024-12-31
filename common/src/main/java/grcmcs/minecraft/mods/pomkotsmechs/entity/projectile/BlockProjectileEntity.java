package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockProjectileEntity extends PomkotsThrowableProjectile {
    private static final int MAX_LIFE_TICKS = 40;
    private int lifeTicks = 0;

    public BlockProjectileEntity(EntityType<? extends BlockProjectileEntity> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        Level world = this.level();
        BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());

        var bs = getBlockState();
        if (!world.isClientSide() && bs != null) {
            world.setBlock(pos, bs, 3);
        }

        this.discard();
    }

    private static final EntityDataAccessor<String> BLOCK_REGISTRY_NAME = SynchedEntityData.defineId(BlockProjectileEntity.class, EntityDataSerializers.STRING);
    private BlockState cachedBlockState;

    @Override
    protected void defineSynchedData() {
        this.entityData.define(BLOCK_REGISTRY_NAME, "minecraft:air");
    }

    public void setBlock(Block block) {
        this.entityData.set(BLOCK_REGISTRY_NAME, BuiltInRegistries.BLOCK.getKey(block).toString());
        this.cachedBlockState = block.defaultBlockState();
    }

    public BlockState getBlockState() {
        if (this.cachedBlockState == null) {
            String registryName = this.entityData.get(BLOCK_REGISTRY_NAME);

            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(registryName));
            this.cachedBlockState = block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        return this.cachedBlockState;
    }
}

