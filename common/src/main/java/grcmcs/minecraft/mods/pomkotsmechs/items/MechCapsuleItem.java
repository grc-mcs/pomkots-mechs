package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MechCapsuleProjectileEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class MechCapsuleItem extends Item {
    public MechCapsuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);


        CompoundTag root = stack.getTag();

        if (root == null || !root.contains(PomkotsMechs.nbtName("MechPreset"))) {
            return base;
        }

        CompoundTag preset = root.getCompound(PomkotsMechs.nbtName("MechPreset"));

        if (!preset.contains(PomkotsMechs.nbtName("ModelName"))) {
            return base;
        }

        String no = preset.getString(PomkotsMechs.nbtName("ModelName"));

        if (no.startsWith("Pmb")) {
            return base.copy().append(" (Boss: " + no + ")");
        } else if (no.startsWith("Pms")) {
            return base.copy().append(" (Mob: " + no + ")");
        } else {
            return base.copy().append(" (Model: " + no + ")");
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            MechCapsuleProjectileEntity projectile = new MechCapsuleProjectileEntity(level, player);
            projectile.setItem(stack);
            projectile.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    1.5F,   // 初速
                    1.0F    // ブレ
            );
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static LivingEntity generateEntity(Level level, ItemStack deployItem) {
        CompoundTag root = deployItem.getTag();

        if (root == null || !root.contains(PomkotsMechs.nbtName("MechPreset"))) {
            return null;
        }

        CompoundTag preset = root.getCompound(PomkotsMechs.nbtName("MechPreset"));

        if (!preset.contains(PomkotsMechs.nbtName("EntityType"))) {
            return null;
        }

        String entityTypeId = preset.getString(PomkotsMechs.nbtName("EntityType"));
        var entType = getEntityType(new ResourceLocation(entityTypeId));
        var ent = entType.create(level);

        if (ent instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Pmvc01Entity mech) {
                applyPresetToCustomMech(preset, mech);
                return mech;
            } else {
                return livingEntity;
            }
        } else {
            return null;
        }
    }

    public static EntityType<?> getEntityType(ResourceLocation id) {
        return BuiltInRegistries.ENTITY_TYPE.get(id);
    }

    public static void applyPresetToCustomMech(
            CompoundTag preset,
            Pmvc01Entity mech
    ) {
        if (preset.contains(PomkotsMechs.nbtName("Color"))) {
            mech.setTextureColor(preset.getInt(PomkotsMechs.nbtName("Color")));
        }

        if (!preset.contains(PomkotsMechs.nbtName("Parts"))) return;

        ListTag parts = preset.getList(PomkotsMechs.nbtName("Parts"), Tag.TAG_COMPOUND);
        for (Tag t : parts) {
            CompoundTag p = (CompoundTag) t;

            int slot = p.getInt(PomkotsMechs.nbtName("Slot"));
            String itemId = p.getString(PomkotsMechs.nbtName("Item"));

            ItemStack partStack = ItemStack.EMPTY;
            if (!PomkotsMechs.nbtName("empty").equals(itemId)) {
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(itemId));
                int count = p.getInt(PomkotsMechs.nbtName("Count"));

                partStack = new ItemStack(item, count);

                if (p.contains(PomkotsMechs.nbtName("Tag"))) {
                    partStack.setTag(p.getCompound(PomkotsMechs.nbtName("Tag")).copy());
                }
            }

            mech.setItem(slot, partStack);
        }

        mech.setChanged();
        mech.setHealth(mech.getMaxHealth());
    }

    public static CompoundTag createPresetTag(
            String modelName,
            ResourceLocation mechType,
            int color,
            List<PresetPart> parts
    ) {
        CompoundTag root = new CompoundTag();
        CompoundTag preset = new CompoundTag();

        preset.putString(PomkotsMechs.nbtName("ModelName"), modelName);
        preset.putString(PomkotsMechs.nbtName("EntityType"), mechType.toString());

        if (mechType.getPath().equals("pmvc01")) {
            preset.putInt(PomkotsMechs.nbtName("Color"), color);

            if (parts != null) {
                ListTag list = new ListTag();
                for (PresetPart p : parts) {
                    CompoundTag t = new CompoundTag();
                    t.putInt(PomkotsMechs.nbtName("Slot"), p.slot());
                    t.putString(PomkotsMechs.nbtName("Item"), p.item().toString());
                    t.putInt(PomkotsMechs.nbtName("Count"), p.count());
                    if (p.tag() != null) {
                        t.put(PomkotsMechs.nbtName("Tag"), p.tag());
                    }
                    list.add(t);
                }

                preset.put(PomkotsMechs.nbtName("Parts"), list);
            }
        }

        root.put(PomkotsMechs.nbtName("MechPreset"), preset);

        return root;
    }

    public record PresetPart(
            int slot,
            ResourceLocation item,
            int count,
            CompoundTag tag
    ) {}

    private static CompoundTag levelTag(int lv) {
        CompoundTag t = new CompoundTag();
        t.putInt(PomkotsMechs.nbtName("Level"), lv);
        return t;
    }

    //"0gray", "1darkgray", "2white", "3red", "4green", "5lightgreen","6blue", "7orange", "rusty"
    public static void buildPreset(String presetName, ItemStack stack) {
        CompoundTag preset = null;

        if ("Rusty".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    8,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("rustyhead"), 1, levelTag(2)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("rustybody"), 1, levelTag(2)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("rustyarm"), 1, levelTag(2)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("rustylegs"), 1, levelTag(2)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("shiga"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("narita"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("shakuji"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("tenpou"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("kawasemi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("kawasemi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinerifle"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("empty"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Deneb".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    6,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("denebhead"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("denebbody"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("denebarm"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("deneblegs"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("saga"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("kansai"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("shinobazu"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("senzoku"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("tsubame"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("tsubame"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinemachinegun"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("magazineshotgun"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Altair".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    3,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("altairhead"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("altairbody"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("altairarm"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("altairlegs"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("saga"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("kansai"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("shinobazu"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("tenpou"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("nosuri"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("biwa"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinemachinegun"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("empty"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Vega".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    7,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("vegahead"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("vegabody"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("vegaarm"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("vegalegs"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("shiga"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("narita"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("biwa"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("biwa"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Sirius".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    2,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("siriushead"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("siriusbody"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("siriusarm"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("siriuslegs"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("saga"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("kansai"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("shakuji"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("shakuji"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("empty"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("kawasemi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinerifle"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("magazinerifle"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("empty"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Aldebaran".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    4,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("aldebaranhead"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("aldebaranbody"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("aldebaranarm"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("aldebaranlegs"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("chiba"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("haneda"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("suwa"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("suwa"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Muknvali".equals(presetName)) {
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmvc01"),
                    1,
                    List.of(
                            new PresetPart(Pmvc01Entity.INV_PARTS_HEAD, PomkotsMechs.id("muknvalihead"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BODY, PomkotsMechs.id("muknvalibody"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_ARMS, PomkotsMechs.id("muknvaliarm"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_LEGS, PomkotsMechs.id("muknvalilegs"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_GENERATOR, PomkotsMechs.id("chiba"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_PARTS_BOOSTER, PomkotsMechs.id("haneda"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_HAND, PomkotsMechs.id("kagenobu"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, PomkotsMechs.id("mukudori"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, PomkotsMechs.id("biwa"), 1, levelTag(5)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT1, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_WEAPON_EXT2, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RA, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LA, PomkotsMechs.id("empty"), 32, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_RS, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_AMMO_LS, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
                            new PresetPart(Pmvc01Entity.INV_FUEL, PomkotsMechs.id("pellet"), 64, levelTag(1))
                    )
            );
        } else if ("Pmb01Mk2".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb01mk2"),
                    0,
                    null
            );
        } else if ("Pmb02".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb02"),
                    0,
                    null
            );
        } else if ("Pmb03".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb03"),
                    0,
                    null
            );
        } else if ("Pmb04".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb04"),
                    0,
                    null
            );
        } else if ("Pmb05".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb05"),
                    0,
                    null
            );
        } else if ("Pmb06".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb06"),
                    0,
                    null
            );
        } else if ("Pmb07".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb07"),
                    0,
                    null
            );
        } else if ("Pmb08".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb08"),
                    0,
                    null
            );
        } else if ("Pmb99".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pmb99"),
                    0,
                    null
            );
        } else if ("Pms01".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms01"),
                    0,
                    null
            );
        } else if ("Pms02".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms02"),
                    0,
                    null
            );
        } else if ("Pms03".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms03"),
                    0,
                    null
            );
        } else if ("Pms04".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms04"),
                    0,
                    null
            );
        } else if ("Pms05".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms05"),
                    0,
                    null
            );
        } else if ("Pms06".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms06"),
                    0,
                    null
            );
        } else if ("Pms07".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms07"),
                    0,
                    null
            );
        } else if ("Pms08".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms08"),
                    0,
                    null
            );
        } else if ("Pms09".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms09"),
                    0,
                    null
            );
        } else if ("Pms10".equals(presetName)){
            preset = createPresetTag(
                    presetName,
                    PomkotsMechs.id("pms10"),
                    0,
                    null
            );
        }

        if (preset != null) {
            stack.getOrCreateTag().merge(preset);
        }
    }
}
