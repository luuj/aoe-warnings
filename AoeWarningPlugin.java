package net.runelite.client.plugins.aoewarnings;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Projectile;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
	name = "<html><font color=#b82584>[J] AoE Warnings",
	enabledByDefault = false,
	description = "Shows the final destination for AoE Attack projectiles",
	tags = {"bosses", "combat", "pve", "overlay"}
)
public class AoeWarningPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "aoe";
	private static final String CUSTOM_AOES_KEY = "customAoes";
	private static final int VERZIK_REGION = 12611;
	private static final Pattern CUSTOM_AOE_PATTERN = Pattern.compile("\\{\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\}");

	@Getter(AccessLevel.PACKAGE)
	private final Set<CrystalBomb> bombs = new HashSet<>();

	// Keyed by projectile so a repeat ProjectileMoved replaces its entry in O(1).
	@Getter(AccessLevel.PACKAGE)
	private final Map<Projectile, ProjectileContainer> projectiles = new HashMap<>();

	// Parsed once from config instead of re-running the regex several times per projectile event.
	private final Map<Integer, Integer> customAoeSizes = new HashMap<>();

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

	@Provides
	AoeWarningConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AoeWarningConfig.class);
	}

	@Override
	protected void startUp()
	{
		rebuildCustomAoes();
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
		customAoeSizes.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CONFIG_GROUP.equals(event.getGroup()) && CUSTOM_AOES_KEY.equals(event.getKey()))
		{
			rebuildCustomAoes();
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		final Projectile projectile = event.getProjectile();
		final int id = projectile.getId();

		final Integer customSize = customAoeSizes.get(id);
		final AoeProjectileInfo projectileInfo = customSize == null ? AoeProjectileInfo.getById(id) : null;
		if (customSize == null && (projectileInfo == null || !isConfigEnabled(projectileInfo)))
		{
			return;
		}

		final int aoeSize = customSize != null ? customSize : projectileInfo.getAoeSize();
		final int remainingCycles = projectile.getRemainingCycles();
		final int lifetime = config.delay() + (remainingCycles * 20);
		// Custom ids always get tick timers; Vasa's ranged AoE never does.
		final int ticksRemaining = projectileInfo == AoeProjectileInfo.VASA_RANGED_AOE ? 0 : remainingCycles / 30;
		final int tickCycle = client.getTickCount() + ticksRemaining;

		projectiles.put(projectile, new ProjectileContainer(projectile, System.currentTimeMillis(), lifetime, tickCycle, event.getPosition(), aoeSize));
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		if (gameObject.getId() == ObjectID.OLM_CRYSTAL_BOMB)
		{
			bombs.add(new CrystalBomb(gameObject, client.getTickCount()));
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		if (gameObject.getId() == ObjectID.OLM_CRYSTAL_BOMB)
		{
			bombs.removeIf(o -> o.getGameObject() == gameObject);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			reset();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		bombs.forEach(CrystalBomb::bombClockUpdate);

		// Expired warnings are pruned here rather than inside the overlay's render loop.
		if (!projectiles.isEmpty())
		{
			final long now = System.currentTimeMillis();
			projectiles.values().removeIf(proj -> proj.isExpired(now));
		}
	}

	private boolean isConfigEnabled(AoeProjectileInfo projectileInfo)
	{
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
				return config.isVerzikEnabled() && regionCheck(VERZIK_REGION);
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
			default:
				return false;
		}
	}

	private void rebuildCustomAoes()
	{
		customAoeSizes.clear();
		Matcher matcher = CUSTOM_AOE_PATTERN.matcher(config.customAoes());
		while (matcher.find())
		{
			try
			{
				int id = Integer.parseInt(matcher.group(1));
				int size = Integer.parseInt(matcher.group(2));
				if (size >= 1 && size <= 15)
				{
					// First valid entry for an id wins, matching the previous behaviour.
					customAoeSizes.putIfAbsent(id, size);
				}
			}
			catch (NumberFormatException ignored)
			{
				// Ignore malformed entries so a single typo does not disable valid IDs.
			}
		}
	}

	private void reset()
	{
		bombs.clear();
		projectiles.clear();
	}

	private boolean regionCheck(int region)
	{
		WorldView wv = client.getTopLevelWorldView();
		return wv != null && ArrayUtils.contains(wv.getMapRegions(), region);
	}
}
