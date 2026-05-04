package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.audio.OggAudioStream;
import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(SoundBufferLibrary.class)
public class SoundBufferLibraryMixin {

    @Inject(
            method = "getStream",
            at = @At("HEAD"),
            cancellable = true
    )
    private void hook(ResourceLocation loc, boolean loop, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
        // 対象以外は何もしない
        if (!loc.getNamespace().equals("pomkotsmechs")) return;
        if (!loc.getPath().endsWith(".ogg")) return;
        if (!loc.getPath().contains("bgm/")) return;

        // 差し替え
        CompletableFuture<AudioStream> future = CompletableFuture.supplyAsync(() -> {
            try {
                ResourceLocation encrypted = new ResourceLocation(
                        loc.getNamespace(),
                        loc.getPath().replace(".ogg", ".dat")
                );

                InputStream dat = Minecraft.getInstance()
                        .getResourceManager()
                        .open(encrypted);

                InputStream decrypted = CryptoUtil.decrypt(dat);

                return loop
                        ? new LoopingAudioStream(OggAudioStream::new, decrypted)
                        : new OggAudioStream(decrypted);

            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.backgroundExecutor());

        cir.setReturnValue(future);
    }
}
