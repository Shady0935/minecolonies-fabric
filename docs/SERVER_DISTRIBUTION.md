# Dedicated-server mod bundle

The `serverModsBundle` Gradle task creates a ZIP under
`project/minecolonies/build/distributions/` containing the remapped MineColonies
Fabric JAR, Fabric API, and the required BlockUI, Domum Ornamentum, Structurize
and MultiPiston JARs under `mods/`. The bundle is for an existing Fabric server;
it does not include Minecraft or the Fabric server launcher.

## 2026-10-20 configuration parity delivery

| Mod | Version |
|---|---|
| MineColonies | 1.0.18-port |
| Structurize | 1.0.4-port |
| Multi-Piston | 0.0.5-port |
| BlockUI | 0.0.2-port |
| Domum Ornamentum | 1.0.2-port |

Update the four changed JARs together on client and server. NightConfig core/toml
3.8.4 are nested only in Structurize. Existing AI-only TOML files expand to the
full original schema on startup, preserving settings and backing up migrations.
An existing world `serverconfig` override takes precedence over the global
Fabric `config` file. See `CONFIGURATION_PARITY.md` for paths and verification.

The release passed 149 required GameTests and 12 unit tests. Production JARs
also passed native configuration/inventory/brewing probes, same-world restart
and a real client/server configuration round trip using Fabric Loader 0.19.5
on the client. The network probe used an isolated loopback-only offline server;
it does not claim an authenticated account login.

The packaging configuration resolves only the published Fabric API runtime JAR,
which already nests its runtime modules. It no longer adds loose transitive
modules or the development GameTest API alongside that JAR. The ZIP contains
exactly six top-level mod JARs, matching the tested distribution hashes.

The final ZIP was extracted into a fresh standalone Java 17 / Fabric Loader
0.15.11 server with only those six JARs. With online authentication and secure
profiles enabled, loopback binding, an automatically assigned port and RCON
disabled, it reached `Done`, saved all three dimensions and exited normally
with code 0. No authenticated client login was performed in that fresh instance.
Evidence: `logs/configuration-parity-bundle-production.log` and
`docs/audit/2026-10-20-configuration-parity-verification.json` (exact ZIP/JAR hashes).

## Building and historical startup verification

Build the dependency modules and MineColonies first, then run
`project/minecolonies/gradlew.bat serverModsBundle`. Extract the ZIP into a
Minecraft 1.20.1 Fabric server using Java 17 and Fabric Loader 0.15.11 or newer.
Keep the same mod versions on the client and server. Leave
`online-mode=true` and `enforce-secure-profile=true` for authenticated play.

The development dedicated server has passed an isolated MultiPiston-enabled
startup, save, clean shutdown and same-world restart with those secure settings.
The packaged ZIP has also been extracted into a fresh standalone Fabric server
using Fabric Loader 0.15.11. It loaded 50 mods, reached `Done`, saved, and
shut down with all three dimensions saved. The test kept online authentication
and secure profiles enabled, bound to loopback, and disabled RCON. The server
log and properties snapshot are preserved in
`logs/minecolonies-fabric-server-bundle-20260922.log` and
`logs/minecolonies-fabric-server-bundle-properties-20260922.txt`.

A login from a fresh authenticated client and a full in-game gameplay/UI pass
remain necessary before release validation is complete. The startup's
`No data fixer registered` warnings for MineColonies entities are documented
upstream limitations; no custom entity migration schemas are provided by the
upstream 1.20.1 implementation.
