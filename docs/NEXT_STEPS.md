# Next steps

1. Bootstrap `project/` from the official MineColonies `release/1.20` source
   without copying its nested Git metadata.
   - Keep `references/minecolonies-1.20.1-forge` untouched.
   - Preserve upstream license headers and resources.

2. Replace the Forge-only root build with a pinned Fabric Loom 1.20.1 build.
   - Start from the smallest common Fabric module.
   - Keep the original source available while the first compile error classes
     are measured.

3. Establish a dependency strategy for BlockUI and Structurize before changing
   MineColonies gameplay code.
   - Compare registries, networking, lifecycle and client entrypoints with
     `references/minecolonies-fabric-port-26.2`.
   - Decide whether each library is a local Fabric subproject or a separately
     built dependency.

4. Build an initial Forge-to-Fabric inventory from the 1.20.1 source.
   - Group residual APIs by registry, event, config, networking, capability,
     rendering and data generation.
   - Use reduction of error classes as the progress metric.

5. Add the first clean build checkpoint and update the test matrix with the
   actual Gradle command and first failure cause.

