package grcmcs.minecraft.mods.pomkotsmechs.client.sound;


import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class PomkotsSoundInstance extends AbstractTickableSoundInstance {
    private final SoundConfig config;
    private int tickCount = 0;
    private float currentVolume = 0.0f;
    private boolean isFadingOut = false;
    private int loopCount = 0;
    private float targetVolume;
    private final Supplier<Vec3> positionSupplier;

    protected PomkotsSoundInstance(SoundEvent soundEvent, SoundConfig config) {
        super(soundEvent, config.soundSource, RandomSource.create());
        this.config = config;
        this.targetVolume = config.volume;
        this.volume = config.fadeInTicks > 0 ? 0.0f : config.volume;
        this.currentVolume = this.volume;
        this.pitch = config.pitch;
        this.looping = config.loopCount != 0;

        // 位置設定
        if (config.positionSupplier != null) {
            this.positionSupplier = config.positionSupplier;
            this.attenuation = config.attenuation;
            Vec3 pos = config.positionSupplier.get();
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        } else if (config.position != null) {
            this.positionSupplier = () -> config.position;
            this.attenuation = config.attenuation;
            this.x = config.position.x;
            this.y = config.position.y;
            this.z = config.position.z;
        } else {
            this.positionSupplier = null;
            this.attenuation = Attenuation.NONE;
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        this.relative = config.relative;
    }

    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.isStopped()) {
            return;
        }

        tickCount++;

        if (positionSupplier != null && !config.relative) {
            Vec3 pos = positionSupplier.get();
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        }

        // フェードアウト処理
        if (isFadingOut) {
            if (config.fadeOutTicks > 0) {
                currentVolume -= targetVolume / config.fadeOutTicks;
                if (currentVolume <= 0.0f) {
                    currentVolume = 0.0f;
                    this.stop();
                }
            } else {
                this.stop();
            }
            this.volume = currentVolume;
            return;
        }

        // フェードイン処理
        if (tickCount <= config.fadeInTicks) {
            currentVolume = (targetVolume * tickCount) / config.fadeInTicks;
            this.volume = currentVolume;
        } else {
            this.volume = targetVolume;
            currentVolume = targetVolume;
        }

        // ループ処理
        if (config.loopCount > 0) {
            // サウンドの長さを取得するのは困難なので、推定時間で管理
            int estimatedDuration = config.estimatedDurationTicks;
            if (estimatedDuration > 0 && tickCount >= estimatedDuration * (loopCount + 1)) {
                loopCount++;
                if (loopCount >= config.loopCount) {
                    // ループ終了、フェードアウト開始
                    startFadeOut();
                }
            }
        }

        // 最大再生時間チェック
        if (config.maxDurationTicks > 0 && tickCount >= config.maxDurationTicks) {
            startFadeOut();
        }
    }

    public void startFadeOut() {
        isFadingOut = true;
        this.looping = false;
    }

    public void stopImmediately() {
        this.stop();
    }
}
