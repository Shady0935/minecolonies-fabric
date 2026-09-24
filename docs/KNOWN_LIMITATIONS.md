# Known limitations

These are confirmed observations, not guesses. Incomplete gameplay coverage
is also tracked in `PORTING_STATUS.md` and `TEST_MATRIX.md`.

## Confirmed technical limitations

- MineColonies entity types currently have no registered Minecraft DataFixer
  schemas in the official Forge 1.20.1 source or the modern Fabric reference.
  Client/server startup therefore reports `No data fixer registered for
  minecolonies:<entity>` for the custom entities. The Fabric port preserves
  that upstream behavior rather than inventing migration schemas; existing
  saves that require entity-version migration are not covered yet.
- Fabric datagen is reproducible for the full registered provider set: 37
  providers generate 3,576 resources, including 967 JSON files, 2,609
  citizen/raider icons and the native MineColonies plus conventional `c:`
  tags. The previous upstream generated snapshot remains only as an audit
  baseline and is not a runtime resource root.
- Entity icons are generated in the dedicated-server datagen environment with
  headless Java2D/ImageIO rather than Minecraft's client-only `NativeImage`.
  The implementation preserves the upstream 8x8 head crop, 16x16 output and
  darkened border; byte-for-byte PNG identity with the Forge NativeImage
  implementation is not promised.
- JEI and JourneyMap integrations are excluded from the target source set.
- The command/entity smoke test summons a citizen without a colony only to
  exercise registration, entity construction and serialization. The upstream
  `CitizenColonyHandler` intentionally removes that isolated citizen on the
  next load, so this does not validate colony-backed citizen persistence.
