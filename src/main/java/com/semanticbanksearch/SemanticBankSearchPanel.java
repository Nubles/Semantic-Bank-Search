package com.semanticbanksearch;

import com.semanticbanksearch.ui.Card;
import com.semanticbanksearch.ui.Chip;
import com.semanticbanksearch.ui.CodexButton;
import com.semanticbanksearch.ui.Crest;
import com.semanticbanksearch.ui.ItemIcons;
import com.semanticbanksearch.ui.ItemRow;
import com.semanticbanksearch.ui.Ring;
import com.semanticbanksearch.ui.SearchField;
import com.semanticbanksearch.ui.SectionHeader;
import com.semanticbanksearch.ui.Segmented;
import com.semanticbanksearch.ui.ShareBar;
import com.semanticbanksearch.ui.StackPanel;
import com.semanticbanksearch.ui.Theme;
import com.semanticbanksearch.ui.Ui;
import com.semanticbanksearch.ui.WrapPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;

/**
 * The side panel. A crest says how much of the account the plugin knows; under it one question
 * box serves four pages. Search answers a question about what you own and why each item fits,
 * Trips checks your kit for a boss or a run, Vault lists everything remembered place by place,
 * and Gaps shows the items no purpose describes yet.
 */
class SemanticBankSearchPanel extends PluginPanel
{
	interface Controller
	{
		void search(String query);

		/** The panel has just been opened and wants an up to date picture. */
		void refresh();

		void showInBank(String query);

		void forget();
	}

	static final int SEARCH = 0;
	static final int TRIPS = 1;
	static final int VAULT = 2;
	static final int GAPS = 3;

	private static final int TEXT_WIDTH = 190;
	private static final int ROWS_PER_PLACE = 250;
	private static final int GAP_ROWS = 200;
	private static final String[] PLACEHOLDERS = {
		"Ask your bank: prayer restore...",
		"Find a trip: vorkath, herb run...",
		"Filter by name...",
		"Filter by name..."
	};
	static final String[] SUGGESTIONS = {
		"prayer restoration",
		"teleport",
		"food over 18 hp",
		"best melee weapon",
		"4 dose prayer pots",
		"things that cut webs",
		"wildy escape",
		"ranged weapons for dragons",
		"poison protection",
		"warm clothing"
	};

	private final Controller controller;
	private final ItemIcons icons;
	private final Crest crest = new Crest();
	private final SearchField search;
	private final Segmented tabs = new Segmented(false, "Search", "Trips", "Vault", "Gaps");
	private final StackPanel content = new StackPanel(6, 8);
	private final JScrollPane scroll = new JScrollPane(content);

	private PanelModel model = PanelModel.loggedOut(System.currentTimeMillis());
	private String query = "";
	private String openTrip;

	SemanticBankSearchPanel(Controller controller, ItemIcons icons)
	{
		super(false);
		this.controller = controller;
		this.icons = icons;
		setLayout(new BorderLayout());
		setBackground(Theme.INK);

		search = new SearchField(this::queryChanged);
		search.setPlaceholder(PLACEHOLDERS[SEARCH]);

		StackPanel north = new StackPanel(6, 0);
		north.add(crest);
		JPanel controls = new JPanel(new BorderLayout(0, 6));
		controls.setOpaque(false);
		controls.setBorder(new EmptyBorder(2, 8, 2, 8));
		controls.add(search, BorderLayout.NORTH);
		controls.add(tabs, BorderLayout.SOUTH);
		north.add(controls);
		add(north, BorderLayout.NORTH);

		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		add(scroll, BorderLayout.CENTER);

		tabs.onChange(index ->
		{
			search.setPlaceholder(PLACEHOLDERS[index]);
			openTrip = null;
			rebuild(true);
		});
		rebuild(true);
	}

	@Override
	public void onActivate()
	{
		controller.refresh();
	}

	/** Called on the Swing thread with a fresh picture of the account. */
	void update(PanelModel model)
	{
		this.model = model;
		updateCrest();
		rebuild(false);
	}

