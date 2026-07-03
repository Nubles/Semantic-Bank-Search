# Coverage Audit Design

## Goal

Add a local-only coverage audit workflow that shows which observed bank and storage items are already understood by the semantic rule library, which items are still uncovered, and why each covered item is considered covered.

## Problem

Semantic Bank Search now has enough categories that adding more rules by intuition is becoming less reliable. The plugin needs a way to answer a practical question: "Which items in my observed storage can the semantic database explain?"

Without that view, coverage improvements are hard to measure. A larger rule database could still miss common items, and broad new patterns could accidentally create false positives.

## Proposed Approach

Create a reusable semantic coverage analyzer that evaluates each `ObservedItem` against the current `SemanticRule` list without requiring a player query. The analyzer returns one coverage row per observed item, including item details, matched categories, matched reasons, best score, and whether the item is covered.

The plugin panel will gain a coverage audit view next to the existing search and all-indexed views. The audit view will sort uncovered items first, then covered items, so missing coverage is immediately visible. Covered rows should show category/reason summaries so users and contributors can see why an item is understood.

## Architecture

- `SemanticCoverageAnalyzer` owns item-to-rule coverage analysis.
- `SemanticCoverageResult` is a simple immutable row model for panel rendering and tests.
- `SemanticRule` exposes read-only coverage helpers so the analyzer does not duplicate rule matching logic.
- `SemanticBankSearchPanel` renders coverage rows using existing Swing card patterns.
- `SemanticBankSearchPlugin` adds a `COVERAGE_AUDIT` panel mode and refreshes it whenever observed storage changes.

## Data Flow

1. The storage index records observed items from the bank and safe observed storage sources.
2. The player clicks a new `Coverage` button in the side panel.
3. The plugin asks `SemanticCoverageAnalyzer` to analyze `index.items()`.
4. The panel renders uncovered items first, then covered items.
5. No network, external AI, menu modification, item action, withdrawal, or unobserved storage inference is introduced.

## UI Behavior

The coverage audit view should be practical and compact:

- Status line: `Covered 42 of 55 observed items.`
- Uncovered rows: item name, source, quantity, visible/remembered state, `No semantic category yet.`
- Covered rows: item name, source, quantity, matched categories, and a concise reason summary.
- Empty state: asks the user to open the bank or observed storage so items can be indexed.

This is an audit and contributor workflow, not an automatic item classifier. It should not highlight items in the bank, because the player has not asked for a specific purpose query.

## Testing

Coverage tests should use small fixture banks with expected covered and uncovered items. The first test set should include:

- A mixed travel and utility bank.
- A skilling and minigame bank.
- A deliberate uncovered set with placeholder/common items that should not be matched by overly broad rules.

The tests should verify:

- Covered items include expected categories.
- Uncovered items stay uncovered.
- Duplicate observed entries from different sources remain separate rows.
- The panel/plugin coverage mode does not set overlay highlights.

## Success Criteria

- Contributors can see which observed items are semantically covered.
- Coverage can be measured repeatedly in tests.
- New coverage work can be driven by uncovered item lists instead of guesswork.
- The plugin remains local-only and passive.
