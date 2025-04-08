package grcmcs.minecraft.mods.pomkotsmechs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandPerformEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.*;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlayerDummyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.*;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
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
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
	public static final RegistrySupplier<EntityType<Pmv03Entity>> PMV03 = registerEntityType("pmv03", Pmv03Entity::new, MobCategory.CREATURE, 4F, 7F);

	public static final RegistrySupplier<EntityType<Pmvc01Entity>> PMVC01 = registerEntityType("pmvc01", Pmvc01Entity::new, MobCategory.CREATURE, 4F, 5.5F);

	// Monster, Boss
	public static final RegistrySupplier<EntityType<Pms01Entity>> PMS01 = registerEntityType("pms01", Pms01Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Charging Mob
	public static final RegistrySupplier<EntityType<Pms02Entity>> PMS02 = registerEntityType("pms02", Pms02Entity::new, MobCategory.MONSTER, 3F, 3F); // Flying Mob
	public static final RegistrySupplier<EntityType<Pms03Entity>> PMS03 = registerEntityType("pms03", Pms03Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Gun Mob
	public static final RegistrySupplier<EntityType<Pms04Entity>> PMS04 = registerEntityType("pms04", Pms04Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Missile
	public static final RegistrySupplier<EntityType<Pms05Entity>> PMS05 = registerEntityType("pms05", Pms05Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Spider

	// Boss
	public static final RegistrySupplier<EntityType<Pmb01Entity>> PMB01 = registerEntityType("pmb01", Pmb01Entity::new, MobCategory.MONSTER, 0.7F, 19F);
	public static final RegistrySupplier<EntityType<Pmb02Entity>> PMB02 = registerEntityType("pmb02", Pmb02Entity::new, MobCategory.MONSTER, 3F, 3F);

	public static final RegistrySupplier<EntityType<EarthbreakEntity>> EARTHBREAK2 = registerEntityType("earthbreak2", EarthbreakEntity::new, MobCategory.MISC, 60F, 0.1F);

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
	public static final RegistrySupplier<EntityType<MissileEnemyLargeEntity>>  MISSILE_ENEMY_LARGE = registerEntityType("missileenemylarge", MissileEnemyLargeEntity::new, MobCategory.MISC, 2F, 2F);

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

	public static final RegistrySupplier<EntityType<KujiraEntity>> KUJIRA = registerEntityType("kujira", KujiraEntity::new, MobCategory.MISC, 30F, 30F);

	private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityType(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
		return ENTITIES.register(name, () ->
				EntityType.Builder.of(factory, category)
						.sized(width, height)
						.build(id(name).toString()));
	}

	// Blocks -------------------------------------------------------------------------------------------

	public static final DeferredRegister<Block> BLOCKS =  DeferredRegister.create(MODID, Registries.BLOCK);
	public static final RegistrySupplier<Block> MECH_WORKBENCH_BLOCK = BLOCKS.register("mechworkbench", ()-> new MechWorkbenchBlock());

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =  DeferredRegister.create(MODID, Registries.BLOCK_ENTITY_TYPE);
	public static final RegistrySupplier<BlockEntityType<MechWorkbenchBlockEntity>> MECH_WORKBENCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("mechworkbenchentity", () -> BlockEntityType.Builder.of(MechWorkbenchBlockEntity::new, MECH_WORKBENCH_BLOCK.get()).build(null));

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

	public static final RegistrySupplier<Item> CORESTONE_PMB01 = ITEMS.register("corestone_pmb01", () -> new CoreStonePMB01Item(new Item.Properties()));

	// ITEMS
	public static final RegistrySupplier<Item> PMB01_SPAWN_EGG = ITEMS.register("pmb01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB01, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS01_SPAWN_EGG = ITEMS.register("pms01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS01, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS02_SPAWN_EGG = ITEMS.register("pms02_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS02, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS03_SPAWN_EGG = ITEMS.register("pms03_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS03, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS04_SPAWN_EGG = ITEMS.register("pms04_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS04, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS05_SPAWN_EGG = ITEMS.register("pms05_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS05, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));

	public static final RegistrySupplier<Item> CORESTONE_PMVC01 = ITEMS.register("corestone_pmvc01", () -> new CoreStonePMVC01Item(new Item.Properties()));

	public static final RegistrySupplier<Item> MECH_WORKBENCH_BLOCK_ITEM = ITEMS.register("mechworkbench_block_item", () -> new BlockItem(MECH_WORKBENCH_BLOCK.get(), new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> WRENCH_ITEM = ITEMS.register("pomkots_wrench", () -> new PomkotsWrenchItem(new Item.Properties().stacksTo(1)));

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
	public static final RegistrySupplier<Item> MACHINE_GUN_MAGAZINE = ITEMS.register("magazinemachinegun", () -> new MagazineMachineGunItem(new Item.Properties().stacksTo(24)));
	public static final RegistrySupplier<Item> MISSILE_MAGAZINE = ITEMS.register("magazinemissile", () -> new MagazineMissileItem(new Item.Properties().stacksTo(12)));
	public static final RegistrySupplier<Item> MISSILE_LARGE_MAGAZINE = ITEMS.register("magazinemissilelarge", () -> new MagazineMissileLargeItem(new Item.Properties().stacksTo(1)));
	public static final RegistrySupplier<Item> GRENADE_MAGAZINE = ITEMS.register("magazinegrenade", () -> new MagazineGrenadeItem(new Item.Properties().stacksTo(12)));
	public static final RegistrySupplier<Item> GATLING_MAGAZINE = ITEMS.register("magazinegatling", () -> new MagazineGatlingItem(new Item.Properties().stacksTo(32)));
	public static final RegistrySupplier<Item> SHOTGUN_MAGAZINE = ITEMS.register("magazineshotgun", () -> new MagazineShotGunItem(new Item.Properties().stacksTo(32)));

	public static final RegistrySupplier<Item> PELLET = ITEMS.register("pellet", () -> new PelletItem(new Item.Properties().stacksTo(64)));

	public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(MODID, Registries.CREATIVE_MODE_TAB);
	public static final RegistrySupplier<CreativeModeTab> BASE_TAB = ITEM_GROUPS.register(PomkotsMechs.id("item_group"), () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(PMB01_SPAWN_EGG.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID))
			.displayItems((parameters, output) -> {
				output.accept(new ItemStack(CORESTONE_PMV01.get()));
				output.accept(new ItemStack(CORESTONE_PMV01B.get()));
				output.accept(new ItemStack(CORESTONE_PMV02.get()));
				output.accept(new ItemStack(CORESTONE_PMB01.get()));
				output.accept(new ItemStack(PMB01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS02_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS03_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS04_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS05_SPAWN_EGG.get()));
				output.accept(new ItemStack(CORESTONE_PMVC01.get()));
				output.accept(new ItemStack(MECH_WORKBENCH_BLOCK_ITEM.get()));
				output.accept(new ItemStack(WRENCH_ITEM.get()));
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

				output.accept(new ItemStack(CHIBA_GENERATOR.get()));
				output.accept(new ItemStack(SHIGA_GENERATOR.get()));
				output.accept(new ItemStack(SAGA_GENERATOR.get()));

				output.accept(new ItemStack(HANEDA_BOOSTER.get()));
				output.accept(new ItemStack(NARITA_BOOSTER.get()));
				output.accept(new ItemStack(KANSAI_BOOSTER.get()));

				output.accept(new ItemStack(SOFT_LOCK_CIRCUIT.get()));
				output.accept(new ItemStack(HARD_LOCK_CIRCUIT.get()));

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
		EntityAttributeRegistry.register(PMB02::get, Pmb02Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMS01::get, Pms01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS02::get, Pms02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS03::get, Pms03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS04::get, Pms04Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS05::get, Pms05Entity::createMobAttributes);

		EntityAttributeRegistry.register(PLAYERDUMMY::get, PlayerDummyEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX1::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX2::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(BLOCK_MASS::get, BlockMassEntity::createMobAttributes);
		EntityAttributeRegistry.register(PRESENT_BOX::get, PresentBoxEntity::createMobAttributes);

		BLOCKS.register();
		BLOCK_ENTITIES.register();
		PARTICLES.register();
		ITEMS.register();
		ITEM_GROUPS.register();
		SOUNDS.register();
		MENUS.register();

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