	/** Opens a page, for previews. */
	void showTab(int index)
	{
		tabs.setSelected(index, false);
		search.setPlaceholder(PLACEHOLDERS[index]);
		rebuild(true);
	}

	/** Asks a question as if it were typed, for suggestions and previews. */
	void ask(String text)
	{
		search.setText(text);
	}

	void openTrip(String name)
	{
		openTrip = name;
		rebuild(true);
	}

	private void queryChanged(String text)
	{
		query = text == null ? "" : text.trim();
		if (tabs.getSelected() == SEARCH)
		{
			controller.search(query);
		}
		else
		{
			if (tabs.getSelected() == TRIPS)
			{
				openTrip = null;
			}
			rebuild(true);
		}
	}

	private void updateCrest()
	{
		if (!model.loggedIn)
		{
			crest.update(null, -1, "Log in to search your bank", false);
		}
		else if (!model.bankSeen)
		{
			crest.update(model.player, model.distinctItems > 0 ? model.distinctItems : -1, "Open your bank once to begin", false);
		}
		else if (model.bankOpen)
		{
			crest.update(model.player, model.distinctItems, "Bank open: matches light up", true);
		}
		else
		{
			PanelModel.PlaceSummary bank = place(Vault.BANK);
			crest.update(model.player, model.distinctItems, "Bank read " + ago(bank == null ? 0 : bank.seen), true);
		}
	}

	private PanelModel.PlaceSummary place(String name)
	{
		for (PanelModel.PlaceSummary place : model.places)
		{
			if (place.name.equals(name))
			{
				return place;
			}
		}
		return null;
	}

	private void rebuild(boolean toTop)
	{
		Rectangle view = scroll.getViewport().getViewRect();
		content.removeAll();
		if (!model.loggedIn)
		{
			content.add(notice("Not logged in",
				"Log in and open your bank. The plugin reads what you own as you play, and keeps it for each account.",
				Theme.GOLD_DIM));
		}
		else
		{
			switch (tabs.getSelected())
			{
				case TRIPS:
					buildTrips();
					break;
				case VAULT:
					buildVault();
					break;
				case GAPS:
					buildGaps();
					break;
				default:
					buildSearch();
			}
		}
		content.revalidate();
		content.repaint();
		if (toTop)
		{
			SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
		}
		else
		{
			SwingUtilities.invokeLater(() -> content.scrollRectToVisible(view));
		}
	}

	// Search

	private void buildSearch()
	{
		Finder.Finding finding = model.finding;
		if (query.isEmpty() || finding == null || !finding.query.equalsIgnoreCase(query))
		{
			buildSuggestions();
			return;
		}

		content.add(readingCard(finding));
		if (finding.hits.isEmpty())
		{
			content.add(notice("Nothing you own fits yet",
				model.bankSeen
					? "Try another purpose or an item's name. Items in storage the plugin has not seen yet can't be found."
					: "Open your bank so the plugin can read what you own.",
				Theme.GOLD_DIM));
			return;
		}

		Map<String, List<Finder.Hit>> groups = new LinkedHashMap<>();
		for (Finder.Hit hit : finding.hits)
		{
			groups.computeIfAbsent(hit.category, k -> new ArrayList<>()).add(hit);
		}
		for (Map.Entry<String, List<Finder.Hit>> group : groups.entrySet())
		{
			content.add(new SectionHeader(group.getKey(), String.valueOf(group.getValue().size())));
			for (Finder.Hit hit : group.getValue())
			{
				String note = String.join(", ", hit.places) + (hit.reason.isEmpty() ? "" : "  -  " + hit.reason);
				content.add(new ItemRow(hit.itemId, hit.name, note, hit.quantity, hit.inOpenBank && model.bankOpen, icons,
					tip(hit.name, hit.reason, String.join(", ", hit.places))));
			}
		}
	}

