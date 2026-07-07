# Item Awareness Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the first phase of complete OSRS item awareness by distinguishing semantic coverage, mechanical tags, unclassified known items, and unknown observed items.

**Architecture:** Keep semantic rules curated. Add a local `ItemAwarenessClassifier` that derives conservative mechanical tags from observed item names, then extend coverage results and panel rendering to display awareness status. This prepares for a future generated all-item catalog without adding runtime network calls.

**Tech Stack:** Java, Swing, JUnit 4, existing coverage analyzer and panel classes.

---

### Task 1: Awareness Model And Analyzer Tests

**Files:**
- Create: `src/main/java/com/semanticbanksearch/ItemAwarenessStatus.java`
- Create: `src/main/java/com/semanticbanksearch/ItemAwarenessClassifier.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticCoverageResult.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticCoverageAnalyzer.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticCoverageAnalyzerTest.java`

- [ ] Add failing tests showing `Barrows teleport` is semantic covered, `Uncut sapphire` is mechanically tagged as gem, `Coins` is known unclassified, and blank/invalid names are unknown observed.
- [ ] Run `.gradlew.bat test --tests com.semanticbanksearch.SemanticCoverageAnalyzerTest` and confirm it fails.
- [ ] Implement the minimal awareness model and classifier.
- [ ] Run the focused test again and confirm it passes.

### Task 2: Coverage Panel Display

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticBankSearchPanelTest.java`

- [ ] Add a failing test showing Coverage displays mechanical tags for known mechanically tagged items and explicit unknown status for unknown observed items.
- [ ] Update coverage cards and section logic to use awareness status.
- [ ] Run focused panel tests and confirm they pass.

### Task 3: Docs And Verification

**Files:**
- Modify: `docs/SEMANTIC_COVERAGE.md`
- Modify: `README.md`
- Modify: `CONTRIBUTING.md`

- [ ] Document that this is phase one of complete item awareness, not a generated full catalog yet.
- [ ] Explain the four awareness states.
- [ ] Run `.gradlew.bat test`, safety scan, and `git diff --check`.
- [ ] Commit as `Add catalog-aware coverage states` and push.
