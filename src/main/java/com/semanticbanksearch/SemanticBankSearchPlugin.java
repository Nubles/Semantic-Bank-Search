package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Semantic Bank Search",
	description = "Searches observed bank and storage items by purpose using local semantic rules.",
	tags = {"bank", "search", "items", "storage", "utility"}
)
public class SemanticBankSearchPlugin extends Plugin
{
	private static final String STORAGE_KEY = "index";
	private static final String BANK_SOURCE_NAME = "Bank";
	private static final long SAVE_INTERVAL_MILLIS = 60_000L;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SemanticBankSearchConfig config;

	@Inject
	private Gson gson;

	private StorageIndex index;
	private SemanticSearchEngine engine;
	private SemanticBankSearchPanel panel;
	private SemanticBankSearchOverlay overlay;
	private NavigationButton navigationButton;
	private String currentQuery = "";
	private boolean bankOpen;
	private long lastPersistMillis;

	@Provides
	SemanticBankSearchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SemanticBankSearchConfig.class);
	}

	@Override
	protected void startUp()
	{
		index = SemanticBankSearchStorage.deserialize(
			gson,
			configManager.getConfiguration(SemanticBankSearchConfig.GROUP, STORAGE_KEY));
		engine = new SemanticSearchEngine(SemanticLibrary.create());
		overlay = new SemanticBankSearchOverlay(config);
		overlayManager.add(overlay);
		panel = new SemanticBankSearchPanel(this::runSearch, this::clearSearch);
		navigationButton = NavigationButton.builder()
			.tooltip("Semantic Bank Search")
			.icon(createIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);

		bankOpen = isBankOpen();
		if (bankOpen)
		{
			observeBank(System.currentTimeMillis());
		}
		clearSearch();
	}

	@Override
	protected void shutDown()
	{
		persist();
		if (overlay != null)
		{
			overlayManager.remove(overlay);
		}
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}

		panel = null;
		overlay = null;
		engine = null;
		navigationButton = null;
		index = null;
		currentQuery = "";
		bankOpen = false;
		lastPersistMillis = 0L;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (index == null)
		{
			return;
		}

		long now = System.currentTimeMillis();
		boolean currentlyBankOpen = isBankOpen();
		boolean previouslyBankOpen = bankOpen;
		bankOpen = currentlyBankOpen;
		if (currentlyBankOpen && config.rememberObservedStorage())
		{
			observeBank(now);
			if (lastPersistMillis == 0L || now - lastPersistMillis >= SAVE_INTERVAL_MILLIS)
			{
				persist();
			}
			refreshCurrentSearch();
		}
		else if (!currentlyBankOpen && previouslyBankOpen)
		{
			index.markSourceNotVisible(StorageSourceType.BANK, BANK_SOURCE_NAME);
			refreshCurrentSearch();
			persist();
		}
	}

	private void runSearch(String query)
	{
		currentQuery = clean(query);
		if (currentQuery.isEmpty())
		{
			clearSearch();
			return;
		}

		refreshCurrentSearch();
	}

	private void clearSearch()
	{
		currentQuery = "";
		if (overlay != null)
		{
			overlay.setHighlightedItemIds(new ArrayList<>());
		}
		if (panel != null)
		{
			panel.clearResults();
		}
	}

	private void refreshCurrentSearch()
	{
		if (panel == null || overlay == null || engine == null || index == null || currentQuery.isEmpty())
		{
			return;
		}

		List<SemanticSearchResult> results = engine.search(currentQuery, index);
		List<Integer> highlightedItemIds = new ArrayList<>();
		for (SemanticSearchResult result : results)
		{
			if (result.isHighlightable())
			{
				highlightedItemIds.add(result.getItemId());
			}
		}

		overlay.setHighlightedItemIds(highlightedItemIds);
		panel.updateResults(currentQuery, results, statusText(results));
	}

	private void observeBank(long now)
	{
		if (index == null || !config.rememberObservedStorage())
		{
			return;
		}

		ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
		if (itemContainer == null)
		{
			return;
		}

		Item[] items = itemContainer.getItems();
		if (items == null)
		{
			return;
		}

		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			int canonicalId = itemManager.canonicalize(item.getId());
			String name = resolveItemName(canonicalId);
			if (canonicalId > 0 && !name.isEmpty())
			{
				index.record(
					canonicalId,
					name,
					item.getQuantity(),
					StorageSourceType.BANK,
					BANK_SOURCE_NAME,
					true,
					now);
			}
		}

		index.trimToMaximumEntries(config.maximumRememberedEntries());
	}

	private String resolveItemName(int itemId)
	{
		if (itemId <= 0)
		{
			return "";
		}

		try
		{
			ItemComposition itemComposition = itemManager.getItemComposition(itemId);
			if (itemComposition == null)
			{
				return "";
			}

			String name = clean(itemComposition.getName());
			return name.equalsIgnoreCase("null") ? "" : name;
		}
		catch (RuntimeException ignored)
		{
			return "";
		}
	}

	private void persist()
	{
		if (index == null)
		{
			return;
		}

		configManager.setConfiguration(
			SemanticBankSearchConfig.GROUP,
			STORAGE_KEY,
			SemanticBankSearchStorage.serialize(gson, index));
		lastPersistMillis = System.currentTimeMillis();
	}

	private String statusText(List<SemanticSearchResult> results)
	{
		if (results == null || results.isEmpty())
		{
			return "";
		}
		if (!bankOpen)
		{
			return "Open the bank to see visible item highlights.";
		}
		if (!config.enableHighlights())
		{
			return "Bank highlights are disabled in plugin settings.";
		}
		return "";
	}

	private boolean isBankOpen()
	{
		Widget bank = client.getWidget(InterfaceID.BANK, 1);
		return bank != null && !bank.isHidden();
	}

	private static String clean(String text)
	{
		return text == null ? "" : text.trim();
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(28, 31, 35));
		graphics.fillRoundRect(1, 1, 14, 14, 4, 4);
		graphics.setColor(new Color(0, 190, 255));
		graphics.drawRoundRect(1, 1, 13, 13, 4, 4);
		graphics.drawOval(4, 4, 5, 5);
		graphics.drawLine(8, 8, 12, 12);
		graphics.setColor(new Color(255, 152, 0));
		graphics.fillOval(10, 3, 2, 2);
		graphics.dispose();
		return image;
	}
}