- The Fabric GameTest now covers server-side Colonial Town Hall creation,
  ownership permissions, blueprint lookup, the real C2S `BuildRequestMessage`
  repair and `BuilderSelectWorkOrderMessage` selection routes, the real C2S
  `DirectPlaceMessage` Town Hall placement route with item consumption and
  blueprint resolution, and the real C2S `DecorationBuildRequestMessage`
  route with asynchronous blueprint resolution and decoration work-order
  registration, the real C2S `PlantationFieldBuildRequestMessage` route with
  asynchronous plantation work-order creation/removal, and the real C2S
  rally-banner toggle/removal routes with serialized Guard Tower locations;
  the real C2S `HutRenameMessage` building-name mutation route,
  and the real C2S colony name/structure/texture style mutation routes,
  plus the real C2S deconstructed-building style route,
  and the real C2S FarmField registration, seed and radius routes,
  builder
  work-order registration, live `JobBuilder` assignment and work-order claim
  selection, colony NBT
  round-trip, residence registration with a citizen-home relationship, and
  miner work-order registration with live `JobMiner` claim selection, plus
  farmer field registration with live `JobFarmer` assignment and Guard Tower
  registration with live `JobKnight` assignment and guard-roster persistence,
  plus a focused real `EntityAIKnight`/`KnightCombatAI` autonomous
  nearby-target-search/hostile-hit cycle and registered-chest sword retrieval
  path, and
  `RaidManager` eligibility/barbarian-event registration and the real horde
  spawn plus event-lifecycle transition, including the short-range registered
  raider `AttackMoveAI` target-selection/damage path against a live citizen,
  the `RaiderWalkAI` direct-target fallback when waypoints are empty and a
  real guard-versus-barbarian target-selection/damage path.
  It also covers server-side warehouse/courier registration, live
  `JobDeliveryman` assignment, warehouse access and the three logistics
  resolver registrations.
  A focused Crusher fixture also loads a real custom cobblestone-to-gravel
  recipe, assigns `JobCrusher` and verifies `EntityAIWorkCrusher` input
  consumption, gravel output and daily-limit accounting.
  A focused Stonemason fixture also loads the custom cobblestone-and-sand-to-
  sandstone recipe, assigns `JobStonemason`/`EntityAIWorkStonemason`, resolves
  a building-origin request through the private worker resolver and verifies
  exact input consumption plus sandstone output. Asynchronous Stonemason
  scheduling, GUI interaction and broader delivery/work-cycle behavior remain
  uncovered.
  A focused Stone Smeltery fixture registers a placed vanilla Furnace through
  the `BlockState` overload, assigns the live smeltery worker/AI, runs a
  cobblestone-to-stone furnace request with deterministic fuel and verifies
  vanilla output retrieval/delivery. GUI rendering, longer asynchronous
  scheduling and broader delivery/work-cycle behavior remain uncovered.
  A companion Smeltery fixture registers a vanilla Furnace, assigns
  `JobSmelter`/`EntityAIWorkSmelter`, follows the normal public parent/child
  request chain through `PublicWorkerCraftingProductionResolver` and verifies
  raw-iron input, deterministic fuel consumption and iron-ingot retrieval.
  GUI rendering, longer scheduling and broader delivery behavior remain
  uncovered.
  A companion Cook fixture registers a vanilla Furnace, assigns
  `JobCook`/`EntityAIWorkCook`, seeds a beef request with deterministic
  dried-kelp-block fuel and verifies raw-food/fuel consumption plus cooked-beef
  retrieval. GUI rendering, broader recipe selection and longer scheduling
  remain uncovered.
  A companion Baker fixture registers a vanilla Furnace, assigns
  `JobBaker`/`EntityAIWorkBaker`, loads the generated bread furnace recipe,
  seeds bread dough with deterministic dried-kelp-block fuel and verifies
  raw-input/fuel consumption plus bread retrieval. GUI rendering, broader
  recipe selection and longer scheduling remain uncovered.
  A companion Cook Assistant fixture raises the Cook to level three, assigns
  `JobCookAssistant`/`EntityAIWorkCookAssistant`, verifies runtime vanilla
  furnace-recipe routing through the public worker resolver, consumes beef and
  deterministic fuel, observes `isCooking` and verifies cooked-beef delivery.
  GUI rendering, broader recipe selection and longer scheduling remain
  uncovered.
  A complete registry fixture instantiates all 25 current MineColonies custom
  entity types and exercises their NBT reload behavior. The two upstream
  non-persistent projectile types are expected to discard during reload; this
  does not replace the rendering or restart-migration checks.
  It does not yet cover complex multi-request scheduling across varied sources
  and destinations or broad logistics beyond the focused two-material Builder
  work-order cycle (rack → two Stack/Delivery requests → Courier → Builder), larger
  canonical construction or full logistics-backed builds,
  full CitizenAI scheduling/work-cycle
  construction, ore/mining AI and resource delivery, farmer crop growth, field
  placement and longer/multi-cell harvest navigation/resource delivery beyond
  the focused worker-AI and short-range navigation cycles, a broader
  Lumberjack autonomous tree-search/navigation cycle,
  tree navigation and resource delivery, or a
  client opening the Town Hall GUI, citizen rendering/full work-cycle
  navigation, long-range hostile-entity target search/navigation, combat and
  rendering after restart, or DataFixer migration for old entity data. A
  dedicated restart probe does
  confirm that saved `EntityCitizen` and `VisitorCitizen` instances reappear
  with colony/citizen NBT. A separate normal dedicated-server probe also
  launches the same saved world twice and confirms clean three-dimension
  saves; it does not replace the pending authenticated gameplay pass.
- The server-side research manager and University building now have an
  automated selection/progression fixture: a non-creative player pays the
  datapack cost, a real University receives a live `JobResearch` citizen, its
  configured worker ticks complete the research and its effect is applied. A
  focused companion also runs `EntityAIWorkResearcher` from an offset position
  to a registered bookshelf, consumes stored mana and advances research through
  normal `CitizenAI` ticks. Long-range bookshelf selection/navigation,
  work-speed scaling, University GUI interaction and the broader set of client
  research packets remain uncovered.
- The Lumberjack worker cycle is now covered with a valid three-log tree,
  normal citizen ticks and a level-compatible stone axe. The fixture uses the
  `Minecolonies Original` pack because the Colonial 1.20.1 pack lacks the
  level-one Lumberjack blueprint. A focused companion also verifies the real
  vanilla oak-sapling replant route on valid dirt and exact inventory
  consumption; the same fixture confirms vanilla leaves select shears through
  the Fabric block-tag adaptation. A companion two-stump fixture verifies the
  planting guard consumes one available sapling once without a free second
  planting. Full autonomous tree search/pathfinding, broader tree navigation,
  resource delivery and GUI interaction remain unverified.
