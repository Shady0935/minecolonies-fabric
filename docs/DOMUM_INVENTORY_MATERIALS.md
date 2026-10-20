# Domum: materiales en iconos de objetos

## Diagnóstico (19 de octubre de 2026)

La captura muestra un muro de terracota roja con `textureData` (el nombre y la
descripción reconocen el material), pero Jade dibuja la textura predeterminada.
El port ya implementa `MateriallyTexturedBakedModel.emitItemQuads`, que lee
ese NBT y construye el modelo con los materiales. El cargador, sin embargo,
solo envuelve identificadores cuyos paths empiezan por `block/` o `item/`.

Fabric también pasa identificadores `ModelResourceLocation`, por ejemplo
`domum_ornamentum:vanilla_wall_compat#inventory`. No tienen prefijo `item/`;
el modelo superior del objeto puede quedarse sin el wrapper y no ejecutar el
renderizado por stack. El icono de Jade utiliza el mismo renderizado de objetos.
No hay evidencia de pérdida del material del bloque en esta captura.

## Comparación con el original

En Domum original de 1.20.1, commit
`ab3dc53f0648b5121e7aa07488fe68f655efde05`, `getRenderPasses(ItemStack, boolean)`
deserializa `textureData` y genera los modelos retexturizados por objeto.
Es una extensión de Forge que el port sustituye por `FabricBakedModel`.
Fuente consultada:
https://github.com/ldtteam/Domum-Ornamentum/blob/ab3dc53f0648b5121e7aa07488fe68f655efde05/src/main/java/com/ldtteam/domumornamentum/client/model/baked/MateriallyTexturedBakedModel.java

También se leyó el código fuente local de Fabric Model Loading API
1.0.4+1802ada577 (`ModelModifier` y `ModelLoaderMixin`); documenta expresamente
el uso del identificador con variante `inventory` para objetos.

## Corrección

`MateriallyTexturedModelLoader` reconoce ahora `#inventory` de los `IDoItem`
registrados de Domum, además de los modelos anteriores. Se usa `WRAP_PHASE`
y se evita envolver dos veces un `MateriallyTexturedBakedModel` existente.
Reutiliza `emitItemQuads`, el NBT, geometría y transformaciones existentes;
no introduce un renderer específico de Jade ni cambia objetos guardados.

## Versiones y alcance de entrega

- Domum Ornamentum: 1.0.1-port (corrección del cargador de modelos).
- Structurize: 1.0.3-port (metadatos/dependencia de Domum).
- Multi-Piston: 0.0.4-port (metadatos/dependencias).
- MineColonies: 1.0.16-port (metadatos y referencias a dependencias).

Structurize y Multi-Piston exigían exactamente Domum 1.0.0-port. Se cambia
su requisito a `>=1.0.1-port <1.1.0`; MineColonies apunta a las nuevas versiones
de ambos. No se modifican la IA ni el comportamiento de estas dependencias.
El paquete de distribución también apunta a los nuevos JAR.

`MANUAL_VALIDATION_PENDING`: iniciar el perfil actualizado y comparar el mismo
muro de terracota roja en inventario, mano y Jade. Revisar también objetos con
dos materiales y recarga de recursos. Los objetos existentes conservan su NBT
y no deberían necesitar recolocación. No se añadieron ni ejecutaron tests.
