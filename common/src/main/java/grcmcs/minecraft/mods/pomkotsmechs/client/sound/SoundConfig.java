package grcmcs.minecraft.mods.pomkotsmechs.client.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * サウンド再生設定
 */
public class SoundConfig {
    float volume = 1.0f;
    float pitch = 1.0f;
    int fadeInTicks = 0;
    int fadeOutTicks = 0;
    int loopCount = 0; // 0=無限ループなし, -1=無限ループ, 1以上=指定回数ループ
    int estimatedDurationTicks = 0; // ループ用のサウンド長さ推定値（tick単位）
    int maxDurationTicks = 0; // 最大再生時間（0=無制限）
    SoundSource soundSource = SoundSource.MASTER;
    Vec3 position = null;
    Supplier<Vec3> positionSupplier = null;
    SoundInstance.Attenuation attenuation = SoundInstance.Attenuation.LINEAR;
    boolean relative = false;

    public String toString() {
        return volume + "," + pitch + "," + fadeInTicks + "," +loopCount + "," + (positionSupplier==null?"null":positionSupplier.get()) + "," + relative;
    }

    public SoundConfig() {
    }

    /**
     * 音量設定（0.0～1.0）
     */
    public SoundConfig volume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        return this;
    }

    /**
     * ピッチ設定（0.5～2.0が一般的）
     */
    public SoundConfig pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    /**
     * フェードイン時間（tick単位、20tick=1秒）
     */
    public SoundConfig fadeIn(int ticks) {
        this.fadeInTicks = Math.max(0, ticks);
        return this;
    }

    /**
     * フェードアウト時間（tick単位、20tick=1秒）
     */
    public SoundConfig fadeOut(int ticks) {
        this.fadeOutTicks = Math.max(0, ticks);
        return this;
    }

    /**
     * ループ回数設定
     *
     * @param count 0=ループなし, -1=無限ループ, 1以上=指定回数ループ
     * @param estimatedDurationTicks 1ループの推定時間（tick単位）
     */
    public SoundConfig loop(int count, int estimatedDurationTicks) {
        this.loopCount = count;
        this.estimatedDurationTicks = estimatedDurationTicks;
        return this;
    }

    /**
     * 無限ループ（手動停止まで再生）
     */
    public SoundConfig loopIndefinitely() {
        this.loopCount = -1;
        return this;
    }

    /**
     * 最大再生時間設定（tick単位）
     * この時間を超えたら自動的にフェードアウト
     */
    public SoundConfig maxDuration(int ticks) {
        this.maxDurationTicks = ticks;
        return this;
    }

    /**
     * サウンドソース設定
     */
    public SoundConfig source(SoundSource source) {
        this.soundSource = source;
        return this;
    }

    /**
     * 固定位置でサウンドを再生（距離減衰あり）
     */
    public SoundConfig at(Vec3 position) {
        this.position = position;
        this.positionSupplier = null;
        return this;
    }

    /**
     * 固定位置でサウンドを再生（座標指定）
     */
    public SoundConfig at(double x, double y, double z) {
        return at(new Vec3(x, y, z));
    }

    /**
     * エンティティに追従してサウンドを再生
     */
    public SoundConfig followEntity(Entity entity) {
        this.positionSupplier = entity::position;
        this.position = null;
        return this;
    }

    /**
     * 動的な位置でサウンドを再生（位置を返すSupplier）
     */
    public SoundConfig followPosition(Supplier<Vec3> positionSupplier) {
        this.positionSupplier = positionSupplier;
        this.position = null;
        return this;
    }

    /**
     * 減衰タイプ設定
     * LINEAR: 距離に応じて線形減衰（デフォルト）
     * NONE: 減衰なし（全体に聞こえる）
     */
    public SoundConfig attenuation(SoundInstance.Attenuation attenuation) {
        this.attenuation = attenuation;
        return this;
    }

    /**
     * 相対位置モード（プレイヤーからの相対位置）
     */
    public SoundConfig relative(boolean relative) {
        this.relative = relative;
        return this;
    }

    // プリセット

    /**
     * 短いSE用のデフォルト設定
     */
    public static SoundConfig shortSound() {
        return new SoundConfig().volume(0.8f);
    }

    /**
     * BGM用のデフォルト設定
     */
    public static SoundConfig loopFadeSE() {
        return new SoundConfig()
                .volume(1f)
                .fadeIn(20)  // 2秒
                .fadeOut(10) // 2秒
                .loopIndefinitely();
    }

    /**
     * アンビエント音用の設定
     */
    public static SoundConfig ambient() {
        return new SoundConfig()
                .volume(0.3f)
                .fadeIn(60)  // 3秒
                .fadeOut(60) // 3秒
                .loopIndefinitely();
    }
}
