# Test matrix

Commands are run from the target workspace using Java 17 and PowerShell.

| Area | Status | Evidence |
|---|---|---|
| Workspace/reference checkout | `AUTOMATED_PASS` | Reference repositories and commit map are present under `references/`; implementation is isolated under `project/`. |
| Fabric dependency projects | `AUTOMATED_PASS` | Structurize and Domum Ornamentum Fabric 1.20.1 builds passed; their dev jars load with the target. |
| Java compilation | `AUTOMATED_PASS` | `project/minecolonies/gradlew.bat compileJava --no-daemon` passed after the Fabric lifecycle and gameplay callback bridges. |
| Fabric 1.20.1 package build | `AUTOMATED_PASS` | `project/minecolonies/gradlew.bat build --no-daemon`; the client-message bridge verification is recorded in `logs/minecolonies-build-s2c-bridge.log`. The final JAR contains all 1,258 JSON resources and zero datagen-cache entries. |
| Datagen | `AUTOMATED_PASS` | `runDatagen` completed with 27 portable providers, 764 generated JSON resources and 2,399 cached writes; evidence is in `logs/minecolonies-datagen-003.log`. |
| Generated resource JSON | `AUTOMATED_PASS` | Recursive parsing of source plus generated resources succeeded for 1,258 JSON files; generated custom ingredient arrays use Fabric's `fabric:any` serializer. |
| Dedicated server startup | `AUTOMATED_PASS` | `project/minecolonies/run/logs/latest.log` reaches `Done` after the server-side client-message registration audit, loads MineColonies listeners and structure packs, and contains no client-classloading crash. |
| Dedicated save/stop/restart | `AUTOMATED_PASS` | The same `run/world` restored the existing colony, accepted `save-all`, stopped with all dimensions saved, restarted to `Done`, and stopped cleanly again; the latest run is in `project/minecolonies/run/logs/latest.log`. |
| Client bootstrap | `AUTOMATED_PASS` | `logs/minecolonies-runclient-s2c-bridge.log` reaches OpenAL and all texture atlases without a crash after the client-message bridge changes. |
| Client/server login | `AUTOMATED_PASS` | `project/minecolonies/run/logs/latest.log` records `PortTest logged in`, `New Server UUID`, `PortTest joined the game`, and clean chunk saves. |
| Networking decoder path | `AUTOMATED_PASS` | The login-time MineColonies server-to-client UUID packet completed without packet/decoder errors; all client-bound message codecs now register on the server through the common bridge while preserving IDs. |
| Fabric lifecycle bridge | `AUTOMATED_PASS` | Fabric callbacks dispatch retained MineColonies server/client ticks, world/chunk/entity lifecycle, commands, login/logout and datapack events; compilation and `logs/minecolonies-runserver-lifecycle.log` are clean. |
| Fabric gameplay callback bridge | `AUTOMATED_PASS` | `FabricGameplayHooks` compiles and is loaded during `logs/minecolonies-gameplay-hooks-startup.log`; it connects real Fabric callbacks for interaction, attack, pre-break/pre-place, dimension change, damage and death. |
| Server JSON listeners | `AUTOMATED_PASS` | Startup and `/reload` rebuild 130 worker recipes, 201 research recipes, quests and 1,455 item-NBT compatibility rules without custom ingredient or reload errors. |
| Command/entity smoke | `AUTOMATED_PASS` | `/minecolonies help`, `/minecolonies colony list`, `/summon minecolonies:citizen` and `/data get entity` executed on the dedicated server. The isolated summoned citizen is intentionally discarded because it has no colony assignment. |
| Datapack reload | `AUTOMATED_PASS` | `/reload` re-ran the six MineColonies server-data listeners and the server stopped cleanly after the reload. |
| Secure/authenticated multiplayer | `MANUAL_VALIDATION_PENDING` | Development credentials returned HTTP 401, so the smoke test used temporary offline mode and restored secure defaults afterward. |
| Registry/resource bootstrap | `AUTOMATED_PASS` | Common/client entrypoints load registries, recipes, sounds, particles, tags and models; no Forge model-loader or MineColonies missing-sound errors remain. |
| Town Hall GUI/colony creation | `AUTOMATED_PASS` / `MANUAL_VALIDATION_PENDING` | Server-side creation, ownership lookup, Town Hall registration and NBT round-trip pass in `logs/minecolonies-gametest-s2c-bridge.log`; GUI/client interaction remains pending. |
| Citizen rendering/work cycle | `MANUAL_VALIDATION_PENDING` | Requires spawning/creating citizens in-game. |
| Builder/blueprint placement | `MANUAL_VALIDATION_PENDING` | Requires BlockUI/Structurize interaction in-game. |
| Structurize pack discovery and blueprint lookup | `AUTOMATED_PASS` | Structurize server-start loading discovers the nested MineColonies style packs; the Colonial Town Hall blueprint is resolved during `logs/minecolonies-gametest-009.log`. |
| Serialization and saved colony data | `AUTOMATED_PASS` / `MANUAL_VALIDATION_PENDING` | Colony permission/NBT round-trip passes in the GameTest fixture; full server stop/restart of a populated colony and DataFixer migration remain pending. |
| Automated tests | `AUTOMATED_PASS` | The Fabric GameTest batch in `logs/minecolonies-gametest-s2c-bridge.log` reports `All 2 required tests passed`. |
