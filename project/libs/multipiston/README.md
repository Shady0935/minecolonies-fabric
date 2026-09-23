# Multi-Piston Fabric 1.20.1

Standalone Fabric port of the `multipiston:multipistonblock` dependency used by
MineColonies 1.20.1. The module preserves the upstream redstone movement,
configurable input/output faces, range and speed, BlockUI screen, item, recipe,
block entity ID, and NBT field names. Structurize's `IRotatableBlockEntity`
contract remains in use so blueprint rotation and mirroring can operate on the
block entity.

The server owns configuration changes and validates reach, interaction
permission, directions, range and speed before applying the Fabric networking
payload. Active movement direction and tick progress are persisted in optional
NBT keys so restarting during a stroke resumes it. Existing upstream saves
without those keys retain their original defaults.

Build from this directory with `gradlew.bat build --no-daemon`. The isolated
GameTest launcher is `gradlew.bat runGametest --no-daemon`; its JUnit report is
written to `build/gametest/junit.xml`.

This mod requires the workspace's Fabric 1.20.1 builds of BlockUI, Structurize
and Domum Ornamentum. MineColonies Fabric declares this module as a required
runtime dependency. The project uses the upstream GPL-3.0-only license; the
original license is included in the built jar.
