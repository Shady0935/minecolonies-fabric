# Porting notes

## Baseline and architecture

The official MineColonies 1.20.1 Forge source is the gameplay and content
baseline. The 1.21.1 NeoForge tree and the modern Fabric checkout are used as
API migration references only; modern gameplay code is not copied wholesale
into the 1.20.1 target.

The target build lives under `project/minecolonies`. Its main source combines
the upstream API and implementation trees while excluding only the optional
JEI/JourneyMap integration packages. Fabric entrypoints are declared in
`fabric.mod.json` for common, client, datagen and GameTest initialization.

The runtime resource source set is limited to `src/main/generated/resources`
and `src/main/resources`. The tracked official 1.20.1 generated snapshot at
`project/minecolonies/src/datagen/generated/minecolonies` remains available as
an audit baseline, but it is no longer loaded at runtime now that the native
Fabric providers cover its generated advancements, quests, translations and
entity icons.

## Dependency decisions

- BlockUI, Domum Ornamentum and Structurize are local Fabric 1.20.1 development
  dependencies under `project/libs` and are loaded by the dev runtime.
- MultiPiston remains an upstream Forge-only standalone dependency in the
  reference material. No MineColonies main-source reference requires it in the
  target runtime, so it is not silently represented by a no-op or fake registry.
  A separate Fabric port can be added if the final gameplay audit proves that
  the standalone block is required.
- JEI and JourneyMap source/integration packages remain excluded until their
  Fabric APIs are deliberately selected.

## Fabric adapters implemented

- Registries and lifecycle bootstrapping use Fabric entrypoints and Fabric
  events while retaining the upstream registry order.
- Forge-shaped compatibility types provide the narrow APIs still used by the
  1.20.1 source. They are bridges, not blanket no-op replacements.
- The Fabric common entrypoint explicitly registers MineColonies' custom
  ingredient serializers before the first resource reload. The retained
  Forge-shaped registry event performs the same idempotent registration as a
  compatibility bridge.
- Capability-like data is bridged through explicit provider hooks and weak
  identity maps where Fabric 1.20.1 has no direct Forge capability equivalent.
- Supply loot uses `LootTableEvents.MODIFY` and the exact upstream target table
  sets. The Forge global-loot-modifier JSON files were removed because Fabric
  does not load that registry.
- Forge composite block models are flattened into ordinary vanilla model JSON;
  the resource audit found no remaining Forge/NeoForge model loader entries.
- Forge worldgen tag names such as `forge:is_plains` and `forge:is_peak` are
  retained as explicit Fabric-compatible tag namespaces because the upstream
  structure data references them.
- Forge-only loader metadata (`META-INF/mods.toml` and
  `META-INF/accesstransformer.cfg`) is not part of the Fabric source or
  artifact. The package audit confirms that the JAR contains the Fabric
  descriptor and mixin configuration only.

## Lifecycle and datapack events

The Fabric common entrypoint now bridges the lifecycle callbacks that the
retained MineColonies event handlers require: server starting/started/stopping,
server and world ticks, world/chunk/entity load, player login/logout, command
registration and datapack reload/synchronisation. Client start/end ticks are
bridged by the client entrypoint.

The six MineColonies server-data listeners raised by the retained reload event
are wrapped as Fabric `IdentifiableResourceReloadListener` instances and
registered with `ResourceManagerHelper`. This keeps the existing listener
implementations while giving Fabric control of reload ordering and execution.
The verified startup and `/reload` path rebuilds 130 worker recipes, 201
research recipes, quests and 1,455 item-NBT compatibility rules.

## Gameplay callbacks

`FabricGameplayHooks` connects the retained permission and quest handlers to
Fabric's real 1.20.1 callbacks: right-click block/item/entity, entity attack,
block-break preflight, player dimension changes, living-entity damage and
living-entity death and `ServerLivingEntityEvents.MOB_CONVERSION`. The
conversion callback posts the retained `LivingConversionEvent.Pre` before the
candidate entity is spawned; if the MineColonies handler replaces a vanilla
conversion with a tavern visitor, the Fabric candidate is discarded. The
retained `canLivingConvert` check is permissive because the Fabric callback is
the cancellation gate in this port. Block placement uses
`BlockPlaceContext.canPlace()` and the resolved placement position to raise the
retained `EntityPlaceEvent` before vanilla performs the placement; cancelling
that event prevents the placement.

