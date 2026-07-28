# Trustworthy Runtime Foundation Implementation Plan

> **For Codex:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace shared synced bank storage with account-isolated local files, establish single-threaded runtime ownership and immutable Swing updates, remove the 800-entry ceiling, and restore green Linux CI.

**Architecture:** `AccountSessionController` owns the active account and mutable `StorageIndex` on RuneLite's client thread. `AccountStorageRepository` persists a name-free JSON document beneath `RuneLite.RUNELITE_DIR`, using a domain-separated account key, atomic replacement, and corrupt-file quarantine. Panel actions cross onto the client thread through `ThreadBridge`; computed results cross back to Swing as revisioned immutable `PanelViewSnapshot` values.

**Tech Stack:** Java 11, RuneLite client/API, Gson 2.10.1, Swing, JUnit 4, Gradle/GitHub Actions

**Scope:** This is milestone 1 of the approved design in `docs/superpowers/specs/2026-07-26-hybrid-deterministic-bank-search-design.md`. The unified semantic core, generated item catalogue, and expanded UX/quality corpus receive separate implementation plans after this foundation is merged and verified.

---

## Runtime Contracts

Use these contracts consistently throughout this milestone.

```java
final class AccountKey
{
    static Optional<AccountKey> fromAccountHash(long accountHash);
    String value();
}
```

- `0L` produces `Optional.empty()`.
- Other values hash the UTF-8 bytes of
  `semantic-bank-search/account/v1:<signed-decimal-account-hash>` with SHA-256.
- `value()` is the full 64-character lowercase hexadecimal digest.
- Raw account hashes and display names never appear in paths or persisted JSON.

```java
final class AccountStorageRepository
{
    static final int SCHEMA_VERSION = 1;
    static final String CURRENT_CATALOGUE_VERSION = "legacy-rules-v1";

    AccountStorageRepository(Path runeLiteDirectory, Gson gson, Clock clock);

    AccountStorageLoadResult load(
        AccountKey accountKey,
        IntFunction<String> itemNameResolver) throws IOException;

    void save(AccountKey accountKey, StorageIndex index) throws IOException;
    void clear(AccountKey accountKey) throws IOException;
}
```

The persisted shape is:

```json
{
  "schemaVersion": 1,
  "catalogueVersion": "legacy-rules-v1",
  "items": [
    {
      "itemId": 2434,
      "quantity": 4,
      "sourceType": "BANK",
      "sourceName": "Bank",
      "currentlyVisible": false,
      "lastSeenMillis": 1720000000000
    }
  ]
}
```

`name`, account hash, account display name, query history, and item metadata are forbidden fields.

```java
final class AccountSessionController
{
    AccountSessionController(
        AccountStorageRepository repository,
        IntFunction<String> itemNameResolver,
        StorageRetentionPolicy retentionPolicy);

    AccountSessionUpdate switchTo(long accountHash);
    AccountSessionUpdate deactivate();
    Optional<String> persist();
    Optional<String> clearActiveAccount();
    StorageIndex activeIndexOrNull();
    AccountKey activeAccountKeyOrNull();
}
```

- `switchTo` persists and unloads the previous account before loading another.
- Repeating the same non-zero hash is a no-op.
- Switching to zero behaves as `deactivate`.
- Load failure starts an empty index for the requested account and returns a
  non-blocking notice; it never retains the previous account's index.
- `deactivate` persists, marks the controller inactive, and drops all account
  references even if persistence fails.

```java
final class StorageRetentionPolicy
{
    static final int DEFAULT_MAXIMUM_ENTRIES = 5_000;
    static final int HARD_MAXIMUM_ENTRIES = 20_000;

    StorageRetentionPolicy(int configuredMaximumEntries);
    void apply(StorageIndex index);
}
```

- Clamp the configured account and source limit to `100..20_000`.
- First trim each source to the configured limit, then trim the account to that
  limit.
- Evict oldest non-visible entries first.
- Never evict a currently visible bank entry.
- If only visible bank entries remain, retain them rather than violating the
  visibility guarantee.

```java
final class ThreadBridge
{
    ThreadBridge(Consumer<Runnable> clientExecutor, Consumer<Runnable> swingExecutor);
    static ThreadBridge runtime(ClientThread clientThread);
    void submitClient(Runnable command);
    void submitSwing(Runnable render);
}
```

