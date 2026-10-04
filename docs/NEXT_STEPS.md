# Next steps

## Profile validation after 2026-10-03 claim persistence fix

1. Re-enter the test profile: verify one-time restoration of inferable Town
   Hall/building territory from the older native colony save.
2. Claim an extra chunk outside that area, remove a different claim, save/exit
   and re-enter twice; verify both operations persist and no backup recovery
   warning appears on ordinary subsequent loads.
3. Previously lost arbitrary manual claims must be recreated: old builds did
   not store their coordinates in any colony recovery file.

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
University parallel-research and focused researcher study/offline-mana checkpoints are now
complete:
`runDatagen` is reproducible with
all 37 providers, native advancements/icons/quests are wired to the Fabric
entrypoint, the generated resource set is the runtime source of truth, retained
lifecycle/event handlers receive real Fabric callbacks, structure packs load
through Fabric server lifecycle, and the populated-tavern conversion creates a
real visitor entity. The latest clean combined Fabric GameTest run passes all
125 tests, including the owner-controlled colony-rank permission envelope,
authorized permission-management routes and a rejected unauthorized rank edit,
permission/rank/player state surviving a complete colony-NBT reload,
two parallel University researches, 24-block Researcher
navigation/offline-mana checks, FarmField colony-NBT persistence,
compost-assisted crop ripening/harvest and the 24-block normal-CitizenAI
Farmer/Courier cycle:
two-cell harvest, hut deposit, pickup-request completion and Warehouse-rack
delivery, the S2C FarmField full refresh (updating an existing
field, adding one and removing a stale field after dispatch), the queued
client-executor building-removal route, the applied ColonyView work-order
update, and the applied citizen-view and visitor-view updates and research-manager state. The
citizen and work-order removal routes also enqueue their client handlers, clear
their split-packet cache entries and leave the server-side fake views untouched
before client-executor dispatch; those client-only callbacks remain unexecuted.
An empty visitor refresh also preserves the existing visitor until dispatch,
then removes it from the client view.
`SyncPathMessage` and `SyncPathReachedMessage` now also traverse real S2C split
envelopes, clear their packet-cache entries and drain client-executor callbacks
in the dedicated-server test; visible renderer-state changes remain a live-client
check.
The full `ColonyViewMessage` subscription envelope also queues exactly one
client callback, clears its packet-cache entry and leaves the view absent before
that callback; applying it still requires a live client world.
The permissions round-trip now changes an authorized rank on the server, then
applies its S2C `PermissionsMessage.View` only after client-executor dispatch;
the pre-dispatch view and both packet-cache entries are checked, and the change
also survives colony-NBT reload.
The run also includes the three standalone MultiPiston tests; its log and JUnit
report are `logs/minecolonies-gametest-s2c-farm-refresh-20260924.log` and
`logs/minecolonies-gametest-s2c-farm-refresh-20260924.xml`. The full package
build also passes in `logs/minecolonies-build-s2c-farm-refresh-20260924.log`. It saved
all three dimensions and shut down cleanly. A Farmer-AI follow-up additionally
proves that compost ripens and enables
harvesting wheat one age below maturity; the fresh 125-test run and JUnit report
are `project/minecolonies/run-gametest-client-bridge-20260927/logs/latest.log`
and `project/minecolonies/build/gametest/junit.xml`.
A live Builder end-to-end fixture now proves automatic work-order claim and navigation,
separate stone and cobblestone Stack/Delivery requests fulfilled as the assigned
Courier autonomously travels from two Warehouse racks to the Builder through its
normal `CitizenAI`, placement of a synthetic 17-block blueprint and order
completion; cross-warehouse/multi-destination logistics, larger builds and
in-game construction remain pending. The suite covers
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
Its 90-test core batch plus isolated Lumberjack, Farmer hoe/navigation/planting,
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
long-range Researcher bookshelf navigation/study/mana (starting 24 blocks from
the registered bookshelf), 24-block two-cell Farmer navigation/harvest/hut
deposit followed by Courier pickup into the Warehouse rack, and Guard
autonomous-target-search/combat cycles with an
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

