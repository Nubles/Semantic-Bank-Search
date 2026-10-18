package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntFunction;
import net.runelite.api.EquipmentInventorySlot;

/**
 * Answers a question about what the account owns. It is the same reading the bank search uses,
 * so the side panel and the bank always agree: a purpose ("prayer restoration"), a kind of item
 * ("weapons", "food"), a measure ("food over 18 hp"), a comparison ("best melee weapon"), a
 * combination ("4 dose prayer pots"), or plainly an item's name. Every answer says why it matched.
 */
final class Finder
{
	static final class Hit
	{
		final int itemId;
		final String name;
		final long quantity;
		final List<String> places;
		final String category;
		final String reason;
		final int score;
		final boolean inOpenBank;

		Hit(int itemId, String name, long quantity, List<String> places, String category, String reason, int score, boolean inOpenBank)
		{
			this.itemId = itemId;
			this.name = name;
			this.quantity = quantity;
			this.places = places;
			this.category = category;
			this.reason = reason;
			this.score = score;
			this.inOpenBank = inOpenBank;
		}
	}

	static final class Finding
	{
		final String query;
		/** How the question was read, for the panel to show; empty when it was only a name. */
		final String reading;
		final boolean understood;
		final List<Hit> hits;

		Finding(String query, String reading, boolean understood, List<Hit> hits)
		{
			this.query = query;
			this.reading = reading;
			this.understood = understood;
			this.hits = hits;
		}

		static Finding empty()
		{
			return new Finding("", "", false, Collections.emptyList());
		}
	}

	static final String NAME_MATCH = "Name match";

	private final SemanticSearchEngine engine;
	private final IntFunction<BankItemMetadata> metadata;
	private final RelativeRankingClassifier ranking = new RelativeRankingClassifier();

	Finder(SemanticSearchEngine engine, IntFunction<BankItemMetadata> metadata)
	{
		this.engine = engine;
		this.metadata = metadata;
	}

	Finding find(String query, StorageIndex index)
	{
		String normalized = SemanticSearchEngine.normalize(query);
		if (normalized.isEmpty() || index == null)
		{
			return Finding.empty();
		}

		List<ObservedItem> items = index.items();
		Map<Integer, BankItemMetadata> cache = new HashMap<>();
		IntFunction<BankItemMetadata> facts = id -> cache.computeIfAbsent(id, metadata::apply);
		List<Integer> ownedIds = new ArrayList<>();
		for (ObservedItem item : items)
		{
			if (!ownedIds.contains(item.getItemId()))
			{
				ownedIds.add(item.getItemId());
			}
		}
		SemanticBankFilter filter = new SemanticBankFilter(engine, facts, () -> ownedIds);
		boolean semantic = filter.isSemantic(normalized);
		String purpose = filter.semanticQuery(normalized);
		boolean ranked = semantic && ranking.recognizes(purpose);
		boolean kind = semantic && filter.clauses(normalized).isEmpty() && filter.isKindQuery(purpose);

		Map<Integer, Hit> hits = new LinkedHashMap<>();
		for (ObservedItem item : items)
		{
			int id = item.getItemId();
			Hit existing = hits.get(id);
			if (existing != null)
			{
				hits.put(id, merge(existing, item));
				continue;
			}

			boolean byPurpose = semantic && Boolean.TRUE.equals(filter.decision(normalized, id));
			boolean byName = !normalized.startsWith(SemanticBankFilter.FORCE_PREFIX)
				&& SemanticSearchEngine.nameMatches(normalized, item.getName());
			if (!byPurpose && !byName)
			{
				continue;
			}

			String category;
			String reason;
			int score;
			BankItemMetadata fact = facts.apply(id);
			if (byPurpose && ranked)
			{
				category = "Best you own";
				reason = ranking.explanation(purpose, fact);
				score = ranking.score(purpose, fact);
			}
			else if (byPurpose && kind)
			{
				category = describeKind(fact);
				reason = describe(fact);
				score = 50;
			}
			else if (byPurpose)
			{
				SemanticRule rule = bestRule(filter.clauses(normalized), purpose, item.getName());
				category = rule != null ? rule.getCategory() : describeKind(fact);
				reason = rule != null ? rule.getReason() : describe(fact);
				score = rule != null ? rule.scoreFor(SemanticSearchEngine.normalize(item.getName())) : 50;
			}
			else
			{
				category = NAME_MATCH;
				reason = "Its name contains \"" + query.trim() + "\".";
				score = 0;
			}
			List<String> places = new ArrayList<>();
			places.add(item.getSourceName());
			hits.put(id, new Hit(id, item.getName(), item.getQuantity(), places, category, reason, score,
				inOpenBank(item)));
		}

		List<Hit> sorted = new ArrayList<>(hits.values());
		sorted.sort(Comparator
			.comparingInt((Hit h) -> h.score).reversed()
			.thenComparing((Hit h) -> h.quantity, Comparator.reverseOrder())
			.thenComparing(h -> h.name, String.CASE_INSENSITIVE_ORDER));
		return new Finding(query.trim(), semantic ? reading(filter, purpose) : "", semantic, sorted);
	}

