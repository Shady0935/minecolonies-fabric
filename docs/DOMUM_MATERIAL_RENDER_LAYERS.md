# Domum: capas por material y caras bajo el agua

## Diagnóstico del 20 de octubre de 2026

Las capturas muestran marcos con materiales opacos que se ven bien sobre el
agua, pero permiten ver caras interiores o pierden superficies al sumergirse.
El port registra los bloques personalizables en una capa genérica translúcida.
`emitBlockQuads` retexturizaba sus caras y las enviaba por el consumidor vanilla,
sin indicar la capa real de los materiales: madera y terracota terminaban en la
misma pasada de mezcla/ordenación que el agua. Es una causa de código compatible
con el síntoma; la recuperación visual sigue pendiente de validación en juego.

En upstream 1.20.1, `MateriallyTexturedBakedModel.getRenderTypes` obtiene las
capas de los componentes y `RetexturedBakedModelBuilder.with` filtra por la
capa del material. Fuente consultada, commit ab3dc53f0648b5121e7aa07488fe68f655efde05:

- https://github.com/ldtteam/Domum-Ornamentum/blob/ab3dc53f0648b5121e7aa07488fe68f655efde05/src/main/java/com/ldtteam/domumornamentum/client/model/baked/RetexturedBakedModelBuilder.java
- https://github.com/ldtteam/Domum-Ornamentum/blob/ab3dc53f0648b5121e7aa07488fe68f655efde05/src/main/java/com/ldtteam/domumornamentum/client/model/baked/MateriallyTexturedBakedModel.java

También se consultó el código local de Fabric Renderer API 3.2.2+1802ada577:
`BlendMode`, `RenderContext`, `MaterialFinder`, `QuadEmitter`, `MutableQuadView`.
`DEFAULT` hereda la capa del bloque; especificar el blend permite elegir la
capa por cara. `fromVanilla` conserva geometría, UV, tinte, sombreado y cull face.

## Cambio en Domum 1.0.2-port

- Se obtiene la capa de cada bloque material mediante
  `ItemBlockRenderTypes.getChunkRenderType` y se traduce a `BlendMode`.
- El emisor de Fabric recibe cada quad con su material: solid, cutout,
  cutout_mipped o translucent. Un marco puede mezclar soporte opaco y cristal
  translúcido, sin convertir la madera en transparente ni el cristal en opaco.
- Las caras fijas de la plantilla sin sustitución usan cutout para conservar
  recortes alfa sin mezclarse con el agua. Los materiales RenderMaterial se
  reutilizan en una caché de como máximo los modos de BlendMode utilizados.
- Se mantiene la transformación de texturas y la geometría existentes, la
  separación de caras con y sin culling y el renderizado previo de los objetos.
- No se cambia waterlogging, estados guardados, paquetes, IA o configuraciones.
  La corrección actúa en cliente al reconstruir la malla del chunk.

Solo se entrega Domum 1.0.2-port. Structurize 1.0.3 y Multi-Piston 0.0.4 ya
aceptan `>=1.0.1-port <1.1.0`; MineColonies 1.0.16 usa Domum sin restricción
de versión. Las referencias a JAR locales y al bundle se actualizan, sin
necesitar reconstruir los demás mods ni cambiar sus versiones.

## Validación pendiente

`MANUAL_VALIDATION_PENDING`: mirar los mismos pilares desde dentro y fuera del
agua; revisar madera/terracota, cristal transparente y tintado, y un marco de
dos materiales. Comparar culling de caras vecinas y comprobar inventario/Jade.
Abrir de nuevo el cliente fuerza la reconstrucción de chunks con el renderer
actualizado. No se añaden ni ejecutan pruebas automatizadas.

## Entrega local

`remapJar --console=plain` terminó correctamente en 25 s; los tres avisos
restantes son llamadas existentes a consumidores obsoletos de Fabric en el
camino de objetos o en el fallback sin datos. Se inspeccionaron el código y
los metadatos del JAR y los requisitos de las tres dependencias activas.

Domum 1.0.2-port instalado en el perfil `Minecolonies Fabric` el 20 de octubre
a las 01:49 COT. Hay una sola versión activa y su SHA-256 coincide con el JAR
de distribución:
`a4eec2b5f9c69c9245298cefed310769b9aef78cfd73d46cf2a211a31e3275db`.

Minecraft de ese perfil (PID 5640) cerró normalmente. La versión anterior
1.0.1 y el manifiesto están en
`mods/codex-backups/20261020-domum-water-645d3079`. Structurize 1.0.3,
Multi-Piston 0.0.4 y MineColonies 1.0.16 mantienen sus hashes y configuraciones.
El servidor remoto no se modificó; el arreglo de renderizado se ejecuta en
el cliente. Sigue pendiente la comprobación visual con los mismos pilares.
