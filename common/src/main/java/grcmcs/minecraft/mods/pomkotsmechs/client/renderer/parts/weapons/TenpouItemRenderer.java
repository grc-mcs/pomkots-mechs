package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KagenobuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TenpouItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TenpouItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class TenpouItemRenderer extends BasePartsItemRenderer.Weapon<TenpouItem> {
    public TenpouItemRenderer() {
            super(new TenpouItemModel());
    }
}
