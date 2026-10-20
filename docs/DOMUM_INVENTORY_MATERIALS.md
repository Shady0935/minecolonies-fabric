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
- Structurize: 1.0.3-port (dependencia de Domum y reapertura de la varita).
- Multi-Piston: 0.0.4-port (metadatos/dependencias).
- MineColonies: 1.0.16-port (metadatos y referencias a dependencias).

Structurize y Multi-Piston exigían exactamente Domum 1.0.0-port. Se cambia
su requisito a `>=1.0.1-port <1.1.0`; MineColonies apunta a las nuevas versiones
de ambos. No se modifica la IA ni la lógica de Multi-Piston/MineColonies.
El paquete de distribución también apunta a los nuevos JAR.

`MANUAL_VALIDATION_PENDING`: iniciar el perfil actualizado y comparar el mismo
muro de terracota roja en inventario, mano y Jade. Revisar también objetos con
dos materiales y recarga de recursos. Los objetos existentes conservan su NBT
y no deberían necesitar recolocación. No se añadieron ni ejecutaron tests.

## Ajuste solicitado durante esta entrega: posición de la varita

El parche anterior ejecutaba `clearPosition()` en todo clic sobre un bloque,
incluido al reabrir una estructura ya desplazada por el jugador. Ahora solo
lo hace cuando el preview no tiene un esquema seleccionado. Si está cargado
o tiene una selección pendiente de carga, conserva su ancla, orientación y
desplazamientos. No se llama a `getBlueprint()` para decidir si hay selección:
una carga asíncrona todavía no terminada tampoco debe perder su posición.

Tras cancelar o confirmar y retirar el preview de la caché, la siguiente
colocación vuelve a tomar el bloque señalado. Usar en el aire sigue reabriendo
el holograma existente. Validación manual pendiente: empezar sin holograma,
mover/rotar uno, cerrar la ventana y hacer clic en otro bloque; debe conservar
el ajuste hasta confirmar o cancelar la colocación.

## Compilación y perfil actualizado

Los cuatro `remapJar --console=plain` terminaron correctamente: Domum 23 s,
Structurize final (incluida la varita) 14 s, Multi-Piston 14 s, MineColonies
1 min 46 s. No se ejecutaron tests ni se inició un cliente de validación.

El 19 de octubre de 2026 a las 23:15 COT quedó actualizado el perfil de
CurseForge `Minecolonies Fabric`, con una sola versión activa de cada mod.
Se comprobaron los metadatos, los requisitos cruzados y la igualdad de hashes
entre los JAR de distribución y los instalados:

| Mod | SHA-256 |
| --- | --- |
| Domum 1.0.1 | `ae0abe62630145e93c322cc9f43e0a4afbb5a5460dc1d0843ecf9c9da91b434a` |
| Structurize 1.0.3 | `b241c0a68a7000d58756870230cb63617bee7e46e3294aee889f305a07243e65` |
| Multi-Piston 0.0.4 | `872922176cfb4f31c0d6fead5fd5597aea1b53009c3c85c742e717055f19b8f9` |
| MineColonies 1.0.16 | `fec0e271c4f06b5641d41ecad4f5722fd18a8a0a8ab1d6f54bd1addc5422ad07` |

Las cuatro versiones anteriores y el manifiesto se conservaron en
`mods/codex-backups/20261019-domum-inventory-29d6c485`. Un bloqueo temporal del
JAR anterior de MineColonies interrumpió la sustitución inicial; se completó
después y se verificó el conjunto final. No se cambiaron configuraciones.

Código de Domum/metadatos respaldado en el commit `6837205b75` de la rama
`fix/domum-inventory-material-models`; el ajuste adicional de la varita y esta
evidencia se guardan en el commit siguiente. No se modificó la rama estable.
El servidor remoto no se actualizó durante esta entrega. Para mantener el
conjunto de dependencias, copiar los cuatro nuevos JAR del perfil y reiniciar.
