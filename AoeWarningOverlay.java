package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import static net.runelite.client.plugins.aoewarnings.ColorUtil.setAlphaComponent;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class AoeWarningOverlay extends Overlay
{
	private static final int FILL_START_ALPHA = 25;
	private static final int OUTLINE_START_ALPHA = 255;

	private final Client client;
	private final AoeWarningPlugin plugin;
	private final AoeWarningConfig config;

	@Inject
	public AoeWarningOverlay(final Client client, final AoeWarningPlugin plugin, final AoeWarningConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WorldPoint lp = client.getLocalPlayer().getWorldLocation();
		Instant now = Instant.now();
		Set<ProjectileContainer> projectiles = plugin.getProjectiles();
		projectiles.forEach(proj ->
		{
			if (proj.getTargetPoint() == null)
			{
				return;
			}
			Color color;

			if (now.isAfter(proj.getStartTime().plus(Duration.ofMillis(proj.getLifetime()))))
			{
				return;
			}

			final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, proj.getTargetPoint(), proj.getAoeProjectileInfo().getAoeSize());

			if (tilePoly == null)
			{
				return;
			}

			final double progress = (System.currentTimeMillis() - proj.getStartTime().toEpochMilli()) / (double) proj.getLifetime();

			final int tickProgress = proj.getFinalTick() - client.getTickCount();
			int fillAlpha, outlineAlpha;
			if (config.isFadeEnabled())
			{
				fillAlpha = (int) ((1 - progress) * FILL_START_ALPHA);
				outlineAlpha = (int) ((1 - progress) * OUTLINE_START_ALPHA);
			}
			else
			{
				fillAlpha = FILL_START_ALPHA;
				outlineAlpha = OUTLINE_START_ALPHA;
			}
			if (tickProgress == 0)
			{
				color = Color.RED;
			}
			else
			{
				color = Color.WHITE;
			}

			if (fillAlpha < 0)
			{
				fillAlpha = 0;
			}
			if (outlineAlpha < 0)
			{
				outlineAlpha = 0;
			}

			if (fillAlpha > 255)
			{
				fillAlpha = 255;
			}
			if (outlineAlpha > 255)
			{
				outlineAlpha = 255;
			}

			if (config.isOutlineEnabled())
			{
				graphics.setColor(new Color(setAlphaComponent(config.overlayColor().getRGB(), outlineAlpha), true));
				graphics.drawPolygon(tilePoly);
			}
			if (config.tickTimers() && tickProgress >= 0)
			{
				OverlayUtil.renderTextLocation(graphics, Integer.toString(tickProgress), config.textSize(),
					config.fontStyle().getFont(), color, centerPoint(tilePoly.getBounds()), config.shadows(), 0);
			}

			graphics.setColor(new Color(setAlphaComponent(config.overlayColor().getRGB(), fillAlpha), true));
			graphics.fillPolygon(tilePoly);
		});
		projectiles.removeIf(proj -> now.isAfter(proj.getStartTime().plus(Duration.ofMillis(proj.getLifetime()))));
		return null;
	}

	private Point centerPoint(Rectangle rect)
	{
		int x = (int) (rect.getX() + rect.getWidth() / 2);
		int y = (int) (rect.getY() + rect.getHeight() / 2);
		return new Point(x, y);
	}
}