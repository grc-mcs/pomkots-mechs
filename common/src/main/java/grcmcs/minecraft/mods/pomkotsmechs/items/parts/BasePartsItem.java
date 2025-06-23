package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BasePartsItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Supplier<Object> renderProvider;

    public BasePartsItem(Properties properties) {
        super(properties);
        if (Platform.isFabric()) {
            renderProvider = GeoItem.makeRenderer(this);
        }
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    public static BiConsumer<Consumer<Object>, BasePartsItem> regForForge;

    public void initializeClient(Consumer<Object> consumer) {
        regForForge.accept(consumer, this);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = (BlockEntityWithoutLevelRenderer)newRenderer();

                return this.renderer;
            }
        });
    }

    public abstract Object newRenderer();

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private CachedBoneFinder parentBoneFinder = null;

    public void setCachedBoneFinder(CachedBoneFinder b) {
        parentBoneFinder = b;
    }

    public CachedBoneFinder getCachedBoneFinder() {
        return parentBoneFinder;
    }

    private Pmvc01Entity parentEntity = null;

    public void setParentEntity(Pmvc01Entity b) {
        parentEntity = b;
    }

    public Pmvc01Entity getParentEntity() {
        return parentEntity;
    }

    private String attachSide = "right";

    public String getSide() {
        return attachSide;
    }

    public void setSide(String s) {
        attachSide = s;
    }

    public abstract String getPartsSeriesName();

    public abstract String getPartsCategory();

    public String getWeaponCategory() {
        return null;
    }

    public int getLevel(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("Level")) {
            setLevel(stack, getMaxLevel());
            return getMaxLevel();
        } else {
            return stack.getTag().getInt("Level");
        }
    }

    public void setLevel(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt(PomkotsMechs.nbtName("Level"), level);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        setLevel(stack, 1);
    }

    public int getMaxLevel() {
        var data = getPartsData();
        if (data == null || data.levels == null || data.levels.isEmpty()) {
            return 1;
        } else {
            return data.levels.size();
        }
    }

    private PomkotsDataPack.PartsData getPartsData() {
        if (this instanceof BasePartsItem.Head || this instanceof BasePartsItem.Body || this instanceof BasePartsItem.Arm || this instanceof BasePartsItem.Legs) {
            return PomkotsDataPackManager.getInstance().getDataPack().getPartsData(getPartsSeriesName() + getPartsCategory());
        } else {
            return PomkotsDataPackManager.getInstance().getDataPack().getPartsData(getPartsSeriesName());
        }
    }

    private PomkotsDataPack.LevelData getLevelData(ItemStack stack, PomkotsDataPack.PartsData data) {
        if (data != null && data.levels != null && !data.levels.isEmpty()) {
            int level = getLevel(stack);
            return data.levels.get(level - 1);
        } else {
            return new PomkotsDataPack.LevelData();
        }
    }

    public String getAdditionalDescription(ItemStack stack) {
        var data = getPartsData();
        return (data != null) ? data.description : "";

    }

    public int getWeight(ItemStack stack) {
        var data = getPartsData();
        return (data != null) ? data.weight : 0;
    }

    public int getDurability(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.durability;
    }

    public int getMaxWeight(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.maxWeight;
    }

    public float getSpeedModifier(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.speedModifier;
    }

    public float getJumpModifier(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.jumpModifier;
    }

    public float getDamage(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.damage;
    }

    public int getMissileMaxNum(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.missileMaxNum;
    }

    public float getMissileLockInterval(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.missileLockInterval;
    }

    public int getMaxEnergy(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.maxEnergy;
    }

    public int getEnergyChargePerTick(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.energyChargePerTick;
    }

    public int getWorkSecPerFuel(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.workSecPerFuel;
    }

    public int getEnergyConsumeEvasion(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.energyConsumeEvasion;
    }

    public int getEnergyConsumeVertical(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.energyConsumeVertical;
    }

    public float getSpeedModifierEvasion(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.speedModifierEvasion;
    }

    public float getSpeedModifierVertical(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.speedModifierVertical;
    }

    public int getBulletsPerMagazine(ItemStack stack) {
        var data = getLevelData(stack, getPartsData());
        return data.bulletsPerMagazine;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        // ツールチップに情報を追加
        tooltip.add(Component.literal(getAdditionalDescription(stack)).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Level: " + getLevel(stack)).withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Weight: " + String.format("%d", getWeight(stack))).withStyle(ChatFormatting.WHITE));

        if (this instanceof Weapon) {
            tooltip.add(Component.literal("Attack: " + String.format("%.1f", getDamage(stack))).withStyle(ChatFormatting.WHITE));

            if ("missile".equals(getWeaponCategory())) {
                tooltip.add(Component.literal("Max Missile Num: " + String.format("%d", getMissileMaxNum(stack))).withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Lock Interval: " + String.format("%.1f", getMissileLockInterval(stack))).withStyle(ChatFormatting.WHITE));
            }
        } else if (this instanceof Generator) {
            tooltip.add(Component.literal("Max Energy: " + String.format("%d", getMaxEnergy(stack))).withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.literal("Energy Charge / Tick: " + String.format("%d", getEnergyChargePerTick(stack))).withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.literal("Work Sec / Fuel: " + String.format("%d", getWorkSecPerFuel(stack))).withStyle(ChatFormatting.WHITE));

        } else if (this instanceof Booster) {
            tooltip.add(Component.literal("Speed Modifier Evasion: " + String.format("%.1f", getSpeedModifierEvasion(stack))).withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.literal("Speed Modifier Vertical: " + String.format("%.1f", getSpeedModifierVertical(stack))).withStyle(ChatFormatting.WHITE));

            tooltip.add(Component.literal("Energy Consume Evasion: " + String.format("%d", getEnergyConsumeEvasion(stack))).withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.literal("Energy Consume Vertical: " + String.format("%d", getEnergyConsumeVertical(stack))).withStyle(ChatFormatting.WHITE));

        } else if (this instanceof MechParts){
            tooltip.add(Component.literal("Durability: " + String.format("%d", getDurability(stack))).withStyle(ChatFormatting.WHITE));
            if ("legs".equals(getPartsCategory())) {
                tooltip.add(Component.literal("Max Weight: " + String.format("%d", getMaxWeight(stack))).withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Speed: " + String.format("%.1f", getSpeedModifier(stack))).withStyle(ChatFormatting.WHITE));
                tooltip.add(Component.literal("Jump Speed: " + String.format("%.1f", getJumpModifier(stack))).withStyle(ChatFormatting.WHITE));
            }
        }
    }

    public String getDefaultColor() {
        return "gray";
    }

    public static abstract class MechParts extends BasePartsItem {
        public MechParts(Properties properties) {
            super(properties);
        }
    }

    public static abstract class Head extends MechParts {
        public Head(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "head";
        }
        public boolean isFullCovered() {
            return false;
        }
    }

    public static abstract class Body extends MechParts {
        public Body(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "body";
        }
    }

    public static abstract class Arm extends MechParts {
        public Arm(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "arm";
        }
    }

    public static abstract class Legs extends MechParts {
        public Legs(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "legs";
        }
    }

    public static abstract class Generator extends MechParts {
        public Generator(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "generator";
        }
    }

    public static abstract class Booster extends MechParts {
        public Booster(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "booster";
        }
    }

    public static abstract class Extension extends BasePartsItem {
        public Extension(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "extension";
        }
    }

    public static abstract class Magazine extends BasePartsItem {
        public Magazine(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "magazine";
        }
    }

    public static abstract class Fuel extends BasePartsItem {
        public Fuel(Properties properties) {
            super(properties);
        }

        public String getPartsCategory() {
            return "fuel";
        }
    }

    public static interface WeaponInterface {
        public static final String ATTACH_POINT_HAND = "hand";
        public static final String ATTACH_POINT_ARM = "arm";
        public static final String ATTACH_POINT_SHOULDER = "shoulder";

        public void tickWeaponInAction(ActionWeapon.WeaponMechInterface context, int tick, boolean isOnFire);
        public String getWeaponAttachPoint();
        public Motion getMotion();
        public int getCoolTime();
        public int maxMultiLockNum();
        public boolean isSoftLockEnabled();
        default public void startUsing(ActionWeapon.WeaponMechInterface context) {
        }
        default public void endUsing(ActionWeapon.WeaponMechInterface context) {
        }
        default public boolean isMatchAmmo(Magazine mag) {
            return false;
        }
    }

    public static abstract class Weapon extends BasePartsItem implements WeaponInterface {

        public Weapon(Properties properties) {
            super(properties);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        public int maxMultiLockNum() {
            return 0;
        }

        public boolean isSoftLockEnabled() {
            return false;
        }

        public void tickWeaponInAction(ActionWeapon.WeaponMechInterface context, int tick, boolean isOnFire) {
            //NOP
        }

        public String getPartsCategory() {
            return "weapon";
        }
    }

    public static abstract class WeaponArm extends Weapon {
        public WeaponArm(Properties properties) {
            super(properties);
        }
    }

    public static abstract class WeaponShoulder extends Weapon {
        public WeaponShoulder(Properties properties) {
            super(properties);
        }
    }
}