```java
final class PanelViewSnapshot
{
    enum Kind { CLEAR, SEARCH, ALL_INDEXED, READINESS, COVERAGE_AUDIT }

    long getRevision();
    Kind getKind();
    String getQuery();
    String getStatus();
    List<SemanticSearchResult> getSearchResults();
    List<ObservedItem> getIndexedItems();
    ReadinessResult getReadinessResult();
    List<SemanticCoverageResult> getCoverageResults();
}
```

Static factories create each snapshot kind. Constructor inputs are defensively
copied, list getters are unmodifiable, and the panel ignores any snapshot older
than the last revision it rendered.

## Task 1: Repair Linux CI And Record A Baseline

**Files:**
- Modify file mode: `gradlew`
- Verify: `.github/workflows/ci.yml`

**Step 1: Confirm the failing mode**

Run:

```powershell
git ls-files --stage gradlew
```

Expected before the change: mode `100644`.

**Step 2: Record the executable mode**

Run:

```powershell
git update-index --chmod=+x gradlew
git ls-files --stage gradlew
```

Expected after the change: mode `100755`.

Do not add a workflow workaround. Keep the canonical Linux command
`./gradlew clean test`.

**Step 3: Run the current suite**

Run:

```powershell
.\gradlew.bat clean test
```

Expected: the existing suite passes before runtime behavior changes.

**Step 4: Commit**

```powershell
git add gradlew
git commit -m "Fix Gradle wrapper permissions"
```

## Task 2: Add Domain-Separated Account Keys

**Files:**
- Create: `src/main/java/com/semanticbanksearch/AccountKey.java`
- Test: `src/test/java/com/semanticbanksearch/AccountKeyTest.java`

**Step 1: Write failing key tests**

Cover:

```java
@Test public void zeroHashHasNoAccountKey()
@Test public void sameHashProducesStableLowercaseSha256Key()
@Test public void signedHashValuesProduceDifferentKeys()
@Test public void keyDoesNotContainRawHash()
@Test public void domainSeparationChangesPlainSha256Result()
```

For the stable-value assertion, calculate the expected digest in the test from
the literal domain-separated input using `MessageDigest`; also assert length 64
and `[0-9a-f]+`. Do not duplicate the production method.

**Step 2: Confirm failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountKeyTest
```

Expected: compilation fails because `AccountKey` does not exist.

**Step 3: Implement the minimal value object**

Use `MessageDigest.getInstance("SHA-256")`, `StandardCharsets.UTF_8`, and an
explicit hexadecimal loop. Convert `NoSuchAlgorithmException` to
`IllegalStateException`, since SHA-256 is required by Java.

Implement `equals`, `hashCode`, and `toString`; `toString` may return the digest
because it is already the non-sensitive path key.

**Step 4: Verify**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountKeyTest
```

Expected: all account-key tests pass.

**Step 5: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/AccountKey.java src/test/java/com/semanticbanksearch/AccountKeyTest.java
git commit -m "Add account-isolated storage keys"
```

## Task 3: Build The Name-Free Local Repository

**Files:**
- Create: `src/main/java/com/semanticbanksearch/AccountStorageDocument.java`
- Create: `src/main/java/com/semanticbanksearch/StoredObservedItem.java`
- Create: `src/main/java/com/semanticbanksearch/AccountStorageLoadResult.java`
- Create: `src/main/java/com/semanticbanksearch/AccountStorageRepository.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchStorage.java`
- Test: `src/test/java/com/semanticbanksearch/AccountStorageRepositoryTest.java`
- Modify test: `src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java`

**Step 1: Write failing repository tests**

Use JUnit `TemporaryFolder`, a fixed `Clock`, and `new Gson()`.

Cover:

```java
@Test public void savesUnderAccountSpecificRuneLiteDirectory()
@Test public void persistedJsonOmitsNamesAndAccountIdentity()
@Test public void roundTripRehydratesNamesFromResolver()
@Test public void accountsNeverReadEachOthersIndexes()
@Test public void saveAtomicallyReplacesExistingDocument()
@Test public void malformedJsonIsQuarantinedAndReturnsEmptyIndex()
@Test public void unsupportedSchemaIsQuarantinedAndReturnsEmptyIndex()
@Test public void invalidEntriesAreDroppedDuringLoad()
@Test public void clearRemovesOnlyTheSelectedAccountFile()
```

Assert the exact path:

```text
<temp>/semantic-bank-search/accounts/<account-key>/index.json
```

For the privacy assertion, inspect both the raw JSON and parsed JSON:

```java
assertFalse(json.contains("Prayer potion"));
assertFalse(json.contains(Long.toString(accountHash)));
assertFalse(root.has("accountHash"));
assertFalse(root.has("displayName"));
assertFalse(root.getAsJsonArray("items").get(0).getAsJsonObject().has("name"));
```

For quarantine, assert the original `index.json` is gone, exactly one
`index.corrupt-<fixed-clock-millis>.json` exists beside it, and the returned
result reports the quarantine path.

**Step 2: Confirm failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountStorageRepositoryTest
```

