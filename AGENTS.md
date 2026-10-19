# Entregas al perfil de pruebas

Instrucción permanente del usuario (19 de octubre de 2026): después de generar
versiones nuevas de los mods de este proyecto, actualizar siempre el perfil
de CurseForge `Minecolonies Fabric` antes de terminar la entrega.

- Carpeta: `C:\Users\Shady\curseforge\minecraft\Instances\Minecolonies Fabric\mods`.
- Instalar los JAR de distribución de `build/libs`, junto con las dependencias
  que hayan cambiado; comprobar versiones y dependencias de los JAR finales.
- Guardar los JAR sustituidos en una subcarpeta de `mods/codex-backups` y dejar
  una sola versión activa de cada mod. Conservar los demás mods del perfil.
- El usuario autoriza cerrar Minecraft si está abierto e impide actualizar.
  Identificar el proceso de Minecraft de este perfil, intentar el cierre normal
  y, si es necesario, terminar solo ese proceso. No pedir otra confirmación ni
  detener procesos Java ajenos, servidores o Gradle.
- Comprobar los hashes de los archivos copiados e informar las versiones que
  quedaron instaladas. El usuario recoge de ese perfil los JAR para el servidor.

Mantener los cambios de desarrollo respaldados en GitHub sin modificar la rama
estable `version/main`.
