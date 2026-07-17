package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.MechTraderEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MechTraderEntityRenderer extends GeoEntityRenderer<MechTraderEntity> {
    public MechTraderEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MechTraderEntityModel());
    }
}