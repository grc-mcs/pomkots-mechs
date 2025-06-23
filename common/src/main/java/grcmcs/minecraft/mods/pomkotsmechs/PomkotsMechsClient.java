package grcmcs.minecraft.mods.pomkotsmechs;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.hud.PomkotsHud;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.UserInteractionManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ExplosionCore;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.FireParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.MissileSmokeParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.SparkParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt04EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_GENERIC_LARGE, (context)->{
			return new MissileBaseEntityRenderer(context);
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
		EntityRendererRegistry.register(PomkotsMechs.BULLET_BEAM, (context)->{
			return new BulletBeamEntityRenderer(context);
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
		EntityRendererRegistry.register(PomkotsMechs.KUJIRA, (context)->{
			return new KujiraEntityRenderer(context);
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


		if (Platform.isFabric()) {
			BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), (context)->{
				return new PomkotsCubeRenderer(context);
			});
			MenuRegistry.registerScreenFactory(PomkotsMechs.MECH_WORKBENCH_GUI.get(), MechWorkbenchScreen::new);
		}

		keyPressManager.registerClient();

		ParticleProviderRegistry.register(PomkotsMechs.FIRE, FireParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.MISSILE_SMOKE, MissileSmokeParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.EXPLOSION_CORE, ExplosionCore.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.SPARK, SparkParticle.Provider::new);

		ClientGuiEvent.RENDER_HUD.register(new PomkotsHud());

		NetworkManager.registerReceiver(NetworkManager.Side.S2C, PomkotsMechs.id(PomkotsMechs.PACKET_UPDATE_DATAPACK), (buf, context) -> {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(buf.readByteArray());
				PomkotsDataPackManager.getInstance().deSerialize(bais);
				bais.close();
			} catch (Exception e) {
				PomkotsMechs.LOGGER.error("Failed to recieve datapack to client", e);
			}
		});
	}
}