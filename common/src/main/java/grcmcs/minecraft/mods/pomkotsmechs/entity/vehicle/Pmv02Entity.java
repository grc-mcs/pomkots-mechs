package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockMassEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockProjectileEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PresentBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

import java.util.LinkedList;
import java.util.List;

public class Pmv02Entity extends PomkotsVehicleBase {
    @Override
    protected String getMechName() {
        return "pmv02";
    }

    public Pmv02Entity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected static final int ACT_NEEDLE = 2;
    protected static final int ACT_DRILL = 3;
    protected static final int ACT_HUMMER = 4;
    protected static final int ACT_ROLLER = 5;
    protected static final int ACT_BLOCKINJECT = 6;
    protected static final int ACT_VACUME = 7;
    protected static final int ACT_PLACE = 8;
    protected static final int ACT_LIFT_BLOCK = 9;
    protected static final int ACT_THROW = 10;

    @Override
    protected void registerActions() {
        super.registerActions();
        this.actionController.registerAction(ACT_NEEDLE, new Action(0, 7, 2), ActionController.ActionType.R_ARM_MAIN);
        this.actionController.registerAction(ACT_DRILL, new Action(0, 20, 2), ActionController.ActionType.L_ARM_MAIN);
        this.actionController.registerAction(ACT_HUMMER, new Action(20, 40, 20), ActionController.ActionType.R_SHL_MAIN);
        this.actionController.registerAction(ACT_ROLLER, new Action(0, 20, 2), ActionController.ActionType.L_SHL_MAIN);
        this.actionController.registerAction(ACT_BLOCKINJECT, new Action(0, 5, 5), ActionController.ActionType.R_ARM_SUB);
        this.actionController.registerAction(ACT_VACUME, new Action(0, 5, 5), ActionController.ActionType.L_ARM_SUB);
        this.actionController.registerAction(ACT_LIFT_BLOCK, new Action(20, 8, 2), ActionController.ActionType.R_SHL_SUB);
        this.actionController.registerAction(ACT_THROW, new Action(20, 3, 7), ActionController.ActionType.R_SHL_SUB);
        this.actionController.registerAction(ACT_PLACE, new Action(0, 20, 2), ActionController.ActionType.L_SHL_SUB);

    }

    private int hummerChargeNum = 0;
    private boolean airialHummer = false;
    private BlockMassEntity heldBlockMassEntity = null; // 持ち上げているブロック塊
//    private LivingEntity heldBlockMassEntity = null; // 持ち上げているブロック塊
    private static final double BLOCK_OFFSET_Y = 6.0;   // 手の高さのオフセット
    private boolean isHoldingBlockMassEntity = false;

//    protected boolean useEnergy(int dec) {
//        return true;
//    }
    
    @Override
    public void tick() {
        super.tick();

        if (this.isServerSide()) {
            if (heldBlockMassEntity != null) {
                var offset = this.position();
                heldBlockMassEntity.setPos(offset.x, offset.y + BLOCK_OFFSET_Y, offset.z);
                heldBlockMassEntity.hasImpulse = true;
            }
        }
    }

