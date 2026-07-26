# Hybrid Deterministic Bank Search Design

## Purpose

Semantic Bank Search will become a local-only, deterministic RuneLite bank search
system that combines complete item awareness with natural purpose queries. It will
preserve ordinary RuneLite and Bank Tags searches, explain every semantic result,
and use the same matching behavior in the native bank field, sidebar, readiness
checks, coverage audits, and automated QA.

The product is not a runtime guide, automation tool, or remote AI client. It
answers one question: which items observed on this account match what the player
is trying to do?

## Success Criteria

- Bank data is isolated by account and never stored in RuneLite-synced
  configuration.
- Normal item-name searches and `tag:` searches are never silently replaced by a
  low-confidence semantic interpretation.
- Every valid item in the committed OSRS mapping snapshot resolves to a canonical
  catalogue entry or a clearly reported unknown-item state.
- Query compilation happens once per input revision, not once per bank item.
- Native bank search and sidebar search produce equivalent semantic decisions.
- Full-bank semantic evaluation supports at least 1,500 unique items without a
  perceptible client-thread pause.
- Every match has stable reason, category, score, and provenance information.
- Runtime behavior remains offline, passive, read-only, and compatible with
  Plugin Hub policy.

## Delivery Decomposition

The work is delivered as four independently testable milestones:

1. Trustworthy runtime foundation: account-local persistence, thread ownership,
   CI repair, and legacy-data removal.
2. Unified semantic core: item facts, compiled search plans, activation
   confidence, exact match modes, and cross-surface parity.
3. Complete deterministic catalogue: offline generation, reviewed overrides,
   provenance, conflict reporting, and a committed runtime artifact.
4. Best-in-class user experience and quality gates: virtualized result UI,
   explanations, saved searches, expanded readiness, full-catalog QA, and
   performance/compatibility gates.

Each milestone must leave the plugin usable and releasable. Later milestones may
extend interfaces established earlier but must not reintroduce parallel search
engines.

## Runtime Architecture

### Account Session

`AccountSessionController` owns the active account identity and lifecycle.
It listens for account hash and game-state changes, unloads the previous account
before switching, and exposes no persistent index while the account hash is
unknown.

Account keys use a domain-separated SHA-256 digest of the RuneLite account hash
rather than the raw value. Display names are never used in paths or stored
metadata.

### Local Persistence

`AccountStorageRepository` stores observed item state below:

`RuneLite.RUNELITE_DIR/semantic-bank-search/accounts/<account-key>/index.json`

The file contains schema version, catalogue version, observed item IDs,
quantities, sources, visibility state, and last-observed times. It does not store
display names. Writes use a temporary file and atomic replacement where the
filesystem supports it. A malformed file is quarantined and replaced with an
empty index.

Only non-sensitive user preferences remain in `ConfigManager`. On first startup
of the new storage version, the old shared `semanticbanksearch.index` key is
deleted rather than assigned to an arbitrary account. Opening the bank rebuilds
the account index.

The in-memory index has no arbitrary 800-item default. Limits are per account and
per source, with a default of 5,000 entries and a hard safety ceiling of 20,000.
Currently visible bank entries are never evicted.

### Thread Ownership

The RuneLite client thread owns account state, storage observation, item metadata
resolution, query compilation, and search evaluation. Swing event handlers post
commands through `ClientThread`; they never read `Client`, `ItemManager`, or the
mutable index directly.

UI updates receive immutable snapshots and are applied on Swing's event dispatch
thread. The runtime uses revision numbers to discard stale asynchronous renders.
This removes the need for shared mutable `StorageIndex` access or broad locks.

### Item Facts

`ItemFacts` is the single runtime representation of a bank item:

- observed item ID and canonical item ID
- canonical name and normalized name tokens
- noted, placeholder, and variation relationships
- inventory actions
- equipment slot and combat/prayer bonuses
- healing and dose data when RuneLite exposes them
- deterministic catalogue tags and capabilities
- source location, quantity, visibility, and last-observed time

