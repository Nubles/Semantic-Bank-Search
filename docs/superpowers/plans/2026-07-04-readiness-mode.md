# Readiness Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a local-only Readiness mode that turns a trip/task query into owned, missing, and substitute item groups.

**Architecture:** Add static readiness packs with slots such as required, recommended, optional, and upgrade. A readiness analyzer matches a player query to a pack, runs existing semantic search for each slot, and returns grouped results for the panel and overlay.

**Tech Stack:** Java, Swing, RuneLite plugin APIs, JUnit 4, existing semantic rule/search/storage classes.

---

### Task 1: Readiness Analyzer

**Files:**
- Create: `src/main/java/com/semanticbanksearch/ReadinessPack.java`
- Create: `src/main/java/com/semanticbanksearch/ReadinessSlot.java`
- Create: `src/main/java/com/semanticbanksearch/ReadinessSlotKind.java`
- Create: `src/main/java/com/semanticbanksearch/ReadinessSlotResult.java`
- Create: `src/main/java/com/semanticbanksearch/ReadinessResult.java`
- Create: `src/main/java/com/semanticbanksearch/ReadinessPackLibrary.java`
- Create: `src/main/java/com/semanticbanksearch/ReadinessAnalyzer.java`
- Test: `src/test/java/com/semanticbanksearch/ReadinessAnalyzerTest.java`

- [ ] **Step 1: Write failing tests**

Add tests showing `barrows trip` returns owned prayer, food, teleport, and missing required spade; and `wildy escape` returns owned escape teleport and missing combo food.

- [ ] **Step 2: Run focused analyzer test**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest`
Expected: FAIL because readiness classes do not exist.

- [ ] **Step 3: Implement readiness model and analyzer**

The analyzer should use `FuzzyMatcher.phraseMatches` for pack aliases and `SemanticSearchEngine.search` for each slot query. Slot results should keep only the best five owned matches.

- [ ] **Step 4: Run focused analyzer test**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest`
Expected: PASS.

### Task 2: Plugin and Panel Wiring

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticBankSearchPanelTest.java`

- [ ] **Step 1: Write failing UI/plugin tests**

Add tests showing the panel has a `Readiness` button and renders sections for owned and missing readiness slots. Add a plugin test showing readiness mode clears previous search highlights and then highlights owned readiness matches.

- [ ] **Step 2: Run focused panel/plugin tests**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.SemanticBankSearchPanelTest --tests com.semanticbanksearch.SemanticBankSearchPluginTest`
Expected: FAIL because Readiness mode is not wired yet.

- [ ] **Step 3: Implement panel rendering and plugin mode**

Add `READINESS` panel mode, a `Readiness` button, `runReadiness`, `refreshReadiness`, and `updateReadiness`. Keep coverage passive and keep readiness highlight-only with no menu actions.

- [ ] **Step 4: Run focused panel/plugin tests**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.SemanticBankSearchPanelTest --tests com.semanticbanksearch.SemanticBankSearchPluginTest`
Expected: PASS.

### Task 3: Docs and Verification

**Files:**
- Modify: `README.md`
- Modify: `docs/SEMANTIC_COVERAGE.md`

- [ ] **Step 1: Update docs**

Document Readiness mode, starter packs, local-only behavior, and the fact that missing items are inferred only from observed storage.

- [ ] **Step 2: Run full verification**

Run: `.\gradlew.bat test`
Expected: PASS.

- [ ] **Step 3: Commit and push**

Commit message: `Add local readiness mode`
