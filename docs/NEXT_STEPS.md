# Next steps

The datagen, lifecycle, gameplay-callback, mob-conversion and first server-side
colony fixture checkpoints are now complete: `runDatagen` is reproducible, the
portable providers are wired to the Fabric entrypoint, the tracked upstream
generated parity snapshot is packaged at valid runtime paths, retained
lifecycle/event handlers receive real Fabric callbacks, structure packs load
through Fabric server lifecycle, and the populated-tavern conversion creates a
real visitor entity.
The following work remains, in order:

1. Run a real gameplay pass with an authenticated client or a repeatable
   offline test profile: place the supply camp/Town Hall, create a colony,
   open each BlockUI screen, and exercise a builder, citizen, warehouse and
   research cycle. Include an inventory/GUI visual check for the custom spear
   renderer.
2. Extend the Fabric GameTest/focused integration fixtures from the current
   representative network and citizen coverage to every client-bound family;
   the seven registered menu opening buffers now have an automated contract
   test. The dedicated restart probe already confirms representative citizen
   and visitor reappearance; broaden it to every custom entity family when
   their gameplay fixtures exist.
3. Continue covering the remaining Forge event points only where a narrow mixin
   or server hook can preserve semantics. Item toss/pickup, farmland trampling
   and bucket filling now have focused Fabric bridges and cancellation coverage;
   hostile natural/chunk-generation position checks and living-entity explosion
   damage are also covered. Explosion start/detonate block filtering, non-living
   blast victims and full start cancellation remain explicit gaps. The retained
   `ArrowLooseEvent` bridge is covered, while the unsupported `ArrowNock`
   extension point stays documented.
4. Keep the documented DataFixer policy aligned with upstream: no custom
   MineColonies 1.20.1 entity schemas exist in either the Forge source or the
   modern Fabric reference, so do not invent migrations. Revisit only if
   upstream publishes schemas or a real legacy-save requirement is defined.
5. Replace the packaged upstream parity snapshot with native Fabric providers
   for the eight excluded datagen areas, then compare freshly generated tags,
   advancements, damage data, entity icons and translations against the current
   validated snapshot.
6. Decide whether the standalone MultiPiston feature belongs in the supported
   Fabric distribution; if it does, port it as its own Fabric module rather
   than hiding its Forge implementation behind compatibility stubs.
7. Revisit JEI and JourneyMap integrations only after the core gameplay pass,
   using explicit Fabric APIs and keeping them optional.
8. Package and test a dedicated-server distribution with secure authentication
   and a fresh client/server pair, then keep the implementation checkpoint
   commits clean and reproducible.
