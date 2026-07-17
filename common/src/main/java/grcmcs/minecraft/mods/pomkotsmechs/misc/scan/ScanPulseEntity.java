package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ScanPulseEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.OPTIONAL_UUID
            );

    private static final EntityDataAccessor<String> OWNER_TEAM =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.STRING
            );

    private static final EntityDataAccessor<Byte> VISIBILITY =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.BYTE
            );

    private static final EntityDataAccessor<Float> MAX_RADIUS =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Integer> EXPANSION_TICKS =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer> HIGHLIGHT_TICKS =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Long> START_GAME_TIME =
            SynchedEntityData.defineId(
                    ScanPulseEntity.class,
                    EntityDataSerializers.LONG
            );

    public ScanPulseEntity(
            EntityType<? extends ScanPulseEntity> type,
            Level level
    ) {
        super(type, level);

        this.noCulling = true;
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER_UUID, Optional.empty());
        entityData.define(OWNER_TEAM, "");
        entityData.define(VISIBILITY, ScanVisibility.OWNER.id());

        entityData.define(MAX_RADIUS, 64.0F);
        entityData.define(EXPANSION_TICKS, 60);
        entityData.define(HIGHLIGHT_TICKS, 100);

        entityData.define(START_GAME_TIME, 0L);
    }

    public void initialize(
            Player owner,
            ScanVisibility visibility,
            float maxRadius,
            int expansionTicks,
            int highlightTicks
    ) {
        setOwnerUUID(owner.getUUID());
        setVisibility(visibility);
        setMaxRadius(maxRadius);
        setExpansionTicks(expansionTicks);
        setHighlightTicks(highlightTicks);

        /*
         * チームは発動時点のものを保存。
         * スキャン中のチーム変更には追従しない仕様。
         */
        setOwnerTeam(
                owner.getTeam() != null
                        ? owner.getTeam().getName()
                        : ""
        );

        setStartGameTime(level().getGameTime());
    }

    @Override
    public void tick() {
        if (firstTick && level().isClientSide) {
            playSoundEffect(PomkotsMechs.SE_SCAN2.get(), 0.7F);
        }

        super.tick();

        if (!level().isClientSide) {
            long elapsed =
                    level().getGameTime() - getStartGameTime();

            /*
             * ドームが最大まで広がればEntity自体は役目を終える。
             * ハイライト時間はクライアントマネージャー側で継続する。
             */
            if (elapsed > getExpansionTicks() + 2L) {
                discard();
            }
        }
    }

        protected void playSoundEffect(SoundEvent event) {
        this.playSoundEffect(event, 1.0F);
    }

    protected void playSoundEffect(SoundEvent event, float volume) {
        float soundEffectVolume = computeVolume(100);

        if (soundEffectVolume > 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * soundEffectVolume, 1.0F, false);
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

    public float getRadius(float partialTick) {
        double elapsed =
                level().getGameTime()
                        - getStartGameTime()
                        + partialTick;

        float progress = (float) Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        elapsed / getExpansionTicks()
                )
        );

        /*
         * 少し滑らかな拡張。
         * 等速がよければ return maxRadius * progress でOK。
         */
        float eased =
                1.0F - (1.0F - progress) * (1.0F - progress);

        return getMaxRadius() * eased;
    }

    public float getPreviousRadius() {
        double elapsed =
                level().getGameTime()
                        - getStartGameTime()
                        - 1.0D;

        float progress = (float) Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        elapsed / getExpansionTicks()
                )
        );

        float eased =
                1.0F - (1.0F - progress) * (1.0F - progress);

        return getMaxRadius() * eased;
    }

    /**
     * このプレイヤーのクライアントにスキャンを見せるか。
     */
    public boolean isVisibleTo(Player player) {
        return switch (getVisibility()) {
            case ALL -> true;

            case OWNER ->
                    getOwnerUUID() != null
                            && getOwnerUUID().equals(player.getUUID());

            case TEAM -> isOwnerOrSameTeam(player);
        };
    }

    private boolean isOwnerOrSameTeam(Player player) {
        UUID ownerUUID = getOwnerUUID();

        if (ownerUUID != null
                && ownerUUID.equals(player.getUUID())) {
            return true;
        }

        String ownerTeam = getOwnerTeam();

        return !ownerTeam.isEmpty()
                && player.getTeam() != null
                && ownerTeam.equals(player.getTeam().getName());
    }

    @Nullable
    public UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        entityData.set(
                OWNER_UUID,
                Optional.ofNullable(uuid)
        );
    }

    public String getOwnerTeam() {
        return entityData.get(OWNER_TEAM);
    }

    public void setOwnerTeam(String teamName) {
        entityData.set(
                OWNER_TEAM,
                teamName != null ? teamName : ""
        );
    }

    public ScanVisibility getVisibility() {
        return ScanVisibility.fromId(
                entityData.get(VISIBILITY)
        );
    }

    public void setVisibility(ScanVisibility visibility) {
        entityData.set(VISIBILITY, visibility.id());
    }

    public float getMaxRadius() {
        return entityData.get(MAX_RADIUS);
    }

    public void setMaxRadius(float radius) {
        entityData.set(MAX_RADIUS, Math.max(0.0F, radius));
    }

    public int getExpansionTicks() {
        return entityData.get(EXPANSION_TICKS);
    }

    public void setExpansionTicks(int ticks) {
        entityData.set(EXPANSION_TICKS, Math.max(1, ticks));
    }

    public int getHighlightTicks() {
        return entityData.get(HIGHLIGHT_TICKS);
    }

    public void setHighlightTicks(int ticks) {
        entityData.set(HIGHLIGHT_TICKS, Math.max(1, ticks));
    }

    public long getStartGameTime() {
        return entityData.get(START_GAME_TIME);
    }

    public void setStartGameTime(long gameTime) {
        entityData.set(START_GAME_TIME, gameTime);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        UUID ownerUUID = getOwnerUUID();

        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }

        tag.putString("OwnerTeam", getOwnerTeam());
        tag.putByte("Visibility", getVisibility().id());
        tag.putFloat("MaxRadius", getMaxRadius());
        tag.putInt("ExpansionTicks", getExpansionTicks());
        tag.putInt("HighlightTicks", getHighlightTicks());
        tag.putLong("StartGameTime", getStartGameTime());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            setOwnerUUID(tag.getUUID("OwnerUUID"));
        }

        setOwnerTeam(tag.getString("OwnerTeam"));
        setVisibility(
                ScanVisibility.fromId(tag.getByte("Visibility"))
        );

        setMaxRadius(tag.getFloat("MaxRadius"));
        setExpansionTicks(tag.getInt("ExpansionTicks"));
        setHighlightTicks(tag.getInt("HighlightTicks"));
        setStartGameTime(tag.getLong("StartGameTime"));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkManager.createAddEntityPacket(this);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }
}
