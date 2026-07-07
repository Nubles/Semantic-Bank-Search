# Beta Release Checklist

Use this checklist before publishing or promoting a Semantic Bank Search beta build.

## Automated Verification

Run these from the plugin workspace:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticQaScorecardTest
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

Run the safety and whitespace checks:

```powershell
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
```

The safety scan should not find new runtime networking, menu/action, process, reflection, or classloader behavior.

## Manual In-Game QA

Use a real account with a messy bank if possible.

1. Open the bank and confirm the plugin indexes visible bank items.
2. Use **Search** for `prayer restoration`, `warm clothing`, `teleport near barrows`, and `barows teleprt`.
3. Confirm visible matching bank items highlight when highlights are enabled.
4. Disable highlights in plugin settings and confirm Search still lists results without bank highlights.
5. Use **All** and confirm observed items show source, quantity, and visible/remembered state.
6. Open safe remembered storage such as Seed Vault or Group Storage, then return to the panel and confirm remembered items appear.
7. Use **Readiness** for `barrows trip`, `vorkath trip`, `herb run`, `wildy escape`, and `clue step`.
8. Confirm Readiness groups owned and missing slots clearly.
9. Confirm missing Readiness slots are understood as "not observed," not account truth.
10. Use **Coverage** and confirm uncovered items appear before covered items.
11. Use **Clear** and confirm panel results and bank highlights reset.
12. Close and reopen the bank, then confirm visible item status refreshes without losing remembered safe storage.

## Beta Acceptance Bar

A beta build is acceptable when:

- Full tests pass.
- Safety scan has no new runtime safety hits.
- README labels the plugin as beta and explains known limitations.
- Readiness has enough starter packs for common early feedback.
- Manual QA has been completed or explicitly marked as still pending.

## Not A 1.0 Until

Do not present this as 1.0 until:

- Real-bank manual QA has been completed.
- Screenshots or GIFs exist for Search, Readiness, Coverage, and All views.
- Coverage gaps from beta users have been triaged.
- Readiness packs have expected positives and negatives for the most requested activities.
