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
filtering and non-living entity filtering are covered by the 13-test GameTest
batch in `logs/minecolonies-gametest-research-tree.log`.

The client entrypoint also forwards item tooltip and play-connection disconnect
callbacks. The adapter is deliberately callback-only: Fabric 1.20.1 does not
expose a direct equivalent for every Forge event, so unsupported event points
remain documented gaps instead of being represented by no-op shims.

The Forge-shaped bow compatibility surface preserves the upstream
`ArrowNock` contract: it posts a result-bearing `ArrowNockEvent`, returns
`null` when no retained listener supplies an action so `ItemPharaoScepter` can
enter `startUsingItem`, returns a listener-supplied action, and maps
cancellation to Forge's `FAIL` result. Its `ArrowLooseEvent` path posts the
retained event bus for players and returns `-1` when a listener cancels the
shot, preserving the MineColonies colony permission handler. The focused
GameTest verifies the scepter-use flow, alternate nock result, cancellation
and neutral paths.

The retained Forge-shaped event bus now stores listeners and consumers in
copy-on-write collections, walks listener superclasses and orders annotated
methods using Forge's `HIGHEST` through `LOWEST` priorities. GameTests create
and remove colony-scoped permission listeners while other fixtures dispatch
gameplay callbacks; stable snapshots preserve the upstream listener semantics
without concurrent `ArrayList` races. A dedicated probe covers priority order
and an inherited handler, and the latest 33-test core batch exercises this under
concurrent colony fixtures.

Projectile impacts now have a narrow cancellable Fabric bridge through
`ProjectileImpactEvent`. `ForgeEventFactory.onProjectileImpact` posts the
original projectile and hit result and returns the event-bus cancellation
result, so `NewBobberEntity` keeps its upstream "cancel means do not call
onHit" behavior. A focused GameTest verifies dispatch, object identity,
cancellation and listener removal. The custom fishing hook now also posts
`ItemFishedEvent` after loot generation and before item entities are spawned;
listeners can change rod damage or cancel the drops, matching Forge's retained
1.20.1 hook semantics while using MineColonies' `NewBobberEntity` type.

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
`logs/minecolonies-gametest-tavern-10.log`. The current 66-test run also
verifies the Pharao Scepter bow-hook bridge and is recorded in
`logs/minecolonies-gametest-arrow-nock.log`.
The same batch also verifies the Fabric loot-table modifier against dungeon and
shipwreck targets, preserving the supply items' instant-placement NBT, and
round-trips a build-window packet and exercises out-of-order split-envelope
reassembly for the login UUID packet, including cache cleanup.

The builder-order fixture creates a `WorkOrderBuilding` for the Colonial Town
Hall, claims the full blueprint footprint required by the upstream
`WorkManager` boundary check, resolves `fundamentals/townhall1.blueprint` and
verifies that `WorkManager` assigns and returns the persistent order ID. A
companion fixture registers a real Colonial Builder, assigns a live citizen to
`JobBuilder`, and verifies that `BuildingBuilder.searchWorkOrder()` assigns the
order and persists the citizen claim. These are server-side work-order and
assignment contracts; they do not claim that Builder AI navigation, material
requests, block placement or BlockUI interaction are complete.

A dedicated C2S fixture also serializes `BuildRequestMessage` in `REPAIR` mode
and then `BuilderSelectWorkOrderMessage`, routes both through the real Fabric
split envelope and server executor, verifies the owner permission path with a
registered `SERVERBOUND` player, and checks that a live `JobBuilder` allows the
handlers to create/claim the Town Hall `WorkOrderBuilding` and assign it to the
citizen job. Builder GUI placement, material consumption and AI navigation
remain manual validation items.

A companion C2S fixture serializes `DirectPlaceMessage` in the same real split
envelope. It places a Town Hall for a registered owner, consumes the supplied
Town Hall item, preserves the selected default style and verifies the
asynchronous `fundamentals/townhall1.blueprint` resolution and split-cache
cleanup. This validates the server packet route; the client GUI and visual
placement workflow remain manual validation items.

