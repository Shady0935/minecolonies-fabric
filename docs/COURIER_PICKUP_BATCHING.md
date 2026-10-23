# Recogidas del repartidor — 1.0.20-port

## Evidencia del servidor (22 de octubre de 2026)

Nova N. Cortez, colonia 1, ciudadano 3, alternó PICKUP en el restaurante
(-657,70,287) y START_WORKING junto al almacén (-695,70,227).
El guardado nativo `world/data/minecolonies_colonies.dat`, actualizado mediante
`save-all`, tenía una sola tarea PickupRequest en su cola y ninguna entrega.
Su inventario estaba vacío en ese guardado. No se interpreta una sola captura
como prueba de todas las idas y vueltas descritas por el usuario.

La consulta directa de los seis estantes y del bloque de restaurante encontró
solo carne cocinada y carbón en un estante; los otros inventarios estaban vacíos.
BuildingCook conserva la comida admitida y el combustible cuando recoge el
repartidor. Sin embargo, descargar productos disparaba una solicitud sin comprobar
si existía algo que este pudiera retirar. Eso permite viajes sin carga útil.

Importante: el archivo legacy `world/minecolonies/minecraft/overworld/colony1.dat`
es una importación antigua, no el estado vivo del port Fabric. Las observaciones
actuales usan el SavedData nativo y comandos de lectura del servidor.

## Cambio

- Las recogidas automáticas de prioridad inferior a 10 comprueban el inventario
  combinado de cabaña y contenedores, usando las mismas reglas de retención del
  repartidor y una lista temporal compartida entre slots. No extraen nada.
- Si hay excedentes, se abre una ventana fija de 400 ticks (20 s a 20 TPS) por
  edificio para acumular producción. Nuevas descargas suben la prioridad pero no
  prolongan el plazo. Solo se genera una solicitud al vencerlo.
- Se reevalúan existencias al despachar. Si ya no queda excedente o hay una
  recogida abierta, no se duplica la solicitud.
- La prioridad y el plazo pendientes se guardan con el edificio y sobreviven al
  reinicio. El gestor existente solo procesa edificios en chunks cargados.
- Prioridad 10 o superior (almacén lleno, máxima prioridad o acción manual)
  conserva despacho inmediato y sustituye cualquier ventana pendiente.
- Las entregas ya agrupan solicitudes del mismo origen/almacén y destino según
  habilidad y capacidad. No se aumentan cantidades solicitadas ni se retira
  comida del restaurante para llenar artificialmente la mochila.

## Alcance

Compilación de distribución `remapJar -x test`; no se ejecuta la suite ni se añaden
pruebas en esta entrega. Pendiente observar la reducción efectiva de viajes con
1.0.20 en el servidor. Una recogida ya encolada en 1.0.19 puede terminar su viaje.
El margen añade hasta 20 segundos más la cadencia del tick de colonia a recogidas
normales; no garantiza grandes cargas si la producción real es escasa.

Se entrega el JAR al perfil CurseForge con copia de seguridad, manteniendo los
mods compañeros y la configuración de debug. El despliegue remoto se informa
por separado; compilar o instalar en el perfil no actualiza el host.

Entrega completada: JAR de 72.487.678 bytes, SHA-256
`d3a4ff16fc6b73a0bbfcf83e13e5f742b0339ef706f6121d7e45dd4c4c869128`.
`remapJar -x test` finalizó correctamente. Perfil actualizado con una sola versión
activa, dependencias y hash comprobados; Minecraft se cerró normalmente.
Anterior y TOML respaldados en `mods/codex-backups/20261022-courier-batch-e64bb202`.
No se modificó la configuración ni se desplegó el JAR al servidor.
Registro: `audit/2026-10-22-courier-pickup-batching-delivery.json`.
