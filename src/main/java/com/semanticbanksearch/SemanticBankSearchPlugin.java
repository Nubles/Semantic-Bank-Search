package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.inject.Provides;
import com.semanticbanksearch.ui.Sigil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.plugins.itemstats.stats.Stats;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Semantic Bank Search",
	description = "Search what you own by what it is for: \"prayer restoration\", \"food over 18 hp\", \"best melee weapon\", in the bank and from anywhere.",
	tags = {"bank", "search", "items", "storage", "purpose", "trip", "prep", "codex"}
)
public class SemanticBankSearchPlugin extends Plugin
{
	private static final String VAULT_KEY = "vault";
	private static final long SAVE_EVERY_MS = 30_000L;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ItemStatChangesService itemStatChangesService;

	@Inject
	private BankSearch bankSearch;

	@Inject
	private SemanticBankSearchConfig config;

	@Inject
	private Gson gson;

	private SemanticSearchEngine engine;
	private SemanticBankFilter bankFilter;
	private Finder finder;
	private ReadinessAnalyzer readiness;
	private SemanticCoverageAnalyzer coverage;
	private SemanticBankSearchOverlay overlay;
	private SemanticBankSearchPanel panel;
	private NavigationButton navButton;

	/** Only touched on the client thread. */
	private final Map<Integer, String> names = new HashMap<>();
	private final Map<Integer, BankItemMetadata> metadata = new HashMap<>();
	private final Set<String> openPlaces = new HashSet<>();
	private Vault vault;
	private String profile;
	private StorageIndex index = new StorageIndex();
	private String query = "";
	private boolean dirty;
	private long lastSave;
	private boolean publishPending;
	private StorageIndex analysedIndex;
	private List<ReadinessResult> trips = Collections.emptyList();
	private List<SemanticCoverageResult> gaps = Collections.emptyList();