Facts are immutable. `ItemFactsResolver` combines live RuneLite metadata with the
bundled catalogue and caches results by canonical item ID for the active client
session.

## Deterministic Catalogue

### Source And Refresh Model

Normal plugin builds never fetch data. They load a committed,
schema-validated `semantic_item_catalog.json` runtime resource.

An explicit developer-only catalogue pipeline performs these stages:

1. Import a cached OSRS item mapping snapshot.
2. Normalize canonical IDs, noted items, placeholders, and variations.
3. Derive mechanical facts from trusted item metadata.
4. Apply reviewed, version-controlled semantic overrides.
5. Validate taxonomy, conflicts, aliases, and required golden items.
6. Compile a compact deterministic runtime resource.
7. Produce a human-readable coverage, conflict, provenance, and item-diff report.

Network access is allowed only in the explicit refresh stage and must identify
the project with a descriptive user agent. Imported source snapshots are cached
with retrieval metadata. Source licences and required attribution are recorded
beside each imported snapshot. Pipeline output is reproducible from committed
inputs.

### Taxonomy

The taxonomy separates facts from recommendations:

- type: food, potion, rune, seed, tool, weapon, armour, jewellery, resource
- equipment: slot, combat style, attack type, defensive role, prayer bonus
- consumable: heal, combo-eat, dose, boost, restoration, protection
- utility: teleport destination, light, warmth, web cutting, clue utility
- activity: boss, Slayer, minigame, skilling workflow, travel, quest utility

Mechanical tags come from deterministic data. Context-dependent activity tags
require reviewed curation and provenance. Tags use stable identifiers independent
of display copy.

### Match Modes

Catalogue rules support explicit match modes:

- canonical item ID membership
- exact normalized name
- whole-token membership
- ordered token phrase
- capability predicate
- numeric predicate

Unrestricted substring matching is prohibited. This prevents cases such as
`sword` matching Swordfish, `axe` matching Pickaxe, `bow` matching Bow string,
and `bolt` matching Bolt of cloth.

## Query Model

### Compilation

`SemanticQueryCompiler.compile(String, QueryContext)` returns an immutable
`SearchPlan`. A plan contains:

- activation mode
- normalized query and recognized aliases
- AND clause groups with OR alternatives
- excluded constraints
- numeric comparisons
- optional owned-item ranking
- matched activity pack
- confidence and user-facing interpretation

Compilation resolves aliases and relevant catalogue tags once. Item evaluation
does not scan the complete rule library.

### Activation

The compiler returns one of three activation modes:

- `PASS_THROUGH`: no confident semantic intent; RuneLite handles the query.
- `ADDITIVE`: an ambiguous but supported short intent; semantic matches are added
  while native name matches remain possible.
- `SEMANTIC_ONLY`: an explicit `sem:`/`sem ` query or a high-confidence
  multiword/compound purpose query.

`tag:` always passes through unchanged. Prefix completion and fuzzy matching are
available only after an explicit semantic prefix. Natural fuzzy matching requires
safe medium/long aliases and may never activate from category-word prefixes.

### Evaluation

`SearchPlan.evaluate(ItemFacts, OwnedItemSet)` returns `MatchResult` containing:

- matched state
- score
- categories
- concise reasons
- provenance

Clause groups use AND semantics; alternatives within a group use OR semantics.
Negative constraints are evaluated last. Owned-item ranking uses a precomputed
ranking context.

Terms such as "strongest" must be truthful. Until attack speed, ammunition or
spell, player stats, and target context are available, the UI describes results
as "highest owned offensive bonus score" rather than claiming highest damage.

## Search Surfaces

`SemanticSearchService` is the only consumer-facing search API. Native bank
filtering, sidebar search, readiness slots, coverage, and tests all call it.
The existing parallel paths in `SemanticSearchEngine` and `SemanticBankFilter`
are migrated behind this service and then removed.

The native `bankSearchFilter` callback remains the primary interaction. It
compiles on input change, reuses one plan and facts cache for every callback, and
does not alter Bank Tags or menu behavior.