The user has clarified that the active goal should continue through the
remaining port work without interruptions for interactive testing. Do not
launch the client or wait on user GUI actions during this goal. GUI-, audio- and
visual-only checks in the numbered list below, plus a fresh authenticated
client/server gameplay pass, are deferred until after the automated port work.
The later in-game iteration list is: overlapping citizen-dialogue sounds and
the missing citizen interaction window; the Town Hall/colony-selection anomaly
(the test colony was deleted and its original state is unavailable); slight
construction-hologram drift while moving; visible BlockUI translations; and
visual confirmation of the Structurize switch-pack icon. The path typo is fixed;
its rendered appearance remains pending, not claimed as visually verified.
The code-only stages of the active port goal are complete: headless
network/event parity, upstream DataFixer and generated-resource decisions,
optional JEI/JourneyMap Fabric integration boundaries, and server packaging
have been verified. The remaining work recorded below requires live GUI,
rendering, audio, or authenticated in-game interaction; per the user's request,
those checks are deferred until after this automated port goal.

1. Deferred until after the automated port goal: continue the real gameplay
   pass. A fresh offline development-profile client
   loads 63 mods and now keeps the saved-world identity stable as `Player847`.
   The user's 2026-09-26 session log confirms supply placement, colony
   foundation, the first citizen, Builder's Hut placement/build progression,
   and Town Hall/research interactions. That pass exposed the missing runtime
   language merge, now fixed at resource processing; the relaunched client
   logs readable colony and Builder messages, while the Town Hall/BlockUI
   button labels still need the user's visual confirmation. Continue with the
   supply ship in water and the remaining BlockUI screens and worker cycles:
   citizen, farmer, miner, Guard Tower, warehouse and research. Automated
   coverage now verifies
   two active University research projects progressing in parallel and the
   Researcher's 24-block bookshelf navigation, study and offline-mana scaling;
   the authenticated University GUI/gameplay cycle remains manual.
   Automated terrain checks now cover valid/invalid supply-camp and supply-ship
   sites, but actual full-blueprint placement still needs the in-game pass. The
   spear's Creative search result, attributes tooltip and first-person held
   model have now passed a visual check; retain the actual BlockUI screens and
   authenticated gameplay cycle as manual work. The live UI pass also exposed
   a plural/singular texture path typo in Structurize's switch-pack window;
   the reference now matches the packaged resource, and the UI's rendered
   appearance remains deferred for visual confirmation.