	private Component readingCard(Finder.Finding finding)
	{
		Card card = new Card(new BorderLayout(0, 4));
		card.accent(finding.understood ? Theme.TEAL : Theme.GOLD_DIM);
		StackPanel lines = new StackPanel(3, 0);
		lines.add(Ui.label(finding.understood ? "READ AS A PURPOSE" : "READ AS A NAME", Theme.small(), Theme.TEXT_FAINT));
		lines.add(Ui.wrapped(finding.understood ? finding.reading : "Items named \"" + finding.query + "\"",
			Theme.body(), finding.understood ? Theme.mix(Theme.TEAL, Theme.TEXT, 0.25) : Theme.TEXT, TEXT_WIDTH - 10));
		int inBank = 0;
		for (Finder.Hit hit : finding.hits)
		{
			if (hit.inOpenBank)
			{
				inBank++;
			}
		}
		String count = finding.hits.size() == 1 ? "1 item found" : finding.hits.size() + " items found";
		if (model.bankOpen && inBank > 0)
		{
			count += ", " + inBank + " lit in your bank";
		}
		lines.add(Ui.label(count, Theme.small(), Theme.TEXT_MUTED));
		card.add(lines, BorderLayout.CENTER);

		if (finding.understood && model.bankSearch)
		{
			JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			actions.setOpaque(false);
			if (model.bankOpen)
			{
				actions.add(new CodexButton("Show in bank", true, () -> controller.showInBank(finding.query)));
			}
			else
			{
				actions.add(Ui.wrapped("Type the same words into the bank's search to see them there.",
					Theme.small(), Theme.TEXT_FAINT, TEXT_WIDTH - 10));
			}
			card.add(actions, BorderLayout.SOUTH);
		}
		return card;
	}

	private void buildSuggestions()
	{
		if (!model.bankSeen)
		{
			content.add(notice("Open your bank",
				"The plugin learns what you own by seeing it. Open your bank once, and any seed vault or group storage"
					+ " you use, and they can be searched from anywhere.",
				Theme.GOLD));
		}
		content.add(new SectionHeader("Ask by purpose"));
		WrapPanel grid = new WrapPanel(5);
		for (String suggestion : SUGGESTIONS)
		{
			grid.add(new Chip(suggestion, () -> search.setText(suggestion)));
		}
		content.add(grid);

		content.add(new SectionHeader("In the bank"));
		content.add(notice(model.bankSearch ? "Works in the bank's search too" : "Bank search is off",
			model.bankSearch
				? "Type a purpose into the bank's own search and the items that serve it join the results. Name"
					+ " searches still work as always. Start with \"sem \" to see purpose matches alone."
				: "Turn on \"Purposes in bank search\" in the plugin's settings to ask the bank itself.",
			Theme.TEAL));
	}

	// Trips

