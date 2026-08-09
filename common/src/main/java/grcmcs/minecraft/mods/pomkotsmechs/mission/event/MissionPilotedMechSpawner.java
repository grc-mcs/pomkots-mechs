package grcmcs.minecraft.mods.pomkotsmechs.mission.event;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DodoItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class MissionPilotedMechSpawner {
    private MissionPilotedMechSpawner() {
    }

    public static Result spawn(ServerLevel level, Vec3 position, float yaw, Configuration configuration) {
        Pmvc01Entity mech = new Pmvc01Entity(PomkotsMechs.PMVC01.get(), level);
        MechPilotEntity pilot = new MechPilotEntity(PomkotsMechs.MECH_PILOT.get(), level);
        try {
            setRotation(mech, yaw);
            setRotation(pilot, yaw);
            mech.setPos(position);
            pilot.setPos(position);
            mech.setShouldSave(false);
            pilot.setShouldSave(false);

            ItemStack[] equipment = configuration.mechEquipment();
            if (equipment.length != 12) {
                throw new IllegalArgumentException("Piloted mech equipment must contain exactly 12 slots");
            }
            for (int slot = 0; slot < equipment.length; slot++) {
                mech.setItem(slot, equipment[slot].copy());
            }
            ItemStack[] additionalCircuits = configuration.additionalCircuits();
            int circuitCount = Math.min(18, additionalCircuits.length);
            for (int slot = 0; slot < circuitCount; slot++) {
                mech.setItem(
                        Pmvc01Entity.CONTAINER_ADDITIONAL_CIRCUIT_START_INDEX + slot,
                        additionalCircuits[slot].copy());
            }
            mech.setTextureColor(configuration.textureColor());
            if (configuration.supplyConsumables()) supplyConsumables(mech);
            mech.setChanged();
            mech.setHealth(mech.getMaxHealth());

            pilot.setItemInHand(InteractionHand.MAIN_HAND, configuration.license().copy());
            pilot.setItemInHand(InteractionHand.OFF_HAND, configuration.role().copy());
            pilot.setModelLocation(configuration.model());
            pilot.setTextureLocation(configuration.texture());
            if (configuration.combatActionRange() > 0.0D) {
                pilot.setCombatActionRange(position, configuration.combatActionRange());
            }
            if (configuration.wingmanMasterId() != null) {
                pilot.setWingmanMasterId(configuration.wingmanMasterId());
            }

            if (!level.addFreshEntity(mech)) {
                throw new IllegalStateException("Could not add piloted mech");
            }
            if (!level.addFreshEntity(pilot)) {
                mech.discard();
                throw new IllegalStateException("Could not add mech pilot");
            }
            if (!pilot.startRiding(mech, true)) {
                pilot.discard();
                mech.discard();
                throw new IllegalStateException("Mech pilot could not mount mech");
            }
            return new Result(pilot, mech);
        } catch (RuntimeException exception) {
            if (!pilot.isRemoved()) pilot.discard();
            if (!mech.isRemoved()) mech.discard();
            throw exception;
        }
    }

    private static void supplyConsumables(Pmvc01Entity mech) {
        supplyMagazine(mech, Pmvc01Entity.INV_WEAPON_RIGHT_HAND, Pmvc01Entity.INV_AMMO_RA);
        supplyMagazine(mech, Pmvc01Entity.INV_WEAPON_LEFT_HAND, Pmvc01Entity.INV_AMMO_LA);
        supplyMagazine(mech, Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, Pmvc01Entity.INV_AMMO_RS);
        supplyMagazine(mech, Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, Pmvc01Entity.INV_AMMO_LS);
        mech.setItem(Pmvc01Entity.INV_FUEL, leveledStack(PomkotsMechs.PELLET.get(), 64));
    }

    private static void supplyMagazine(Pmvc01Entity mech, int weaponSlot, int magazineSlot) {
        if (!(mech.getItem(weaponSlot).getItem() instanceof BasePartsItem.Weapon weapon)) return;
        ItemStack magazine = switch (weapon.getWeaponCategory()) {
            case RIFLE -> leveledStack(PomkotsMechs.RIFLE_MAGAZINE.get(), 32);
            case SHOT_GUN -> leveledStack(PomkotsMechs.SHOTGUN_MAGAZINE.get(), 32);
            case MACHINE_GUN -> weapon.isMatchAmmo((BasePartsItem.Magazine) PomkotsMechs.MACHINE_GUN_MAGAZINE.get())
                    ? leveledStack(PomkotsMechs.MACHINE_GUN_MAGAZINE.get(), 32)
                    : leveledStack(PomkotsMechs.GATLING_MAGAZINE.get(), 32);
            case MISSILE -> weapon instanceof DodoItem
                    ? leveledStack(PomkotsMechs.MISSILE_LARGE_MAGAZINE.get(), 2)
                    : leveledStack(PomkotsMechs.MISSILE_MAGAZINE.get(), 12);
            case GRENADE -> leveledStack(PomkotsMechs.GRENADE_MAGAZINE.get(), 12);
            default -> ItemStack.EMPTY;
        };
        if (!magazine.isEmpty()) mech.setItem(magazineSlot, magazine);
    }

    private static ItemStack leveledStack(Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (item instanceof BasePartsItem parts) parts.setLevel(stack, 1);
        return stack;
    }

    private static void setRotation(Entity entity, float yaw) {
        entity.setYRot(yaw);
        entity.yRotO = yaw;
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);
    }

    public record Configuration(
            ItemStack[] mechEquipment,
            int textureColor,
            boolean supplyConsumables,
            ItemStack license,
            ItemStack role,
            String model,
            String texture,
            UUID wingmanMasterId,
            double combatActionRange,
            ItemStack[] additionalCircuits
    ) {
    }

    public record Result(MechPilotEntity pilot, Pmvc01Entity mech) {
    }
}