2. Network coverage: the headless audit is complete for message routes that can
   be checked without a live client. The source/test inventory found no
   concrete serverbound message class without a GameTest reference; S2C
   split-envelope, codec and callback-queue coverage is recorded below. Leave
   client UI, rendering and audio execution for the deferred in-game pass.
   Existing focused fixtures include the real
     client-to-server Town Hall rename, colony foundation, direct Town Hall
      placement, hut/building rename, Builder work-order creation/selection,
   decoration work-order creation, building reactivation, Miner level selection,
   Guard Tower patrol-mine assignment, building dirty marking, move-in policy,
   citizen pause/resume, Farmer hire/fire and hiring-mode transitions, residence
   assignment/unassignment, free-interaction permission changes, Builder
   inventory transfer, minimum-stock module updates, Guard Tower entity-filter
   updates, Smeltery item-filter updates, GiveTool inventory binding,
   citizen-inventory transfer, single-citizen recall, assigned-citizen
   worker/hut recall, Stone Smeltery recipe/menu routes, colony color/help and
   spy-hiring routes, citizen-inventory menu opening, request-state updates,
   building pickup, force pickup, warehouse sort/upgrade, Enchanter station
   assignment, Postbox request creation, citizen-restart scheduling,
   resource-scroll warehouse snapshots, build-tool inventory swapping, and
   University research actions. The fresh 125-test suite verifies all fifteen
   colony-view/citizen/removal/chunk-claim/visitor/colony-map S2C registrations,
   twelve particle/audio registrations and four build/suggestion/scanning
   registrations, with 37 targeted payload round-trips across colony-view
   framing, colony-map summaries, colony/building/chunk/visitor state, opaque
   global-quest/research/recipe/compatibility sync, particle/audio feedback,
   build and suggestion windows, scan NBT and path debugging. It additionally
   sends 19 map, chunk-capability, build/suggestion/scan and particle/audio
   messages through the split envelope, asserting client-executor queuing and
   packet-cache cleanup while deliberately leaving GUI, rendering and audio
   callbacks unexecuted. The dedicated-server test also verifies the common
   client bridge returns before resolving client-only classes. All nine
   serverbound `PermissionsMessage` routes pass through the real C2S split
   envelope: owner-authorized rank permission changes; authorized rank/player
   add/remove, player-rank assignment, rank-type and subscriber edits; and
   rejection of an unauthorized rank addition. `AddPlayer` resolves connected
   players before consulting the optional profile cache, and every tested route
   confirms cache cleanup. The `ColonyViewMessage` round-trip caught and fixed
   its decoder retaining the full packet header as view data. The four opaque
   sync payloads remain byte-identical across repeated encoding; the citizen,
   permissions, work-order and visitor view decoders copy only the message
   payload; split `ServerUUIDMessage` client-executor dispatch and fishing-hook
   angler synchronization also remain covered. The full `ColonyViewMessage`
   S2C subscription envelope queues its client callback, clears the split cache
   and leaves the view absent before dispatch; application requires a live
   client world. The permissions route changes an authorized neutral-rank
   action through C2S, confirms the local view stays unchanged until S2C
   `PermissionsMessage.View` dispatch, then matches the server and survives
   colony-NBT reload. Other S2C split-envelope tests verify colony-view
   removal, FarmField updates, and applied citizen-view, visitor-view,
   work-order and research-manager updates after the client executor runs.
   Building-, citizen- and work-order-removal cases verify client-executor
   queuing/cache cleanup without mutating dedicated-server views; client-only
   removal callbacks remain unexecuted. An empty visitor refresh removes stale
   data only after dispatch. Evidence: `project/minecolonies/run-gametest-client-bridge-20260927/logs/latest.log`
   and `project/minecolonies/build/gametest/junit.xml`. The seven registered menu opening buffers already have an
   automated contract test. The dedicated restart probe confirms
   representative citizen and visitor reappearance; broaden it to every
   custom entity family when their gameplay fixtures exist.
3. The retained-event audit found and restored two missing client publishers:
   `PlaySoundEvent` at the vanilla sound-manager boundary and
   `CustomizeGuiOverlayEvent.DebugText` at debug-text assembly. Item toss/pickup,
   farmland trampling, bucket filling, hostile natural/chunk-generation spawn
   checks, living-entity explosion damage, explosion start/detonate filtering,
   bow hooks, projectile impact, fishing loot and pre-dimension travel already
   have focused bridges/tests. Keep the documented block-placement preflight
   timing and direct `CapabilityHooks` provider adaptation explicit; defer sound
   and visible-overlay behavior checks to the user-facing pass. The common
   client-message bridge exits on a dedicated server before resolving
   client-only classes. Do not add shims for Forge events MineColonies does not
   consume.
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
   125-test MineColonies + MultiPiston run also passes. Keep it as a required
   runtime dependency; the BlockUI screen still needs visual in-game validation.
7. The MineColonies JEI and JourneyMap Fabric adapters and the Domum Ornamentum
   Architect's Cutter JEI adapter are now ported and compile-only. Keep actual
   recipe/transfer, ghost-slot, map-overlay and waypoint checks in the deferred
   client pass, with the optional mods installed.
8. The server-mod ZIP has been rebuilt successfully; the bundle contains
   MineColonies, Structurize, BlockUI, Domum Ornamentum, MultiPiston and Fabric
   API, while leaving optional JEI/JourneyMap APIs out. A previous fresh
   standalone server launch/save/clean-stop test passed with secure
   authentication settings. A fresh authenticated client join and real in-game
   pass are deferred until after the automated port goal; package evidence and
   properties snapshot are in `logs/`.
