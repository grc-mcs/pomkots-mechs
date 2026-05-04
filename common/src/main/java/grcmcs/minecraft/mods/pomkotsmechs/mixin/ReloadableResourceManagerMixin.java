package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.EncryptedPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
    @Shadow
    private PackType type;

    @ModifyVariable(
            method = "createReload",
            at = @At("HEAD"),
            argsOnly = true
    )
    private List<PackResources> wrapPacks(List<PackResources> original) {
        if (this.type != PackType.CLIENT_RESOURCES) {
            return original;
        }

//        List<PackResources> wrapped = new ArrayList<>();
//
//        for (PackResources pack : original) {
//            if (pack instanceof EncryptedPackResources) {
//                wrapped.add(pack);
//            } else {
//                wrapped.add(new EncryptedPackResources(pack));
//            }
//        }

        List<PackResources> wrapped = new ArrayList<>();
        for (PackResources pack : original) {
            PomkotsMechs.LOGGER.info("packid:" + pack.packId());
            PomkotsMechs.LOGGER.info("packid:" + pack.getNamespaces(PackType.CLIENT_RESOURCES));
            // MODのパックだけラップ、それ以外は素通し
            if (pack instanceof EncryptedPackResources) {
                wrapped.add(pack);
            } else if (pack.packId().contains(PomkotsMechs.MODID)) {
                wrapped.add(new EncryptedPackResources(pack));
            } else if (pack.getNamespaces(PackType.CLIENT_RESOURCES).contains(PomkotsMechs.MODID)) {
                wrapped.add(new EncryptedPackResources(pack));
            } else {
                wrapped.add(pack);
            }
        }

        return wrapped;
    }
}