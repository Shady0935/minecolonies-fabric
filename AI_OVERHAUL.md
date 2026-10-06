# MineColonies Fabric 1.20.1 — AI Overhaul

## Contexto

Este repositorio contiene un port avanzado de MineColonies a **Fabric 1.20.1**.

El port base ya está en un estado bastante avanzado y se ha probado in-game. La funcionalidad principal funciona, incluyendo la IA existente de los colonos. El objetivo de este proyecto **NO es rehacer el port ni modernizar MineColonies en general**, sino crear una rama experimental dedicada específicamente a mejorar la IA, navegación, toma de decisiones y rendimiento de los colonos.

El repositorio es:

`https://github.com/Shady0935/minecolonies-fabric`

Antes de modificar nada:

1. Inspecciona el estado actual del repositorio.
2. Identifica la rama actual que contiene el port funcional.
3. Asegúrate de que el working tree esté limpio o comprende cualquier cambio existente antes de actuar.
4. Crea una rama nueva exclusiva para este trabajo.

Usa preferiblemente:

`ai-overhaul`

Si ese nombre ya existe, reutilízala únicamente si claramente pertenece a este proyecto y contiene el trabajo esperado. En caso contrario crea un nombre equivalente no destructivo.

**Nunca hagas este trabajo directamente sobre la rama estable/base del port.**

---

# Regla fundamental de Git

Todo el proyecto debe mantenerse respaldado continuamente en GitHub.

No acumules una enorme cantidad de trabajo local sin subir.

Debes:

- trabajar siempre en la nueva rama;
- hacer commits pequeños pero significativos;
- hacer un commit de checkpoint al terminar cada fase importante;
- hacer `push` de esos checkpoints al remoto;
- no usar `git push --force`;
- no reescribir commits ya publicados salvo necesidad extraordinaria;
- no borrar la rama base;
- no mezclar cambios no relacionados con IA;
- no destruir trabajo existente del port.

Cuando una fase alcance un estado funcional y probado, crea un checkpoint claramente reconocible.

Ejemplos:

- `checkpoint: baseline AI instrumentation`
- `checkpoint: upstream AI and pathfinding backport`
- `checkpoint: smart builder planner`
- `checkpoint: delivery routing improvements`
- `checkpoint: production worker spatial planning`
- `checkpoint: guard coordination improvements`
- `checkpoint: adaptive AI scheduler`
- `checkpoint: AI overhaul stabilization`

Haz push después de cada checkpoint.

Puedes además crear tags locales/remotos de checkpoints si resulta útil, pero los commits y la rama remota son obligatorios.

---

# Principio general

Nuestro objetivo no es simplemente hacer que los colonos "piensen más".

El objetivo es:

> **Que tomen mejores decisiones, hagan menos trabajo inútil y requieran menos navegación innecesaria.**

Una mejora de IA que multiplique brutalmente el coste por tick no es una mejora.

Busca sistemáticamente soluciones donde:

- una decisión local barata evite un pathfinding caro;
- una caché evite búsquedas repetidas;
- un evento evite polling;
- una ruta reutilizable evite recalculaciones;
- varias acciones puedan agruparse desde una misma posición;
- varios objetivos puedan procesarse espacialmente;
- información compartida evite que cada NPC calcule lo mismo individualmente.

La meta ideal es:

**mejor comportamiento + igual o mejor rendimiento.**

---

# Compatibilidad

Este proyecto debe permanecer en:

**Minecraft Fabric 1.20.1**

No conviertas el proyecto a NeoForge, Forge moderno ni otra versión de Minecraft.

Cuando estudies MineColonies upstream moderno, úsalo como referencia de comportamiento y arquitectura.

No intentes copiar ciegamente archivos actuales que dependan de APIs inexistentes en 1.20.1.

Haz **backports semánticos**:

1. entiende qué problema resuelve upstream;
2. entiende cómo lo resuelve;
3. adapta la solución correctamente a nuestra arquitectura Fabric 1.20.1;
4. mantén compatibilidad con nuestro port actual.

---

# FASE 0 — Auditoría y baseline

Antes de comenzar cambios grandes, estudia en profundidad la IA actual.

Revisa como mínimo:

