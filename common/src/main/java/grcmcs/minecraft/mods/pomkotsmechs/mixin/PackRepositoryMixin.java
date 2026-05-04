package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.google.common.collect.ImmutableMap;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.EncryptedPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Inject(
            method = "discoverAvailable",
            at = @At("RETURN"),
            cancellable = true
    )
    private void wrapModPacks(CallbackInfoReturnable<Map<String, Pack>> cir) {
        Map<String, Pack> original = cir.getReturnValue();
        Map<String, Pack> modified = new HashMap<>(original);

        modified.replaceAll((id, pack) -> {
            if (!id.contains(PomkotsMechs.MODID)) return pack;

            Pack wrapped = Pack.readMetaAndCreate(
                    pack.getId(),
                    pack.getTitle(),
                    true,
                    path -> new EncryptedPackResources(
                            new PathPackResources(PomkotsMechs.MODID, PomkotsMechs.getModRootPath(), true)
                    ),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );
            return wrapped != null ? wrapped : pack;
        });

        cir.setReturnValue(ImmutableMap.copyOf(modified));
    }
}
