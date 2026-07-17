package grcmcs.minecraft.mods.pomkotsmechs;

import com.mojang.serialization.Codec;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.block.*;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.*;
import grcmcs.minecraft.mods.pomkotsmechs.block.migration.AssetAnchorBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.migration.AssetAnchorBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaBattleResultMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadKeyCardMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.AttachedMuzzleFlashOptions;
import grcmcs.minecraft.mods.pomkotsmechs.command.PomkotsCommands;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.BlockPlacementPreviewEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.PlacementPreviewEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxLegsEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt04Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.ArenaReceptionistEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.*;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret.Pmvt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitStackFactory;
import grcmcs.minecraft.mods.pomkotsmechs.items.coin.PomCoinGoldItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.coin.PomCoinItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.coin.PomCoinSilverItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.*;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.fuel.PelletItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters.HanedaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters.KansaiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters.NaritaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.ChibaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.SagaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.ShigaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotConfiguratorItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotLicenseItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.EncryptedPackResources;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.migration.AssetMigrationManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ScanPulseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import grcmcs.minecraft.mods.pomkotsmechs.survival.SurvivalInitActions;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

public class PomkotsMechs {
	public static final String MODID = "pomkotsmechs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final String SCOREBOARD_NAME_FOR_PROGRESS = "pomkots.mp";

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	public static String nbtName(String name) {
		return MODID + name;
	}

	// ENTITIES -------------------------------------------------------------------------------------------

