package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
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
		Map<Projectile, ProjectileContainer> projectiles = plugin.getProjectiles();
		if (projectiles.isEmpty() || client.getLocalPlayer() == null)
		{
			return null;
		}

		// Read the clock and config once per frame rather than once per projectile.
		final long now = System.currentTimeMillis();
		final int tickCount = client.getTickCount();
		final Color overlayColor = config.overlayColor();
		final boolean fade = config.isFadeEnabled();
		final boolean outline = config.isOutlineEnabled();
		final boolean tickTimers = config.tickTimers();
		final int textSize = config.textSize();
		final int fontStyle = config.fontStyle().getFont();
		final boolean shadows = config.shadows();

		for (ProjectileContainer proj : projectiles.values())
		{
			if (proj.getTargetPoint() == null || proj.isExpired(now))
			{
				continue;
			}

			final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, proj.getTargetPoint(), proj.getAreaSize());
			if (tilePoly == null)
			{
				continue;
			}

			int fillAlpha = FILL_START_ALPHA;
			int outlineAlpha = OUTLINE_START_ALPHA;
			if (fade)
			{
				final double progress = (now - proj.getStartTimeMillis()) / (double) proj.getLifetime();
				fillAlpha = (int) ((1 - progress) * FILL_START_ALPHA);
				outlineAlpha = (int) ((1 - progress) * OUTLINE_START_ALPHA);
			}
			fillAlpha = applyConfigAlpha(fillAlpha, overlayColor);
			outlineAlpha = applyConfigAlpha(outlineAlpha, overlayColor);

			// Fill first so the translucent fill doesn't sit on top of the outline and timer text.
			graphics.setColor(new Color(setAlphaComponent(overlayColor.getRGB(), fillAlpha), true));
			graphics.fillPolygon(tilePoly);

			if (outline)
			{
				graphics.setColor(new Color(setAlphaComponent(overlayColor.getRGB(), outlineAlpha), true));
				graphics.drawPolygon(tilePoly);
			}

			final int tickProgress = proj.getFinalTick() - tickCount;
			if (tickTimers && tickProgress >= 0)
			{
				OverlayUtil.renderTextLocation(graphics, Integer.toString(tickProgress), textSize, fontStyle,
					tickProgress == 0 ? Color.RED : Color.WHITE, centerPoint(tilePoly.getBounds()), shadows, 0);
			}
		}

		return null;
	}

	private static int applyConfigAlpha(int alpha, Color color)
	{
		return Math.max(0, Math.min(255, alpha * color.getAlpha() / 255));
	}

	private static Point centerPoint(Rectangle rect)
	{
		int x = (int) (rect.getX() + rect.getWidth() / 2);
		int y = (int) (rect.getY() + rect.getHeight() / 2);
		return new Point(x, y);
	}
}