The sidebar shows the current interpretation, result count, owned source,
match explanation, and readiness state. It does not duplicate a weaker search
implementation.

## User Experience

The sidebar uses a virtualized `JList` with lightweight renderers instead of one
Swing component tree per item. Primary modes are Search, Readiness, Saved, and
Coverage. Developer-level coverage details remain collapsed by default.

Useful features are:

- current semantic interpretation and explicit active-mode indicator
- reasons such as "Restores prayer" or "Teleports near Barrows"
- source filters for bank, Seed Vault, and Group Storage
- recent and locally saved queries
- local include/exclude corrections for an item and semantic intent
- readiness substitutes and honest "not observed" missing states
- clear-account-data action

Corrections are account-local data. They never mutate global catalogue files and
can be exported deliberately as a review fixture without including bank contents.

## Error Handling

- Unknown item: preserve native name matching and show an unknown catalogue state.
- Missing catalogue: start in metadata-only degraded mode and report the problem.
- Catalogue schema mismatch: reject the artifact and use degraded mode.
- Corrupt account index: quarantine it, start empty, and show a non-blocking notice.
- Account switch: clear facts, search plans, highlights, results, and mutable state
  before loading the next account.
- Metadata failure: fail closed for numeric/ranking predicates but preserve
  ordinary name search.
- Storage write failure: keep the in-memory index, rate-limit warnings, and retry
  on the next scheduled persistence point.

## Quality System

### Semantic Corpus

The QA corpus evaluates each query against the complete bundled catalogue, not a
fixture containing only named positives and negatives. Cases use canonical item
IDs and include:

- expected required positives
- allowed alternatives
- expected negatives and near misses
- activation mode
- expected interpretation
- supported account/storage context

Initial release gates require at least 200 player-style queries across combat,
travel, utility, skilling, minigames, clues, boss preparation, consumables, and
ordinary name-search compatibility.

### Required Tests

- account isolation and account-switch clearing
- proof that bank contents are absent from `ConfigManager`
- atomic persistence and corrupt-file recovery
- `tag:` and unrecognized-query pass-through
- ambiguous short-query additive behavior
- explicit semantic-only behavior
- no Swordfish/Pickaxe/Bow string/Bolt of cloth category bleed
- native/sidebar/readiness parity
- complete-catalog canonicalization and conflict validation
- catalogue generation reproducibility
- Bank Tags subscription-order compatibility
- 1,500-item compilation/evaluation workload with one facts resolution per item
- Swing virtualization and stale-render rejection

### Performance Gates

The build records, but does not use a flaky single-run wall-clock assertion for,
these targets on the reference development machine:

- query compilation median below 2 ms
- 1,500-item evaluation median below 20 ms
- no repeated full-rule-library scan per item
- one item-facts resolution per canonical item per account session

Automated tests enforce operation counts, cache behavior, and a generous timeout.
A repeatable benchmark task reports median, p95, and maximum timings.

## Release Gates

Before advancing the Plugin Hub marker:

1. Linux CI passes with executable Gradle wrapper metadata.
2. Full Gradle and catalogue-pipeline tests pass.
3. Runtime safety scan confirms no runtime networking or automation.
4. Account isolation and local-storage migration are manually verified.
5. Native search is tested with Bank, Bank Tags, layouts/placeholders, Seed Vault,
   and Group Storage.
6. The 200-query full-catalog scorecard meets its committed precision and recall
   thresholds with zero protected compatibility regressions.
7. Manual authenticated QA records RuneLite version, operating system, bank size,
   enabled compatibility plugins, and screenshots for representative flows.

## Non-Goals

- Runtime network access, remote AI, telemetry, or downloaded semantic data
- Clicking, withdrawing, depositing, tagging, moving, or menu alteration
- Inferring unopened POH or storage contents
- Replacing boss guides, gear calculators, or exact damage-per-second tools
- Claiming that an unobserved item is not owned
- Silent automatic promotion of generated or AI-proposed semantic tags
