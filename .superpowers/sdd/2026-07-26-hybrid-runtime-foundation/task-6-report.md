# Task 6 Report: Client-Thread Commands And Revisioned Swing Snapshots

## Status

PASS. Task 6 is implemented and verified. Visible panel modes continue to render through the existing helpers, panel snapshots are applied only on Swing's event dispatch thread, revisions older than the last rendered revision are ignored, the clear button emits only its command, and overlay highlights are published as immutable snapshots.

Task 7 plugin-wide callback routing was intentionally not implemented.

## Files Changed

- `src/main/java/com/semanticbanksearch/ThreadBridge.java` (created): separates client-thread command submission from Swing render submission and ignores null work.
- `src/main/java/com/semanticbanksearch/PanelViewSnapshot.java` (created): provides revisioned snapshot factories for clear, search, all-indexed, readiness, and coverage views; lists are defensively copied and unmodifiable, including element copies for mutable `ObservedItem` values.
- `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`: adds the EDT-only `applySnapshot` API, lower-revision rejection, package-private test accessors, private render helpers, compatibility adapters for the pre-Task-7 plugin, and command-only clear-button behavior.
- `src/main/java/com/semanticbanksearch/SemanticBankSearchOverlay.java`: publishes an unmodifiable highlight set through a `volatile` field.
- `src/test/java/com/semanticbanksearch/ThreadBridgeTest.java` (created): covers executor isolation, deferred execution, and null submission.
- `src/test/java/com/semanticbanksearch/PanelViewSnapshotTest.java` (created): covers defensive list/element copies and unmodifiable getters.
- `src/test/java/com/semanticbanksearch/SemanticBankSearchPanelTest.java`: routes rendering assertions through snapshots on EDT and covers stale revisions, off-EDT rejection, and clear-button authority.
- `.superpowers/sdd/2026-07-26-hybrid-runtime-foundation/task-6-report.md` (created): this report.

## Commits

- `4f3bbdb9f3d5e2c7dba7d52a69244c10a4a0111c` - `Enforce client and Swing thread ownership`
- Report documentation - the commit containing this file.

## TDD Evidence

### Baseline

Command:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticBankSearchPanelTest
```

Result before Task 6 tests: `BUILD SUCCESSFUL`.

### RED

Tests were added before any production edits. Command:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.ThreadBridgeTest --tests com.semanticbanksearch.PanelViewSnapshotTest --tests com.semanticbanksearch.SemanticBankSearchPanelTest
```

Result: expected `compileTestJava` failure with 36 missing-symbol errors for `ThreadBridge`, `PanelViewSnapshot`, `applySnapshot`, and the three panel test accessors. The failure was caused by the absent Task 6 API, not a test syntax or fixture error.

### GREEN

The same focused command passed after the minimal implementation and again after green-only cleanup:

```text
ThreadBridgeTest: 3 tests, 0 failures, 0 errors
PanelViewSnapshotTest: 2 tests, 0 failures, 0 errors
SemanticBankSearchPanelTest: 7 tests, 0 failures, 0 errors
Focused total: 12 tests, 0 failures, 0 errors
BUILD SUCCESSFUL
```

Full regression command:

```powershell
.\gradlew.bat test
```

Result: `216 tests, 0 failures, 0 errors` across 29 suites; `BUILD SUCCESSFUL`.

## Thread And Stale-Update Coverage

- `clientCommandsUseOnlyClientExecutor` proves client commands are queued only to the client executor and remain deferred until the recorded runnable is executed.
- `rendersUseOnlySwingExecutor` proves renders are queued only to the Swing executor and remain deferred until explicitly executed.
- `nullCommandsAreIgnored` proves neither executor receives null work.
- `panelAppliesSnapshotOnSwingEventThread` proves direct off-EDT application throws and successful state assertions run through `SwingUtilities.invokeAndWait`.
- Existing visible-mode tests now capture Swing component state through `SwingUtilities.invokeAndWait`.
- `panelIgnoresOlderRevision` applies revision 2 followed by revision 1 and proves revision, result count, and status remain from revision 2.
- `clearButtonEmitsCommandWithoutRenderingLocally` proves the command fires while the rendered revision, result count, and status remain authoritative until a response snapshot arrives.
- Overlay publication uses a complete unmodifiable copy assigned to a `volatile` field, so render observes either the previous complete set or the next complete set, never a set being mutated in place.

## Self-Review

- Checked every Task 6 requirement against the diff; no requirement gaps found.
- Confirmed the implementation touches only the seven specified production/test files plus this required report.
- Confirmed `ThreadBridge.runtime(clientThread)` is exactly backed by `clientThread::invoke` and `SwingUtilities::invokeLater`.
- Confirmed list snapshots cannot be changed through getters or source-list mutation; mutable observed items are copied by value.
- Confirmed `applySnapshot` rejects only lower revisions, so equal-revision replacement remains permitted as specified.
- Confirmed all actual clear/search/index/readiness/coverage render helpers are private.
- Confirmed the clear listener no longer calls `clearResults()` directly.
- Confirmed no Task 7 account integration, callback routing, or plugin state-lock work was pulled forward.
- Retained package-private compatibility adapters because the current plugin still calls the pre-Task-7 panel methods; Task 7 explicitly replaces those calls with authoritative snapshots.
- `git diff --check` reported no whitespace errors; only expected Windows line-ending normalization notices before staging.

## Concerns

No blocking concerns. The package-private compatibility adapters are deliberate temporary scaffolding required to preserve compilation and current behavior until Task 7 completes plugin-wide `ThreadBridge` and snapshot routing.