Several Forge gameplay events that have no direct Fabric callback now use narrow
adapters rather than broad compatibility no-ops. `Player.drop` posts the
retained `ItemTossEvent` after vanilla creates the item entity and discards that
entity when a listener cancels; `ItemEntity.playerTouch` posts
`EntityItemPickupEvent` before vanilla consumes the stack. `FarmBlock.fallOn`
redirects only the `turnToDirt` mutation, so a canceled `FarmlandTrampleEvent`
preserves the farmland while retaining vanilla fall damage. Finally,
`FabricGameplayHooks` posts `FillBucketEvent` for bucket use with the same
raycast target before vanilla performs the fill. The focused server fixture in
`logs/minecolonies-gametest-explosion-damage-retry.log` verifies the three
mixin-backed cancellation paths plus the hostile-spawn and living-entity
explosion-policy bridges; generic explosion phases still require different
internal interception points.

The retained `MobSpawnEvent.PositionCheck` is bridged at the two vanilla
`NaturalSpawner` spawn-rule gates: natural spawning and chunk-generation
spawning. The hook posts the event with the mob's resolved position before
calling vanilla `checkSpawnRules`; a `DENY` result blocks only that candidate.
Spawner, spawn-egg, `/summon`, raid and MineColonies-owned spawn paths do not
pass through this redirect, matching the upstream handler's explicit spawner
exception and avoiding a broad entity-load veto.

Explosion protection has a deliberately split scope on 1.20.1. The existing
`ALLOW_DAMAGE` callback now applies `turnOffExplosionsInColonies` to living
entities with the upstream policy: `DAMAGE_PLAYERS` protects non-hostile
colony entities, `DAMAGE_NOTHING` protects all non-player colony entities, and
the two permissive policies allow the damage. A narrow `Explosion` mixin now
posts the retained `ExplosionEvent.Start` before vanilla computes the blast;
when the colony handler cancels it, both blast computation and finalization are
skipped. The adapter also reads vanilla's actual `x/y/z` fields, not the
nonexistent Forge `getPosition()` method. Fabric 1.20.1 still exposes no
generic detonate callback, so a narrow interception immediately before
vanilla's entity-damage loop publishes `ExplosionEvent.Detonate` with both
live affected-block and affected-entity lists. The retained handler can remove
protected colony blocks and non-living entities before vanilla applies damage
or destroys blocks. The existing `Explosion.Start` cancellation still skips
both computation and finalization. Start cancellation, living damage, block
filtering and non-living entity filtering are covered by the 12-test GameTest
batch in `logs/minecolonies-gametest-explosion-start.log`.

The client entrypoint also forwards item tooltip and play-connection disconnect
callbacks. The adapter is deliberately callback-only: Fabric 1.20.1 does not
expose a direct equivalent for every Forge event, so unsupported event points
remain documented gaps instead of being represented by no-op shims.

The Forge-shaped bow compatibility surface preserves the upstream neutral
`ArrowNock` contract: when no retained listener supplies a result it returns
`null`, allowing `ItemPharaoScepter` to enter `startUsingItem`. Its
`ArrowLooseEvent` path posts the retained event bus for players and returns
`-1` when a listener cancels the shot, preserving the MineColonies colony
permission handler. The focused GameTest verifies both the scepter-use flow
and the cancellation gate.

## Server-side colony fixture and structure packs

Fabric GameTest is enabled through the `fabric-gametest` entrypoint in
`fabric.mod.json`. `MineColoniesGameTests` verifies common registries, creates
a real Town Hall block entity, creates a colony through `IColonyManager`,
attaches the Colonial Town Hall blueprint, checks ownership/building indexes,
spawns an `EntityCitizen` through `CitizenManager`, round-trips colony and
citizen NBT, and drives the Fabric Town Hall protection callback for both an
outsider and the colony owner. It also dispatches the Fabric mob-conversion
callback into the retained `LivingConversionEvent.Pre` event and builds a
populated level-one tavern fixture. That fixture resolves the owning colony
from the claimed chunk, creates exactly one `VisitorCitizen` through the
retained visitor manager, assigns it to the tavern and discards Fabric's
vanilla villager candidate. The earlier eight-test batch is recorded in
`logs/minecolonies-gametest-tavern-10.log`. The current nine-test batch also
verifies the Pharao Scepter bow-hook bridge and is recorded in
`logs/minecolonies-gametest-arrow-hooks.log`.
The same batch also verifies the Fabric loot-table modifier against dungeon and
shipwreck targets, preserving the supply items' instant-placement NBT, and
round-trips a build-window packet and exercises out-of-order split-envelope
reassembly for the login UUID packet, including cache cleanup.