A second companion C2S fixture serializes `DecorationBuildRequestMessage` in
the same real split envelope. It keeps the far-away blueprint footprint
resident while the asynchronous callback runs, then verifies the real
`WorkOrderDecoration` registration, `BUILD` type, Colonial style, blueprint
path, clockwise rotation, mirror flag, target level and split-cache cleanup.
This validates the server packet route; the decoration GUI and actual Builder
placement remain manual validation items.

A third companion C2S fixture serializes `HutRenameMessage` with a raw
server-bound building reference, routes it through the same split envelope and
server executor, resolves the Town Hall through `AbstractBuildingServerMessage`
and verifies the building custom name plus split-cache cleanup. The added
constructor is a server-bound transport aid; the normal client building-view
constructor remains unchanged. This validates the building GUI action's server
route while the actual BlockUI screen remains manual.

A fourth companion C2S fixture serializes `ColonyNameStyleMessage`,
`ColonyStructureStyleMessage` and `ColonyTextureStyleMessage` in three real
split envelopes. It verifies owner-permission dispatch, mutation of the live
colony's name, structure-pack and texture-style values, and cleanup of all
three split-cache entries. The colony banner/style GUI remains manual.

A fifth companion C2S fixture serializes `ToggleHousingMessage`,
`ToggleJobMessage` and `ColonyFlagChangeMessage` in real split envelopes. It
verifies manual housing and hiring allocation toggles, banner-pattern
serialization, owner-permission dispatch and cleanup of all three split-cache
entries. The Town Hall management GUI remains manual.

A sixth companion C2S fixture serializes `BuildingSetStyleMessage` with a raw
server-bound building reference. It resolves a real deconstructed Town Hall,
applies a loaded structure pack and verifies the building state plus split-cache
cleanup. The normal building-style BlockUI interaction remains manual.

A seventh companion C2S fixture serializes `FarmFieldRegistrationMessage`,
`FarmFieldUpdateSeedMessage` and `FarmFieldPlotResizeMessage` in three real
split envelopes. It creates a scarecrow-backed `FarmField`, registers it in
the colony, changes the seed to a carrot, resizes the east radius and verifies
all three cache entries are removed. The Farmer field-management GUI and crop
AI remain manual.

An eighth companion C2S fixture serializes `ChangeDeliveryPriorityMessage`
twice against a real Builder building. It increases and decreases the live
pickup priority through separate split envelopes, verifies the worker-module
gate and owner permission, and confirms both cache entries are removed. The
Builder logistics GUI remains manual.

A ninth companion C2S fixture serializes `AssignmentModeMessage` and
`AssignFieldMessage` against a real Farmer and `FarmField`. It enables manual
assignment, assigns the field, frees it again through separate split
envelopes, verifies the runtime module id and field ownership transitions, and
confirms all three cache entries are removed. The Farmer field-management GUI
remains manual.

A tenth companion C2S fixture serializes `TriggerSettingMessage` against a real
Farmer settings module. It round-trips a `BoolSetting`, disables fertilizer
requests through the server handler and confirms the split-cache entry is
removed. The generic building-settings GUI remains manual.

An eleventh companion C2S fixture serializes `ReactivateBuildingMessage` with a
real deactivated Builder hut. It routes the position-only request through the
server executor, re-registers the `BuildingBuilder`, removes the `deactivated`
tag and confirms that the split-cache entry is removed. The in-game
reactivation screen remains manual.

A twelfth companion C2S fixture serializes `MinerSetLevelMessage` against a
real Colonial Miner. It routes the selected level through the server executor,
persists the new `currentLevel` value in `MinerLevelManagementModule` and
confirms that the split-cache entry is removed. The miner level GUI and the
actual shaft-construction cycle remain manual.

A thirteenth companion C2S fixture serializes `GuardSetMinePosMessage` against
a real Guard Tower and Miner. It assigns the Miner as the patrol target,
clears the target through the message's no-position form and confirms both
split-cache entries are removed. The guard patrol GUI and navigation/combat
cycle remain manual. The server handler now honors the clear form instead of
re-reading the previous mine position.

