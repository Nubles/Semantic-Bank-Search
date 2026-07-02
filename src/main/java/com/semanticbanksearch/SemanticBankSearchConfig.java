package com.semanticbanksearch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(SemanticBankSearchConfig.GROUP)
public interface SemanticBankSearchConfig extends Config
{
	String GROUP = "semanticbanksearch";

	@ConfigItem(
		keyName = "enableHighlights",
		name = "Enable highlights",
		description = "Highlight matching visible bank items after a semantic search.",
		position = 0
	)
	default boolean enableHighlights()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rememberObservedStorage",
		name = "Remember observed storage",
		description = "Remember locally observed bank and storage items for future searches.",
		position = 1
	)
	default boolean rememberObservedStorage()
	{
		return true;
	}

	@Range(
		min = 100,
		max = 2000
	)
	@ConfigItem(
		keyName = "maximumRememberedEntries",
		name = "Maximum remembered entries",
		description = "Maximum number of locally observed item entries to retain.",
		position = 2
	)
	default int maximumRememberedEntries()
	{
		return 800;
	}
}
