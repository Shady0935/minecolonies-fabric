# Dedicated-server mod bundle

The `serverModsBundle` Gradle task creates a ZIP under
`project/minecolonies/build/distributions/` containing the remapped MineColonies
Fabric JAR, Fabric API, and the required BlockUI, Domum Ornamentum, Structurize
and MultiPiston JARs under `mods/`. The bundle is for an existing Fabric server;
it does not include Minecraft or the Fabric server launcher.

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