- state machines;
- `AbstractAISkeleton`;
- `AbstractEntityAIBasic`;
- `AbstractEntityAIStructure`;
- `AbstractEntityAIStructureWithWorkOrder`;
- todas las implementaciones `EntityAIWork*`;
- Builder;
- Deliveryman;
- Farmer;
- Lumberjack;
- Miner;
- guards;
- CitizenAI;
- navegación;
- walk proxies;
- path jobs;
- stuck handler;
- movement handler;
- caching de bloques/chunks;
- request system donde interactúe con IA;
- tick scheduling de ciudadanos.

Identifica:

- polling redundante;
- búsquedas repetidas;
- recalculaciones de ruta;
- target switching innecesario;
- loops caros;
- scans de inventarios frecuentes;
- scans de entidades frecuentes;
- trabajo duplicado entre ciudadanos;
- decisiones que provoquen movimiento absurdo;
- código que permanezca de arquitecturas viejas y que upstream ya haya corregido.

No cambies por cambiar.

Comprende primero los flujos.

---

# Instrumentación y métricas

Antes de optimizar agresivamente, añade instrumentación suficiente para poder comparar comportamiento.

Debe ser posible medir, al menos durante desarrollo:

## General

- tiempo aproximado consumido por IA;
- tiempo aproximado consumido por pathfinding;
- path jobs creados;
- path jobs completados;
- path jobs cancelados;
- repaths;
- stuck events;
- stuck recoveries;
- distancia caminada;
- tiempo caminando;
- tiempo idle;
- tiempo trabajando;
- cambios de estado;
- cambios de objetivo.

Evita convertir la instrumentación en un problema de rendimiento.

Debe poder desactivarse o ser suficientemente ligera.

## Builder

Además:

- bloques colocados;
- bloques rotos;
- distancia caminada;
- acciones de construcción;
- cambios de `workFrom`;
- path jobs;
- acciones por minuto;
- distancia recorrida por acción;
- tiempo caminando frente a tiempo construyendo.

## Deliveryman

- entregas;
- items entregados;
- warehouse returns;
- destinos visitados;
- entregas agrupadas;
- distancia recorrida;
- distancia por entrega.

Usa esta instrumentación para validar posteriormente que las mejoras realmente ayudan.

Cuando esta base esté funcional:

**checkpoint + commit + push.**

---

# FASE 1 — Backport de mejoras modernas de IA y pathfinding

Esta fase debe ocurrir ANTES de inventar nuestro sistema nuevo.

Investiga el MineColonies upstream actual y su historial desde la época de nuestra base 1.20.1 hasta la versión moderna.

Busca específicamente commits relacionados con:

- AI;
- pathfinding;
- navigation;
- builder movement;
- worker movement;
- stuck handler;
- ladders;
- stairs;
- bridges;
- collision;
- path costs;
- heuristic;
- safe destinations;
- `EntityNavigationUtils`;
- `PathJobMoveCloseToXNearY`;
- walk proxies;
- path node handling;
- water;
- holes;
- drops;
- road walking;
- work orders.

Estudia especialmente cambios modernos equivalentes a:

- centralización de navegación mediante `EntityNavigationUtils`;
- comunicación más explícita entre AI y pathfinding;
- distinción correcta entre "llegué" y "sigo caminando";
- safe destination handling;
- reconocimiento de path jobs equivalentes para no recrearlos continuamente;
- `PathJobMoveCloseToXNearY`;
- improvements de heuristic;
- node revisiting;
- path costs;
- directional block costs;
- ladder entry;
- bridge finding;
- stuck recovery;
- evitar rutas absurdamente caras;
- prevención de caídas del Builder;
- detección correcta de huecos;
- bounding boxes;
- mejoras de seguimiento del path.

Consulta tanto código moderno como historial de Git cuando sea necesario.

## IMPORTANTE

No presupongas que todo cambio moderno debe portarse.

Clasifica cada candidato como:

- directamente aplicable;
- requiere adaptación;
- irrelevante para 1.20.1;
- incompatible;
- ya resuelto de otra manera en nuestro port.

Implementa los beneficios relevantes de forma incremental.

Compila y prueba regularmente.

No hagas un gigantesco rewrite en un solo commit.

---

# Builder moderno

Presta especial atención al Builder.

Nuestra implementación 1.20.1 utiliza mecanismos bastante primitivos para elegir posiciones de trabajo y recuperarse de fallos de navegación.

Investiga cómo upstream moderno maneja:

