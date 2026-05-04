package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Mixin(PathPackResources.class)
public abstract class PathPackResourcesMixin implements PackResources {

    @Inject(
            method = "getResource",
            at = @At("RETURN"),
            cancellable = true
    )
    private void interceptOggResource(
            PackType type,
            ResourceLocation location,
            CallbackInfoReturnable<IoSupplier<InputStream>> cir
    ) {
        // このパックがMODのものか確認
        if (!this.packId().contains(PomkotsMechs.MODID)) return;
        // oggのみ対象
        System.out.println(location);

        if (!location.getPath().endsWith(".ogg")) return;

        IoSupplier<InputStream> original = cir.getReturnValue();
        if (original == null) return;

        cir.setReturnValue(() -> {
            return CryptoUtil.decrypt(original.get());
        });
    }
}