A fourteenth companion C2S fixture serializes `MarkBuildingDirtyMessage` against
a real Town Hall. It clears the building's initial dirty state, routes the
empty-payload message through the server executor and verifies that the live
building becomes dirty again before confirming cache cleanup.

A fifteenth companion C2S fixture serializes `ToggleMoveInMessage` against a
real colony, disables and re-enables the move-in policy and confirms both
split-cache entries are removed.

A sixteenth companion C2S fixture serializes `PauseCitizenMessage` against a
real live citizen. It pauses and resumes the citizen through separate split
envelopes and confirms both cache entries are removed.

A seventeenth companion C2S fixture serializes `HireFireMessage` against a real
Farmer worker module. It hires and fires the same live citizen through separate
split envelopes, verifies the `JobFarmer` assignment transition and confirms
both cache entries are removed.

An eighteenth companion C2S fixture serializes `BuildingHiringModeMessage`
against the Farmer worker module. It switches the module from `DEFAULT` to
`AUTO` and back through separate split envelopes, verifying both live state
transitions and cache cleanup.

A nineteenth companion C2S fixture serializes `AdjustSkillCitizenMessage`
against a live creative citizen. It routes a Strength increment through the
real split envelope and server executor, verifies the owner/creative permission
path and the persisted skill-level mutation, and confirms split-cache cleanup.

A twentieth companion C2S fixture serializes `CourierHiringModeMessage` against
a real Warehouse courier-assignment module. It switches the module from
`DEFAULT` to `AUTO` and back through separate split envelopes, verifying both
live transitions and cache cleanup.

A twenty-first companion C2S fixture serializes `QuarryHiringModeMessage`
against a real Simple Quarry module. It uses the actual quarry block and
structure pack, switches the module from `DEFAULT` to `AUTO` and back, and
confirms the route and split-cache cleanup. The message's client-view
constructor now preserves the module runtime ID required by the server handler.

A twenty-second companion C2S fixture serializes `MinerRepairLevelMessage`
against a real Miner with a populated `MinerLevelManagementModule`. It routes
the repair request through the server executor, creates the expected shaft
`WorkOrderMiner` for the selected level and confirms split-cache cleanup.

A twenty-third companion C2S fixture serializes `WorkOrderChangeMessage` against
a real Town Hall work order. It updates the registered order's priority and
then removes the same order through separate split envelopes, verifying both
`WorkManager` mutations and cache cleanup.

A twenty-fourth companion C2S fixture serializes `AssignUnassignMessage` against
a real Colonial residence. It registers the level-one house blueprint, creates
a live citizen, assigns and then unassigns the citizen through separate split
envelopes, verifies the `LivingBuildingModule` membership and
`CitizenData.getHomeBuilding()` transitions, and confirms both cache entries
are removed. Automatic housing capture, sleep/navigation and the residence GUI
remain manual validation items.

A twenty-fifth companion C2S fixture serializes
`ChangeFreeToInteractBlockMessage` against a real colony owner. It adds and
removes a chest block and an absolute position through separate split envelopes,
verifies both `Colony` permission collections and confirms all four cache
entries are removed. The Town Hall permissions GUI and in-game protection
interaction remain manual validation items.

A twenty-sixth companion C2S fixture serializes `TransferItemsRequestMessage`
against a real Builder. It registers the level-one Builder blueprint, seeds the
non-creative owner's inventory with eight cobblestones, transfers the stack
through the server executor and verifies the Builder inventory receives all
items while the owner inventory is consumed and the split cache is cleared.
The inventory GUI, request resolution and Builder logistics AI remain manual
validation items.

A twenty-seventh companion C2S fixture serializes
`AddMinimumStockToBuildingModuleMessage` and
`RemoveMinimumStockFromBuildingModuleMessage` against a real Builder. It adds
three oak planks to the Builder's `MinimumStockModule`, removes the same entry
through its runtime module id and verifies both module transitions and split
cache cleanup. The minimum-stock GUI, request resolution and logistics AI
remain manual validation items.

A twenty-eighth companion C2S fixture serializes `AssignFilterableEntityMessage`
against a real Guard Tower. It adds and removes the `minecraft:zombie` entity
through the Guard Tower entity-list module, verifies both filter transitions
and confirms split-cache cleanup. The implementation now resolves the module
by runtime ID, matching the modern reference behavior; the filter GUI and
patrol/combat cycle remain manual validation items.

