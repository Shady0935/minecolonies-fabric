# Porting status

Checkpoint: 2026-09-18. A checked item is verified in the target workspace;
it is not inferred only from an upstream reference. This is a functional
runtime checkpoint, not a claim that every gameplay path has been manually
completed.

## Workspace and references

- [x] Dedicated workspace inspected
- [x] Upstream references cloned and kept isolated
- [x] Exact branches and checkout commits recorded
- [x] Usable modern Fabric architectural reference identified
- [x] Root implementation repository baseline committed as the current runtime
  checkpoint; further gameplay work remains on top of it

## Build and dependencies

- [x] Fabric Loom bootstrap for Minecraft 1.20.1
- [x] Fabric Loader/API and Java 17 pinned
- [x] BlockUI Fabric development dependency resolved
- [x] Domum Ornamentum Fabric development dependency built and loaded
- [x] Structurize Fabric development dependency built and loaded
- [x] MultiPiston decision recorded: no runtime MineColonies source usage was
  found, so the Forge-only standalone mod is not included in this target yet
- [x] MineColonies Fabric 1.20.1 source compiles and packages
- [x] Clean reproducible `build` checkpoint

## Runtime

- [x] Fabric datagen task and portable provider set; `runDatagen` registered 27
  providers and generated 764 JSON resources. The eight remaining Forge-only
  providers are explicitly excluded and tracked in the limitations.
- [x] Dedicated server startup reaches `Done`
- [x] Dedicated server `save-all`/`stop` and same-world restart verified
- [x] Client bootstrap reaches OpenAL and all texture atlases
- [x] Fabric built-in renderer registration for the MineColonies spear is
  lifecycle-safe; renderer construction is deferred until `EntityModelSet` is
  available (visual item rendering still needs a manual pass)
- [x] Client/server classloading audit completed without a startup crash
- [x] Fabric play networking registered in both directions
- [x] Client-bound message codecs are server-loadable; visual execution is
  isolated behind the client bridge while preserving the upstream message IDs
- [x] Offline local client/server login smoke test reaches the world and sends
  the MineColonies server UUID packet
- [x] Fabric lifecycle bridge dispatches server/client ticks, world/chunk/entity
  lifecycle, commands, login/logout and datapack reload callbacks
- [x] Fabric gameplay bridge dispatches right-click block/item/entity, attack,
  pre-break, pre-place, dimension-change, damage, death and mob-conversion
  callbacks; the retained bow adapter dispatches and respects cancellation of
  `ArrowLooseEvent`; client tooltip and disconnect callbacks are also connected
- [x] Server JSON listeners are registered through Fabric's server-data reload
  manager and rebuild 130 worker recipes, 201 research recipes, quests and
  1,455 item-NBT compatibility rules

## Gameplay systems

- [x] Core registries, recipes, tags, sounds, particles and content bootstrap
- [x] Supply loot modifier targets vanilla dungeon/shipwreck tables and
  preserves instant-placement NBT, exercised by Fabric GameTest
- [x] Colony saved data, ownership permissions and NBT round-trip exercised by Fabric GameTest
- [x] Citizen entity creation, colony registration and citizen-data NBT
  round-trip exercised by Fabric GameTest
- [ ] Citizen rendering/work cycle and hostile custom entities exercised
- [x] Town Hall and colony creation exercised server-side by Fabric GameTest
- [x] Colony protection callback denies unauthorized Town Hall access and
  accepts the colony owner through Fabric's interaction event
- [x] Mob conversion callback dispatches the retained `LivingConversionEvent.Pre`,
  preserves the tavern visitor conversion gate and has an end-to-end populated
  tavern fixture that creates a `VisitorCitizen` and discards the vanilla candidate
- [x] Forge-shaped bow hooks preserve neutral `ArrowNock` semantics and dispatch
  retained `ArrowLooseEvent` cancellation; the Pharao Scepter use/shot gate is
  covered by Fabric GameTest
- [ ] Builder and Builder Hut
- [ ] Residence and housing
- [ ] Warehouse and courier logistics
- [ ] Farmer and Miner
- [ ] Research
- [ ] Guards and raids
- [x] Structurize pack discovery and Town Hall blueprint lookup exercised by Fabric GameTest
- [ ] BlockUI screens and client-to-server gameplay actions

## Validation

- [x] Automated Java compilation and package build
- [x] Client bootstrap reaches resource reload and OpenAL after the spear
  renderer registration fix; the visual result remains manual-validation work
- [x] Automated resource serialization coverage: datagen completed and a
  recursive JSON parse covered 1,258 source plus generated JSON files
- [x] Network transport smoke coverage for login/server-to-client packet path
- [x] Fabric GameTest suite covers registry bootstrap, Colonial pack discovery,
  Town Hall registration, colony creation, permissions, NBT round-trip and
  representative network codec/split-envelope behavior, plus citizen entity
  registration and serialization, supply-loot target tables, Town Hall
  protection callbacks, populated-tavern mob-conversion replacement and bow-hook
  compatibility semantics
- [x] Core runtime command/entity smoke test and datapack reload
- [x] Gameplay callback adapter compiles and is loaded by both dedicated-server
  and client bootstrap; server-side Town Hall protection and tavern visitor
  replacement are exercised by real colony fixtures, while the retained bow
  cancellation path is exercised by a focused fixture
- [x] Core colony creation and persistence runtime fixture
- [x] Dedicated-server restart after common S2C registration changes
- [x] Manual validation list and remaining limitations documented