The same shutdown pass exposed that colony serialization can run after the
world reference has been detached. `AbstractSchematicProvider.getRotation()`
now returns the neutral runtime rotation in that serialization-only state,
keeping save/stop clean without dropping the persisted colony payload.

Structurize's server pack loader is connected to Fabric's
`SERVER_STARTING` callback. Its mod-resource scan also descends through the
`blueprints/minecolonies/<style>` layout used by the official MineColonies
styles, so the Colonial and Original packs are available before gameplay
looks up a blueprint. Client-bound network messages used during join and chunk
claim now keep their common codecs server-loadable and delegate visual work to
the client bridge by reflection; their upstream message IDs remain stable.

## Fabric datagen

The target exposes Fabric Loom's `runDatagen` task through the
`fabric-datagen` entrypoint. `MineColoniesDataGenerator` registers the
portable 1.20.1 recipe, research, loot and worker-crafting providers and
explicitly registers the three custom ingredient serializers before provider
execution. This makes datagen independent of runtime event ordering.

The verified run registered 37 providers and generated 3,576 resources:
967 JSON files, 2,609 citizen/raider icon PNGs and 19 conventional `c:` tag
files. Generated resources live under
`project/minecolonies/src/main/generated/resources` and are part of the main
resource source set; the Fabric datagen cache is ignored and excluded from
the packaged resources. The packaged JAR contains 1,453 JSON resources with
paths rooted directly at `assets/` and `data/`; no generated parity snapshot
is needed at runtime.

Custom ingredient arrays are converted to Fabric's `fabric:any` custom
ingredient rather than being placed inside a vanilla `Ingredient` array. This
preserves the upstream alternative-input semantics on Fabric 1.20.1.

All eight formerly Forge-shaped providers now run natively on Fabric: block
tags, damage tags, damage types, entity type tags, item tags, advancements,
entity icons and quest translations. The port also generates the missing `c:`
conventional block/item tags that Fabric API 0.92.2 does not ship, so worker
recipes and item filters do not resolve to missing tags at runtime.

The advancement provider preserves external vanilla parents such as
`minecraft:story/root` through metadata-only parent objects because Fabric's
1.20.1 exporter only resolves parents emitted by the same provider. The icon
provider walks `ModContainer#getRootPaths()` and uses headless Java2D/ImageIO
processing, avoiding the client-only `NativeImage` class during server-side
datagen while preserving the upstream head crop and dark border. Quest
translations are read deterministically from authored quest JSON and emitted
alongside the transformed quest files.

The parity audit compared 948 shared JSON files semantically: 705 are equal to
the retained upstream snapshot and 243 differ only for deliberate Fabric
adaptations. Those adaptations are the native `c:` conventional tags and
replacements for Forge tag references, vanilla-compatible loot-table output,
and removal of the standalone `multipiston:multipistonblock` compatibility
entry because MultiPiston is not in the Fabric runtime. The snapshot remains
tracked as an audit baseline rather than being loaded at runtime.

## Networking

The upstream MineColonies message IDs and fragmentation envelope remain owned
by `NetworkChannel`. The former no-op `SimpleChannel` implementation is now a
real Fabric transport:

1. one Fabric play channel carries the registered split-packet envelope;
2. Fabric server receivers decode C2S messages on the server executor;
3. a client-only bridge registers the S2C receiver and keeps client classes out
   of dedicated-server classloading;
4. player, dimension, nearby, tracking-entity, tracking-chunk and broadcast
   targets map to `PlayerLookup` queries;
5. split payloads copy their actual readable bytes, including zero-length
   messages, and completed reassembly entries are invalidated.

