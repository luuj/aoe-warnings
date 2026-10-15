package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.FontType;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("aoe")
public interface AoeWarningConfig extends Config
{
	@ConfigSection(
		name = "AoE attacks",
		description = "Which AoE attacks to mark",
		position = 0
	)
	String npcTitle = "NPC AOEs";

	@ConfigSection(
		name = "Appearance",
		description = "How the overlay is drawn",
		position = 1
	)
	String appearanceSection = "appearanceSection";

	@ConfigItem(
		keyName = "lizardmanaoe",
		name = "Lizardman shamans",
		description = "Mark where Lizardman shaman acid lands",
		position = 0,
		section = npcTitle
	)
	default boolean isShamansEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "icedemon",
		name = "Ice demon",
		description = "Mark where the Ice demon's attacks land",
		position = 1,
		section = npcTitle
	)
	default boolean isIceDemonEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "vasa",
		name = "Vasa",
		description = "Mark where Vasa's rocks land",
		position = 2,
		section = npcTitle
	)
	default boolean isVasaEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tekton",
		name = "Tekton",
		description = "Mark where Tekton's meteors land",
		position = 3,
		section = npcTitle
	)
	default boolean isTektonEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "olm",
		name = "Olm crystals",
		description = "Mark where the Great Olm's falling crystals land",
		position = 4,
		section = npcTitle
	)
	default boolean isOlmEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "bombDisplay",
		name = "Olm bombs",
		description = "Mark the Great Olm's crystal bombs with a timer, colored by how close you are",
		position = 5,
		section = npcTitle
	)
	default boolean bombDisplay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "verzik",
		name = "Verzik",
		description = "Mark where Verzik's purple Nylocas and falling rocks land",
		position = 6,
		section = npcTitle
	)
	default boolean isVerzikEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "kephri",
		name = "Kephri",
		description = "Mark where Kephri's fireballs land",
		position = 7,
		section = npcTitle
	)
	default boolean isKephriEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "zebak",
		name = "Zebak",
		description = "Mark where Zebak's poison lands",
		position = 8,
		section = npcTitle
	)
	default boolean isZebakEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "warden",
		name = "Warden",
		description = "Mark where the Wardens' stunlock and bomb attacks land",
		position = 9,
		section = npcTitle
	)
	default boolean isWardenEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "customAoes",
		name = "Custom AoE list",
		description = "One entry per line: projectile ID, radius. Radius is tiles around the target tile, up to 7: "
			+ "0 = just the tile, 1 = 3x3, 2 = 5x5. Listed built-in IDs override their normal toggle and radius. "
			+ "Example: 1234, 1",
		position = 10,
		section = npcTitle
	)
	default String customAoes()
	{
		return "";
	}

	@Alpha
	@ConfigItem(
		keyName = "overlayColor",
		name = "Border color",
		description = "Outline color of the marked area",
		position = 0,
		section = appearanceSection
	)
	default Color overlayColor()
	{
		return new Color(0, 150, 200, 255);
	}

	@Alpha
	@ConfigItem(
		keyName = "fillColor",
		name = "Fill color",
		description = "Fill color of the marked area",
		position = 1,
		section = appearanceSection
	)
	default Color fillColor()
	{
		return new Color(0, 150, 200, 25);
	}

	@Range(min = 1, max = 5)
	@ConfigItem(
		keyName = "borderWidth",
		name = "Border width",
		description = "Thickness of the outline",
		position = 2,
		section = appearanceSection
	)
	default int borderWidth()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "tickTimers",
		name = "Show ticks remaining",
		description = "Show how many ticks until each attack lands",
		position = 3,
		section = appearanceSection
	)
	default boolean tickTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fade",
		name = "Fade out",
		description = "Fade the areas as their duration runs out",
		position = 4,
		section = appearanceSection
	)
	default boolean isFadeEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "font",
		name = "Font",
		description = "Font used for the overlay text",
		position = 5,
		section = appearanceSection
	)
	default FontType font()
	{
		return FontType.BOLD.withSize(32);
	}

	@Range(min = 1, max = 20)
	@Units(" tiles")
	@ConfigItem(
		keyName = "renderDistance",
		name = "Render distance",
		description = "Only draw within this many tiles of your player",
		position = 6,
		section = appearanceSection
	)
	default int renderDistance()
	{
		return 20;
	}

	@Units(Units.MILLISECONDS)
	@ConfigItem(
		keyName = "delay",
		name = "Linger time",
		description = "How long the marked area stays up after the attack lands",
		position = 7,
		section = appearanceSection
	)
	default int delay()
	{
		return 300;
	}
}
