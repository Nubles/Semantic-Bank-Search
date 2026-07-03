package com.semanticbanksearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class SemanticBankSearchPanel extends PluginPanel
{
	private static final String[] EXAMPLE_SEARCHES = {
		"teleport near barrows",
		"crush weapons",
		"poison protection",
		"prayer restoration",
		"warm clothing",
		"things that cut webs",
		"fastest food I own"
	};

	private final Consumer<String> searchConsumer;
	private final Runnable allIndexedConsumer;
	private final Runnable coverageAuditConsumer;
	private final boolean coverageAuditAvailable;
	private final Runnable clearConsumer;
	private final JPanel resultsContainer = new JPanel();
	private final JLabel statusLabel = new JLabel(" ");
	private final JTextField searchField = new JTextField();
	private int renderSequence;

	public SemanticBankSearchPanel(Consumer<String> searchConsumer, Runnable allIndexedConsumer, Runnable clearConsumer)
	{
		this(searchConsumer, allIndexedConsumer, null, clearConsumer);
	}

	public SemanticBankSearchPanel(
		Consumer<String> searchConsumer,
		Runnable allIndexedConsumer,
		Runnable coverageAuditConsumer,
		Runnable clearConsumer)
	{
		super();
		this.searchConsumer = searchConsumer == null ? ignored -> { } : searchConsumer;
		this.allIndexedConsumer = allIndexedConsumer == null ? () -> { } : allIndexedConsumer;
		this.coverageAuditAvailable = coverageAuditConsumer != null;
		this.coverageAuditConsumer = coverageAuditConsumer == null ? () -> { } : coverageAuditConsumer;
		this.clearConsumer = clearConsumer == null ? () -> { } : clearConsumer;

		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
		resultsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(searchControls(), BorderLayout.NORTH);
		add(new JScrollPane(resultsContainer), BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		searchField.addActionListener(event -> runSearch(searchField.getText()));
		clearResults();
	}

	public void updateResults(String query, List<SemanticSearchResult> results, String status)
	{
		int sequence = nextRenderSequence();
		List<SemanticSearchResult> safeResults = results == null ? Collections.emptyList() : new ArrayList<>(results);
		String safeStatus = status == null ? "" : status;
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> renderResults(sequence, safeResults, safeStatus));
			return;
		}

		renderResults(sequence, safeResults, safeStatus);
	}

	public void updateIndexedItems(List<ObservedItem> items, String status)
	{
		int sequence = nextRenderSequence();
		List<ObservedItem> safeItems = copyObservedItems(items);
		String safeStatus = status == null ? "" : status;
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> renderIndexedItems(sequence, safeItems, safeStatus));
			return;
		}

		renderIndexedItems(sequence, safeItems, safeStatus);
	}

	public void updateCoverageAudit(List<SemanticCoverageResult> results, String status)
	{
		int sequence = nextRenderSequence();
		List<SemanticCoverageResult> safeResults = results == null ? Collections.emptyList() : new ArrayList<>(results);
		String safeStatus = status == null ? "" : status;
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> renderCoverageAudit(sequence, safeResults, safeStatus));
			return;
		}

		renderCoverageAudit(sequence, safeResults, safeStatus);
	}

	public void clearResults()
	{
		int sequence = nextRenderSequence();
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> renderClearResults(sequence));
			return;
		}

		renderClearResults(sequence);
	}

	private void renderResults(int sequence, List<SemanticSearchResult> results, String status)
	{
		if (!isLatestRenderSequence(sequence))
		{
			return;
		}

		resultsContainer.removeAll();
		statusLabel.setText(status.trim().isEmpty() ? " " : status.trim());

		if (results.isEmpty())
		{
			resultsContainer.add(textBlock(
				"No matching items",
				"Try another purpose, exact item name, or open storage so the plugin can observe more owned items."));
		}
		else
		{
			for (SemanticSearchResult result : results)
			{
				resultsContainer.add(resultCard(result));
			}
		}

		revalidate();
		repaint();
	}

	private void renderIndexedItems(int sequence, List<ObservedItem> items, String status)
	{
		if (!isLatestRenderSequence(sequence))
		{
			return;
		}

		resultsContainer.removeAll();
		statusLabel.setText(status.trim().isEmpty() ? "" : status.trim());

		if (items.isEmpty())
		{
			resultsContainer.add(textBlock(
				"No indexed items yet",
				"Open your bank so Semantic Bank Search can observe and index visible bank items."));
		}
		else
		{
			for (ObservedItem item : items)
			{
				resultsContainer.add(indexedItemCard(item));
			}
		}

		revalidate();
		repaint();
	}

	private void renderCoverageAudit(int sequence, List<SemanticCoverageResult> results, String status)
	{
		if (!isLatestRenderSequence(sequence))
		{
			return;
		}

		resultsContainer.removeAll();
		statusLabel.setText(status.trim().isEmpty() ? "" : status.trim());

		if (results.isEmpty())
		{
			resultsContainer.add(textBlock(
				"No indexed items yet",
				"Open your bank so Semantic Bank Search can audit observed item coverage."));
		}
		else
		{
			for (SemanticCoverageResult result : results)
			{
				resultsContainer.add(coverageCard(result));
			}
		}

		revalidate();
		repaint();
	}

	private void renderClearResults(int sequence)
	{
		if (!isLatestRenderSequence(sequence))
		{
			return;
		}

		resultsContainer.removeAll();
		statusLabel.setText(" ");
		resultsContainer.add(textBlock(
			"Search your observed items",
			"Type what you need, like poison protection or warm clothing, then press Enter."));
		revalidate();
		repaint();
	}

	private synchronized int nextRenderSequence()
	{
		return ++renderSequence;
	}

	private synchronized boolean isLatestRenderSequence(int sequence)
	{
		return sequence == renderSequence;
	}

	private JPanel searchControls()
	{
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JLabel title = new JLabel("Semantic Bank Search");
		title.setForeground(ColorScheme.BRAND_ORANGE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
		panel.add(title, BorderLayout.NORTH);

		JPanel controls = new JPanel(new BorderLayout(0, 4));
		controls.setOpaque(false);
		controls.add(searchField, BorderLayout.NORTH);

		JPanel buttons = new JPanel(new GridLayout(1, 0, 4, 0));
		buttons.setOpaque(false);

		JButton searchButton = new JButton("Search");
		searchButton.setFocusable(false);
		searchButton.addActionListener(event -> runSearch(searchField.getText()));
		buttons.add(searchButton);

		JButton allIndexedButton = new JButton("All Indexed");
		allIndexedButton.setFocusable(false);
		allIndexedButton.addActionListener(event -> allIndexedConsumer.run());
		buttons.add(allIndexedButton);

		if (coverageAuditAvailable)
		{
			JButton coverageButton = new JButton("Coverage");
			coverageButton.setFocusable(false);
			coverageButton.addActionListener(event -> coverageAuditConsumer.run());
			buttons.add(coverageButton);
		}

		JButton clearButton = new JButton("Clear");
		clearButton.setFocusable(false);
		clearButton.addActionListener(event -> {
			searchField.setText("");
			clearConsumer.run();
			clearResults();
		});
		buttons.add(clearButton);

		controls.add(buttons, BorderLayout.SOUTH);
		panel.add(controls, BorderLayout.CENTER);

		JPanel examples = new JPanel(new GridLayout(0, 1, 0, 4));
		examples.setOpaque(false);
		for (String example : EXAMPLE_SEARCHES)
		{
			JButton button = new JButton(example);
			button.setFocusable(false);
			button.addActionListener(event -> {
				searchField.setText(example);
				runSearch(example);
			});
			examples.add(button);
		}
		panel.add(examples, BorderLayout.SOUTH);

		return panel;
	}

	private static List<ObservedItem> copyObservedItems(List<ObservedItem> items)
	{
		List<ObservedItem> safeItems = new ArrayList<>();
		if (items == null)
		{
			return safeItems;
		}

		for (ObservedItem item : items)
		{
			if (item != null)
			{
				safeItems.add(item);
			}
		}
		return safeItems;
	}

	private void runSearch(String query)
	{
		searchConsumer.accept(query == null ? "" : query.trim());
	}

	private static JPanel resultCard(SemanticSearchResult result)
	{
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));

		JLabel title = new JLabel(result.getItemName());
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		panel.add(title, BorderLayout.NORTH);

		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		body.add(detailLabel(result.getCategory()));
		body.add(wrappedText(result.getReason()));
		body.add(detailLabel(details(result)));
		panel.add(body, BorderLayout.CENTER);
		return panel;
	}

	private static JPanel indexedItemCard(ObservedItem item)
	{
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));

		JLabel title = new JLabel(item.getName());
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		panel.add(title, BorderLayout.NORTH);

		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		body.add(detailLabel(indexedDetails(item)));
		panel.add(body, BorderLayout.CENTER);
		return panel;
	}

	private static JPanel coverageCard(SemanticCoverageResult result)
	{
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)));

		JLabel title = new JLabel(result.getItemName());
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		panel.add(title, BorderLayout.NORTH);

		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);
		body.add(detailLabel(coverageDetails(result)));

		String categories = joinLimited(result.getCategories(), 3);
		if (categories.isEmpty())
		{
			body.add(wrappedText("No semantic category yet."));
		}
		else
		{
			String reasons = joinLimited(result.getReasons(), 2);
			body.add(wrappedText(reasons.isEmpty() ? "Matched by semantic coverage rules." : reasons));
		}
		panel.add(body, BorderLayout.CENTER);
		return panel;
	}

	private static JPanel textBlock(String title, String body)
	{
		JPanel panel = new JPanel(new BorderLayout(0, 6));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(ColorScheme.BRAND_ORANGE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		panel.add(titleLabel, BorderLayout.NORTH);
		panel.add(wrappedText(body), BorderLayout.CENTER);
		return panel;
	}

	private static JLabel detailLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setFont(label.getFont().deriveFont(11f));
		return label;
	}

	private static JTextArea wrappedText(String body)
	{
		JTextArea text = new JTextArea(body == null ? "" : body);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(false);
		text.setFocusable(false);
		text.setOpaque(false);
		text.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return text;
	}

	private static String details(SemanticSearchResult result)
	{
		String source = result.getSourceName().isEmpty()
			? result.getSourceType().name()
			: result.getSourceName();
		String highlightState = result.isHighlightable() ? "highlightable" : "not highlightable";
		return "Source " + source
			+ " | Quantity " + result.getQuantity()
			+ " | " + highlightState;
	}

	private static String indexedDetails(ObservedItem item)
	{
		String source = item.getSourceName().isEmpty()
			? item.getSourceType().name()
			: item.getSourceName();
		String highlightState = item.isCurrentlyVisible() && item.getSourceType() == StorageSourceType.BANK
			? "visible in bank"
			: "remembered";
		return "Source " + source
			+ " | Quantity " + item.getQuantity()
			+ " | " + highlightState;
	}

	private static String coverageDetails(SemanticCoverageResult result)
	{
		String source = result.getSourceName().isEmpty()
			? result.getSourceType().name()
			: result.getSourceName();
		String highlightState = result.isCurrentlyVisible() && result.getSourceType() == StorageSourceType.BANK
			? "visible in bank"
			: "remembered";
		String categories = joinLimited(result.getCategories(), 3);
		String coverage = categories.isEmpty()
			? " | Uncovered"
			: " | Categories " + categories;
		return "Source " + source
			+ " | Quantity " + result.getQuantity()
			+ " | " + highlightState
			+ coverage;
	}

	private static String joinLimited(List<String> values, int limit)
	{
		if (values == null || values.isEmpty() || limit <= 0)
		{
			return "";
		}

		List<String> joinedValues = new ArrayList<>();
		int remaining = 0;
		for (String value : values)
		{
			if (value == null || value.trim().isEmpty())
			{
				continue;
			}

			if (joinedValues.size() < limit)
			{
				joinedValues.add(value.trim());
			}
			else
			{
				remaining++;
			}
		}

		String joined = String.join(", ", joinedValues);
		if (remaining > 0)
		{
			return joined + ", +" + remaining + " more";
		}
		return joined;
	}
}
