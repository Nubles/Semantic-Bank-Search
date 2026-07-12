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

The full suite must include regression coverage for:

- Native `bankSearchFilter` integration and Bank Tags pass-through.
- Metadata-backed equipment and consumable searches.
- Compound, numerical, and owned-relative ranking searches.
- Null metadata, unavailable stat data, ties, and per-query metadata caching.

```powershell
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
```

The safety scan should not find new runtime networking, menu/action, process, reflection, or classloader behavior.

## Manual In-Game QA

Use a real account with a messy bank if possible.

1. Open the bank and confirm the plugin indexes visible bank items.
2. Type `prayer restoration`, `warm clothing`, and `barows teleprt` directly into RuneLite's native bank search.
3. Confirm ordinary item-name searches such as `shark` retain RuneLite behavior.
4. Confirm `sem fastest food` works without requiring a colon.
5. Test `weapon`, `weps`, `armor`, `gear`, `food`, `pots`, `full pots`, and `low dose pots`.
6. Test `4 dose prayer pots`, `melee gear with prayer`, and `ranged weapons for dragons`.
7. Test `food over 18 hp`, `best food`, `good food`, `best prayer gear`, and strongest weapon searches.
8. Confirm ranking explanations show the winning value in the sidebar.
9. Enable Bank Tags and confirm `tag:` searches and layout placeholders remain unaffected.
10. Confirm visible matching bank items highlight when highlights are enabled.
11. Disable highlights and confirm searches still list results without highlights.
12. Rapidly change and clear searches; confirm there are no stale results or hidden-bank dead ends.
13. Test noted items, potion doses, charged variants, and duplicate item variations.
14. Use **All** and confirm observed items show source, quantity, and visible/remembered state.
15. Open Seed Vault or Group Storage, then confirm remembered items appear.
16. Use **Readiness** for `barrows trip`, `vorkath trip`, `herb run`, `wildy escape`, and `clue step`.
17. Confirm Readiness groups owned and missing slots clearly.
18. Confirm missing Readiness slots mean "not observed," not account truth.
19. Use **Coverage** and confirm uncovered items appear before covered items.
20. Use **Clear** and confirm panel results and bank highlights reset.
21. Close and reopen the bank; confirm visible state refreshes without losing remembered storage.
22. Restart RuneLite and confirm saved observations load without errors.

Record the RuneLite version, operating system, bank size, enabled compatibility plugins, and outcome.

## Beta Acceptance Bar

A beta build is acceptable when:

- Full tests pass.
- Safety scan has no new runtime safety hits.
- README labels the plugin as beta and explains known limitations.
- Readiness has enough starter packs for common early feedback.
- Manual QA has been completed or explicitly marked as still pending.

- Native bank search and Bank Tags compatibility have been verified.
- A near-full bank remains responsive during rapid searches.
- The Plugin Hub packager builds the exact candidate commit.
## Not A 1.0 Until

Do not present this as 1.0 until:

- Real-bank manual QA has been completed.
- Screenshots or GIFs exist for Search, Readiness, Coverage, and All views.
- Coverage gaps from beta users have been triaged.
- Readiness packs have expected positives and negatives for the most requested activities.
