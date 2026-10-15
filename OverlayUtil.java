package net.runelite.client.plugins.aoewarnings;

import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import net.runelite.api.Point;

public class OverlayUtil
{
	private static Font cachedFont;

	private OverlayUtil()
	{
	}

	public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset)
	{
		if (canvasPoint == null || Strings.isNullOrEmpty(txtString))
		{
			return;
		}

		graphics.setFont(getFont(fontStyle, fontSize));

		int x = canvasPoint.getX();
		int y = canvasPoint.getY() + yOffset;

		// The shadow is now controlled solely by the "Shadows" setting. Previously a shadow was
		// always drawn, and enabling the setting stacked a second one on top.
		if (shadows)
		{
			graphics.setColor(Color.BLACK);
			graphics.drawString(txtString, x + 1, y + 1);
		}

		graphics.setColor(fontColor);
		graphics.drawString(txtString, x, y);
	}

	// Fonts are reused across frames instead of allocating a new Font per label.
	private static Font getFont(int style, int size)
	{
		Font font = cachedFont;
		if (font == null || font.getStyle() != style || font.getSize() != size)
		{
			font = new Font("Arial", style, size);
			cachedFont = font;
		}
		return font;
	}
}