- `workFrom`;
- previous working position;
- `gotoPos`;
- `PathJobMoveCloseToXNearY`;
- safe work positions;
- avoiding drops;
- stuck detection;
- path invalidation;
- reutilización de posición de trabajo;
- evitar reposicionarse cuando el siguiente bloque sigue siendo alcanzable.

Backporta las mejoras modernas aplicables.

El Builder debe, como mínimo, dejar de elegir/recalcular posiciones de forma innecesariamente torpe cuando upstream moderno ya tenga una solución mejor.

---

# Checkpoint obligatorio de upstream

Cuando hayas terminado de portar las mejoras modernas que sean razonablemente aplicables:

1. compila;
2. ejecuta tests disponibles;
3. corrige errores;
4. prueba el juego/servidor si el entorno lo permite;
5. revisa logs;
6. comprueba que los ciudadanos siguen funcionando;
7. compara métricas baseline;
8. documenta brevemente qué se backporteó y qué se descartó.

Después crea un checkpoint explícito:

`checkpoint: upstream AI and pathfinding backport`

Haz commit.

Haz push.

**NO continúes al overhaul experimental hasta que este checkpoint exista en GitHub y compile correctamente.**

Este checkpoint debe servir para volver a una versión que sea básicamente:

> "MineColonies 1.20.1 Fabric + mejoras modernas de IA/pathfinding"

sin nuestras ideas experimentales posteriores.

---

# FASE 2 — Builder 2.0: Smart Work-Site Planner

Después del checkpoint upstream, mejora el Builder más allá de upstream.

Este es uno de los objetivos principales.

Actualmente un Builder tiende a pensar demasiado en:

> siguiente bloque → dónde me pongo → caminar → acción

Queremos que considere una pequeña ventana futura de trabajo.

## Objetivo

Cuando sea viable, analiza varias operaciones próximas del blueprint/StructurePlacer, por ejemplo una ventana limitada de aproximadamente:

- 16;
- 32;
- hasta 64;

operaciones relevantes.

No ejecutes esas operaciones prematuramente.

Úsalas solamente para planificación.

Busca una posición de trabajo desde la cual el Builder pueda realizar varias de esas acciones sin reposicionarse.

---

## Scoring de posiciones

Evalúa posiciones candidatas usando factores como:

### positivos

- cantidad de próximos bloques alcanzables;
- cercanía a la posición actual;
- path corto;
- suelo caminable;
- posición segura;
- buena accesibilidad;
- reutilización de posición actual;
- continuidad espacial de próximas operaciones.

### negativos

- agua;
- caída;
- salto innecesario;
- peligro;
- path caro;
- atravesar estructuras innecesariamente;
- posiciones encerrables por la propia construcción;
- necesidad probable de repath inmediato.

No busques perfección global.

Una heurística local barata es preferible a un algoritmo extremadamente caro.

---

## Regla fundamental del Builder

Si desde su posición actual puede ejecutar de forma razonable varias de las siguientes acciones:

**NO DEBE CAMINAR.**

Queremos reducir:

- `workFrom` changes;
- path jobs;
- distancia caminada;
- reposicionamientos;
- stuck events.

Mantén intacto el balance normal de:

- mining delay;
- placing delay;
- skill progression;
- experiencia;
- hambre;
- consumo de materiales;
- animaciones.

No "mejores" el Builder haciendo trampa o teleportándolo.

Debe verse más inteligente, no mágicamente más rápido.

---

## Cache del plan

No recalcules todo cada tick.

Mantén información como:

- posición de trabajo actual;
- operaciones cubiertas;
- validez del plan;
- último target;
- razones de invalidación.

Invalida cuando:

- cambie significativamente la zona de trabajo;
- el path falle;
- el Builder quede stuck;
- cambie físicamente el entorno;
- la posición deje de ser segura;
- el objetivo se aleje fuera de tolerancia;
- cambie de etapa.

---

## Builder benchmark

Compara en estructuras representativas:

- distancia total;
- cantidad de path jobs;
- `workFrom` changes;
- tiempo de construcción;
- acciones realizadas;
- CPU/pathfinding.

No sacrifiques consistencia del build order ni rompas Structurize.

Cuando sea estable:

`checkpoint: smart builder planner`

Commit + push.

---

# FASE 3 — Deliveryman / logística

Mejora la IA del Deliveryman.

