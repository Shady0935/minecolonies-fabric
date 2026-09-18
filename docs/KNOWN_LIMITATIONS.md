# Known limitations

These are confirmed observations, not guesses. Incomplete gameplay coverage
is also tracked in `PORTING_STATUS.md` and `TEST_MATRIX.md`.

## Confirmed technical limitations

- MineColonies entity types currently have no registered Minecraft DataFixer
  schemas. Client startup reports `No data fixer registered for
  minecolonies:<entity>` for the custom entities. Existing saves that require
  entity-version migration are therefore not covered yet.
- Fabric datagen is available and reproducible for the portable provider set:
  27 providers generated 764 JSON resources. Eight upstream providers remain
  explicitly excluded because they still depend on Forge-only APIs:
  `DefaultAdvancementsProvider`, `DefaultBlockTagsProvider`,
  `DefaultDamageTagsProvider`, `DefaultDamageTypeProvider`,
  `DefaultEntityIconProvider`, `DefaultEntityTypeTagsProvider`,
  `DefaultItemTagsProvider` and `QuestTranslationProvider`. Their exact parity
  is not claimed until those providers are ported or replaced with Fabric
  implementations.
- JEI and JourneyMap integrations are excluded from the target source set.
- The standalone MultiPiston Forge dependency is not included in the Fabric
  runtime. The current MineColonies main source has no direct runtime reference
  to it, but its independent block/content remains unported.
- The command/entity smoke test summons a citizen without a colony only to
  exercise registration, entity construction and serialization. The upstream
  `CitizenColonyHandler` intentionally removes that isolated citizen on the
  next load, so this does not validate colony-backed citizen persistence.
- Fabric 1.20.1 has no direct callback equivalent for several retained Forge
  event points: farmland trampling, pre-conversion, explosion start/detonate,
  item toss/pickup, bucket fill, arrow loose and mob-spawn position checks.
  Those paths remain explicit coverage gaps; they are not hidden behind broad
  no-op adapters.
- The retained block-place event is emitted as a pre-vanilla placement
  preflight because Fabric has no matching post-placement event. The bridge
  validates `BlockPlaceContext` and prevents placement when the retained event
  denies it, but it is not a byte-for-byte replacement for Forge's post-place
  timing.

## Validation still pending

- Town Hall placement, colony creation, permissions, BlockUI screens, builder
  placement, citizen work cycles, logistics, research and raids still need a
  real in-game interaction pass.
- Client-to-server gameplay packets have not been driven through every GUI or
  block interaction. The transport itself has a login/server-to-client smoke
  pass, including `ServerUUIDMessage`.
- The new interaction/combat callback bridge has been loaded by dedicated
  server and client bootstrap, but Town Hall placement, colony creation and
  the resulting permission/quest actions still require a real in-game or
  focused integration fixture.
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

## Intentional compatibility residues

The source still contains a narrow Forge-shaped compatibility layer because
the 1.20.1 gameplay code uses those types. The `forge` worldgen tag namespace
is also intentional and backed by Fabric-loaded tag files. These are tracked
bridges, not evidence that the runtime still depends on Forge.
