# Porting notes

## Baseline and architecture

The official MineColonies 1.20.1 Forge source is the gameplay and content
baseline. The 1.21.1 NeoForge tree and the modern Fabric checkout are used as
API migration references only; modern gameplay code is not copied wholesale
into the 1.20.1 target.

The target build lives under `project/minecolonies`. Its main source combines
the upstream API and implementation trees while excluding the still-unported
generation and optional JEI/JourneyMap integration packages. Fabric entrypoints
are declared in `fabric.mod.json` for common and client initialization.

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

## Fabric datagen

The target exposes Fabric Loom's `runDatagen` task through the
`fabric-datagen` entrypoint. `MineColoniesDataGenerator` registers the
portable 1.20.1 recipe, research, loot and worker-crafting providers and
explicitly registers the three custom ingredient serializers before provider
execution. This makes datagen independent of runtime event ordering.

The verified run registered 27 providers, generated 764 JSON resources and
completed with 2,399 cached writes. Generated resources live under
`project/minecolonies/src/main/generated/resources` and are part of the main
resource source set; the Fabric datagen cache is ignored and excluded from
the packaged resources.

Custom ingredient arrays are converted to Fabric's `fabric:any` custom
ingredient rather than being placed inside a vanilla `Ingredient` array. This
preserves the upstream alternative-input semantics on Fabric 1.20.1.

The eight Forge-only generation providers remain excluded explicitly: default
advancements, block tags, damage tags, damage types, entity icons, entity type
tags, item tags and quest translations. This is a tracked parity gap, not a
silent no-op.

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

## Resource and startup fixes

The port includes the missing English language resource, Fabric-compatible
block/entity tags, upstream sounds, vanilla spear model overrides, lowercase
resource paths, and flattened blockhut composite models. Client bootstrap now
reaches OpenAL and all atlases; remaining warnings are listed separately.

## Status vocabulary

- `NOT_IMPLEMENTED_YET`: work is planned or incomplete; it is not evidence of
  an incompatibility.
- `KNOWN_LIMITATION`: a confirmed technical limitation with a reproducible
  observation.
- `MANUAL_VALIDATION_PENDING`: the code path is present but requires in-game
  interaction that has not yet been exercised by the terminal-only workflow.
