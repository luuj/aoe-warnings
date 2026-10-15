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
	/** Radius in tiles around the target tile (0 = just the target tile). */
	private final int radius;
	private final int lifetime;
	private final int finalTick;
	private final LocalPoint targetPoint;

	ProjectileContainer(Projectile projectile, long startTimeMillis, int lifetime, int finalTick, LocalPoint targetPoint, int radius)
	{
		this.projectile = projectile;
		this.startTimeMillis = startTimeMillis;
		this.targetPoint = targetPoint;
		this.radius = radius;
		this.lifetime = lifetime;
		this.finalTick = finalTick;
	}

	/** Side length of the square drawn: the target tile plus the radius on every side. */
	int getAreaSize()
	{
		return 2 * radius + 1;
	}

	boolean isExpired(long nowMillis)
	{
		return nowMillis > startTimeMillis + lifetime;
	}
}
