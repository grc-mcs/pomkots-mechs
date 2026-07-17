package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;

public final class MechStatus {

    private MechStats stats;

    private MechFeatures features;

    public MechStatus(
    ) {
        this.stats = MechStats.defaultMech();
        this.features = new MechFeatures();
    }

    public MechStatus(
            MechStats stats,
            MechFeatures features
    ) {
        this.stats = stats;
        this.features = features;
    }

    public MechStats stats() {
        return stats;
    }

    public MechFeatures features() {
        return features;
    }

    public MechStatus copy() {
        return new MechStatus(
                stats.copy(),
                features.copy()
        );
    }

    public void setupBaseMechParams(Pmvc01Entity.MechParam param) {
        this.stats = MechStats.defaultMech();
        this.stats.health(param.durability);
        this.stats.knockBackResistance(Pmvc01Entity.BASE_KNOCKBACK_RESISTANCE);
        this.stats.moveSpeed(param.speed);
        this.stats.jumpPower(param.jump);
        this.stats.energyCapacity(param.maxEnergy);
        this.stats.energyRecovery(param.energyChargePerTick);
        this.stats.dashSpeed(param.speedModifierEvasion);
        this.stats.verticalSpeed(param.speedModifierVertical);
        this.stats.energyCost(1.0);
    }
}