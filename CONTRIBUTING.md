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
- [Beta Release Checklist](docs/BETA_RELEASE_CHECKLIST.md) covers the manual in-game QA needed before promoting a beta build.

For semantic changes, run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticQaScorecardTest
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest
.\gradlew.bat test
```

## Adding Semantic Coverage

Read [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) before adding or changing rules.

The short version:

1. Start with a real player query.
2. Add expected positive and negative examples to the curated query pack.
3. Prefer phrase aliases over broad single-word aliases.
4. Keep item patterns specific enough to avoid false positives.
5. Make the smallest rule change needed to pass the tests.`r`n6. Use mechanical item-awareness tags only as triage aids; do not treat them as semantic purpose coverage.

## Beta Release QA

Before publishing or promoting a beta build, complete the automated checks and the manual in-game checklist in [Beta Release Checklist](docs/BETA_RELEASE_CHECKLIST.md).

Manual QA should use a real account with a messy bank where possible, and should confirm:

- Search, fuzzy search, and visible bank highlights.
- All indexed item display and remembered storage behavior.
- Readiness owned/missing groups for common packs.
- Coverage grouping for uncovered and covered items.
- Clear behavior resetting panel results and highlights.

If manual QA has not been completed, say that explicitly in the PR or release notes. Do not present the plugin as 1.0 until real-bank QA, screenshots or GIFs, and beta feedback triage are complete.

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

## Before Opening A PR

Run:

```powershell
.\gradlew.bat test
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
```

If the safety scan returns matches from documentation or unchanged files, call that out in the PR and explain why they are not new runtime behavior.
