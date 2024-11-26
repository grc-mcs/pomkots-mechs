package grcmcs.minecraft.mods.pomkotsmechs;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.client.hud.PomkotsHud;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.UserInteractionManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ExplosionCore;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.FireParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.MissileSmokeParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.SparkParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.*;

public class PomkotsMechsClient {
	private static final UserInteractionManager keyPressManager = new UserInteractionManager();

	public static void initialize() {
		EntityRendererRegistry.register(PomkotsMechs.PMV01, (context)->{
			return new Pmv01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMV01B, (context)->{
			return new Pmv01bEntityRenderer(context);
		});

		EntityRendererRegistry.register(PomkotsMechs.PMB01, (context)->{
			return new Pmb01EntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.PMB02, (context)->{
			return new Pmb02EntityRenderer(context);
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

		EntityRendererRegistry.register(PomkotsMechs.NS01, (context)->{
			return new NoukinSkeltonEntityRenderer(context);
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
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_VERTICAL, (context)->{
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_ENEMY, (context)->{
			return new MissileBaseEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.MISSILE_ENEMY_LARGE, (context)->{
			return new MissileBaseEntityRenderer(context);
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
		EntityRendererRegistry.register(PomkotsMechs.HITBOX1, (context)->{
			return new HitBoxEntityRenderer(context);
		});
		EntityRendererRegistry.register(PomkotsMechs.HITBOX2, (context)->{
			return new HitBoxEntityRenderer(context);
		});

		keyPressManager.registerClient();

		ParticleProviderRegistry.register(PomkotsMechs.FIRE, FireParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.MISSILE_SMOKE, MissileSmokeParticle.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.EXPLOSION_CORE, ExplosionCore.Provider::new);
		ParticleProviderRegistry.register(PomkotsMechs.SPARK, SparkParticle.Provider::new);

		ClientGuiEvent.RENDER_HUD.register(new PomkotsHud());
	}
}