package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BossBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PomkotsCubeBlockEntity extends ChestBlockEntity implements GeoBlockEntity {
    private final EntityType [] bossList = {
        PomkotsMechs.PMB01.get(),
        PomkotsMechs.PMB02.get(),
        PomkotsMechs.PMB03.get()
    };
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterList = new ArrayList<>();

    private List<UUID> spawnedMonsters = new ArrayList<>();
    private int summonActionTickCount = 0;

    public PomkotsCubeBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), pos, state);

        generateTargetMonsterList.add(PomkotsMechs.PMS01.get());
        generateTargetMonsterList.add(PomkotsMechs.PMS02.get());
        generateTargetMonsterList.add(PomkotsMechs.PMS03.get());
        generateTargetMonsterList.add(PomkotsMechs.PMS04.get());
        generateTargetMonsterList.add(PomkotsMechs.PMS05.get());
    }

    public void openBox() {
        switch (getMode()) {
            case PomkotsCubeBlockEntity.MODE_BLUE:
                break;
            case PomkotsCubeBlockEntity.MODE_YELLOW:
            case PomkotsCubeBlockEntity.MODE_RED:
                if (!this.level.isClientSide) {
                    if (spawnedMonsters.isEmpty()) {
                        summonActionTickCount = 1;
                    }
                } else {
                    playOpenFailAnimation();
                }
                break;
            case PomkotsCubeBlockEntity.MODE_PURPLE:
                playOpenFailAnimation();
                break;
            default:
                playOpenFailAnimation();
                break;
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, PomkotsCubeBlockEntity entity) {
        entity.tick();
    }

    public void tick() {
        if (level != null && !level.isClientSide) {
            if (!spawnedMonsters.isEmpty()) {
                spawnedMonsters.removeIf(uuid -> {
                    Entity e = ((ServerLevel) level).getEntity(uuid);
                    return e == null || !e.isAlive();
                });

                if (spawnedMonsters.isEmpty()) {
                    updateMode(MODE_BLUE);
                }

                summonActionTickCount = 0;
            } else if (summonActionTickCount > 0) {
                summonActionTickCount++;

                switch(getMode()) {
                    case PomkotsCubeBlockEntity.MODE_YELLOW :
                        if (summonActionTickCount == 10) {
                            AlertEntity e = new AlertEntity(PomkotsMechs.ALERT.get(), level);
                            var bp = this.getBlockPos();
                            e.setPos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
                            level.addFreshEntity(e);
                        } else if (summonActionTickCount > 50) {
                            summonMobs(this.level, this.getBlockPos(), 5);
                            summonActionTickCount = 0;
                        }
                        break;
                    case PomkotsCubeBlockEntity.MODE_RED:
                        if (summonActionTickCount == 10) {
                            AlertEntity e = new AlertEntity(PomkotsMechs.ALERTRED.get(), level);
                            var bp = this.getBlockPos();
                            e.setPos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
                            level.addFreshEntity(e);
                        } else if (summonActionTickCount == 60) {
                            var bPos = this.getBlockPos();
                            var bosses = this.level.getEntitiesOfClass(BaseBossEntity.class, new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 1, bPos.getZ() + 1).inflate(40));
                            var players = this.level.getEntitiesOfClass(Player.class, new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 1, bPos.getZ() + 1).inflate(100));

                            if (!bosses.isEmpty()) {
                                for (var boss: bosses) {
                                    if (boss.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
                                        boss.boot();

                                        for (var player: players) {
                                            boss.addHateToEntity(player, 50);
                                        }
                                        this.spawnedMonsters.add(boss.getUUID());
                                    }
                                }
                            }
                        }
                        break;
//                    case PomkotsCubeBlockEntity.MODE_RED:
//                        if (summonActionTickCount == 10) {
//                            AlertEntity e = new AlertEntity(PomkotsMechs.ALERTRED.get(), level);
//                            var bp = this.getBlockPos();
//                            e.setPos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
//                            level.addFreshEntity(e);
//                        } else if (summonActionTickCount == 60) {
//                            summonBossBoxBehindChest(this.level, this.getBlockPos(), this.getBlockState());
//                        } else if (summonActionTickCount == 90) {
//                            summonBossBehindChest(this.level, this.getBlockPos(), this.getBlockState());
//                            summonActionTickCount = 0;
//                        }
//                        break;
                }
            }
        }
    }

    private void summonMobs(Level level, BlockPos centerPos, int count) {
        if (!level.isClientSide && spawnedMonsters.isEmpty()) {
            Random rand = new Random();

            int spawned = 0;
            for (int i = 0; i < 100 && spawned < count; i++) {
                int x = centerPos.getX() + rand.nextInt(100) - 50;
                int z = centerPos.getZ() + rand.nextInt(100) - 50;

                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos spawnPos = new BlockPos(x, y, z);

                if (level.getBlockState(spawnPos).isAir() && level.getBlockState(spawnPos.below()).isSolid()) {
                    BaseSmallMonsterEntity mob = getRandomMobType(rand).create(level);
                    if (mob != null) {
                        mob.moveTo(x + 0.5, y, z + 0.5, rand.nextFloat() * 360F, 0);
                        mob.setPersistence(true);
                        level.addFreshEntity(mob);
                        spawned++;
                        spawnedMonsters.add(mob.getUUID());
                    }
                }
            }
            if (!spawnedMonsters.isEmpty()) {
                this.setChanged();
                BlockState state = this.getBlockState();
                this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            }
        }
    }

    public void summonBossBoxBehindChest(Level level, BlockPos chestPos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction behind = facing.getOpposite();

        BlockPos offset = chestPos.relative(behind, 15);

        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, offset);

        BossBoxEntity boss = PomkotsMechs.BOSSBOX.get().create(level);
        if (boss != null) {
            boss.setPos(offset.getX() + 0.5, offset.getY(), offset.getZ() + 0.5);
            level.addFreshEntity(boss);
        }
    }

    public void summonBossBehindChest(Level level, BlockPos chestPos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction behind = facing.getOpposite();

        BlockPos offset = chestPos.relative(behind, 15);

        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, offset);

        if (this.spawnTargetBoss >= bossList.length) {
            this.spawnTargetBoss = 0;
        }

        Entity boss = bossList[this.spawnTargetBoss].create(level);

        if (boss != null) {
            // ボスの初期位置
            double bx = offset.getX() + 0.5;
            double by = offset.getY();
            double bz = offset.getZ() + 0.5;

            // チェストの中央座標
            double cx = chestPos.getX() + 0.5;
            double cz = chestPos.getZ() + 0.5;

            // 箱の方向ベクトル → 角度（yaw）に変換
            double dx = cx - bx;
            double dz = cz - bz;
            float yaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));

            boss.moveTo(bx, by, bz, 0f, 0f);

            boss.setYRot(yaw);
            boss.setYBodyRot(boss.getYRot());
            boss.setYHeadRot(boss.getYRot());
            boss.yRotO = boss.getYRot();
