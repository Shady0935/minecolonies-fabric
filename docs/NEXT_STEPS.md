# Next steps

The datagen, lifecycle, gameplay-callback, mob-conversion and first server-side
colony fixture checkpoints are now complete: `runDatagen` is reproducible, the
portable providers are wired to the Fabric entrypoint, retained
lifecycle/event handlers receive real Fabric callbacks, structure packs load
through Fabric server lifecycle, generated JSON is included in the resource
source set, and the populated-tavern conversion creates a real visitor entity.
The following work remains, in order:

1. Run a real gameplay pass with an authenticated client or a repeatable
   offline test profile: place the supply camp/Town Hall, create a colony,
   open each BlockUI screen, and exercise a builder, citizen, warehouse and
   research cycle. Include an inventory/GUI visual check for the custom spear
   renderer.
2. Extend the Fabric GameTest/focused integration fixtures from the current
   representative network and citizen coverage to every client-bound family,
   then turn the populated-colony stop/restart smoke test into an explicit
   citizen-persistence assertion.
3. Cover the remaining Forge event points that have no direct Fabric callback
   only where a narrow mixin or server hook can preserve semantics. The retained
   `ArrowLooseEvent` bridge is now covered; keep the unsupported `ArrowNock`
   extension point and other gaps documented when that is not technically safe.
4. Port the entity DataFixer registrations or document a deliberate migration
   policy for existing MineColonies 1.20.1 saves.
5. Port the eight excluded Forge-only datagen providers and compare their
   generated tags, advancements, damage data, entity icons and translations
   against the preserved upstream resources.
6. Decide whether the standalone MultiPiston feature belongs in the supported
   Fabric distribution; if it does, port it as its own Fabric module rather
   than hiding its Forge implementation behind compatibility stubs.
7. Revisit JEI and JourneyMap integrations only after the core gameplay pass,
   using explicit Fabric APIs and keeping them optional.
8. Package and test a dedicated-server distribution with secure authentication
   and a fresh client/server pair, then keep the implementation checkpoint
   commits clean and reproducible.