A twenty-ninth companion C2S fixture serializes `AssignFilterableItemMessage`
and `ResetFilterableItemMessage` against a real Smeltery. It adds, removes,
re-adds and resets an iron-ore filter through the Smeltery ore-list module,
verifies every transition and confirms split-cache cleanup. The item-filter
GUI and smelting/logistics cycle remain manual validation items.

A thirtieth companion C2S fixture serializes `GiveToolMessage` against a real
Town Hall. It gives the owner a compass, verifies it is selected in the hotbar
with the Town Hall position and colony ID in NBT, and confirms split-cache
cleanup. The client tool GUI and actual tool interaction remain manual
validation items.

A thirty-first companion C2S fixture serializes
`TransferItemsToCitizenRequestMessage` against a real live citizen. It moves
seven cobblestones from the non-creative owner's inventory to the citizen
inventory, verifies the source is consumed and confirms split-cache cleanup.
The citizen inventory GUI and worker logistics cycle remain manual validation
items.

A thirty-second companion C2S fixture serializes `RecallSingleCitizenMessage`
against a real Town Hall and live citizen. It moves the citizen away, recalls
it through the server executor, verifies the safe spawn point and persisted
last position, and confirms split-cache cleanup. Navigation and the Town Hall
citizen GUI remain manual validation items.

A thirty-third companion C2S fixture serializes `RecallCitizenMessage` and
`RecallCitizenHutMessage` against a real Builder and assigned live citizen. It
routes both worker and hut recall variants through the split envelope/server
executor, verifies the safe Builder spawn point, preserves the Builder
assignment and confirms both cache entries are cleaned. Citizen navigation and
the Builder GUI remain manual validation items.

A thirty-fourth companion C2S fixture serializes `AddRemoveRecipeMessage`,
`ChangeRecipePriorityMessage`, `ToggleRecipeMessage` and
`OpenCraftingGUIMessage` against a real Stone Smeltery. It adds two compatible
recipes, moves the second recipe up, disables and re-enables the selected
recipe, opens the furnace crafting menu and confirms every split-cache entry
is cleaned. The recipe GUI rendering and actual worker crafting cycle remain
manual validation items.

A thirty-fifth companion C2S fixture serializes `ToggleHelpMessage`,
`TeamColonyColorChangeMessage` and `HireSpiesMessage` against a real colony.
It toggles progress notifications, applies the `AQUA` team color, consumes
five gold ingots and enables spies through separate split envelopes, then
confirms every cache entry is cleaned. The Town Hall control GUI and raid/spy
gameplay cycle remain manual validation items.

The residence fixture registers a real Colonial house through the same
`BuildingEntry` and `TileEntityColonyBuilding` path used by gameplay, resolves
the level-one house blueprint, assigns a live citizen through
`LivingBuildingModule` and verifies that both the module and `CitizenData`
retain the home relationship. Automatic housing capture during colony ticks,
citizen sleep/navigation and the residence GUI remain manual validation items.

The warehouse fixture registers Colonial warehouse and courier buildings from
their real `TileEntityColonyBuilding` entries, resolves the level-one storage
blueprints, assigns a live `JobDeliveryman` citizen to the courier and
`CourierAssignmentModule`, and verifies warehouse access plus the
`WarehouseRequestResolver`, `DeliveryRequestResolver` and
`PickupRequestResolver` registrations. This validates the server-side
logistics wiring; request creation/resolution, inventory transfer, courier
navigation and the client logistics GUI remain open.

The miner fixture follows the same real building-registration path for a
Colonial miner, resolves `fundamentals/mine1.blueprint`, assigns a live citizen
to `JobMiner`, creates a persistent `WorkOrderMiner` through the colony
`WorkManager` and verifies that `BuildingMiner.searchWorkOrder()` selects the
order and persists the citizen claim. Mine-shaft construction, ore/mining AI,
placement, resource delivery and the miner GUI remain open.

