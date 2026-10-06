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
- [ ] Audit the current AI/navigation code and identify baseline metrics.
- [ ] Run and record an unmodified AI-relevant baseline.

### Checkpoints

- None yet.

## Findings

The stable workspace documentation records a 125-test combined Fabric GameTest
run and a packaged dedicated-server start/save/stop baseline. Interactive
in-game worker behavior and several client GUI checks remain pending. These
existing results are context, not measurements of AI pathing or worker
efficiency; this branch must establish AI-specific metrics before optimization.

## Metrics

No AI-specific baseline has been measured yet.

## Risks and open questions

- AI timing and pathfinding metrics must remain opt-in or low overhead.
- Async pathfinding thread ownership and server-thread world access need to be
  traced before adding instrumentation or shared caches.
- Existing excluded build/run state is not present in the fresh worktree; reuse
  only after verifying the worktree's build artifacts and test results.
