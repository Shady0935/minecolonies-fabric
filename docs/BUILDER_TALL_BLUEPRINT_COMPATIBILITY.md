# Constructor: compatibilidad con esquemas altos (1.0.15-port)

## Diagnóstico del 19 de octubre de 2026

En el servidor de pruebas con 1.0.14, Kohen (colonia 1, ciudadano 2) repetía
`work-target-unreachable` desde los pies (-666.7741,68,236.6264), intentando
trabajar en (-667,75,236). Su posición era segura, pero el bloque quedaba
fuera del alcance 3D de cinco bloques incluso contando su altura de ojos 1.53.
La ruta de un nodo no podía resolver esa diferencia real de altura.

La versión estable del port, commit 2964454e155a3b43ffe28330e2204431f14bf81f,
usaba `BlockPosUtil.getDistance2D` en `walkToConstructionSite`: permitía trabajar
en altura desde una posición cercana horizontalmente. El overhaul introdujo
una restricción 3D que rompió esa compatibilidad. No es una prueba de un fallo
en la colisión de las escaleras de la captura.

## Cambio

- El constructor vuelve a permitir alcance vertical dentro de los límites
  transformados del esquema cargado para su obra activa, dentro de la altura
  válida del mundo. Conserva el límite horizontal Manhattan de cinco bloques
  y la exclusión de trabajar en su propia columna a la altura de sus pies o debajo.
- El objetivo y la puntuación de búsqueda usan la misma regla horizontal en
  este modo; la búsqueda conserva colisiones físicas, soporte caminable,
  posición inicial real y comprobaciones de seguridad del constructor.
- Las demás profesiones y destinos fuera del esquema conservan el alcance 3D.
  No se alteran recursos, herramientas, durabilidad ni los multiplicadores 2x.
- No se añaden andamios ni escalada automática. El modo permite trabajar en
  plantas superiores desde el suelo, siguiendo el comportamiento anterior del port.

En `config/minecolonies-server.toml` se genera al iniciar:

```toml
[ai_work_speed]
builderverticalreachcompatibility = true
```

Se puede establecer en false y reiniciar para exigir alcance físico 3D; los
esquemas altos necesitarán entonces accesos y posiciones de trabajo adecuadas.
Las opciones de velocidad existentes se conservan.

## Entrega

MineColonies 1.0.15-port, con Structurize 1.0.2-port y Multi-Piston 0.0.3-port.
Compilación mediante `remapJar --console=plain`, sin añadir ni ejecutar tests.

`MANUAL_VALIDATION_PENDING`: actualizar MineColonies en el servidor y reiniciar;
comprobar que Kohen coloca el objetivo de Y=75 y sigue avanzando en la columna
alta, sin repetir el mismo fallo cada 100 ticks. El diagnóstico del servidor
identifica la regresión; no demuestra todavía la recuperación con 1.0.15.
