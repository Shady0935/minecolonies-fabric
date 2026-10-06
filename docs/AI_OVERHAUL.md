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

### Checkpoints

- `checkpoint: baseline AI instrumentation` — pending commit and push.

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

## Risks and open questions

- AI timing and pathfinding metrics must remain opt-in or low overhead.
- Async pathfinding thread ownership and server-thread world access need to be
  traced before adding instrumentation or shared caches.
- Existing excluded build/run state is not present in the fresh worktree; reuse
  only after verifying the worktree's build artifacts and test results.