- Fabric 1.20.1 has no direct callback equivalent for several retained Forge
  event points. Narrow adapters now cover farmland trampling and item
  toss/pickup, while the bucket-use callback posts the retained `FillBucketEvent`
  before vanilla fills the bucket; the focused server fixture verifies the
  cancellation semantics of the three mixin-backed paths. Natural and
  chunk-generation hostile spawn position checks are now covered at
  `NaturalSpawner`, living-entity explosion damage follows the configured
  colony policy through `ALLOW_DAMAGE`, and a narrow `Explosion` mixin now
  preserves `ExplosionEvent.Start` cancellation with the real vanilla
  position. The same mixin publishes `ExplosionEvent.Detonate` immediately
  before vanilla's damage loop with the live block and entity lists, so the
  colony policy filters protected blocks and non-living entities at the
  correct point. No broad no-op adapter is used for the covered explosion
  paths.
- The retained bow adapter now dispatches the result-bearing `ArrowNockEvent`
  and `ArrowLooseEvent`. The no-listener nock path follows Forge's neutral
  `null` contract, listeners can supply an alternate action or cancel with a
  failure result, and the Pharao Scepter flow is GameTest-covered.
- Projectile impacts now dispatch a cancellable `ProjectileImpactEvent` with
  the original hit result; the focused bridge is covered. Fishing loot now
  dispatches `ItemFishedEvent` for MineColonies' custom `NewBobberEntity`; the
  vanilla `FishingHook` remains outside this mod's custom hook surface.
- Mob conversion now has a real bridge through Fabric's pre-spawn
  `MOB_CONVERSION` callback. The automated fixture constructs a populated
  tavern, creates one `VisitorCitizen`, assigns it to that tavern and confirms
  that the unrelated vanilla candidate is discarded. Broader conversion types
  still depend on the retained event coverage listed above.
- The retained block-place event is emitted as a pre-vanilla placement
  preflight because Fabric has no matching post-placement event. The bridge
  validates `BlockPlaceContext` and prevents placement when the retained event
  denies it, but it is not a byte-for-byte replacement for Forge's post-place
  timing.

## Validation still pending

- Authenticated in-game interaction with BlockUI screens (including the
  MultiPiston configuration window and an actual rendered entity/block hook
  overlay), larger canonical Builder work-order pathfinding/placement, fully
  complex multi-source/destination Courier scheduling and broad stock logistics, full citizen work
  cycles/scheduling,
  miner shaft construction and mining placement/resource logistics beyond the
  covered stone and vanilla-ore AI cycles,
  farmer crop growth/field placement and longer harvest navigation/resource
  delivery beyond the covered short-range route (the direct empty-field hoeing
  and hoed-field seed planting paths are covered), broader Lumberjack tree
  search/navigation, long-range University researcher
  AI navigation/work-speed and GUI,
  full guard navigation and long-running ambient target search beyond the
  covered short-range barbarian response, and broader request-driven guard
  equipment (the current checkpoint covers one automatically reassigned
  late-arriving sword request from a registered storage chest and its normal
  pickup path) still need a real
  in-game interaction pass. Controlled guard and raid combat now have automated
  GameTest coverage. The focused server-side
  CitizenAI/Builder worker tick cycle, colony-backed advanced navigation
  including a solid-barrier detour, and the Builder construction-site navigation
  proxy plus a live assigned Builder `BUILDING_STEP` placement/consumption
  cycle and automatic order claim are covered by GameTest. A separate live
  work-order fixture also navigates to and completes a synthetic 17-block
  blueprint with stone and cobblestone requested from the Warehouse rack through
  separate normal Stack/Delivery requests while the assigned Courier autonomously
  navigates from the rack to the Builder through its `CitizenAI`, then consumed
  by the Builder. Larger canonical builds, complex multi-source routes and longer
  post-selection work cycles remain pending. Server-side
  Town Hall, direct and client-to-server colony creation, builder work-order registration and live
  `JobBuilder` claim selection, colony-backed citizen registration, research
  manager selection/progression, residence registration/home assignment,
  warehouse/courier resolver wiring, the focused deterministic Builder
  material-request/pickup and one-block placement path through the real worker
  AI state machine, including the live assigned-order `BUILDING_STEP` route
  (full CitizenAI navigation/scheduling is still pending),
  Miner work-order/claim selection,
  Farmer field assignment plus focused short-range harvest/navigation,
  Lumberjack registration/assignment, focused three-log worker/replant cycle and
  the two-stump planting guard, Guard
  Tower/knight assignment plus the focused
  knight autonomous target-selection/hostile-hit path, RaidManager event
  registration/spawn lifecycle and the University
  worker-tick plus focused researcher study/mana paths have automated GameTest
  fixtures; multi-request logistics,
  full Builder work-order navigation and the dedicated restart
  probe confirms representative citizen-entity reappearance but is not a full
  gameplay-cycle test.
