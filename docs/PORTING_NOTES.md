# Porting notes

## Initial architecture decision

The official MineColonies 1.20.1 source is the functional baseline. The
modern NeoForge and Fabric trees are comparison material for platform APIs;
modern gameplay code is not copied wholesale into the 1.20.1 target.

## Reference audit

The first downloaded `Desertnyotram/minecolonies` repository is not a usable
source port: it contains release JARs rather than source, and its modern JAR
does not identify itself as MineColonies. The usable Fabric comparison is the
`unknown-wq/minecolonies` `fabric-26.2` checkout. This distinction is recorded
so it is not accidentally treated as trusted source later.

## MultiPiston

The official dependency is published from the `ldtteam/Piston-Unlimited`
repository. The initial question—port, replace, absorb, or remove it—remains
open until the 1.20.1 source usage and the Fabric reference are compared.

## Status vocabulary

- `NOT_IMPLEMENTED_YET`: work is planned or incomplete; it is not evidence of
  an incompatibility.
- `KNOWN_LIMITATION`: a confirmed technical or upstream limitation, recorded
  only after investigation and a reproducible observation.

