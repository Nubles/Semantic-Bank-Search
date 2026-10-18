package com.semanticbanksearch;

import com.semanticbanksearch.ui.Theme;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/**
 * Lights up what the side panel found: a soft gold wash under the slot and a gilt outline
 * traced around the item's own silhouette, in the bank and the inventory.
 */
public class SemanticBankSearchOverlay extends WidgetItemOverlay
{
	private final SemanticBankSearchConfig config;
	private final ItemManager itemManager;
	private volatile Set<Integer> highlightedItemIds = Collections.emptySet();

	public SemanticBankSearchOverlay(SemanticBankSearchConfig config, ItemManager itemManager)
	{
		this.config = config;
		this.itemManager = itemManager;
		showOnBank();
		showOnInventory();
	}

	public void setHighlightedItemIds(Collection<Integer> itemIds)
	{
		highlightedItemIds = itemIds == null || itemIds.isEmpty()
			? Collections.emptySet()
			: Collections.unmodifiableSet(new HashSet<>(itemIds));
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		Set<Integer> ids = highlightedItemIds;
		if (ids.isEmpty() || !config.enableHighlights() || widgetItem == null)
		{
			return;
		}
		int canonical = itemManager.canonicalize(itemId);
		if (!ids.contains(itemId) && !ids.contains(canonical))
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();
		if (bounds == null)
		{
			return;
		}

		graphics.setColor(Theme.alpha(Theme.GOLD, 38));
		graphics.fillRoundRect(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2, 6, 6);
		BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), Theme.GOLD_BRIGHT);
		if (outline != null)
		{
			graphics.drawImage(outline, bounds.x, bounds.y, null);
		}
	}
}
