# Esquemas y colocación: MineColonies 1.0.8 / Structurize 1.0.1

Rama `fix/blueprint-placement`, basada en `ai-overhaul` (`2ea2f75248`).
Las correcciones conservan el overhaul existente y no modifican `version/main`.

## Aviso de esquema original ausente

La captura muestra `com.minecolonies.coremod.invalidbuilding`, no un diagnóstico
de bloques dañados. En el servidor de pruebas, la residencia en `-660 64 362`
guarda `pack="Lost Mesa City"`, `path="fundamentals/residence5.blueprint"` y
`schematicName="residence5"`. El log del 19 de octubre de 2026 (UTC) registra
Lost Mesa City a las 04:19:01 y avisos genéricos de restauración de datos a las
04:49:11, 04:49:16 y 04:49:26. Los cinco niveles de esa residencia están incluidos
en los recursos del proyecto.

`TileEntityColonyBuilding.load` llamaba primero a `super.load`. Esa llamada lee
los datos del esquema y puede resolver la colonia, asociar el edificio y ejecutar
`safeUpdateTEDataFromSchematic` antes de leer el pack y la ruta del bloque.
La validación puede observar un pack vacío o anterior; al desreferenciar
`te.getStructurePack().getName()` falla y puede emitir el aviso aunque el archivo
exista. Los logs anteriores no conservaban la excepción, por lo que no permiten
atribuir retrospectivamente cada aviso a esta causa.

Se cargan ahora la identidad, colonia y espejo antes de la llamada que notifica
al edificio. La conversión de estilos antiguos consulta el nombre y las etiquetas
del NBT entrante. Se conserva la regla de que la posición del propietario de una
cabaña es su propia posición tras cargar los datos de inventario. Los dos errores
de restauración registran posición, edificio, pack, ruta y excepción para poder
diagnosticar cualquier caso restante. No se ocultan avisos de esquemas ausentes.

La referencia local de MineColonies 1.21 en `dae860ee` también llama a la carga
superior antes de leer el pack; esto no basta para afirmar que el fallo observado
se reproduce en una versión publicada de upstream.

## Holograma lejos del clic

La ventana solo usaba la posición recibida si la vista previa no tenía posición.
La colocación creativa conserva la vista previa, de modo que un clic posterior
podía reutilizar el ancla anterior. Cancelar con X eliminaba ese estado, lo que
explica que repetir la selección lo corrigiese.

Un clic nuevo sobre un bloque reinicia posición y desplazamiento de suelo antes
de abrir la ventana. Conserva esquema, rotación y espejo. Usar la varita al aire
reanuda la vista previa; cambiar de pack desde la ventana conserva el ancla actual.
Reiniciar también el desplazamiento evita acumular el ajuste de altura anterior.

## Entrega y comprobación

- MineColonies: `minecolonies-fabric-1.20.1-1.0.8-port.jar`.
- Structurize: `structurize-fabric-1.20.1-1.0.1-port.jar`.
- Compilación y empaquetado con `remapJar` de ambos módulos.
- No se han ejecutado pruebas automáticas ni se ha validado esta versión en juego.
- `MANUAL_VALIDATION_PENDING`: colocar la residencia de la captura en creativo;
  volver a colocar otra construcción lejos sin cancelar; comprobar el ancla tras
  usar la varita al aire, rotarla, reflejarla y cambiar de pack.
- Sustituir ambos JAR en cliente y servidor, sin mantener dos versiones activas
  del mismo mod. Los artefactos se generan en `project/**/build/libs`.
- Esta entrega no modifica el mundo ni los mods del servidor remoto o CurseForge.

## Corrección de dependencias de arranque (1.0.9)

El primer paquete omitió una restricción de Multi-Piston 0.0.1: su manifiesto
exige exactamente Structurize 1.0.0. Fabric rechaza Structurize 1.0.1 antes de
iniciar el servidor. MineColonies 1.0.8, a su vez, exige Multi-Piston 0.0.1.

- Multi-Piston 0.0.2 acepta explícitamente Structurize 1.0.0 o 1.0.1 y compila
  contra 1.0.1. No cambia su lógica de juego.
- MineColonies 1.0.9 requiere Structurize 1.0.1 y Multi-Piston 0.0.2, y compila
  contra ambos. Conserva las correcciones de 1.0.8 y el overhaul.
- El conjunto de actualización sustituye tres JAR: MineColonies 1.0.9,
  Structurize 1.0.1 y Multi-Piston 0.0.2, tanto en cliente como en servidor.
- Los manifiestos empaquetados deben coincidir con esta cadena; no basta con
  cambiar las versiones de compilación en `gradle.properties`.
- El ZIP de servidor referencia también las nuevas versiones. Multi-Piston
  consume los JAR locales de desarrollo como `implementation`, igual que
  Structurize: ya están en mappings `named` y no deben remapearse de nuevo como
  dependencias `intermediary` (el access widener de Structurize lo rechazaba).
