package net.runelite.client.plugins.aoewarnings;

import java.util.HashMap;
import java.util.Map;

public enum AoeProjectileInfo
{
	LIZARDMAN_SHAMAN_AOE(1293, 5),
	ICE_DEMON_RANGED_AOE(1324, 3),
	ICE_DEMON_ICE_BARRAGE_AOE(366, 3),
	KEPHRI_FIREBALL(2266,3),
	KEPHRI_DRONE(2147,3),
	ZEBAK_SPLAT1(1555,1),
	ZEBAK_SPLAT2(2194,1),
	WARDEN_STUN(2210,1),
	WARDEN_BOMB(2225,7),
	VASA_AWAKEN_AOE(1327, 3),
	VASA_RANGED_AOE(1329, 3),
	TEKTON_METEOR_AOE(660, 3),
	OLM_FALLING_CRYSTAL(1357, 3),
	VERZIK_PURPLE_SPAWN(12, 3),
	VERZIK_P1_ROCKS(12, 1);

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
	 * How long the indicator should last for this AoE warning This might
	 * need to be a bit longer than the projectile actually takes to land as
	 * there is a fade effect on the warning
	 */
	private final int aoeSize;

	AoeProjectileInfo(int id, int aoeSize)
	{
		this.id = id;
		this.aoeSize = aoeSize;
	}

	public static AoeProjectileInfo getById(int id)
	{
		return map.get(id);
	}

	public int getId()
	{
		return id;
	}

	public int getAoeSize()
	{
		return aoeSize;
	}
}