Expected: compilation fails because the repository types do not exist.

**Step 3: Implement persistence DTOs**

`AccountStorageDocument` and `StoredObservedItem` are package-private Gson DTOs.
Give them no-argument constructors and validated conversion methods. Keep runtime
`ObservedItem` names out of the DTO.

Validation rules:

- schema version must equal `1`;
- item ID must be positive;
- quantity and timestamps clamp to zero;
- null source type normalizes to `OTHER_STORAGE`;
- null source name normalizes to an empty string;
- resolver failures or blank names become `"Item <id>"`, preserving the observed
  item until the catalogue milestone can classify it.

Keep `SemanticBankSearchStorage` only as the JSON sanitizer/adapter used by the
repository. Replace its string-only public surface with package-private methods:

```java
static AccountStorageDocument toDocument(StorageIndex index);
static StorageIndex toIndex(
    AccountStorageDocument document,
    IntFunction<String> itemNameResolver);
```

Delete the old `serialize`/`deserialize` contract and rewrite its tests around
the name-free document conversion.

**Step 4: Implement atomic storage and quarantine**

Build paths only from `AccountKey.value()`. Save with:

1. `Files.createDirectories(accountDirectory)`;
2. write UTF-8 JSON to `index.json.tmp`;
3. move with `ATOMIC_MOVE` and `REPLACE_EXISTING`;
4. on `AtomicMoveNotSupportedException`, retry with `REPLACE_EXISTING`;
5. in a `finally` block, remove a leftover temp file.

On malformed JSON, wrong schema, or structurally invalid root data, move the
original document to `index.corrupt-<clock-millis>.json` and return an empty
`StorageIndex`. If that quarantine filename exists, append `-1`, `-2`, and so on
without overwriting evidence.

Do not catch ordinary read/write `IOException` inside the repository; the session
controller needs to distinguish an unavailable filesystem from corrupt content.

**Step 5: Verify**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountStorageRepositoryTest --tests com.semanticbanksearch.SemanticBankSearchStorageTest
```

Expected: all repository and conversion tests pass.

**Step 6: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/AccountStorageDocument.java src/main/java/com/semanticbanksearch/StoredObservedItem.java src/main/java/com/semanticbanksearch/AccountStorageLoadResult.java src/main/java/com/semanticbanksearch/AccountStorageRepository.java src/main/java/com/semanticbanksearch/SemanticBankSearchStorage.java src/test/java/com/semanticbanksearch/AccountStorageRepositoryTest.java src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java
git commit -m "Store observed items in account-local files"
```

## Task 4: Replace The 800-Entry Ceiling With Retention Guarantees

**Files:**
- Create: `src/main/java/com/semanticbanksearch/StorageRetentionPolicy.java`
- Modify: `src/main/java/com/semanticbanksearch/StorageIndex.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchConfig.java`
- Modify test: `src/test/java/com/semanticbanksearch/StorageIndexTest.java`
- Create test: `src/test/java/com/semanticbanksearch/StorageRetentionPolicyTest.java`

**Step 1: Write failing retention tests**

Cover:

```java
@Test public void defaultsToFiveThousandEntries()
@Test public void clampsConfiguredLimitToHardCeiling()
@Test public void evictsOldestNonVisibleEntriesFirst()
@Test public void neverEvictsVisibleBankEntries()
@Test public void trimsEachStorageSourceBeforeAccountTotal()
@Test public void returnsSnapshotsWithoutReorderingMutableBackingState()
```

