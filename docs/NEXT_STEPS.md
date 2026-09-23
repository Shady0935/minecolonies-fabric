# Next steps

The datagen, lifecycle, gameplay-callback, mob-conversion, first server-side
colony fixture, owned-colony deletion, colony teleport permission, mercenary hiring, citizen interaction response/close, GUI button advancement trigger, GUI open advancement trigger, recipe-teaching transfer, builder work-order/assignment, residence assignment and
unassignment,
warehouse/courier resolver wiring, Miner work-order/assignment, Farmer field
assignment, Guard Tower/knight assignment, RaidManager eligibility/event
registration, research-manager, building-rename, building-reactivation,
Miner-level selection, Guard Tower patrol-mine assignment, building dirty
marking, move-in policy, citizen pause/resume, Farmer hire/fire and hiring-mode
transitions, creative citizen skill adjustment, Courier hiring-mode and Quarry
hiring-mode transitions, Miner level repair and
work-order priority/removal, residence assignment/unassignment, free-interaction
permission changes, Builder inventory transfer, minimum-stock module updates,
Guard Tower entity-filter updates, Smeltery item-filter updates, GiveTool
inventory binding, citizen-inventory transfer, single-citizen recall,
assigned-citizen worker/hut recall, Stone Smeltery recipe/menu routes,
colony color/help and spy-hiring routes, citizen-inventory menu opening,
request-state updates, building pickup, force pickup, warehouse sort/upgrade,
Enchanter station assignment, Postbox request, citizen-restart scheduling,
resource-scroll warehouse snapshot and build-tool inventory routes, and
University worker-tick and focused researcher study/mana checkpoints are now
complete:
`runDatagen` is reproducible with
all 37 providers, native advancements/icons/quests are wired to the Fabric
entrypoint, the generated resource set is the runtime source of truth, retained
lifecycle/event handlers receive real Fabric callbacks, structure packs load
through Fabric server lifecycle, and the populated-tavern conversion creates a
real visitor entity. The latest clean combined Fabric GameTest run passes all
113 required tests, including the three standalone MultiPiston tests, and covers
construction and NBT reload behavior for all 25
currently registered custom entity types (including the two intentional
non-persistent projectile cases), and verifies Forge event priority plus
inherited listener dispatch. Focused follow-ups also pass the real
`ColonyDeleteOwnMessage` and `TeleportToColonyMessage` routes, verifying full
owner-colony cleanup and neutral/friend teleport permission; the focused
`HireMercenaryMessage` route also creates a real colony-linked mercenary group,
and the focused citizen interaction route delivers both response and close
callbacks to a live handler.
The focused GUI button route also completes the generated `check_out_guide`
advancement through its real server handler.
The focused GUI-open route also reaches the registered `open_gui_window` trigger
with a matching dynamic criterion; the generated resources currently contain no
shipped consumer for that trigger.
The focused recipe-teaching routes also reach the server-side furnace and
crafting menus through real split envelopes, update the furnace input and both
2x2/3x3 crafting shapes, and clear the packet cache; JEI's optional client
transfer remains pending.
Its 91-test core batch plus isolated Lumberjack, Farmer hoe/navigation/planting,
Miner, Miner shaft, housing, Builder, Guard, sleep/wake and entity batches also exercise real
client-to-server Town Hall rename, colony foundation, direct Town Hall
placement, hut/building rename, colony style settings, colony allocation/flag
settings, Builder delivery priority, Farmer field assignment/settings,
building reactivation, Miner level selection, Guard Tower patrol-mine assignment/clearing,
building dirty marking, colony move-in policy, citizen pause/resume, Farmer hire/fire and hiring-mode transitions,
creative citizen skill adjustment,
Courier and Quarry hiring-mode transitions,
Miner level repair work-order creation,
work-order priority/removal,
residence assignment/unassignment,
free-interaction block/position permission changes,
Builder inventory transfer,
minimum-stock module add/remove,
Guard Tower entity-filter add/remove,
Smeltery item-filter add/remove/reset,
GiveTool inventory binding,
citizen inventory transfer,
single-citizen recall,
assigned-citizen worker/hut recall,
Stone Smeltery recipe add/reorder/toggle and crafting-menu opening,
colony color/help and spy-hiring control routes,
citizen-inventory menu opening,
request-state updates,
building pickup,
force pickup,
warehouse sort/upgrade,
Enchanter station assignment,
Postbox request creation,
citizen-restart scheduling,
resource-scroll warehouse snapshot updates,
build-tool inventory swapping,
plantation-field work-order creation/removal,
rally-banner activation/deactivation/removal,
deconstructed-building style,
FarmField configuration,
Builder work-order creation/selection,
decoration work-order creation, plantation/rally routes and University research actions,
plus focused Farmer harvest/hoe/planting, Lumberjack three-log chopping, vanilla-leaf
shears selection, oak-sapling replanting and the two-stump planting guard,
short-range
Researcher bookshelf navigation/study/mana, short-range Farmer
harvest/navigation and Guard autonomous-target-search/combat cycles with an
automatic late-arriving registered-chest sword request and normal pickup, plus the
registered-raider `AttackMoveAI` short-range target/damage path, the
`RaiderWalkAI` direct-target fallback when waypoints are empty, a real
`JobKnight` response against a registered MineColonies barbarian raider and
controlled guard/raid combat fixtures. Full entity work cycles,
sustained guard navigation/ambient target search, broader request-driven
equipment provisioning and client rendering are still open. A fresh normal
dedicated-server restart probe also passes on the same saved world; evidence is
in `logs/minecolonies-dedicated-normal-restart-20260922.log`. The Crusher's
custom-recipe worker cycle, the Stonemason building-request/custom-recipe
  cycle, the Stone Smeltery furnace worker cycle, the Smeltery raw-ore
  furnace worker cycle, the Cook furnace worker cycle, the Baker furnace
  worker cycle and the level-three Cook Assistant runtime-furnace worker cycle
  are now covered by
  the latest checkpoint. The Stonemason fixture also
  confirms the private resolver's synchronous completion when its worker already
  holds the cobblestone and sand inputs; asynchronous scheduling and the GUI
  remain open.
