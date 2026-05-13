package net.runelite.client.plugins.aoewarnings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
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
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
		plugin.getBombs().forEach(bomb ->
		{
			final LocalPoint localLoc = LocalPoint.fromWorld(client, bomb.getWorldLocation());
			final WorldPoint worldLoc = bomb.getWorldLocation();

			if (localLoc == null)
			{
				return;
			}

			final double distanceX = Math.abs(worldLoc.getX() - loc.getX());
			final double distanceY = Math.abs(worldLoc.getY() - loc.getY());

			Color colorCode = Color.decode(SAFE);

			if (distanceX < 1 && distanceY < 1)
			{
				colorCode = Color.decode(LETHAL);
			}
			else if (distanceX < 2 && distanceY < 2)
			{
				colorCode = Color.decode(DANGER);
			}
			else if (distanceX < 3 && distanceY < 3)
			{
				colorCode = Color.decode(WARNING);
			}
			else if (distanceX < 4 && distanceY < 4)
			{
				colorCode = Color.decode(CAUTION);
			}
			final LocalPoint centerPoint = new LocalPoint(localLoc.getX(), localLoc.getY());
			final Polygon poly = Perspective.getCanvasTileAreaPoly(client, centerPoint, BOMB_AOE);

			if (poly != null)
			{
				graphics.setColor(colorCode);
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
				OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, bombTimerString, colorCode);
			}
		});

	}
}
