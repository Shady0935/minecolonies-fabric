# Restauración de configuración y auditoría del port

## Alcance y estado

Trabajo iniciado el 20 de octubre de 2026, rama `fix/configuration-parity-audit`.
La configuración anterior conservaba valores predeterminados en memoria y solo
persistía las opciones de IA añadidas. La implementación nueva registra el árbol
completo: 88 opciones de servidor de MineColonies (incluidas las dos de debug),
5 de cliente, 2 comunes y 15 de servidor de Structurize.

La base de comparación es el Forge 1.20.1 fijado en `UPSTREAM_MAPPING.md`,
`b6987ade75e796dcbab573cdee0144ad54d1da12` para MineColonies. Las referencias
locales del workspace estable se leen sin modificarlas. La rama estable no se
modifica. La auditoría y la validación están todavía EN CURSO.

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
- Se conservan las secciones desconocidas, incluidas `[ai_work_speed]` y las
  opciones de depuración existentes. Las modificaciones de migración/corrección
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

## Auditoría complementaria en curso

`tools/audit_port_parity.py` compara inventarios, hashes normalizados de fuentes y
recursos y candidatos de adaptación de eventos. Primera pasada: 14.734 archivos
originales, 14.918 del port; 14.087 idénticos, 640 diferentes, 7 ausentes y 191 nuevos.
No faltan fuentes Java. Son cifras de la primera pasada, no del árbol final.

Los siete ausentes son metadatos/transformadores de Forge, tres JSON de loot
modifiers sustituidos por hooks Fabric y dos texturas con nombres inválidos/duplicados.
Los métodos vacíos o constantes se revisan por su contrato y consumidores; el escaneo
no equivale a una prueba de paridad funcional.

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

El error observado de JourneyMap en el perfil procede del entrypoint de Waystones
que exige `journeymap.client.api.IClientPlugin`, no del plugin v2 de MineColonies.
Se registra como incompatibilidad del perfil externo; no se modifica Waystones.

## Evidencias provisionales

Diez pruebas JUnit de persistencia, validación y snapshots pasaron antes y después
de compartir el motor con Structurize. Se agregaron cuatro GameTests para las
definiciones reales, migración, autoridad remota y conversiones vanilla de pociones.
La suite Minecraft y la comprobación final de empaquetado/cliente siguen pendientes.
No se debe presentar el mod como completamente verificado mientras falten esas fases.