Build small policies in tests, such as a limit of two, through a package-private
constructor:

```java
StorageRetentionPolicy(int configuredMaximumEntries, int hardMaximumEntries)
```

The public production constructor always uses the hard ceiling `20_000`.

**Step 2: Confirm failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.StorageRetentionPolicyTest --tests com.semanticbanksearch.StorageIndexTest
```

Expected: new tests fail because retention behavior is absent.

**Step 3: Make index trimming deterministic**

Replace `trimToMaximumEntries` with:

```java
void trimToLimits(int maximumAccountEntries, int maximumEntriesPerSource);
```

Use a stable ordering of:

1. evictable before protected visible bank entries;
2. oldest `lastSeenMillis`;
3. item ID;
4. source type;
5. case-insensitive source name.

Do not sort the mutable backing list in `items()`. Copy first, sort the copy, then
return defensive `ObservedItem` copies. This avoids reads changing later eviction
order.

**Step 4: Update configuration**

Change `maximumRememberedEntries` to:

```java
@Range(min = 100, max = 20_000)
default int maximumRememberedEntries()
{
    return 5_000;
}
```

Update its label and description to say this is an account-local observed-entry
limit. Existing values are clamped by `StorageRetentionPolicy`.

**Step 5: Verify**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.StorageRetentionPolicyTest --tests com.semanticbanksearch.StorageIndexTest
```

Expected: all retention tests pass.

**Step 6: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/StorageRetentionPolicy.java src/main/java/com/semanticbanksearch/StorageIndex.java src/main/java/com/semanticbanksearch/SemanticBankSearchConfig.java src/test/java/com/semanticbanksearch/StorageRetentionPolicyTest.java src/test/java/com/semanticbanksearch/StorageIndexTest.java
git commit -m "Expand account storage retention safely"
```

## Task 5: Add The Account Session Lifecycle

**Files:**
- Create: `src/main/java/com/semanticbanksearch/AccountSessionUpdate.java`
- Create: `src/main/java/com/semanticbanksearch/AccountSessionController.java`
- Test: `src/test/java/com/semanticbanksearch/AccountSessionControllerTest.java`

**Step 1: Write failing lifecycle tests**

Use a real `AccountStorageRepository` against `TemporaryFolder`; avoid mocks.

Cover:

```java
@Test public void zeroHashLeavesNoActiveIndex()
@Test public void activatingAccountLoadsOnlyItsIndex()
@Test public void switchingPersistsFirstAccountBeforeLoadingSecond()
@Test public void repeatedHashDoesNotReloadOrReplaceIndex()
@Test public void deactivationPersistsThenDropsAllAccountState()
@Test public void failedSaveStillDropsPreviousAccountBeforeSwitch()
@Test public void corruptLoadReturnsAUserFacingQuarantineNotice()
@Test public void loadFailureStartsEmptyWithoutReusingPreviousIndex()
@Test public void clearActiveAccountDeletesDiskAndMemoryState()
```

To induce an I/O failure portably, construct the repository with a RuneLite path
whose `semantic-bank-search` component is an existing regular file.

Assert object identity changes when switching accounts and becomes null on
deactivation. Assert account A's item is never present in account B's active
index.

**Step 2: Confirm failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountSessionControllerTest
```

Expected: compilation fails because the session types do not exist.

**Step 3: Implement controller ownership**

`AccountSessionUpdate` is immutable and contains:

```java
boolean isChanged();
StorageIndex getActiveIndex(); // nullable only when inactive
List<String> getNotices();
```

Use these exact user-facing notices:

- corrupt file:
  `A damaged local index was quarantined; this account will rebuild as storage is observed.`
- load failure:
  `Local account storage could not be loaded; this session started with an empty index.`
- save failure:
  `Local account storage could not be saved.`
- cleared:
  `Local observed storage for this account was cleared.`

The controller logs exception details through SLF4J but exposes only the concise
notice. Apply `StorageRetentionPolicy` after load and immediately before save.

