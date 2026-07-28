# Runtime Foundation Residual Hardening Plan

> **For Codex:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** Close the two residual whole-branch review findings without expanding the runtime-foundation milestone.

**Architecture:** Treat retention configuration changes like every other UI-originated command: capture the active account epoch, queue work through `ThreadBridge.submitClient`, and discard it if an account transition occurs first. Make the CI whitespace range resolver always produce a real base distinct from `HEAD`, using the default-branch merge base for new feature branches, `HEAD^` for zero-SHA default-branch pushes, and the empty tree for a true root commit.

**Tech Stack:** Java 11, RuneLite event bus, Swing/client thread bridge, JUnit 4, GitHub Actions Bash

---

## Task 1: Route Runtime Retention Changes To The Client Thread

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java`

**Step 1: Write failing thread-ownership tests**

Add:

```java
@Test public void retentionConfigChangeQueuesClientCommandBeforeMutatingIndex()
@Test public void queuedRetentionConfigChangeCannotMutateNextAccount()
```

The first test must call the subscribed `onConfigChanged` method with a matching
group/key while using a recording `ThreadBridge`. Assert before the queued client
task runs:

- the active index size is unchanged;
- no persistence occurred;
- no Swing snapshot was queued.

Run the client task and assert the new clamped retention limit is applied,
trimmed state is persisted, and the active view refresh is queued.

The second test queues the config change under account A, performs a real
transition to account B, then executes the old client task. Assert account B's
index, persistence count, query, highlights, and queued Swing task count are
unchanged.

**Step 2: Confirm RED**

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticBankSearchPluginTest
```

Expected: the new test proves the current subscriber mutates account state
synchronously before the recorded client task runs.

**Step 3: Implement the minimal route**

The subscribed method may inspect only event group/key. For the matching setting,
capture the current `accountSessionEpoch` and call:

```java
threadBridge.submitClient(() ->
{
    if (capturedEpoch != accountSessionEpoch.get())
    {
        return;
    }
    applyRetentionConfigChange();
});
```

Move all config reads, controller policy replacement, trimming, persistence,
panel refresh, and notice handling into the client-thread helper. If the bridge
is unavailable during startup/shutdown, ignore the event rather than touching
mutable account state from the caller's thread.

**Step 4: Verify**

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticBankSearchPluginTest --tests com.semanticbanksearch.AccountSessionControllerTest --tests com.semanticbanksearch.ThreadBridgeTest
```

Expected: all focused tests pass.

**Step 5: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java
git commit -m "Route retention config changes to client thread"
```

## Task 2: Make Zero-SHA CI Whitespace Checks Non-Empty

**Files:**
- Modify: `.github/workflows/ci.yml`

**Step 1: Reproduce the bad range**

Run the workflow's zero-SHA range-selection block locally with the default branch
pointing at `HEAD`. Confirm the selected base equals `HEAD`, so the old command
would inspect an empty range.

Record this as the RED evidence in the task report.

**Step 2: Correct range selection**

Keep `fetch-depth: 0`.

For pull requests, use the event base SHA to `HEAD`.

For ordinary pushes with a non-zero existing `before` SHA, use
`before..HEAD`.

For zero/missing-before pushes:

1. calculate the default-branch merge base;
2. use it only when it is non-empty and differs from `HEAD`;
3. otherwise use `HEAD^` when a parent exists;
4. otherwise use `git hash-object -t tree /dev/null` and compare that empty
   tree directly to `HEAD`.

Never pass an empty-tree object to three-dot merge-base syntax. Use a direct
two-endpoint diff for the root-commit case.

Retain the ConfigManager privacy step and its allowance for
`unsetConfiguration`.

**Step 3: Verify all workflow branches**

Run the Bash block locally for:

- pull request base SHA;
- normal push `before` SHA;
- zero-SHA feature branch with a distinct default-branch merge base;
- zero-SHA default branch where merge base equals `HEAD`;
- synthetic root commit with no parent.

Assert every selected range includes at least one changed commit/tree. Also run:

```powershell
.\gradlew.bat clean test
git diff --check HEAD^ HEAD
git ls-files --stage gradlew
git ls-files .superpowers
git status --short
```

Expected:

- complete test suite passes;
- no whitespace errors;
- `gradlew` mode is `100755`;
- no tracked scratch files;
- clean worktree after commit.

**Step 4: Commit**

```powershell
git add .github/workflows/ci.yml
git commit -m "Harden CI whitespace range selection"
```

## Exit Criteria

- ConfigChanged never mutates `StorageIndex`, account state, search state, or
  persistence from Swing/event-caller context.
- A queued account-A retention change cannot affect account B.
- Every CI event path checks a non-empty submitted range, including zero-SHA
  default-branch and root-commit pushes.
- Full tests and repository integrity checks pass.

