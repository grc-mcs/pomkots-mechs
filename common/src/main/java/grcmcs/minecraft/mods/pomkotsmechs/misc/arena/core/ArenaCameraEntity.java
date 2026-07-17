package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ArenaCameraEntity extends LivingEntity {
    private static final int MAX_LIFE_TICKS = 300;

    private int targetEntityId = -1;
    private int lifeTicks = MAX_LIFE_TICKS;

    private Vec3 startPos = Vec3.ZERO;

    private Vec3 orbitStartPos = null;
    private float startYaw;
    private float startPitch;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public ArenaCameraEntity(
            EntityType<? extends LivingEntity> type,
            Level level
    ) {
        super(type, level);

        setNoGravity(true);
        setInvulnerable(true);
    }



    public void initialize(
            ServerPlayer player,
            Entity target
    ) {
        this.targetEntityId = target.getId();
        this.startPos =
                player.getEyePosition();

        this.startYaw =
                player.getYRot();

        this.startPitch =
                player.getXRot();

        moveTo(
                startPos.x,
                startPos.y,
                startPos.z,
                startYaw,
                startPitch
        );

//        lookTarget =
//                target.position().add(
//                        0,
//                        target.getBbHeight() * 0.6,
//                        0
//                );
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }


    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public void tick() {
        setInvisible(false);
        setNoGravity(true);

        super.tick();

        if (level().isClientSide()) {
            return;
        }

        Entity target =
                level().getEntity(
                        targetEntityId
                );

        if (target == null) {
            discard();
            return;
        }

        tickOrbit(target);

        lifeTicks--;

        this.hasImpulse = true;

        if (lifeTicks <= 0) {
            discard();
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }
    private void tickOrbit(
            Entity target
    ) {
        int elapsed = MAX_LIFE_TICKS - lifeTicks;

        Vec3 targetEye =
                target.getBoundingBox().getCenter();

        Vec3 forward =
                target.getLookAngle()
                        .normalize();

        Vec3 cameraPos;

        // =====================================================
        // Phase1
        // プレイヤー視点 → 正面へ急接近
        // =====================================================

        if (elapsed < 20) {

            float t =
                    elapsed / 20F;

            Vec3 nearPos =
                    targetEye
                            .add(
                                    forward.scale(2.5)
                            )
                            .add(
                                    0,
                                    0.5,
                                    0
                            );

            cameraPos =
                    startPos.lerp(
                            nearPos,
                            easeOutCubic(t)
                    );
        }

        // =====================================================
        // Phase2
        // 正面方向へ少し引く
        // =====================================================

        else if (elapsed < 40) {

            float t =
                    (elapsed - 20) / 20F;

            Vec3 from =
                    targetEye
                            .add(
                                    forward.scale(2.5)
                            )
                            .add(
                                    0,
                                    0.5,
                                    0
                            );

            Vec3 to =
                    targetEye
                            .add(
                                    forward.scale(6.0)
                            )
                            .add(
                                    0,
                                    2.0,
                                    0
                            );

            cameraPos =
                    from.lerp(
                            to,
                            easeInOut(t)
                    );

            orbitStartPos = cameraPos;
        }

        // =====================================================
        // Phase3
        // 正面位置から左旋回
        // =====================================================

        else {

            if (orbitStartPos == null) {

                orbitStartPos =
                        targetEye
                                .add(
                                        forward.scale(6.0)
                                )
                                .add(
                                        0,
                                        2.0,
                                        0
                                );
            }

            float t =
                    (elapsed - 40) / 60F;

            double dx =
                    orbitStartPos.x
                            - target.getX();

            double dz =
                    orbitStartPos.z
                            - target.getZ();

            double radius =
                    Math.sqrt(
                            dx * dx
                                    + dz * dz
                    );

            double startAngle =
                    Math.atan2(
                            dz,
                            dx
                    );

            double angle =
                    startAngle
                            + Math.toRadians(
                            t * 120.0
                    );

            double x =
                    target.getX()
                            + Math.cos(angle)
                            * radius;

            double z =
                    target.getZ()
                            + Math.sin(angle)
                            * radius;

            double y =
                    orbitStartPos.y;

            cameraPos =
                    new Vec3(
                            x,
                            y,
                            z
                    );
        }

        moveTo(
                cameraPos.x,
                cameraPos.y,
                cameraPos.z
        );

        lookAt(
                EntityAnchorArgument.Anchor.EYES,
                targetEye
        );
    }

    private void tickOrbit3(
            Entity target
    ) {
        int elapsed = MAX_LIFE_TICKS - lifeTicks;

        Vec3 targetEye = target.getBoundingBox().getCenter();

        Vec3 cameraPos;

        // =====================================================
        // Phase1
        // プレイヤー視点 → 急接近
        // =====================================================

        if (elapsed < 20) {

            float t =
                    elapsed / 20F;

            Vec3 nearPos =
                    targetEye.add(
                            -2.5,
                            0.5,
                            0
                    );

            cameraPos =
                    startPos.lerp(
                            nearPos,
                            easeOutCubic(t)
                    );
        }

        // =====================================================
        // Phase2
        // 少し引く
        // =====================================================

        else if (elapsed < 40) {

            float t =
                    (elapsed - 20) / 20F;

            Vec3 from =
                    targetEye.add(
                            -2.5,
                            0.5,
                            0
                    );

            Vec3 to =
                    targetEye.add(
                            -6.0,
                            2.0,
                            0
                    );

            cameraPos =
                    from.lerp(
                            to,
                            easeInOut(t)
                    );

            orbitStartPos = cameraPos;
        }

        // =====================================================
        // Phase3
        // 引いた位置から左旋回
        // =====================================================

        else {

            if (orbitStartPos == null) {

                orbitStartPos =
                        targetEye.add(
                                -6.0,
                                2.0,
                                0
                        );
            }

            float t =
                    (elapsed - 40) / 60F;

            double dx =
                    orbitStartPos.x
                            - target.getX();

            double dz =
                    orbitStartPos.z
                            - target.getZ();

            double radius =
                    Math.sqrt(
                            dx * dx
                                    + dz * dz
                    );

            double startAngle =
                    Math.atan2(
                            dz,
                            dx
                    );

            double angle =
                    startAngle
                            + Math.toRadians(
                            t * 120.0
                    );

            double x =
                    target.getX()
                            + Math.cos(angle)
                            * radius;

            double z =
                    target.getZ()
                            + Math.sin(angle)
                            * radius;

            double y =
                    orbitStartPos.y;

            cameraPos =
                    new Vec3(
                            x,
                            y,
                            z
                    );
        }

        moveTo(
                cameraPos.x,
                cameraPos.y,
                cameraPos.z
        );

        lookAt(
                EntityAnchorArgument.Anchor.EYES,
                targetEye
        );
    }


    private Vec3 orbitCenter;
    private Vec3 lookTarget;
    private double orbitRadius;
    private double orbitStartAngle;

    private void tickOrbit2(
            Entity target
    ) {

        int elapsed =
                MAX_LIFE_TICKS
                        - lifeTicks;

        Vec3 cameraPos;

        // ==========================================
        // 固定注視点
        // ==========================================

        if (lookTarget == null) {

            lookTarget =
                    target.position().add(
                            0,
                            target.getBbHeight() * 0.6,
                            0
                    );
        }

        // ==========================================
        // Phase1
        // ==========================================

        if (elapsed < 20) {

            float t =
                    elapsed / 20F;

            Vec3 nearPos =
                    lookTarget.add(
                            -2.5,
                            0.5,
                            0
                    );

            cameraPos =
                    startPos.lerp(
                            nearPos,
                            easeOutCubic(t)
                    );
        }

        // ==========================================
        // Phase2
        // ==========================================

        else if (elapsed < 40) {

            float t =
                    (elapsed - 20)
                            / 20F;

            Vec3 from =
                    lookTarget.add(
                            -2.5,
                            0.5,
                            0
                    );

            Vec3 to =
                    lookTarget.add(
                            -6.0,
                            2.0,
                            0
                    );

            cameraPos =
                    from.lerp(
                            to,
                            easeInOut(t)
                    );

            orbitStartPos =
                    cameraPos;

            orbitCenter =
                    lookTarget;

            orbitRadius =
                    Math.sqrt(
                            Math.pow(
                                    orbitStartPos.x
                                            - orbitCenter.x,
                                    2
                            )
                                    +
                                    Math.pow(
                                            orbitStartPos.z
                                                    - orbitCenter.z,
                                            2
                                    )
                    );

            orbitStartAngle =
                    Math.atan2(
                            orbitStartPos.z
                                    - orbitCenter.z,
                            orbitStartPos.x
                                    - orbitCenter.x
                    );
        }

        // ==========================================
        // Phase3
        // ==========================================

        else {

            if (orbitStartPos == null) {

                orbitStartPos =
                        lookTarget.add(
                                -6.0,
                                2.0,
                                0
                        );

                orbitCenter =
                        lookTarget;

                orbitRadius =
                        Math.sqrt(
                                Math.pow(
                                        orbitStartPos.x
                                                - orbitCenter.x,
                                        2
                                )
                                        +
                                        Math.pow(
                                                orbitStartPos.z
                                                        - orbitCenter.z,
                                                2
                                        )
                        );

                orbitStartAngle =
                        Math.atan2(
                                orbitStartPos.z
                                        - orbitCenter.z,
                                orbitStartPos.x
                                        - orbitCenter.x
                        );
            }

            float t =
                    (elapsed - 40)
                            / 60F;

            double angle =
                    orbitStartAngle
                            + Math.toRadians(
                            t * 120.0
                    );

            double x =
                    orbitCenter.x
                            + Math.cos(angle)
                            * orbitRadius;

            double z =
                    orbitCenter.z
                            + Math.sin(angle)
                            * orbitRadius;

            double y =
                    orbitStartPos.y;

            cameraPos =
                    new Vec3(
                            x,
                            y,
                            z
                    );
        }

        moveTo(
                cameraPos.x,
                cameraPos.y,
                cameraPos.z
        );

        lookAt(
                EntityAnchorArgument.Anchor.EYES,
                lookTarget
        );
    }


    private static float easeOutCubic(
            float t
    ) {
        return 1F
                - (float) Math.pow(
                1F - t,
                3
        );
    }

    private static float easeInOut(
            float t
    ) {
        return t < 0.5F
                ? 2F * t * t
                : 1F
                - (float) Math.pow(
                -2F * t + 2F,
                2
        ) / 2F;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean save(
            CompoundTag tag
    ) {
        return false;
    }
}