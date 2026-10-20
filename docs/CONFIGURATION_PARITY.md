# Restauración de configuración y auditoría del port

## Alcance y estado

Trabajo iniciado el 20 de octubre de 2026, rama `fix/configuration-parity-audit`.
La configuración anterior conservaba valores predeterminados en memoria y solo
persistía las opciones de IA añadidas. La implementación nueva registra el árbol
completo: las 86 opciones originales de servidor de MineColonies y las siete de
IA del port, para un total de 93 opciones de servidor,
5 de cliente, 2 comunes y 15 de servidor de Structurize.
Las expresiones originales de definición, incluidos predeterminados y límites,
coinciden para las 86 opciones de servidor, las cinco de cliente y las dos comunes.

La base de comparación es el Forge 1.20.1 fijado en `UPSTREAM_MAPPING.md`,
`b6987ade75e796dcbab573cdee0144ad54d1da12` para MineColonies. Las referencias
locales del workspace estable se leen sin modificarlas. La rama estable no se
modifica. La comprobación final de la entrega se registra al final de este documento.

## Persistencia

Un motor tipado compartido en Structurize conserva las rutas y claves originales,
valores predeterminados, límites, booleanos, enteros, largos, decimales finitos,
enumeraciones y listas. Usa NightConfig core/toml 3.8.4, empaquetados dentro de
Structurize, para admitir TOML real: listas multilínea, escapes y comentarios.

- `config/minecolonies-server.toml`, `minecolonies-common.toml` y, en cliente,
  `minecolonies-client.toml` se generan y cargan de verdad.
- Structurize genera y carga `config/structurize-server.toml`.
- Una configuración de servidor ya existente en `world/serverconfig/` prevalece
  para ese mundo. Si no existe, se usa el archivo global de Fabric en `config/`.
  No se crea una segunda copia silenciosa que invalide futuras ediciones del global.
- Se conservan las secciones desconocidas. `[ai_work_speed]` y las opciones de
  depuración existentes pasan a pertenecer al mismo árbol tipado y sincronizado.
  Las modificaciones de migración/corrección
  generan una copia `.before-fabric-<id>.bak` antes de reemplazar el archivo.
- Valores inválidos individuales se registran y restauran al predeterminado.
  Un error de sintaxis TOML detiene la carga con un error claro, preservando el
  archivo y los valores vivos anteriores. La escritura usa reemplazo atómico
  cuando el sistema de archivos lo permite.
- Los comentarios, valores predeterminados y límites se generan para cada opción.
- Las ediciones requieren reinicio; no se promete recarga automática en caliente.
- Las copias de seguridad de colonias incluyen el archivo efectivo de configuración,
  también cuando procede de `config/` en lugar de `world/serverconfig/`.

## Autoridad del servidor

Cada mod tiene un canal S2C dedicado, con esquema versionado y tamaño acotado.
Al conectar, el servidor envía los valores efectivos de servidor/comunes. El cliente
valida el conjunto completo antes de aplicarlo en memoria y conserva sus preferencias
exclusivas de cliente. No se registran receptores C2S para alterar configuración.
Un snapshot inválido se rechaza; no se guarda en los archivos del cliente.

Al desconectar se restauran los valores locales. En servidor integrado no se
reaplica el snapshot por red porque cliente y servidor comparten los objetos.
Al cerrar un mundo se restablecen los valores globales para el siguiente mundo.

## Comparación de fuentes y adaptadores

`tools/audit_port_parity.py` compara inventarios, hashes normalizados de fuentes y
recursos y candidatos de adaptación de eventos. Incluye las fuentes de cliente
separadas de Multi-Piston. La comparación comprende 15.848 archivos originales
entre los cinco módulos. El inventario no ejecuta las referencias.

| Módulo | Original | Port | Idénticos | Cambiados | Ausentes por ruta |
|---|---:|---:|---:|---:|---:|
| MineColonies | 14.734 | 14.922 | 14.086 | 641 | 7 |
| BlockUI | 102 | 105 | 71 | 31 | 0 |
| Structurize | 518 | 534 | 424 | 94 | 0 |
| Domum Ornamentum | 471 | 476 | 416 | 55 | 0 |
| Multi-Piston | 23 | 25 | 5 | 11 | 7 |

