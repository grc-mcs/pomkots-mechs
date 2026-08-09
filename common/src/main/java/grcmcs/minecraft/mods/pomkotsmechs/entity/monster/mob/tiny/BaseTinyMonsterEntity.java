package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

public abstract class BaseTinyMonsterEntity extends BaseSmallMonsterEntity {
    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.ATTACK_DAMAGE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 100);
    }

    public BaseTinyMonsterEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected boolean usesSeparateDamageHitBox() {
        return false;
    }

    protected int attackingTime = 0;

    public boolean isAttacking() {
        return attackingTime > 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickAttack();

        if (this.isClientSide() && this.isDeadOrDying() && this.deathTime == 10) {
            generateDestroyedExplosionEffect();
        }
    }

    protected void choiMove() {
        this.addDeltaMovement(new Vec3(0,0,0.001).yRot((float) Math.toRadians((-1.0) * this.getYRot())));
    }

    protected void tickAttack() {
        if (this.attackingTime > 0) {
            attackingTime--;
        }
    }

    @Override
    public boolean tryAttack() {
        if (this.attackCooltime == 0 && !this.isAttacking()) {
            startAttack();
            this.attackCooltime = getMaxAttackCooltime();

            return true;
        } else {
            return false;
        }
    }

    protected void startAttack() {
        this.rotateToTarget(this.getTarget());
        attackingTime = getMaxAttackTicks();
    }

    abstract protected int getMaxAttackTicks();

    @Override
    public void doAttack() {

    }

    public static boolean canSpawn(EntityType<Pmss01Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }

    @Override
    protected void generateDestroyedExplosionEffect() {
        if (isClientSide()) {
            this.playSoundEffect(PomkotsMechs.SE_EXPLOSION_EVENT.get());
            this.generateLava();
        }
    }

    private void generateLava() {
        if (this.level().isClientSide) {
            RandomSource random = this.level().getRandom();

            // 100個のLavaパーティクルをバラまく
            for (int i = 0; i < 50; i++) {
                // ランダムな速度を生成
                double velocityX = random.nextDouble() * 3.0 - 1;
                double velocityY = random.nextDouble() * 3.0 - 1;
                double velocityZ = random.nextDouble() * 3.0 - 1;

                // パーティクルをクライアント側で発生させる
                this.level().addAlwaysVisibleParticle(ParticleTypes.LAVA, true,
                        this.getX(), this.getY(), this.getZ(), // 位置
                        velocityX, velocityY, velocityZ // 速度
                );
            }
        }
    }


    protected void playSounds(SoundKeyframeEvent event) {
        if ("se_step".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_EARTHRAISE_EVENT.get(), 0.4F, 32);

        } else if ("se_machine".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_MACHINE.get(), 1, 32);

        } else if ("se_impact".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GASHAN.get(), 1, 32);

        } else if ("se_shoot".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_RIFLE.get(), 1, 32);

        } else if ("se_boot".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_MACHINE.get(), 1, 32);
        }
    }
}
