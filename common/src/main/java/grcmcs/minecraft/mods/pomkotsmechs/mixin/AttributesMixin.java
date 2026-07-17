package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Attributes.class)
public abstract class AttributesMixin {

    private static final double MAX_HEALTH_LIMIT = 100_000.0D;

    /**
     * Attributes.MAX_HEALTH の RangedAttribute 生成時に、
     * コンストラクター第4引数の最大値を差し替える。
     *
     * RangedAttribute(String descriptionId,
     *                 double defaultValue,
     *                 double minValue,
     *                 double maxValue)
     */
    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/RangedAttribute;<init>(Ljava/lang/String;DDD)V",
                    ordinal = 0
            ),
            index = 3
    )
    private static double pomkotsmechs$increaseMaxHealthLimit(double original) {
        return MAX_HEALTH_LIMIT;
    }
}
