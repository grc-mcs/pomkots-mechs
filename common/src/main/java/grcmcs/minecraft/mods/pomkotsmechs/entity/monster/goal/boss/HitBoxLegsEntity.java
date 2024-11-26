package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HitBoxLegsEntity extends HitBoxEntity {
    public static AttributeSupplier.Builder createMobAttributes() {
        return createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public HitBoxLegsEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        this(entityType, world, null);
    }

    public HitBoxLegsEntity(EntityType<? extends LivingEntity> entityType, Level world, LivingEntity parent) {
        super(entityType, world, parent);
    }

    private float damageCount = 100;

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 当たった場合、親エンティティにダメージを伝える
        if (isParentActive()) {
            damageCount -= amount;

            if (damageCount < 0 && breakCallback != null) {
                breakCallback.accept(null);
                damageCount = 100;
            }

            return parentEntity.hurt(source, amount * 0.5F);
        } else {
            return super.hurt(source, amount);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (isParentActive()) {
            return parentEntity.interact(player, hand);
        } else {
            return super.interact(player, hand);
        }
    }
//
//    @Override
//    public boolean isInvisible() {
//        // エンティティを常に不可視に設定
//        return true;
//    }
//
//    @Override
//    public boolean shouldRenderAtSqrDistance(double distance) {
//        // どの距離でも表示しない
//        return false;
//    }
}
