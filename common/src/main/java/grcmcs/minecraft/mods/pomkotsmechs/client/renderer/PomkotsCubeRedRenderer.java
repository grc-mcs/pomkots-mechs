package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockRedEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockYellowEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeRedModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeYellowModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class PomkotsCubeRedRenderer extends GeoBlockRenderer<PomkotsCubeBlockRedEntity> {
    public PomkotsCubeRedRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new PomkotsCubeRedModel());
    }
}