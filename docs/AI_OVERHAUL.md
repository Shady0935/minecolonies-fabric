# AI overhaul work log

## Scope and source plan

This branch isolates the AI, navigation, decision-making, and performance work
for the MineColonies Fabric 1.20.1 port. The complete execution plan is kept in
the repository root at `AI_OVERHAUL.md`; this file records findings,
implementation decisions, validation, metrics, and checkpoints.

## Starting point

- Branch: `ai-overhaul`.
- Base: `origin/version/main` at `2964454e155a3b43ffe28330e2204431f14bf81f`.
- Stable port tag: `port-stable-baseline-2026-10-06` (code commit
  `4da9f1d8c9`).
- Minecraft/loader target: Fabric 1.20.1; Java 17.
- Port code: `project/minecolonies`; dependency modules: `project/libs`.
- The original vanilla checkout remains separate and unchanged.

## Progress

### Phase 0 — audit and baseline

- [x] Read the full `AI_OVERHAUL.md` plan (1,100 lines).
- [x] Review workspace layout, upstream mapping, recent port status, test
  matrix, pending validation, and known limitations.
- [x] Audit the current AI/navigation code and identify baseline metrics.
- [x] Run and record an unmodified AI-relevant baseline.
- [x] Add opt-in AI/pathfinding, Builder, and Deliveryman instrumentation.

### Phase 1 — upstream AI and pathfinding backport

- [x] Review upstream history and classify applicable changes against the 1.20.1 port.
- [x] Backport safe Builder work-position search, directional path costs, liquid headroom collision handling, and staged stuck recovery.
- [x] Compile and run the full 128-test GameTest suite; inspect logs and metrics.
- [x] Commit and push the required upstream-backport checkpoint.

### Checkpoints

- `checkpoint: baseline AI instrumentation` — `644b4319ee`, committed and pushed to `origin/ai-overhaul`.
- `checkpoint: upstream AI and pathfinding backport` —
  `7aabb36b9e2aa5c38d4fc8b8f6285151a76aaf74`, committed and pushed to
  `origin/ai-overhaul`.

## Findings

The stable workspace documentation records a 125-test combined Fabric GameTest
run and a packaged dedicated-server start/save/stop baseline. Interactive
in-game worker behavior and several client GUI checks remain pending. These
existing results are context, not measurements of AI pathing or worker
efficiency.

The AI branch ran the current 128-test GameTest suite before instrumentation.
That run had one required failure: `farmeraiwalkstoassignedfieldandharvestscrop`
timed out with the farmer still `PREPARING` and the crop untouched. A later
instrumented run also executed 128 tests and had two different required
failures: the Stonemason and Farmer hoe fixtures could not create live
citizens. The long Farmer-to-Courier navigation case passed in that run. The
changing failures indicate a flaky or fixture-sensitive suite; they do not
establish that instrumentation caused either failure. Console and JUnit logs
are kept locally in ignored files under `logs/`.

Audited flows include citizen/state-machine tick dispatch, worker AI skeletons,
walk proxies/navigation, asynchronous path jobs, stuck recovery, structure
work, Builder, Deliveryman, Farmer, Miner, guards, request interactions, and
citizen scheduling. Existing Deliveryman destination batching and Miner shaft,
ladder, graph, and proxy behavior are already present and should be preserved
unless later measurements show a specific problem. The Builder currently picks
one random work position and invalidates it by distance; a later planner must
build on this behavior rather than replace it blindly.

## Metrics

The opt-in profiler is enabled with `-Dminecolonies.ai.metrics=true` and is
disabled by default. It reports 60-second aggregates for AI tick time, async
path duration/node counts, submitted/completed/cancelled paths, repaths, stuck
events/recovery attempts, movement distance/activity ticks, state/target
changes, Builder work-position changes/actions/placement steps/block changes,
and Deliveryman deliveries/items/returns/destinations/grouped deliveries and
route distance. Structure block-state comparisons only run when enabled.
Builder placed/removed counts describe changed blueprint positions, not
physical blocks represented by multi-block structures. One Builder action is
one `StructurePlacer` call; placement steps are successful build-phase results.

The profiling run is instrumentation evidence rather than a comparable
performance baseline: it contains mixed GameTest workloads, and counters reset
each minute. One sample window (`08:27:25`) reported 100,632 citizen AI ticks,
119 submitted/completed paths, 11 repaths, 39,838 visited nodes, 0.749 ms
average path time, 1,834.151 blocks of citizen movement, 12 Builder actions
(11 successful placement steps), and 2 deliveries of 2 items over 20.159
blocks of delivery-route distance. That runtime preceded the final per-position
Builder block counters and `maxPathMs` output; the latest source including
those additions compiled, but those fields have not yet been captured in a
runtime session.

Validation on the latest source:

