package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BlockProjectileEntityRenderer extends EntityRenderer<BlockProjectileEntity> {
    private final ItemRenderer itemRenderer;

    public BlockProjectileEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BlockProjectileEntity entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource source, int light) {
        stack.pushPose();
        stack.scale(1F, 1F, 1F);

        this.itemRenderer.renderStatic(
                new ItemStack(entity.getBlockState().getBlock()),
                ItemDisplayContext.NONE,
                light,
                OverlayTexture.NO_OVERLAY,
                stack,
                source,
                entity.level(),
                entity.getId()
        );

        stack.popPose();
        super.render(entity, yaw, partialTicks, stack, source, light);
    }

    @Override
    public ResourceLocation getTextureLocation(BlockProjectileEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
