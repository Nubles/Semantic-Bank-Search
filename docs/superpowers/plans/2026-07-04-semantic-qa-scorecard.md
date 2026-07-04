# Semantic QA Scorecard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a developer-only Semantic QA scorecard that measures curated semantic search quality across expected positives and negatives.

**Architecture:** Add test-scope case and scorecard helpers that evaluate curated query cases against `SemanticSearchEngine` and produce aggregate counts plus a readable report. Refactor the existing curated query-pack test to use the shared case list so future semantic cases feed both regression tests and the scorecard.

**Tech Stack:** Java, JUnit 4, existing `SemanticSearchEngine`, `StorageIndex`, and static rule library.

---

## File Structure

- Create `src/test/java/com/semanticbanksearch/SemanticQueryCase.java`: immutable test-scope model for one query scenario.
- Create `src/test/java/com/semanticbanksearch/SemanticQueryCases.java`: shared curated query case list.
- Create `src/test/java/com/semanticbanksearch/SemanticQaScorecard.java`: evaluator and report formatter.
- Create `src/test/java/com/semanticbanksearch/SemanticQaScorecardTest.java`: focused scorecard tests.
- Modify `src/test/java/com/semanticbanksearch/CuratedQueryPackTest.java`: consume shared cases instead of embedding scenario data directly.
- Create `docs/SEMANTIC_QA_SCORECARD.md`: contributor-facing scorecard snapshot and workflow.

## Tasks

### Task 1: Scorecard Test

- [ ] Write a failing test that references `SemanticQaScorecard`, `SemanticQueryCase`, and aggregate counters.
- [ ] Run `./gradlew.bat test --tests com.semanticbanksearch.SemanticQaScorecardTest` and confirm it fails because the scorecard does not exist.

### Task 2: Scorecard Helpers

- [ ] Implement `SemanticQueryCase` with scenario, query, positives, and negatives.
- [ ] Implement `SemanticQaScorecard` with total case count, pass count, positive count, negative count, missing positives, false positives, and Markdown report output.
- [ ] Run the focused scorecard test and confirm it passes.

### Task 3: Shared Curated Cases

- [ ] Move current curated query-pack cases into `SemanticQueryCases`.
- [ ] Refactor `CuratedQueryPackTest` to iterate through the shared cases.
- [ ] Run `./gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest`.

### Task 4: Docs And Verification

- [ ] Add `docs/SEMANTIC_QA_SCORECARD.md` with the current scorecard purpose and current passing totals.
- [ ] Run scorecard, curated pack, semantic engine, and full test suite.
- [ ] Run safety scan, `git diff --check`, and `git status --short --branch`.
- [ ] Commit and push the scorecard update.