**Step 4: Verify**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountSessionControllerTest
```

Expected: all lifecycle tests pass.

**Step 5: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/AccountSessionUpdate.java src/main/java/com/semanticbanksearch/AccountSessionController.java src/test/java/com/semanticbanksearch/AccountSessionControllerTest.java
git commit -m "Own observed storage by active account"
```

## Task 6: Establish Client-Thread Commands And Revisioned Swing Snapshots

**Files:**
- Create: `src/main/java/com/semanticbanksearch/ThreadBridge.java`
- Create: `src/main/java/com/semanticbanksearch/PanelViewSnapshot.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchOverlay.java`
- Create test: `src/test/java/com/semanticbanksearch/ThreadBridgeTest.java`
- Create test: `src/test/java/com/semanticbanksearch/PanelViewSnapshotTest.java`
- Modify test: `src/test/java/com/semanticbanksearch/SemanticBankSearchPanelTest.java`

**Step 1: Write failing bridge tests**

Cover:

```java
@Test public void clientCommandsUseOnlyClientExecutor()
@Test public void rendersUseOnlySwingExecutor()
@Test public void nullCommandsAreIgnored()
```

Use recording `Consumer<Runnable>` instances. Verify submission is deferred until
the recorded runnable is explicitly executed.

**Step 2: Write failing snapshot tests**

Cover:

```java
@Test public void factoriesDefensivelyCopyLists()
@Test public void listGettersAreUnmodifiable()
@Test public void panelIgnoresOlderRevision()
@Test public void panelAppliesSnapshotOnSwingEventThread()
```

Use `SwingUtilities.invokeAndWait` for panel assertions. Add package-private panel
test accessors for last rendered revision, current result count, and current
status rather than using reflection.

**Step 3: Confirm failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.ThreadBridgeTest --tests com.semanticbanksearch.PanelViewSnapshotTest --tests com.semanticbanksearch.SemanticBankSearchPanelTest
```

Expected: compilation fails for the new types and snapshot panel API.

**Step 4: Implement the bridge and snapshots**

`ThreadBridge.runtime(clientThread)` uses:

```java
new ThreadBridge(clientThread::invoke, SwingUtilities::invokeLater)
```

`SemanticBankSearchPanel.applySnapshot` is the only plugin-facing render method.
It asserts EDT ownership, rejects revisions lower than `lastRenderedRevision`,
and delegates internally to the existing clear/search/index/readiness/coverage
render helpers. Make those helpers private.

Remove the clear button's direct `clearResults()` call. It should emit only the
clear command; the client-thread response produces the authoritative clear
snapshot.

Make the overlay highlight set an immutable copy published through a `volatile`
field so render never observes a set being mutated.

**Step 5: Verify**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.ThreadBridgeTest --tests com.semanticbanksearch.PanelViewSnapshotTest --tests com.semanticbanksearch.SemanticBankSearchPanelTest
```

Expected: all bridge, snapshot, and panel tests pass.

**Step 6: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/ThreadBridge.java src/main/java/com/semanticbanksearch/PanelViewSnapshot.java src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java src/main/java/com/semanticbanksearch/SemanticBankSearchOverlay.java src/test/java/com/semanticbanksearch/ThreadBridgeTest.java src/test/java/com/semanticbanksearch/PanelViewSnapshotTest.java src/test/java/com/semanticbanksearch/SemanticBankSearchPanelTest.java
git commit -m "Enforce client and Swing thread ownership"
```

## Task 7: Integrate Account Storage Into The Plugin

**Files:**
- Create: `src/main/java/com/semanticbanksearch/LegacyStorageCleanup.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Modify test: `src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java`
- Create test: `src/test/java/com/semanticbanksearch/LegacyStorageCleanupTest.java`

**Step 1: Write failing cleanup and lifecycle integration tests**

Cover:

```java
@Test public void legacyCleanupUnsetsOnlySharedIndexKey()
@Test public void accountChangeClearsOldHighlightsBeforeLoadingNewIndex()
@Test public void loginScreenDeactivatesAccount()
@Test public void loggedInStateActivatesCurrentAccountHash()
@Test public void panelActionsAreDeferredOntoClientThread()
@Test public void panelRendersAreDeferredOntoSwingThread()
@Test public void noActiveAccountProducesEmptyLoggedOutSnapshot()
@Test public void shutdownPersistsAndDeactivatesAccount()
```

