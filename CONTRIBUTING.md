# Contributing

Thanks for helping improve Semantic Bank Search. The plugin is intentionally local-only, read-only, and advisory, so contributions should preserve that safety model.

## Development

Use the Gradle wrapper from the repository root:

```powershell
.\gradlew.bat test
```

## Quality Workflow

Semantic coverage is protected by repeatable tests and scorecards:

- [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) explains how to add new purpose coverage safely.
- [Semantic QA Scorecard](docs/SEMANTIC_QA_SCORECARD.md) tracks curated player queries, expected positives, expected negatives, and current pass totals.

For semantic changes, run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticQaScorecardTest
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test --tests com.semanticbanksearch.BetaNativeBankQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest
.\gradlew.bat test
```

For the trustworthy runtime foundation, also run the focused checks:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountKeyTest
.\gradlew.bat test --tests com.semanticbanksearch.AccountStorageRepositoryTest
.\gradlew.bat test --tests com.semanticbanksearch.AccountSessionControllerTest
.\gradlew.bat test --tests com.semanticbanksearch.PanelViewSnapshotTest
```

## Adding Semantic Coverage

Read [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) before adding or changing rules.

The short version:

1. Start with a real player query.
2. Add expected positive and negative examples to the curated query pack.
3. Prefer phrase aliases over broad single-word aliases.
4. Keep item patterns specific enough to avoid false positives.
5. Make the smallest rule change needed to pass the tests.
6. Use mechanical item-awareness tags only as triage aids; do not treat them as semantic purpose coverage.

## Release QA

Before publishing a build, run the automated checks above, then in game confirm:

- Typing a name into the bank search works exactly as before, letter by letter.
- A purpose typed into the bank search adds the items that serve it; "sem " shows them alone.
- The side panel's Search, Trips, Vault and Gaps pages match what the bank holds, and matches light up in gold.

## Safety Rules

Contributions must not add:

- Network calls or external AI/API calls.
- Runtime downloads.
- Menu modification.
- Item clicking, moving, withdrawing, or depositing.
- Reflection or classloader behavior.
- Process execution.
- Storage inference for containers the client has not observed.

The plugin may list and highlight matching observed items, but it must not act on items.

Runtime persistence review rules:

- What the account owns is kept in RuneLite's per-account profile config (`setRSProfileConfiguration`), never in files.
- Never write a raw account hash or account display name anywhere.
- Swing code must not read client state; the plugin builds a `PanelModel` on the client thread and hands it to Swing whole.
- The bank search callback may turn items on; only the explicit "sem " prefix may turn items off.

## Before Opening A PR

Run:

```powershell
.\gradlew.bat test
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
```

If the safety scan returns matches from documentation or unchanged files, call that out in the PR and explain why they are not new runtime behavior.
