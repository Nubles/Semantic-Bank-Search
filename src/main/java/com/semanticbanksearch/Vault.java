package com.semanticbanksearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * Everything the plugin has seen this account own, place by place: the bank, the inventory,
 * worn gear and the storage the game lets RuneLite read. Each place keeps the last picture of
 * its contents and when that picture was taken. Saved per account in RuneLite's profile config.
 */
final class Vault
{
	static final String BANK = "Bank";
	static final String INVENTORY = "Inventory";
	static final String WORN = "Worn";
	static final String SEED_VAULT = "Seed vault";
	static final String GROUP_STORAGE = "Group storage";

	/** Display order: where you would look first. */
	static final List<String> PLACES = Collections.unmodifiableList(
		Arrays.asList(BANK, INVENTORY, WORN, SEED_VAULT, GROUP_STORAGE));

	static final class Place
	{
		long seen;
		Map<Integer, Integer> items = new LinkedHashMap<>();
	}

	Map<String, Place> places = new LinkedHashMap<>();

	/**
	 * Replaces what a place holds.
	 *
	 * @return whether its contents changed
	 */
	boolean replace(String name, Map<Integer, Integer> items, long now)
	{
		Place place = places.get(name);
		if (place == null)
		{
			place = new Place();
			places.put(name, place);
		}
		boolean changed = !place.items.equals(items) || place.seen == 0;
		place.items = new LinkedHashMap<>(items);
		place.seen = now;
		return changed;
	}

	Place place(String name)
	{
		return places.get(name);
	}

	boolean isEmpty()
	{
		for (Place place : places.values())
		{
			if (!place.items.isEmpty())
			{
				return false;
			}
		}
		return true;
	}

	/** Distinct items across every place. */
	int distinctItems()
	{
		Set<Integer> ids = new HashSet<>();
		for (Place place : places.values())
		{
			ids.addAll(place.items.keySet());
		}
		return ids.size();
	}

	/** Gson leaves fields null when a saved vault lacks them; this puts them back. */
	Vault repair()
	{
		if (places == null)
		{
			places = new LinkedHashMap<>();
		}
		places.values().removeIf(p -> p == null);
		for (Place place : places.values())
		{
			if (place.items == null)
			{
				place.items = new LinkedHashMap<>();
			}
			place.items.entrySet().removeIf(e -> e.getKey() == null || e.getValue() == null || e.getValue() <= 0);
		}
		return this;
	}

	/**
	 * Builds the search index. Items in a place that is open on screen right now are marked
	 * visible, so the bank overlay knows what it can light up.
	 */
	StorageIndex toIndex(IntFunction<String> names, Set<String> openPlaces)
	{
		StorageIndex index = new StorageIndex();
		for (String name : PLACES)
		{
			Place place = places.get(name);
			if (place == null)
			{
				continue;
			}
			StorageSourceType type = BANK.equals(name) ? StorageSourceType.BANK : StorageSourceType.OTHER_STORAGE;
			boolean open = openPlaces.contains(name);
			for (Map.Entry<Integer, Integer> entry : place.items.entrySet())
			{
				String itemName = names.apply(entry.getKey());
				if (itemName != null && !itemName.isEmpty())
				{
					index.record(entry.getKey(), itemName, entry.getValue(), type, name, open, place.seen);
				}
			}
		}
		return index;
	}
}
