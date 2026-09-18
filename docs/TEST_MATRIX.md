# Test matrix

Commands are run from the target workspace using Java 17 and PowerShell.

| Area | Status | Evidence |
|---|---|---|
| Workspace/reference checkout | `AUTOMATED_PASS` | Reference repositories and commit map are present under `references/`; implementation is isolated under `project/`. |
| Fabric dependency projects | `AUTOMATED_PASS` | Structurize and Domum Ornamentum Fabric 1.20.1 builds passed; their dev jars load with the target. |
| Java compilation | `AUTOMATED_PASS` | `project/minecolonies/gradlew.bat compileJava --no-daemon` passed after the Fabric lifecycle and gameplay callback bridges. |
| Fabric 1.20.1 package build | `AUTOMATED_PASS` | `project/minecolonies/gradlew.bat build --no-daemon` passed after the tavern visitor-conversion fixture; evidence is in `logs/minecolonies-build-tavern-conversion.log`. The final JAR contains all 1,258 JSON resources and zero datagen-cache entries. |
| Datagen | `AUTOMATED_PASS` | `runDatagen` completed with 27 portable providers, 764 generated JSON resources and 2,399 cached writes; evidence is in `logs/minecolonies-datagen-003.log`. |
| Generated resource JSON | `AUTOMATED_PASS` | Recursive parsing of source plus generated resources succeeded for 1,258 JSON files; generated custom ingredient arrays use Fabric's `fabric:any` serializer. |
| Dedicated server startup | `AUTOMATED_PASS` | `logs/minecolonies-runserver-citizen-restart.log` reaches `Done`, restores MineColonies colonies 1–5, loads listeners and structure packs, and contains no client-classloading crash. |
| Dedicated save/stop/restart | `AUTOMATED_PASS` | The same `run/world` restored colonies 1–5, accepted `save-all`, stopped with all dimensions saved, and completed with `BUILD SUCCESSFUL`; evidence is in `logs/minecolonies-runserver-citizen-restart.log`. |
| Client bootstrap | `AUTOMATED_PASS` | `logs/minecolonies-runclient-s2c-bridge.log` reaches OpenAL and all texture atlases without a crash after the client-message bridge changes. |
| Client/server login | `AUTOMATED_PASS` | `project/minecolonies/run/logs/latest.log` records `PortTest logged in`, `New Server UUID`, `PortTest joined the game`, and clean chunk saves. |
| Networking decoder path | `AUTOMATED_PASS` | `logs/minecolonies-gametest-network-codec.log` round-trips `OpenDecoBuildWindowMessage`, verifies server registration of representative S2C messages, reassembles an out-of-order `ServerUUIDMessage`, and confirms cache invalidation. |
| Fabric lifecycle bridge | `AUTOMATED_PASS` | Fabric callbacks dispatch retained MineColonies server/client ticks, world/chunk/entity lifecycle, commands, login/logout and datapack events; compilation and `logs/minecolonies-runserver-lifecycle.log` are clean. |
| Fabric gameplay callback bridge | `AUTOMATED_PASS` | `FabricGameplayHooks` compiles and is loaded during `logs/minecolonies-gameplay-hooks-startup.log`; it connects real Fabric callbacks for interaction, attack, pre-break/pre-place, dimension change, damage, death and mob conversion. |
| Server JSON listeners | `AUTOMATED_PASS` | Startup and `/reload` rebuild 130 worker recipes, 201 research recipes, quests and 1,455 item-NBT compatibility rules without custom ingredient or reload errors. |
| Supply loot modifier | `AUTOMATED_PASS` | `logs/minecolonies-gametest-supply-loot.log` samples the configured vanilla dungeon and shipwreck tables, observes both MineColonies supply items, and verifies `Placement:"instant"` NBT. |
| Colony protection callbacks | `AUTOMATED_PASS` | `logs/minecolonies-gametest-tavern-10.log` drives Fabric's `UseBlockCallback` through the Town Hall permission handler: an outsider is denied and the colony owner is accepted in an isolated fixture. |
| Mob conversion callback and tavern visitor replacement | `AUTOMATED_PASS` | `logs/minecolonies-gametest-tavern-10.log` dispatches Fabric's `MOB_CONVERSION` callback into the retained `LivingConversionEvent.Pre` bridge, leaves an unrelated conversion intact, and verifies that a populated tavern creates one `VisitorCitizen`, assigns it to the tavern and discards the vanilla candidate. |
| Command/entity smoke | `AUTOMATED_PASS` | `/minecolonies help`, `/minecolonies colony list`, `/summon minecolonies:citizen` and `/data get entity` executed on the dedicated server. The isolated summoned citizen is intentionally discarded because it has no colony assignment. |
| Datapack reload | `AUTOMATED_PASS` | `/reload` re-ran the six MineColonies server-data listeners and the server stopped cleanly after the reload. |
| Secure/authenticated multiplayer | `MANUAL_VALIDATION_PENDING` | Development credentials returned HTTP 401, so the smoke test used temporary offline mode and restored secure defaults afterward. |
| Registry/resource bootstrap | `AUTOMATED_PASS` | Common/client entrypoints load registries, recipes, sounds, particles, tags and models; no Forge model-loader or MineColonies missing-sound errors remain. |
| Town Hall GUI/colony creation | `AUTOMATED_PASS` / `MANUAL_VALIDATION_PENDING` | Server-side creation, ownership lookup, Town Hall registration, protection callback, populated-tavern visitor conversion and NBT round-trip pass in `logs/minecolonies-gametest-tavern-10.log`; GUI/client interaction remains pending. |
| Citizen entity registration/serialization | `AUTOMATED_PASS` / `MANUAL_VALIDATION_PENDING` | `logs/minecolonies-gametest-citizen.log` spawns the registered `EntityCitizen`, links it to `CitizenData`, serializes identity/position and reloads the citizen from colony NBT; rendering and work AI remain pending. |
| Citizen rendering/work cycle | `MANUAL_VALIDATION_PENDING` | Requires client rendering and an in-game work-cycle pass. |
| Builder/blueprint placement | `MANUAL_VALIDATION_PENDING` | Requires BlockUI/Structurize interaction in-game. |
| Structurize pack discovery and blueprint lookup | `AUTOMATED_PASS` | Structurize server-start loading discovers the nested MineColonies style packs; the Colonial Town Hall blueprint is resolved during `logs/minecolonies-gametest-citizen.log`. |
| Serialization and saved colony data | `AUTOMATED_PASS` / `MANUAL_VALIDATION_PENDING` | Colony and citizen permission/NBT round-trips pass in the GameTest fixture; a real restart restored the populated colony records, but explicit post-restart citizen-entity assertions and DataFixer migration remain pending. |
| Automated tests | `AUTOMATED_PASS` | The Fabric GameTest batch in `logs/minecolonies-gametest-tavern-10.log` reports `All 8 required tests passed`. |
