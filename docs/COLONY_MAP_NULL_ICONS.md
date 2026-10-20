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
