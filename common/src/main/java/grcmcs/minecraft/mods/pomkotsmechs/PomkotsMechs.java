package grcmcs.minecraft.mods.pomkotsmechs;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss.HitBoxLegsEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01bEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.controller.PlayerDummyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.CoreStonePMB01Item;
import grcmcs.minecraft.mods.pomkotsmechs.items.CoreStonePMV01BItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.CoreStonePMV01Item;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomkotsMechs {
	public static final String MODID = "pomkotsmechs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	// ENTITIES -------------------------------------------------------------------------------------------

	public static  final  DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(MODID, Registries.ENTITY_TYPE);

	// Vehicles
	public static final RegistrySupplier<EntityType<Pmv01Entity>> PMV01 = registerEntityType("pmv01", Pmv01Entity::new, MobCategory.CREATURE, 3F, 4F);
	public static final RegistrySupplier<EntityType<Pmv01bEntity>> PMV01B = registerEntityType("pmv01b", Pmv01bEntity::new, MobCategory.CREATURE, 3F, 4F);

	// Monster, Boss
	public static final RegistrySupplier<EntityType<Pms01Entity>> PMS01 = registerEntityType("pms01", Pms01Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Charging Mob
	public static final RegistrySupplier<EntityType<Pms02Entity>> PMS02 = registerEntityType("pms02", Pms02Entity::new, MobCategory.MONSTER, 3F, 3F); // Flying Mob
	public static final RegistrySupplier<EntityType<Pms03Entity>> PMS03 = registerEntityType("pms03", Pms03Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Gun Mob
	public static final RegistrySupplier<EntityType<Pms04Entity>> PMS04 = registerEntityType("pms04", Pms04Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Missile
	public static final RegistrySupplier<EntityType<Pms05Entity>> PMS05 = registerEntityType("pms05", Pms05Entity::new, MobCategory.MONSTER, 0.9F, 3F); // Spider

	// Boss
	public static final RegistrySupplier<EntityType<Pmb01Entity>> PMB01 = registerEntityType("pmb01", Pmb01Entity::new, MobCategory.MONSTER, 0.7F, 19F);
	public static final RegistrySupplier<EntityType<Pmb02Entity>> PMB02 = registerEntityType("pmb02", Pmb02Entity::new, MobCategory.MONSTER, 3F, 3F);

	public static final RegistrySupplier<EntityType<NoukinSkeltonEntity>> NS01 = registerEntityType("noukinskelton", NoukinSkeltonEntity::new, MobCategory.CREATURE, 0.5F, 3F);

	// Projectile
	public static final RegistrySupplier<EntityType<BulletEntity>> BULLET = registerEntityType("bullet", BulletEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<BulletMiddleEntity>> BULLETMIDDLE = registerEntityType("bulletmiddle", BulletMiddleEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<GrenadeEntity>> GRENADE = registerEntityType("grenade", GrenadeEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<GrenadeLargeEntity>> GRENADELARGE = registerEntityType("grenadelarge", GrenadeLargeEntity::new, MobCategory.MISC, 2F, 2F);
	public static final RegistrySupplier<EntityType<MissileVerticalEntity>> MISSILE_VERTICAL = registerEntityType("missilevertical", MissileVerticalEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<MissileEnemyEntity>> MISSILE_ENEMY = registerEntityType("missileenemy", MissileEnemyEntity::new, MobCategory.MISC, 1F, 1F);
	public static final RegistrySupplier<EntityType<MissileEnemyLargeEntity>>  MISSILE_ENEMY_LARGE = registerEntityType("missileenemylarge", MissileEnemyLargeEntity::new, MobCategory.MISC, 2F, 2F);

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
	public static final RegistrySupplier<Item> CORESTONE_PMB01 = ITEMS.register("corestone_pmb01", () -> new CoreStonePMB01Item(new Item.Properties()));

	// ITEMS
	public static final RegistrySupplier<Item> PMB01_SPAWN_EGG = ITEMS.register("pmb01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMB01, 0x111111, 0xFF0000, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS01_SPAWN_EGG = ITEMS.register("pms01_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS01, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS02_SPAWN_EGG = ITEMS.register("pms02_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS02, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS03_SPAWN_EGG = ITEMS.register("pms03_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS03, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS04_SPAWN_EGG = ITEMS.register("pms04_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS04, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));
	public static final RegistrySupplier<Item> PMS05_SPAWN_EGG = ITEMS.register("pms05_spawn_egg", () -> new ArchitecturySpawnEggItem(PomkotsMechs.PMS05, 0x111111, 0x555555, new Item.Properties().stacksTo(64)));

	public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(MODID, Registries.CREATIVE_MODE_TAB);
	public static final RegistrySupplier<CreativeModeTab> CUSTOM_TAB = ITEM_GROUPS.register(PomkotsMechs.id("item_group"), () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, -1)
			.icon(() -> new ItemStack(PMB01_SPAWN_EGG.get()))
			.title(Component.translatable("itemGroup." + PomkotsMechs.MODID))
			.displayItems((parameters, output) -> {
				output.accept(new ItemStack(CORESTONE_PMV01.get()));
				output.accept(new ItemStack(CORESTONE_PMV01B.get()));
				output.accept(new ItemStack(CORESTONE_PMB01.get()));
				output.accept(new ItemStack(PMB01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS01_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS02_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS03_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS04_SPAWN_EGG.get()));
				output.accept(new ItemStack(PMS05_SPAWN_EGG.get()));
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

	public static PomkotsConfig CONFIG;

	public static void initialize() {
		AutoConfig.register(PomkotsConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(PomkotsConfig.class).getConfig();

		ENTITIES.register();

		EntityAttributeRegistry.register(PMV01::get, Pmv01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMV01B::get, Pmv01bEntity::createMobAttributes);

		EntityAttributeRegistry.register(PMB01::get, Pmb01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMB02::get, Pmb02Entity::createMobAttributes);

		EntityAttributeRegistry.register(PMS01::get, Pms01Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS02::get, Pms02Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS03::get, Pms03Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS04::get, Pms04Entity::createMobAttributes);
		EntityAttributeRegistry.register(PMS05::get, Pms05Entity::createMobAttributes);

		EntityAttributeRegistry.register(NS01::get, NoukinSkeltonEntity::createMobAttributes);

		EntityAttributeRegistry.register(PLAYERDUMMY::get, PlayerDummyEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX1::get, HitBoxEntity::createMobAttributes);
		EntityAttributeRegistry.register(HITBOX2::get, HitBoxEntity::createMobAttributes);

		PARTICLES.register();
		ITEMS.register();
		ITEM_GROUPS.register();
		SOUNDS.register();

		registerServerUserInteraction();
		registerServerTargetLock();
	}

	public static final String PACKET_DRIVER_INPUT = "kpm";
	public static final String PACKET_LOCK_SOFT = "ls";
	public static final String PACKET_UNLOCK_SOFT = "uls";
	public static final String PACKET_LOCK_HARD = "lh";
	public static final String PACKET_UNLOCK_HARD = "ulh";
	public static final String PACKET_LOCK_MULTI = "lm";
	public static final String PACKET_UNLOCK_MULTI = "ulm";

	public static void registerServerUserInteraction() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_DRIVER_INPUT), (buf, context) -> {
			Player player = context.getPlayer();

			short keyPressStatus = buf.readShort();

			Entity vehicle = player.getVehicle();

			if (vehicle instanceof Pmv01Entity bot) {
				bot.setDriverInput(new DriverInput(keyPressStatus));
			} else if (vehicle instanceof Pmb01Entity bot) {
				bot.setDriverInput(new DriverInput(keyPressStatus));
			}
		});
	}

	public static void registerServerTargetLock() {
		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_HARD), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmv01Entity bot) {
				bot.lockTargetHard(player.level().getEntity(targetEntityId));
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_UNLOCK_HARD), (buf, context) -> {
			Player player = context.getPlayer();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmv01Entity bot) {
				bot.unlockTargetHard();
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_SOFT), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmv01Entity bot) {
				bot.lockTargetSoft(player.level().getEntity(targetEntityId));
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_UNLOCK_SOFT), (buf, context) -> {
			Player player = context.getPlayer();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmv01Entity bot) {
				bot.unlockTargetSoft();
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_LOCK_MULTI), (buf, context) -> {
			Player player = context.getPlayer();

			int targetEntityId = buf.readInt();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmv01Entity bot) {
				bot.lockTargetMulti(player.level().getEntity(targetEntityId));
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.C2S, PomkotsMechs.id(PACKET_UNLOCK_MULTI), (buf, context) -> {
			Player player = context.getPlayer();

			Entity vehicle = player.getVehicle();
			if (vehicle instanceof Pmv01Entity bot) {
				bot.unlockTargetMulti();
			}
		});
	}
}