MineColonies ya agrupa algunas entregas, así que estudia primero el comportamiento actual y el upstream moderno.

No reemplaces funcionalidad buena innecesariamente.

## Objetivo

Reducir viajes redundantes.

En lugar de tratar cada solicitud como un viaje aislado, agrupa tareas compatibles considerando:

- destino;
- proximidad;
- prioridad;
- capacidad de inventario;
- dependencia;
- urgencia;
- items disponibles;
- items ya transportados.

Queremos pasar, cuando sea seguro, de:

warehouse → A → warehouse → B → warehouse → A

a:

warehouse → A → B → warehouse

o:

warehouse → A con varios pedidos para A.

No necesitas resolver exactamente Traveling Salesman.

Utiliza heurísticas baratas:

- nearest useful destination;
- same destination batching;
- route insertion;
- priority weighted distance.

Nunca permitas que una optimización logística rompa el Request System.

Respeta:

- prioridad;
- ownership;
- cantidad;
- delivery completion;
- inventory safety;
- cancellation;
- concurrent delivery semantics.

Cuando sea estable:

`checkpoint: delivery routing improvements`

Commit + push.

---

# FASE 4 — Farmer, Lumberjack y otros trabajadores espaciales

Busca trabajos donde muchas acciones independientes ocurran físicamente cerca unas de otras.

Implementa planificación espacial únicamente donde tenga sentido.

## Farmer

Agrupa trabajo en una zona/campo antes de cruzar repetidamente a otro extremo.

Mantén una caché ligera de parcelas relevantes.

Prioriza algo parecido a:

- acción necesaria;
- reachability;
- distancia;
- agrupación espacial.

## Lumberjack

Mantén una caché temporal de árboles válidos/conocidos cuando sea apropiado.

Evita scans globales repetidos.

Prioriza:

- árbol válido;
- reachable;
- cercano;
- recientemente comprobado.

Una vez elegido un árbol/cluster, termina coherentemente el trabajo antes de cambiar de objetivo salvo que haya una razón real.

## Otros

Inspecciona:

- herders;
- planter;
- florist;
- fisherman;
- healer;
- undertaker;
- composter;
- producción;
- crafting jobs.

Aplica batching/caching únicamente cuando mejore comportamiento o rendimiento.

No fuerces una misma arquitectura a profesiones donde no encaje.

Cuando esta familia de mejoras sea estable:

`checkpoint: production worker spatial planning`

Commit + push.

---

# FASE 5 — Miner navigation

El Miner posee información estructural de la mina:

- levels;
- nodes;
- parent relationships;
- shafts;
- ladder locations.

Aprovecha ese conocimiento.

Evalúa si trayectos largos pueden dividirse jerárquicamente:

1. navegación lógica entre nodos de mina;
2. navegación local entre posiciones cercanas.

No hagas búsquedas volumétricas caras si ya existe un grafo lógico conocido.

Evita romper:

- shafts;
- ladder traversal;
- active nodes;
- repair;
- mining levels;
- guards patrullando minas.

Si una navegación jerárquica mejora realmente el sistema, impleméntala.

Si después de medir resulta peor o innecesariamente compleja, conserva únicamente las mejoras demostrables.

Checkpoint cuando corresponda.

---

# FASE 6 — Guards y coordinación

Investiga:

- Knight;
- Ranger;
- Druid;
- guard target scanning;
- combat AI;
- colony raid management.

Busca cálculos duplicados entre guards.

Considera implementar un índice ligero compartido por colonia para amenazas conocidas.

Ejemplo conceptual:

`ColonyThreatIndex`

con:

- entity id;
- posición;
- threat type;
- last seen tick;
- target actual si aplica;
- prioridad.

No mantengas referencias inválidas indefinidamente.

Limpia entradas expiradas.

---

## Knight

Prioriza amenazas razonablemente:

1. enemigo atacando ciudadano;
2. enemigo atacando guard;
3. raider dentro de colonia;
4. amenaza próxima.

Evita target switching innecesario.

---

## Ranger

Mejora selección de posiciones considerando:

- line of sight;
- rango ideal;
- path seguro;
- no acercarse absurdamente cuando ya tiene tiro;
- reposicionarse únicamente si es necesario.

---

## Druid

Prioriza curación de forma razonable:

- aliado crítico;
- guard en combate;
- ciudadano herido;
- resto.

