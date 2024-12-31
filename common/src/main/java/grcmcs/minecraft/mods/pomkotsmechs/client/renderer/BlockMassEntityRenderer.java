package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockMassEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockMassEntityRenderer extends EntityRenderer<BlockMassEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public BlockMassEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(BlockMassEntity entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
        BlockState[][][] states = entity.getBlockStates();
        Vec3 interpolatedPos = entity.getPosition(partialTicks).add(-entity.xo, -entity.yo, -entity.zo);
//        stack.pushPose();
//
//        stack.translate(interpolatedPos.x, interpolatedPos.y, interpolatedPos.z);
        for (int x = 0; x < states.length; x++) {
            for (int y = 0; y < states[x].length; y++) {
                for (int z = 0; z < states[x][y].length; z++) {
                    BlockState state = states[x][y][z];
                    if (state != null) {
                        stack.pushPose();
                        stack.translate(x - 2.0F, y, z - 2.0F); // 原点からのオフセット

                        BakedModel model = blockRenderer.getBlockModel(state);
                        blockRenderer.getModelRenderer().renderModel(
                                stack.last(),
                                buffer.getBuffer(RenderType.solid()),
                                state,
                                model,
                                1.0F, 1.0F, 1.0F,
                                light,
                                OverlayTexture.NO_OVERLAY);

                        stack.popPose();
                    }
                }
            }
        }
//        stack.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(BlockMassEntity entity) {
        return null; // テクスチャは不要
    }
}
