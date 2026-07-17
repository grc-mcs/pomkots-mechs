package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaBattleResultMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMatchResultData;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DodoItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaInstance;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class ArenaUtil {

    public static Entity findEntity(ServerLevel level, UUID uuid) {
        return level.getEntity(uuid);
    }

    public static Entity findEntity(ServerLevel level, UUID uuid, ArenaFighterData.FighterType type) {
        if (uuid == null) {
            return null;
        }

        if (type == ArenaFighterData.FighterType.PLAYER) {
            return level.getPlayerByUUID(uuid);
        } else {
            return level.getEntity(uuid);
        }
    }

    public static boolean initPilotEntities(ArenaMatchContext context) {
        var match = context.match();

        var f1 = context.arena().getFighter(match.getChallengerId());
        if (f1.getType() == ArenaFighterData.FighterType.PLAYER) {
            var ent = findEntity(context.getLevel(), f1.getFighterId(), ArenaFighterData.FighterType.PLAYER);
            if (ent != null) {
                match.setChallengerEntityId(ent.getUUID());
            } else {
                return false;
            }
        } else {
            var gatePos = match.getGatePosA();
            var yaw = getBlockYaw(gatePos, context.getLevel());

            var npc = setupNpc(f1, gatePos, context.getLevel(), yaw);

            match.setChallengerEntityId(npc.getUUID());

            var mech = setupNpcMech(f1, gatePos, context.getLevel(), yaw);

            npc.startRiding(mech);
        }

        var f2 = context.arena().getFighter(match.getOpponentId());
        if (f2.getType() == ArenaFighterData.FighterType.PLAYER) {
            var ent = findEntity(context.getLevel(), f2.getFighterId(), ArenaFighterData.FighterType.PLAYER);
            if (ent != null) {
                match.setOpponentEntityId(ent.getUUID());
            } else {
                return false;
            }
        } else {
            var gatePos = match.getGatePosB();
            var yaw = getBlockYaw(gatePos, context.getLevel());

            var npc = setupNpc(f2, gatePos.above(1), context.getLevel(), yaw);
            ArenaUtil.rotateEntity(npc, yaw);

            match.setOpponentEntityId(npc.getUUID());

            var mech = setupNpcMech(f2, gatePos.above(1), context.getLevel(), yaw);
            ArenaUtil.rotateEntity(mech, yaw);

            npc.startRiding(mech);
        }

        ArenaManager.update(context.server(), context.arena().getArenaId());

        return true;
    }

    public static void supplyMechConsumables(Pmvc01Entity mech) {
        supplyConsumables(mech);
        mech.setHealth(mech.getMaxHealth());
        mech.setChanged();
    }

    private static float getBlockYaw(BlockPos pos, ServerLevel level) {
        var state = level.getBlockState(pos);
        Direction facing = state.getValue(ArenaGateBlock.FACING);

        return switch (facing) {
            case SOUTH -> 0F;
            case WEST  -> 90F;
            case NORTH -> 180F;
            case EAST  -> -90F;
            default    -> 0F;
        };
    }

    private static void rotateEntity(Entity ent, float yaw) {
        ent.setYRot(yaw);
        ent.yRotO = yaw;
        ent.setYHeadRot(yaw);
        ent.setYBodyRot(yaw);
    }

    private static MechPilotEntity setupNpc(ArenaFighterData fighter, BlockPos pos, ServerLevel level, float yaw) {
        MechPilotEntity npc = new MechPilotEntity(PomkotsMechs.MECH_PILOT.get(), level);

        npc.setModelLocation(fighter.getModel().toString());
        npc.setTextureLocation(fighter.getTexture().toString());

        var licenseItem = getLicenseItem(fighter.getAiLevel());
        var roleItem = new ItemStack(PomkotsMechs.PILOT_ROLE_GLADIATOR_ITEM.get());

        npc.setItemInHand(InteractionHand.MAIN_HAND, licenseItem);
        npc.setItemInHand(InteractionHand.OFF_HAND, roleItem);
        npc.setShouldSave(false);

        npc.moveTo(pos, 0, 0);

        level.addFreshEntity(npc);

        return npc;
    }

    private static ItemStack getLicenseItem(String aiLevel) {
        if ("novice".equals(aiLevel)) {
            return new ItemStack(PomkotsMechs.PILOT_LICENSE_NOVICE_ITEM.get());
        } else if ("intermediate".equals(aiLevel)) {
            return new ItemStack(PomkotsMechs.PILOT_LICENSE_INTERMEDIATE_ITEM.get());
        } else if ("advanced".equals(aiLevel)) {
            return new ItemStack(PomkotsMechs.PILOT_LICENSE_ADVANCED_ITEM.get());
        } else {
            return new ItemStack(PomkotsMechs.PILOT_LICENSE_LEGEND_ITEM.get());
        }
    }

    private static Pmvc01Entity setupNpcMech(ArenaFighterData fighter, BlockPos pos, ServerLevel level, float yaw) {
        var mech = new Pmvc01Entity(PomkotsMechs.PMVC01.get(), level);
        var mechDef = fighter.getMechData();

        level.addFreshEntity(mech);

        mech.setItem(Pmvc01Entity.INV_PARTS_HEAD, mechDef.getParts().get(0));
        mech.setItem(Pmvc01Entity.INV_PARTS_BODY, mechDef.getParts().get(1));
        mech.setItem(Pmvc01Entity.INV_PARTS_ARMS, mechDef.getParts().get(2));
        mech.setItem(Pmvc01Entity.INV_PARTS_LEGS, mechDef.getParts().get(3));
        mech.setItem(Pmvc01Entity.INV_PARTS_GENERATOR, mechDef.getParts().get(4));
        mech.setItem(Pmvc01Entity.INV_PARTS_BOOSTER, mechDef.getParts().get(5));
        mech.setItem(Pmvc01Entity.INV_WEAPON_RIGHT_HAND, mechDef.getParts().get(6));
        mech.setItem(Pmvc01Entity.INV_WEAPON_LEFT_HAND, mechDef.getParts().get(7));
        mech.setItem(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, mechDef.getParts().get(8));
        mech.setItem(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, mechDef.getParts().get(9));
        mech.setItem(Pmvc01Entity.INV_WEAPON_EXT1, mechDef.getParts().get(10));
        mech.setItem(Pmvc01Entity.INV_WEAPON_EXT2, mechDef.getParts().get(11));
        mech.setTextureColor(mechDef.getTextureColor());

        supplyConsumables(mech);

        mech.setChanged();
        mech.setShouldSave(false);

        mech.setHealth(mech.getMaxHealth());

        mech.moveTo(pos, 0, 0);

        return mech;
    }

    private static void supplyConsumables(Pmvc01Entity mech) {
        supplyWeaponMagazines(mech, Pmvc01Entity.INV_WEAPON_RIGHT_HAND, Pmvc01Entity.INV_AMMO_RA);
        supplyWeaponMagazines(mech, Pmvc01Entity.INV_WEAPON_LEFT_HAND, Pmvc01Entity.INV_AMMO_LA);
        supplyWeaponMagazines(mech, Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER, Pmvc01Entity.INV_AMMO_RS);
        supplyWeaponMagazines(mech, Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER, Pmvc01Entity.INV_AMMO_LS);

        mech.setItem(Pmvc01Entity.INV_FUEL, getItemStack((BasePartsItem)PomkotsMechs.PELLET.get(), 64));
    }

    private static void supplyWeaponMagazines(Pmvc01Entity mech, int weaponSlot, int magazineSlot) {
        var itemStack = mech.getItem(weaponSlot);
        if (itemStack.getItem() instanceof BasePartsItem.Weapon weapon) {
            if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.RIFLE) {
                mech.setItem(magazineSlot, getItemStack((BasePartsItem) PomkotsMechs.RIFLE_MAGAZINE.get(), 32));
            } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.SHOT_GUN) {
                mech.setItem(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.SHOTGUN_MAGAZINE.get(), 32));
            } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.MACHINE_GUN) {
                if (weapon.isMatchAmmo((BasePartsItem.Magazine) PomkotsMechs.MACHINE_GUN_MAGAZINE.get())) {
                    mech.setItem(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.MACHINE_GUN_MAGAZINE.get(), 32));
                } else {
                    mech.setItem(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.GATLING_MAGAZINE.get(), 32));
                }
            } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.MISSILE) {
                if (weapon instanceof DodoItem) {
                    mech.setItem(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.MISSILE_LARGE_MAGAZINE.get(), 2));
                } else {
                    mech.setItem(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.MISSILE_MAGAZINE.get(), 12));
                }
            } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.GRENADE) {
                mech.setItem(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.GRENADE_MAGAZINE.get(), 12));
            }
        }
    }

    private static ItemStack getItemStack(BasePartsItem item, int num) {
        var stack = new ItemStack(item, num);
        item.setLevel(stack, 1);

        return stack;
    }

    public static boolean isNear(
            Entity entity,
            BlockPos center,
            double range
    ) {

        AABB area = new AABB(
                center.getCenter(),
                center.getCenter()
        ).inflate(
                range
        );

        System.out.println(center + ":" + entity + ":" + entity.getVehicle() + ":" +  area.contains(
                entity.position()
        ));

        return area.contains(
                entity.position()
        );
    }


    public static void setupCameras(ArenaMatchContext context) {
        setupCamera(
                context,
                context.getChallengerEntity(),
                context.getOpponentEntity()
        );

        setupCamera(
                context,
                context.getOpponentEntity(),
                context.getChallengerEntity()
        );
    }

    public static void setupCamera(
            ArenaMatchContext context,
            Entity self,
            Entity target
    ) {
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        ArenaCameraEntity camera =
                new ArenaCameraEntity(
                        PomkotsMechs.ARENA_CAMERA.get(),
                        player.level()
                );

        camera.initialize(
                player,
                target
        );

        player.level().addFreshEntity(
                camera
        );

        sendCameraPacket(PomkotsMechs.PACKET_ARENA_OPENING_START, player);

        player.connection.send(
                new ClientboundSetCameraPacket(
                        camera
                )
        );

        context.runtime().put(
                "camera_" + player.getUUID(),
                camera
        );
    }

    public static void playSounds(SoundEvent se, ArenaMatchContext context) {
        if (context.getChallengerEntity() instanceof ServerPlayer sp1) {
            sp1.playNotifySound(se, SoundSource.MASTER, 1F, 0.7F);
        }
        if (context.getOpponentMechEntity() instanceof ServerPlayer sp2) {
            sp2.playNotifySound(se, SoundSource.MASTER, 1F, 0.7F);
        }
    }

    public static void restoreCameras(ArenaMatchContext context) {
        restoreCamera(
                context,
                context.getChallengerEntity()
        );

        restoreCamera(
                context,
                context.getOpponentEntity()
        );
    }

    public static void restoreCamera(
            ArenaMatchContext context,
            Entity self
    ) {
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        player.connection.send(
                new ClientboundSetCameraPacket(
                        player
                )
        );

        sendCameraPacket(PomkotsMechs.PACKET_ARENA_OPENING_END, player);

        context.runtime().remove(
                "camera_" + player.getUUID()
        );
    }

    public static void startScreenFade(ArenaMatchContext context, int in, int keep, int out) {
        var p1 = context.getChallengerEntity();
        var p2 = context.getOpponentEntity();

        if (p1 instanceof ServerPlayer sp1) {
            sendScreenFadePacket(sp1, in, keep, out);
        }

        if (p2 instanceof ServerPlayer sp2) {
            sendScreenFadePacket(sp2, in, keep, out);
        }
    }

    public static void sendScreenFadePacket(ServerPlayer player, int in, int keep, int out) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(in);
        buf.writeInt(keep);
        buf.writeInt(out);

        NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_GENERAR_SCREEN_FADE), buf);
    }

    private static void sendCameraPacket(String packet, ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        NetworkManager.sendToPlayer(player, PomkotsMechs.id(packet), buf);
    }

    public static ArenaFighterData getChallengerData(ArenaMatchContext context) {
        return getFighterData(context.match().getChallengerId(), context);
    }

    public static ArenaFighterData getOpponentData(ArenaMatchContext context) {
        return getFighterData(context.match().getOpponentId(), context);
    }

    public static ArenaFighterData getFighterData(UUID fighterUUID, ArenaMatchContext context) {
        return context.arena().getFighter(fighterUUID);
    }

    public static void openResultScreen(ServerPlayer serverPlayer, ArenaMatchResultData.Entry data) {
        MenuRegistry.openExtendedMenu(
                serverPlayer,

                new SimpleMenuProvider(
                        (id, inv, player) ->
                            new ArenaBattleResultMenu(
                                    id,
                                    inv,
                                    serverPlayer,
                                    data),
                        Component.literal(
                                "Arena Result"
                        )
                ),

                buf -> {
                    ArenaMatchResultData.write(
                            buf,
                            data
                    );

                }
        );
    }

    // そもそもアリーナ周りのentityは保存しないようにしたのでいらなくなった説。
    public static void finalizeMatch(ArenaMatchData match, ArenaInstance arena, ServerLevel level) {
        finalizeGate(match.getGatePosA(), match, arena, level);
        finalizeGate(match.getGatePosB(), match, arena, level);

        finalizeFighter(match.getChallengerId(), match.getChallengerEntityId(), match.getChallengerMechId(), match, arena, level);
        finalizeFighter(match.getOpponentId(), match.getOpponentEntityId(), match.getOpponentMechId(), match, arena, level);
    }

    private static void finalizeFighter(UUID fighterId, UUID fighterEntityId, UUID mechEntityId, ArenaMatchData match, ArenaInstance arena, ServerLevel level) {
        if (fighterId == null) {
            return;
        }

        var fighterInfo = arena.getFighter(fighterId);

        if (fighterInfo == null) {
            return;
        }

        if (fighterInfo.getType() == ArenaFighterData.FighterType.NPC) {
            if (mechEntityId != null) {
                var ent = level.getEntity(mechEntityId);
                if (ent instanceof Pmvc01Entity) {
                    ent.ejectPassengers();
                    ent.discard();
                }
            }

            if (fighterEntityId != null) {
                var ent = level.getEntity(fighterEntityId);
                if (ent instanceof MechPilotEntity) {
                    ent.discard();
                }
            }
        }
    }

    private static void finalizeGate(BlockPos gatePos, ArenaMatchData match, ArenaInstance arena, ServerLevel level) {
        if (gatePos != null) {
            if (level.getBlockEntity(gatePos) instanceof ArenaGateBlockEntity ent) {
                ent.refreshElevator();
            }
        }
    }
}
