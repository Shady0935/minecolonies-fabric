# Next steps

The next work should follow this order so that the port moves from a bootable
runtime checkpoint to a gameplay-complete release candidate:

1. Run a real gameplay pass with an authenticated client or a repeatable
   offline test profile: place the supply camp/Town Hall, create a colony,
   open each BlockUI screen, and exercise a builder, citizen, warehouse and
   research cycle.
2. Add Fabric GameTest or focused integration fixtures for registry contents,
   packet encode/decode, split-packet reassembly, colony persistence and the
   supply-loot target table set.
3. Port the entity DataFixer registrations or document a deliberate migration
   policy for existing MineColonies 1.20.1 saves.
4. Restore data generation as a Fabric entrypoint/task and compare generated
   tags, recipes, loot, models and structures against the preserved upstream
   resources.
5. Decide whether the standalone MultiPiston feature belongs in the supported
   Fabric distribution; if it does, port it as its own Fabric module rather
   than hiding its Forge implementation behind compatibility stubs.
6. Revisit JEI and JourneyMap integrations only after the core gameplay pass,
   using explicit Fabric APIs and keeping them optional.
7. Create the first clean implementation commit, then package and test a
   dedicated-server distribution with secure authentication and a fresh
   client/server pair.
