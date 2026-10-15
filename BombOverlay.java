package net.runelite.client.plugins.aoewarnings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class BombOverlay extends Overlay
{
	// Decoded once instead of Color.decode() per bomb per frame.
	private static final Color SAFE = new Color(0x00cc00);
	private static final Color CAUTION = new Color(0xffff00);
	private static final Color WARNING = new Color(0xff9933);
	private static final Color DANGER = new Color(0xff6600);
	private static final Color LETHAL = new Color(0xcc0000);
	private static final Color FILL = new Color(0, 0, 0, 10);
	private static final Stroke STROKE = new BasicStroke(1);

	private static final int BOMB_AOE = 7;
	private static final int BOMB_DETONATE_TIME = 8;
	private static final double ESTIMATED_TICK_LENGTH = .6;

	private final DecimalFormat timeLeftFormatter = new DecimalFormat("#0.0", DecimalFormatSymbols.getInstance(Locale.US));

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
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.bombDisplay() && !plugin.getBombs().isEmpty())
		{
			drawDangerZone(graphics);
		}
		return null;
	}

	private void drawDangerZone(Graphics2D graphics)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		final WorldView wv = client.getTopLevelWorldView();
		final WorldPoint loc = player.getWorldLocation();
		final long now = System.currentTimeMillis();
		final int tickCount = client.getTickCount();
		graphics.setFont(config.font().getFont());
		final FontMetrics fm = graphics.getFontMetrics();

		for (CrystalBomb bomb : plugin.getBombs())
		{
			final WorldPoint worldLoc = bomb.getWorldLocation();
			final LocalPoint localLoc = LocalPoint.fromWorld(wv, worldLoc);
			if (localLoc == null)
			{
				continue;
			}

			final int distanceX = Math.abs(worldLoc.getX() - loc.getX());
			final int distanceY = Math.abs(worldLoc.getY() - loc.getY());

			final Color colorCode;
			if (distanceX < 1 && distanceY < 1)
			{
				colorCode = LETHAL;
			}
			else if (distanceX < 2 && distanceY < 2)
			{
				colorCode = DANGER;
			}
			else if (distanceX < 3 && distanceY < 3)
			{
				colorCode = WARNING;
			}
			else if (distanceX < 4 && distanceY < 4)
			{
				colorCode = CAUTION;
			}
			else
			{
				colorCode = SAFE;
			}

			final Polygon poly = Perspective.getCanvasTileAreaPoly(client, localLoc, BOMB_AOE);
			if (poly != null)
			{
				graphics.setColor(colorCode);
				graphics.setStroke(STROKE);
				graphics.drawPolygon(poly);
				graphics.setColor(FILL);
				graphics.fillPolygon(poly);
			}

			double timeLeft = ((BOMB_DETONATE_TIME - (tickCount - bomb.getTickStarted())) * ESTIMATED_TICK_LENGTH)
				- (now - bomb.getLastClockUpdate().toEpochMilli()) / 1000.0;
			timeLeft = Math.max(0.0, timeLeft);

			final String bombTimerString = timeLeftFormatter.format(timeLeft);
			// Project the bomb's tile at ground height. The old call passed the plane number
			// as the Z height, which put the timer at sea level instead of on the tile.
			final Point canvasPoint = Perspective.localToCanvas(client, localLoc, worldLoc.getPlane());
			if (canvasPoint != null)
			{
				Point textPoint = new Point(canvasPoint.getX() - fm.stringWidth(bombTimerString) / 2, canvasPoint.getY() + fm.getAscent() / 2);
				OverlayUtil.renderTextLocation(graphics, textPoint, bombTimerString, colorCode);
			}
		}
	}
}
