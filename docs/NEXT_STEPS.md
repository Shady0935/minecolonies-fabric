# Next steps

The datagen, lifecycle, gameplay-callback, mob-conversion, first server-side
colony fixture, builder work-order/assignment, residence assignment,
warehouse/courier resolver wiring, Miner work-order/assignment, Farmer field
assignment, Guard Tower/knight assignment, RaidManager eligibility/event
registration, research-manager and University worker-tick checkpoints are now
complete:
`runDatagen` is reproducible with
all 37 providers, native advancements/icons/quests are wired to the Fabric
entrypoint, the generated resource set is the runtime source of truth, retained
lifecycle/event handlers receive real Fabric callbacks, structure packs load
through Fabric server lifecycle, and the populated-tavern conversion creates a
real visitor entity. The latest clean Fabric GameTest run passes all 29
required tests, covers construction and NBT reload behavior for all 25
currently registered custom entity types (including the two intentional
non-persistent projectile cases), and verifies Forge event priority plus
inherited listener dispatch. Its 28-test core batch also exercises real
client-to-server Town Hall rename and University research actions; entity AI,
combat and client rendering are still open.
The following work remains, in order:

1. Run a real gameplay pass with an authenticated client or a repeatable
   offline test profile: place the supply camp/Town Hall, create a colony,
   open each BlockUI screen, and exercise a builder, citizen, farmer, miner,
   Guard Tower, warehouse and research cycle. The server-side research manager and
   University worker-tick
   paths are automated, so the remaining research work here is the real
   researcher AI/walking and GUI cycle.
   Include an inventory/GUI visual check for the custom spear renderer.
2. Extend the Fabric GameTest/focused integration fixtures from the current
   representative server/client network coverage, including the real
   client-to-server Town Hall and University research actions, fishing-hook and citizen coverage to every
   client-bound family; the seven registered menu opening buffers now have an
   automated contract test. The dedicated restart probe already confirms
   representative citizen and visitor reappearance; broaden it to every
   custom entity family when their gameplay fixtures exist.
3. Continue covering the remaining Forge event points only where a narrow mixin
   or server hook can preserve semantics. Item toss/pickup, farmland trampling
   and bucket filling now have focused Fabric bridges and cancellation coverage;
   hostile natural/chunk-generation position checks, living-entity explosion
   damage, `ExplosionEvent.Start` cancellation and complete detonate block and
   entity filtering are also covered. The retained
   `ArrowNockEvent`, `ArrowLooseEvent`, projectile-impact and fishing-loot
   bridges are covered; remaining unsupported Forge points stay documented
   individually.
4. Keep the documented DataFixer policy aligned with upstream: no custom
   MineColonies 1.20.1 entity schemas exist in either the Forge source or the
   modern Fabric reference, so do not invent migrations. Revisit only if
   upstream publishes schemas or a real legacy-save requirement is defined.
5. Keep the retained upstream generated snapshot as an audit baseline. The
   semantic comparison is complete: native Fabric output is authoritative at
   runtime, and the deliberate differences are recorded in
   `PORTING_NOTES.md`; the snapshot is no longer part of the runtime source
   set.
6. Decide whether the standalone MultiPiston feature belongs in the supported
   Fabric distribution; if it does, port it as its own Fabric module rather
   than hiding its Forge implementation behind compatibility stubs.
7. Revisit JEI and JourneyMap integrations only after the core gameplay pass,
   using explicit Fabric APIs and keeping them optional.
8. Package and test a dedicated-server distribution with secure authentication
   and a fresh client/server pair, then keep the implementation checkpoint
   commits clean and reproducible.