The farmer fixture registers a Colonial farmer through its real building entry,
resolves `agriculture/horticulture/farm1.blueprint`, creates a scarecrow-backed
`FarmField` with a wheat seed and assigns a live citizen to `JobFarmer`. The
`FarmerFieldsModule` then retains the field and its owning building ID. Crop
growth/harvesting AI, field placement, resource delivery and the farming GUI
remain manual validation items.

The guard fixture registers a Colonial Guard Tower through its real building
entry, resolves `military/guardtower1.blueprint`, verifies the default guard
position and patrol task, locates the knight `GuardBuildingModule` and assigns
a live citizen to `JobKnight`. The building then exposes that citizen through
its guard roster. Guard navigation/combat AI, equipment provisioning, raids
and combat outcomes remain manual validation items.

The raid fixture raises a real colony above `RaidManager.MIN_REQUIRED_RAIDLEVEL`
with live citizens, verifies the forced barbarian `IColonyRaidEvent` and its
calculated spawn point, then disables colony raid events and confirms new raid
requests return `CANNOT_RAID`. This covers manager eligibility and event
registration; raider spawning, navigation, guard response and combat outcomes
remain manual validation items.

The research-cycle fixture selects `minecolonies:civilian/ambition` through the
real local research tree with a non-creative player, consumes its one-diamond
cost, advances the configured branch progress, records completion and verifies
the `minecolonies:effects/blockhutmysticalsite` effect. The same fixture now
registers a real Colonial University, assigns a live citizen to `JobResearch`
and drives the configured progress through repeated `BuildingUniversity`
server-side worker ticks. Researcher AI walking/mana behavior, work-speed
scaling and the client GUI remain manual validation items.

The same shutdown pass exposed that colony serialization can run after the
world reference has been detached. `AbstractSchematicProvider.getRotation()`
now returns the neutral runtime rotation in that serialization-only state,
keeping save/stop clean without dropping the persisted colony payload.

Structurize's server pack loader is connected to Fabric's
`SERVER_STARTING` callback. Its mod-resource scan also descends through the
`blueprints/minecolonies/<style>` layout used by the official MineColonies
styles, so the Colonial and Original packs are available before gameplay
looks up a blueprint. The explosion-protection GameTest also assigns the real
`Colonial/fundamentals/townhall1.blueprint` to its Town Hall before registering
the building; the latest 66-test run therefore resolves the structure without
Structurize directory-read or rotation errors. Client-bound network messages used during join and chunk
claim now keep their common codecs server-loadable and delegate visual work to
the client bridge by reflection; their upstream message IDs remain stable.

The same Fabric server-data reload builds the default research tree through the
retained `ResearchListener`. The latest 33-test core GameTest batch resolves all four
default branches, representative research IDs using their full branch paths,
branch metadata and the citizen-cap effect through `IGlobalResearchTree`; its
research-cycle fixture also covers item-cost consumption, progress completion,
effect application and the real University worker-tick path.

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