	public static  final  DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MODID, Registries.ENTITY_TYPE);

	// Vehicles
	public static final RegistrySupplier<EntityType<Pmv01Entity>> PMV01 = registerEntityType("pmv01", Pmv01Entity::new, MobCategory.CREATURE, 3F, 4F);
	public static final RegistrySupplier<EntityType<Pmv01bEntity>> PMV01B = registerEntityType("pmv01b", Pmv01bEntity::new, MobCategory.CREATURE, 3F, 4F);
	public static final RegistrySupplier<EntityType<Pmv02Entity>> PMV02 = registerEntityType("pmv02", Pmv02Entity::new, MobCategory.CREATURE, 3F, 4F);
	public static final RegistrySupplier<EntityType<Pmv03pEntity>> PMV03P = registerEntityType("pmv03p", Pmv03pEntity::new, MobCategory.CREATURE, 3F, 4F);
	public static final RegistrySupplier<EntityType<Pmv03Entity>> PMV03 = registerEntityType("pmv03", Pmv03Entity::new, MobCategory.CREATURE, 4F, 10F);

	public static final RegistrySupplier<EntityType<Pmvc01Entity>> PMVC01 = registerEntityTypeLongTracking("pmvc01", Pmvc01Entity::new, MobCategory.CREATURE, 4F, 5.5F);

	public static final RegistrySupplier<EntityType<Pmvt01Entity>> PMVT01 = registerEntityType("pmvt01", Pmvt01Entity::new, MobCategory.CREATURE, 3F, 2F);

	// Monster, Boss
	public static final RegistrySupplier<EntityType<Pmss01Entity>> PMSS01 = registerEntityType("pmss01", Pmss01Entity::new, MobCategory.MONSTER, 0.9F, 2F); // Charging Mob
	public static final RegistrySupplier<EntityType<Pmss02Entity>> PMSS02 = registerEntityType("pmss02", Pmss02Entity::new, MobCategory.MONSTER, 0.9F, 2F); // Charging Mob
	public static final RegistrySupplier<EntityType<Pmss03Entity>> PMSS03 = registerEntityType("pmss03", Pmss03Entity::new, MobCategory.MONSTER, 0.9F, 2F); // Charging Mob

	public static final RegistrySupplier<EntityType<Pms01Entity>> PMS01 = registerEntityType("pms01", Pms01Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Charging Mob
	public static final RegistrySupplier<EntityType<Pms02Entity>> PMS02 = registerEntityType("pms02", Pms02Entity::new, MobCategory.MONSTER, 3F, 3F); // Flying Mob
	public static final RegistrySupplier<EntityType<Pms03Entity>> PMS03 = registerEntityType("pms03", Pms03Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Gun Mob
	public static final RegistrySupplier<EntityType<Pms04Entity>> PMS04 = registerEntityType("pms04", Pms04Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Missile
	public static final RegistrySupplier<EntityType<Pms05Entity>> PMS05 = registerEntityType("pms05", Pms05Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Spider
	public static final RegistrySupplier<EntityType<Pms06Entity>> PMS06 = registerEntityType("pms06", Pms06Entity::new, MobCategory.MONSTER, 5F, 5F); // Spider
	public static final RegistrySupplier<EntityType<Pms07Entity>> PMS07 = registerEntityType("pms07", Pms07Entity::new, MobCategory.MONSTER, 5F, 5F); // Spider
	public static final RegistrySupplier<EntityType<Pms08Entity>> PMS08 = registerEntityType("pms08", Pms08Entity::new, MobCategory.MONSTER, 5F, 5F); // Spider
	public static final RegistrySupplier<EntityType<Pms09Entity>> PMS09 = registerEntityType("pms09", Pms09Entity::new, MobCategory.MONSTER, 3F, 3F); // Spider
	public static final RegistrySupplier<EntityType<Pms10Entity>> PMS10 = registerEntityType("pms10", Pms10Entity::new, MobCategory.MONSTER, 3F, 3F); // Spider

	// Carriers
	public static final RegistrySupplier<EntityType<Pmc01Entity>> PMC01 = registerEntityType("pmc01", Pmc01Entity::new, MobCategory.MONSTER, 3F, 15F); // Spider
	public static final RegistrySupplier<EntityType<Pmc02Entity>> PMC02 = registerEntityType("pmc02", Pmc02Entity::new, MobCategory.MONSTER, 7F, 7F); // Spider

	// Boss
	public static final RegistrySupplier<EntityType<Pmb01Entity>> PMB01 = registerEntityType("pmb01", Pmb01Entity::new, MobCategory.MONSTER, 0.7F, 19F);
	public static final RegistrySupplier<EntityType<Pmb01mk2Entity>> PMB01MK2 = registerEntityType("pmb01mk2", Pmb01mk2Entity::new, MobCategory.MONSTER, 3F, 16F);

	public static final RegistrySupplier<EntityType<Pmb02Entity>> PMB02 = registerEntityType("pmb02", Pmb02Entity::new, MobCategory.MONSTER, 3F, 7F);
	public static final RegistrySupplier<EntityType<Pmb03Entity>> PMB03 = registerEntityType("pmb03", Pmb03Entity::new, MobCategory.MONSTER, 3F, 16F);
	public static final RegistrySupplier<EntityType<Pmb04Entity>> PMB04 = registerEntityType("pmb04", Pmb04Entity::new, MobCategory.MONSTER, 3F, 16F);
	public static final RegistrySupplier<EntityType<Pmb05Entity>> PMB05 = registerEntityType("pmb05", Pmb05Entity::new, MobCategory.MONSTER, 3F, 24F);
	public static final RegistrySupplier<EntityType<Pmb06Entity>> PMB06 = registerEntityType("pmb06", Pmb06Entity::new, MobCategory.MONSTER, 3F, 24F);
	public static final RegistrySupplier<EntityType<Pmb07Entity>> PMB07 = registerEntityType("pmb07", Pmb07Entity::new, MobCategory.MONSTER, 3F, 16F);
	public static final RegistrySupplier<EntityType<Pmb08Entity>> PMB08 = registerEntityType("pmb08", Pmb08Entity::new, MobCategory.MONSTER, 3F, 8F);

	public static final RegistrySupplier<EntityType<Pmb99Entity>> PMB99 = registerEntityType("pmb99", Pmb99Entity::new, MobCategory.MONSTER, 3F, 16F);

	public static final RegistrySupplier<EntityType<EarthbreakEntity>> EARTHBREAK2 = registerEntityType("earthbreak2", EarthbreakEntity::new, MobCategory.MISC, 60F, 0.1F);

	// Turrets
	public static final RegistrySupplier<EntityType<Pmt01Entity>> PMT01 = registerEntityType("pmt01", Pmt01Entity::new, MobCategory.MONSTER, 3F, 3F);
	public static final RegistrySupplier<EntityType<Pmt02Entity>> PMT02 = registerEntityType("pmt02", Pmt02Entity::new, MobCategory.MONSTER, 3F, 3F);
	public static final RegistrySupplier<EntityType<Pmt03Entity>> PMT03 = registerEntityType("pmt03", Pmt03Entity::new, MobCategory.MONSTER, 3F, 3F);
	public static final RegistrySupplier<EntityType<Pmt04Entity>> PMT04 = registerEntityType("pmt04", Pmt04Entity::new, MobCategory.MONSTER, 6F, 1F);

	// Misc
	public static final RegistrySupplier<EntityType<MechTraderEntity>> MECH_TRADER = registerEntityType("mech_trader", MechTraderEntity::new, MobCategory.CREATURE, 6F, 3F);
	public static final RegistrySupplier<EntityType<MechPilotEntity>> MECH_PILOT = registerEntityTypeLongTracking("mech_pilot", MechPilotEntity::new, MobCategory.CREATURE, 0.5F, 2F);
	public static final RegistrySupplier<EntityType<ArenaReceptionistEntity>> ARENA_RECEP = registerEntityType("arena_receptionist", ArenaReceptionistEntity::new, MobCategory.CREATURE, 0.5F, 2F);

	// Projectile
	public static final RegistrySupplier<EntityType<BulletEntity>> BULLET = registerEntityType("bullet", BulletEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletMiddleEntity>> BULLETMIDDLE = registerEntityType("bulletmiddle", BulletMiddleEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<GrenadeEntity>> GRENADE = registerEntityType("grenade", GrenadeEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<GrenadeLargeEntity>> GRENADELARGE = registerEntityType("grenadelarge", GrenadeLargeEntity::new, MobCategory.MISC, 2F, 2F);

	public static final RegistrySupplier<EntityType<MissileGenericEntity>> MISSILE_GENERIC = registerEntityType("missilegeneric", MissileGenericEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<MissileGenericLargeEntity>> MISSILE_GENERIC_LARGE = registerEntityType("missilegenericlarge", MissileGenericLargeEntity::new, MobCategory.MISC, 2F, 2F);

	public static final RegistrySupplier<EntityType<MissileVerticalEntity>> MISSILE_VERTICAL = registerEntityType("missilevertical", MissileVerticalEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<MissileHorizontalEntity>> MISSILE_HORIZONTAL = registerEntityType("missilehorizontal", MissileHorizontalEntity::new, MobCategory.MISC, 1F, 1F);

	public static final RegistrySupplier<EntityType<MissileEnemyEntity>> MISSILE_ENEMY = registerEntityType("missileenemy", MissileEnemyEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<MissileEnemyLargeEntity>> MISSILE_ENEMY_LARGE = registerEntityType("missileenemylarge", MissileEnemyLargeEntity::new, MobCategory.MISC, 2F, 2F);

	public static final RegistrySupplier<EntityType<MissilePodEntity>>  MISSILE_POD = registerEntityType("missilepod", MissilePodEntity::new, MobCategory.MISC, 2F, 2F);

	public static final RegistrySupplier<EntityType<BlockProjectileEntity>> BLOCK_PROJECTILE = registerEntityType("blockprojectile", BlockProjectileEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BlockMassEntity>> BLOCK_MASS = registerEntityType("blockmass", BlockMassEntity::new, MobCategory.MISC, 4F, 4F);
	public static final RegistrySupplier<EntityType<PresentBoxEntity>> PRESENT_BOX = registerEntityType("present", PresentBoxEntity::new, MobCategory.MISC, 4F, 4F);

	public static final RegistrySupplier<EntityType<RockSmallEntity>> ROCK_SMALL = registerEntityType("rocksmall", RockSmallEntity::new, MobCategory.MISC, 3F, 3F);
	public static final RegistrySupplier<EntityType<RockLargeEntity>> ROCK_LARGE = registerEntityType("rocklarge", RockLargeEntity::new, MobCategory.MISC, 15F, 15F);
	public static final RegistrySupplier<EntityType<NeedleEntity>> NEEDLE = registerEntityType("needle", NeedleEntity::new, MobCategory.MISC, 3F, 1F);
	public static final RegistrySupplier<EntityType<MineEntity>> MINE = registerEntityType("mine", MineEntity::new, MobCategory.MISC, 10F, 5F);

	// Projectile for custom
	public static final RegistrySupplier<EntityType<BulletRifleEntity>> BULLET_RIFLE = registerEntityType("bulletrifle", BulletRifleEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletMachineEntity>> BULLET_MACHINE = registerEntityType("bulletmachine", BulletMachineEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletMachineEntity>> BULLET_MACHINE_LARGE = registerEntityType("bulletmachinelarge", BulletMachineEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletGrenadeEntity>> BULLET_GRENADE = registerEntityType("bulletgrenade", BulletGrenadeEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletGrenadeLargeEntity>> BULLET_GRENADE_LARGE = registerEntityType("bulletgrenadelarge", BulletGrenadeLargeEntity::new, MobCategory.MISC, 6F, 6F);

	public static final RegistrySupplier<EntityType<BulletBeamEntity>> BULLET_BEAM = registerEntityType("bulletbeam", BulletBeamEntity::new, MobCategory.MISC, 2F, 2F);

	// Other
	public static final RegistrySupplier<EntityType<PlayerDummyEntity>> PLAYERDUMMY = registerEntityType("playerdummy", PlayerDummyEntity::new, MobCategory.CREATURE, 1F, 2F);

	public static final RegistrySupplier<EntityType<ExplosionEntity>> EXPLOSION = registerEntityType("explosion", ExplosionEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<EarthbreakEntity>> EARTHBREAK = registerEntityType("earthbreak", EarthbreakEntity::new, MobCategory.MISC, 60F, 4F);
	public static final RegistrySupplier<EntityType<EarthraiseEntity>> EARTHRAISE = registerEntityType("earthraise", EarthraiseEntity::new, MobCategory.MISC, 10F, 8F);
	public static final RegistrySupplier<EntityType<SlashEntity>> EXPLOADSLASH = registerEntityType("exploadslash", SlashEntity::new, MobCategory.MISC, 4F, 5F);
	public static final RegistrySupplier<EntityType<WaveHorizontalEntity>> WAVE_HOR = registerEntityType("wave_h", WaveHorizontalEntity::new, MobCategory.MISC, 24F, 3F);

	public static final RegistrySupplier<EntityType<ElevatorEntity>> ELEVATOR = registerEntityTypeLongTracking("elevator", ElevatorEntity::new, MobCategory.MISC, 6.5F, 0.5F);
	public static final RegistrySupplier<EntityType<ArenaCameraEntity>> ARENA_CAMERA = registerEntityTypeLongTracking("arena_camera", ArenaCameraEntity::new, MobCategory.MISC, 0.1F, 0.1F);

	public static final RegistrySupplier<EntityType<HitBoxEntity>> HITBOX1 = registerEntityType("hitbox1", HitBoxEntity::new, MobCategory.MISC, 10F, 8F);
	public static final RegistrySupplier<EntityType<HitBoxLegsEntity>> HITBOX2 = registerEntityType("hitbox2", HitBoxLegsEntity::new, MobCategory.MISC, 10F, 9F);

	public static final RegistrySupplier<EntityType<BossHitBoxEntity>> HITBOX_PMB02 = registerEntityType("hitbox_pmb02", BossHitBoxEntity::new, MobCategory.MISC, 8F, 8F);

	public static final RegistrySupplier<EntityType<BossHitBoxEntity>> HITBOX_PMB03 = registerEntityType("hitbox_pmb03", BossHitBoxEntity::new, MobCategory.MISC, 12F, 16F);
	public static final RegistrySupplier<EntityType<BossHitBoxEntity>> HITBOX_PMB06 = registerEntityType("hitbox_pmb06", BossHitBoxEntity::new, MobCategory.MISC, 24F, 32F);

	public static final RegistrySupplier<EntityType<BossHitBoxEntity>> HITBOX_PMB08 = registerEntityType("hitbox_pmb08", BossHitBoxEntity::new, MobCategory.MISC, 16F, 12F);


//	public static final RegistrySupplier<EntityType<RaidControllerEntity>> RAID_CONTROLLER = registerEntityType("raid_controller", RaidControllerEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<RaidControllerEntity>> RAID_CONTROLLER =
			ENTITIES.register("raid_controller", () ->
					EntityType.Builder.<RaidControllerEntity>of(
									RaidControllerEntity::new,
									MobCategory.MISC
							)
							.sized(1f, 1f)
							.clientTrackingRange(100)
							.build(id("raid_controller").toString())
			);


	public static final RegistrySupplier<EntityType<RaidObjectiveEntity>> RAID_OBJECTIVE = registerEntityType("raid_objective", RaidObjectiveEntity::new, MobCategory.MISC, 12F, 16F);

	public static final RegistrySupplier<EntityType<KujiraEntity>> KUJIRA = registerEntityType("kujira", KujiraEntity::new, MobCategory.MISC, 30F, 30F);
	public static final RegistrySupplier<EntityType<PlateEntity>> PLATE = registerEntityType("plate", PlateEntity::new, MobCategory.MISC, 1F, 10F);

	public static final RegistrySupplier<EntityType<AlertEntity>> ALERT = registerEntityType("alert", AlertEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<AlertRedEntity>> ALERTRED = registerEntityType("alertred", AlertRedEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<BossBoxEntity>> BOSSBOX = registerEntityType("bossbox", BossBoxEntity::new, MobCategory.MISC, 1F, 1F);

	public static final RegistrySupplier<EntityType<ScanPulseEntity>> SCAN_PULSE = ENTITIES.register(
			"scan_pulse",
			() -> EntityType.Builder
					.<ScanPulseEntity>of(
							ScanPulseEntity::new,
							MobCategory.MISC
					)
					.sized(0.1F, 0.1F)
					.clientTrackingRange(20)
					.updateInterval(1)
					.build("pomkotsmechs:scan_pulse")
	);

	public static final RegistrySupplier<EntityType<BlockPlacementPreviewEntity>> BLOCK_PLACEMENT_PREVIEW =
			ENTITIES.register("block_placement_preview", () ->
					EntityType.Builder.<BlockPlacementPreviewEntity>of(
									BlockPlacementPreviewEntity::new,
									MobCategory.MISC
							)
							.sized(1f, 1f)
							.build(id("block_placement_preview").toString())
			);

	public static final RegistrySupplier<EntityType<PlacementPreviewEntity>> PLACEMENT_PREVIEW =
			ENTITIES.register("placement_preview", () ->
					EntityType.Builder.<PlacementPreviewEntity>of(
									PlacementPreviewEntity::new,
									MobCategory.MISC
							)
							.sized(1f, 1f)   // 実体なし
							.clientTrackingRange(1)
							.updateInterval(Integer.MAX_VALUE)
							.build(id("placement_preview").toString())
	);

	public static final RegistrySupplier<EntityType<MechCapsuleProjectileEntity>> MECH_CAPSULE_PROJECTILE =
			ENTITIES.register(
					"mech_capsule_projectile",
					() -> EntityType.Builder
							.<MechCapsuleProjectileEntity>of(MechCapsuleProjectileEntity::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(4)
							.updateInterval(10)
							.build("mech_capsule_projectile")
			);

	private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
		return ENTITIES.register(name, () ->
				EntityType.Builder.of(factory, category)
						.sized(width, height)
						.build(id(name).toString()));
	}

	private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityTypeLongTracking(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
		return ENTITIES.register(name, () ->
				EntityType.Builder.of(factory, category)
						.sized(width, height)
						.clientTrackingRange(30)
						.build(id(name).toString()));
	}

	private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityTypeTurret(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
		return ENTITIES.register(name, () ->
				EntityType.Builder.of(factory, category)
						.sized(width, height)
						.clientTrackingRange(16)
						.build(id(name).toString()));
	}

	// Blocks -------------------------------------------------------------------------------------------

	public static final DeferredRegister<Block> BLOCKS =  DeferredRegister.create(MODID, Registries.BLOCK);

	public static final RegistrySupplier<Block> MECH_WORKBENCH_BLOCK = BLOCKS.register("mechworkbench", ()-> new MechWorkbenchBlock());
	public static final RegistrySupplier<Block> PARTS_WORKBENCH_BLOCK = BLOCKS.register("partsworkbench", ()-> new PartsWorkbenchBlock());
	public static final RegistrySupplier<Block> MECH_SALVAGER_BLOCK = BLOCKS.register("mechsalvager", ()-> new MechSalvagerBlock());

	public static final RegistrySupplier<Block> CORE_STONE_BLOCK = BLOCKS.register("corestone", ()-> new CoreStoneBlock());

	public static final RegistrySupplier<Block> POMKOTS_CUBE_BLOCK = BLOCKS.register("pomkotscube", ()-> new PomkotsCubeBlock());
	public static final RegistrySupplier<Block> POMKOTS_CUBE_BLOCK_YELLOW = BLOCKS.register("pomkotscube_yellow", ()-> new PomkotsCubeBlockYellow());
	public static final RegistrySupplier<Block> POMKOTS_CUBE_BLOCK_RED = BLOCKS.register("pomkotscube_red", ()-> new PomkotsCubeBlockRed());
	public static final RegistrySupplier<Block> POMKOTS_CUBE_BLOCK_PURPLE = BLOCKS.register("pomkotscube_purple", ()-> new PomkotsCubeBlockPurple());

	public static final RegistrySupplier<Block> POMKOTS_LEVER_BLOCK = BLOCKS.register("pomkotslever", ()-> new PomkotsLeverBlock());


	public static final RegistrySupplier<Block> EXCHANGE_BLOCK = BLOCKS.register("exchange", ()-> new ExchangeBlock());
	public static final RegistrySupplier<Block> CASK_BLOCK = BLOCKS.register("cask", ()-> new CaskBlock());

	public static final RegistrySupplier<Block> STRUCTURE_SPAWNER_BLOCK = BLOCKS.register("structure_spawner", ()-> new StructureSpawnerBlock());
	public static final RegistrySupplier<Block> ENTITY_SPAWNER_BLOCK = BLOCKS.register("entity_spawner", ()-> new EntitySpawnerBlock());
	public static final RegistrySupplier<Block> COMMAND_EXECUTOR_BLOCK = BLOCKS.register("command_executor", ()-> new CommandExecutorBlock());
	public static final RegistrySupplier<Block> PLACE_HOLDER_BLOCK = BLOCKS.register("place_holder", ()-> new PlaceHolderBlock());
	public static final RegistrySupplier<Block> CUSTOM_SPAWNER_BLOCK = BLOCKS.register("custom_spawner", ()-> new CustomSpawnerBlock());

	public static final RegistrySupplier<Block> ARENA_CONTROLLER_BLOCK = BLOCKS.register("arena_controller", ()-> new ArenaControllerBlock());
	public static final RegistrySupplier<Block> ARENA_GATE_BLOCK = BLOCKS.register("arena_gate", ()-> new ArenaGateBlock());
	public static final RegistrySupplier<Block> ARENA_BATTLEFIELD_ANCHOR_BLOCK = BLOCKS.register("arena_battlefield_anchor", ()-> new ArenaBattleFieldAnchorBlock());
	public static final RegistrySupplier<Block> ARENA_TELEPORT_BLOCK = BLOCKS.register("arena_teleport_block", ()-> new ArenaTeleportBlock());

	public static final RegistrySupplier<Block> ASSET_ANCHOR = BLOCKS.register("asset_anchor", ()-> new AssetAnchorBlock());

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =  DeferredRegister.create(MODID, Registries.BLOCK_ENTITY_TYPE);
	public static final RegistrySupplier<BlockEntityType<MechWorkbenchBlockEntity>> MECH_WORKBENCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("mechworkbenchentity", () -> BlockEntityType.Builder.of(MechWorkbenchBlockEntity::new, MECH_WORKBENCH_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<PartsWorkbenchBlockEntity>> PARTS_WORKBENCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("partsworkbenchentity", () -> BlockEntityType.Builder.of(PartsWorkbenchBlockEntity::new, PARTS_WORKBENCH_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<MechSalvagerBlockEntity>> MECH_SALVAGER_BLOCK_ENTITY = BLOCK_ENTITIES.register("mechsalvagerentity", () -> BlockEntityType.Builder.of(MechSalvagerBlockEntity::new, MECH_SALVAGER_BLOCK.get()).build(null));

	public static final RegistrySupplier<BlockEntityType<CoreStoneBlockEntity>> CORE_STONE_BLOCK_ENTITY = BLOCK_ENTITIES.register("corestoneblockentity", () -> BlockEntityType.Builder.of(CoreStoneBlockEntity::new, CORE_STONE_BLOCK.get()).build(null));

	public static final RegistrySupplier<BlockEntityType<PomkotsCubeBlockEntity>> POMKOTS_CUBE_BLOCK_ENTITY = BLOCK_ENTITIES.register("pomkotscubeentity", () -> BlockEntityType.Builder.of(PomkotsCubeBlockEntity::new, POMKOTS_CUBE_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<PomkotsCubeBlockYellowEntity>> POMKOTS_CUBE_BLOCK_ENTITY_YELLOW = BLOCK_ENTITIES.register("pomkotscubeentity_yellow", () -> BlockEntityType.Builder.of(PomkotsCubeBlockYellowEntity::new, POMKOTS_CUBE_BLOCK_YELLOW.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<PomkotsCubeBlockRedEntity>> POMKOTS_CUBE_BLOCK_ENTITY_RED = BLOCK_ENTITIES.register("pomkotscubeentity_red", () -> BlockEntityType.Builder.of(PomkotsCubeBlockRedEntity::new, POMKOTS_CUBE_BLOCK_RED.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<PomkotsCubeBlockPurpleEntity>> POMKOTS_CUBE_BLOCK_ENTITY_PURPLE = BLOCK_ENTITIES.register("pomkotscubeentity_purple", () -> BlockEntityType.Builder.of(PomkotsCubeBlockPurpleEntity::new, POMKOTS_CUBE_BLOCK_PURPLE.get()).build(null));

	public static final RegistrySupplier<BlockEntityType<PomkotsLeverBlockEntity>> POMKOTS_LEVER_BLOCK_ENTITY = BLOCK_ENTITIES.register("pomkotsleverentity", () -> BlockEntityType.Builder.of(PomkotsLeverBlockEntity::new, POMKOTS_LEVER_BLOCK.get()).build(null));

	public static final RegistrySupplier<BlockEntityType<StructureSpawnerBlockEntity>> STRUCTURE_SPAWNER_BLOCK_ENTITY = BLOCK_ENTITIES.register("structure_spawner_entity", () -> BlockEntityType.Builder.of(StructureSpawnerBlockEntity::new, STRUCTURE_SPAWNER_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<EntitySpawnerBlockEntity>> ENTITY_SPAWNER_BLOCK_ENTITY = BLOCK_ENTITIES.register("entity_spawner_entity", () -> BlockEntityType.Builder.of(EntitySpawnerBlockEntity::new, ENTITY_SPAWNER_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<CustomSpawnerBlockEntity>> CUSTOM_SPAWNER_BLOCK_ENTITY = BLOCK_ENTITIES.register("custom_spawner_entity", () -> BlockEntityType.Builder.of(CustomSpawnerBlockEntity::new, CUSTOM_SPAWNER_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<CommandExecutorBlockEntity>> ENTITY_COMMAND_EXECUTOR_BLOCK = BLOCK_ENTITIES.register("command_executor_entity", () -> BlockEntityType.Builder.of(CommandExecutorBlockEntity::new, COMMAND_EXECUTOR_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<PlaceHolderBlockEntity>> PLACE_HOLDER_BLOCK_ENTITY = BLOCK_ENTITIES.register("place_holder_block_entity", () -> BlockEntityType.Builder.of(PlaceHolderBlockEntity::new, PLACE_HOLDER_BLOCK.get()).build(null));

	public static final RegistrySupplier<BlockEntityType<ArenaControllerBlockEntity>> ARENA_CONTROLLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("arena_controller_entity", () -> BlockEntityType.Builder.of(ArenaControllerBlockEntity::new, ARENA_CONTROLLER_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<ArenaGateBlockEntity>> ARENA_GATE_BLOCK_ENTITY = BLOCK_ENTITIES.register("arena_gate_entity", () -> BlockEntityType.Builder.of(ArenaGateBlockEntity::new, ARENA_GATE_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<ArenaBattleFieldAnchorBlockEntity>> ARENA_BATTLEFIELD_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("arena_battlefield_anchor_entity", () -> BlockEntityType.Builder.of(ArenaBattleFieldAnchorBlockEntity::new, ARENA_BATTLEFIELD_ANCHOR_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<ArenaTeleportBlockEntity>> ARENA_TELEPORT_BLOCK_ENTITY = BLOCK_ENTITIES.register("arena_teleport_block_entity", () -> BlockEntityType.Builder.of(ArenaTeleportBlockEntity::new, ARENA_TELEPORT_BLOCK.get()).build(null));

	public static final RegistrySupplier<BlockEntityType<AssetAnchorBlockEntity>> ASSET_ANCHOR_BE = BLOCK_ENTITIES.register("asset_anchor_block_entity", () -> BlockEntityType.Builder.of(AssetAnchorBlockEntity::new, ASSET_ANCHOR.get()).build(null));


	// PARTICLES -------------------------------------------------------------------------------------------

	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(MODID, Registries.PARTICLE_TYPE);

	public static final RegistrySupplier<SimpleParticleType> FIRE = PARTICLES.register("fire", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MISSILE_SMOKE = PARTICLES.register("missilesmoke", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> EXPLOSION_CORE = PARTICLES.register("explosioncore", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> SPARK = PARTICLES.register("spark", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> SPARK_ORANGE = PARTICLES.register("spark_orange", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> SPARK_RED = PARTICLES.register("spark_red", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> SPIRAL = PARTICLES.register("sparkb", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MAGAZINE = PARTICLES.register("magazine", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MAGAZINE_RIGHT = PARTICLES.register("magazine_right", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MAGAZINE_LEFT = PARTICLES.register("magazine_left", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MECH_DUST = PARTICLES.register("dust", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MECH_DUST_HEAVY = PARTICLES.register("dust_heavy", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<ParticleType<AttachedMuzzleFlashOptions>> MUZZLE_FLASH = PARTICLES.register("muzzle_flash",
			() -> new ParticleType<AttachedMuzzleFlashOptions>(true,
			AttachedMuzzleFlashOptions.DESERIALIZER) {
				@Override
				public Codec<AttachedMuzzleFlashOptions> codec() {
					return AttachedMuzzleFlashOptions.CODEC;
				}
			}
	);

	public static class PomkotsSimpleParticleType extends SimpleParticleType {
		protected PomkotsSimpleParticleType(boolean bl) {
			super(bl);
		}
	}

	// ITEMS ---------------------------------------------------------------------------------------------------

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MODID, Registries.ITEM);

	// ITEMS
	public static final RegistrySupplier<Item> MECH_WORKBENCH_BLOCK_ITEM = ITEMS.register("mechworkbench_block_item", () -> new BlockItem(MECH_WORKBENCH_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PARTS_WORKBENCH_BLOCK_ITEM = ITEMS.register("partsworkbench_block_item", () -> new BlockItem(PARTS_WORKBENCH_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> MECH_SALVAGER_BLOCK_ITEM = ITEMS.register("mechsalvager_block_item", () -> new BlockItem(MECH_SALVAGER_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> CORE_STONE_BLOCK_ITEM = ITEMS.register("corestone_block_item", () -> new BlockItem(CORE_STONE_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> POMKOTS_CUBE_BLOCK_ITEM = ITEMS.register("pomkotscube", () -> new BlockItem(POMKOTS_CUBE_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POMKOTS_CUBE_BLOCK_ITEM_YELLOW = ITEMS.register("pomkotscube_yellow", () -> new BlockItem(POMKOTS_CUBE_BLOCK_YELLOW.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POMKOTS_CUBE_BLOCK_ITEM_RED = ITEMS.register("pomkotscube_red", () -> new BlockItem(POMKOTS_CUBE_BLOCK_RED.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POMKOTS_CUBE_BLOCK_ITEM_PURPLE = ITEMS.register("pomkotscube_purple", () -> new BlockItem(POMKOTS_CUBE_BLOCK_PURPLE.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> POMKOTS_LEVER_BLOCK_ITEM = ITEMS.register("pomkotslever", () -> new BlockItem(POMKOTS_LEVER_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> EXCHANGE_BLOCK_ITEM = ITEMS.register("exchange", () -> new BlockItem(EXCHANGE_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> CASK_BLOCK_ITEM = ITEMS.register("cask", () -> new BlockItem(CASK_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> STRUCTURE_SPAWNER_BLOCK_ITEM = ITEMS.register("structure_spawner", () -> new BlockItem(STRUCTURE_SPAWNER_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> ENTITY_SPAWNER_BLOCK_ITEM = ITEMS.register("entity_spawner", () -> new BlockItem(ENTITY_SPAWNER_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> CUSTOM_SPAWNER_BLOCK_ITEM = ITEMS.register("custom_spawner", () -> new BlockItem(CUSTOM_SPAWNER_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> COMMAND_EXECUTOR_BLOCK_ITEM = ITEMS.register("command_executor", () -> new BlockItem(COMMAND_EXECUTOR_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> PLACE_HOLDER_BLOCK_ITEM = ITEMS.register("place_holder", () -> new BlockItem(PLACE_HOLDER_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> ARENA_CONTROLLER_BLOCK_ITEM = ITEMS.register("arena_controller", () -> new BlockItem(ARENA_CONTROLLER_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> ARENA_GATE_BLOCK_ITEM = ITEMS.register("arena_gate", () -> new BlockItem(ARENA_GATE_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> ARENA_BATTLEFIELD_ANCHOR_BLOCK_ITEM = ITEMS.register("arena_battlefield_anchor", () -> new BlockItem(ARENA_BATTLEFIELD_ANCHOR_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> ARENA_TELEPORT_BLOCK_ITEM = ITEMS.register("arena_teleport", () -> new BlockItem(ARENA_TELEPORT_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> ASSET_ANCHOR_ITEM = ITEMS.register("asset_anchor", () -> new BlockItem(ASSET_ANCHOR.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> WRENCH_ITEM = ITEMS.register("pomkots_wrench", () -> new PomkotsWrenchItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SPANNER_ITEM = ITEMS.register("pomkots_spanner", () -> new PomkotsSpannerItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> MECH_CAPSULE2_ITEM = ITEMS.register("mech_capsule2", () -> new MechCapsule2Item(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> MECH_CAPSULE_ITEM = ITEMS.register("mech_capsule", () -> new MechCapsuleItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> POMKOTS_RADAR_ITEM = ITEMS.register("pomkots_radar", () -> new PomkotsRadarItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> POMKOTS_DATAPAD_ITEM = ITEMS.register("pomkots_datapad", () -> new PomkotsDatapadItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> KEYCARD_ITEM = ITEMS.register("keycard", () -> new KeycardItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> CUBEKEY_ITEM = ITEMS.register("cubekey", () -> new Item(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> CUBEKEYFRAGMENT_ITEM = ITEMS.register("cubekeyfragment", () -> new Item(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> CUBEKEY_ITEM_PURPLE = ITEMS.register("cubekey_purple", () -> new Item(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> WORLD_LOG_FRAGMENT_ITEM = ITEMS.register("world_log_fragment", () -> new FlavorTextItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> FOLKLORE_COMPENDIUM_ITEM = ITEMS.register("folklore_compendium", () -> new FlavorTextItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> VILLAGERS_JOURNAL_ITEM = ITEMS.register("villagers_journal", () -> new FlavorTextItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> REPAIRKIT_ITEM = ITEMS.register("repairkit", () -> new RepairKitItem(new Item.Properties().stacksTo(8)));

	public static final RegistrySupplier<Item> TURRET_01_ITEM = ITEMS.register("turret01", () -> new Turret01Item(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> P_TITANIUM_INGOT = ITEMS.register("p_titanium_ingot", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> P_TITANIUM_NUGGET = ITEMS.register("p_titanium_nugget", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> LARGE_STEEL_PLATE = ITEMS.register("large_steel_plate", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POM_COIN = ITEMS.register("pom_coin", () -> new PomCoinItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POM_COIN_SILVER = ITEMS.register("pom_coin_silver", () -> new PomCoinSilverItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POM_COIN_GOLD = ITEMS.register("pom_coin_gold", () -> new PomCoinGoldItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> PILOT_CONRFIGURATOR_ITEM = ITEMS.register("pilot_configurator_item", () -> new PilotConfiguratorItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_LICENSE_NOVICE_ITEM = ITEMS.register("pilot_license_novice", () -> new PilotLicenseItem.PilotLicenseNovice(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_LICENSE_INTERMEDIATE_ITEM = ITEMS.register("pilot_license_intermediate", () -> new PilotLicenseItem.PilotLicenseIntermediate(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_LICENSE_ADVANCED_ITEM = ITEMS.register("pilot_license_advanced", () -> new PilotLicenseItem.PilotLicenseAdvanced(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_LICENSE_LEGEND_ITEM = ITEMS.register("pilot_license_legend", () -> new PilotLicenseItem.PilotLicenseLegend(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> PILOT_ROLE_GUARDIAN_ITEM = ITEMS.register("pilot_role_guardian", () -> new PilotRoleItem.PlotRoleGuardian(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_ROLE_RAIDER_ITEM = ITEMS.register("pilot_role_raider", () -> new PilotRoleItem.PlotRoleRaider(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_ROLE_WINGMAN_ITEM = ITEMS.register("pilot_role_wingman", () -> new PilotRoleItem.PlotRoleWingman(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PILOT_ROLE_GLADIATOR_ITEM = ITEMS.register("pilot_role_gladiator", () -> new PilotRoleItem.PlotRoleGladiator(new Item.Properties().stacksTo(64)));

	// PARTS
	public static final RegistrySupplier<Item> RUSTY_HEAD = ITEMS.register("rustyhead", () -> new RustyItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> RUSTY_BODY = ITEMS.register("rustybody", () -> new RustyItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> RUSTY_ARM = ITEMS.register("rustyarm", () -> new RustyItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> RUSTY_LEGS = ITEMS.register("rustylegs", () -> new RustyItem.Legs(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> ALDEBARAN_HEAD = ITEMS.register("aldebaranhead", () -> new AldebaranItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> ALDEBARAN_BODY = ITEMS.register("aldebaranbody", () -> new AldebaranItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> ALDEBARAN_ARM = ITEMS.register("aldebaranarm", () -> new AldebaranItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> ALDEBARAN_LEGS = ITEMS.register("aldebaranlegs", () -> new AldebaranItem.Legs(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> DENEB_HEAD = ITEMS.register("denebhead", () -> new DenebItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> DENEB_BODY = ITEMS.register("denebbody", () -> new DenebItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> DENEB_ARM = ITEMS.register("denebarm", () -> new DenebItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> DENEB_LEGS = ITEMS.register("deneblegs", () -> new DenebItem.Legs(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> ALTAIR_HEAD = ITEMS.register("altairhead", () -> new AltairItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> ALTAIR_BODY = ITEMS.register("altairbody", () -> new AltairItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> ALTAIR_ARM = ITEMS.register("altairarm", () -> new AltairItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> ALTAIR_LEGS = ITEMS.register("altairlegs", () -> new AltairItem.Legs(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> VEGA_HEAD = ITEMS.register("vegahead", () -> new VegaItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> VEGA_BODY = ITEMS.register("vegabody", () -> new VegaItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> VEGA_ARM = ITEMS.register("vegaarm", () -> new VegaItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> VEGA_LEGS = ITEMS.register("vegalegs", () -> new VegaItem.Legs(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> SIRIUS_HEAD = ITEMS.register("siriushead", () -> new SiriusItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SIRIUS_BODY = ITEMS.register("siriusbody", () -> new SiriusItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SIRIUS_ARM = ITEMS.register("siriusarm", () -> new SiriusItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SIRIUS_LEGS = ITEMS.register("siriuslegs", () -> new SiriusItem.Legs(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> MUKNVALI_HEAD = ITEMS.register("muknvalihead", () -> new MuknvaliItem.Head(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MUKNVALI_BODY = ITEMS.register("muknvalibody", () -> new MuknvaliItem.Body(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MUKNVALI_ARM = ITEMS.register("muknvaliarm", () -> new MuknvaliItem.Arm(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MUKNVALI_LEGS = ITEMS.register("muknvalilegs", () -> new MuknvaliItem.Legs(new Item.Properties().stacksTo(1)));

	// GENERATORS
	public static final RegistrySupplier<Item> CHIBA_GENERATOR = ITEMS.register("chiba", () -> new ChibaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SHIGA_GENERATOR = ITEMS.register("shiga", () -> new ShigaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SAGA_GENERATOR = ITEMS.register("saga", () -> new SagaItem(new Item.Properties().stacksTo(1)));

	// BOOSTERS
	public static final RegistrySupplier<Item> HANEDA_BOOSTER = ITEMS.register("haneda", () -> new HanedaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> NARITA_BOOSTER = ITEMS.register("narita", () -> new NaritaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> KANSAI_BOOSTER = ITEMS.register("kansai", () -> new KansaiItem(new Item.Properties().stacksTo(1)));

	// EXTENSIONS
	public static final RegistrySupplier<Item> SOFT_LOCK_CIRCUIT = ITEMS.register("circuitsoftlock", () -> new CircuitSoftLockItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> HARD_LOCK_CIRCUIT = ITEMS.register("circuithardlock", () -> new CircuitHardLockItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> HOVER_UNIT = ITEMS.register("hoverunit", () -> new HoverUnitItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> RAIL_SLIDER = ITEMS.register("railslider", () -> new RailSliderItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SB_PROTO = ITEMS.register("protosbunit", () -> new SBUnitProtoTypeItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BUILDER_UNIT = ITEMS.register("builderunit", () -> new BuilderUnitItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> CORE_DRILL = ITEMS.register("coredrill", () -> new CoreDrillItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> GLIDER_UNIT = ITEMS.register("gliderunit", () -> new GliderUnitItem(new Item.Properties().stacksTo(1)));

	// CIRCUITS
	public static final RegistrySupplier<Item> CIRCUIT_BASE = ITEMS.register("circuits/circuit", () -> new CircuitItem(new Item.Properties().stacksTo(64)));

	// WEAPONS

	public static final RegistrySupplier<Item> SHAKUJI_WEAPON = ITEMS.register("shakuji", () -> new ShakujiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SHINOBAZU_WEAPON = ITEMS.register("shinobazu", () -> new ShinobazuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SENZOKU_WEAPON = ITEMS.register("senzoku", () -> new SenzokuItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> KASUMI_WEAPON = ITEMS.register("kasumi", () -> new KasumiItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BIWA_WEAPON = ITEMS.register("biwa", () -> new BiwaItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> KAWASEMI_WEAPON = ITEMS.register("kawasemi", () -> new KawasemiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> NOSURI_WEAPON = ITEMS.register("nosuri", () -> new NosuriItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> TSUBAME_WEAPON = ITEMS.register("tsubame", () -> new TsubameItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> MUKUDORI_WEAPON = ITEMS.register("mukudori", () -> new MukudoriItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> UGUISU_WEAPON = ITEMS.register("uguisu", () -> new UguisuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> DODO_WEAPON = ITEMS.register("dodo", () -> new DodoItem(new Item.Properties().stacksTo(1)));

	// melee
	public static final RegistrySupplier<Item> TENPOU_WEAPON = ITEMS.register("tenpou", () -> new TenpouItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> KAGENOBU_WEAPON = ITEMS.register("kagenobu", () -> new KagenobuItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> TAKAO_WEAPON = ITEMS.register("takao", () -> new TakaoItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> JINBA_WEAPON = ITEMS.register("jinba", () -> new JinbaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> DAIGOMARU_WEAPON = ITEMS.register("daigomaru", () -> new DaigomaruItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SHOUTOU_WEAPON = ITEMS.register("shoutou", () -> new ShoutouItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> AMAGI_WEAPON = ITEMS.register("amagi", () -> new AmagiItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> TSURUGI_WEAPON = ITEMS.register("tsurugi", () -> new TsurugiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MITAKE_WEAPON = ITEMS.register("mitake", () -> new MitakeItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SUWA_WEAPON = ITEMS.register("suwa", () -> new SuwaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> KAGAMI_WEAPON = ITEMS.register("kagami", () -> new KagamiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MASHU_WEAPON = ITEMS.register("mashu", () -> new MashuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> GASSAN_WEAPON = ITEMS.register("gassan", () -> new GassanItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> WADA_WEAPON = ITEMS.register("wada", () -> new WadaItem(new Item.Properties().stacksTo(1)));

	// MAGAZINES
	public static final RegistrySupplier<Item> RIFLE_MAGAZINE = ITEMS.register("magazinerifle", () -> new MagazineRifleItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> MACHINE_GUN_MAGAZINE = ITEMS.register("magazinemachinegun", () -> new MagazineMachineGunItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> MISSILE_MAGAZINE = ITEMS.register("magazinemissile", () -> new MagazineMissileItem(new Item.Properties().stacksTo(12)));
	public static final RegistrySupplier<Item> MISSILE_LARGE_MAGAZINE = ITEMS.register("magazinemissilelarge", () -> new MagazineMissileLargeItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> GRENADE_MAGAZINE = ITEMS.register("magazinegrenade", () -> new MagazineGrenadeItem(new Item.Properties().stacksTo(12)));
	public static final RegistrySupplier<Item> GATLING_MAGAZINE = ITEMS.register("magazinegatling", () -> new MagazineGatlingItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> SHOTGUN_MAGAZINE = ITEMS.register("magazineshotgun", () -> new MagazineShotGunItem(new Item.Properties().stacksTo(32)));

	public static final RegistrySupplier<Item> PELLET = ITEMS.register("pellet", () -> new PelletItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<ArmorItem> POMKOTS_ARMOR_HELMET = ITEMS.register("pomkotsarmorhelmet", () -> new PomkotsArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<ArmorItem> POMKOTS_ARMOR_CHESTPLATE = ITEMS.register("pomkotsarmorchestplate", () -> new PomkotsArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<ArmorItem> POMKOTS_ARMOR_LEGGINGS = ITEMS.register("pomkotsarmorleggings", () -> new PomkotsArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<ArmorItem> POMKOTS_ARMOR_BOOTS = ITEMS.register("pomkotsarmorboots", () -> new PomkotsArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<ArmorItem> WANDERER_ARMOR_HELMET = ITEMS.register("wandererarmorhelmet", () -> new WandererArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<ArmorItem> WANDERER_ARMOR_CHESTPLATE = ITEMS.register("wandererarmorchestplate", () -> new WandererArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<ArmorItem> WANDERER_ARMOR_LEGGINGS = ITEMS.register("wandererarmorleggings", () -> new WandererArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<ArmorItem> WANDERER_ARMOR_BOOTS = ITEMS.register("wandererarmorboots", () -> new WandererArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> CARTON = ITEMS.register("carton", () -> new CartonItem(new Item.Properties().stacksTo(1)));

	// SPAWN EGGS
	public static final RegistrySupplier<Item> CORESTONE_PMVC01 = ITEMS.register("corestone_pmvc01", () -> new CoreStonePMVC01Item(new Item.Properties()));

	public static final RegistrySupplier<Item> CORESTONE_PMV01 = ITEMS.register("corestone_pmv01", () -> new CoreStonePMV01Item(new Item.Properties()));
	public static final RegistrySupplier<Item> CORESTONE_PMV01B = ITEMS.register("corestone_pmv01b", () -> new CoreStonePMV01BItem(new Item.Properties()));
	public static final RegistrySupplier<Item> CORESTONE_PMV02 = ITEMS.register("corestone_pmv02", () -> new CoreStonePMV02Item(new Item.Properties()));
	public static final RegistrySupplier<Item> CORESTONE_PMV03 = ITEMS.register("corestone_pmv03", () -> new CoreStonePMV03Item(new Item.Properties()));

	public static final RegistrySupplier<Item> CORESTONE_PMB01 = ITEMS.register("corestone_pmb01", () -> new CoreStonePMB01Item(new Item.Properties()));

	public static final RegistrySupplier<Item> PMB01_SPAWN_EGG = ITEMS.register("pmb01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB01, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB01MK2_SPAWN_EGG = ITEMS.register("pmb01mk2_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB01MK2, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB02_SPAWN_EGG = ITEMS.register("pmb02_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB02, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB03_SPAWN_EGG = ITEMS.register("pmb03_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB03, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB04_SPAWN_EGG = ITEMS.register("pmb04_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB04, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB05_SPAWN_EGG = ITEMS.register("pmb05_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB05, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB06_SPAWN_EGG = ITEMS.register("pmb06_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB06, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB07_SPAWN_EGG = ITEMS.register("pmb07_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB07, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB08_SPAWN_EGG = ITEMS.register("pmb08_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB08, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> PMS01_SPAWN_EGG = ITEMS.register("pms01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS01, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS02_SPAWN_EGG = ITEMS.register("pms02_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS02, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS03_SPAWN_EGG = ITEMS.register("pms03_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS03, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS04_SPAWN_EGG = ITEMS.register("pms04_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS04, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS05_SPAWN_EGG = ITEMS.register("pms05_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS05, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS06_SPAWN_EGG = ITEMS.register("pms06_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS06, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS07_SPAWN_EGG = ITEMS.register("pms07_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS07, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS08_SPAWN_EGG = ITEMS.register("pms08_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS08, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS09_SPAWN_EGG = ITEMS.register("pms09_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS09, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS10_SPAWN_EGG = ITEMS.register("pms10_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS10, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> MECH_PILOT_SPAWN_EGG = ITEMS.register("mech_pilot_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.MECH_PILOT, 0xAAAAFF, 0x888888, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> ARENA_RECEP_SPAWN_EGG = ITEMS.register("arena_receptionist_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.ARENA_RECEP, 0xFFAAFF, 0x888888, new Item.Properties().stacksTo(64)));

	public static ItemStack createMechTemplate(String mechName, int pColor, int sColor) {
		ItemStack stack = new ItemStack(PomkotsMechs.MECH_CAPSULE_ITEM.get());
		MechCapsuleItem.buildPreset(mechName, stack);

		CompoundTag root = stack.getOrCreateTag();
		root.putInt(PomkotsMechs.nbtName("PrimaryColor"), pColor);
		root.putInt(PomkotsMechs.nbtName("SecondaryColor"), sColor);

		return stack;
	}

	public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(MODID, Registries.CREATIVE_MODE_TAB);
	public static final RegistrySupplier<CreativeModeTab> BASE_TAB = ITEM_GROUPS.register(PomkotsMechs.id("item_group"), () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(MECH_WORKBENCH_BLOCK_ITEM.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID))
			.displayItems((parameters, output) -> {
				output.accept(new ItemStack(MECH_WORKBENCH_BLOCK_ITEM.get()));
				output.accept(new ItemStack(PARTS_WORKBENCH_BLOCK_ITEM.get()));
				output.accept(new ItemStack(MECH_SALVAGER_BLOCK_ITEM.get()));
				output.accept(new ItemStack(POMKOTS_CUBE_BLOCK_ITEM.get()));
				output.accept(new ItemStack(POMKOTS_CUBE_BLOCK_ITEM_YELLOW.get()));
				output.accept(new ItemStack(POMKOTS_CUBE_BLOCK_ITEM_RED.get()));
				output.accept(new ItemStack(POMKOTS_CUBE_BLOCK_ITEM_PURPLE.get()));

				output.accept(new ItemStack(POMKOTS_LEVER_BLOCK_ITEM.get()));

				output.accept(new ItemStack(CORE_STONE_BLOCK_ITEM.get()));

				output.accept(new ItemStack(EXCHANGE_BLOCK_ITEM.get()));
				output.accept(new ItemStack(CASK_BLOCK_ITEM.get()));
				output.accept(new ItemStack(STRUCTURE_SPAWNER_BLOCK_ITEM.get()));
				output.accept(new ItemStack(ENTITY_SPAWNER_BLOCK_ITEM.get()));
				output.accept(new ItemStack(CUSTOM_SPAWNER_BLOCK_ITEM.get()));
				output.accept(new ItemStack(COMMAND_EXECUTOR_BLOCK_ITEM.get()));
				output.accept(new ItemStack(PLACE_HOLDER_BLOCK_ITEM.get()));

				output.accept(new ItemStack(ARENA_CONTROLLER_BLOCK_ITEM.get()));
				output.accept(new ItemStack(ARENA_GATE_BLOCK_ITEM.get()));
				output.accept(new ItemStack(ARENA_BATTLEFIELD_ANCHOR_BLOCK_ITEM.get()));
				output.accept(new ItemStack(ARENA_TELEPORT_BLOCK_ITEM.get()));

				output.accept(new ItemStack(ASSET_ANCHOR_ITEM.get()));

				output.accept(new ItemStack(WRENCH_ITEM.get()));
				output.accept(new ItemStack(SPANNER_ITEM.get()));
				output.accept(new ItemStack(MECH_CAPSULE2_ITEM.get()));
//				output.accept(new ItemStack(MECH_CAPSULE_ITEM.get()));

				output.accept(new ItemStack(POMKOTS_RADAR_ITEM.get()));
				output.accept(new ItemStack(POMKOTS_DATAPAD_ITEM.get()));

				output.accept(new ItemStack(KEYCARD_ITEM.get()));
				output.accept(new ItemStack(CUBEKEY_ITEM.get()));
				output.accept(new ItemStack(CUBEKEYFRAGMENT_ITEM.get()));
				output.accept(new ItemStack(CUBEKEY_ITEM_PURPLE.get()));

				output.accept(new ItemStack(WORLD_LOG_FRAGMENT_ITEM.get()));
				output.accept(new ItemStack(FOLKLORE_COMPENDIUM_ITEM.get()));
				output.accept(new ItemStack(VILLAGERS_JOURNAL_ITEM.get()));

				output.accept(new ItemStack(REPAIRKIT_ITEM.get()));

				output.accept(new ItemStack(POM_COIN.get()));
				output.accept(new ItemStack(POM_COIN_SILVER.get()));
				output.accept(new ItemStack(POM_COIN_GOLD.get()));

				output.accept(new ItemStack(PILOT_CONRFIGURATOR_ITEM.get()));
				output.accept(new ItemStack(PILOT_LICENSE_NOVICE_ITEM.get()));
				output.accept(new ItemStack(PILOT_LICENSE_INTERMEDIATE_ITEM.get()));
				output.accept(new ItemStack(PILOT_LICENSE_ADVANCED_ITEM.get()));
				output.accept(new ItemStack(PILOT_LICENSE_LEGEND_ITEM.get()));
				output.accept(new ItemStack(PILOT_ROLE_WINGMAN_ITEM.get()));
				output.accept(new ItemStack(PILOT_ROLE_GUARDIAN_ITEM.get()));
				output.accept(new ItemStack(PILOT_ROLE_GLADIATOR_ITEM.get()));
				output.accept(new ItemStack(PILOT_ROLE_RAIDER_ITEM.get()));

				output.accept(new ItemStack(WANDERER_ARMOR_HELMET.get()));
				output.accept(new ItemStack(WANDERER_ARMOR_CHESTPLATE.get()));
				output.accept(new ItemStack(WANDERER_ARMOR_LEGGINGS.get()));
				output.accept(new ItemStack(WANDERER_ARMOR_BOOTS.get()));

				output.accept(new ItemStack(POMKOTS_ARMOR_HELMET.get()));
				output.accept(new ItemStack(POMKOTS_ARMOR_CHESTPLATE.get()));
				output.accept(new ItemStack(POMKOTS_ARMOR_LEGGINGS.get()));
				output.accept(new ItemStack(POMKOTS_ARMOR_BOOTS.get()));

				output.accept(new ItemStack(CARTON.get()));
			})
			.build()
	);

	public static final RegistrySupplier<CreativeModeTab> EGG_TAB = ITEM_GROUPS.register(
			PomkotsMechs.id("item_group_items_spawn"),
			() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(MECH_CAPSULE_ITEM.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".items_spawn"))
			.displayItems((parameters, output) -> {
				output.accept(createMechTemplate("Rusty", 0xFFADADAD, 0xFF542C04));
				output.accept(createMechTemplate("Deneb", 0xFFADADAD, 0xFF0015FF));
				output.accept(createMechTemplate("Altair", 0xFFADADAD, 0xFFFF0000));
				output.accept(createMechTemplate("Vega", 0xFFADADAD, 0xFFFFAE01));
				output.accept(createMechTemplate("Sirius", 0xFFADADAD, 0xFFFFFFFF));
				output.accept(createMechTemplate("Aldebaran", 0xFFADADAD, 0xFF7DB570));
				output.accept(createMechTemplate("Muknvali", 0xFFADADAD, 0xFF000000));
				output.accept(createMechTemplate("Pmb01Mk2", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb02", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb03", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb04", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb05", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb06", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb07", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb08", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pmb99", 0xFF3B3B3B, 0xFFFF0000));
				output.accept(createMechTemplate("Pms01", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms02", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms03", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms04", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms05", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms06", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms07", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms08", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms09", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(createMechTemplate("Pms10", 0xFF3B3B3B, 0xFFFFEF02));
				output.accept(new ItemStack(CORESTONE_PMVC01.get()));
				output.accept(new ItemStack(CORESTONE_PMV01.get()));
				output.accept(new ItemStack(CORESTONE_PMV01B.get()));
				output.accept(new ItemStack(CORESTONE_PMV02.get()));
				output.accept(new ItemStack(CORESTONE_PMV03.get()));
				output.accept(new ItemStack(CORESTONE_PMB01.get()));

				output.accept(new ItemStack(PMB01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB01MK2_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB02_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB03_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB04_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB05_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB06_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB07_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMB08_SPAWN_EGG.get()));

				output.accept(new ItemStack(PMS01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS02_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS03_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS04_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS05_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS06_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS07_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS08_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS09_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS10_SPAWN_EGG.get()));

				output.accept(new ItemStack(MECH_PILOT_SPAWN_EGG.get()));
//				output.accept(new ItemStack(ARENA_RECEP_SPAWN_EGG.get()));

				output.accept(new ItemStack(TURRET_01_ITEM.get()));
			})
			.build()
	);

	public static final RegistrySupplier<CreativeModeTab> PARTS_TAB =
			ITEM_GROUPS.register(PomkotsMechs.id("item_group_parts"),
			() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(ALTAIR_BODY.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".parts"))
			.displayItems((parameters, output) -> {
				output.accept(new ItemStack(RUSTY_HEAD.get()));
				output.accept(new ItemStack(RUSTY_BODY.get()));
				output.accept(new ItemStack(RUSTY_ARM.get()));
				output.accept(new ItemStack(RUSTY_LEGS.get()));
				output.accept(new ItemStack(DENEB_HEAD.get()));
				output.accept(new ItemStack(DENEB_BODY.get()));
				output.accept(new ItemStack(DENEB_ARM.get()));
				output.accept(new ItemStack(DENEB_LEGS.get()));
				output.accept(new ItemStack(ALTAIR_HEAD.get()));
				output.accept(new ItemStack(ALTAIR_BODY.get()));
				output.accept(new ItemStack(ALTAIR_ARM.get()));
				output.accept(new ItemStack(ALTAIR_LEGS.get()));
				output.accept(new ItemStack(VEGA_HEAD.get()));
				output.accept(new ItemStack(VEGA_BODY.get()));
				output.accept(new ItemStack(VEGA_ARM.get()));
				output.accept(new ItemStack(VEGA_LEGS.get()));
				output.accept(new ItemStack(SIRIUS_HEAD.get()));
				output.accept(new ItemStack(SIRIUS_BODY.get()));
				output.accept(new ItemStack(SIRIUS_ARM.get()));
				output.accept(new ItemStack(SIRIUS_LEGS.get()));
				output.accept(new ItemStack(ALDEBARAN_HEAD.get()));
				output.accept(new ItemStack(ALDEBARAN_BODY.get()));
				output.accept(new ItemStack(ALDEBARAN_ARM.get()));
				output.accept(new ItemStack(ALDEBARAN_LEGS.get()));
				output.accept(new ItemStack(MUKNVALI_HEAD.get()));
				output.accept(new ItemStack(MUKNVALI_ARM.get()));
				output.accept(new ItemStack(MUKNVALI_BODY.get()));
				output.accept(new ItemStack(MUKNVALI_LEGS.get()));

				output.accept(new ItemStack(CHIBA_GENERATOR.get()));
				output.accept(new ItemStack(SHIGA_GENERATOR.get()));
				output.accept(new ItemStack(SAGA_GENERATOR.get()));

				output.accept(new ItemStack(HANEDA_BOOSTER.get()));
				output.accept(new ItemStack(NARITA_BOOSTER.get()));
				output.accept(new ItemStack(KANSAI_BOOSTER.get()));

				output.accept(new ItemStack(SOFT_LOCK_CIRCUIT.get()));
				output.accept(new ItemStack(HARD_LOCK_CIRCUIT.get()));
				output.accept(new ItemStack(HOVER_UNIT.get()));
				output.accept(new ItemStack(RAIL_SLIDER.get()));
				output.accept(new ItemStack(SB_PROTO.get()));
				output.accept(new ItemStack(BUILDER_UNIT.get()));
				output.accept(new ItemStack(GLIDER_UNIT.get()));
				output.accept(new ItemStack(CORE_DRILL.get()));

				output.accept(new ItemStack(PELLET.get()));

				CircuitStackFactory.addAll(output);
			})
			.build()
	);

	public static final RegistrySupplier<CreativeModeTab> WEAPON_TAB =
			ITEM_GROUPS.register(PomkotsMechs.id("item_group_parts_weapons"),
					() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
							.icon(() -> new ItemStack(SHAKUJI_WEAPON.get()))
							.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".parts_weapons"))
							.displayItems((parameters, output) -> {
								output.accept(new ItemStack(SHAKUJI_WEAPON.get()));
								output.accept(new ItemStack(SHINOBAZU_WEAPON.get()));
								output.accept(new ItemStack(SENZOKU_WEAPON.get()));
								output.accept(new ItemStack(KASUMI_WEAPON.get()));
								output.accept(new ItemStack(KAGAMI_WEAPON.get()));
								output.accept(new ItemStack(UGUISU_WEAPON.get()));
								output.accept(new ItemStack(MASHU_WEAPON.get()));

								output.accept(new ItemStack(TENPOU_WEAPON.get()));
								output.accept(new ItemStack(TSURUGI_WEAPON.get()));
								output.accept(new ItemStack(MITAKE_WEAPON.get()));

								output.accept(new ItemStack(KAGENOBU_WEAPON.get()));
								output.accept(new ItemStack(TAKAO_WEAPON.get()));
								output.accept(new ItemStack(JINBA_WEAPON.get()));
								output.accept(new ItemStack(DAIGOMARU_WEAPON.get()));
								output.accept(new ItemStack(SHOUTOU_WEAPON.get()));
								output.accept(new ItemStack(WADA_WEAPON.get()));

								output.accept(new ItemStack(AMAGI_WEAPON.get()));

								output.accept(new ItemStack(BIWA_WEAPON.get()));
								output.accept(new ItemStack(SUWA_WEAPON.get()));

								output.accept(new ItemStack(KAWASEMI_WEAPON.get()));
								output.accept(new ItemStack(NOSURI_WEAPON.get()));
								output.accept(new ItemStack(TSUBAME_WEAPON.get()));
								output.accept(new ItemStack(MUKUDORI_WEAPON.get()));
								output.accept(new ItemStack(DODO_WEAPON.get()));

								output.accept(new ItemStack(RIFLE_MAGAZINE.get()));
								output.accept(new ItemStack(SHOTGUN_MAGAZINE.get()));
								output.accept(new ItemStack(MACHINE_GUN_MAGAZINE.get()));
								output.accept(new ItemStack(GATLING_MAGAZINE.get()));
								output.accept(new ItemStack(MISSILE_MAGAZINE.get()));
								output.accept(new ItemStack(MISSILE_LARGE_MAGAZINE.get()));
								output.accept(new ItemStack(GRENADE_MAGAZINE.get()));

							})
							.build()
	);

	public static final RegistrySupplier<Item> QUEST_SHEET_01 = ITEMS.register("quest_sheet_01", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_02 = ITEMS.register("quest_sheet_02", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_03 = ITEMS.register("quest_sheet_03", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_04 = ITEMS.register("quest_sheet_04", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_05 = ITEMS.register("quest_sheet_05", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_06 = ITEMS.register("quest_sheet_06", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_07 = ITEMS.register("quest_sheet_07", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_08 = ITEMS.register("quest_sheet_08", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_09 = ITEMS.register("quest_sheet_09", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_10 = ITEMS.register("quest_sheet_10", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_11 = ITEMS.register("quest_sheet_11", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_12 = ITEMS.register("quest_sheet_12", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_13 = ITEMS.register("quest_sheet_13", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_14 = ITEMS.register("quest_sheet_14", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_15 = ITEMS.register("quest_sheet_15", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_16 = ITEMS.register("quest_sheet_16", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_17 = ITEMS.register("quest_sheet_17", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_18 = ITEMS.register("quest_sheet_18", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_19 = ITEMS.register("quest_sheet_19", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> QUEST_SHEET_20 = ITEMS.register("quest_sheet_20", () -> new QuestSheetItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_DENEB = ITEMS.register("blue_print_deneb", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_ALTAIR = ITEMS.register("blue_print_altair", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_VEGA = ITEMS.register("blue_print_vega", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SIRIUS = ITEMS.register("blue_print_sirius", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_ALDEBARAN = ITEMS.register("blue_print_aldebaran", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_MUKNVALI = ITEMS.register("blue_print_muknvali", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_CHIBA = ITEMS.register("blue_print_chiba", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SHIGA = ITEMS.register("blue_print_shiga", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SAGA = ITEMS.register("blue_print_saga", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_HANEDA = ITEMS.register("blue_print_haneda", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_NARITA = ITEMS.register("blue_print_narita", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_KANSAI = ITEMS.register("blue_print_kansai", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_SHAKUJI = ITEMS.register("blue_print_shakuji", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SHINOBAZU = ITEMS.register("blue_print_shinobazu", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SENZOKU = ITEMS.register("blue_print_senzoku", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_KASUMI = ITEMS.register("blue_print_kasumi", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_KAGAMI = ITEMS.register("blue_print_kagami", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_UGUISU = ITEMS.register("blue_print_uguisu", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_MASHU = ITEMS.register("blue_print_mashu", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_TSURUGI = ITEMS.register("blue_print_tsurugi", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_MITAKE = ITEMS.register("blue_print_mitake", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_KAGENOBU = ITEMS.register("blue_print_kagenobu", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_TAKAO = ITEMS.register("blue_print_takao", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_JINBA = ITEMS.register("blue_print_jinba", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_AMAGI = ITEMS.register("blue_print_amagi", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_DAIGOMARU = ITEMS.register("blue_print_daigomaru", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SHOUTOU = ITEMS.register("blue_print_shoutou", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_WADA = ITEMS.register("blue_print_wada", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_BIWA = ITEMS.register("blue_print_biwa", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SUWA = ITEMS.register("blue_print_suwa", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_KAWASEMI = ITEMS.register("blue_print_kawasemi", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_NOSURI = ITEMS.register("blue_print_nosuri", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_MUKUDORI = ITEMS.register("blue_print_mukudori", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_TSUBAME = ITEMS.register("blue_print_tsubame", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_DODO = ITEMS.register("blue_print_dodo", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_PROTO_SB_UNIT = ITEMS.register("blue_print_proto_super_boost_unit", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_BUILDER_UNIT = ITEMS.register("blue_print_builder_unit", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_HOVER_UNIT = ITEMS.register("blue_print_hover_unit", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_RAIL_SLIDER_UNIT = ITEMS.register("blue_print_rail_slider_unit", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_GLIDER_UNIT = ITEMS.register("blue_print_glider_unit", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> HM_ARMOR_SHARD = ITEMS.register("materials/heavy_mech_armor_shard", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_BLADE_FRAGMENT = ITEMS.register("materials/heavy_mech_blade_fragment", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_CIRCUIT_BOARD = ITEMS.register("materials/heavy_mech_circuit_board", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_MGUN_FRAGMENT= ITEMS.register("materials/heavy_mech_mgun_fragment", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_POWER_CELL = ITEMS.register("materials/heavy_mech_power_cell_debris", () -> new MaterialItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> HM_CARBIDE_ALLOY = ITEMS.register("materials/heavy_mech_carbide_alloy", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_CONSTRUCTION_UNIT_DEBRIS = ITEMS.register("materials/heavy_mech_construction_unit_debris", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_HIGHT_TORQUE_ACTUATOR = ITEMS.register("materials/heavy_mech_high_torque_actuator", () -> new MaterialItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> HM_BOOSTER_DEBRIS = ITEMS.register("materials/heavy_mech_booster_debris", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_HOVER_DEBRIS = ITEMS.register("materials/heavy_mech_hover_debris", () -> new MaterialItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> HM_PMB01_CORE_STONE_FRAGMENT = ITEMS.register("materials/pmb01_core_stone_fragment", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_PMB04_CORE_STONE_FRAGMENT = ITEMS.register("materials/pmb04_core_stone_fragment", () -> new MaterialItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> HM_PMB07_CORE_STONE_FRAGMENT = ITEMS.register("materials/pmb07_core_stone_fragment", () -> new MaterialItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> SOLD_ITEM = ITEMS.register("sold", () -> new MaterialItem(new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<CreativeModeTab> SHEET_TAB =
			ITEM_GROUPS.register(PomkotsMechs.id("item_group_zmaterials"),
					() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
							.icon(() -> new ItemStack(P_TITANIUM_INGOT.get()))
							.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".zmaterials"))
							.displayItems((parameters, output) -> {
								output.accept(new ItemStack(P_TITANIUM_INGOT.get()));
								output.accept(new ItemStack(P_TITANIUM_NUGGET.get()));
								output.accept(new ItemStack(LARGE_STEEL_PLATE.get()));

								output.accept(new ItemStack(HM_ARMOR_SHARD.get()));
								output.accept(new ItemStack(HM_BLADE_FRAGMENT.get()));
								output.accept(new ItemStack(HM_MGUN_FRAGMENT.get()));
								output.accept(new ItemStack(HM_CIRCUIT_BOARD.get()));
								output.accept(new ItemStack(HM_POWER_CELL.get()));

								output.accept(new ItemStack(HM_CARBIDE_ALLOY.get()));
								output.accept(new ItemStack(HM_CONSTRUCTION_UNIT_DEBRIS.get()));
								output.accept(new ItemStack(HM_HIGHT_TORQUE_ACTUATOR.get()));
								output.accept(new ItemStack(HM_BOOSTER_DEBRIS.get()));
								output.accept(new ItemStack(HM_HOVER_DEBRIS.get()));

								output.accept(new ItemStack(HM_PMB01_CORE_STONE_FRAGMENT.get()));
								output.accept(new ItemStack(HM_PMB04_CORE_STONE_FRAGMENT.get()));
								output.accept(new ItemStack(HM_PMB07_CORE_STONE_FRAGMENT.get()));

								output.accept(new ItemStack(BLUE_PRINT_DENEB.get()));
								output.accept(new ItemStack(BLUE_PRINT_ALTAIR.get()));
								output.accept(new ItemStack(BLUE_PRINT_VEGA.get()));
								output.accept(new ItemStack(BLUE_PRINT_SIRIUS.get()));
								output.accept(new ItemStack(BLUE_PRINT_ALDEBARAN.get()));
								output.accept(new ItemStack(BLUE_PRINT_MUKNVALI.get()));

								output.accept(new ItemStack(BLUE_PRINT_CHIBA.get()));
								output.accept(new ItemStack(BLUE_PRINT_SHIGA.get()));
								output.accept(new ItemStack(BLUE_PRINT_SAGA.get()));

								output.accept(new ItemStack(BLUE_PRINT_HANEDA.get()));
								output.accept(new ItemStack(BLUE_PRINT_NARITA.get()));
								output.accept(new ItemStack(BLUE_PRINT_KANSAI.get()));

								output.accept(new ItemStack(BLUE_PRINT_SHAKUJI.get()));
								output.accept(new ItemStack(BLUE_PRINT_SHINOBAZU.get()));
								output.accept(new ItemStack(BLUE_PRINT_SENZOKU.get()));
								output.accept(new ItemStack(BLUE_PRINT_KASUMI.get()));
								output.accept(new ItemStack(BLUE_PRINT_KAGAMI.get()));
								output.accept(new ItemStack(BLUE_PRINT_UGUISU.get()));
								output.accept(new ItemStack(BLUE_PRINT_MASHU.get()));

								output.accept(new ItemStack(BLUE_PRINT_TSURUGI.get()));
								output.accept(new ItemStack(BLUE_PRINT_MITAKE.get()));
								output.accept(new ItemStack(BLUE_PRINT_KAGENOBU.get()));
								output.accept(new ItemStack(BLUE_PRINT_TAKAO.get()));
								output.accept(new ItemStack(BLUE_PRINT_JINBA.get()));

								output.accept(new ItemStack(BLUE_PRINT_AMAGI.get()));
								output.accept(new ItemStack(BLUE_PRINT_DAIGOMARU.get()));
								output.accept(new ItemStack(BLUE_PRINT_SHOUTOU.get()));
								output.accept(new ItemStack(BLUE_PRINT_WADA.get()));

								output.accept(new ItemStack(BLUE_PRINT_BIWA.get()));
								output.accept(new ItemStack(BLUE_PRINT_SUWA.get()));

								output.accept(new ItemStack(BLUE_PRINT_KAWASEMI.get()));
								output.accept(new ItemStack(BLUE_PRINT_NOSURI.get()));
								output.accept(new ItemStack(BLUE_PRINT_MUKUDORI.get()));
								output.accept(new ItemStack(BLUE_PRINT_TSUBAME.get()));
								output.accept(new ItemStack(BLUE_PRINT_DODO.get()));

								output.accept(new ItemStack(BLUE_PRINT_PROTO_SB_UNIT.get()));
								output.accept(new ItemStack(BLUE_PRINT_BUILDER_UNIT.get()));
								output.accept(new ItemStack(BLUE_PRINT_HOVER_UNIT.get()));
								output.accept(new ItemStack(BLUE_PRINT_RAIL_SLIDER_UNIT.get()));
								output.accept(new ItemStack(BLUE_PRINT_GLIDER_UNIT.get()));

								output.accept(new ItemStack(QUEST_SHEET_01.get()));
								output.accept(new ItemStack(QUEST_SHEET_02.get()));
								output.accept(new ItemStack(QUEST_SHEET_03.get()));
								output.accept(new ItemStack(QUEST_SHEET_04.get()));
								output.accept(new ItemStack(QUEST_SHEET_05.get()));
								output.accept(new ItemStack(QUEST_SHEET_06.get()));
								output.accept(new ItemStack(QUEST_SHEET_07.get()));
								output.accept(new ItemStack(QUEST_SHEET_08.get()));
								output.accept(new ItemStack(QUEST_SHEET_09.get()));
								output.accept(new ItemStack(QUEST_SHEET_10.get()));
								output.accept(new ItemStack(QUEST_SHEET_11.get()));
								output.accept(new ItemStack(QUEST_SHEET_12.get()));
								output.accept(new ItemStack(QUEST_SHEET_13.get()));
								output.accept(new ItemStack(QUEST_SHEET_14.get()));
								output.accept(new ItemStack(QUEST_SHEET_15.get()));
								output.accept(new ItemStack(QUEST_SHEET_16.get()));
								output.accept(new ItemStack(QUEST_SHEET_17.get()));
								output.accept(new ItemStack(QUEST_SHEET_18.get()));
								output.accept(new ItemStack(QUEST_SHEET_19.get()));
								output.accept(new ItemStack(QUEST_SHEET_20.get()));
							})
							.build()
			);

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(PomkotsMechs.MODID, Registries.SOUND_EVENT);

	public static final RegistrySupplier<SoundEvent> SE_BOOSTER_EVENT = SOUNDS.register(id("se_booster"), () -> SoundEvent.createVariableRangeEvent(id("se_booster")));
	public static final RegistrySupplier<SoundEvent> SE_BOSSDOWN_EVENT = SOUNDS.register(id("se_bossdown"), () -> SoundEvent.createVariableRangeEvent(id("se_bossdown")));
	public static final RegistrySupplier<SoundEvent> SE_EARTHBREAK_EVENT = SOUNDS.register(id("se_earthbreak"), () -> SoundEvent.createVariableRangeEvent(id("se_earthbreak")));
	public static final RegistrySupplier<SoundEvent> SE_EARTHRAISE_EVENT = SOUNDS.register(id("se_earthraise"), () -> SoundEvent.createVariableRangeEvent(id("se_earthraise")));
	public static final RegistrySupplier<SoundEvent> SE_EXPLOADSPARK_EVENT = SOUNDS.register(id("se_exploadspark"), () -> SoundEvent.createVariableRangeEvent(id("se_exploadspark")));
	public static final RegistrySupplier<SoundEvent> SE_EXPLOSION_EVENT = SOUNDS.register(id("se_explosion"), () -> SoundEvent.createVariableRangeEvent(id("se_explosion")));
	public static final RegistrySupplier<SoundEvent> SE_GATLING_EVENT = SOUNDS.register(id("se_gatling"), () -> SoundEvent.createVariableRangeEvent(id("se_gatling")));
	public static final RegistrySupplier<SoundEvent> SE_GRENADE_EVENT = SOUNDS.register(id("se_grenade"), () -> SoundEvent.createVariableRangeEvent(id("se_grenade")));
	public static final RegistrySupplier<SoundEvent> SE_HIT_EVENT = SOUNDS.register(id("se_hit"), () -> SoundEvent.createVariableRangeEvent(id("se_hit")));
	public static final RegistrySupplier<SoundEvent> SE_HIT_MIDDLE_EVENT = SOUNDS.register(id("se_hit_middle"), () -> SoundEvent.createVariableRangeEvent(id("se_hit_middle")));
	public static final RegistrySupplier<SoundEvent> SE_HIT_HEAVY_EVENT = SOUNDS.register(id("se_hit_heavy"), () -> SoundEvent.createVariableRangeEvent(id("se_hit_heavy")));
	public static final RegistrySupplier<SoundEvent> SE_JUMP_EVENT = SOUNDS.register(id("se_jump"), () -> SoundEvent.createVariableRangeEvent(id("se_jump")));
	public static final RegistrySupplier<SoundEvent> SE_MISSILE_EVENT = SOUNDS.register(id("se_missile"), () -> SoundEvent.createVariableRangeEvent(id("se_missile")));
	public static final RegistrySupplier<SoundEvent> SE_ONGROUND_EVENT = SOUNDS.register(id("se_onground"), () -> SoundEvent.createVariableRangeEvent(id("se_onground")));
	public static final RegistrySupplier<SoundEvent> SE_PILEBUNKER_EVENT = SOUNDS.register(id("se_pilebunker"), () -> SoundEvent.createVariableRangeEvent(id("se_pilebunker")));
	public static final RegistrySupplier<SoundEvent> SE_WALK_EVENT = SOUNDS.register(id("se_walk"), () -> SoundEvent.createVariableRangeEvent(id("se_walk")));
	public static final RegistrySupplier<SoundEvent> SE_TARGET_EVENT = SOUNDS.register(id("se_target"), () -> SoundEvent.createVariableRangeEvent(id("se_target")));
	public static final RegistrySupplier<SoundEvent> SE_DRILL1 = SOUNDS.register(id("se_drill1"), () -> SoundEvent.createVariableRangeEvent(id("se_drill1")));
	public static final RegistrySupplier<SoundEvent> SE_DRILL2 = SOUNDS.register(id("se_drill2"), () -> SoundEvent.createVariableRangeEvent(id("se_drill2")));
	public static final RegistrySupplier<SoundEvent> SE_HUMMER1 = SOUNDS.register(id("se_hummer1"), () -> SoundEvent.createVariableRangeEvent(id("se_hummer1")));
	public static final RegistrySupplier<SoundEvent> SE_HUMMER2 = SOUNDS.register(id("se_hummer2"), () -> SoundEvent.createVariableRangeEvent(id("se_hummer2")));
	public static final RegistrySupplier<SoundEvent> SE_LIFT = SOUNDS.register(id("se_lift"), () -> SoundEvent.createVariableRangeEvent(id("se_lift")));
	public static final RegistrySupplier<SoundEvent> SE_NEEDLE = SOUNDS.register(id("se_needle"), () -> SoundEvent.createVariableRangeEvent(id("se_needle")));
	public static final RegistrySupplier<SoundEvent> SE_PLACE = SOUNDS.register(id("se_place"), () -> SoundEvent.createVariableRangeEvent(id("se_place")));
	public static final RegistrySupplier<SoundEvent> SE_ROLLER1 = SOUNDS.register(id("se_roller1"), () -> SoundEvent.createVariableRangeEvent(id("se_roller1")));
	public static final RegistrySupplier<SoundEvent> SE_ROLLER2 = SOUNDS.register(id("se_roller2"), () -> SoundEvent.createVariableRangeEvent(id("se_roller2")));
	public static final RegistrySupplier<SoundEvent> SE_STEP = SOUNDS.register(id("se_step"), () -> SoundEvent.createVariableRangeEvent(id("se_step")));
	public static final RegistrySupplier<SoundEvent> SE_THROW = SOUNDS.register(id("se_throw"), () -> SoundEvent.createVariableRangeEvent(id("se_throw")));
	public static final RegistrySupplier<SoundEvent> SE_WATER = SOUNDS.register(id("se_water"), () -> SoundEvent.createVariableRangeEvent(id("se_water")));
	public static final RegistrySupplier<SoundEvent> SE_RIFLE = SOUNDS.register(id("se_rifle"), () -> SoundEvent.createVariableRangeEvent(id("se_rifle")));
	public static final RegistrySupplier<SoundEvent> SE_SHOTGUN = SOUNDS.register(id("se_shotgun"), () -> SoundEvent.createVariableRangeEvent(id("se_shotgun")));
	public static final RegistrySupplier<SoundEvent> SE_SABER = SOUNDS.register(id("se_saber"), () -> SoundEvent.createVariableRangeEvent(id("se_saber")));
	public static final RegistrySupplier<SoundEvent> SE_ALERT = SOUNDS.register(id("se_alert"), () -> SoundEvent.createVariableRangeEvent(id("se_alert")));
	public static final RegistrySupplier<SoundEvent> SE_BOSSBOXOPEN = SOUNDS.register(id("se_bossboxopen"), () -> SoundEvent.createVariableRangeEvent(id("se_bossboxopen")));
	public static final RegistrySupplier<SoundEvent> SE_WALK_LARGE = SOUNDS.register(id("se_walk_large"), () -> SoundEvent.createVariableRangeEvent(id("se_walk_large")));
	public static final RegistrySupplier<SoundEvent> SE_MACHINE = SOUNDS.register(id("se_machine"), () -> SoundEvent.createVariableRangeEvent(id("se_machine")));
	public static final RegistrySupplier<SoundEvent> SE_IMPACT_1 = SOUNDS.register(id("se_impact_1"), () -> SoundEvent.createVariableRangeEvent(id("se_impact_1")));
	public static final RegistrySupplier<SoundEvent> SE_IMPACT_2 = SOUNDS.register(id("se_impact_2"), () -> SoundEvent.createVariableRangeEvent(id("se_impact_2")));
	public static final RegistrySupplier<SoundEvent> SE_CHAINSAW = SOUNDS.register(id("se_chainsaw"), () -> SoundEvent.createVariableRangeEvent(id("se_chainsaw")));
	public static final RegistrySupplier<SoundEvent> SE_BEAM1 = SOUNDS.register(id("se_beam1"), () -> SoundEvent.createVariableRangeEvent(id("se_beam1")));
	public static final RegistrySupplier<SoundEvent> SE_BEAM2 = SOUNDS.register(id("se_beam2"), () -> SoundEvent.createVariableRangeEvent(id("se_beam2")));
	public static final RegistrySupplier<SoundEvent> SE_CHARGE = SOUNDS.register(id("se_charge"), () -> SoundEvent.createVariableRangeEvent(id("se_charge")));
	public static final RegistrySupplier<SoundEvent> SE_GASHAN = SOUNDS.register(id("se_gashan"), () -> SoundEvent.createVariableRangeEvent(id("se_gashan")));
	public static final RegistrySupplier<SoundEvent> SE_BOOST = SOUNDS.register(id("se_boost"), () -> SoundEvent.createVariableRangeEvent(id("se_boost")));
	public static final RegistrySupplier<SoundEvent> SE_DASH = SOUNDS.register(id("se_dash"), () -> SoundEvent.createVariableRangeEvent(id("se_dash")));
	public static final RegistrySupplier<SoundEvent> SE_GUN_1 = SOUNDS.register(id("se_gun1"), () -> SoundEvent.createVariableRangeEvent(id("se_gun1")));
	public static final RegistrySupplier<SoundEvent> SE_GUN_2 = SOUNDS.register(id("se_gun2"), () -> SoundEvent.createVariableRangeEvent(id("se_gun2")));
	public static final RegistrySupplier<SoundEvent> SE_GUN_3 = SOUNDS.register(id("se_gun3"), () -> SoundEvent.createVariableRangeEvent(id("se_gun3")));
	public static final RegistrySupplier<SoundEvent> SE_BOOST_CHARGE = SOUNDS.register(id("se_boost_charge"), () -> SoundEvent.createVariableRangeEvent(id("se_boost_charge")));
	public static final RegistrySupplier<SoundEvent> SE_HERI = SOUNDS.register(id("se_heri"), () -> SoundEvent.createVariableRangeEvent(id("se_heri")));
	public static final RegistrySupplier<SoundEvent> SE_RELOAD = SOUNDS.register(id("se_reload"), () -> SoundEvent.createVariableRangeEvent(id("se_reload")));
	public static final RegistrySupplier<SoundEvent> SE_CLOSE_DOOR = SOUNDS.register(id("se_close_door"), () -> SoundEvent.createVariableRangeEvent(id("se_close_door")));
	public static final RegistrySupplier<SoundEvent> SE_START_CAR = SOUNDS.register(id("se_start_car"), () -> SoundEvent.createVariableRangeEvent(id("se_start_car")));
	public static final RegistrySupplier<SoundEvent> SE_REGISTER = SOUNDS.register(id("se_register"), () -> SoundEvent.createVariableRangeEvent(id("se_register")));
	public static final RegistrySupplier<SoundEvent> SE_BEEP = SOUNDS.register(id("se_beep"), () -> SoundEvent.createVariableRangeEvent(id("se_beep")));
	public static final RegistrySupplier<SoundEvent> SE_COIN = SOUNDS.register(id("se_coin"), () -> SoundEvent.createVariableRangeEvent(id("se_coin")));
	public static final RegistrySupplier<SoundEvent> SE_ELEVATOR = SOUNDS.register(id("se_elevator"), () -> SoundEvent.createVariableRangeEvent(id("se_elevator")));
	public static final RegistrySupplier<SoundEvent> SE_CHEERS = SOUNDS.register(id("se_cheers"), () -> SoundEvent.createVariableRangeEvent(id("se_cheers")));
	public static final RegistrySupplier<SoundEvent> SE_ARENA_END = SOUNDS.register(id("se_arena_end"), () -> SoundEvent.createVariableRangeEvent(id("se_arena_end")));
	public static final RegistrySupplier<SoundEvent> SE_ARENA_START = SOUNDS.register(id("se_arena_start"), () -> SoundEvent.createVariableRangeEvent(id("se_arena_start")));
	public static final RegistrySupplier<SoundEvent> SE_SCAN1 = SOUNDS.register(id("se_scan1"), () -> SoundEvent.createVariableRangeEvent(id("se_scan1")));
	public static final RegistrySupplier<SoundEvent> SE_SCAN2 = SOUNDS.register(id("se_scan2"), () -> SoundEvent.createVariableRangeEvent(id("se_scan2")));

	public static final RegistrySupplier<SoundEvent> BGM_TITLE = SOUNDS.register(id("title"), () -> SoundEvent.createVariableRangeEvent(id("title")));
	public static final RegistrySupplier<SoundEvent> BGM_OPENING = SOUNDS.register(id("opening"), () -> SoundEvent.createVariableRangeEvent(id("opening")));
	public static final RegistrySupplier<SoundEvent> BGM_RUIN_CITY = SOUNDS.register(id("ruin_city"), () -> SoundEvent.createVariableRangeEvent(id("ruin_city")));
	public static final RegistrySupplier<SoundEvent> BGM_RUIN_SCATTERED = SOUNDS.register(id("ruin_scattered"), () -> SoundEvent.createVariableRangeEvent(id("ruin_scattered")));
	public static final RegistrySupplier<SoundEvent> BGM_BATTLE_BOSS = SOUNDS.register(id("battle_boss"), () -> SoundEvent.createVariableRangeEvent(id("battle_boss")));
	public static final RegistrySupplier<SoundEvent> BGM_BATTLE_RAID = SOUNDS.register(id("battle_raid"), () -> SoundEvent.createVariableRangeEvent(id("battle_raid")));
	public static final RegistrySupplier<SoundEvent> BGM_FIELD = SOUNDS.register(id("field_a"), () -> SoundEvent.createVariableRangeEvent(id("field_a")));


	public static PomkotsConfig CONFIG;

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(PomkotsMechs.MODID, Registries.MENU);

	public static final RegistrySupplier<MenuType<MechWorkbenchMenu>> MECH_WORKBENCH_GUI = MENUS.register(
			"mechworkbench_gui",
			() -> new MenuType<>(MechWorkbenchMenu::new, FeatureFlagSet.of())
	);
	public static final RegistrySupplier<MenuType<PartsWorkbenchMenu>> PARTS_WORKBENCH_GUI = MENUS.register(
			"partsworkbench_gui",
			() -> new MenuType<>(PartsWorkbenchMenu::new, FeatureFlagSet.of())
	);
	public static final RegistrySupplier<MenuType<MechSalvagerMenu>> MECH_SALVAGER_GUI = MENUS.register(
			"mechsalvager_gui",
			() -> new MenuType<>(MechSalvagerMenu::new, FeatureFlagSet.of())
	);
	public static final RegistrySupplier<MenuType<RadarTargetSelectMenu>> POMKOTS_RADAR_GUI = MENUS.register(
			"pomkots_radar_gui",
			() -> new MenuType<>(RadarTargetSelectMenu::new, FeatureFlagSet.of())
	);
	public static final RegistrySupplier<MenuType<MechTraderMenu>> MECH_TRADER_GUI = MENUS.register(
			"mech_trader_gui",
			() -> new MenuType<>(MechTraderMenu::new, FeatureFlagSet.of())
	);
	public static final RegistrySupplier<MenuType<PilotMenu>> PILOT_CONFIG_GUI = MENUS.register(
			"pilot_config_gui",
			() -> new MenuType<>(PilotMenu::new, FeatureFlagSet.of())
	);

	public static final RegistrySupplier<MenuType<DataPadMenu>> POMKOTS_DATAPAD_GUI = MENUS.register(
			"pomkots_datapad_gui",
			() -> new MenuType<>(DataPadMenu::new, FeatureFlagSet.of())
	);

	public static final RegistrySupplier<MenuType<DataPadKeyCardMenu>> POMKOTS_DATAPAD_KEYCARD_GUI = MENUS.register(
			"pomkots_datapad_keycard_gui",
			() -> new MenuType<>(DataPadKeyCardMenu::new, FeatureFlagSet.of())
	);

	public static final RegistrySupplier<MenuType<ArenaReceptionistMenu>>
			ARENA_RECEPTIONIST =
			MENUS.register(
					"arena_receptionist",
					() -> MenuRegistry.ofExtended(
							ArenaReceptionistMenu::new
					)
			);

	public static final RegistrySupplier<MenuType<ArenaBattleResultMenu>>
			ARENA_RESULT_MENU =
			MENUS.register(
					"arena_result_menu",
					() -> MenuRegistry.ofExtended(
							ArenaBattleResultMenu::new
					)
			);

	public static final GameRules.Key<GameRules.BooleanValue> RULE_MAP_EDIT = GameRules.register("pomkotsmechsMapEditMode", GameRules.Category.MISC, GameRules.BooleanValue.create(false));

	public static void initialize() {
		AutoConfig.register(PomkotsConfig.class, GsonConfigSerializer::new);
		var configHolder = AutoConfig.getConfigHolder(PomkotsConfig.class);
		configHolder.registerSaveListener((manager, config) -> {
			try {
				Utils.updateDestConfig(config);
				Utils.updateDropConfig(config);
			} catch (Exception e) {
				PomkotsMechs.LOGGER.info("Failed to save setting.", e);
			}

			return InteractionResult.SUCCESS;

		});
		CONFIG = configHolder.getConfig();


		ENTITIES.register();

		EntityAttributeRegistry.register(PMV01::get, Pmv01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMV01B::get, Pmv01bEntity::createMobAttributes);
		EntityAttributeRegistry.register(PMV02::get, Pmv02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMV03P::get, Pmv03pEntity::createMobAttributes);
		EntityAttributeRegistry.register(PMV03::get, Pmv03Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMVC01::get, Pmvc01Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMVT01::get, Pmvt01Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMB01::get, Pmb01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB01MK2::get, Pmb01mk2Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB02::get, Pmb02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB03::get, Pmb03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB04::get, Pmb04Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB05::get, Pmb05Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB06::get, Pmb06Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB07::get, Pmb07Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB08::get, Pmb07Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMB99::get, Pmb99Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMSS01::get, Pmss01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMSS02::get, Pmss02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMSS03::get, Pmss03Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMS01::get, Pms01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS02::get, Pms02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS03::get, Pms03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS04::get, Pms04Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS05::get, Pms05Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS06::get, Pms06Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS07::get, Pms07Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS08::get, Pms08Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS09::get, Pms09Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS10::get, Pms09Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMC01::get, Pmc01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMC02::get, Pmc02Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMT01::get, Pmt01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMT02::get, Pmt02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMT03::get, Pmt03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMT04::get, Pmt04Entity::createMobAttributes);

		EntityAttributeRegistry.register(MECH_TRADER::get, MechTraderEntity::createMobAttributes);
		EntityAttributeRegistry.register(MECH_PILOT::get, MechPilotEntity::createMobAttributes);
		EntityAttributeRegistry.register(ARENA_RECEP::get, ArenaReceptionistEntity::createMobAttributes);

		EntityAttributeRegistry.register(PLAYERDUMMY::get, PlayerDummyEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX1::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX2::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX_PMB02::get, BossHitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX_PMB03::get, BossHitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX_PMB06::get, BossHitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX_PMB08::get, BossHitBoxEntity::createMobAttributes);

		EntityAttributeRegistry.register(RAID_CONTROLLER::get, RaidControllerEntity::createMobAttributes);
		EntityAttributeRegistry.register(RAID_OBJECTIVE::get, RaidObjectiveEntity::createMobAttributes);

		EntityAttributeRegistry.register(BLOCK_MASS::get, BlockMassEntity::createMobAttributes);
		EntityAttributeRegistry.register(PRESENT_BOX::get, PresentBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(KUJIRA::get, KujiraEntity::createMobAttributes);
		EntityAttributeRegistry.register(PLATE::get, PlateEntity::createMobAttributes);
		EntityAttributeRegistry.register(ELEVATOR::get, ElevatorEntity::createMobAttributes);
		EntityAttributeRegistry.register(ARENA_CAMERA::get, ArenaCameraEntity::createMobAttributes);

		BLOCKS.register();
		BLOCK_ENTITIES.register();
		PARTICLES.register();
		ITEMS.register();
		ITEM_GROUPS.register();
		SOUNDS.register();
		MENUS.register();

		SpawnPlacementsRegistry.register(PMS01, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pms01Entity::canSpawn);
		SpawnPlacementsRegistry.register(PMS02, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pms02Entity::canSpawn);
		SpawnPlacementsRegistry.register(PMS03, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pms03Entity::canSpawn);
		SpawnPlacementsRegistry.register(PMS04, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pms04Entity::canSpawn);
		SpawnPlacementsRegistry.register(PMS05, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pms05Entity::canSpawn);

		CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> {
			PomkotsCommands.register(dispatcher);
		});

		registerServerUserInteraction();
		registerServerMechWorkbenchReceiver();
		registerServerTargetLock();
		registerServerMechSalvagerReceiver();
		registerServerPartsWorkbenchReceiver();
		registerServerArenaRecipientisReceiver();

		if (Platform.isFabric()) {
			LifecycleEvent.SERVER_STARTED.register(SurvivalInitActions::onServerStarted);
			PlayerEvent.PLAYER_JOIN.register(SurvivalInitActions::onPlayerJoin);
		}

		LifecycleEvent.SERVER_STARTED.register(
                ArenaManager::onServerStarted
		);

		PlayerEvent.PLAYER_JOIN.register(PomkotsMechs::sendDataPack2Player);
		PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) -> {
			if (!wonGame && !newPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
				var oldInventory = oldPlayer.getInventory();
				for (int i = 0; i < oldInventory.getContainerSize(); i++) {
					var stack = oldInventory.getItem(i);
					if (stack.is(PomkotsMechs.KEYCARD_ITEM.get()) || stack.is(PomkotsMechs.POMKOTS_RADAR_ITEM.get())) {
						newPlayer.getInventory().add(stack.copy());
					}
				}
			}
		});

		TickEvent.PLAYER_POST.register((player)->{
			if (player instanceof ServerPlayer sp && (sp.tickCount % 100) == 0) {
				ServerBGMTracker.tick(sp);
			}
		});

		TickEvent.SERVER_PRE.register(
			ArenaManager::tick
		);

		TickEvent.SERVER_POST.register(
			AssetMigrationManager::tick
		);
	}

	public static void sendDataPack2Player(ServerPlayer player) {
		try {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PomkotsDataPackManager.getInstance().serializeServerData(baos);
			baos.flush();
			baos.close();

			buf.writeByteArray(baos.toByteArray());
			NetworkManager.sendToPlayer(player, PomkotsMechs.id(PACKET_UPDATE_DATAPACK), buf);

		} catch (Exception e) {
			LOGGER.error("Failed to send datapack to client", e);
		}
	}

	public static final String PACKET_UPDATE_DATAPACK = "udp";

	public static final String PACKET_DRIVER_INPUT = "kpm";
	public static final String PACKET_LOCK_SOFT = "ls";
	public static final String PACKET_UNLOCK_SOFT = "uls";
	public static final String PACKET_LOCK_HARD = "lh";
	public static final String PACKET_UNLOCK_HARD = "ulh";
	public static final String PACKET_LOCK_MULTI = "lm";
	public static final String PACKET_LOCK_MULTI_CUSTOM = "lmc";
	public static final String PACKET_UNLOCK_MULTI = "ulm";

	public static final String PACKET_CHANGE_CUSTOM_NAME = "ccn";
	public static final String PACKET_CHANGE_TEXTURE = "ctx";
	public static final String PACKET_SECURITY_GENCARD = "sgc";
	public static final String PACKET_REPAIR_MECH = "rpm";
	public static final String PACKET_AUTO_SUPPLY = "asp";
	public static final String PACKET_MECH_CHANGE_PARTS = "mwbcp";

	public static final String PACKET_SUMMON_MECH = "smm";
	public static final String PACKET_PARTS_WKBNCH_TAB_CHANGE = "pwt";
	public static final String PACKET_PARTS_WKBNCH_CRAFT = "pwc";
	public static final String PACKET_PARTS_WKBNCH_UPGRADE = "pwu";

	public static final String PACKET_START_OPENING = "so";
	public static final String PACKET_BGM_STATE = "bs";

	public static final String PACKET_RADAR_SELECT_TARGET = "radar_select_target";

	public static final String PACKET_ARENA_SAVE_PROFILE = "aspp";
	public static final String PACKET_ARENA_REMOVE_PROFILE = "arpp";
	public static final String PACKET_ARENA_OPENING_START = "aros";
	public static final String PACKET_ARENA_OPENING_END = "aroe";

	public static final String PACKET_ARENA_REFRESH_RANKING = "arr";
	public static final String PACKET_ARENA_START_MATCH = "asm";

	public static final String PACKET_GENERAR_SCREEN_FADE = "gsf";

	public static void registerServerArenaRecipientisReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_ARENA_START_MATCH), (buf, context) -> {
			Player player = context.getPlayer();

			boolean isRankMatch = buf.readBoolean();
			String arenaId = buf.readUtf();
			UUID targetUUID = buf.readUUID();

			System.out.println(player);

			context.queue(() -> {
				ArenaManager.startRankMatch(
						(ServerPlayer)player,
						!isRankMatch,
						arenaId,
						targetUUID
				);
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_ARENA_SAVE_PROFILE), (buf, context) -> {
			Player player = context.getPlayer();
			String name = buf.readUtf();
			String comment = buf.readUtf();
			String arenaId = buf.readUtf();

			context.queue(() -> {
				if (!(player.containerMenu instanceof ArenaReceptionistMenu menu)) {
					return;
				}
				menu.savePlayerProfile(player, name, comment, arenaId);
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_ARENA_REMOVE_PROFILE), (buf, context) -> {
			Player player = context.getPlayer();
			String arenaId = buf.readUtf();

			context.queue(() -> {
				if (!(player.containerMenu instanceof ArenaReceptionistMenu menu)) {
					return;
				}
				menu.removePlayerProfile(player, arenaId);
			});
		});
	}

	public static void registerServerPartsWorkbenchReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_PARTS_WKBNCH_TAB_CHANGE), (buf, context) -> {
			Player p = context.getPlayer();
			PartsWorkbenchMenu.Tab tab = PartsWorkbenchMenu.getTab(buf.readInt());

			if (tab != null && p instanceof ServerPlayer player && player.containerMenu instanceof PartsWorkbenchMenu menu) {
				menu.setTab(tab);
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_PARTS_WKBNCH_CRAFT), (buf, context) -> {
			Player p = context.getPlayer();
			var item = buf.readById(BuiltInRegistries.ITEM);

			if (p instanceof ServerPlayer player && player.containerMenu instanceof PartsWorkbenchMenu menu) {
				menu.craftItem(item, player);
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_PARTS_WKBNCH_UPGRADE), (buf, context) -> {
			Player p = context.getPlayer();
			var item = buf.readById(BuiltInRegistries.ITEM);

			if (p instanceof ServerPlayer player && player.containerMenu instanceof PartsWorkbenchMenu menu) {
				menu.upgradeParts(player);
			}
		});
	}

	public static void registerServerUserInteraction() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_DRIVER_INPUT), (buf, context) -> {
			Player player = context.getPlayer();
			short keyPressStatus = buf.readShort();

			context.queue(() -> {
				if (Utils.isRidingPomkotsControllable(player)) {
					Entity vehicle = player.getVehicle();
					((PomkotsControllable) vehicle).setDriverInput(new DriverInput(keyPressStatus));
				}
			});
		});
	}

	public static void registerServerMechSalvagerReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_SUMMON_MECH), (buf, context) -> {
			Player p = context.getPlayer();
			var mechUuid = buf.readUUID();

			context.queue(() -> {
				if (p instanceof ServerPlayer player && player.containerMenu instanceof MechSalvagerMenu menu) {
					menu.summon(player, mechUuid);
				}
			});
		});
	}

	public static void registerServerMechWorkbenchReceiver() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_MECH_CHANGE_PARTS), (buf, context) -> {
			int containerId = buf.readInt();
			int mechSlot = buf.readInt();
			int sourceSlot = buf.readInt();
			int command = buf.readInt();

			context.queue(() -> {
				var menu = context.getPlayer().containerMenu;
				if (menu instanceof MechWorkbenchMenu wmenu && wmenu.containerId == containerId) {
					wmenu.handlePartsChange(command, mechSlot, sourceSlot);
				}
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_CHANGE_CUSTOM_NAME), (buf, context) -> {
			UUID targetEntityId = buf.readUUID();
			String customName = buf.readUtf();

			context.queue(() -> {
				var entity = ((ServerLevel) context.getPlayer().level()).getEntity(targetEntityId);

				if (entity instanceof Pmvc01Entity mech) {
					if (customName.isEmpty()) {
						mech.setCustomName(null);
					} else {
						mech.setCustomName(Component.literal(customName));
					}
				}
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_CHANGE_TEXTURE), (buf, context) -> {
			UUID targetEntityId = buf.readUUID();
			int textureColor = buf.readInt();

			context.queue(() -> {
				var entity = ((ServerLevel) context.getPlayer().level()).getEntity(targetEntityId);

				if (entity instanceof Pmvc01Entity mech) {
					mech.setTextureColor(textureColor);
				}
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_SECURITY_GENCARD), (buf, context) -> {
			UUID targetEntityId = buf.readUUID();

			context.queue(() -> {
				var entity = ((ServerLevel) context.getPlayer().level()).getEntity(targetEntityId);

				if (entity instanceof Pmvc01Entity mech) {
					ItemStack stack = new ItemStack(PomkotsMechs.KEYCARD_ITEM.get());
					if (stack.getItem() instanceof KeycardItem keycard && context.getPlayer() instanceof ServerPlayer sp) {
						keycard.setMech(stack, mech, entity.level());
						giveItemToPlayer(sp, stack);
					}
				}
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_REPAIR_MECH), (buf, context) -> {
			UUID targetEntityId = buf.readUUID();
			ServerPlayer player = (ServerPlayer) context.getPlayer();

			context.queue(() -> {
				var entity = ((ServerLevel) player.level()).getEntity(targetEntityId);
				if (entity instanceof Pmvc01Entity mech) {
					if (player.containerMenu instanceof MechWorkbenchMenu menu) {
						menu.startMechRepair(mech);
						player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.repair}"));
					}
				}
			});
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_AUTO_SUPPLY), (buf, context) -> {
			UUID targetEntityId = buf.readUUID();
			ServerPlayer player = (ServerPlayer) context.getPlayer();

			context.queue(() -> {
				var entity = ((ServerLevel) player.level()).getEntity(targetEntityId);
				if (entity instanceof Pmvc01Entity mech) {
					if (player.containerMenu instanceof MechWorkbenchMenu menu) {
						menu.autoSupplyAll();
						player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.autosupply}"));
					}
				}
			});
		});
	}

	public static boolean giveItemToPlayer(ServerPlayer player, ItemStack stack) {
		Inventory inventory = player.getInventory();

		// 空きスロット検索
		int freeSlot = inventory.getFreeSlot();

		// 空き無し
		if (freeSlot == -1) {

			player.sendSystemMessage(
					Component.literal("Inventory is full!")
			);

			return false;
		}

		// 空きスロットへセット
		inventory.setItem(freeSlot, stack);

		// インベントリ同期
		player.containerMenu.broadcastChanges();

		return true;
	}

	public static void registerServerTargetLock() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_HARD), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof PomkotsVehicle bot) {
				bot.getLockTargets().lockTargetHard(player.level().getEntity(targetEntityId));
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_UNLOCK_HARD), (buf, context) -> {
			Player player = context.getPlayer();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof PomkotsVehicle bot) {
				bot.getLockTargets().unlockTargetHard();
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_SOFT), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof PomkotsVehicle bot) {
				bot.getLockTargets().lockTargetSoft(player.level().getEntity(targetEntityId));
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_UNLOCK_SOFT), (buf, context) -> {
			Player player = context.getPlayer();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof PomkotsVehicle bot) {
				bot.getLockTargets().unlockTargetSoft();
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_MULTI), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof PomkotsVehicle bot) {
				bot.getLockTargets().lockTargetMulti(player.level().getEntity(targetEntityId));
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_UNLOCK_MULTI), (buf, context) -> {
			Player player = context.getPlayer();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof PomkotsVehicle bot) {
				bot.getLockTargets().unlockTargetMulti();
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_MULTI_CUSTOM), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();
			int slot = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmvc01Entity bot) {
				bot.getLockTargets().lockTargetMulti(player.level().getEntity(targetEntityId), slot, bot);
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S,
			PomkotsMechs.id(PACKET_RADAR_SELECT_TARGET),
			(buf, context) -> {
				int idx = buf.readInt();
				context.queue(() -> {
					ServerPlayer player = (ServerPlayer) context.getPlayer();
					if (player.containerMenu instanceof RadarTargetSelectMenu menu) {
						menu.setSelectedTarget(idx);
					}
				});
			});
	}

	public static void loadDataPack(ResourceManager manager) {
		PomkotsDataPackManager.getInstance().loadDataPack(manager);
	}

	public static void wrapModPack(Consumer<Pack> adder) {
		// MOD自身のリソースパックを取得してラップ
		Pack wrapped = Pack.readMetaAndCreate(
				"mod:" + MODID,
				Component.literal(MODID),
				true,
				path -> new EncryptedPackResources(
						new PathPackResources(path.toString(),
								getModRootPath(),
								true)
				),
				PackType.CLIENT_RESOURCES,
				Pack.Position.TOP,
				PackSource.BUILT_IN
		);
		if (wrapped != null) adder.accept(wrapped);
	}

	public static Path getModRootPath() {
		return Platform.getMod(MODID).getFilePaths().get(0);
	}
}