The following work remains, in order:

1. Run a real gameplay pass with an authenticated client or a repeatable
   offline test profile: place the supply camp/Town Hall, create a colony,
   open each BlockUI screen, and exercise a builder, citizen, farmer, miner,
   Guard Tower, warehouse and research cycle. The server-side research manager and
   University worker-tick and focused researcher bookshelf navigation/study/mana
   paths are automated, so the remaining research work here is long-range researcher
   AI/walking, work-speed scaling and the GUI cycle.
   Include an inventory/GUI visual check for the custom spear renderer.
2. Extend the Fabric GameTest/focused integration fixtures from the current
   representative server/client network coverage, including the real
     client-to-server Town Hall rename, colony foundation, direct Town Hall
      placement, hut/building rename, Builder work-order creation/selection,
    decoration work-order creation, building reactivation, Miner level selection, Guard Tower patrol-mine assignment, building dirty marking, move-in policy, citizen pause/resume, Farmer hire/fire and hiring-mode transitions, residence assignment/unassignment, free-interaction permission changes, Builder inventory transfer, minimum-stock module updates, Guard Tower entity-filter updates, Smeltery item-filter updates, GiveTool inventory binding, citizen-inventory transfer, single-citizen recall, assigned-citizen worker/hut recall, Stone Smeltery recipe/menu routes, colony color/help and spy-hiring routes, citizen-inventory menu opening, request-state updates, building pickup, force pickup, warehouse sort/upgrade, Enchanter station assignment, Postbox request creation, citizen-restart scheduling, resource-scroll warehouse snapshots, build-tool inventory swapping, and University research actions; the latest fresh 113-test suite verifies all fourteen colony-view/citizen/removal/chunk-claim/visitor S2C registrations, seven particle/audio registrations and four build/suggestion/scanning registrations, with twenty-six payload round-trips across colony/building/chunk/visitor state, build and suggestion windows, scan NBT, path debugging and visual/audio feedback. The citizen, permissions, work-order and visitor view decoders now copy only the message payload, verified by round-trips; split `ServerUUIDMessage` client-executor dispatch and fishing-hook angler synchronization also remain covered. Continue payload/dispatch coverage across the other client-bound families. The seven registered menu opening buffers already have an
   automated contract test. The dedicated restart probe confirms
   representative citizen and visitor reappearance; broaden it to every
   custom entity family when their gameplay fixtures exist.
3. Continue covering the remaining Forge event points only where a narrow mixin
   or server hook can preserve semantics. Item toss/pickup, farmland trampling
   and bucket filling now have focused Fabric bridges and cancellation coverage;
   hostile natural/chunk-generation position checks, living-entity explosion
   damage, `ExplosionEvent.Start` cancellation and complete detonate block and
   entity filtering are also covered. The retained
   `ArrowNockEvent`, `ArrowLooseEvent`, projectile-impact and fishing-loot
   bridges are covered; remaining unsupported Forge points stay documented
   individually.
4. Keep the documented DataFixer policy aligned with upstream: no custom
   MineColonies 1.20.1 entity schemas exist in either the Forge source or the
   modern Fabric reference, so do not invent migrations. Revisit only if
   upstream publishes schemas or a real legacy-save requirement is defined.
5. Keep the retained upstream generated snapshot as an audit baseline. The
   semantic comparison is complete: native Fabric output is authoritative at
   runtime, and the deliberate differences are recorded in
   `PORTING_NOTES.md`; the snapshot is no longer part of the runtime source
   set.
6. MultiPiston's separate Fabric 1.20.1 module is implemented and its focused
   registration, redstone stroke/retraction and NBT tests pass. The combined
   113-test MineColonies + MultiPiston run also passes. Keep it as a required
   runtime dependency; the BlockUI screen still needs visual in-game validation.
7. Revisit JEI and JourneyMap integrations only after the core gameplay pass,
   using explicit Fabric APIs and keeping them optional.
8. The server-mod ZIP now builds and passes a fresh standalone Fabric server
   launch/save/clean-stop test with secure authentication settings. Next, join
   it from a fresh authenticated client and complete a real in-game pass; the
   package test log and properties snapshot are in `logs/`.