	private void buildTrips()
	{
		List<ReadinessResult> trips = new ArrayList<>();
		for (ReadinessResult trip : model.trips)
		{
			if (query.isEmpty() || trip.getPackName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
				|| trip.getDescription().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
			{
				trips.add(trip);
			}
		}
		if (openTrip == null && trips.size() == 1 && !query.isEmpty())
		{
			openTrip = trips.get(0).getPackName();
		}
		if (openTrip != null)
		{
			for (ReadinessResult trip : model.trips)
			{
				if (trip.getPackName().equals(openTrip))
				{
					buildTrip(trip);
					return;
				}
			}
			openTrip = null;
		}

		content.add(new SectionHeader("Trips", trips.size() + " checks"));
		if (trips.isEmpty())
		{
			content.add(notice("No trip by that name", "Clear the box to see every trip the plugin can check.", Theme.GOLD_DIM));
		}
		for (ReadinessResult trip : trips)
		{
			content.add(tripCard(trip));
		}
	}

	private Component tripCard(ReadinessResult trip)
	{
		int need = trip.getRequiredSlotCount();
		int have = trip.getCoveredRequiredSlotCount();
		int owned = 0;
		for (ReadinessSlotResult slot : trip.getSlotResults())
		{
			if (!slot.isMissing())
			{
				owned++;
			}
		}
		int total = trip.getSlotResults().size();
		boolean ready = have >= need;
		Card card = new Card(new BorderLayout(8, 0));
		card.accent(ready ? Theme.GREEN : Theme.EMBER);
		Ring ring = new Ring(34);
		ring.set(owned, total);
		card.add(ring, BorderLayout.WEST);
		StackPanel lines = new StackPanel(2, 0);
		lines.add(Ui.label(trip.getPackName(), Theme.title(), Theme.TEXT));
		lines.add(Ui.label(ready ? "Ready to go" : (need - have) + " essential missing", Theme.small(),
			ready ? Theme.GREEN : Theme.EMBER));
		lines.add(Ui.wrapped(trip.getDescription(), Theme.small(), Theme.TEXT_FAINT, TEXT_WIDTH - 50));
		card.add(lines, BorderLayout.CENTER);
		card.onClick(() -> openTrip(trip.getPackName()));
		return card;
	}

	private void buildTrip(ReadinessResult trip)
	{
		JPanel back = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		back.setOpaque(false);
		back.add(new CodexButton("All trips", false, () ->
		{
			openTrip = null;
			rebuild(true);
		}));
		content.add(back);

		int owned = 0;
		for (ReadinessSlotResult slot : trip.getSlotResults())
		{
			if (!slot.isMissing())
			{
				owned++;
			}
		}
		Card head = new Card(new BorderLayout(10, 0));
		head.highlighted(true);
		Ring ring = new Ring(48);
		ring.set(owned, trip.getSlotResults().size());
		head.add(ring, BorderLayout.WEST);
		StackPanel lines = new StackPanel(2, 0);
		lines.add(Ui.label(trip.getPackName(), Theme.title(), Theme.GOLD_BRIGHT));
		lines.add(Ui.wrapped(trip.getDescription(), Theme.small(), Theme.TEXT_MUTED, TEXT_WIDTH - 60));
		head.add(lines, BorderLayout.CENTER);
		content.add(head);

		content.add(new SectionHeader("Kit", owned + " of " + trip.getSlotResults().size()));
		for (ReadinessSlotResult slot : trip.getSlotResults())
		{
			content.add(slotCard(slot));
		}
		content.add(Ui.wrapped("Only items the plugin has seen count. A missing slot may be in storage it has not read yet.",
			Theme.small(), Theme.TEXT_FAINT, TEXT_WIDTH));
	}

	private Component slotCard(ReadinessSlotResult slot)
	{
		boolean missing = slot.isMissing();
		boolean essential = slot.getKind() == ReadinessSlotKind.REQUIRED;
		Color accent = !missing ? Theme.GREEN : essential ? Theme.BLOOD : Theme.GOLD_DIM;
		Card card = new Card(new BorderLayout(0, 4));
		card.accent(accent);
		JPanel title = new JPanel(new BorderLayout());
		title.setOpaque(false);
		title.add(Ui.label(slot.getSlotName(), Theme.body(), missing ? Theme.TEXT_MUTED : Theme.TEXT), BorderLayout.CENTER);
		title.add(Ui.label(kind(slot.getKind()), Theme.small(), essential ? Theme.mix(Theme.GOLD, Theme.TEXT, 0.2) : Theme.TEXT_FAINT),
			BorderLayout.EAST);
		card.add(title, BorderLayout.NORTH);
		if (missing)
		{
			card.add(Ui.wrapped((essential ? "Missing. " : "Not seen. ") + slot.getReason(), Theme.small(),
				essential ? Theme.mix(Theme.BLOOD, Theme.TEXT, 0.35) : Theme.TEXT_FAINT, TEXT_WIDTH - 20), BorderLayout.CENTER);
		}
		else
		{
			StackPanel rows = new StackPanel(0, 0);
			int shown = 0;
			for (SemanticSearchResult item : slot.getOwnedItems())
			{
				if (shown++ == 3)
				{
					break;
				}
				rows.add(new ItemRow(item.getItemId(), item.getItemName(), item.getSourceName(), item.getQuantity(),
					item.isHighlightable() && model.bankOpen, icons, tip(item.getItemName(), slot.getReason(), item.getSourceName())));
			}
			card.add(rows, BorderLayout.CENTER);
		}
		return card;
	}

	private static String kind(ReadinessSlotKind kind)
	{
		switch (kind)
		{
			case REQUIRED:
				return "ESSENTIAL";
			case RECOMMENDED:
				return "ADVISED";
			case UPGRADE:
				return "UPGRADE";
			default:
				return "OPTIONAL";
		}
	}

	// Vault

	private void buildVault()
	{
		if (model.places.isEmpty())
		{
			content.add(notice("Nothing remembered yet", "Open your bank and it will appear here.", Theme.GOLD));
			return;
		}

		content.add(new SectionHeader("Places", model.distinctItems + " items"));
		JPanel grid = new JPanel(new GridLayout(0, 2, 5, 5));
		grid.setOpaque(false);
		for (PanelModel.PlaceSummary place : model.places)
		{
			grid.add(placeTile(place));
		}
		content.add(grid);

		String filter = query.toLowerCase(Locale.ROOT);
		for (PanelModel.PlaceSummary place : model.places)
		{
			List<ObservedItem> items = new ArrayList<>();
			for (ObservedItem item : model.items)
			{
				if (item.getSourceName().equals(place.name)
					&& (filter.isEmpty() || item.getName().toLowerCase(Locale.ROOT).contains(filter)))
				{
					items.add(item);
				}
			}
			if (items.isEmpty())
			{
				continue;
			}
			items.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
			content.add(new SectionHeader(place.name, String.valueOf(items.size())));
			int shown = 0;
			for (ObservedItem item : items)
			{
				if (shown++ == ROWS_PER_PLACE)
				{
					content.add(Ui.label("and " + (items.size() - ROWS_PER_PLACE) + " more; type to filter", Theme.small(), Theme.TEXT_FAINT));
					break;
				}
				content.add(new ItemRow(item.getItemId(), item.getName(), null, item.getQuantity(),
					item.isCurrentlyVisible() && place.name.equals(Vault.BANK) && model.bankOpen, icons, null));
			}
		}

		content.add(new SectionHeader("Privacy"));
		content.add(Ui.wrapped("Kept in RuneLite's settings for this account only. Nothing is sent anywhere by this plugin.",
			Theme.small(), Theme.TEXT_FAINT, TEXT_WIDTH));
		JPanel forget = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		forget.setOpaque(false);
		forget.add(new CodexButton("Forget this account", false, controller::forget));
		content.add(forget);
	}

	private JComponent placeTile(PanelModel.PlaceSummary place)
	{
		Card card = new Card(new BorderLayout());
		card.setBorder(new EmptyBorder(6, 9, 6, 6));
		card.accent(place.open ? Theme.GOLD_BRIGHT : Theme.GOLD_DIM);
		StackPanel lines = new StackPanel(1, 0);
		lines.add(Ui.label(place.name, Theme.small(), Theme.GOLD));
		lines.add(Ui.label(String.format("%,d", place.items), Theme.title(), Theme.TEXT));
		lines.add(Ui.label(place.open ? "open now" : ago(place.seen), Theme.small(), place.open ? Theme.TEAL : Theme.TEXT_FAINT));
		card.add(lines, BorderLayout.CENTER);
		return card;
	}

	// Gaps

	private void buildGaps()
	{
		if (model.coverage.isEmpty())
		{
			content.add(notice("Nothing to check yet", "Open your bank and its items will be checked here.", Theme.GOLD));
			return;
		}
		int covered = 0;
		int kind = 0;
		int unknown = 0;
		for (SemanticCoverageResult result : model.coverage)
		{
			if (result.isCovered())
			{
				covered++;
			}
			else if (result.getAwarenessStatus() == ItemAwarenessStatus.MECHANICALLY_TAGGED)
			{
				kind++;
			}
			else
			{
				unknown++;
			}
		}

		Card summary = new Card(new BorderLayout(0, 6));
		summary.accent(Theme.TEAL);
		StackPanel lines = new StackPanel(4, 0);
		int total = model.coverage.size();
		lines.add(Ui.wrapped(Math.round(100.0 * covered / total) + "% of your items have a purpose", Theme.body(), Theme.TEXT,
			TEXT_WIDTH - 10));
		lines.add(new ShareBar(new int[]{covered, kind, unknown}, new Color[]{Theme.TEAL, Theme.GOLD_DIM, Theme.STROKE}));
		lines.add(legend(Theme.TEAL, covered + " searchable by purpose"));
		lines.add(legend(Theme.GOLD_DIM, kind + " known by kind only"));
		lines.add(legend(Theme.STROKE, unknown + " not described yet"));
		summary.add(lines, BorderLayout.CENTER);
		content.add(summary);

		String filter = query.toLowerCase(Locale.ROOT);
		List<SemanticCoverageResult> gaps = new ArrayList<>();
		for (SemanticCoverageResult result : model.coverage)
		{
			if (!result.isCovered() && (filter.isEmpty() || result.getItemName().toLowerCase(Locale.ROOT).contains(filter)))
			{
				gaps.add(result);
			}
		}
		gaps.sort((a, b) -> a.getItemName().compareToIgnoreCase(b.getItemName()));
		content.add(new SectionHeader("Not described yet", String.valueOf(gaps.size())));
		content.add(Ui.wrapped("No purpose in the library names these yet. They still turn up by name, and by kind when they are"
			+ " gear, food or potions.",
			Theme.small(), Theme.TEXT_FAINT, TEXT_WIDTH));
		int shown = 0;
		for (SemanticCoverageResult gap : gaps)
		{
			if (shown++ == GAP_ROWS)
			{
				content.add(Ui.label("and " + (gaps.size() - GAP_ROWS) + " more; type to filter", Theme.small(), Theme.TEXT_FAINT));
				break;
			}
			String note = gap.getMechanicalTags().isEmpty() ? gap.getSourceName() : String.join(", ", gap.getMechanicalTags());
			content.add(new ItemRow(gap.getItemId(), gap.getItemName(), note, gap.getQuantity(), false, icons, null));
		}
	}

	private static JComponent legend(Color color, String text)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setOpaque(false);
		JLabel swatch = new JLabel()
		{
			@Override
			protected void paintComponent(java.awt.Graphics g)
			{
				g.setColor(color);
				g.fillRect(0, 3, 8, 8);
			}
		};
		swatch.setPreferredSize(new Dimension(8, 12));
		row.add(swatch, BorderLayout.WEST);
		row.add(Ui.label(text, Theme.small(), Theme.TEXT_MUTED), BorderLayout.CENTER);
		return row;
	}