    @Override
    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        if (this.isMainMode()) {
            if (!driverInput.isWeaponRightShoulderPressed() && this.actionController.getAction(ACT_HUMMER).isCharging()) {
                this.actionController.getAction(ACT_HUMMER).fireAction();
                airialHummer = false;
            }

            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.getAction(ACT_NEEDLE).startAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                this.actionController.getAction(ACT_DRILL).startAction();
            } else if (driverInput.isWeaponRightShoulderPressed()) {
                var hAction = this.actionController.getAction(ACT_HUMMER);
                hAction.startAction();

                if (hAction.isCharging()) {
                    hummerChargeNum = hAction.currentChargeTime;
                    if (justLanded(this)) {
                        airialHummer = true;
                        hAction.fireAction();
                    } else {
                        airialHummer = false;
                    }
                }
            } else if (driverInput.isWeaponLeftShoulderPressed()) {
                this.actionController.getAction(ACT_ROLLER).startAction();
            }
        } else {
            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.getAction(ACT_BLOCKINJECT).startAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                this.actionController.getAction(ACT_VACUME).startAction();
            } else if (driverInput.isWeaponRightShoulderPressed()) {
                if (!this.isHoldingBlockMassEntity) {
                    this.actionController.getAction(ACT_LIFT_BLOCK).startAction();
                } else {
                    this.actionController.getAction(ACT_THROW).startAction();
                    this.fireThrowBlock(this.level(), false);
                    isHoldingBlockMassEntity = false;
                    actionController.getAction(ACT_LIFT_BLOCK).currentCoolTime = 20;
                }
            } else if (driverInput.isWeaponLeftShoulderPressed()) {
                if (this.isHoldingBlockMassEntity) {
                    this.actionController.getAction(ACT_THROW).startAction();
                    this.fireThrowBlock(this.level(), true);
                    isHoldingBlockMassEntity = false;
                    actionController.getAction(ACT_LIFT_BLOCK).currentCoolTime = 20;
                } else {
                    this.actionController.getAction(ACT_PLACE).startAction();
                }
//                this.actionController.getAction(ACT_RAISETHROW).startAction();
            }
        }
    }

    @Override
    protected void fireWeapons() {
        Level level = level();

        if (actionController.getAction(ACT_NEEDLE).isOnFire()) {
            this.fireNeedle(level);
        } else if (actionController.getAction(ACT_DRILL).isOnFire()) {
            this.fireDrill(level);
        } else if (actionController.getAction(ACT_HUMMER).currentFireTime == 5) {
            this.fireHummer(level);
        } else if (actionController.getAction(ACT_ROLLER).isOnFire()) {
            this.fireRollerPlace(level, false);
        } else if (actionController.getAction(ACT_BLOCKINJECT).isOnFire()) {
            this.fireBlockInjection(level);
        } else if (actionController.getAction(ACT_VACUME).isOnFire()) {
            this.fireVacume(level);
        } else if (actionController.getAction(ACT_LIFT_BLOCK).isOnFire()) {
            this.fireLiftBlock(level);
        } else if (actionController.getAction(ACT_LIFT_BLOCK).isOnEnd) {
            isHoldingBlockMassEntity = true;
        } else if (actionController.getAction(ACT_PLACE).isOnFire()) {
            this.fireRollerPlace(level, true);
        }
    }

    public void fireBlockInjection(Level level) {
        if (this.isServerSide() && this.getDrivingPassenger() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();

            if (mainHand.getItem() instanceof BlockItem blockItem) {
                if (!isAvailableBlock(blockItem.getBlock().defaultBlockState())) {
                    return;
                }
                // メインハンドがブロックの場合
                Block block = blockItem.getBlock();

                // 射出するブロックエンティティを生成
                BlockProjectileEntity blockEntity = new BlockProjectileEntity(
                        PomkotsMechs.BLOCK_PROJECTILE.get(), // エンティティタイプ
                        player.level()
                );
                blockEntity.setBlock(block);

                // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
                // ので、3tick前の座標をオフセットにする
                // なんかaddVelocity周りが悪さしてる…？
                var offset = posHistory.getFirst();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(-1.65, 2.0F, 3.5F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
                blockEntity.setPos(offset.add(muzzlPos));

                float[] angle = getShootAngleNolock(player);

                blockEntity.shootFromRotation(blockEntity, angle[0], angle[1], this.getFallFlyingTicks(), 1.2F, 0F);

                player.level().addFreshEntity(blockEntity);

                if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
                    mainHand.shrink(1);
                }
            }
        }
    }

    public void fireVacume(Level level) {
        if (this.isServerSide() && this.getDrivingPassenger() instanceof Player player) {
            var hitResult = raycastBlock(player, true);
            if (hitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(hitResult.getBlockPos()).getBlock() instanceof LiquidBlock liquidBlock) {
                // 水または溶岩である場合
                if (liquidBlock == Blocks.WATER || liquidBlock == Blocks.LAVA) {
                    var offset = hitResult.getBlockPos();

                    for (int x = 0; x < 3; x++) {
                        for (int z = 0; z < 3; z++) {
                            var pos = new BlockPos(offset.getX() + x - 1, offset.getY(), offset.getZ() + z - 1);
                            var bs = level.getBlockState(pos);
                            var b = bs.getBlock();

                            if (b == Blocks.WATER || b == Blocks.LAVA) {
                                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }

            // ほんとはバケツに汲んだりバケツから吐き出したりしたいけど、色んなバケツがあったりしてめんどくさそうだから今はちょっと置いとく

//            ItemStack mainHandItem = player.getMainHandItem();
//            if (mainHandItem.is(Items.BUCKET) || mainHandItem.is(Items.WATER_BUCKET) || mainHandItem.is(Items.LAVA_BUCKET)) {
//                if (hitResult.getType() != HitResult.Type.BLOCK) {
//                    return;
//                }
//
//                BlockPos hitPos = hitResult.getBlockPos();
//
//                if (level instanceof ServerLevel serverLevel) {
//                    if (mainHandItem.is(Items.BUCKET)) {
//                        tryFillBucket(player, serverLevel, hitPos);
//                    } else if (mainHandItem.is(Items.WATER_BUCKET) || mainHandItem.is(Items.LAVA_BUCKET)) {
//                        tryPlaceFluid(player, serverLevel, hitPos);
//                    }
//                }
//            }
        }
    }

//    private void tryFillBucket(Player player, ServerLevel level, BlockPos pos) {
//        if (level.getBlockState(pos).getBlock() instanceof LiquidBlock liquidBlock) {
//            // 水または溶岩である場合
//            if (liquidBlock == Blocks.WATER || liquidBlock == Blocks.LAVA) {
//                ItemStack filledBucket = liquidBlock == Blocks.WATER ? new ItemStack(Items.WATER_BUCKET) : new ItemStack(Items.LAVA_BUCKET);
//
//                if (player.getInventory().add(filledBucket)) {
//                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
//                }
//            }
//        }
//    }
//
//    private void tryPlaceFluid(Player player, ServerLevel level, BlockPos pos) {
//        BlockPos placePos = pos.relative(player.getDirection());
//
//        if (!level.getBlockState(placePos).isAir()) {
//            return;
//        }
//
//        ItemStack mainHandItem = player.getMainHandItem();
//
//        if (mainHandItem.is(Items.WATER_BUCKET)) {
//            // 水を配置
//            level.setBlock(placePos, Blocks.WATER.defaultBlockState(), 3);
//        } else if (mainHandItem.is(Items.LAVA_BUCKET)) {
//            // 溶岩を配置
//            level.setBlock(placePos, Blocks.LAVA.defaultBlockState(), 3);
//        } else {
//            return;
//        }
//
//        var stacks = getItemStacks(mainHandItem, player, 1);
//        if (stacks != null && !stacks.isEmpty()) {
//            stacks.get(0);
//        }
//        // バケツを空にする
//        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
//    }

    private void fireLiftBlock(Level level) {
        if (this.isServerSide() && heldBlockMassEntity == null) {
            var driver = this.getDrivingPassenger();

            if (driver instanceof Player player) {

                //クリスマス
//                        PresentBoxEntity blockMass = new PresentBoxEntity(PomkotsMechs.PRESENT_BOX.get(), this.level());
//                        blockMass.setPos(this.getX(), this.getY() + BLOCK_OFFSET_Y, this.getZ());
//                        blockMass.setNoGravity(true);
//                        this.level().addFreshEntity(blockMass);
//                        this.heldBlockMassEntity = blockMass;
                BlockState blockState = getMainHandBlock(player);

                if (blockState != null && isAvailableBlock(blockState)) {
                    var stacks = this.getItemStacks(player.getMainHandItem(), player, 64);

                    if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null) {
                        BlockMassEntity blockMass = new BlockMassEntity(PomkotsMechs.BLOCK_MASS.get(), this.level());
                        blockMass.initializeBlocksServer(blockState);
                        blockMass.setPos(this.getX(), this.getY() + BLOCK_OFFSET_Y, this.getZ());
                        blockMass.setNoGravity(true);
                        this.level().addFreshEntity(blockMass);
                        this.heldBlockMassEntity = blockMass;

                        if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
                            this.consumeItemStackFromTop(stacks, 64);
                        }
                    }
                }
            }
        }
    }

    // メインハンドのアイテムからブロック情報を取得
    private BlockState getMainHandBlock(LivingEntity player) {
        if (player != null && player.getMainHandItem() != null && player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return null;
    }

    private List<ItemStack> getItemStacks(ItemStack mainHandStack, Player player, int totalAmountToConsume) {
        List<ItemStack> stacks = new LinkedList<>();

        int remainingAmount = totalAmountToConsume;
        Item mainHandItem = mainHandStack.getItem();

        // プレイヤーのインベントリ内を走査
        for (ItemStack stack : player.getInventory().items) {
            if (stack != mainHandStack && stack.getItem() == mainHandItem) {
                stacks.add(stack);

                int stackCount = stack.getCount();

                if (stackCount <= remainingAmount) {
                    remainingAmount -= stackCount;
                } else {
                    return stacks;
                }
            }
        }

        // メインハンドのアイテムを消費
        if (remainingAmount > 0) {
            stacks.add(mainHandStack);

            int mainHandCount = mainHandStack.getCount();

            if (mainHandCount <= remainingAmount) {
                remainingAmount -= mainHandCount;
            } else {
                remainingAmount = 0;
            }
        }

        if (remainingAmount > 0) {
            return null;
        } else {
            return stacks;
        }
    }

    private void consumeItemStackFromTop(List<ItemStack> stacks, int num) {
        for (var stack: stacks) {
            if (stack.getCount() > num) {
                stack.setCount(stack.getCount() - num);
                return;
            } else {
                num -= stack.getCount();
                stack.setCount(0);
            }
        }
    }

    private void fireThrowBlock(Level level, boolean vertical) {
        if (heldBlockMassEntity != null && this.isServerSide()) {
            // ブロック塊に速度を与えて投げる
            if (vertical) {
                heldBlockMassEntity.setDeltaMovement(new Vec3(0, -1, 0));
            } else {
                heldBlockMassEntity.setDeltaMovement(this.getLookAngle().scale(1.5));
            }

            heldBlockMassEntity.setNoGravity(false);
            this.heldBlockMassEntity = null;
        }
    }

    private float[] getShootAngleNolock(LivingEntity driver) {
        var lookAngle = driver.getLookAngle();

        var xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 6.5;
        var yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 95.0;

        return new float[]{(float)xRot, (float)yRot};
    }

    public void fireNeedle(Level level) {
        var player = this.getDrivingPassenger();
        if (isServerSide() && player != null) {
            var hitResult = raycastBlock(player, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // ヒットしたブロックの位置を取得
                var blockPos = hitResult.getBlockPos();

                // ブロックを破壊
                level.destroyBlock(blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
            }
        }
    }

    private BlockHitResult raycastBlock(LivingEntity player, boolean targetFluid) {
        // 視線方向を取得
        Vec3 eyePosition = player.getEyePosition(1.0F);
        eyePosition = new Vec3(eyePosition.x, eyePosition.y + 1, eyePosition.z);
        Vec3 lookDirection = player.getLookAngle();
        Vec3 rayEnd = eyePosition.add(lookDirection.scale(40));

        // レイキャスト
        BlockHitResult hitResult = this.level().clip(new net.minecraft.world.level.ClipContext(
                eyePosition,
                rayEnd,
                ClipContext.Block.OUTLINE,
                targetFluid ? ClipContext.Fluid.SOURCE_ONLY: ClipContext.Fluid.NONE,
                player
        ));

        return hitResult;
    }

    public void fireDrill(Level level) {
        if (isServerSide() && tickCount % 10 == 0 && this.getDrivingPassenger() != null) {
            breakBlockSphere(5, getOffsetByLookingDirection(this.getDrivingPassenger(), 5));
        }
    }

    public boolean isDrilling() {
        return actionController.getAction(ACT_DRILL).isInAction();
    }

    public Vec3 getOffsetByLookingDirection(LivingEntity entity, int distance) {
        Vec3 lookDirection = entity.getLookAngle();
        Vec3 playerPosition = new Vec3(0, 4, 0);

        return playerPosition.add(
                0,
                lookDirection.y * distance,
                (1 - Math.abs(lookDirection.y)) * distance
        );
    }

    public void fireRollerPlace(Level level, boolean place) {
        if (isServerSide() && tickCount % 4 == 0) {
            double forwardX = -Math.sin(Math.toRadians(this.getYRot()));
            double forwardZ = Math.cos(Math.toRadians(this.getYRot()));

            // 前方5マスのオフセットを計算
            BlockPos origin = this.blockPosition().offset(
                    (int) (forwardX * 5),
                    0,
                    (int) (forwardZ * 5)
            );

            // 範囲を指定してブロックを破壊
            int rangeX = 7; // x方向の範囲
            int rangeY = 15; // y方向の範囲
            int rangeZ = 7; // z方向の範囲

            if (!place) {
                // 破壊モード
                for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                    for (int y = 0; y <= rangeY; y++) {
                        for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                            BlockPos targetPos = origin.offset(x, y, z);
                            if (!level.isEmptyBlock(targetPos)) {
                                level.destroyBlock(targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                            }
                        }
                    }
                }
            } else {
                // 張り替えモード
                var driver = this.getDrivingPassenger();
                if (driver instanceof Player player) {
                    BlockState blockState = getMainHandBlock(player);

                    if (blockState != null && isAvailableBlock(blockState)) {
                        var blockToPlace = blockState.getBlock();
                        var stacks = this.getItemStacks(player.getMainHandItem(), player, rangeX * rangeZ);

                        if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null) {
                            int consumedBlocks = 0;
                            for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                                for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                                    BlockPos targetPos = origin.offset(x, -1, z);
                                    BlockPos a = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                    var bs = this.level().getBlockState(a);

                                    if (!blockState.equals(bs)) {
                                        this.level().setBlock(a, blockToPlace.defaultBlockState(), 3);
                                        consumedBlocks++;
                                    }
                                }
                            }
                            if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
                                this.consumeItemStackFromTop(stacks, consumedBlocks);
                            }
                        }
                    }
                }
            }
        }
    }

    private void fireHummer(Level world) {
        if (!world.isClientSide()) {
            int rad = 0;

            if (hummerChargeNum < 10) {
                rad = 5;
            } else if (hummerChargeNum < 39) {
                rad = 10;
            } else {
                rad = 15;
            }

            if (!airialHummer) {
                breakBlocksCube(rad);
            } else {
                breakBlockSphere(rad, new Vec3(0,0,5));
            }
        }
    }

    private void breakBlocksCube(int radius) {
        BlockPos curBP = this.blockPosition();
        Vec3 vec = new Vec3(0, 0, 5);
        vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        BlockPos pos = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

        // 円柱の高さを設定
        int height = 8; // 高さ2ブロックの円柱
        for (int y = 0; y < height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = pos.offset(x, y, z);
                    BlockState state = this.level().getBlockState(blockPos);
                    if (!state.isAir()) {
                        if (x * x + z * z <= radius * radius) {
                            this.level().destroyBlock(blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                        }
                    }
                }
            }
        }
    }

    private void breakBlockSphere(int radius, Vec3 offset) {
        // 半径の2乗を計算（球体判定に利用）
        int radiusSquared = radius * radius;

        BlockPos curBP = this.blockPosition();
        Vec3 vec = offset;
        vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        BlockPos pos = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

        // 範囲を指定してループ処理
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radiusSquared) {
                        BlockPos targetPos = pos.offset(x, y, z);
                        BlockState state = this.level().getBlockState(targetPos);
                        // ブロックが空でない場合に破壊
                        if (!state.isAir()) {
                            this.level().destroyBlock(targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                        }
                    }
                }
            }
        }
    }

    private boolean isAvailableBlock(BlockState bs) {
        return !(bs.getBlock() instanceof ShulkerBoxBlock);
    }

    @Override
    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
        if (this.actionController.getAction(ACT_LIFT_BLOCK).isInAction()) {
            if (this.actionController.getAction(ACT_LIFT_BLOCK).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".raise"));
        } else if (this.actionController.getAction(ACT_THROW).isInAction()) {
            if (this.actionController.getAction(ACT_THROW).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".throw"));

        }

        return null;
    }

    @Override
    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "continuous", 0, event -> {
            if (this.isHoldingBlockMassEntity) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".keep"));
            }

            if (this.actionController.getAction(ACT_NEEDLE).isInAction()) {
                if (this.actionController.getAction(ACT_NEEDLE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_NEEDLE).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".gatringgun1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".gatringgun2"));
                }
            } else if (this.actionController.getAction(ACT_DRILL).isInAction()) {
                if (this.actionController.getAction(ACT_DRILL).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_DRILL).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".drill1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".drill2"));
                }
            } else if (this.actionController.getAction(ACT_HUMMER).isInAction()) {
                if (this.actionController.getAction(ACT_HUMMER).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_HUMMER).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".hummer1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".hummer2"));
                }
            } else if (this.actionController.getAction(ACT_ROLLER).isInAction()) {
                if (this.actionController.getAction(ACT_ROLLER).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_ROLLER).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".roller1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".roller2"));
                }
            } else if (this.actionController.getAction(ACT_PLACE).isInAction()) {
                if (this.actionController.getAction(ACT_PLACE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_PLACE).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".roller1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".roller2"));
                }
            }  else if (this.actionController.getAction(ACT_BLOCKINJECT).isInAction()) {
                if (this.actionController.getAction(ACT_BLOCKINJECT).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_BLOCKINJECT).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".blockinjection"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".blockinjection_keep"));
                }
            } else if (this.actionController.getAction(ACT_VACUME).isInAction()) {
                if (this.actionController.getAction(ACT_VACUME).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_VACUME).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".vacum"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".vacum_keep"));
                }

            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));
    }

    @Override
    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        if ("se_jump".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_booster".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get());
        } else if ("se_onground".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_drill1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_DRILL1.get());
        } else if ("se_drill2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_DRILL2.get());
        } else if ("se_hummer1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_HUMMER1.get());
        } else if ("se_hummer2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_HUMMER2.get());
        } else if ("se_lift".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_LIFT.get());
        } else if ("se_needle".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_NEEDLE.get());
        } else if ("se_roller1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER1.get());
        } else if ("se_roller2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER2.get());
        } else if ("se_step".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_STEP.get());
        } else if ("se_throw".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_THROW.get());
        } else if ("se_water".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_WATER.get());
        }
    }

    @Override
    public boolean shouldRenderDefaultHud(String hudName) {
        return "renderHotbar".equals(hudName);
    }
}
