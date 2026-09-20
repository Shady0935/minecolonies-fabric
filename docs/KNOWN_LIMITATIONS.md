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
- The standalone MultiPiston Forge dependency is not included in the Fabric
  runtime. The current MineColonies main source has no direct runtime reference
  to it, but its independent block/content remains unported.
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
  nearby-target-search/hostile-hit cycle and
  `RaidManager` eligibility/barbarian-event registration.
  It also covers server-side warehouse/courier registration, live
  `JobDeliveryman` assignment, warehouse access and the three logistics
  resolver registrations.
  A complete registry fixture instantiates all 25 current MineColonies custom
  entity types and exercises their NBT reload behavior. The two upstream
  non-persistent projectile types are expected to discard during reload; this
  does not replace the pending combat, rendering or restart-migration
  checks.
  It does not yet cover multi-request resolution/delivery, automatic logistics
  inventory transfer beyond the focused rack-backed cycle, courier navigation,
  full Builder work-order pathfinding and
  larger construction, full CitizenAI scheduling/work-cycle
  construction, ore/mining AI and resource delivery, farmer crop growth, field
  placement and longer harvest navigation/resource delivery beyond the focused
  worker-AI cycle, a Lumberjack autonomous tree-search/replanting cycle,
  tree navigation and resource delivery, or a
  client opening the Town Hall GUI, citizen rendering/full work-cycle
  navigation, full hostile-entity target search/navigation, sustained
  AI/combat/rendering after restart, or DataFixer migration for old entity data. A
  dedicated restart probe does
  confirm that saved `EntityCitizen` and `VisitorCitizen` instances reappear
  with colony/citizen NBT.
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
  level-one Lumberjack blueprint. Full autonomous tree search/pathfinding,
  replanting, resource delivery and GUI interaction remain unverified.
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

- Authenticated in-game interaction with BlockUI screens (including an actual
  rendered entity/block hook overlay), full Builder work-order pathfinding/larger
  placement, full citizen work cycles/scheduling, logistics,
  miner shaft/ore behavior and mining AI,
  farmer crop growth/field placement and longer harvest navigation/resource
  delivery, Lumberjack tree search/replanting, long-range University researcher
  AI navigation/work-speed and GUI,
  full guard target search/navigation,
  request-driven guard equipment, and raids
  still need a real in-game interaction pass. The focused server-side
  CitizenAI/Builder worker tick cycle, colony-backed advanced navigation
  including a solid-barrier detour, and the Builder construction-site navigation
  proxy plus a live assigned Builder `BUILDING_STEP` placement/consumption
  cycle and automatic order claim are covered by GameTest, but full work-order
  pathfinding, larger construction and longer post-selection work cycles remain
  pending. Server-side
  Town Hall, direct and client-to-server colony creation, builder work-order registration and live
  `JobBuilder` claim selection, colony-backed citizen registration, research
  manager selection/progression, residence registration/home assignment,
  warehouse/courier resolver wiring, the focused deterministic Builder
  material-request/pickup and one-block placement path through the real worker
  AI state machine, including the live assigned-order `BUILDING_STEP` route
  (full CitizenAI navigation/scheduling is still pending),
  Miner work-order/claim selection,
  Farmer field assignment, Lumberjack registration/assignment and focused
  three-log worker cycle, Guard Tower/knight assignment plus the focused
  knight autonomous target-selection/hostile-hit path, RaidManager event registration and the University
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
  the common codecs for the client-bound colony, particle,
  audio, pathfinding, build-window and scan messages are now server-load safe,
  but their visual/client behavior still needs an in-game interaction pass.
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
- The latest 89-test GameTest run still logs the upstream-style warning that
  `fundamentals/townhall1.blueprint` has an incorrect `Primary Offset` when a
  fixture registers a Town Hall. The tested server routes still complete and
  the suite passes; the evidence is in
  `logs/minecolonies-gametest-researcher-green-89.log`. The
  blueprint data should be audited before treating this warning as resolved.

## Intentional compatibility residues

The source still contains a narrow Forge-shaped compatibility layer because
the 1.20.1 gameplay code uses those types. The `forge` worldgen tag namespace
is also intentional and backed by Fabric-loaded tag files. These are tracked
bridges, not evidence that the runtime still depends on Forge.