Los siete ausentes de MineColonies son metadatos/transformadores de Forge, tres JSON de loot
modifiers sustituidos por hooks Fabric y dos texturas con nombres inválidos/duplicados.
No faltan fuentes Java de MineColonies. En Multi-Piston, dos ventanas se trasladaron
a `src/client/java`, los eventos de ciclo de vida se sustituyeron por los entrypoints
Fabric y las tres abstracciones de red Forge por `MultiPistonNetwork` y su receptor
de cliente. El séptimo archivo es `mods.toml`, sustituido por `fabric.mod.json`.
Los métodos vacíos o constantes se revisan por su contrato y consumidores; el escaneo
no equivale a una prueba de paridad funcional.

Se revisaron los 75 suscriptores detectados y los 155 archivos del adaptador de
MineColonies. Los cuatro candidatos sin constructor directo quedan explicados:
el evento base de interacción recibe sus subtipos; `EntityInteract` recibe el
subtipo específico; JourneyMap emite su radar mediante su propio registro v2;
y la recarga Forge se sustituye por la carga Fabric al arrancar. No hay vigilancia
automática del archivo: las ediciones de configuración requieren reinicio.

Las clases base con resultados constantes conservan los contratos usados por
este port: registro anticipado, proveedores explícitos, validación de inventario
permisiva por defecto y extensiones de cliente opcionales. Esto no constituye una
implementación pública de todas las APIs de Forge ni de inventarios arbitrarios
que solo expongan Fabric Transfer API sin un `Container` o proveedor compatible.

Hallazgos ya corregidos en el código de trabajo:

- Configuración completa de MineColonies y Structurize, antes no persistida.
- Adaptador de brewing vacío: usa ahora `PotionBrewing` y permite registrar recetas
  adicionales; el descubrimiento incluye la adaptación vanilla.
- Evento de decoración de ítems sin consumidor: conectado a `GuiGraphics`, para el
  contador de peticiones de la tablilla en inventario/hotbar.
- Evento de recetas sincronizadas sin emisión: conectado al paquete vanilla de
  actualización de recetas, para actualizar los caches de recetas del cliente.
- Finders de categoría de recetas registrados pero descartados: aplicados a la
  clasificación del libro vanilla; composting conserva UNKNOWN como en el original.
- Decoraciones de BlockUI: delegan de nuevo a la ruta vanilla completa para
  conservar contador, durabilidad, cooldown y decoradores personalizados.
- Reflexión con nombres de desarrollo sobre Minecraft: sustituida por referencias
  remapeadas y access wideners para elaboración, escalones, explosiones, generadores,
  colmenas, tickets y navegadores activos. La consulta de mundo de cliente usa un
  puente propio cuyo interior también se remapea.
- Cancelación de eventos: respeta prioridad y `receiveCanceled`; los observadores
  de pruebas que necesitan ver cancelaciones lo declaran explícitamente.
- Inventarios por cara: conserva ranuras, restricciones de hornos, límite de una
  botella y simulación sin mutación. Las vistas `LazyOptional.cast()` comparten
  ahora la invalidación del proveedor original.
- La caché de proveedores usa referencias débiles para los contenedores y sus
  handlers, conservando la identidad mientras el handler siga en uso. Los
  proveedores de mundo se retiran después del guardado final del servidor y al
  desconectar el cliente. Los bloques retirados no exponen un nuevo inventario.
- Consulta de chunks: reconoce el cache inmediato de acceso vanilla antes de la
  lista de holders, evitando rechazar un chunk cargado en el mismo tick.
- Enlaces de `/mc help`: vuelven a ser clicables para las URLs HTTP/HTTPS usadas.
- ZIP del servidor: la dependencia de empaquetado de Fabric API deja de resolver
  módulos transitivos sueltos. El JAR oficial ya contiene sus módulos de runtime;
  así se evitan duplicados y la inclusión del módulo de GameTests en distribución.

El error observado de JourneyMap en el perfil procede del entrypoint de Waystones
que exige `journeymap.client.api.IClientPlugin`, no del plugin v2 de MineColonies.
Se registra como incompatibilidad del perfil externo; no se modifica Waystones.

## Verificación y entrega

Doce pruebas JUnit cubren persistencia, migración, validación, snapshots, restauración
local e invalidación de capacidades. Nueve GameTests adicionales comprueban los
esquemas reales, TOML, autoridad remota, brewing vanilla, inventarios, cancelación,
accesos remapeados, visibilidad inmediata de chunks y reutilización/caducidad de
los proveedores de inventario.