- Client-to-server gameplay packets have not been driven through every GUI or
  block interaction. Real `TownHallRenameMessage`, `CreateColonyMessage`,
  `BuildRequestMessage`, `BuilderSelectWorkOrderMessage`,
  `DirectPlaceMessage`, `DecorationBuildRequestMessage`, `HutRenameMessage`,
  `PlantationFieldBuildRequestMessage`, `ToggleBannerRallyGuardsMessage` and
  `RemoveFromRallyingListMessage`,
  the three colony style messages, `BuildingSetStyleMessage`, the three
  FarmField messages, `RestartCitizenMessage`,
  `ResourceScrollSaveWarehouseSnapshotMessage`,
  `SwitchBuildingWithToolMessage` and `TryResearchMessage` actions now cross the Fabric
  split envelope, reach the server executor and mutate the owner
  colony/work-order/research state or place the Town Hall and resolve its
  blueprint;
  the common codecs for client-bound colony, particle/audio, pathfinding,
  build-window, scan, global-quest, research-tree, custom-recipe-manager and
  compatibility messages are now server-load safe. Four opaque synchronization
  payloads preserve their bytes through repeated serialization, but visual and
  client behavior still needs an in-game interaction pass.
  The transport also has a login/server-to-client smoke pass, including
  `ServerUUIDMessage`; the seven registered MineColonies menu types also have
  automated extended-opening-buffer forwarding coverage. GUI rendering and
  interaction are still pending.
- The MineColonies spear now has a lifecycle-safe Fabric built-in renderer
  registration, and client bootstrap reaches resource reload and OpenAL without
  the previous `EntityModelSet` initialization crash. No automated screenshot
  or in-game inventory/GUI assertion currently verifies the rendered appearance.
- The new interaction/combat callback bridge has been loaded by dedicated
  server and client bootstrap. The focused fixture now drives the server-side
  Town Hall protection callback for an owner and an unauthorized player;
  client GUI actions and the full gameplay loop remain pending.
- A valid authenticated profile was not available in the development client;
  the network smoke test consequently used a temporary offline server setting.
  The checked-in/test workspace configuration was restored to
  `online-mode=true` and `enforce-secure-profile=true`.

## Non-blocking warnings observed

- Vanilla goat-horn sound events still warn as missing in the development
  asset set.
- A small number of non-power-of-two textures reduce their mip level, and one
  vanilla emissive shader reports an unused sampler. Neither caused the client
  bootstrap to fail.
- The latest combined 114-test GameTest run still logs the upstream-style warning that
  `fundamentals/townhall1.blueprint` has an incorrect `Primary Offset` when a
  fixture registers a Town Hall. The tested server routes still complete and
  the suite passes; the latest evidence is in
  `logs/minecolonies-gametest-builder-two-materials-20260923.log`. The
  blueprint data should be audited before treating this warning as resolved.
- The latest clean combined 114-case suite hardens only the transient Raider combat
  fixture state before starting the real `RaiderMeleeAI`/`AttackMoveAI` path
  and records 114/114 required tests passed, including the three focused
  MultiPiston tests. Long-range and fully in-game raid
  behavior, including client interaction, remain manual validation items.

## Intentional compatibility residues

The source still contains a narrow Forge-shaped compatibility layer because
the 1.20.1 gameplay code uses those types. The `forge` worldgen tag namespace
is also intentional and backed by Fabric-loaded tag files. These are tracked
bridges, not evidence that the runtime still depends on Forge.
