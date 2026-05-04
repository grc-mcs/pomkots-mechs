package grcmcs.minecraft.mods.pomkotsmechs;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.PartsWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.narration.IntroNarrationScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.hud.PomkotsHud;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.UserInteractionManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc.BlockPlacementPreviewRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc.PlacementPreviewRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt04EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsSoundManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsBGMManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.nbt.CompoundTag;

import java.io.ByteArrayInputStream;

public class PomkotsMechsClient {
	private static final UserInteractionManager keyPressManager = new UserInteractionManager();

	public static void initialize() {
		EntityRendererRegistry.register(PomkotsMechs.PMV01, (context)->{
			return new Pmv01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMV01B, (context)->{
			return new Pmv01bEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMV02, (context)->{
			return new Pmv02EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMV03P, (context)->{
			return new Pmv03pEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMV03, (context)->{
			return new Pmv03EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMVC01, (context)->{
			return new Pmvc01EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMVT01, (context)->{
			return new Pmvt01EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMB01, (context)->{
			return new Pmb01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB01MK2, (context)->{
			return new Pmb01mk2EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB02, (context)->{
			return new Pmb02EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB03, (context)->{
			return new Pmb03EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB04, (context)->{
			return new Pmb04EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB05, (context)->{
			return new Pmb05EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB06, (context)->{
			return new Pmb06EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB07, (context)->{
			return new Pmb07EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB08, (context)->{
			return new Pmb08EntityRenderer(context);
		});


		EntityRendererRegistry.register(PomkotsMechs.PMB99, (context)->{
			return new Pmb99EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMSS01, (context)->{
			return new Pmss01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMSS02, (context)->{
			return new Pmss02EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMSS03, (context)->{
			return new Pmss03EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMS01, (context)->{
			return new Pms01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS02, (context)->{
			return new Pms02EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS03, (context)->{
			return new Pms03EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS04, (context)->{
			return new Pms04EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS05, (context)->{
			return new Pms05EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS06, (context)->{
			return new Pms06EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS07, (context)->{
			return new Pms07EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS08, (context)->{
			return new Pms08EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS09, (context)->{
			return new Pms09EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMS10, (context)->{
			return new Pms10EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMC01, (context)->{
			return new Pmc01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMC02, (context)->{
			return new Pmc02EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMT01, (context)->{
			return new Pmt01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMT02, (context)->{
			return new Pmt02EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMT03, (context)->{
			return new Pmt03EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMT04, (context)->{
			return new Pmt04EntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.EARTHBREAK2, (context)->{
			return new EarthbreakEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PLAYERDUMMY, (context)->{
			return new PlayerDummyEntityRenderer(context);
		});


		EntityRendererRegistry.register(PomkotsMechs.BULLET, (context)->{
			return new BulletEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLETMIDDLE, (context)->{
			return new BulletMiddleEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.GRENADE, (context)->{
			return new GrenadeEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.GRENADELARGE, (context)->{
			return new GrenadeLargeEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.MISSILE_GENERIC, (context)->{
			return new MissileGenericEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_GENERIC_LARGE, (context)->{
			return new MissileGenericEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_VERTICAL, (context)->{
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_HORIZONTAL, (context)->{
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_ENEMY, (context)->{
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_ENEMY_LARGE, (context)->{
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_POD, (context)->{
			return new MissilePodEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.BLOCK_PROJECTILE, (context)->{
			return new BlockProjectileEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BLOCK_MASS, (context)->{
			return new BlockMassEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PRESENT_BOX, (context)->{
			return new PresentBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLET_RIFLE, (context)->{
			return new BulletRifleEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLET_MACHINE, (context)->{
			return new BulletMachineEntityRenderer(context, 0.4F);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLET_MACHINE_LARGE, (context)->{
			return new BulletMachineEntityRenderer(context, 0.7F);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLET_GRENADE, (context)->{
			return new BulletGrenadeEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLET_GRENADE_LARGE, (context)->{
			return new BulletGrenadeLargeEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BULLET_BEAM, (context)->{
			return new BulletBeamEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.ROCK_SMALL, (context)->{
			return new RockSmallEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.ROCK_LARGE, (context)->{
			return new RockLargeEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.NEEDLE, (context)->{
			return new NeedleEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MINE, (context)->{
			return new MineEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.EXPLOSION, (context)->{
			return new ExplosionEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.EARTHBREAK, (context)->{
			return new EarthbreakEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.EARTHRAISE, (context)->{
			return new EarthraiseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.EXPLOADSLASH, (context)->{
			return new SlashEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.WAVE_HOR, (context)->{
			return new WaveHorizontalEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.KUJIRA, (context)->{
			return new KujiraEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PLATE, (context)->{
			return new PlateEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.ALERT, (context)->{
			return new AlertEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.ALERTRED, (context)->{
			return new AlertEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.BOSSBOX, (context)->{
			return new BossBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX1, (context)->{
			return new HitBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX2, (context)->{
			return new HitBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB02, (context)->{
			return new BossHitBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB03, (context)->{
			return new BossHitBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB06, (context)->{
			return new BossHitBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB08, (context)->{
			return new BossHitBoxEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.RAID_CONTROLLER, (context)->{
			return new RaidControllerEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.RAID_OBJECTIVE, (context)->{
			return new RaidObjectiveEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PLACEMENT_PREVIEW, (context)->{
			return new PlacementPreviewRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.BLOCK_PLACEMENT_PREVIEW, (context)->{
			return new BlockPlacementPreviewRenderer(context);
		});

		EntityRendererRegistry.register(
				PomkotsMechs.MECH_CAPSULE_PROJECTILE,
				ThrownItemRenderer::new
		);

		if (Platform.isFabric()) {
			BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), (context)->{
				return new PomkotsCubeRenderer(context);
			});
			BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW.get(), (context)->{
				return new PomkotsCubeYellowRenderer(context);
			});
			BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), (context)->{
				return new PomkotsCubeRedRenderer(context);
			});
			BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_PURPLE.get(), (context)->{
				return new PomkotsCubePurpleRenderer(context);
			});


			BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), (context)->{
				return new PomkotsLeverRenderer(context);
			});

			MenuRegistry.registerScreenFactory(PomkotsMechs.MECH_WORKBENCH_GUI.get(), MechWorkbenchScreen::new);
			MenuRegistry.registerScreenFactory(PomkotsMechs.PARTS_WORKBENCH_GUI.get(), PartsWorkbenchScreen::new);
			MenuRegistry.registerScreenFactory(PomkotsMechs.MECH_SALVAGER_GUI.get(), MechSalvagerScreen::new);
			MenuRegistry.registerScreenFactory(PomkotsMechs.POMKOTS_RADAR_GUI.get(), RadarTargetSelectScreen::new);

//			ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((arg)->{
//				SurvivalInitActions.onClientJoin(arg);
//			});
			ColorHandlerRegistry.registerItemColors(
				(stack, tintIndex) -> {
					CompoundTag tag = stack.getTag();
					if (tag == null) return 0xFFFFFFFF;

					if (tintIndex == 0 && tag.contains(PomkotsMechs.nbtName("PrimaryColor"))) return tag.getInt(PomkotsMechs.nbtName("PrimaryColor"));
					if (tintIndex == 1 && tag.contains(PomkotsMechs.nbtName("SecondaryColor"))) return tag.getInt(PomkotsMechs.nbtName("SecondaryColor"));

					return 0xFFFFFFFF;
				},
				PomkotsMechs.MECH_CAPSULE_ITEM.get()
			);
		}

		keyPressManager.registerClient();

		ParticleProviderRegistry.register(PomkotsMechs.FIRE, FireParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.MISSILE_SMOKE, MissileSmokeParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.EXPLOSION_CORE, ExplosionCore.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.SPARK, SparkParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.SPIRAL, SpiralAttractParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.MAGAZINE, MagazineParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.MAGAZINE_RIGHT, MagazineParticle.ProviderR::new);
		ParticleProviderRegistry.register(PomkotsMechs.MAGAZINE_LEFT, MagazineParticle.ProviderL::new);
		ParticleProviderRegistry.register(PomkotsMechs.MECH_DUST, DustParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.MECH_DUST_HEAVY, DustHeavyParticle.Provider::new);

		ClientGuiEvent.RENDER_HUD.register(new PomkotsHud());
		ClientTickEvent.CLIENT_POST.register(PomkotsSoundManager::cleanup);

		NetworkManager.registerReceiver(NetworkManager.Side.S2C, PomkotsMechs.id(PomkotsMechs.PACKET_UPDATE_DATAPACK), (buf, context) -> {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(buf.readByteArray());
				PomkotsDataPackManager.getInstance().deSerialize(bais);
				bais.close();
			} catch (Exception e) {
				PomkotsMechs.LOGGER.error("Failed to recieve datapack to client", e);
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.S2C, PomkotsMechs.id(PomkotsMechs.PACKET_START_OPENING), (buf, context) -> {
			if (Platform.isForge()) {
				Minecraft mc = Minecraft.getInstance();
				mc.tell(()->{
					mc.setScreen(new IntroNarrationScreen());
				});
			} else {
				context.queue(() -> {
					Minecraft mc = Minecraft.getInstance();
					mc.execute(() -> mc.setScreen(new IntroNarrationScreen()));
				});
			}
		});

		NetworkManager.registerReceiver(NetworkManager.Side.S2C, PomkotsMechs.id(PomkotsMechs.PACKET_BGM_STATE), (buf, context) -> {
			var newState = buf.readEnum(BGMState.class);
			PomkotsBGMManager.setState(newState);
		});

//		ClientTickEvent.CLIENT_POST.register(mc -> {
//			if (mc.player == null || mc.level == null) return;
//
//			boolean building = true;
//
//			if (building) {
//				PlacementPreviewEntityManager.enable(mc.level);
//			} else {
//				PlacementPreviewEntityManager.disable();
//			}
//
//			PlacementPreviewEntityManager.tick(mc);
//		});
	}
}