	// Shared

	private static Component notice(String title, String body, Color accent)
	{
		Card card = new Card(new BorderLayout(0, 4));
		card.accent(accent);
		card.add(Ui.label(title, Theme.body(), Theme.TEXT), BorderLayout.NORTH);
		card.add(Ui.wrapped(body, Theme.small(), Theme.TEXT_MUTED, TEXT_WIDTH - 10), BorderLayout.CENTER);
		return card;
	}

	private static String tip(String name, String reason, String places)
	{
		return "<html><b>" + Ui.escape(name) + "</b>"
			+ (reason == null || reason.isEmpty() ? "" : "<br>" + Ui.escape(reason))
			+ (places == null || places.isEmpty() ? "" : "<br>In: " + Ui.escape(places)) + "</html>";
	}

	private String ago(long millis)
	{
		if (millis <= 0)
		{
			return "not yet";
		}
		long minutes = Math.max(0, (model.now - millis) / 60_000L);
		if (minutes < 1)
		{
			return "just now";
		}
		if (minutes < 60)
		{
			return minutes + " min ago";
		}
		long hours = minutes / 60;
		if (hours < 24)
		{
			return hours + " h ago";
		}
		long days = hours / 24;
		return days == 1 ? "yesterday" : days + " days ago";
	}
}
