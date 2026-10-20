# Kohen: bucle ante un bloque superior (1.0.13)

19 de octubre de 2026. Rama `fix/builder-block-face-reach`, basada en
`feat/worker-speed-controls` (`fdf373328e`). Conserva los ajustes de velocidad.

## Evidencia del servidor, sin cambios al mundo

El arranque confirma MineColonies 1.0.12, Structurize 1.0.2, Multi-Piston 0.0.3,
y los multiplicadores 2x con revisión de minado cada tick. La consulta de
ciudadano 2, colonia 1, devuelve Kohen Y. Gordon en
`WORKING / BUILDING_STEP`, habilidades 99.

La muestra de logs de 02:13–02:14 UTC del 20 de octubre (21:13–21:14 COT del
19 de octubre) repite cada 100 ticks:

- origen `(-666,68,236)`;
- objetivo `(-666,74,237)`;
- ruta de un solo nodo, destino no alcanzado;
- `work-target-unreachable`, sin avances de objetivo en la muestra.

La posición precisa consultada es `(-665.543754958563,68,236.65284992521006)`.
Los bloques de pies y cabeza son aire, el soporte inferior no es aire y el
objetivo superior es aire: está intentando colocar un bloque, no minar allí.
No se reinició la IA, teletransportó al ciudadano ni editó ningún bloque.

## Causa y corrección

El alcance compartido por el planificador y `PathJobMoveCloseToXNearY` usaba
distancia desde los ojos al centro del objetivo. Con la altura de ojos normal
del adulto, el centro de ese bloque queda a unos 5,07 bloques del centro del
nodo de apoyo, pero su cara inferior queda dentro del alcance de cinco bloques
(aproximadamente 4,48 desde la posición precisa observada).
El constructor rechazaba ese trabajo y solicitaba otra ruta idéntica.

Ahora el cálculo mide la distancia mínima desde los ojos al volumen de la celda
objetivo. La búsqueda usa el centro inferior del nodo; el constructor usa sus
coordenadas reales cuando evalúa dónde está parado. Ambos conservan la misma
regla de alcance de cinco bloques, el límite horizontal Manhattan y la exclusión
de la misma columna cuando el trabajador está a la altura del bloque o encima.

La seguridad de la posición actual comprueba la caja real del trabajador, sin
desplazarla artificialmente al centro de su celda. Las posiciones futuras siguen
comprobando soporte, fluidos y colisiones. Los diagnósticos de ruta fallida añaden
altura de pies, altura de ojos y seguridad de la posición actual.

No se aumentan multiplicadores ni presupuestos de búsqueda, no se omiten bloques
del esquema y no se cambia el mundo para sortear el fallo. Un objetivo realmente
fuera de alcance sigue necesitando una posición accesible; esta corrección no
crea accesos, escaleras o andamios.

## Entrega y límites

Solo cambia MineColonies a **1.0.13-port**. Dependencias: Structurize 1.0.2 y
Multi-Piston 0.0.3. Se compila con `remapJar --console=plain`, sin añadir ni
ejecutar pruebas automatizadas. La evidencia de logs se conserva localmente en
`logs/kohen-face-reach-baseline.log`, excluida de Git.

`MANUAL_VALIDATION_PENDING`: instalar 1.0.13 en el servidor y comprobar que
Kohen supera `(-666,74,237)`, continúa su orden y no repite esa ruta de un nodo.
El diagnóstico geométrico no equivale a haber verificado la recuperación en juego.