//            boss.yBodyRotO = boss.getYRot();
//            boss.yHeadRotO = boss.getYRot();

            level.addFreshEntity(boss);

            spawnedMonsters.add(boss.getUUID());
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    private EntityType<? extends BaseSmallMonsterEntity> getRandomMobType(Random rand) {
        int max = generateTargetMonsterList.size();
        return generateTargetMonsterList.get(Math.abs(rand.nextInt()) % max);
    }

    public static final int MODE_BLUE = 0;
    public static final int MODE_YELLOW = 1;
    public static final int MODE_RED = 2;
    public static final int MODE_PURPLE = 3;

    private int mode = MODE_BLUE;

    private int spawnTargetBoss = 0;

    public int getMode() {
        return mode;
    }

    public void incrementMode() {
        mode++;
        if (mode > MODE_PURPLE) {
            mode = MODE_BLUE;
        }
        updateMode(mode);
    }

    public void updateMode(int mode) {
        this.mode = mode;

        this.setChanged();
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt(PomkotsMechs.nbtName("CubeMode"), mode);

        tag.putInt(PomkotsMechs.nbtName("SpawnTargetBoss"), spawnTargetBoss);

        ListTag list = new ListTag();
        for (UUID uuid : spawnedMonsters) {
            list.add(NbtUtils.createUUID(uuid));
        }
        tag.put(PomkotsMechs.nbtName("SpawnedMonsters"), list);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        this.mode = tag.getInt(PomkotsMechs.nbtName("CubeMode"));

        this.spawnTargetBoss = tag.getInt(PomkotsMechs.nbtName("SpawnTargetBoss"));

        spawnedMonsters.clear();
        ListTag list = (ListTag) tag.get(PomkotsMechs.nbtName("SpawnedMonsters"));
        for (Tag t : list) {
            spawnedMonsters.add(NbtUtils.loadUUID(t));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private boolean playLockedAnimation = false;

    public void playOpenFailAnimation() {
        if (this.level.isClientSide) {
            this.playLockedAnimation = true;
        }
    }

    private float prevOpeness = 0;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, event -> {
            float curOpenness = event.getAnimatable().getOpenNess(event.getPartialTick());
            float prevOpenness = this.prevOpeness;

            this.prevOpeness = curOpenness;

            if (playLockedAnimation) {
                this.playLockedAnimation = false;
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pomkotscube.openfail"));
            } else if (prevOpenness == 0 && curOpenness> 0) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.open"));
            } else if (prevOpenness == 1 && curOpenness < 1) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.close"));
            } else {
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object blockEntity) {
        return level != null ? level.getGameTime() : 0;
    }
}
