package net.runelite.client.plugins.aoewarnings;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;

@Getter(AccessLevel.PACKAGE)
class CrystalBomb
{
	private GameObject gameObject;
	private Instant plantedOn;
	private Instant lastClockUpdate;
	private int objectId;
	private int tickStarted;
	private WorldPoint worldLocation;

	CrystalBomb(GameObject gameObject, int startTick)
	{
		this.gameObject = gameObject;
		this.objectId = gameObject.getId();
		this.plantedOn = Instant.now();
		this.worldLocation = gameObject.getWorldLocation();
		this.tickStarted = startTick;
	}

	void bombClockUpdate()
	{
		lastClockUpdate = Instant.now();
	}
}
