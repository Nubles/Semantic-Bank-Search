# Beta Release Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Semantic Bank Search suitable for a beta release by expanding readiness coverage and tightening public release expectations.

**Architecture:** Keep the runtime local-only and passive. Expand `ReadinessPackLibrary` using existing semantic search queries, add analyzer tests that prove common beta packs resolve owned items, and update release docs to clearly label beta limitations and manual QA steps.

**Tech Stack:** Java, Swing, RuneLite plugin APIs, JUnit 4, Markdown docs.

---

### Task 1: Readiness Beta Pack Tests

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/ReadinessAnalyzerTest.java`

- [ ] Add tests for beta readiness packs: Vorkath, Zulrah, Fight Caves, birdhouse run, farm contract, Slayer task, Wildy boss, Dagannoth Kings, quest tools, and wintertodt.
- [ ] Run `.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest` and confirm it fails because the packs are not present yet.

### Task 2: Readiness Beta Pack Data

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/ReadinessPackLibrary.java`

- [ ] Add compact readiness packs using existing semantic queries instead of new broad item patterns.
- [ ] Avoid slots that can claim a task is covered from an unrelated item.
- [ ] Run `.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest` and confirm it passes.

### Task 3: Beta Release Messaging

**Files:**
- Modify: `README.md`
- Modify: `runelite-plugin.properties`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Create: `docs/BETA_RELEASE_CHECKLIST.md`

- [ ] Add beta wording to the plugin description and README.
- [ ] Add Known Limitations near the top of the README.
- [ ] Add supported readiness pack list.
- [ ] Add manual in-game QA checklist covering search, all indexed items, readiness, coverage, highlights, remembered storage, and clear behavior.

### Task 4: Verification and Release Prep

**Files:**
- No new source files expected.

- [ ] Run `.\gradlew.bat test`.
- [ ] Run safety scan for networking/menu action/withdrawal/process/reflection/classloader behavior.
- [ ] Run `git diff --check`.
- [ ] Commit as `Prepare beta release readiness`.
- [ ] Push the branch and update PR #2 with beta readiness notes.
