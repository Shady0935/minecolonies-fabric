# Upstream mapping

This file records the exact references used by the Fabric 1.20.1 port. The
repositories under `references/` are read-only inputs; all implementation work
belongs under `project/`.

## MineColonies

### Functional baseline — Minecraft 1.20.1 Forge

- Remote: `https://github.com/ldtteam/minecolonies.git`
- Branch: `release/1.20`
- Commit at checkout: `b6987ade75e796dcbab573cdee0144ad54d1da12`
- Loader: Forge
- Minecraft: 1.20.1
- Java: 17

### Modern comparison — Minecraft 1.21.1 NeoForge

- Remote: `https://github.com/ldtteam/minecolonies.git`
- Branch: `version/1.21`
- Commit at checkout: `421c19a0f084e8b28e901a77bcdebf501200f8f`
- Loader: NeoForge
- Minecraft: 1.21.1
- Java: 21

## Fabric port references

### Initial candidate (not usable as source)

- Remote: `https://github.com/Desertnyotram/minecolonies.git`
- Branch: `main`
- Commit at checkout: `a2f0fac60559616a231b2e8ffe2650a92e40ef8f`
- Finding: repository contains only two JARs and a README. The modern JAR
  manifest identifies itself as `Lush Utils` and contains no MineColonies
  package/source tree. It is retained only as an audit trail and is not used
  as an implementation reference.

### Usable architectural reference

- Remote: `https://github.com/unknown-wq/minecolonies.git`
- Branch: `fabric-26.2`
- Commit at checkout: `a2cfc677627d9ecb4577f65b0d893bea4d264a5c`
- Loader: Fabric
- Minecraft: 26.2
- Provenance stated by the project: adapted from the official NeoForge 1.21.1
  line; used here only to study Fabric-side architecture and not as a direct
  backport source.

## Dependencies

The MineColonies 1.20.1 functional baseline declares BlockUI, Structurize,
Domum Ornamentum and MultiPiston as required dependencies. Branches checked
out for comparison:

| Component | 1.20.x reference | Commit | Modern reference | Commit |
|---|---|---:|---|---:|
| BlockUI | `ldtteam/BlockUI`, `release/1.20.1` | `542ff0d43615a638d87410fce00b6c29c2313509` | `release/main` | `78dae57f044e47f0e9b31390dda86f861963d402` |
| Structurize | `ldtteam/Structurize`, `version/1.20` | `8bf1f636105ecd0dac69846fa71e78c4b11cbafd` | `version/1.21` | `d0a1797da112abb75d2e5053ef09fb9f3caaf6c1` |
| Domum Ornamentum | `ldtteam/Domum-Ornamentum`, `release/1.20.1` | `ab3dc53f0648b5121e7aa07488fe68f655efde05` | `version/1.21.1` | `58c52049e75f2352fd5f6404b498a98fda7e7fd8` |
| MultiPiston | `ldtteam/Piston-Unlimited`, `version/1.20` | `7dc3db066977760dd3c722380002b83f72df2c23` | `release/1.21.1` | `34ab47124a4329dafa92cb0d21017ca811b4daef` |

MultiPiston is not a repository named `MultiPiston`; the upstream GitHub
project is `Piston-Unlimited`. It remains a separate required mod in the
official 1.20.1 dependency metadata. The target now ports it as the independent
Fabric module at `project/libs/multipiston`, keeps MineColonies' required
runtime dependency metadata, and restores its item to the generated NBT
compatibility data. Its gameplay baseline is the `version/1.20` checkout above;
the port retains the separate block/item, block entity, redstone movement,
BlockUI configuration window, networking and upstream resource IDs.

## Target

The target is a new Fabric 1.20.1 implementation under `project/`, with the
1.20.1 Forge source as the gameplay/content baseline and the modern NeoForge →
Fabric source comparison as the API migration dictionary.