`LegacyStorageCleanup.remove` accepts a `BiConsumer<String, String>` so its exact
group/key call is unit-testable without constructing RuneLite's
`ConfigManager`.

For plugin tests, add one package-private dependency setter that accepts
`AccountSessionController` and `ThreadBridge`; do not add separate setters for
internal fields. Keep client API access behind tiny package-private event helper
methods so tests can pass explicit account hashes and game states without a
mocking framework:

```java
void handleAccountHashChanged(long accountHash);
void handleGameStateChanged(GameState gameState, long accountHash);
```

The subscribed event methods only read `client.getAccountHash()` and delegate.

**Step 2: Confirm failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.LegacyStorageCleanupTest --tests com.semanticbanksearch.SemanticBankSearchPluginTest
```

Expected: tests fail because cleanup, account events, and thread routing are not
integrated.

**Step 3: Replace ConfigManager persistence**

At startup:

1. call `LegacyStorageCleanup.remove(configManager::unsetConfiguration)`;
2. construct `AccountStorageRepository` from `RuneLite.RUNELITE_DIR.toPath()`;
3. construct `StorageRetentionPolicy(config.maximumRememberedEntries())`;
4. construct `AccountSessionController`;
5. construct `ThreadBridge.runtime(clientThread)`;
6. initialize semantic services and UI;
7. activate `client.getAccountHash()`;
8. start observation only when an active index exists.

Inject `net.runelite.client.callback.ClientThread`.

Delete:

- `STORAGE_KEY`;
- startup `configManager.getConfiguration(...)`;
- `persistToConfig`;
- the `Consumer<StorageIndex>` production persistence path;
- all `configManager.setConfiguration(...)` calls for bank contents.

Retain `ConfigManager` only for the plugin's ordinary settings and the one-time
legacy key deletion.

**Step 4: Add account and game-state subscriptions**

Subscribe to `AccountHashChanged` and `GameStateChanged`.

- On `AccountHashChanged`, pass `client.getAccountHash()` to
  `handleAccountHashChanged`.
- On `LOGGED_IN`, activate/switch to the current hash.
- On `LOGIN_SCREEN` or `LOGIN_SCREEN_AUTHENTICATOR`, mark storage sources not
  visible, persist, deactivate, clear highlights, and publish a logged-out
  snapshot.
- Do not deactivate on `HOPPING`, `CONNECTION_LOST`, or `LOADING`.

Every switch clears `visibleStorageSourceKeys`, overlay highlights, current query,
and stale panel state before exposing the next index. Put the latest
`AccountSessionUpdate` notice into the next snapshot status.

**Step 5: Route all UI traffic**

Wrap panel callbacks with `threadBridge.submitClient`. All methods that read
`Client`, `ItemManager`, `StorageIndex`, account state, or perform semantic work
remain client-thread methods.

Replace direct panel calls in:

- `clearSearch`;
- `refreshIndexedItems`;
- `refreshCoverageAudit`;
- `refreshReadiness`;
- `refreshCurrentSearch`.

Each method creates a `PanelViewSnapshot` and submits one Swing render. Remove
`viewStateLock`; `panelMode`, `currentQuery`, and `viewRevision` are client-thread
state. Increment the revision for every user command and account transition.

**Step 6: Apply retention at observation boundaries**

After a safe-storage scan changes the index, call the session controller's
retention policy through a package-private `trimActiveIndex()` method, then
persist on the existing interval/visibility-loss rules. Persistence now delegates
to `AccountSessionController.persist()`.

**Step 7: Verify focused behavior**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.LegacyStorageCleanupTest --tests com.semanticbanksearch.AccountSessionControllerTest --tests com.semanticbanksearch.SemanticBankSearchPluginTest --tests com.semanticbanksearch.SafeStorageObservationTest
```

Expected: all account, plugin, and observation tests pass.

**Step 8: Prove bank data is absent from ConfigManager**

Run:

```powershell
rg -n "getConfiguration|setConfiguration|semanticbanksearch\\.index|STORAGE_KEY" src/main/java/com/semanticbanksearch
```

Expected: no shared-index read/write code. The only relevant match may be
`unsetConfiguration` in startup cleanup.

**Step 9: Commit**

