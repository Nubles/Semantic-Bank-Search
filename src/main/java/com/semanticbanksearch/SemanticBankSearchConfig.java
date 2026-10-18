package com.semanticbanksearch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SemanticBankSearchConfig.GROUP)
public interface SemanticBankSearchConfig extends Config
{
	String GROUP = "semanticbanksearch";

	@ConfigItem(
		keyName = "bankSearch",
		name = "Purposes in bank search",
		description = "Typing a purpose such as \"prayer restoration\" or \"food over 18 hp\" into the bank's own search"
			+ " also shows the items that serve it. Ordinary name searches are never narrowed. Start with \"sem \" to"
			+ " search by purpose only.",
		position = 0
	)
	default boolean bankSearch()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableHighlights",
		name = "Light up matches",
		description = "Outline the items the side panel found in gold, in the bank and the inventory.",
		position = 1
	)
	default boolean enableHighlights()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rememberObservedStorage",
		name = "Remember storage",
		description = "Remember what the bank and other storage held, so you can search them while they are closed."
			+ " Kept in RuneLite's settings for each account. Turn off to search only what is open.",
		position = 2
	)
	default boolean rememberObservedStorage()
	{
		return true;
	}
}
