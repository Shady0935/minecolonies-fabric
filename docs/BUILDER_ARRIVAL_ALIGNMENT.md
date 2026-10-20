# Kohen: precisión de llegada a la posición de trabajo (1.0.14)

19 de octubre de 2026. Rama `fix/builder-arrival-alignment`, basada en
`fix/builder-block-face-reach` (`731840d584`). Conserva el alcance a caras y
los multiplicadores de trabajo de las versiones anteriores.

## Lecturas del servidor

El arranque confirma 1.0.13, dependencias vigentes y multiplicadores 2x.
Kohen sigue en `WORKING / BUILDING_STEP`, pero cambió de posición y objetivo:

- Celda actual: `(-670,69,232)`; posición precisa:
  `(-669.1879572112301,69,232.5695413723932)`.
- Objetivo vacío: `(-674,74,232)`.
- Altura de ojos registrada: 1,53; `currentSafe=true`.
- La ruta devuelve un nodo con `reachesDestination=true`.
- El constructor registra después `work-target-unreachable`, cada 100 ticks.

La muestra de 02:24–02:25 UTC del 20 de octubre corresponde a las 21:24–21:25
COT del 19 de octubre. A las 02:29 UTC, el objetivo anterior `(-666,74,237)`
ya no era aire; el objetivo nuevo sí. El bloqueo actual es distinto al anterior.
Las consultas fueron de lectura: no hubo movimientos forzados, reinicios de IA
ni modificaciones del mundo.

## Causa

La 1.0.13 comparaba la posición real del constructor con un nodo de ruta
representado por el centro de su celda. El centro de esta celda queda a unos
4,93 bloques de la celda objetivo; la posición real queda a unos 5,15. El
desplazamiento al centro sería de unos 32 cm. La ruta aceptaba el nodo inicial
sin mover al colono y la comprobación del trabajo rechazaba su ubicación real.

La corrección anterior de alcance a la cara era necesaria para el primer
objetivo, pero dejó esta diferencia entre llegada teórica y llegada física.

## Corrección

- `PathJobMoveCloseToXNearY` captura la posición precisa de los pies al crear el
  trabajo, junto con la altura de ojos. El nodo inicial solo cuenta como destino
  alcanzado si esa posición real está dentro de alcance. No se accede a la
  entidad desde el hilo de búsqueda para esta comprobación.
- Si centrar ligeramente al constructor dentro de su celda permite trabajar,
  se solicita ese desplazamiento al controlador normal de movimiento. Se
  mantiene su altura actual y el límite de cinco bloques. La orden es de caminar,
  sin teletransporte ni cambio directo de velocidad física o posición.
- El ajuste exige estar en suelo, no estar en fluido, soporte transitable en el
  destino y ausencia de colisiones en toda la caja barrida por el cuerpo. Solo
  cubre el centro de la celda actual, como máximo unos 0,71 bloques en horizontal.
- Cada intento dura como máximo 20 ticks. Si no alcanza una posición válida,
  cede a la búsqueda ordinaria y deja 40 ticks antes de reintentar ese ajuste.
  Los cambios de objetivo/celda y los reinicios del plan limpian ese estado.
- El constructor solo permite colocar cuando la posición real cumple el alcance.
  No se aceptan bloques pendientes como terminados para salir del bucle.
- El verbose añade `work-position-align`, y los fallos muestran posición precisa,
  `centeredReach` y `actualReach`, para distinguir otro problema de llegada de un
  destino realmente inaccesible.

## Entrega y verificación pendiente

Solo cambia MineColonies a **1.0.14-port**; Structurize 1.0.2 y Multi-Piston
0.0.3 permanecen compatibles. Compilación con `remapJar --console=plain`.
No se añaden ni ejecutan pruebas automatizadas. La evidencia local está en
`logs/kohen-arrival-alignment-baseline.log`, excluida de Git.

`MANUAL_VALIDATION_PENDING`: sustituir el JAR del servidor y comprobar que Kohen
camina esos centímetros o busca otra posición, coloca `(-674,74,232)` y continúa.
Confirmar que no alterna indefinidamente entre rutas alcanzadas y posiciones
rechazadas. La recuperación de la nueva versión todavía no está verificada en
juego; los logs de 1.0.13 identifican el desacuerdo, no validan el arreglo.

## Compilación e instalación

`remapJar --console=plain` terminó correctamente en 2 min 15 s, con los avisos
existentes sobre APIs obsoletas. Se inspeccionaron el manifiesto, las dependencias
activas y la presencia de la captura de posición inicial y del ajuste local en
las clases compiladas. Código respaldado en `origin/fix/builder-arrival-alignment`,
commit `39e96649b6`.

El 19 de octubre a las 21:31 COT se instaló el JAR de MineColonies 1.0.14 en
el perfil de CurseForge, con SHA-256 igual al de distribución:
`b6e944efc026eaac51ec67afd46aab41de54ff1311138d2694a193cee8e4a27b`.
Hay una sola versión activa; Structurize 1.0.2, Multi-Piston 0.0.3 y la config
de velocidad permanecen como estaban.

Minecraft de ese perfil (PID 29736) cerró normalmente. No fue necesario forzarlo
ni detener otros procesos Java. La versión anterior y el manifiesto están en
`mods/codex-backups/20261019-builder-alignment-5088773f`.
El servidor remoto sigue en 1.0.13; solo necesita sustituir MineColonies y reiniciar.
