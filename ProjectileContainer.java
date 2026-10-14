package net.runelite.client.plugins.aoewarnings;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;

@Getter(AccessLevel.PACKAGE)
class ProjectileContainer
{
	private Projectile projectile;
	private Instant startTime;
	private int aoeSize;
	private int lifetime;
	private int finalTick;
	@Setter(AccessLevel.PACKAGE)
	private LocalPoint targetPoint;

	ProjectileContainer(Projectile projectile, Instant startTime, int lifetime, int finalTick, LocalPoint targetPoint, int aoeSize)
	{
		this.projectile = projectile;
		this.startTime = startTime;
		this.targetPoint = targetPoint;
		this.aoeSize = aoeSize;
		this.lifetime = lifetime;
		this.finalTick = finalTick;
	}
}
