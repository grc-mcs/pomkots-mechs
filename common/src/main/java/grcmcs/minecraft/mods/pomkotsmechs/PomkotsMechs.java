package grcmcs.minecraft.mods.pomkotsmechs;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.block.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxLegsEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt04Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.*;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlayerDummyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.*;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.RailSliderItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.fuel.PelletItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters.HanedaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters.KansaiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters.NaritaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CircuitHardLockItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CircuitSoftLockItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.ChibaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.SagaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.ShigaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.*;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

public class PomkotsMechs {
	public static final String MODID = "pomkotsmechs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

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

	public static final RegistrySupplier<EntityType<Pmvc01Entity>> PMVC01 = registerEntityType("pmvc01", Pmvc01Entity::new, MobCategory.CREATURE, 4F, 5.5F);

	// Monster, Boss
	public static final RegistrySupplier<EntityType<Pms01Entity>> PMS01 = registerEntityType("pms01", Pms01Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Charging Mob
	public static final RegistrySupplier<EntityType<Pms02Entity>> PMS02 = registerEntityType("pms02", Pms02Entity::new, MobCategory.MONSTER, 3F, 3F); // Flying Mob
	public static final RegistrySupplier<EntityType<Pms03Entity>> PMS03 = registerEntityType("pms03", Pms03Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Gun Mob
	public static final RegistrySupplier<EntityType<Pms04Entity>> PMS04 = registerEntityType("pms04", Pms04Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Missile
	public static final RegistrySupplier<EntityType<Pms05Entity>> PMS05 = registerEntityType("pms05", Pms05Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Spider
	public static final RegistrySupplier<EntityType<Pms06Entity>> PMS06 = registerEntityType("pms06", Pms06Entity::new, MobCategory.MONSTER, 5F, 5F); // Spider
	public static final RegistrySupplier<EntityType<Pms07Entity>> PMS07 = registerEntityType("pms07", Pms07Entity::new, MobCategory.MONSTER, 5F, 5F); // Spider
	public static final RegistrySupplier<EntityType<Pms08Entity>> PMS08 = registerEntityType("pms08", Pms08Entity::new, MobCategory.MONSTER, 5F, 5F); // Spider
	public static final RegistrySupplier<EntityType<Pms09Entity>> PMS09 = registerEntityType("pms09", Pms09Entity::new, MobCategory.MONSTER, 3F, 3F); // Spider

	// Boss
	public static final RegistrySupplier<EntityType<Pmb01Entity>> PMB01 = registerEntityType("pmb01", Pmb01Entity::new, MobCategory.MONSTER, 0.7F, 19F);
	public static final RegistrySupplier<EntityType<Pmb01mk2Entity>> PMB01MK2 = registerEntityType("pmb01mk2", Pmb01mk2Entity::new, MobCategory.MONSTER, 3F, 16F);

	public static final RegistrySupplier<EntityType<Pmb02Entity>> PMB02 = registerEntityType("pmb02", Pmb02Entity::new, MobCategory.MONSTER, 3F, 7F);
	public static final RegistrySupplier<EntityType<Pmb03Entity>> PMB03 = registerEntityType("pmb03", Pmb03Entity::new, MobCategory.MONSTER, 3F, 16F);
	public static final RegistrySupplier<EntityType<Pmb04Entity>> PMB04 = registerEntityType("pmb04", Pmb04Entity::new, MobCategory.MONSTER, 3F, 16F);
	public static final RegistrySupplier<EntityType<Pmb05Entity>> PMB05 = registerEntityType("pmb05", Pmb05Entity::new, MobCategory.MONSTER, 3F, 24F);
	public static final RegistrySupplier<EntityType<Pmb06Entity>> PMB06 = registerEntityType("pmb06", Pmb06Entity::new, MobCategory.MONSTER, 3F, 24F);

	public static final RegistrySupplier<EntityType<EarthbreakEntity>> EARTHBREAK2 = registerEntityType("earthbreak2", EarthbreakEntity::new, MobCategory.MISC, 60F, 0.1F);

	// Turrets
	public static final RegistrySupplier<EntityType<Pmt01Entity>> PMT01 = registerEntityType("pmt01", Pmt01Entity::new, MobCategory.MONSTER, 3F, 3F);
	public static final RegistrySupplier<EntityType<Pmt02Entity>> PMT02 = registerEntityType("pmt02", Pmt02Entity::new, MobCategory.MONSTER, 3F, 3F);
	public static final RegistrySupplier<EntityType<Pmt03Entity>> PMT03 = registerEntityType("pmt03", Pmt03Entity::new, MobCategory.MONSTER, 3F, 3F);
	public static final RegistrySupplier<EntityType<Pmt04Entity>> PMT04 = registerEntityType("pmt04", Pmt04Entity::new, MobCategory.MONSTER, 6F, 1F);

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

	// Projectile for custom
	public static final RegistrySupplier<EntityType<BulletRifleEntity>> BULLET_RIFLE = registerEntityType("bulletrifle", BulletRifleEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletMachineEntity>> BULLET_MACHINE = registerEntityType("bulletmachine", BulletMachineEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletMachineEntity>> BULLET_MACHINE_LARGE = registerEntityType("bulletmachinelarge", BulletMachineEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletGrenadeEntity>> BULLET_GRENADE = registerEntityType("bulletgrenade", BulletGrenadeEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletBeamEntity>> BULLET_BEAM = registerEntityType("bulletbeam", BulletBeamEntity::new, MobCategory.MISC, 2F, 2F);

	// Other
	public static final RegistrySupplier<EntityType<PlayerDummyEntity>> PLAYERDUMMY = registerEntityType("playerdummy", PlayerDummyEntity::new, MobCategory.CREATURE, 1F, 2F);

	public static final RegistrySupplier<EntityType<ExplosionEntity>> EXPLOSION = registerEntityType("explosion", ExplosionEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<EarthbreakEntity>> EARTHBREAK = registerEntityType("earthbreak", EarthbreakEntity::new, MobCategory.MISC, 60F, 4F);
	public static final RegistrySupplier<EntityType<EarthraiseEntity>> EARTHRAISE = registerEntityType("earthraise", EarthraiseEntity::new, MobCategory.MISC, 10F, 8F);
	public static final RegistrySupplier<EntityType<SlashEntity>> EXPLOADSLASH = registerEntityType("exploadslash", SlashEntity::new, MobCategory.MISC, 4F, 5F);

	public static final RegistrySupplier<EntityType<HitBoxEntity>> HITBOX1 = registerEntityType("hitbox1", HitBoxEntity::new, MobCategory.MISC, 10F, 8F);
	public static final RegistrySupplier<EntityType<HitBoxLegsEntity>> HITBOX2 = registerEntityType("hitbox2", HitBoxLegsEntity::new, MobCategory.MISC, 10F, 9F);

	public static final RegistrySupplier<EntityType<BossHitBoxEntity>> HITBOX_PMB02 = registerEntityType("hitbox_pmb02", BossHitBoxEntity::new, MobCategory.MISC, 8F, 8F);

	public static final RegistrySupplier<EntityType<BossHitBoxEntity>> HITBOX_PMB03 = registerEntityType("hitbox_pmb03", BossHitBoxEntity::new, MobCategory.MISC, 12F, 16F);


	public static final RegistrySupplier<EntityType<KujiraEntity>> KUJIRA = registerEntityType("kujira", KujiraEntity::new, MobCategory.MISC, 30F, 30F);

	public static final RegistrySupplier<EntityType<AlertEntity>> ALERT = registerEntityType("alert", AlertEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<AlertRedEntity>> ALERTRED = registerEntityType("alertred", AlertRedEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<BossBoxEntity>> BOSSBOX = registerEntityType("bossbox", BossBoxEntity::new, MobCategory.MISC, 1F, 1F);

	private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
		return ENTITIES.register(name, () ->
				EntityType.Builder.of(factory, category)
						.sized(width, height)
						.build(id(name).toString()));
	}

	// Blocks -------------------------------------------------------------------------------------------

	public static final DeferredRegister<Block> BLOCKS =  DeferredRegister.create(MODID, Registries.BLOCK);
	public static final RegistrySupplier<Block> MECH_WORKBENCH_BLOCK = BLOCKS.register("mechworkbench", ()-> new MechWorkbenchBlock());
	public static final RegistrySupplier<Block> POMKOTS_CUBE_BLOCK = BLOCKS.register("pomkotscube", ()-> new PomkotsCubeBlock());
	public static final RegistrySupplier<Block> EXCHANGE_BLOCK = BLOCKS.register("exchange", ()-> new ExchangeBlock());
	public static final RegistrySupplier<Block> CASK_BLOCK = BLOCKS.register("cask", ()-> new CaskBlock());

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =  DeferredRegister.create(MODID, Registries.BLOCK_ENTITY_TYPE);
	public static final RegistrySupplier<BlockEntityType<MechWorkbenchBlockEntity>> MECH_WORKBENCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("mechworkbenchentity", () -> BlockEntityType.Builder.of(MechWorkbenchBlockEntity::new, MECH_WORKBENCH_BLOCK.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<PomkotsCubeBlockEntity>> POMKOTS_CUBE_BLOCK_ENTITY = BLOCK_ENTITIES.register("pomkotscubeentity", () -> BlockEntityType.Builder.of(PomkotsCubeBlockEntity::new, POMKOTS_CUBE_BLOCK.get()).build(null));

	// PARTICLES -------------------------------------------------------------------------------------------

	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(MODID, Registries.PARTICLE_TYPE);

	public static final RegistrySupplier<SimpleParticleType> FIRE = PARTICLES.register("fire", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> MISSILE_SMOKE = PARTICLES.register("missilesmoke", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> EXPLOSION_CORE = PARTICLES.register("explosioncore", () -> new PomkotsSimpleParticleType(false));
	public static final RegistrySupplier<SimpleParticleType> SPARK = PARTICLES.register("spark", () -> new PomkotsSimpleParticleType(false));

	public static class PomkotsSimpleParticleType extends SimpleParticleType {
		protected PomkotsSimpleParticleType(boolean bl) {
			super(bl);
		}
	}

	// ITEMS ---------------------------------------------------------------------------------------------------

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MODID, Registries.ITEM);

	public static final RegistrySupplier<Item> CORESTONE_PMV01 = ITEMS.register("corestone_pmv01", () -> new CoreStonePMV01Item(new Item.Properties()));
	public static final RegistrySupplier<Item> CORESTONE_PMV01B = ITEMS.register("corestone_pmv01b", () -> new CoreStonePMV01BItem(new Item.Properties()));
	public static final RegistrySupplier<Item> CORESTONE_PMV02 = ITEMS.register("corestone_pmv02", () -> new CoreStonePMV02Item(new Item.Properties()));
	public static final RegistrySupplier<Item> CORESTONE_PMV03 = ITEMS.register("corestone_pmv03", () -> new CoreStonePMV03Item(new Item.Properties()));

	public static final RegistrySupplier<Item> CORESTONE_PMB01 = ITEMS.register("corestone_pmb01", () -> new CoreStonePMB01Item(new Item.Properties()));

	// ITEMS
	public static final RegistrySupplier<Item> PMB01_SPAWN_EGG = ITEMS.register("pmb01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB01, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB01MK2_SPAWN_EGG = ITEMS.register("pmb01mk2_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB01MK2, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB02_SPAWN_EGG = ITEMS.register("pmb02_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB02, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB03_SPAWN_EGG = ITEMS.register("pmb03_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB03, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB04_SPAWN_EGG = ITEMS.register("pmb04_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB04, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB05_SPAWN_EGG = ITEMS.register("pmb05_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB05, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMB06_SPAWN_EGG = ITEMS.register("pmb06_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB06, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> PMS01_SPAWN_EGG = ITEMS.register("pms01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS01, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS02_SPAWN_EGG = ITEMS.register("pms02_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS02, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS03_SPAWN_EGG = ITEMS.register("pms03_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS03, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS04_SPAWN_EGG = ITEMS.register("pms04_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS04, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS05_SPAWN_EGG = ITEMS.register("pms05_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS05, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS06_SPAWN_EGG = ITEMS.register("pms06_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS06, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS07_SPAWN_EGG = ITEMS.register("pms07_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS07, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS08_SPAWN_EGG = ITEMS.register("pms08_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS08, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS09_SPAWN_EGG = ITEMS.register("pms09_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS09, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> CORESTONE_PMVC01 = ITEMS.register("corestone_pmvc01", () -> new CoreStonePMVC01Item(new Item.Properties()));

	public static final RegistrySupplier<Item> MECH_WORKBENCH_BLOCK_ITEM = ITEMS.register("mechworkbench_block_item", () -> new BlockItem(MECH_WORKBENCH_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POMKOTS_CUBE_BLOCK_ITEM = ITEMS.register("pomkotscube", () -> new BlockItem(POMKOTS_CUBE_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> EXCHANGE_BLOCK_ITEM = ITEMS.register("exchange", () -> new BlockItem(EXCHANGE_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> CASK_BLOCK_ITEM = ITEMS.register("cask", () -> new BlockItem(CASK_BLOCK.get(), new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> WRENCH_ITEM = ITEMS.register("pomkots_wrench", () -> new PomkotsWrenchItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> P_TITANIUM_INGOT = ITEMS.register("p_titanium_ingot", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> P_TITANIUM_NUGGET = ITEMS.register("p_titanium_nugget", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> LARGE_STEEL_PLATE = ITEMS.register("large_steel_plate", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> POM_COIN = ITEMS.register("pom_coin", () -> new PTitaniumItem(new Item.Properties().stacksTo(64)));

	// PARTS

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

	// WEAPONS

	public static final RegistrySupplier<Item> SHAKUJI_WEAPON = ITEMS.register("shakuji", () -> new ShakujiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SHINOBAZU_WEAPON = ITEMS.register("shinobazu", () -> new ShinobazuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SENZOKU_WEAPON = ITEMS.register("senzoku", () -> new SenzokuItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> KASUMI_WEAPON = ITEMS.register("kasumi", () -> new KasumiItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BIWA_WEAPON = ITEMS.register("biwa", () -> new BiwaItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> KAWASEMI_WEAPON = ITEMS.register("kawasemi", () -> new KawasemiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> NOSURI_WEAPON = ITEMS.register("nosuri", () -> new NosuriItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MUKUDORI_WEAPON = ITEMS.register("mukudori", () -> new MukudoriItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> UGUISU_WEAPON = ITEMS.register("uguisu", () -> new UguisuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> DODO_WEAPON = ITEMS.register("dodo", () -> new DodoItem(new Item.Properties().stacksTo(1)));

	// melee
	public static final RegistrySupplier<Item> JINBA_WEAPON = ITEMS.register("jinba", () -> new JinbaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> TAKAO_WEAPON = ITEMS.register("takao", () -> new TakaoItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> KAGENOBU_WEAPON = ITEMS.register("kagenobu", () -> new KagenobuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> TSURUGI_WEAPON = ITEMS.register("tsurugi", () -> new TsurugiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> SUWA_WEAPON = ITEMS.register("suwa", () -> new SuwaItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> KAGAMI_WEAPON = ITEMS.register("kagami", () -> new KagamiItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> MASHU_WEAPON = ITEMS.register("mashu", () -> new MashuItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> GASSAN_WEAPON = ITEMS.register("gassan", () -> new GassanItem(new Item.Properties().stacksTo(1)));

	// MAGAZINES
	public static final RegistrySupplier<Item> RIFLE_MAGAZINE = ITEMS.register("magazinerifle", () -> new MagazineRifleItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> MACHINE_GUN_MAGAZINE = ITEMS.register("magazinemachinegun", () -> new MagazineMachineGunItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> MISSILE_MAGAZINE = ITEMS.register("magazinemissile", () -> new MagazineMissileItem(new Item.Properties().stacksTo(12)));
	public static final RegistrySupplier<Item> MISSILE_LARGE_MAGAZINE = ITEMS.register("magazinemissilelarge", () -> new MagazineMissileLargeItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> GRENADE_MAGAZINE = ITEMS.register("magazinegrenade", () -> new MagazineGrenadeItem(new Item.Properties().stacksTo(12)));
	public static final RegistrySupplier<Item> GATLING_MAGAZINE = ITEMS.register("magazinegatling", () -> new MagazineGatlingItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> SHOTGUN_MAGAZINE = ITEMS.register("magazineshotgun", () -> new MagazineShotGunItem(new Item.Properties().stacksTo(32)));

	public static final RegistrySupplier<Item> PELLET = ITEMS.register("pellet", () -> new PelletItem(new Item.Properties().stacksTo(64)));

	public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(MODID, Registries.CREATIVE_MODE_TAB);
	public static final RegistrySupplier<CreativeModeTab> BASE_TAB = ITEM_GROUPS.register(PomkotsMechs.id("item_group"), () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(MECH_WORKBENCH_BLOCK_ITEM.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID))
			.displayItems((parameters, output) -> {
				output.accept(new ItemStack(MECH_WORKBENCH_BLOCK_ITEM.get()));
				output.accept(new ItemStack(POMKOTS_CUBE_BLOCK_ITEM.get()));
				output.accept(new ItemStack(EXCHANGE_BLOCK_ITEM.get()));
				output.accept(new ItemStack(CASK_BLOCK_ITEM.get()));
				output.accept(new ItemStack(WRENCH_ITEM.get()));
				output.accept(new ItemStack(P_TITANIUM_INGOT.get()));
				output.accept(new ItemStack(P_TITANIUM_NUGGET.get()));
				output.accept(new ItemStack(LARGE_STEEL_PLATE.get()));
				output.accept(new ItemStack(POM_COIN.get()));

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

				output.accept(new ItemStack(PMS01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS02_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS03_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS04_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS05_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS06_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS07_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS08_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS09_SPAWN_EGG.get()));

				output.accept(new ItemStack(CORESTONE_PMVC01.get()));
			})
			.build()
	);

	public static final RegistrySupplier<CreativeModeTab> PARTS_TAB =
			ITEM_GROUPS.register(PomkotsMechs.id("item_group_parts"),
			() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(ALTAIR_BODY.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".parts"))
			.displayItems((parameters, output) -> {
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

				output.accept(new ItemStack(PELLET.get()));
			})
			.build()
	);

	public static final RegistrySupplier<CreativeModeTab> WEAPON_TAB =
			ITEM_GROUPS.register(PomkotsMechs.id("item_group_weapons"),
					() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
							.icon(() -> new ItemStack(SHAKUJI_WEAPON.get()))
							.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".weapons"))
							.displayItems((parameters, output) -> {
								output.accept(new ItemStack(SHAKUJI_WEAPON.get()));
								output.accept(new ItemStack(SHINOBAZU_WEAPON.get()));
								output.accept(new ItemStack(SENZOKU_WEAPON.get()));
								output.accept(new ItemStack(KASUMI_WEAPON.get()));
								output.accept(new ItemStack(KAGAMI_WEAPON.get()));
								output.accept(new ItemStack(UGUISU_WEAPON.get()));
								output.accept(new ItemStack(MASHU_WEAPON.get()));

								output.accept(new ItemStack(TSURUGI_WEAPON.get()));
								output.accept(new ItemStack(KAGENOBU_WEAPON.get()));
								output.accept(new ItemStack(TAKAO_WEAPON.get()));
								output.accept(new ItemStack(JINBA_WEAPON.get()));

								output.accept(new ItemStack(BIWA_WEAPON.get()));
								output.accept(new ItemStack(SUWA_WEAPON.get()));

								output.accept(new ItemStack(KAWASEMI_WEAPON.get()));
								output.accept(new ItemStack(NOSURI_WEAPON.get()));
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
	public static final RegistrySupplier<Item> BLUE_PRINT_KAGENOBU = ITEMS.register("blue_print_kagenobu", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_TAKAO = ITEMS.register("blue_print_takao", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_JINBA = ITEMS.register("blue_print_jinba", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_BIWA = ITEMS.register("blue_print_biwa", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_SUWA = ITEMS.register("blue_print_suwa", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<Item> BLUE_PRINT_KAWASEMI = ITEMS.register("blue_print_kawasemi", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_NOSURI = ITEMS.register("blue_print_nosuri", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_MUKUDORI = ITEMS.register("blue_print_mukudori", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> BLUE_PRINT_DODO = ITEMS.register("blue_print_dodo", () -> new BluePrintItem(new Item.Properties().stacksTo(1)));

	public static final RegistrySupplier<CreativeModeTab> SHEET_TAB =
			ITEM_GROUPS.register(PomkotsMechs.id("item_group_sheets"),
					() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
							.icon(() -> new ItemStack(BLUE_PRINT_DENEB.get()))
							.title(Component.translatable("itemGroup." + PomkotsMechs.MODID + ".sheets"))
							.displayItems((parameters, output) -> {
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

								output.accept(new ItemStack(BLUE_PRINT_DENEB.get()));
								output.accept(new ItemStack(BLUE_PRINT_ALTAIR.get()));
								output.accept(new ItemStack(BLUE_PRINT_VEGA.get()));
								output.accept(new ItemStack(BLUE_PRINT_SIRIUS.get()));
								output.accept(new ItemStack(BLUE_PRINT_ALDEBARAN.get()));

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
								output.accept(new ItemStack(BLUE_PRINT_KAGENOBU.get()));
								output.accept(new ItemStack(BLUE_PRINT_TAKAO.get()));
								output.accept(new ItemStack(BLUE_PRINT_JINBA.get()));

								output.accept(new ItemStack(BLUE_PRINT_BIWA.get()));
								output.accept(new ItemStack(BLUE_PRINT_SUWA.get()));

								output.accept(new ItemStack(BLUE_PRINT_KAWASEMI.get()));
								output.accept(new ItemStack(BLUE_PRINT_NOSURI.get()));
								output.accept(new ItemStack(BLUE_PRINT_MUKUDORI.get()));
								output.accept(new ItemStack(BLUE_PRINT_DODO.get()));
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

	public static PomkotsConfig CONFIG;

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(PomkotsMechs.MODID, Registries.MENU);
	public static final RegistrySupplier<MenuType<MechWorkbenchMenu>> MECH_WORKBENCH_GUI = MENUS.register(
			"mechworkbench_gui",
			() -> new MenuType<>(MechWorkbenchMenu::new, FeatureFlagSet.of())
	);

	public static void initialize() {
		AutoConfig.register(PomkotsConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(PomkotsConfig.class).getConfig();

		ENTITIES.register();

		EntityAttributeRegistry.register(PMV01::get, Pmv01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMV01B::get, Pmv01bEntity::createMobAttributes);
		EntityAttributeRegistry.register(PMV02::get, Pmv02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMV03P::get, Pmv03pEntity::createMobAttributes);
		EntityAttributeRegistry.register(PMV03::get, Pmv03Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMVC01::get, Pmvc01Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMB01::get, Pmb01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB01MK2::get, Pmb01mk2Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB02::get, Pmb02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB03::get, Pmb03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB04::get, Pmb04Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB05::get, Pmb05Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB06::get, Pmb06Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMS01::get, Pms01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS02::get, Pms02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS03::get, Pms03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS04::get, Pms04Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS05::get, Pms05Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS06::get, Pms06Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS07::get, Pms07Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS08::get, Pms08Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS09::get, Pms09Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMT01::get, Pmt01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMT02::get, Pmt02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMT03::get, Pmt03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMT04::get, Pmt04Entity::createMobAttributes);

		EntityAttributeRegistry.register(PLAYERDUMMY::get, PlayerDummyEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX1::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX2::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX_PMB02::get, BossHitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX_PMB03::get, BossHitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(BLOCK_MASS::get, BlockMassEntity::createMobAttributes);
		EntityAttributeRegistry.register(PRESENT_BOX::get, PresentBoxEntity::createMobAttributes);

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

		registerServerUserInteraction();
		registerServerChangeTexture();
		registerServerTargetLock();

		PlayerEvent.PLAYER_JOIN.register(PomkotsMechs::sendDataPack2Player);
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

	public static final String PACKET_DRIVER_INPUT = "kpm";
	public static final String PACKET_LOCK_SOFT = "ls";
	public static final String PACKET_UNLOCK_SOFT = "uls";
	public static final String PACKET_LOCK_HARD = "lh";
	public static final String PACKET_UNLOCK_HARD = "ulh";
	public static final String PACKET_LOCK_MULTI = "lm";
	public static final String PACKET_LOCK_MULTI_CUSTOM = "lmc";
	public static final String PACKET_UNLOCK_MULTI = "ulm";
	public static final String PACKET_CHANGE_TEXTURE = "ctx";
	public static final String PACKET_UPDATE_DATAPACK = "udp";

	public static void registerServerUserInteraction() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_DRIVER_INPUT), (buf, context) -> {
			Player player = context.getPlayer();

			short keyPressStatus = buf.readShort();

			if (Utils.isRidingPomkotsControllable(player)) {
				Entity vehicle = player.getVehicle();
				((PomkotsControllable)vehicle).setDriverInput(new DriverInput(keyPressStatus));
			}
		});
	}

	public static void registerServerChangeTexture() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_CHANGE_TEXTURE), (buf, context) -> {
			int targetEntityId = buf.readInt();
			int textureColor = buf.readInt();

			var entity = context.getPlayer().level().getEntity(targetEntityId);

			if (entity instanceof Pmvc01Entity mech) {
				mech.setTextureColor(textureColor);
			}
		});
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
	}

	public static void loadDataPack(ResourceManager manager) {
		PomkotsDataPackManager.getInstance().loadDataPack(manager);
	}
}