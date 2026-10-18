package com.semanticbanksearch;

import java.util.Collections;
import java.util.List;

/** Everything the side panel shows, built on the client thread and handed whole to Swing. */
final class PanelModel
{
	static final class PlaceSummary
	{
		final String name;
		final int items;
		final long quantity;
		final long seen;
		final boolean open;

		PlaceSummary(String name, int items, long quantity, long seen, boolean open)
		{
			this.name = name;
			this.items = items;
			this.quantity = quantity;
			this.seen = seen;
			this.open = open;
		}
	}

	final boolean loggedIn;
	final String player;
	final boolean bankSeen;
	final boolean bankOpen;
	final boolean bankSearch;
	final int distinctItems;
	final List<PlaceSummary> places;
	final List<ObservedItem> items;
	final List<ReadinessResult> trips;
	final List<SemanticCoverageResult> coverage;
	final Finder.Finding finding;
	final long now;

	PanelModel(boolean loggedIn, String player, boolean bankSeen, boolean bankOpen, boolean bankSearch, int distinctItems,
		List<PlaceSummary> places, List<ObservedItem> items, List<ReadinessResult> trips,
		List<SemanticCoverageResult> coverage, Finder.Finding finding, long now)
	{
		this.loggedIn = loggedIn;
		this.player = player;
		this.bankSeen = bankSeen;
		this.bankOpen = bankOpen;
		this.bankSearch = bankSearch;
		this.distinctItems = distinctItems;
		this.places = places;
		this.items = items;
		this.trips = trips;
		this.coverage = coverage;
		this.finding = finding;
		this.now = now;
	}

	static PanelModel loggedOut(long now)
	{
		return new PanelModel(false, null, false, false, true, -1, Collections.emptyList(), Collections.emptyList(),
			Collections.emptyList(), Collections.emptyList(), Finder.Finding.empty(), now);
	}
}
