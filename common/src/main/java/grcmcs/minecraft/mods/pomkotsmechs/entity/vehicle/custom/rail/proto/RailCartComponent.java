package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.proto;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class RailCartComponent {
    private LivingEntity ent;

    private boolean onRails;
    private boolean canUseRail = true;

    public RailCartComponent(LivingEntity ent) {
        this.ent = ent;
    }

    public boolean canUseRail() {
        return this.canUseRail;
    }

    public void tick() {

        if (!ent.level().isClientSide) {


            int k = Mth.floor(ent.getX());
            int i = Mth.floor(ent.getY());
            int j = Mth.floor(ent.getZ());
            if (ent.level().getBlockState(new BlockPos(k, i - 1, j)).is(BlockTags.RAILS)) {
                --i;
            }

            BlockPos blockpos = new BlockPos(k, i, j);
            BlockState blockstate = ent.level().getBlockState(blockpos);

            this.onRails = BaseRailBlock.isRail(blockstate);
            if (this.canUseRail() && this.onRails) {
                this.moveAlongTrack(blockpos, blockstate);
            } else {
//                this.comeOffTrack();
            }
//
//            this.setXRot(0.0F);
//            double d1 = this.xo - this.getX();
//            double d3 = this.zo - this.getZ();
//            if (d1 * d1 + d3 * d3 > 0.001) {
//                this.setYRot((float)(Mth.atan2(d3, d1) * 180.0 / Math.PI));
//                if (this.flipped) {
//                    this.setYRot(this.getYRot() + 180.0F);
//                }
//            }
//
//            double d4 = (double)Mth.wrapDegrees(this.getYRot() - this.yRotO);
//            if (d4 < -170.0 || d4 >= 170.0) {
//                this.setYRot(this.getYRot() + 180.0F);
//                this.flipped = !this.flipped;
//            }
//
//            this.setRot(this.getYRot(), this.getXRot());
//            AABB box;
//            if (this.getCollisionHandler() != null) {
//                box = this.getCollisionHandler().getMinecartCollisionBox(this);
//            } else {
//                box = this.getBoundingBox().inflate(0.20000000298023224, 0.0, 0.20000000298023224);
//            }
//
//            if (this.canBeRidden() && this.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
//                List<Entity> list = this.level().getEntities(this, box, EntitySelector.pushableBy(this));
//                if (!list.isEmpty()) {
//                    for(int l = 0; l < list.size(); ++l) {
//                        Entity entity1 = (Entity)list.get(l);
//                        if (!(entity1 instanceof Player) && !(entity1 instanceof IronGolem) && !(entity1 instanceof AbstractMinecart) && !this.isVehicle() && !entity1.isPassenger()) {
//                            entity1.startRiding(this);
//                        } else {
//                            entity1.push(this);
//                        }
//                    }
//                }
//            } else {
//                Iterator var13 = this.level().getEntities(this, box).iterator();
//
//                while(var13.hasNext()) {
//                    Entity entity = (Entity)var13.next();
//                    if (!this.hasPassenger(entity) && entity.isPushable() && entity instanceof AbstractMinecart) {
//                        entity.push(this);
//                    }
//                }
//            }
//
//            this.updateInWaterStateAndDoFluidPushing();
//            if (this.isInLava()) {
//                this.lavaHurt();
//                this.fallDistance *= 0.5F;
//            }
        }
    }

    protected void comeOffTrack() {
        double d0 = ent.onGround() ? this.getMaxSpeed() : (double)this.getMaxSpeedAirLateral();
        Vec3 vec3 = ent.getDeltaMovement();
        ent.setDeltaMovement(Mth.clamp(vec3.x, -d0, d0), vec3.y, Mth.clamp(vec3.z, -d0, d0));
        if (ent.onGround()) {
            ent.setDeltaMovement(ent.getDeltaMovement().scale(0.5));
        }

        if (this.getMaxSpeedAirVertical() > 0.0F && ent.getDeltaMovement().y > (double)this.getMaxSpeedAirVertical()) {
            if (Math.abs(ent.getDeltaMovement().x) < 0.30000001192092896 && Math.abs(ent.getDeltaMovement().z) < 0.30000001192092896) {
                ent.setDeltaMovement(new Vec3(ent.getDeltaMovement().x, 0.15000000596046448, ent.getDeltaMovement().z));
            } else {
                ent.setDeltaMovement(new Vec3(ent.getDeltaMovement().x, (double)this.getMaxSpeedAirVertical(), ent.getDeltaMovement().z));
            }
        }

        ent.move(MoverType.SELF, ent.getDeltaMovement());
        if (!ent.onGround()) {
            ent.setDeltaMovement(ent.getDeltaMovement().scale(this.getDragAir()));
        }
    }

    public float getMaxSpeed() {
        return 4;
    }

    public float getMaxSpeedAirLateral() {
        return 4;
    }

    public float getMaxSpeedAirVertical() {
        return 4;
    }

    public double getDragAir() {
        return 0.949999988079071;
    }

    public double getMaxSpeedWithRail() {
        return 4;
    }

    protected void moveAlongTrack(BlockPos curRailPos, BlockState curRailState) {
        ent.resetFallDistance();
        double entPosX = ent.getX();
        double entPosY = ent.getY();
        double entPosZ = ent.getZ();
        Vec3 entityPosAdjustedByRailPos = this.getPos(entPosX, entPosY, entPosZ);
        entPosY = (double)curRailPos.getY();

        boolean isPoweredRail = false;
        boolean notPoweredRail = false;
        BaseRailBlock baserailblock = (BaseRailBlock)curRailState.getBlock();
        if (baserailblock instanceof PoweredRailBlock) {
            isPoweredRail = (Boolean)curRailState.getValue(PoweredRailBlock.POWERED);
            notPoweredRail = !isPoweredRail;
        }

        double slopeAdj = this.getSlopeAdjustment();
        if (ent.isInWater()) {
            slopeAdj *= 0.2;
        }

        Vec3 entVel = ent.getDeltaMovement();
        RailShape railshape = getRailDirection(curRailState);
        switch (railshape) {
            case ASCENDING_EAST:
                ent.setDeltaMovement(entVel.add(-slopeAdj, 0.0, 0.0));
                ++entPosY;
                break;
            case ASCENDING_WEST:
                ent.setDeltaMovement(entVel.add(slopeAdj, 0.0, 0.0));
                ++entPosY;
                break;
            case ASCENDING_NORTH:
                ent.setDeltaMovement(entVel.add(0.0, 0.0, slopeAdj));
                ++entPosY;
                break;
            case ASCENDING_SOUTH:
                ent.setDeltaMovement(entVel.add(0.0, 0.0, -slopeAdj));
                ++entPosY;
        }

        entVel = ent.getDeltaMovement();
        Pair<Vec3i, Vec3i> pair = exits(railshape);
        Vec3i railDir1 = (Vec3i)pair.getFirst();
        Vec3i railDir2 = (Vec3i)pair.getSecond();
        double d4 = (double)(railDir2.getX() - railDir1.getX());
        double d5 = (double)(railDir2.getZ() - railDir1.getZ());
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        double d7 = entVel.x * d4 + entVel.z * d5;
        if (d7 < 0.0) {
            d4 = -d4;
            d5 = -d5;
        }

        double d8 = Math.min(2.0, entVel.horizontalDistance());
        entVel = new Vec3(d8 * d4 / d6, entVel.y, d8 * d5 / d6);
        ent.setDeltaMovement(entVel);

        Entity entity = ent.getFirstPassenger();
        if (entity instanceof Player) {
            Vec3 vec32 = entity.getDeltaMovement();
            double d9 = vec32.horizontalDistanceSqr();
            double d11 = ent.getDeltaMovement().horizontalDistanceSqr();
            if (d9 > 1.0E-4 && d11 < 0.01) {
                ent.setDeltaMovement(ent.getDeltaMovement().add(vec32.x * 0.1, 0.0, vec32.z * 0.1));
                notPoweredRail = false;
            }
        }

        double entVelHor;
        if (notPoweredRail) {
            entVelHor = ent.getDeltaMovement().horizontalDistance();
            if (entVelHor < 0.03) {
                ent.setDeltaMovement(Vec3.ZERO);
            } else {
                ent.setDeltaMovement(ent.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }

        entVelHor = (double)curRailPos.getX() + 0.5 + (double)railDir1.getX() * 0.5;
        double d10 = (double)curRailPos.getZ() + 0.5 + (double)railDir1.getZ() * 0.5;
        double d12 = (double)curRailPos.getX() + 0.5 + (double)railDir2.getX() * 0.5;
        double d13 = (double)curRailPos.getZ() + 0.5 + (double)railDir2.getZ() * 0.5;
        d4 = d12 - entVelHor;
        d5 = d13 - d10;
        double d14;
        if (d4 == 0.0) {
            d14 = entPosZ - (double)curRailPos.getZ();
        } else if (d5 == 0.0) {
            d14 = entPosX - (double)curRailPos.getX();
        } else {
            double d15 = entPosX - entVelHor;
            double d16 = entPosZ - d10;
            d14 = (d15 * d4 + d16 * d5) * 2.0;
        }

        entPosX = entVelHor + d4 * d14;
        entPosZ = d10 + d5 * d14;
        ent.setPos(entPosX, entPosY, entPosZ);

        this.moveMinecartOnRail(curRailPos);

        if (railDir1.getY() != 0 && Mth.floor(ent.getX()) - curRailPos.getX() == railDir1.getX() && Mth.floor(ent.getZ()) - curRailPos.getZ() == railDir1.getZ()) {
            ent.setPos(ent.getX(), ent.getY() + (double)railDir1.getY(), ent.getZ());
        } else if (railDir2.getY() != 0 && Mth.floor(ent.getX()) - curRailPos.getX() == railDir2.getX() && Mth.floor(ent.getZ()) - curRailPos.getZ() == railDir2.getZ()) {
            ent.setPos(ent.getX(), ent.getY() + (double)railDir2.getY(), ent.getZ());
        }

        this.applyNaturalSlowdown();

        Vec3 vec33 = this.getPos(ent.getX(), ent.getY(), ent.getZ());
        Vec3 vec36;
        double d27;
        if (vec33 != null && entityPosAdjustedByRailPos != null) {
            double d17 = (entityPosAdjustedByRailPos.y - vec33.y) * 0.05;
            vec36 = ent.getDeltaMovement();
            d27 = vec36.horizontalDistance();
            if (d27 > 0.0) {
                ent.setDeltaMovement(vec36.multiply((d27 + d17) / d27, 1.0, (d27 + d17) / d27));
            }

            ent.setPos(ent.getX(), vec33.y, ent.getZ());
        }

        int j = Mth.floor(ent.getX());
        int i = Mth.floor(ent.getZ());
        if (j != curRailPos.getX() || i != curRailPos.getZ()) {
            vec36 = ent.getDeltaMovement();
            d27 = vec36.horizontalDistance();
            ent.setDeltaMovement(d27 * (double)(j - curRailPos.getX()), vec36.y, d27 * (double)(i - curRailPos.getZ()));
        }

        if (isPoweredRail) {
            vec36 = ent.getDeltaMovement();
            d27 = vec36.horizontalDistance();
            if (d27 > 0.01) {
                double d19 = 0.06;
                ent.setDeltaMovement(vec36.add(vec36.x / d27 * 0.06, 0.0, vec36.z / d27 * 0.06));
            } else {
                Vec3 vec37 = ent.getDeltaMovement();
                double d20 = vec37.x;
                double d21 = vec37.z;

                ent.setDeltaMovement(d20, vec37.y, d21);
            }
        }

    }

    public RailShape getRailDirection(BlockState state) {
        BaseRailBlock railBlock = (BaseRailBlock) state.getBlock();
        return state.getValue(railBlock.getShapeProperty());
    }

    public Vec3 getPos(double d, double e, double f) {
        int i = Mth.floor(d);
        int j = Mth.floor(e);
        int k = Mth.floor(f);
        if (ent.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
            --j;
        }

        BlockState blockstate = ent.level().getBlockState(new BlockPos(i, j, k));
        if (BaseRailBlock.isRail(blockstate)) {
            RailShape railshape = getRailDirection(blockstate);
            Pair<Vec3i, Vec3i> pair = exits(railshape);
            Vec3i vec3i = (Vec3i)pair.getFirst();
            Vec3i vec3i1 = (Vec3i)pair.getSecond();
            double d0 = (double)i + 0.5 + (double)vec3i.getX() * 0.5;
            double d1 = (double)j + 0.0625 + (double)vec3i.getY() * 0.5;
            double d2 = (double)k + 0.5 + (double)vec3i.getZ() * 0.5;
            double d3 = (double)i + 0.5 + (double)vec3i1.getX() * 0.5;
            double d4 = (double)j + 0.0625 + (double)vec3i1.getY() * 0.5;
            double d5 = (double)k + 0.5 + (double)vec3i1.getZ() * 0.5;
            double d6 = d3 - d0;
            double d7 = (d4 - d1) * 2.0;
            double d8 = d5 - d2;
            double d9;
            if (d6 == 0.0) {
                d9 = f - (double)k;
            } else if (d8 == 0.0) {
                d9 = d - (double)i;
            } else {
                double d10 = d - d0;
                double d11 = f - d2;
                d9 = (d10 * d6 + d11 * d8) * 2.0;
            }

            d = d0 + d6 * d9;
            e = d1 + d7 * d9;
            f = d2 + d8 * d9;
            if (d7 < 0.0) {
                ++e;
            } else if (d7 > 0.0) {
                e += 0.5;
            }

            return new Vec3(d, e, f);
        } else {
            return null;
        }
    }

    private double getSlopeAdjustment() {
        return 0.0078125D;
    }

    protected void applyNaturalSlowdown() {
        double d0 = ent.isVehicle() ? 0.997 : 0.96;
        Vec3 vec3 = ent.getDeltaMovement();
        vec3 = vec3.multiply(d0, 0.0, d0);
        if (ent.isInWater()) {
            vec3 = vec3.scale(0.949999988079071);
        }

        ent.setDeltaMovement(vec3);
    }

    public void moveMinecartOnRail(BlockPos pos) {
        double d24 = ent.isVehicle() ? 0.75 : 1.0;
        double d25 = this.getMaxSpeedWithRail();
        Vec3 vec3d1 = ent.getDeltaMovement();
        ent.move(MoverType.SELF, new Vec3(Mth.clamp(d24 * vec3d1.x, -d25, d25), 0.0, Mth.clamp(d24 * vec3d1.z, -d25, d25)));
    }

    private static Pair<Vec3i, Vec3i> exits(RailShape arg) {
        return EXITS.get(arg);
    }

    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS;

    static {
        EXITS = Util.make(Maps.newEnumMap(RailShape.class), (enumMap) -> {
            Vec3i vec3i = Direction.WEST.getNormal();
            Vec3i vec3i1 = Direction.EAST.getNormal();
            Vec3i vec3i2 = Direction.NORTH.getNormal();
            Vec3i vec3i3 = Direction.SOUTH.getNormal();
            Vec3i vec3i4 = vec3i.below();
            Vec3i vec3i5 = vec3i1.below();
            Vec3i vec3i6 = vec3i2.below();
            Vec3i vec3i7 = vec3i3.below();
            enumMap.put(RailShape.NORTH_SOUTH, Pair.of(vec3i2, vec3i3));
            enumMap.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i1));
            enumMap.put(RailShape.ASCENDING_EAST, Pair.of(vec3i4, vec3i1));
            enumMap.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i5));
            enumMap.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i2, vec3i7));
            enumMap.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i6, vec3i3));
            enumMap.put(RailShape.SOUTH_EAST, Pair.of(vec3i3, vec3i1));
            enumMap.put(RailShape.SOUTH_WEST, Pair.of(vec3i3, vec3i));
            enumMap.put(RailShape.NORTH_WEST, Pair.of(vec3i2, vec3i));
            enumMap.put(RailShape.NORTH_EAST, Pair.of(vec3i2, vec3i1));
        });
    }
}