```powershell
git add src/main/java/com/semanticbanksearch/LegacyStorageCleanup.java src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java src/test/java/com/semanticbanksearch/LegacyStorageCleanupTest.java src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java
git commit -m "Integrate account-local runtime storage"
```

## Task 8: Document Privacy, Recovery, And Beta Verification

**Files:**
- Modify: `README.md`
- Modify: `CONTRIBUTING.md`
- Modify: `docs/BETA_RELEASE_CHECKLIST.md`

**Step 1: Update user documentation**

In `README.md`, state:

- observed storage is local-only and account-isolated;
- the exact directory pattern, using `<account-key>` rather than a raw hash;
- account display names and item names are not persisted;
- deleting/clearing an index causes it to rebuild as storage is observed;
- malformed files are quarantined automatically;
- ordinary preferences may still use RuneLite configuration.

Do not claim full item coverage or unified search parity yet; those belong to
later milestones.

**Step 2: Update contributor workflow**

Add focused commands for:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.AccountKeyTest
.\gradlew.bat test --tests com.semanticbanksearch.AccountStorageRepositoryTest
.\gradlew.bat test --tests com.semanticbanksearch.AccountSessionControllerTest
.\gradlew.bat test --tests com.semanticbanksearch.PanelViewSnapshotTest
```

Add explicit review rules:

- no bank contents in `ConfigManager`;
- no raw account hash or display name on disk;
- no Swing callback may read RuneLite client state directly;
- persistence failures must never retain another account's in-memory index.

**Step 3: Expand beta checklist**

Add manual scenarios:

1. log into account A, observe bank, log out, log into account B, and verify no A
   results appear;
2. restart RuneLite and verify account A restores only after A is active;
3. inspect the local JSON and verify item/account names are absent;
4. corrupt `index.json`, restart, and verify a quarantine file plus a
   non-blocking notice;
5. switch worlds and verify the account index is retained;
6. test a bank larger than 800 unique entries;
7. run clear-account-data and verify only the active account is cleared.

Mark clear-account-data as pending UI exposure if Task 7 only exposes the
controller operation; milestone 4 will add the final settings control.

**Step 4: Commit**

```powershell
git add README.md CONTRIBUTING.md docs/BETA_RELEASE_CHECKLIST.md
git commit -m "Document account-local storage guarantees"
```

## Task 9: Full Milestone Verification

**Files:**
- Verify only; fix the smallest relevant files if a check exposes a defect.

**Step 1: Run the complete suite from a clean build**

```powershell
.\gradlew.bat clean test
```

Expected: every test passes.

**Step 2: Run policy and privacy scans**

```powershell
rg -n "getConfiguration|setConfiguration|semanticbanksearch\\.index|STORAGE_KEY" src/main/java/com/semanticbanksearch
rg -n "https?://|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|ProcessBuilder|ClassLoader|java\\.lang\\.reflect" src/main/java
```

Expected:

- no bank-index reads/writes through `ConfigManager`;
- no runtime networking, automation, menu mutation, process execution,
  classloader, or reflection code.

**Step 3: Verify wrapper mode and patch integrity**

```powershell
git ls-files --stage gradlew
git diff --check HEAD
git status --short
```

Expected:

- `gradlew` mode is `100755`;
- no whitespace errors;
- only intentional milestone changes are present.

**Step 4: Review test report**

Open `build/reports/tests/test/index.html` or inspect the XML totals. Record the
executed, failed, skipped, and duration figures in the final implementation
summary.

**Step 5: Commit any verification-only correction**

If verification required a correction:

```powershell
git add <only-the-corrected-files>
git commit -m "Finish runtime foundation verification"
```

If no correction was needed, do not create an empty commit.

## Milestone Exit Criteria

- Linux can execute `./gradlew`.
- No observed item data is read from or written to RuneLite configuration.
- Account hash zero exposes no active persistent index.
- Switching accounts persists and unloads the previous index before loading the
  next.
- Persisted JSON contains no item names, account hash, or display name.
- Corrupt JSON is quarantined and reported without crashing.
- Default retention is 5,000 with a 20,000 hard ceiling and visible-bank
  protection.
- Panel commands run through `ClientThread`; panel renders run through Swing EDT
  with stale revision rejection.
- Full clean tests, privacy scan, runtime safety scan, and whitespace checks pass.
