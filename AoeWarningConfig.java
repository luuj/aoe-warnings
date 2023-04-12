
package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import java.awt.Font;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("aoe")
public interface AoeWarningConfig extends Config
{
	@Getter(AccessLevel.PACKAGE)
	@AllArgsConstructor
	enum FontStyle
	{
		BOLD("Bold", Font.BOLD),
		ITALIC("Italic", Font.ITALIC),
		PLAIN("Plain", Font.PLAIN);

		private String name;
		private int font;

		@Override
		public String toString()
		{
			return getName();
		}
	}

	@ConfigSection(
		name = "Overlay",
		description = "",
		position = 1
	)
	String overlayTitle = "Overlay";

	@ConfigItem(
		position = 2,
		keyName = "overlayColor",
		name = "Overlay Color",
		description = "Configures the color of the AoE Projectile Warnings overlay",
		section = overlayTitle
	)
	default Color overlayColor()
	{
		return new Color(0, 150, 200);
	}

	@ConfigItem(
		keyName = "outline",
		name = "Display Outline",
		description = "Configures whether or not AoE Projectile Warnings have an outline",
		section = overlayTitle,
		position = 3
	)
	default boolean isOutlineEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "delay",
		name = "Fade Delay",
		description = "Configures the amount of time in milliseconds that the warning lingers for after the projectile has touched the ground",
		section = overlayTitle,
		position = 4
	)
	default int delay()
	{
		return 300;
	}

	@ConfigItem(
		keyName = "fade",
		name = "Fade Warnings",
		description = "Configures whether or not AoE Projectile Warnings fade over time",
		section = overlayTitle,
		position = 5
	)
	default boolean isFadeEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tickTimers",
		name = "Tick Timers",
		description = "Configures whether or not AoE Projectile Warnings has tick timers overlaid as well.",
		section = overlayTitle,
		position = 6
	)
	default boolean tickTimers()
	{
		return true;
	}

	@ConfigSection(
		position = 7,
		name = "Text",
		description = ""
	)
	String textTitle = "Text";

	@ConfigItem(
		position = 8,
		keyName = "fontStyle",
		name = "Font Style",
		description = "Bold/Italics/Plain",
		section = textTitle
	)
	default FontStyle fontStyle()
	{
		return FontStyle.BOLD;
	}

	@Range(
		min = 10,
		max = 40
	)
	@ConfigItem(
		position = 9,
		keyName = "textSize",
		name = "Text Size",
		description = "Text Size for Timers.",
		section = textTitle
	)
	default int textSize()
	{
		return 32;
	}

	@ConfigItem(
		position = 10,
		keyName = "shadows",
		name = "Shadows",
		description = "Adds Shadows to text.",
		section = textTitle
	)
	default boolean shadows()
	{
		return true;
	}
	@ConfigSection(
		name = "NPC AOEs",
		description = "",
		position = 11
	)
	String npcTitle = "NPC AOEs";

	@ConfigItem(
		keyName = "lizardmanaoe",
		name = "Lizardman Shamans",
		description = "Configures whether or not AoE Projectile Warnings for Lizardman Shamans is displayed",
		section = npcTitle,
		position = 12
	)
	default boolean isShamansEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "icedemon",
		name = "Ice Demon",
		description = "Configures whether or not AoE Projectile Warnings for Ice Demon is displayed",
		section = npcTitle,
		position = 13
	)
	default boolean isIceDemonEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "vasa",
		name = "Vasa",
		description = "Configures whether or not AoE Projectile Warnings for Vasa is displayed",
		section = npcTitle,
		position = 14
	)
	default boolean isVasaEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tekton",
		name = "Tekton",
		description = "Configures whether or not AoE Projectile Warnings for Tekton is displayed",
		section = npcTitle,
		position = 15
	)
	default boolean isTektonEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "olm",
		name = "Olm Crystals",
		description = "Configures whether or not AoE Projectile Warnings for The Great Olm are displayed",
		section = npcTitle,
		position = 16
	)
	default boolean isOlmEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "bombDisplay",
		name = "Olm Bombs",
		description = "Display a timer and colour-coded AoE for Olm's crystal-phase bombs.",
		section = npcTitle,
		position = 17
	)
	default boolean bombDisplay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "verzik",
		name = "Verzik",
		description = "Configures if Verzik purple Nylo/falling rock AOE is shown",
		section = npcTitle,
		position = 18
	)
	default boolean isVerzikEnabled()
	{
		return true;
	}
}