The local smoke test logged the player into a dedicated server and delivered
the login-time `ServerUUIDMessage` without decoder/channel errors. The test
used offline mode only because the development profile had no valid Mojang
credentials; `run/server.properties` was restored to secure defaults afterward.

The server-side GameTest also exercises chunk capability and quest/compatibility
sync registration far enough to ensure join and initial colony creation do not
raise an unknown-message error on the Fabric channel. `GlobalQuestSyncMessage`,
chunk capability updates and compatibility snapshots use the same common
message IDs while their client behavior is isolated in `ClientNetworkHooks`.

The same isolation now covers the remaining client-bound message families:
colony-list/view removal, particles, audio, pathfinding debug updates, build
window opening and scan saving. Their common message classes contain codecs and
server-safe dispatch only; `ClientMessageBridge` resolves the client hook by
reflection after the Fabric client entrypoint is present. `NetworkChannel`
registers these classes on both logical environments without shifting the
upstream numeric IDs. The dedicated server was started, asked to save and stop,
then restarted on the same world after this change; `logs/minecolonies-runserver-citizen-restart.log`
records restoration of MineColonies colony data and a clean save/stop. A
focused follow-up query after the restart found a persisted `EntityCitizen` and
`VisitorCitizen` with their colony/citizen NBT and then saved all dimensions
cleanly; the transcript is in
`logs/minecolonies-runserver-citizen-entity-restart.log`.

The fishing bobber has one additional Fabric-specific spawn-state bridge.
Forge's `IEntityAdditionalSpawnData` is not consumed automatically by Fabric's
vanilla entity-spawn packet path, so `NewBobberEntity` mirrors the angler's
entity ID into `SynchedEntityData`. The client resolves that ID lazily when the
citizen entity becomes available, while the existing extra spawn buffer remains
read for compatibility. The 12-test GameTest batch verifies the synced
round-trip in `logs/minecolonies-gametest-fishing-hook.log`.

The entity DataFixer audit is deliberately closed as a compatibility policy,
not as an unverified TODO. The official Forge 1.20.1 entity initializer builds
the same custom entity types without MineColonies DataFixer schemas, and the
modern Fabric reference does not introduce them either. Minecraft therefore
logs one `No data fixer registered` message per custom entity at bootstrap;
this port preserves upstream behavior and does not invent migration schemas.
Old entity data that needs a version migration remains outside the supported
migration guarantee until upstream defines those schemas.

The container-opening bridge had a separate Forge semantic that the first Fabric
implementation did not preserve: `NetworkHooks.openScreen(provider, writer)`
must forward the extra bytes consumed by the client-side menu factory. It now
uses Fabric's `ExtendedScreenHandlerFactory` and `ExtendedScreenHandlerType`,
so the citizen inventory, building inventory, rack, grave, furnace-crafting,
building-crafting and brewing-stand menus receive the same opening data as the
upstream path. The 10-test GameTest batch verifies all seven registrations and
a non-trivial buffer round-trip in `logs/minecolonies-gametest-extended-screen-handlers.log`;
actual screen rendering and interaction remain manual validation.

## Resource and startup fixes

The port includes the missing English language resource, Fabric-compatible
block/entity tags, upstream sounds, generated MineColonies tags/advancements/
damage data/quest resources and entity icons, vanilla spear model overrides,
lowercase resource paths, and flattened blockhut composite models. The custom
spear item renderer is registered through Fabric's `BuiltinItemRendererRegistry`; its
construction is lazy because the client entrypoint runs before Minecraft has
finished constructing `EntityModelSet`. The fixed bootstrap reaches resource
reload, OpenAL and all atlases without the earlier initialization crash; the
actual inventory/GUI appearance still needs a manual visual pass. Evidence is
in `logs/minecolonies-client-spear-renderer-fixed.log` and remaining warnings
are listed separately.

## Status vocabulary

- `NOT_IMPLEMENTED_YET`: work is planned or incomplete; it is not evidence of
  an incompatibility.
- `KNOWN_LIMITATION`: a confirmed technical limitation with a reproducible
  observation.
- `MANUAL_VALIDATION_PENDING`: the code path is present but requires in-game
  interaction that has not yet been exercised by the terminal-only workflow.
