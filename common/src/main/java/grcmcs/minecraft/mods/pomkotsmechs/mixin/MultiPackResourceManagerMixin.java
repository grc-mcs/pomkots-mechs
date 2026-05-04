package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    private void hook(ResourceLocation loc, CallbackInfoReturnable<Optional<Resource>> cir) {
System.out.println(loc);
        if (loc.getNamespace().equals("pomkotsmechs")
                && loc.getPath().endsWith(".ogg")
                && loc.getPath().contains("bgm/")) {

            ResourceLocation encrypted = new ResourceLocation(
                    loc.getNamespace(),
                    loc.getPath().replace(".ogg", ".dat")
            );

            Optional<Resource> res = ((MultiPackResourceManager)(Object)this).getResource(encrypted);

            if (res.isPresent()) {
                Resource r = res.get();

                cir.setReturnValue(Optional.of(new Resource(
                        r.source(),
                        () -> CryptoUtil.decrypt(r.open())
                )));
            }
        }
    }
}
