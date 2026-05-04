package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockYellowEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeYellowModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class PomkotsCubeYellowRenderer extends GeoBlockRenderer<PomkotsCubeBlockYellowEntity> {
    public PomkotsCubeYellowRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new PomkotsCubeYellowModel());
    }
}