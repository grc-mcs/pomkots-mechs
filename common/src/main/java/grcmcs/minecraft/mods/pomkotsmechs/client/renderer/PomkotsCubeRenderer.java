package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsCubeRenderer extends GeoBlockRenderer<PomkotsCubeBlockEntity> {
    public PomkotsCubeRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new PomkotsCubeModel());
    }

//    /**
//     * 視錐台の外でもレンダー対象にする。
//     */
//    @Override
//    public boolean shouldRenderOffScreen(
//            PomkotsCubeBlockEntity blockEntity
//    ) {
//        return true;
//    }

    /**
     * BlockEntityRendererの描画距離。
     *
     * 128を返す場合、おおむね128ブロック以内が対象。
     */
    @Override
    public int getViewDistance() {
        return 128;
    }
}