Evita que varios druidas gasten continuamente decisiones caras sobre el mismo conjunto de entidades si puede compartirse/cacharse información.

Cuando sea estable:

`checkpoint: guard coordination improvements`

Commit + push.

---

# FASE 7 — Adaptive AI Scheduler

Revisa cuánto trabajo de alto nivel ejecutan los ciudadanos mientras:

- trabajan;
- caminan;
- esperan;
- están idle;
- esperan requests;
- duermen;
- están pausados.

La navegación física puede necesitar actualizarse frecuentemente, pero la toma de decisiones de alto nivel no siempre necesita hacerlo.

Implementa intervalos razonables según contexto.

Ejemplo conceptual, NO obligación exacta:

- acción activa: alta frecuencia;
- navegación estable: frecuencia media;
- waiting request: baja frecuencia o evento;
- idle: baja frecuencia.

---

# Distribución temporal

Evita que todos los ciudadanos ejecuten tareas periódicas en el mismo tick.

Usa distribución determinista basada, por ejemplo, en:

`citizenId % interval`

o mecanismo equivalente.

Queremos suavizar MSPT y eliminar spikes periódicos.

No introduzcas comportamiento no determinista difícil de depurar.

Cuando sea estable:

`checkpoint: adaptive AI scheduler`

Commit + push.

---

# FASE 8 — Reducir polling con invalidación/eventos

Busca lógica similar a:

`cada X ticks -> revisar si algo cambió`

Cuando el sistema ya sabe exactamente cuándo ocurre el cambio.

Candidatos:

- inventory changes;
- completed requests;
- building changes;
- work order changes;
- job reassignment;
- home changes;
- day/night transitions;
- threat detection;
- resource availability.

Cuando sea seguro, utiliza:

- dirty flags;
- event hooks;
- cached state;
- explicit invalidation.

Siempre conserva fallback periódico si un sistema completamente event-driven puede perder eventos por carga/descarga de chunks o estados externos.

Nunca sacrifiques robustez por pureza arquitectónica.

---

# Pathfinding avanzado

Después de estabilizar lo anterior, estudia si merece la pena introducir navegación jerárquica general.

MineColonies conoce conceptos de alto nivel:

- roads;
- waypoints;
- buildings;
- colony layout;
- mine nodes.

Para distancias grandes, evalúa:

local path
→ waypoint/road graph
→ waypoint cercano al destino
→ local path

en lugar de A* detallado sobre todo el trayecto.

No implementes esto únicamente porque suene elegante.

Mide.

Solo consérvalo si aporta beneficios claros de:

- nodos explorados;
- tiempo;
- estabilidad;
- comportamiento.

---

# Stuck recovery

Implementa una estrategia progresiva.

En vez de:

`stuck -> repath -> stuck -> repath`

prefiere algo equivalente a:

1. comprobar si puede avanzarse al siguiente nodo;
2. micro recuperación local;
3. invalidar path actual;
4. buscar posición alternativa;
5. volver a waypoint conocido;
6. ampliar presupuesto de búsqueda;
7. fallback final según comportamiento existente/configuración.

Añade cooldown/backoff cuando múltiples intentos fallen.

Un ciudadano imposible de mover no debe generar pathfinding caro continuamente.

---

# Rendimiento y caches

Para cualquier caché añadida:

documenta claramente:

- qué almacena;
- quién la invalida;
- cuándo expira;
- qué ocurre con unload/reload;
- qué ocurre al cambiar dimensión;
- qué ocurre al morir/despawn;
- qué ocurre si cambia el bloque/mundo.

Evita caches globales eternas.

Evita memory leaks.

Evita guardar objetos de Level/Entity más tiempo del necesario.

---

# Thread safety

El pathfinding de MineColonies puede involucrar trabajo asíncrono.

Nunca asumas que cualquier estructura es thread-safe.

Antes de introducir acceso concurrente:

- entiende quién escribe;
- quién lee;
- en qué thread;
- qué datos pertenecen al Minecraft server thread.

No hagas world access inseguro desde threads de pathfinding.

Mantén las mismas garantías o mejores que upstream.

---

# Filosofía de cambios

NO:

- grandes rewrites sin checkpoints;
- reemplazar todo por Brain API de Minecraft;
- meter librerías de IA externas;
- usar machine learning;
- meter dependencias innecesarias;
- cambiar versión de Minecraft;
- cambiar loader;
- degradar comportamiento vanilla de MineColonies;
- eliminar mecánicas para ganar rendimiento;
- hacer teleport hacks;
- cambiar balance de profesiones sin necesidad.

