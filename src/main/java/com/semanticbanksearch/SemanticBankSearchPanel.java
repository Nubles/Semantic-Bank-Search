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
	private final Runnable clearConsumer;
	private final JPanel resultsContainer = new JPanel();
	private final JLabel statusLabel = new JLabel(" ");
	private final JTextField searchField = new JTextField();

	public SemanticBankSearchPanel(Consumer<String> searchConsumer, Runnable clearConsumer)
	{
		super();
		this.searchConsumer = searchConsumer == null ? ignored -> { } : searchConsumer;
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
		List<SemanticSearchResult> safeResults = results == null ? Collections.emptyList() : new ArrayList<>(results);
		resultsContainer.removeAll();
		searchField.setText(query == null ? "" : query);
		statusLabel.setText(status == null || status.trim().isEmpty() ? " " : status.trim());

		if (safeResults.isEmpty())
		{
			resultsContainer.add(textBlock(
				"No matching items",
				"Try another purpose, exact item name, or open storage so the plugin can observe more owned items."));
		}
		else
		{
			for (SemanticSearchResult result : safeResults)
			{
				resultsContainer.add(resultCard(result));
			}
		}

		revalidate();
		repaint();
	}

	public void clearResults()
	{
		resultsContainer.removeAll();
		statusLabel.setText(" ");
		resultsContainer.add(textBlock(
			"Search your observed items",
			"Type what you need, like poison protection or warm clothing, then press Enter."));
		revalidate();
		repaint();
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

		JPanel controls = new JPanel(new BorderLayout(4, 4));
		controls.setOpaque(false);
		controls.add(searchField, BorderLayout.CENTER);

		JButton clearButton = new JButton("Clear");
		clearButton.setFocusable(false);
		clearButton.addActionListener(event -> {
			searchField.setText("");
			clearConsumer.run();
			clearResults();
		});
		controls.add(clearButton, BorderLayout.EAST);
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
}
