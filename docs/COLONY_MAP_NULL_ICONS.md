# Crash al abrir el mapa del ayuntamiento (1.0.17-port)

## Evidencia

El crash report del perfil de CurseForge
`crash-2026-10-20_02.21.09-client.txt` registra un `NullPointerException` en
`View.removeChild`: `child` era null. La llamada procede de
`WindowColonyMap.addCitizens` (línea 393 de 1.0.16), durante el constructor
abierto por `WindowMainPage.mapButtonClicked`.

Al añadir por primera vez un ciudadano, el mapa intentaba retirar el icono
anterior con `citizens.get(data)`, aunque todavía no estaba en esa caché.
Al actualizar edificios existía una segunda llamada equivalente: dentro de
la rama `uiBuilding == null` se intentaba retirar ese mismo icono inexistente.
Tras corregir solo el primer caso, el segundo habría causado otro crash al
inicializar los edificios durante el cambio de escala.

El log también contiene respuestas HTTP fallidas de la consulta de features
del ayuntamiento. No son la excepción fatal: el stack del crash identifica
la creación de iconos y no pasa por ese hilo de consulta.

## Corrección

- Ciudadanos: guardar el icono nuevo y retirar el anterior solo cuando existe.
  La sustitución de iconos conserva su comportamiento.
- Edificios: quitar la retirada imposible dentro de la creación inicial;
  las actualizaciones posteriores siguen reutilizando el icono existente.
- No se cambia globalmente `BlockUI.View.removeChild`, ni se oculta la
  excepción con un catch. El cambio corrige las dos llamadas inválidas del mapa.

Solo MineColonies cambia a 1.0.17-port; Structurize 1.0.3, Multi-Piston 0.0.4,
Domum 1.0.2 y BlockUI 0.0.1 mantienen versiones y requisitos compatibles.

`MANUAL_VALIDATION_PENDING`: abrir el ayuntamiento y su mapa con ciudadanos
cargados y edificios; acercar y alejar el zoom, cerrar y reabrir. El crash y
la revisión de ambas rutas establecen la causa; la recuperación visual debe
confirmarse con el cliente actualizado. No se añaden ni ejecutan tests.

## Compilación e instalación

`remapJar --console=plain`: BUILD SUCCESSFUL en 3 min 31 s, con los avisos
existentes sobre APIs obsoletas. Código respaldado en GitHub, rama
`fix/colony-map-null-icons`, commit `e462e4448b`.

El 20 de octubre a las 02:26 COT se instaló MineColonies 1.0.17 en el perfil
`Minecolonies Fabric`. Se comprobaron metadatos, versiones de dependencias,
una sola copia activa y SHA-256 igual al del JAR de distribución:
`f4f36efcb06998178a0e0800325fec6ec38142a1b16f67863cd91e4469c9f1c2`.

La versión anterior 1.0.16, copia de la config y manifiesto quedaron en
`mods/codex-backups/20261020-colony-map-219312aa`. La configuración conserva
su hash. No había un proceso de Minecraft de ese perfil que cerrar.
Domum 1.0.2, Structurize 1.0.3 y Multi-Piston 0.0.4 conservan sus hashes.

No se modificó el servidor ni se cambió el protocolo de red. El arreglo se
ejecuta en la interfaz del cliente; sigue pendiente abrir el mapa con el nuevo
JAR para confirmar en juego que funcionan sus iconos y el zoom.
