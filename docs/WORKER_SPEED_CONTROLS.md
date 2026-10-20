# Velocidad de trabajo: MineColonies 1.0.12

19 de octubre de 2026, rama `feat/worker-speed-controls`, basada en
`fix/warehouse-consistency` (`e8e6e948cc`). Conserva las correcciones anteriores.

## Observación de Kohen

Las consultas de lectura del servidor identifican a Kohen Y. Gordon como
ciudadano 2 de la colonia 1, constructor, con las once habilidades a 99.
En la muestra observada está en `WORKING / BUILDING_STEP`, orden 5, avanzando
en BUILD_SOLID alrededor de `(-666,68,236)`. El log reciente contiene avances
de objetivos y rutas alcanzables; no demuestra que esté inmóvil.
`action-progress` también puede registrar posiciones ya correctas: no debe
interpretarse cada línea como un bloque colocado o roto.

La lectura puntual de `HandItems` dio ambas manos vacías durante construcción;
no verifica ni contradice que tenga una pala con Eficiencia V en inventario.
El equipo puede cambiar entre colocar y retirar bloques. No se modificó al
ciudadano, al edificio ni al mundo para investigar esto.

## Frenos encontrados en el código

- El minado usa dureza, velocidad base de herramienta, habilidad e investigación,
  pero omitía el bonus de Eficiencia. El constructor ya tenía un factor propio
  de 0,5 sobre ese cálculo; se conserva.
- `MINE_BLOCK` se comprobaba cada 10 ticks, mientras `BUILDING_STEP` tenía una
  cadencia de 5. Son límites de reacción además del tiempo calculado de minado.
- `hasNotDelayed(0)` también exigía volver en otra transición antes de romper.
  Con habilidad 99 muchos cálculos ya dan cero; Eficiencia adicional no puede
  eliminar por sí sola esta espera fija.

## Ajustes

El servidor genera esta sección en `config/minecolonies-server.toml` y la lee
al arrancar. Se mantienen las demás secciones y sus opciones.

```toml
[ai_work_speed]
workeraispeedmultiplier = 2.0
miningspeedmultiplier = 2.0
builderplacementspeedmultiplier = 2.0
builderminingcheckinterval = 1
```

- `workeraispeedmultiplier`: reduce el intervalo entre las transiciones normales
  de los trabajadores civiles, con un mínimo de un tick. No ejecuta múltiples
  acciones del mismo trabajador en un tick. Las transiciones globales de bloqueo
  y eventos mantienen su frecuencia; el descuento de esperas explícitas sigue
  usando el tiempo normal del servidor. Los guardias conservan su cadencia.
- `miningspeedmultiplier`: divide el cálculo compartido de minado. Se añade
  `eficiencia² + 1` a la velocidad de las herramientas que son efectivas para el
  bloque. Las profesiones con cálculo propio, como el florista, lo conservan;
  el modo PvP conserva también su fórmula fija existente.
- `builderplacementspeedmultiplier`: divide la espera de colocación de las IA
  de estructuras, respetando habilidad, investigación y el resto de requisitos.
- `builderminingcheckinterval`: cadencia de revisión del estado de minado de
  las IA de estructuras. Por defecto pasa de 10 a 1 tick.
- Una espera calculada de cero no crea una espera artificial. El helper limpia
  también el objetivo anterior y su indicador de espera para evitar saltar el
  tiempo de otro bloque si la herramienta o el objetivo cambian.

Multiplicadores permitidos: 0,25 a 8; intervalo: 1 a 20 ticks enteros. Los valores
no válidos vuelven al predeterminado con aviso. Reiniciar tras cambiar opciones.
Valores 1 y un intervalo 10 recuperan las cadencias anteriores, manteniendo las
correcciones de Eficiencia y de esperas cero.

Los multiplicadores afectan mecanismos distintos: no garantizan que una obra
completa tarde exactamente la mitad. Caminatas, materiales, accesibilidad,
inventario, hambre, recetas, durabilidad y permisos siguen aplicándose. Puede
aumentar el coste de trabajo y navegación al producir más acciones por segundo.
No hay cambios al movimiento físico ni al tiempo del mundo, hornos o cultivos.

## Entrega y verificación

Solo cambia MineColonies, a **1.0.12-port**. Mantiene las dependencias exactas
Structurize 1.0.2 y Multi-Piston 0.0.3. No se añaden ni ejecutan pruebas
automáticas. `remapJar --console=plain` terminó en 2 min 25 s; una compilación
final de 1 min 2 s incluye la limpieza de esperas cero. Los avisos son los
existentes sobre APIs obsoletas y operaciones sin comprobar. La inspección del
bytecode confirma el caso de espera cero y la llamada a `clearWorkTarget`.

`MANUAL_VALIDATION_PENDING`: retirar terreno con Kohen y su herramienta real,
comparar bloques efectivamente cambiados por segundo; observar colocación,
consumo de materiales/durabilidad y tiempo del servidor con varios constructores.
El análisis de límites por código no sustituye esa medición en juego.

## Checkpoint e instalación

Código respaldado en `origin/feat/worker-speed-controls`, commit `3d1229b5db`.
El 19 de octubre a las 21:05 COT quedó instalado en el perfil `Minecolonies
Fabric` el JAR `minecolonies-fabric-1.20.1-1.0.12-port.jar`, SHA-256:
`3309ae61bb6a858939eae16ada9ec22b8ac53ad3fb83aa0c74469458cab386d7`.
El archivo del perfil coincide con el artefacto compilado y hay una sola versión
activa. Structurize 1.0.2 y Multi-Piston 0.0.3 permanecen compatibles.

Se cerró normalmente el cliente de ese perfil (PID 26792), sin terminación
forzada. La versión anterior 1.0.11, la configuración previa y el manifiesto
están en `mods/codex-backups/20261019-worker-speed-916f7793`. La configuración
del perfil ya incluye los valores predeterminados de la sección nueva.

El servidor remoto fue consultado sin modificaciones. El log de arranque
confirma que tiene MineColonies 1.0.11 / Structurize 1.0.2 / Multi-Piston 0.0.3:
solo necesita sustituir el JAR de MineColonies y reiniciar. La nueva versión
añadirá su sección de velocidad al TOML del servidor al arrancar. No se ha
validado todavía la mejora de rendimiento o productividad de 1.0.12 en juego.
