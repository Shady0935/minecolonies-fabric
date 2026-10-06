# Organización y base estable de pruebas

Limpieza del 6 de octubre de 2026. Código conservado: `4da9f1d8c9`, con la
corrección de orientación de Domum Ornamentum. La etiqueta
`port-stable-baseline-2026-10-06` identifica esta base en Git. Se considera la
primera base estable de pruebas acordada con el usuario; siguen vigentes las
limitaciones y verificaciones manuales documentadas en `KNOWN_LIMITATIONS.md`
y `NEXT_STEPS.md`.

## Ubicaciones vigentes

- `project/minecolonies` y `project/libs`: código del port y dependencias.
- `references`: fuentes upstream para comparar, sin desarrollar allí.
- `project/**/build/libs`: JAR actuales. Se conservan también `build/devlibs`
  y cachés de compilación para seguir compilando las dependencias.
- `test-instance/creative-miner-packaged-20261003`: servidor dedicado vigente
  para pruebas con los JAR empaquetados.
- `project/minecolonies/run`: ejecución de desarrollo y sus mundos actuales.
- `project/minecolonies/run-client-dedicated-styles-20261005`: última instancia
  de cliente, conservada con sus configuraciones y mundos.
- `logs`: evidencias y compilaciones; `docs`: estado y pasos pendientes.

## Checkpoints eliminados

Se eliminaron 200 carpetas de ejecuciones históricas y copias de mundos de prueba,
que ocupaban 9,041 GiB. El workspace ocupaba aproximadamente 11,873 GiB antes
de la limpieza. Se preservaron los mundos actuales de desarrollo, la instancia
empaquetada vigente, las referencias y el historial Git. El perfil externo de
CurseForge y los servidores remotos no se modificaron.

Evidencias conservadas:

- `logs/archive/20261006/historical-test-evidence.zip`: 574 logs, informes y
  configuraciones; 9,73 MiB, verificado sin errores de CRC.
- `logs/archive/20261006/cleanup-manifest.json`: rutas eliminadas, tamaños,
  commit de código y hashes SHA-256 de los JAR preservados.

Las rutas antiguas citadas en otros documentos pueden dejar de existir: buscar
su misma ruta relativa dentro del ZIP. Las copias de mundos eliminadas no están
incluidas en el archivo.

## Retención de nuevas pruebas

Reutilizar la instancia empaquetada vigente o crear instancias temporales bajo
`test-instance`. Al terminar una investigación, conservar logs y resultados,
y eliminar copias de mundos que ya no se necesiten. Evitar acumular una carpeta
completa por intento. Las carpetas `run-*` quedan excluidas de Git.
Conservar los cinco JAR vigentes y registrar sus hashes al cambiar la base
estable de pruebas.
