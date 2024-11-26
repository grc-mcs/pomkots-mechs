package grcmcs.minecraft.mods.pomkotsmechs.entity;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class Utils {

//    private static ProtoBotEntity.Direction getDirectionFromVelocity(Entity ent) {
//        ProtoBotEntity.Direction dir = ProtoBotEntity.Direction.F;
//        Vec3 localVelocity = ent.getDeltaMovement().yRot((float) Math.toRadians((1.0) * ent.getYRot()));
//        if (Math.abs(localVelocity.z) >= Math.abs(localVelocity.x)) {
//            dir = (localVelocity.z >= 0)? ProtoBotEntity.Direction.F : ProtoBotEntity.Direction.B;
//        } else {
//            dir = (localVelocity.x <= 0)? ProtoBotEntity.Direction.R : ProtoBotEntity.Direction.L;
//        }
//        return dir;
//    }

    private static Vec3 createEvasionVelocityFromLocalVelocity(Entity ent) {
        Vec3 localVelocity = ent.getDeltaMovement().yRot((float) Math.toRadians((1.0) * ent.getYRot()));
        var forward = localVelocity.z;
        var sideways = localVelocity.x;

        Vec3 vel;
        if (forward == 0 && sideways == 0) {
            vel = new Vec3(0, 0, 1);
        } else if (Math.abs(sideways) >= 0.1) {
            vel = new Vec3(sideways, 0, 0).normalize();
        } else  {
            vel = new Vec3(sideways, 0, forward).normalize();
        }

        return vel;
    }

    // TODO くそかっこ悪いのでなんとかしたい…。半径指定したら計算して出してくれるようにする
    public static final ArrayList<Vec3i> circlePosRad9 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, 0));}
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, 1));}
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, -1));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 1, 0, 2));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 1, 0, -2));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 2, 0, 3));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 2, 0, -3));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 3, 0, 4));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 3, 0, -4));}
    }

    public static final ArrayList<Vec3i> circlePosRad7 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, -1));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, 0));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, 1));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 2, 0, 2));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 2, 0, -2));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 3, 0, 3));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 3, 0, -3));}
    }

    public static final ArrayList<Vec3i> circlePosRad5 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, 1));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, 0));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, -1));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 3, 0, 2));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 3, 0, -2));}
    }
}
