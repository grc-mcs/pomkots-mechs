package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public abstract class PomkotsThrowableProjectile extends ThrowableProjectile {
    protected LivingEntity shooter;
    private float soundEffectVolume = -1;

    public PomkotsThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, null, world);
    }

    public PomkotsThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, LivingEntity shooter, Level world) {
        super(entityType, world);
        this.shooter = shooter;
    }

    @Override
    public void tick() {
        this.soundEffectVolume = -1;
        super.tick();
    }

    public void onHitEntityPublic(Entity entity) {
        this.onHitEntity(new EntityHitResult(entity));
    }

    public float getHitDamage() {
        return 0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    public LivingEntity getShooter() {
        return this.shooter;
    }

    protected void playSoundEffect(SoundEvent event) {
        this.playSoundEffect(event, 1.0F);
    }

    protected void playSoundEffect(SoundEvent event, float volume) {
        this.playSoundEffect(event, volume, 64);
    }

    protected void playSoundEffect(SoundEvent event, float volume, double maxDistance) {
        if (this.soundEffectVolume < 0) {
            this.soundEffectVolume = computeVolume(maxDistance);
        }

        if (this.soundEffectVolume > 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
        }
    }

    private float computeVolume(double maxDistance) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0f;

        double distance = player.distanceTo(this);
        if (distance > maxDistance) return 0f;

        float volume = 1.0f - (float)(distance / maxDistance);

        return Mth.clamp(volume, 0f, 1f);
    }
}