SÍ:

- heurísticas;
- planificación;
- caches;
- información compartida;
- pathfinding mejor;
- event-driven;
- batching;
- adaptive scheduling;
- mejor recovery;
- mejores decisiones.

---

# Testing

Después de cambios importantes:

1. compila;
2. ejecuta tests existentes;
3. revisa warnings relevantes;
4. inicia servidor/juego cuando sea viable;
5. revisa logs;
6. busca excepciones;
7. prueba ciudadanos;
8. compara métricas.

Casos que deben recibir atención especial:

- colonia pequeña;
- colonia grande;
- Builder construyendo edificio grande;
- Builder upgrade;
- Builder repair;
- Builder remove;
- Builder sin materiales;
- Builder recibiendo delivery;
- construcción irregular;
- desniveles;
- agua;
- ladders;
- stairs;
- bridges;
- doors;
- paths;
- minas profundas;
- deliveries múltiples;
- guards durante raid;
- chunk unload/reload;
- restart de servidor;
- citizen recall;
- cambio de job;
- muerte/despawn.

---

# Regresiones

Si una mejora experimental produce problemas:

1. identifica el checkpoint estable anterior;
2. aísla el cambio culpable;
3. revierte únicamente esa parte;
4. conserva las mejoras anteriores;
5. documenta brevemente por qué se descartó.

Precisamente por esto los checkpoints son obligatorios.

No intentes salvar eternamente una idea mala solamente porque ya consumió tiempo.

---

# Documentación interna

Mantén un documento en el repositorio, por ejemplo:

`docs/AI_OVERHAUL.md`

Actualízalo durante el proyecto.

Debe contener:

- arquitectura encontrada;
- problemas identificados;
- mejoras upstream backporteadas;
- commits upstream relevantes;
- mejoras propias;
- métricas antes/después;
- ideas descartadas;
- riesgos conocidos;
- checkpoints disponibles;
- tareas pendientes.

No conviertas el documento en un diario gigantesco.

Debe servir para continuar el trabajo posteriormente.

---

# Criterio de éxito

Al terminar queremos que un jugador perciba que:

- Builders caminan mucho menos inútilmente;
- Builders cambian menos de posición;
- ciudadanos se atascan menos;
- ciudadanos usan rutas más razonables;
- Deliverymen realizan menos viajes redundantes;
- trabajadores completan zonas antes de moverse;
- guards reaccionan de forma más coherente;
- grandes colonias generan menos spikes;
- ninguna profesión pierde funcionalidad;
- el rendimiento es igual o mejor.

Las métricas deberían corroborarlo.

---

# Autonomía

Trabaja de forma autónoma.

No te detengas constantemente para preguntar qué hacer después.

Investiga el código, upstream y el historial Git cuando haga falta.

Toma decisiones conservadoras cuando existan varias alternativas razonables.

Si una mejora resulta demasiado riesgosa, deja documentada la oportunidad y sigue con otra.

No abandones todo el goal porque una fase particular presente dificultades.

---

# Orden obligatorio

El orden general es:

1. auditoría;
2. instrumentación/baseline;
3. mejoras upstream;
4. **checkpoint upstream obligatorio**;
5. Builder 2.0;
6. Deliveryman;
7. trabajadores espaciales;
8. Miner;
9. guards;
10. scheduler;
11. polling/event invalidation;
12. optimizaciones adicionales justificadas por profiling;
13. estabilización final.

No empieces nuestros experimentos de Builder 2.0 antes de tener el checkpoint funcional del backport upstream.

---

# Estado final

Antes de considerar terminado el proyecto:

- working tree limpio;
- rama correcta;
- todos los cambios importantes commiteados;
- todos los commits pusheados;
- GitHub actualizado;
- build funcional;
- tests razonables realizados;
- logs revisados;
- documentación actualizada;
- checkpoints claros;
- comparación de baseline vs resultado;
- ninguna regresión conocida grave sin documentar.

Crea finalmente:

`checkpoint: AI overhaul stabilization`

y haz push.

El resultado debe quedar en GitHub listo para que otra sesión/agente pueda continuar exactamente desde ahí sin depender del contexto de esta conversación.