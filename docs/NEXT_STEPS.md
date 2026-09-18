# Next steps

The datagen, lifecycle and gameplay-callback checkpoints are now complete:
`runDatagen` is reproducible, the portable providers are wired to the Fabric
entrypoint, retained lifecycle/event handlers receive real Fabric callbacks,
and generated JSON is included in the resource source set. The following work
remains, in order:

1. Run a real gameplay pass with an authenticated client or a repeatable
   offline test profile: place the supply camp/Town Hall, create a colony,
   open each BlockUI screen, and exercise a builder, citizen, warehouse and
   research cycle.
2. Add Fabric GameTest or focused integration fixtures for registry contents,
   packet encode/decode, split-packet reassembly, gameplay permissions,
   colony persistence and the supply-loot target table set.
3. Cover the remaining Forge event points that have no direct Fabric callback
   only where a narrow mixin or server hook can preserve semantics; keep the
   unsupported paths documented when that is not technically safe.
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
