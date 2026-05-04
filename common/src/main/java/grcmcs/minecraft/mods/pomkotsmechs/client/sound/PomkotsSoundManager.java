package grcmcs.minecraft.mods.pomkotsmechs.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 効果音管理ライブラリ
 * フェードイン・アウト、ループ、任意停止に対応
 */
public class PomkotsSoundManager {

    private static final Map<String, PomkotsSoundInstance> activeSounds = new HashMap<>();

    /**
     * サウンドを再生
     *
     * @param id 識別用ID（停止時に使用）
     * @param soundEvent 再生するサウンドイベント
     * @param config 再生設定
     * @return 再生ID
     */
    public static String playSound(String id, SoundEvent soundEvent, SoundConfig config) {
        // 既に同じIDで再生中の場合は停止
        if (activeSounds.get(id) != null) {
            return id;
        }

        PomkotsSoundInstance sound = new PomkotsSoundInstance(soundEvent, config);
        activeSounds.put(id, sound);

        Minecraft.getInstance().getSoundManager().play(sound);

        return id;
    }

    /**
     * ランダムなIDで再生（IDを気にしない場合）
     */
    public static String playSound(SoundEvent soundEvent, SoundConfig config) {
        return playSound(UUID.randomUUID().toString(), soundEvent, config);
    }

    /**
     * サウンドを停止
     *
     * @param id 再生時に指定したID
     * @param fadeOut フェードアウトするか（falseの場合は即座に停止）
     */
    public static void stopSound(String id, boolean fadeOut) {
        PomkotsSoundInstance sound = activeSounds.get(id);
        if (sound != null && !sound.isStopped()) {
            if (fadeOut) {
                sound.startFadeOut();
            } else {
                sound.stopImmediately();
                activeSounds.remove(id);
            }
        }
    }

    /**
     * サウンドを停止（フェードアウトあり）
     */
    public static void stopSound(String id) {
        stopSound(id, true);
    }

    /**
     * 全てのサウンドを停止
     */
    public static void stopAllSounds(int entId, boolean fadeOut) {
        for (String id : activeSounds.keySet().toArray(new String[0])) {
            if (id.startsWith(entId + "")) {
                stopSound(id, fadeOut);
            }
        }
    }

    /**
     * サウンドが再生中か確認
     */
    public static boolean isPlaying(String id) {
        PomkotsSoundInstance sound = activeSounds.get(id);
        return sound != null && !sound.isStopped();
    }

    /**
     * 内部クリーンアップ（停止済みのサウンドを削除）
     */
    public static void cleanup() {
        activeSounds.entrySet().removeIf(entry -> entry.getValue().isStopped());
    }

    public static void cleanup(Minecraft minecraft) {
        cleanup();
    }



}