La primera pasada completa ejecutó 144 casos: 142 pasaron. Las repeticiones
detectaron interferencia entre fixtures y observadores que no declaraban
`receiveCanceled`. Los ciclos largos se aislaron fuera del terreno de otras pruebas,
esperando a que sus chunks puedan simular entidades; se conservan las exigencias
de navegación, consumo exacto de almacén, entrega y finalización de obra.

La pasada final `configuration-parity-verified-suite.log` terminó con **149/149
GameTests obligatorios aprobados**, guardado de las tres dimensiones y salida
correcta. La compilación final y las **12/12 pruebas JUnit** también terminaron
correctamente. El XML final y el inventario comparativo están en `docs/audit/`.

Los nueve probes nuevos pasaron además en el servidor con JAR remapeado. La
repetición manual de los ciclos largos detectó un error del lanzador de pruebas:
consultar el heightmap de una zona aún no cargada enterraba el fixture cuatro
bloques. Se precargó la zona de origen. El límite de recogida del mensajero se
comprueba con `CitizenConstants.DEFAULT_RANGE_FOR_DELAY` (4), igual que el original,
en lugar del radio 3 que exigía antes la prueba. No se cambió la distancia del NPC.

El cliente de producción usa Minecraft 1.20.1, Fabric Loader **0.19.5**, los cinco
JAR de distribución y Fabric API 0.92.2. Arrancó, conectó por loopback a la instancia
aislada, recibió recetas/configuración y salió normalmente. La comprobación de
memoria por JDK JDI evaluó únicamente getters, con estos resultados:

| Valor | Conectado al servidor | Restaurado al desconectar |
|---|---:|---:|
| Máximo de ciudadanos | 351 | 250 |
| Multiplicador de IA | 2.0 | 1.5 |
| Operaciones de Structurize | 1000 | 1500 |
| Debug de peticiones | false | true |
| Voces, preferencia exclusiva de cliente | false | false |

Los hashes de los cuatro TOML del cliente fueron idénticos antes y después de
la conexión, y se volvieron a comprobar tras cerrar el cliente. El resultado
estructurado es `logs/configuration-parity-network-verified.json`. Una lectura
adicional del depurador, ya desconectado, rechazó correctamente la expectativa
de valores remotos porque encontró los locales restaurados; se conserva ese
intento separado en los logs. La instancia dedicada reinició con las cuatro
colonias de prueba conservadas y se detuvo normalmente después de la conexión.

Versiones de entrega: MineColonies **1.0.18-port**, Structurize **1.0.4-port**,
Multi-Piston **0.0.5-port** y BlockUI **0.0.2-port**. Domum permanece en **1.0.2-port**.
NightConfig core/toml 3.8.4 se incluyen solamente dentro de Structurize.
Es necesario actualizar juntos los cuatro JAR cambiados en cliente y servidor.
El perfil CurseForge `Minecolonies Fabric` ya contiene esos cuatro JAR, una sola
versión activa por mod y hashes idénticos a los artefactos comprobados. Los cuatro
JAR anteriores y los TOML existentes se guardaron en
`mods/codex-backups/20261020-config-parity-d98a4b8f`.
La migración del perfil/servidor ocurre al arrancar la nueva versión; no hay que
reemplazar a mano los ajustes existentes por una plantilla.

El ZIP final contiene exactamente seis JAR (los cinco mods y Fabric API), sin
módulos sueltos de desarrollo. Se extrajo en una instancia nueva de Fabric Loader
0.15.11/Java 17, con `online-mode=true`, perfiles seguros, enlace exclusivo a
loopback, puerto automático y RCON desactivado. Alcanzó `Done`, guardó las tres
dimensiones y salió con código 0. No se conectó una cuenta autenticada a esa
instancia. Los hashes del ZIP y de sus JAR, resultados de pruebas y entrega están
conservados en `docs/audit/2026-10-20-configuration-parity-verification.json`.
El servidor remoto de Tatnik no se actualizó en esta entrega; el perfil contiene
los cuatro JAR que se deben subir juntos. La rama de trabajo está respaldada
en GitHub; `version/main` no recibe estos cambios.

La comparación de archivos y una suite automatizada no certifican todas las
combinaciones de esquemas, profesiones e integraciones opcionales en una partida.
JEI/JourneyMap y las comprobaciones visuales pendientes conservan esa distinción
en `KNOWN_LIMITATIONS.md`. Los recursos fuente del Nether con mayúsculas ya existen
en upstream y Minecraft los ignora; esta entrega no los presenta como un arreglo visual.
