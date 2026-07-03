# Semantic Coverage Governance Design

## Goal

Add public contributor guidance for growing Semantic Bank Search's static semantic database safely.

The project now has broad local semantic coverage and a curated query pack. The next quality step is to document the rules of the road so future coverage changes remain useful, local-only, and precise.

## Problem

The plugin's value depends on trust and precision:

- Users need confidence that searches are local-only, read-only, and approximate.
- Contributors need to know that broad aliases and item patterns can create false positives.
- Future rule additions should start from real player queries with explicit expected positives and negatives.

Without public guidance, semantic coverage can grow in ways that look helpful but quietly weaken search quality.

## Approach

Create a contributor-facing document at `docs/SEMANTIC_COVERAGE.md`.

The guide should explain:

1. The semantic database is static, local, and intentionally approximate.
2. New coverage should begin with curated query-pack tests.
3. Every query-pack row should include expected positive and negative items.
4. Aliases should prefer player phrases over broad single words.
5. Item patterns should be specific enough to avoid category bleed.
6. The plugin must not add network calls, external AI, menu actions, item movement, or storage mutation.
7. Contributors should run focused semantic tests and full tests before opening or updating a PR.

Add a short README link near the semantic database description:

```markdown
See [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) for how to add new semantic coverage safely.
```

## Content Requirements

The guide should include these sections:

- `What This Database Is`
- `Safe Coverage Workflow`
- `Writing Query Pack Cases`
- `Adding Rule Aliases`
- `Adding Item Patterns`
- `Terms To Treat Carefully`
- `Safety Boundaries`
- `Verification`

The guide should explicitly call out risky broad terms:

- `travel`
- `teleport`
- `ring`
- `staff`
- `axe`
- `bar`
- `tool`
- `utility`
- `food`
- `shield`

The guide should include one compact example showing a query with positives and negatives, and one compact example of a phrase alias.

## Out of Scope

- Removing or rewriting existing internal planning docs.
- Changing runtime plugin behavior.
- Adding new semantic categories.
- Changing the query-pack helper structure.
- Claiming complete OSRS item coverage.

## Testing

This is a documentation-only change. Verification should include:

```powershell
git diff --check
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git status --short --branch
```

If test execution is available, run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
```

## Success Criteria

- Public docs explain how to add semantic coverage safely.
- README points contributors to the guide.
- Local-only/read-only safety posture remains prominent.
- No production Java behavior changes are made.
