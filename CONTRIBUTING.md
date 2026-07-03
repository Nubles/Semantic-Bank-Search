# Contributing

Thanks for helping improve Semantic Bank Search. The plugin is intentionally local-only, read-only, and advisory, so contributions should preserve that safety model.

## Development

Use the Gradle wrapper from the repository root:

```powershell
.\gradlew.bat test
```

For semantic coverage changes, also run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

## Adding Semantic Coverage

Read [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) before adding or changing rules.

The short version:

1. Start with a real player query.
2. Add expected positive and negative examples to the curated query pack.
3. Prefer phrase aliases over broad single-word aliases.
4. Keep item patterns specific enough to avoid false positives.
5. Make the smallest rule change needed to pass the tests.

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