- `project/libs/structurize`: `gradlew build --no-daemon -x sourcesJar -x remapSourcesJar` — passed.
- `project/minecolonies`: `gradlew build --no-daemon -x sourcesJar -x remapSourcesJar` — passed (Java 17; existing deprecation/removal warnings).
- Full baseline GameTest: 128 tests, 1 required failure: `farmeraiwalkstoassignedfieldandharvestscrop` timed out with the farmer `PREPARING` and the crop untouched (`logs/ai-overhaul-baseline-gametest-20261006.log`, ignored by Git).
- Full instrumented GameTest: 128 tests, 2 different required fixture failures: Stonemason and Farmer hoe fixtures could not create live citizens. The long Farmer-to-Courier navigation case passed. Logs and JUnit output are in ignored local files under `logs/`.
- `runDatagen` stops on an existing invalid icon resource name, `minecolonies:citizen/nether/BaseSkelF` (`DefaultEntityIconProvider.java:98`); outside the AI work.
- A clean `multipiston` build stops because its Structurize development JAR reports `Namespace mismatch, expected intermediary got named`; the MineColonies build used the already-built local dependency artifact.

## Phase 1 — upstream AI and pathfinding backport

### Candidate classification and implementation

- **Applicable with 1.20.1 adaptation — `PathJobMoveCloseToXNearY`:** added a path job that finds a walkable work position near the active construction block while preferring positions close to the work order. Builder path options are configured before async search begins, and drops are disabled for this search. The Builder reuses its selected `workFrom`, invalidates it when standing above the block or after stuck recovery, and can continue an adjacent build step while selecting the next position. The no-work-order GameTest path uses the Builder hut as its nearby anchor.
- **Applicable — stuck recovery:** starts at recovery level -5 and waits five seconds before early retries. It advances one node at a time before stronger recovery, no longer decreases progress on a non-advancing index, ignores a null path while async calculation is still active, and reserves global timeout/goal teleport for an active safe destination. Unsafe or intermediate routes stop instead of using full-goal recovery. A safe-goal match is cleared when an explicitly unsafe path job reuses that same position.
- **Applicable — directional path costs:** added upstream's perpendicular-facing penalty through the port's existing `calcAdditionalCost` hook, checking the entered and exited blocks plus their support blocks. The default penalty matches upstream.
- **Applicable — liquid collision bounds:** when checking headroom over liquid, use a full block shape instead of the liquid's often-empty collision shape.
- **Already represented — navigation result semantics:** the port already distinguishes path calculation, following, completion, cancellation, and whether the destination was reached in `PathResult`; the shared walk proxy also returns arrival separately from continuing movement. `moveToXYZ` already reuses equivalent active destinations. The broad upstream `EntityNavigationUtils` and public API rename were not copied wholesale because they would replace working 1.20.1 proxy/navigation APIs without adding a distinct behavior benefit.
- **Already represented — Miner routing and ladder entry:** Miner shaft/ladder/graph proxies and specialized path jobs exist. Ladder following already offsets toward the ladder by 0.4 blocks; the different modern offset was not layered on top.
- **Deferred — node reopening, corner visitation, and expensive-route cutoffs:** modern commits depend on a substantially different A* visited/open-node loop and search termination. The 1.20.1 port closes nodes under a legacy search and stops at the first destination, so changing these together needs a focused algorithm port and dedicated path fixtures. The node identity fix was unnecessary here: the port stores the node under the same immutable position used to create it.
- **Deferred — mutable-vector allocation:** no profile evidence showed vector allocation as a current AI bottleneck.

### Validation and observations

- Final MineColonies build: `gradlew build --no-daemon -x sourcesJar -x remapSourcesJar` — passed on Java 17; only the existing deprecation/removal warnings remained.
- Full GameTest: `runGametest --no-daemon -PgameTestRunDir=run-gametest-ai-overhaul-phase1-recheck-20261006` ran 128 tests in 499.39 seconds. Two required fixtures failed to create citizens: `stonemasonrequestassignsworkerandcompletescustomrecipe` and `farmeraihoesassignedemptyfield`. These are the same two fixture failures as the earlier instrumented Phase 0 run. The other 126 tests passed.
- Builder navigation and construction tests passed, including `citizenbuilderusesnavigationforconstructionsite`, `buildercompletesmultistageblueprint`, `citizenbuilderexecutesassignedstructurestep`, and the 104.87-second `citizenbuildercompletesworkorderthroughnavigation` end-to-end case. The first Phase 1 attempt exposed a null work order in the navigation-only fixture; the fallback anchor fixed it, and the rerun completed without a server crash.
- Run output: `logs/ai-overhaul-phase1-gametest-recheck.log`; JUnit report: `logs/ai-overhaul-phase1-gametest-junit-20261006.xml`; isolated server log: `project/minecolonies/run-gametest-ai-overhaul-phase1-recheck-20261006/logs/latest.log` (these are ignored local evidence files).
- Opt-in telemetry was active during the suite. A mixed-workload window at `09:08:16` reported 109,909 AI ticks, 215 completed paths, 60 repaths, 94,797 nodes visited, 1.038 ms average path time, 2,549.562 blocks moved, and 22 Builder actions with 16 placement steps (1.231 blocks per action). This does not establish a performance improvement over the Phase 0 window because the two samples cover different test mixes and test timing.
- The sleep-worker batch window at `09:09:16` reported 147 stuck events and 39 recovery attempts, with a 102.836 ms maximum AI tick. The full suite completed, but this artificial shared-world workload is a follow-up signal for an isolated sleep/worker recovery comparison, not evidence of a regression by itself.
- The run had fixture-sensitive citizen creation failures but successfully exercised long Farmer/Courier navigation, Miner, Builder navigation, and full Builder construction. Interactive play was not run in this headless environment.

