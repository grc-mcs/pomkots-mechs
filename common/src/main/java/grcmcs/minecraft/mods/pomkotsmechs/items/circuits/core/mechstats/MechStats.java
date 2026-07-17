package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

public class MechStats {

    private double health;

    private double knockBackResistance;

    private double energyCapacity;

    private double energyRecovery;

    private double energyCost;

    private double moveSpeed;

    private double verticalSpeed;

    private double dashSpeed;

    private double jumpPower;

    private double damageAll;

    private double rifleDamage;

    private double machineGunDamage;

    private double shotgunDamage;

    private double missileDamage;

    private double grenadeDamage;

    private double meleeDamage;

    public static MechStats defaultMech() {
        return new MechStats()
                .health(1.0)
                .knockBackResistance(1.0)
                .energyCapacity(1.0)
                .energyRecovery(1.0)
                .energyCost(1.0)
                .moveSpeed(1.0)
                .verticalSpeed(1.0)
                .dashSpeed(1.0)
                .jumpPower(1.0)
                .damageAll(1.0)
                .rifleDamage(1.0)
                .machineGunDamage(1.0)
                .shotgunDamage(1.0)
                .missileDamage(1.0)
                .grenadeDamage(1.0)
                .meleeDamage(1.0);
    }

    public MechStats() {
    }

    public MechStats(MechStats other) {
        this.health = other.health;
        this.knockBackResistance = other.knockBackResistance;
        this.energyCapacity = other.energyCapacity;
        this.energyRecovery = other.energyRecovery;
        this.energyCost = other.energyCost;
        this.moveSpeed = other.moveSpeed;
        this.verticalSpeed = other.verticalSpeed;
        this.dashSpeed = other.dashSpeed;
        this.jumpPower = other.jumpPower;
        this.damageAll = other.damageAll;
        this.rifleDamage = other.rifleDamage;
        this.machineGunDamage = other.machineGunDamage;
        this.shotgunDamage = other.shotgunDamage;
        this.missileDamage = other.missileDamage;
        this.grenadeDamage = other.grenadeDamage;
        this.meleeDamage = other.meleeDamage;
    }

    public static MechStats copyOf(MechStats other) {
        return new MechStats(other);
    }

    public double get(MechStatType statType) {
        return switch (statType) {
            case HEALTH -> health;
            case KNOCKBACK_RESISTANCE -> knockBackResistance;
            case ENERGY_CAPACITY -> energyCapacity;
            case ENERGY_RECOVERY -> energyRecovery;
            case ENERGY_COST -> energyCost;
            case MOVE_SPEED -> moveSpeed;
            case VERTICAL_SPEED -> verticalSpeed;
            case DASH_SPEED -> dashSpeed;
            case JUMP_POWER -> jumpPower;
            case DAMAGE_ALL -> damageAll;
            case DAMAGE_RIFLE -> rifleDamage;
            case DAMAGE_MACHINE_GUN -> machineGunDamage;
            case DAMAGE_SHOTGUN -> shotgunDamage;
            case DAMAGE_MISSILE -> missileDamage;
            case DAMAGE_GRENADE -> grenadeDamage;
            case DAMAGE_MELEE -> meleeDamage;
        };
    }

    public void set(MechStatType statType, double value) {
        switch (statType) {
            case HEALTH -> health = value;
            case KNOCKBACK_RESISTANCE -> knockBackResistance = value;
            case ENERGY_CAPACITY -> energyCapacity = value;
            case ENERGY_RECOVERY -> energyRecovery = value;
            case ENERGY_COST -> energyCost = value;
            case MOVE_SPEED -> moveSpeed = value;
            case VERTICAL_SPEED -> verticalSpeed = value;
            case DASH_SPEED -> dashSpeed = value;
            case JUMP_POWER -> jumpPower = value;
            case DAMAGE_ALL -> damageAll = value;
            case DAMAGE_RIFLE -> rifleDamage = value;
            case DAMAGE_MACHINE_GUN -> machineGunDamage = value;
            case DAMAGE_SHOTGUN -> shotgunDamage = value;
            case DAMAGE_MISSILE -> missileDamage = value;
            case DAMAGE_GRENADE -> grenadeDamage = value;
            case DAMAGE_MELEE -> meleeDamage = value;
        }
    }

    public void add(MechStatType statType, double value) {
        set(statType, get(statType) + value);
    }

    public void multiply(MechStatType statType, double multiplier) {
        set(statType, get(statType) * multiplier);
    }

    public MechStats copy() {
        return new MechStats(this);
    }

    public double health() {
        return health;
    }

    public MechStats health(double health) {
        this.health = health;
        return this;
    }

    public double energyCapacity() {
        return energyCapacity;
    }

    public MechStats energyCapacity(double energyCapacity) {
        this.energyCapacity = energyCapacity;
        return this;
    }

    public double energyRecovery() {
        return energyRecovery;
    }

    public MechStats energyRecovery(double energyRecovery) {
        this.energyRecovery = energyRecovery;
        return this;
    }

    public double energyCost() {
        return energyCost;
    }

    public MechStats energyCost(double energyCost) {
        this.energyCost = energyCost;
        return this;
    }

    public double moveSpeed() {
        return moveSpeed;
    }

    public MechStats moveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
        return this;
    }

    public double verticalSpeed() {
        return verticalSpeed;
    }

    public MechStats verticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
        return this;
    }

    public double dashSpeed() {
        return dashSpeed;
    }

    public MechStats dashSpeed(double dashSpeed) {
        this.dashSpeed = dashSpeed;
        return this;
    }

    public double jumpPower() {
        return jumpPower;
    }

    public MechStats jumpPower(double jumpPower) {
        this.jumpPower = jumpPower;
        return this;
    }

    public double damageAll() {
        return damageAll;
    }

    public MechStats damageAll(double damageAll) {
        this.damageAll = damageAll;
        return this;
    }

    public double rifleDamage() {
        return rifleDamage;
    }

    public MechStats rifleDamage(double rifleDamage) {
        this.rifleDamage = rifleDamage;
        return this;
    }

    public double machineGunDamage() {
        return machineGunDamage;
    }

    public MechStats machineGunDamage(double machineGunDamage) {
        this.machineGunDamage = machineGunDamage;
        return this;
    }

    public double shotgunDamage() {
        return shotgunDamage;
    }

    public MechStats shotgunDamage(double shotgunDamage) {
        this.shotgunDamage = shotgunDamage;
        return this;
    }

    public double missileDamage() {
        return missileDamage;
    }

    public MechStats missileDamage(double missileDamage) {
        this.missileDamage = missileDamage;
        return this;
    }

    public double grenadeDamage() {
        return grenadeDamage;
    }

    public MechStats grenadeDamage(double grenadeDamage) {
        this.grenadeDamage = grenadeDamage;
        return this;
    }

    public double meleeDamage() {
        return meleeDamage;
    }

    public MechStats meleeDamage(double meleeDamage) {
        this.meleeDamage = meleeDamage;
        return this;
    }

    public double knockBackResistance() {
        return knockBackResistance;
    }

    public MechStats knockBackResistance(double knockBackResistance) {
        this.knockBackResistance = knockBackResistance;
        return this;
    }
}