The C2S side now uses the same logical-origin convention as the Forge-shaped
envelope: a packet received by the server is marked as originating on the
client, while a packet received by the client is marked as originating on the
server. This matters because `SplitPacketMessage` rejects an inner message
whose execution side does not match its destination. A focused GameTest
serializes `TownHallRenameMessage`, `CreateColonyMessage`, `TryResearchMessage`,
`BuildRequestMessage`, `BuilderSelectWorkOrderMessage`, `DirectPlaceMessage`,
`DecorationBuildRequestMessage`, `HutRenameMessage`, `ColonyNameStyleMessage`,
`ColonyStructureStyleMessage`, `ColonyTextureStyleMessage`,
`ToggleHousingMessage`, `ToggleJobMessage`, `ColonyFlagChangeMessage`,
`ChangeDeliveryPriorityMessage`, `AssignmentModeMessage`, `AssignFieldMessage`,
`TriggerSettingMessage` and `BuildingSetStyleMessage`,
`ReactivateBuildingMessage`,
`MinerSetLevelMessage`,
`GuardSetMinePosMessage`,
`MarkBuildingDirtyMessage`,
`ToggleMoveInMessage`,
`PauseCitizenMessage`,
`HireFireMessage`,
`BuildingHiringModeMessage`,
`AdjustSkillCitizenMessage`,
`CourierHiringModeMessage`,
`QuarryHiringModeMessage`,
`MinerRepairLevelMessage`,
`WorkOrderChangeMessage`,
`AssignUnassignMessage`,
`ChangeFreeToInteractBlockMessage`,
`TransferItemsRequestMessage`,
`AddMinimumStockToBuildingModuleMessage`,
`RemoveMinimumStockFromBuildingModuleMessage`,
`AssignFilterableEntityMessage`,
`AssignFilterableItemMessage`,
`ResetFilterableItemMessage`,
`GiveToolMessage`,
`TransferItemsToCitizenRequestMessage`,
`RecallSingleCitizenMessage`,
`RecallCitizenMessage` and `RecallCitizenHutMessage`,
`AddRemoveRecipeMessage`,
`ChangeRecipePriorityMessage`,
`ToggleRecipeMessage` and `OpenCraftingGUIMessage`,
`ToggleHelpMessage`,
`TeamColonyColorChangeMessage` and `HireSpiesMessage`,
`FarmFieldRegistrationMessage`,
`FarmFieldUpdateSeedMessage` and `FarmFieldPlotResizeMessage`,
wraps each in the
real split envelope, sends them
through `SimpleChannel`'s server dispatch boundary and verifies the deferred
server-executor actions, owner permission, colony-name mutation, Town Hall
foundation with its requested pack/blueprint, non-creative research cost and
in-progress state, Town Hall repair-work-order creation/claiming by the
requested Builder, Builder work-order selection for the citizen job, direct
Town Hall placement with item consumption and blueprint resolution, and
decoration work-order creation with asynchronous blueprint resolution and
rotation/mirror preservation, building custom-name mutation, colony-style and
colony-management updates, Builder delivery-priority mutation, Farmer field
assignment-mode/ownership mutation and Farmer settings mutation,
deconstructed-building style mutation, Farmer hiring-mode transition,
creative citizen Strength adjustment, Courier hiring-mode transition and
Quarry hiring-mode transition and Miner level repair work-order creation. The
work-order priority and removal transitions, residence assignment and
unassignment, free-interaction block/position permission changes, Builder
inventory transfer, minimum-stock module updates, Guard Tower entity-filter
updates, Smeltery item-filter add/remove/reset, GiveTool inventory binding,
citizen inventory transfer, single-citizen recall, assigned-citizen
worker/hut recall, Stone Smeltery recipe add/reorder/toggle and crafting-menu
opening, colony color/help and spy-hiring control routes are also covered. The
far-away colony fixtures use disjoint coordinate bands so concurrent GameTest
execution does not depend on whichever fixture claims the first candidate.
The research,
colony-foundation and Builder fixtures
register the test player with a real `SERVERBOUND` connection so normal
colony-view packets are sent during setup. Evidence for the complete run is in
`logs/minecolonies-gametest-c2s-colony-control.log`; it reports
`All 66 required tests passed` (65 core tests plus one entity batch test).

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
read for compatibility. The 13-test GameTest batch verifies the synced
round-trip in `logs/minecolonies-gametest-fishing-hook.log`.

The entity DataFixer audit is deliberately closed as a compatibility policy,
not as an unverified TODO. The official Forge 1.20.1 entity initializer builds
the same custom entity types without MineColonies DataFixer schemas, and the
modern Fabric reference does not introduce them either. Minecraft therefore
logs one `No data fixer registered` message per custom entity at bootstrap;
this port preserves upstream behavior and does not invent migration schemas.
Old entity data that needs a version migration remains outside the supported
migration guarantee until upstream defines those schemas.

The Fabric GameTest registry fixture now enumerates the target registry and
verifies all 25 MineColonies custom entity types can be constructed, retain
their registered type identity and exercise an NBT reload. The two custom
projectile types whose upstream `save` implementation deliberately returns
`false` are expected to discard during reload; all other registered types must
retain their position. This closes the registration/constructor/persistence
contract for the complete current entity list without claiming that hostile AI,
citizen work AI, combat, restart migration or client rendering have been
exercised.

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
