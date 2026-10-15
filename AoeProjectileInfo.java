package net.runelite.client.plugins.aoewarnings;

import java.util.HashMap;
import java.util.Map;

public enum AoeProjectileInfo
{
	LIZARDMAN_SHAMAN_AOE(1293, 2),
	ICE_DEMON_RANGED_AOE(1324, 1),
	ICE_DEMON_ICE_BARRAGE_AOE(366, 1),
	KEPHRI_FIREBALL(2266, 1),
	KEPHRI_DRONE(2147, 1),
	ZEBAK_SPLAT1(1555, 0),
	ZEBAK_SPLAT2(2194, 0),
	WARDEN_STUN(2210, 0),
	WARDEN_BOMB(2225, 3),
	VASA_AWAKEN_AOE(1327, 1),
	VASA_RANGED_AOE(1329, 1),
	TEKTON_METEOR_AOE(660, 1),
	OLM_FALLING_CRYSTAL(1357, 1),
	VERZIK_PURPLE_SPAWN(1586, 1),
	VERZIK_P1_ROCKS(1435, 0);

	private static final Map<Integer, AoeProjectileInfo> map = new HashMap<>();

	static
	{
		for (AoeProjectileInfo aoe : values())
		{
			map.put(aoe.id, aoe);
		}
	}

	/**
	 * The id of the projectile to trigger this AoE warning
	 */
	private final int id;
	/**
	 * Radius in tiles around the target tile (0 = just the target tile, 1 = 3x3, 2 = 5x5).
	 */
	private final int radius;

	AoeProjectileInfo(int id, int radius)
	{
		this.id = id;
		this.radius = radius;
	}

	public static AoeProjectileInfo getById(int id)
	{
		return map.get(id);
	}

	public int getId()
	{
		return id;
	}

	public int getRadius()
	{
		return radius;
	}
}
