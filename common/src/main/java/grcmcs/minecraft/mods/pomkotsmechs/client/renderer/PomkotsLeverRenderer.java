package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsLeverBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsLeverModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsLeverRenderer extends GeoBlockRenderer<PomkotsLeverBlockEntity> {
    public PomkotsLeverRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new PomkotsLeverModel());
    }
}