package net.runelite.client.plugins.aoewarnings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class BombOverlay extends Overlay
{

	private static final String SAFE = "#00cc00";
	private static final String CAUTION = "#ffff00";
	private static final String WARNING = "#ff9933";
	private static final String DANGER = "#ff6600";
	private static final String LETHAL = "#cc0000";
	private static final int BOMB_AOE = 7;
	private static final int BOMB_DETONATE_TIME = 8;
	private static final double ESTIMATED_TICK_LENGTH = .6;
	private static final NumberFormat TIME_LEFT_FORMATTER =
		DecimalFormat.getInstance(Locale.US);

	static
	{
		((DecimalFormat) TIME_LEFT_FORMATTER).applyPattern("#0.0");
	}

	private final Client client;
	private final AoeWarningPlugin plugin;
	private final AoeWarningConfig config;

	@Inject
	public BombOverlay(final Client client, final AoeWarningPlugin plugin, final AoeWarningConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.bombDisplay())
		{
			drawDangerZone(graphics);
		}
		return null;
	}

	private void drawDangerZone(Graphics2D graphics)
	{
		final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
		Map<WorldPoint, Integer> aoeTiles = new HashMap<>();
		plugin.getBombs().forEach(bomb ->
		{
			final LocalPoint localLoc = LocalPoint.fromWorld(client, bomb.getWorldLocation());
			final WorldPoint worldLoc = bomb.getWorldLocation();

			if (localLoc == null)
			{
				return;
			}

			final double distance_x = Math.abs(worldLoc.getX() - loc.getX());
			final double distance_y = Math.abs(worldLoc.getY() - loc.getY());

			Color color_code = Color.decode(SAFE);

			if (distance_x < 1 && distance_y < 1)
			{
				color_code = Color.decode(LETHAL);
			}
			else if (distance_x < 2 && distance_y < 2)
			{
				color_code = Color.decode(DANGER);
			}
			else if (distance_x < 3 && distance_y < 3)
			{
				color_code = Color.decode(WARNING);
			}
			else if (distance_x < 4 && distance_y < 4)
			{
				color_code = Color.decode(CAUTION);
			}
			final LocalPoint CenterPoint = new LocalPoint(localLoc.getX(), localLoc.getY());
			final Polygon poly = Perspective.getCanvasTileAreaPoly(client, CenterPoint, BOMB_AOE);

			if (poly != null)
			{
				graphics.setColor(color_code);
				graphics.setStroke(new BasicStroke(1));
				graphics.drawPolygon(poly);
				graphics.setColor(new Color(0, 0, 0, 10));
				graphics.fillPolygon(poly);
			}

			final Instant now = Instant.now();
			double timeLeft = ((BOMB_DETONATE_TIME - (client.getTickCount() - bomb.getTickStarted())) * ESTIMATED_TICK_LENGTH) -
				(now.toEpochMilli() - bomb.getLastClockUpdate().toEpochMilli()) / 1000.0;

			timeLeft = Math.max(0.0, timeLeft);
			final String bombTimerString = TIME_LEFT_FORMATTER.format(timeLeft);
			final int textWidth = graphics.getFontMetrics().stringWidth(bombTimerString);
			final int textHeight = graphics.getFontMetrics().getAscent();
			final Point canvasPoint = Perspective.localToCanvas(client, localLoc.getX(), localLoc.getY(), worldLoc.getPlane());

			if (canvasPoint != null)
			{
				Point canvasCenterPoint = new Point(canvasPoint.getX() - textWidth / 2, canvasPoint.getY() + textHeight / 2);
				OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, bombTimerString, color_code);
			}
		});

		aoeTiles.forEach((tile, count) ->
		{
			LocalPoint localPoint = LocalPoint.fromWorld(client, tile);

			if (localPoint == null)
				return;

			Color color = Color.decode(SAFE);

			if (count == 2)
				color = Color.decode(CAUTION);
			if (count == 3)
				color = Color.decode(WARNING);
			if (count == 4)
				color = Color.decode(DANGER);
			if (count >= 5)
				color = Color.decode(LETHAL);

			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
			graphics.fill(Perspective.getCanvasTilePoly(client, localPoint));
		});
	}
}
