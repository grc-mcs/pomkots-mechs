package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsBGMManager;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {

    @Shadow
    private int nextSongDelay;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (PomkotsBGMManager.isSuppressingVanilla()) {
            // バニラBGMを止め続ける
            nextSongDelay = 20 * 60 * 60;
            MusicManager self = (MusicManager)(Object)this;
            // 流れてたら止める
            self.stopPlaying();
        }
    }
}