	@Provides
	SemanticBankSearchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SemanticBankSearchConfig.class);
	}

	@Override
	protected void startUp()
	{
		List<SemanticRule> rules = SemanticLibrary.create();
		engine = new SemanticSearchEngine(rules);
		bankFilter = new SemanticBankFilter(engine, this::metadataFor, this::bankItemIds);
		finder = new Finder(engine, this::metadataFor);
		readiness = new ReadinessAnalyzer(rules, ReadinessPackLibrary.create());
		coverage = new SemanticCoverageAnalyzer(rules);
		overlay = new SemanticBankSearchOverlay(config, itemManager);
		overlayManager.add(overlay);

		panel = new SemanticBankSearchPanel(new SemanticBankSearchPanel.Controller()
		{
			@Override
			public void search(String text)
			{
				clientThread.invokeLater(() ->
				{
					query = text;
					publish();
				});
			}

			@Override
			public void refresh()
			{
				clientThread.invokeLater(SemanticBankSearchPlugin.this::publish);
			}

			@Override
			public void showInBank(String text)
			{
				clientThread.invokeLater(() -> showInBankSearch(text));
			}

			@Override
			public void forget()
			{
				int choice = JOptionPane.showConfirmDialog(panel,
					"Forget everything Semantic Bank Search remembers about this account?\nIt will relearn when you open your bank.",
					"Forget this account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.OK_OPTION)
				{
					clientThread.invokeLater(SemanticBankSearchPlugin.this::forgetAccount);
				}
			}
		}, (itemId, onLoaded) ->
		{
			AsyncBufferedImage image = itemManager.getImage(itemId);
			image.onLoaded(onLoaded);
			return image;
		});

		navButton = NavigationButton.builder()
			.tooltip("Semantic Bank Search")
			.icon(Sigil.icon(16))
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		LegacyStorageCleanup.remove(configManager::unsetConfiguration);
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				loadProfile();
				readOpenContainers();
			}
			publish();
		});
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(this::save);
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
		overlay = null;
		panel = null;
		navButton = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			save();
			openPlaces.clear();
		}
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			vault = null;
			profile = null;
			index = new StorageIndex();
			metadata.clear();
			overlay.setHighlightedItemIds(Collections.emptyList());
			publish();
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		save();
		loadProfile();
		readOpenContainers();
		publish();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (SemanticBankSearchConfig.GROUP.equals(event.getGroup()) && !VAULT_KEY.equals(event.getKey()))
		{
			clientThread.invokeLater(this::publish);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		String place = placeOf(event.getContainerId());
		if (place == null || vault == null)
		{
			return;
		}
		openPlaces.add(place);
		if (record(place, event.getItemContainer()))
		{
			rebuildIndex();
			publishPending = true;
		}
		saveIfDue();
	}

	/** Container changes come in bursts; the panel is brought up to date at most once a tick. */
	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (publishPending)
		{
			publish();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			openPlaces.add(Vault.BANK);
			clientThread.invokeLater(() ->
			{
				rebuildIndex();
				publish();
			});
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		String place = event.getGroupId() == InterfaceID.BANKMAIN ? Vault.BANK
			: event.getGroupId() == InterfaceID.SEED_VAULT ? Vault.SEED_VAULT
			: event.getGroupId() == InterfaceID.SHARED_BANK ? Vault.GROUP_STORAGE
			: null;
		if (place != null && openPlaces.remove(place))
		{
			save();
			rebuildIndex();
			publish();
		}
	}

	/**
	 * The bank's own search runs this callback for every item. A purpose match turns an item on;
	 * nothing here ever turns one off, so a name search the game would answer stays untouched.
	 */
	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!"bankSearchFilter".equals(event.getEventName()) || !config.bankSearch() || bankFilter == null)
		{
			return;
		}

		int[] intStack = client.getIntStack();
		int intStackSize = client.getIntStackSize();
		Object[] objectStack = client.getObjectStack();
		int objectStackSize = client.getObjectStackSize();
		if (intStackSize < 2 || objectStackSize < 1 || !(objectStack[objectStackSize - 1] instanceof String))
		{
			return;
		}

		String search = (String) objectStack[objectStackSize - 1];
		int itemId = intStack[intStackSize - 1];
		Boolean decision = bankFilter.decision(search, itemId);
		if (decision == null)
		{
			return;
		}
		if (decision)
		{
			intStack[intStackSize - 2] = 1;
		}
		else if (search.trim().toLowerCase().startsWith(SemanticBankFilter.FORCE_PREFIX))
		{
			// "sem " asks for purpose matches alone.
			intStack[intStackSize - 2] = 0;
		}
	}

	// Account

	private void loadProfile()
	{
		profile = configManager.getRSProfileKey();
		vault = null;
		index = new StorageIndex();
		metadata.clear();
		if (profile == null)
		{
			return;
		}
		if (config.rememberObservedStorage())
		{
			String saved = configManager.getRSProfileConfiguration(SemanticBankSearchConfig.GROUP, VAULT_KEY);
			if (saved != null && !saved.isEmpty())
			{
				try
				{
					Vault read = gson.fromJson(saved, Vault.class);
					vault = read == null ? null : read.repair();
				}
				catch (JsonParseException e)
				{
					log.warn("Semantic Bank Search could not read its saved vault; starting fresh", e);
				}
			}
		}
		if (vault == null)
		{
			vault = new Vault();
		}
		lastSave = System.currentTimeMillis();
		rebuildIndex();
	}

	private void readOpenContainers()
	{
		if (vault == null)
		{
			return;
		}
		boolean changed = false;
		for (int id : new int[]{InventoryID.INV, InventoryID.WORN, InventoryID.BANK, InventoryID.SEED_VAULT, InventoryID.INV_GROUP_TEMP})
		{
			ItemContainer container = client.getItemContainer(id);
			String place = placeOf(id);
			if (container != null && place != null)
			{
				if (id == InventoryID.BANK && !isBankOpen())
				{
					continue;
				}
				changed |= record(place, container);
			}
		}
		if (isBankOpen())
		{
			openPlaces.add(Vault.BANK);
		}
		if (changed)
		{
			rebuildIndex();
		}
	}

	private void forgetAccount()
	{
		if (profile != null)
		{
			configManager.unsetRSProfileConfiguration(SemanticBankSearchConfig.GROUP, VAULT_KEY);
		}
		vault = new Vault();
		dirty = false;
		readOpenContainers();
		rebuildIndex();
		publish();
	}

	private void saveIfDue()
	{
		if (dirty && System.currentTimeMillis() - lastSave >= SAVE_EVERY_MS)
		{
			save();
		}
	}

	private void save()
	{
		if (!dirty || vault == null || profile == null)
		{
			return;
		}
		dirty = false;
		lastSave = System.currentTimeMillis();
		if (config.rememberObservedStorage())
		{
			configManager.setConfiguration(SemanticBankSearchConfig.GROUP, profile, VAULT_KEY, gson.toJson(vault));
		}
	}

	// Reading the game

	private static String placeOf(int containerId)
	{
		switch (containerId)
		{
			case InventoryID.BANK:
				return Vault.BANK;
			case InventoryID.INV:
				return Vault.INVENTORY;
			case InventoryID.WORN:
				return Vault.WORN;
			case InventoryID.SEED_VAULT:
				return Vault.SEED_VAULT;
			case InventoryID.INV_GROUP_TEMP:
				return Vault.GROUP_STORAGE;
			default:
				return null;
		}
	}

	private boolean record(String place, ItemContainer container)
	{
		Map<Integer, Integer> items = new LinkedHashMap<>();
		for (Item item : container.getItems())
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			int id = itemManager.canonicalize(item.getId());
			if (id > 0)
			{
				items.merge(id, item.getQuantity(), (a, b) -> (int) Math.min(Integer.MAX_VALUE, (long) a + b));
			}
		}
		boolean changed = vault.replace(place, items, System.currentTimeMillis());
		dirty |= changed;
		return changed;
	}

	private void rebuildIndex()
	{
		index = vault == null ? new StorageIndex() : vault.toIndex(this::nameOf, openPlaces);
	}

	private String nameOf(int itemId)
	{
		return names.computeIfAbsent(itemId, id ->
		{
			ItemComposition composition = itemManager.getItemComposition(id);
			String name = composition == null ? "" : composition.getName();
			return name == null || "null".equalsIgnoreCase(name) ? "" : name.trim();
		});
	}

	private BankItemMetadata metadataFor(int itemId)
	{
		BankItemMetadata cached = metadata.get(itemId);
		if (cached != null || metadata.containsKey(itemId))
		{
			return cached;
		}
		BankItemMetadata read = readMetadata(itemId);
		metadata.put(itemId, read);
		return read;
	}

	private BankItemMetadata readMetadata(int itemId)
	{
		ItemComposition composition = itemManager.getItemComposition(itemId);
		if (composition == null)
		{
			return null;
		}
		boolean wieldable = false;
		boolean wearable = false;
		boolean edible = false;
		boolean drinkable = false;
		String[] actions = composition.getInventoryActions();
		if (actions != null)
		{
			for (String action : actions)
			{
				wieldable |= "Wield".equalsIgnoreCase(action);
				wearable |= "Wield".equalsIgnoreCase(action) || "Wear".equalsIgnoreCase(action);
				edible |= "Eat".equalsIgnoreCase(action);
				drinkable |= "Drink".equalsIgnoreCase(action);
			}
		}
		ItemStats stats = itemManager.getItemStats(itemId);
		ItemEquipmentStats eq = stats == null ? null : stats.getEquipment();
		return new BankItemMetadata(
			nameOf(itemId),
			wearable || stats != null && stats.isEquipable(),
			wieldable,
			edible,
			drinkable,
			eq == null ? -1 : eq.getSlot(),
			eq == null ? 0 : eq.getAstab(),
			eq == null ? 0 : eq.getAslash(),
			eq == null ? 0 : eq.getAcrush(),
			eq == null ? 0 : eq.getAmagic(),
			eq == null ? 0 : eq.getArange(),
			eq == null ? 0 : eq.getStr(),
			eq == null ? 0 : eq.getRstr(),
			eq == null ? 0 : eq.getMdmg(),
			eq == null ? 0 : eq.getPrayer(),
			healing(itemId));
	}

	private int healing(int itemId)
	{
		try
		{
			Effect effect = itemStatChangesService.getItemStatChanges(itemId);
			if (effect != null)
			{
				for (StatChange change : effect.calculate(client).getStatChanges())
				{
					if (change != null && change.getStat() == Stats.HITPOINTS)
					{
						return Math.max(0, change.getTheoretical());
					}
				}
			}
		}
		catch (RuntimeException ignored)
		{
			// Level-dependent or missing data never satisfies a healing search.
		}
		return -1;
	}

	private List<Integer> bankItemIds()
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		List<Integer> ids = new ArrayList<>();
		if (bank != null)
		{
			Set<Integer> seen = new HashSet<>();
			for (Item item : bank.getItems())
			{
				if (item != null && item.getId() > 0 && seen.add(item.getId()))
				{
					ids.add(item.getId());
				}
			}
		}
		return ids;
	}

	private boolean isBankOpen()
	{
		Widget items = client.getWidget(InterfaceID.Bankmain.ITEMS);
		return items != null && !items.isHidden();
	}

	/** Puts a purpose search into the bank's own search box, so the bank shows the same answer. */
	private void showInBankSearch(String text)
	{
		if (!isBankOpen() || text == null || text.trim().isEmpty())
		{
			return;
		}
		bankSearch.initSearch();
		clientThread.invokeLater(() ->
		{
			client.setVarcStrValue(VarClientID.MESLAYERINPUT, text.trim());
			bankSearch.layoutBank();
		});
	}

	// Panel

	private void publish()
	{
		publishPending = false;
		SemanticBankSearchPanel target = panel;
		if (target == null)
		{
			return;
		}
		long now = System.currentTimeMillis();
		if (vault == null || client.getGameState() != GameState.LOGGED_IN && client.getGameState() != GameState.LOADING)
		{
			if (overlay != null)
			{
				overlay.setHighlightedItemIds(Collections.emptyList());
			}
			PanelModel model = PanelModel.loggedOut(now);
			SwingUtilities.invokeLater(() -> target.update(model));
			return;
		}

		Finder.Finding finding = finder.find(query, index);
		List<Integer> lit = new ArrayList<>();
		for (Finder.Hit hit : finding.hits)
		{
			lit.add(hit.itemId);
		}
		overlay.setHighlightedItemIds(lit);

		List<PanelModel.PlaceSummary> places = new ArrayList<>();
		for (String name : Vault.PLACES)
		{
			Vault.Place place = vault.place(name);
			if (place == null || place.seen == 0)
			{
				continue;
			}
			long qty = 0;
			for (int q : place.items.values())
			{
				qty += q;
			}
			places.add(new PanelModel.PlaceSummary(name, place.items.size(), qty, place.seen, openPlaces.contains(name)));
		}
		Vault.Place bank = vault.place(Vault.BANK);
		Player local = client.getLocalPlayer();
		List<ObservedItem> items = index.items();
		// Trips and gaps look at the whole vault; they are only worked out again when it has
		// changed and someone is looking at the panel.
		if (analysedIndex != index && target.isShowing())
		{
			analysedIndex = index;
			trips = readiness.analyzeAll(index);
			gaps = coverage.analyze(items);
		}
		PanelModel model = new PanelModel(
			true,
			local == null ? null : local.getName(),
			bank != null && bank.seen > 0,
			isBankOpen(),
			config.bankSearch(),
			vault.distinctItems(),
			places,
			items,
			trips,
			gaps,
			finding,
			now);
		SwingUtilities.invokeLater(() -> target.update(model));
	}
}
