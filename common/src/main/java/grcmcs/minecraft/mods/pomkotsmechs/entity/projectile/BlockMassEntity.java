package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockMassEntity extends LivingEntity {
    private static final int SIZE = 4; // 4x4x4のサイズ
    private BlockState[][][] blockStates = new BlockState[SIZE][SIZE][SIZE];
    private boolean isInitialized = false;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public BlockMassEntity(EntityType<? extends BlockMassEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    @Override
    public void tick() {
        super.tick();
        // 重力を適用
        if (!this.isNoGravity()) {
            this.addDeltaMovement(new Vec3(0, -0.08, 0));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
        }

        if (this.onGround()) {
            if (!this.level().isClientSide) {
                placeBlocks();
                this.discard();
            } else {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), PomkotsMechs.SE_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    public BlockState[][][] getBlockStates() {
        if (!isInitialized) {
            this.initializeBlocks(getBlockState());
        }
        return this.blockStates;
    }

    // ブロックの配置処理
    private void placeBlocks() {
        Level serverLevel = this.level();
        BlockPos basePos = this.blockPosition();

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockState state = blockStates[x][y][z];
                    if (state != null) {
                        BlockPos pos = basePos.offset(x-2, y, z-2);
                        serverLevel.setBlock(pos, state, 3);
                    }
                }
            }
        }
    }

    public void initializeBlocksServer(BlockState blockState) {
        this.initializeBlocks(blockState);
        this.setBlock(blockState.getBlock());
    }

    public void initializeBlocks(BlockState blockState) {
        // 配列にブロックを格納（単一ブロックで初期化）
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    blockStates[x][y][z] = blockState;
                }
            }
        }
        this.isInitialized = true;
    }

    private static final EntityDataAccessor<String> BLOCK_REGISTRY_NAME = SynchedEntityData.defineId(BlockMassEntity.class, EntityDataSerializers.STRING);
    private BlockState cachedBlockState;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
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

//    // Abstractクラスの仮実装
//    @Override
//    protected void readAdditionalSaveData(CompoundTag compoundTag) {
//
//    }
//
//    @Override
//    protected void addAdditionalSaveData(CompoundTag compoundTag) {
//
//    }


    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }
}
