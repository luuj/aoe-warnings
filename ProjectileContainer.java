package net.runelite.client.plugins.aoewarnings;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;

@Getter(AccessLevel.PACKAGE)
class ProjectileContainer
{
	private final Projectile projectile;
	private final long startTimeMillis;
	private final int aoeSize;
	private final int lifetime;
	private final int finalTick;
	private final LocalPoint targetPoint;

	ProjectileContainer(Projectile projectile, long startTimeMillis, int lifetime, int finalTick, LocalPoint targetPoint, int aoeSize)
	{
		this.projectile = projectile;
		this.startTimeMillis = startTimeMillis;
		this.targetPoint = targetPoint;
		this.aoeSize = aoeSize;
		this.lifetime = lifetime;
		this.finalTick = finalTick;
	}

	boolean isExpired(long nowMillis)
	{
		return nowMillis > startTimeMillis + lifetime;
	}
}
