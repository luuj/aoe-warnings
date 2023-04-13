package net.runelite.client.plugins.aoewarnings;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.Projectile;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;
import static net.runelite.client.plugins.aoewarnings.AoeWarningConfig.*;


@PluginDescriptor(
	name = "<html><font color=#b82584>[J] AoE Warnings",
	enabledByDefault = false,
	description = "Shows the final destination for AoE Attack projectiles",
	tags = {"bosses", "combat", "pve", "overlay"}
)
public class AoeWarningPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	private final Set<CrystalBomb> bombs = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	private final Set<ProjectileContainer> projectiles = new HashSet<>();

	@Inject
	public AoeWarningConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AoeWarningOverlay coreOverlay;

	@Inject
	private BombOverlay bombOverlay;

	@Inject
	private Client client;

	private static final int VERZIK_REGION = 12611;

	@Provides
	AoeWarningConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AoeWarningConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(coreOverlay);
		overlayManager.add(bombOverlay);
		reset();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(coreOverlay);
		overlayManager.remove(bombOverlay);
		reset();
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
		final Projectile projectile = event.getProjectile();

		if (AoeProjectileInfo.getById(projectile.getId()) == null)
		{
			return;
		}

		final int id = projectile.getId();
		final int lifetime = config.delay() + (projectile.getRemainingCycles() * 20);
		int ticksRemaining = projectile.getRemainingCycles() / 30;
		if (!isTickTimersEnabledForProjectileID(id))
		{
			ticksRemaining = 0;
		}
		final int tickCycle = client.getTickCount() + ticksRemaining;
		if (isConfigEnabledForProjectileId(id, false))
		{
			projectiles.add(new ProjectileContainer(projectile, Instant.now(), lifetime, tickCycle, event.getPosition()));
		}

	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();

		switch (gameObject.getId())
		{
			case ObjectID.CRYSTAL_BOMB:
				bombs.add(new CrystalBomb(gameObject, client.getTickCount()));
				break;
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();

		switch (gameObject.getId())
		{
			case ObjectID.CRYSTAL_BOMB:
				bombs.removeIf(o -> o.getGameObject() == gameObject);
				break;
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			return;
		}
		reset();
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		bombs.forEach(CrystalBomb::bombClockUpdate);
	}

	private boolean isTickTimersEnabledForProjectileID(int projectileId)
	{
		AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);

		if (projectileInfo == null)
		{
			return false;
		}

		switch (projectileInfo)
		{
			case VASA_RANGED_AOE:
				return false;
		}

		return true;
	}

	private boolean isConfigEnabledForProjectileId(int projectileId, boolean notify)
	{
		AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);
		if (projectileInfo == null)
		{
			return false;
		}

		switch (projectileInfo)
		{
			case LIZARDMAN_SHAMAN_AOE:
				return config.isShamansEnabled();
			case ICE_DEMON_RANGED_AOE:
			case ICE_DEMON_ICE_BARRAGE_AOE:
				return config.isIceDemonEnabled();
			case VASA_AWAKEN_AOE:
			case VASA_RANGED_AOE:
				return config.isVasaEnabled();
			case TEKTON_METEOR_AOE:
				return config.isTektonEnabled();
			case VERZIK_P1_ROCKS:
				if (regionCheck(VERZIK_REGION))
				{
					return config.isVerzikEnabled();
				}
			case OLM_FALLING_CRYSTAL:
				return config.isOlmEnabled();
			case VERZIK_PURPLE_SPAWN:
				return config.isVerzikEnabled();
			case KEPHRI_FIREBALL:
			case KEPHRI_DRONE:
				return config.isKephriEnabled();
			case ZEBAK_SPLAT1:
			case ZEBAK_SPLAT2:
				return config.isZebakEnabled();
			case WARDEN_BOMB:
			case WARDEN_STUN:
				return config.isWardenEnabled();
		}

		return false;
	}

	private void reset()
	{
		bombs.clear();
		projectiles.clear();
	}

	private boolean regionCheck(int region)
	{
		return ArrayUtils.contains(client.getMapRegions(), region);
	}
}