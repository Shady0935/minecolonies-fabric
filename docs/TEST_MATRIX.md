# Test matrix

Commands are run from the target workspace using Java 17 and PowerShell.

| Area | Status | Evidence |
|---|---|---|
| Workspace/reference checkout | `AUTOMATED_PASS` | Reference repositories and commit map are present under `references/`; implementation is isolated under `project/`. |
| Fabric dependency projects | `AUTOMATED_PASS` | Structurize and Domum Ornamentum Fabric 1.20.1 builds passed; their dev jars load with the target. |
| Java compilation | `AUTOMATED_PASS` | `project/minecolonies/gradlew.bat compileJava --no-daemon` passed after the networking and loot fixes. |
| Fabric 1.20.1 package build | `AUTOMATED_PASS` | `project/minecolonies/gradlew.bat build --no-daemon`; final output is in `logs/minecolonies-build-final.log`. |
| Datagen | `NOT_AVAILABLE` | `gradlew tasks --all` exposes no datagen/runData task; generation source is explicitly excluded pending a Fabric provider port. |
| Dedicated server startup | `AUTOMATED_PASS` | `runServer` reached `Done` in the recorded server runs; rotated logs include `project/minecolonies/run/logs/2026-09-18-2.log.gz` and `2026-09-18-3.log.gz`. |
| Dedicated save/stop/restart | `AUTOMATED_PASS` | Same `run/world` accepted `save-all`, stopped with all dimensions saved, restarted to `Done`, and stopped again cleanly. |
| Client bootstrap | `AUTOMATED_PASS` | `logs/minecolonies-runclient-003-models.log` and `logs/minecolonies-runclient-004-network.log` reach OpenAL/texture atlases without a crash. |
| Client/server login | `AUTOMATED_PASS` | `project/minecolonies/run/logs/latest.log` records `PortTest logged in`, `New Server UUID`, `PortTest joined the game`, and clean chunk saves. |
| Networking decoder path | `AUTOMATED_PASS` | The login-time MineColonies server-to-client UUID packet completed without packet/decoder errors; all target kinds are implemented in the Fabric transport. |
| Secure/authenticated multiplayer | `MANUAL_VALIDATION_PENDING` | Development credentials returned HTTP 401, so the smoke test used temporary offline mode and restored secure defaults afterward. |
| Registry/resource bootstrap | `AUTOMATED_PASS` | Common/client entrypoints load registries, recipes, sounds, particles, tags and models; no Forge model-loader or MineColonies missing-sound errors remain. |
| Town Hall GUI/colony creation | `MANUAL_VALIDATION_PENDING` | Requires a real in-game interaction pass. |
| Citizen rendering/work cycle | `MANUAL_VALIDATION_PENDING` | Requires spawning/creating citizens in-game. |
| Builder/blueprint placement | `MANUAL_VALIDATION_PENDING` | Requires BlockUI/Structurize interaction in-game. |
| Serialization and saved colony data | `MANUAL_VALIDATION_PENDING` | Server persistence was tested for the world lifecycle; colony-specific save/load and DataFixer migration remain untested. |
| Automated tests | `NOT_IMPLEMENTED_YET` | No port-specific unit/GameTest suite has been added yet. |
