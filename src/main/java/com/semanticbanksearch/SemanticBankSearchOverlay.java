package com.semanticbanksearch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class SemanticBankSearchOverlay extends WidgetItemOverlay
{
	private static final Color FILL_COLOR = new Color(0, 170, 255, 45);
	private static final Color BORDER_COLOR = new Color(0, 190, 255, 220);

	private final SemanticBankSearchConfig config;
	private Set<Integer> highlightedItemIds = Collections.emptySet();

	public SemanticBankSearchOverlay(SemanticBankSearchConfig config)
	{
		this.config = config;
		showOnBank();
	}

	public void setHighlightedItemIds(Collection<Integer> itemIds)
	{
		if (itemIds == null || itemIds.isEmpty())
		{
			highlightedItemIds = Collections.emptySet();
			return;
		}

		highlightedItemIds = new HashSet<>(itemIds);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (config == null || !config.enableHighlights() || !highlightedItemIds.contains(itemId) || widgetItem == null)
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();
		if (bounds == null)
		{
			return;
		}

		graphics.setStroke(new BasicStroke(2f));
		graphics.setColor(FILL_COLOR);
		graphics.fill(bounds);
		graphics.setColor(BORDER_COLOR);
		graphics.draw(bounds);
	}
}