### Checkpoint

The checkpoint `7aabb36b9e2aa5c38d4fc8b8f6285151a76aaf74` is committed and
pushed to `origin/ai-overhaul`; the phase 1 build passed before the checkpoint.

## Phase 2 — Smart Builder planner

### Implementation

- Added a read-only preview iterator to Structurize. It uses the same iterator
  implementation and progress cursor as live placement without advancing the
  Builder's active iterator or invoking placement callbacks.
- The Builder scans at most 512 iterator entries to collect up to 32 relevant
  operations from the active blueprint stage. It applies stage-specific
  operation filters and does not execute planned actions early.
- Candidate work positions are scored by how many upcoming operations they can
  reach, distance from the citizen, and distance from the work-order location.
  A safe current position that can reach the next operation is used in place;
  route searches disable drops.
- The cached plan records stage, progress cursor, work-order location, candidate
  targets, route anchor, and invalidation reason. It rebuilds on stage, site,
  cursor, target, safety, path, or stuck changes. A saved `workFrom` is reused
  while safe and in range of the next operation, including when it is farther
  from the citizen than the old two-block heuristic allowed.
- The planner recognizes when its iterator preview has exhausted the active
  stage, cancels stale navigation, and lets Structurize complete the phase. This
  prevents the Builder from routing back to the last placed block.
- Added opt-in counters for planner window size, covered operations, in-place
  decisions, and invalidation reasons. Runtime profiling remains disabled by
  default; the GameTest run enabled it with `-PaiMetrics=true`.

### Validation and observations

- `project/libs/structurize`: `gradlew build --no-daemon -x sourcesJar -x remapSourcesJar` — passed.
- `project/minecolonies`: same build command — passed on Java 17; existing
  deprecation/removal warnings remain.
- Focused navigation and end-to-end Builder GameTests passed together with the
  three required Multipiston tests (`All 5 required tests passed`). The
  multi-stage blueprint and the material-delivery end-to-end case also passed
  in earlier focused runs.
- The latest complete GameTest run executed all 128 tests (JUnit duration
  702.971 seconds) and exited with three required failures:
  `citizenbuilderusesnavigationforconstructionsite` ended with no active path
  four blocks from its destination; `farmeraiwalkstoassignedfieldandharvestscrop`
  timed out with the Farmer in `PREPARING`, both crops still mature, and no
  Warehouse pickup request; `citizenbuildercompletesworkorderthroughnavigation`
  ended before construction materials arrived, with its Courier one block from
  the racks and both inventories empty. The two Builder failures passed in the
  focused rerun. A previous full Phase 2 run had only the known Stonemason
  citizen-creation fixture failure. Varying failures across complete-suite runs
  point to fixture/order sensitivity under the shared GameTest world; this is
  not proof that the suite is fully stable.
- In the latest 60-second mixed-workload metrics window (`11:15:14`), the
  Builder recorded 22 actions, 16 placement steps, 25.382 blocks of movement,
  1.154 blocks per action, 161 walking ticks, and 27 `workFrom` changes. The
  planner recorded 16 in-place decisions covering 16 operations. The earlier
  Phase 2 window with the same action and placement counts recorded 26.894
  blocks, 1.222 blocks per action, 385 walking ticks, and 21 `workFrom`
  changes. These are mixed-workload windows rather than a controlled A/B; they
  do not establish a causal performance improvement. Movement was lower in the
  latest window, while `workFrom` changes were slightly higher.
- The full-suite run also produced a high stuck/recovery sample during the
  shared Farmer/Courier workload. Treat it as an isolated follow-up signal,
  consistent with the prior sleep-worker batch warning, rather than as a
  representative steady-state rate.

### Checkpoint

The required checkpoint `checkpoint: smart builder planner` is committed as
`bef9ea7b052d6ec9b921a312a5fadf1034c59031` and pushed to
`origin/ai-overhaul`. The full suite remains order-sensitive; the two Builder
cases that failed there passed in isolation.

## Risks and open questions

- AI timing and pathfinding metrics must remain opt-in or low overhead.
- Async pathfinding thread ownership and server-thread world access need to be
  traced before adding instrumentation or shared caches.
- Existing excluded build/run state is not present in the fresh worktree; reuse
  only after verifying the worktree's build artifacts and test results.
- The high stuck/recovery counters in the mixed sleep-worker GameTest window need a focused before/after comparison before treating them as representative worker behavior.
