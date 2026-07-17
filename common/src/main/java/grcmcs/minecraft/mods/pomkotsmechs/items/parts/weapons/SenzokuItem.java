package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.SenzokuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineShotGunItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class SenzokuItem extends BasePartsItem.WeaponArm {
    public SenzokuItem(Properties properties) {
        super(properties);
    }

    @Override
    public SenzokuItemRenderer newRenderer() {
        return new SenzokuItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            int bulletNum = 10;

            for (int i = 0; i < bulletNum; i++) {
                if (mechInterface.consumeAmmo(1)) {
                    if (!world.isClientSide()) {
                        BulletMachineEntity be = new BulletMachineEntity(
                                PomkotsMechs.BULLET_MACHINE.get(), world,
                                mechInterface.getMechEntity(),
                                mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
                        );

                        var offset = mechInterface.getOffset();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        var muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0F, 5F);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                        be.setPos(offset.add(muzzlPos));

                        float[] angle = randomizeShootingAngle(mechInterface.getShootingAngle(be, true),30,mechInterface.getMechEntity().getRandom());

                        be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 7, 1);

                        world.addFreshEntity(be);
                    } else if (i == 0){

                        ParticleUtil.spawnAttachedMuzzleFlash(
                                (ClientLevel) world,
                                mechInterface.getMechEntity(),
                                mechInterface.getAttachPoint(),
                                16.0D,
                                new Vec3(1.8 * mechInterface.isRight(), 3.0F, 5F)
                        );
                        mechInterface.playSoundEffect(PomkotsMechs.SE_GUN_3.get());
                    }
                }
            }
        }
    }

    @Override
    public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
        return BulletMachineEntity.calcRangeCategoryStatic(distance);
    }

    private float[] randomizeShootingAngle(float[] baseShootingAngle, float spread, RandomSource random) {
        float newPitch = baseShootingAngle[0] + (random.nextFloat() - 0.5f) * spread;
        float newYaw = baseShootingAngle[1] + (random.nextFloat() - 0.5f) * spread;

        return new float[]{newPitch, newYaw};
    }

    @Override
    public boolean isMatchAmmo(Magazine mag) {
        return mag instanceof MagazineShotGunItem;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_HAND;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.SHOT_GUN;
    }

    @Override
    public Motion getMotion() {
        return Motion.RIFLE;
    }

    @Override
    public int getCoolTime() {
        return 40;
    }

    @Override
    public String getPartsSeriesName() {
        return "senzoku";
    }
}
