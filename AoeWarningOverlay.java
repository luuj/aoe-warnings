package net.runelite.client.plugins.aoewarnings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class AoeWarningOverlay extends Overlay
{
	private final Client client;
	private final AoeWarningPlugin plugin;
	private final AoeWarningConfig config;

	private BasicStroke stroke;
	private int strokeWidth = -1;

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
		if (projectiles.isEmpty())
		{
			return null;
		}

		final Player player = client.getLocalPlayer();
		final LocalPoint playerLp = player == null ? null : player.getLocalLocation();
		if (playerLp == null)
		{
			return null;
		}

		// Read the clock and config once per frame rather than once per projectile.
		final long now = System.currentTimeMillis();
		final int tickCount = client.getTickCount();
		final Color border = config.overlayColor();
		final Color fill = config.fillColor();
		final boolean fade = config.isFadeEnabled();
		final boolean showTicks = config.tickTimers();
		final int renderDistance = config.renderDistance();

		final Font oldFont = graphics.getFont();
		graphics.setFont(config.font().getFont());
		graphics.setStroke(getStroke(config.borderWidth()));

		for (ProjectileContainer proj : projectiles.values())
		{
			final LocalPoint target = proj.getTargetPoint();
			if (target == null || proj.isExpired(now))
			{
				continue;
			}

			// Distance to the nearest edge of the marked area, in tiles. Skip targets in a
			// different world view (e.g. on a boat) since scene coordinates aren't comparable.
			if (target.getWorldView() != playerLp.getWorldView()
				|| Math.max(Math.abs(target.getSceneX() - playerLp.getSceneX()), Math.abs(target.getSceneY() - playerLp.getSceneY())) - proj.getRadius() > renderDistance)
			{
				continue;
			}

			final Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, target, proj.getAreaSize());
			if (tilePoly == null)
			{
				continue;
			}

			final double alphaScale = fade ? 1.0 - (now - proj.getStartTimeMillis()) / (double) proj.getLifetime() : 1.0;
			graphics.setColor(scaleAlpha(fill, alphaScale));
			graphics.fill(tilePoly);
			graphics.setColor(scaleAlpha(border, alphaScale));
			graphics.draw(tilePoly);

			final int ticksLeft = proj.getFinalTick() - tickCount;
			if (showTicks && ticksLeft >= 0)
			{
				final String text = Integer.toString(ticksLeft);
				final Point textLoc = Perspective.getCanvasTextLocation(client, graphics, target, text, 0);
				if (textLoc != null)
				{
					// OverlayUtil already draws a black 1px shadow under the text.
					OverlayUtil.renderTextLocation(graphics, textLoc, text, ticksLeft == 0 ? Color.RED : Color.WHITE);
				}
			}
		}

		graphics.setFont(oldFont);
		return null;
	}

	private BasicStroke getStroke(int width)
	{
		if (stroke == null || strokeWidth != width)
		{
			stroke = new BasicStroke(width);
			strokeWidth = width;
		}
		return stroke;
	}

	private static Color scaleAlpha(Color color, double scale)
	{
		if (scale >= 1.0)
		{
			return color;
		}
		int alpha = (int) Math.round(color.getAlpha() * Math.max(0.0, scale));
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