	private SemanticRule bestRule(List<String> clauses, String purpose, String itemName)
	{
		if (clauses.isEmpty())
		{
			return engine.bestRule(purpose, itemName);
		}
		for (String clause : clauses)
		{
			SemanticRule rule = engine.bestRule(clause, itemName);
			if (rule != null)
			{
				return rule;
			}
		}
		return null;
	}

	private static Hit merge(Hit hit, ObservedItem item)
	{
		List<String> places = new ArrayList<>(hit.places);
		if (!places.contains(item.getSourceName()))
		{
			places.add(item.getSourceName());
		}
		return new Hit(hit.itemId, hit.name, hit.quantity + item.getQuantity(), places, hit.category, hit.reason,
			hit.score, hit.inOpenBank || inOpenBank(item));
	}

	private static boolean inOpenBank(ObservedItem item)
	{
		return item.isCurrentlyVisible() && item.getSourceType() == StorageSourceType.BANK;
	}

	/** A short line saying how the question was understood. */
	private String reading(SemanticBankFilter filter, String purpose)
	{
		List<String> clauses = filter.clauses(purpose);
		if (!clauses.isEmpty())
		{
			List<String> parts = new ArrayList<>();
			for (String clause : clauses)
			{
				parts.add(capitalise(clause));
			}
			return String.join(" + ", parts);
		}
		if (filter.isKindQuery(purpose))
		{
			return capitalise(purpose);
		}
		List<String> categories = engine.categories(purpose);
		if (!categories.isEmpty())
		{
			return String.join(", ", categories.subList(0, Math.min(3, categories.size())))
				+ (categories.size() > 3 ? " and " + (categories.size() - 3) + " more" : "");
		}
		return capitalise(purpose);
	}

	static String describeKind(BankItemMetadata item)
	{
		if (item == null)
		{
			return "Owned item";
		}
		if (item.isEdible())
		{
			return "Food";
		}
		if (item.isDrinkable())
		{
			return "Potions";
		}
		if (item.getEquipmentSlot() == EquipmentInventorySlot.WEAPON.getSlotIdx() || item.isWieldable())
		{
			return "Weapons";
		}
		if (item.isEquipable())
		{
			return "Equipment";
		}
		return "Owned item";
	}

	/** What the game's own numbers say about an item, for matches made by kind or measure. */
	static String describe(BankItemMetadata item)
	{
		if (item == null)
		{
			return "";
		}
		if (item.isEdible())
		{
			return item.getHealing() >= 0 ? "Heals " + item.getHealing() + " hitpoints." : "Can be eaten.";
		}
		if (item.isDrinkable())
		{
			return "Can be drunk.";
		}
		if (item.isEquipable())
		{
			List<String> parts = new ArrayList<>();
			int melee = item.meleeScore();
			int ranged = item.rangedScore();
			float magic = item.magicScore();
			if (melee > 0 && melee >= ranged && melee >= magic)
			{
				parts.add("melee " + melee);
			}
			else if (ranged > 0 && ranged >= magic)
			{
				parts.add("ranged " + ranged);
			}
			else if (magic > 0)
			{
				parts.add("magic " + Math.round(magic));
			}
			if (item.getPrayer() > 0)
			{
				parts.add("+" + item.getPrayer() + " prayer");
			}
			return parts.isEmpty() ? "Can be worn." : "Worn for " + String.join(", ", parts) + ".";
		}
		return "";
	}

	private static String capitalise(String text)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}
		return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
	}
}
