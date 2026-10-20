# Reintentos sin avance y diagnóstico de navegación

20 de octubre de 2026. Rama `fix/pathfinding-no-progress`.

## Evidencia del servidor

En MineColonies 1.0.18, una muestra de 500 líneas entre 16:24:55 y 16:25:01 del
reloj del log contenía 39 resultados de búsquedas con destino. De ellos, 37
devolvían un solo nodo sin alcanzar el destino: Esther 13, Ishaan 13, Langston 7
y Natalia 4. Sus posiciones no cambiaban. Las 254 líneas de pasos de rutas
ocupaban más de la mitad de la muestra. Los recorridos aleatorios se contabilizan
aparte: `canReach=false` en un paseo no demuestra por sí solo un fallo.

Natalia y Esther tenían los pies a Y=62.5 y un nodo de inicio a Y=63. Esa
representación entera es normal sobre una losa inferior y se conserva. No se
pudo inspeccionar posteriormente ese terreno en el host porque ya no estaba
cargado; no se atribuye el atasco concreto a un bloque confirmado.

## Cambios en 1.0.19-port

- Un resultado nulo o de un solo nodo, sin destino alcanzable, se rechaza como
  ruta sin progreso. No se sigue y no se presenta como llegada.
- Para el mismo destino explícito y clase de búsqueda, sin desplazamiento de
  más de medio bloque, los fallos consecutivos esperan 10, 20, 40, 80 y hasta
  100 ticks entre nuevos cálculos (0.5 a 5 segundos a 20 TPS). Los consumidores
  reciben el resultado fallido ya completado, no un futuro ficticio en progreso.
- Cambiar de destino, tipo de búsqueda o moverse más de medio bloque invalida
  la espera. Una ruta útil también limpia el historial. Los cambios del terreno
  se vuelven a evaluar como máximo al vencer la espera de cinco segundos.
- El destino sigue visible al detector de atascos durante la espera. El historial
  de fallos sobrevive a `stop()`. Las búsquedas de combate sin destino explícito
  no comparten una entrada de caché bajo una clave nula.
- Las rutas parciales que sí permiten moverse se conservan; acabar su lista de
  nodos no las convierte en llegadas al destino original.
- El seguimiento usa la altura real de la superficie de colisión inferior,
  incluso si es menor de un bloque. La holgura de cabeza considera la altura
  real del NPC, capturada al crear el trabajo, en lugar de exigir siempre dos
  bloques. Se corrigen coordenadas de consulta de superficies y la fracción X/Z
  de posiciones negativas al preparar el inicio.

## Logs

`[AI-NAV] path-summary` sustituye el par de mensajes por cada trabajo. Incluye
posición, altura de pies, origen/destino, nodos, tiempo de cálculo, tiempo desde
el envío a la cola, cálculos enviados, solicitudes diferidas y fallos consecutivos.
Se emite cada cinco segundos cuando hay resultados; un cambio de resultado puede
adelantarlo, con un máximo de uno por segundo por NPC. No es un heartbeat de
colonos inactivos. `REACHABLE` significa que la ruta calculada puede alcanzar el
destino, no que el NPC haya llegado físicamente.

```toml
[pathfinding]
pathfindingdebugverbosity = 0
citizenaidebuglogging = true
citizenaidebugcitizens = ["Esther", "Natalia"]
```

La lista acepta fragmentos de nombre sin distinguir mayúsculas o UUID exactos;
vacía incluye todos. Afecta a navegación, recuperación y logs del constructor.
Los volcados verbose también respetan la selección:

- 0: sin volcados de búsquedas/nodos; los resúmenes de IA son independientes.
- 1: resumen de búsqueda, sin una línea por nodo.
- 2–10: nodos y exploración detallada, solo para investigación puntual.

Se conserva la opción original por compatibilidad. El archivo del servidor
`config/minecolonies-server.toml` se cambió de verbosity 10 a 0 y se verificó su
lectura; los demás ajustes se conservaron. Requiere reinicio. El esquema nuevo
contiene 94 opciones de servidor (86 originales y ocho del port). Los comentarios
ingleses del port ahora tienen además una fuente rastreada en
`src/main/translations/en_us`, independiente de los assets descargados.

## Entrega y alcance de comprobación

Solo cambia MineColonies a 1.0.19-port. Structurize 1.0.4, Multi-Piston 0.0.5,
BlockUI 0.0.2 y Domum 1.0.2 siguen siendo las dependencias de esta entrega.
Se compila el JAR remapeado y se comprueban metadatos, traducciones y hashes de
instalación. La suite de 149 GameTests/12 unitarios pertenece a 1.0.18: no se
presenta como una ejecución de este parche. No se añaden nuevas pruebas en esta
entrega. El contador del probe de esquema existente se actualiza de 93 a 94.

La reducción real de búsquedas y el movimiento de estos colonos quedan pendientes
de observar con el nuevo JAR en el servidor. La modificación del TOML no cambia
la configuración en memoria hasta reiniciar. Los datos de instalación y respaldo
se registran al completar la entrega en `logs/20261020-ai-nav-profile-install.json`.
