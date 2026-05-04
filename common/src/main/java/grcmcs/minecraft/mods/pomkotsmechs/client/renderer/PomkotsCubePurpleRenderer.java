package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockPurpleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockRedEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubePurpleModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeRedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsCubePurpleRenderer extends GeoBlockRenderer<PomkotsCubeBlockPurpleEntity> {
    public PomkotsCubePurpleRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new PomkotsCubePurpleModel());
    }
}