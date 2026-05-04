package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class PomkotsLeverBlockEntity extends BlockEntity implements GeoBlockEntity, PomkotsUnbreakableBlock {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected String command = "";

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(PomkotsMechs.nbtName("Command"), command);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.command = tag.getString(PomkotsMechs.nbtName("Command"));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    // ====================== コンストラクタ ======================

    public PomkotsLeverBlockEntity(BlockPos pos, BlockState state) {
        this(PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), pos, state);
    }

    protected PomkotsLeverBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void use(ServerLevel level, BlockPos pos) {
        if (command == null || command.isEmpty()) {
            return;
        }

        // コマンド実行
        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                (ServerLevel) level,
                2, // 権限レベル
                "command_block_once",
                Component.literal("command_block_once"),
                level.getServer(),
                null
        );

        try {
            level.getServer().getCommands().performPrefixedCommand(source, this.command);
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to execute command: {}", this.command, e);
        }
    }

    // ====================== アニメーション関係 ======================

    private boolean playAnimation = false;

    public void playAnimation() {
        if (this.level.isClientSide) {
            this.playAnimation = true;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, event -> {
            if (playAnimation) {
                this.playAnimation = false;
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pomkotscube.use"));
            } else {
                return PlayState.CONTINUE;
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            var sound = soundKeyframeEvent.getKeyframeData().getSound();

            if ("lever".equals(sound)) {
                var pos = this.getBlockPos();
                var v = new Vec3(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
                this.level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), PomkotsMechs.SE_PILEBUNKER_EVENT.get(), SoundSource.PLAYERS, 0.5F, 1.0F, false);
                ParticleUtil.addSparkParticles(v, this.level);
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
