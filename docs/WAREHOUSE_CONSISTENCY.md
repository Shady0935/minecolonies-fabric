# Almacenes: comparación de reparación y estantes dobles

19 de octubre de 2026. Rama `fix/warehouse-consistency`, basada en
`fix/builder-work-reach` (`cfda39ace1`). Conserva el overhaul y las correcciones
de alcance de 1.0.10. La rama estable `version/main` permanece aparte.

## Diagnóstico con datos del servidor

El almacén en `(-695,70,227)` está guardado como construido (`isBuilt=1`),
nivel 5, esquema `craftsmanship/storage/warehouse5.blueprint`, estilo Lost Mesa
City. El chat del cliente registra la finalización de Kairi a las 04:57:49 COT.
El botón Reparar existe para cualquier edificio de nivel superior a cero,
incluso terminado; el color rojo del nombre está fijado en
`layouthutpageactionsminwoinv.xml`. Ninguno de esos elementos indica daños.

La lista de materiales sí tiene falsos positivos:

- La comparación de Structurize trataba como parte del esquema todo el NBT de
  los bloques que implementan la interfaz de texturas de Domum. Las cabañas y
  los estantes también implementan esa interfaz, con cero componentes. La
  cabaña original contiene colonia 0, posición del autor y versión 0; en el
  servidor tiene colonia 1, su posición real y versión 2. No son reparaciones.
- El esquema incluido conserva en las losas de tejas una textura antigua para
  `minecraft:block/acacia_planks`. La losa actual solo declara los componentes
  `oak_planks` y `dark_oak_planks`; su carga descarta el componente antiguo.
  Las lecturas de varias losas del tejado muestran precisamente esos dos
  componentes actuales con los materiales correctos. La losa en `(-700,80,221)`
  coincide también en orientación, forma y agua con el esquema.
- El predicado que debía omitir el bloque central de cabaña y los bloques
  protegidos de la estimación estaba combinado con `false`, anulándolo.

Estas comprobaciones identifican causas reproducibles en los datos y el código;
no equivalen a haber localizado en el mundo las dos unidades concretas que
mostraba esa captura, ni garantizan que todo bloque del edificio esté intacto.

Los estantes de `(-693,70,221)` y `(-692,70,221)` presentan un fallo real:
ambos son `variant=blockrackempty`, orientados este/oeste entre sí. Cada uno
dibuja el modelo doble completo y produce la superposición de la captura.
El esquema original contiene una mitad visible y otra `blockrackair`.
Ambos inventarios tienen 54 ranuras y multiplicador 3; no deben reemplazarse.
Todas las consultas remotas fueron de lectura, sin modificar bloques ni NBT.

## Cambios

- Structurize compara únicamente los componentes de textura declarados por el
  bloque actual, manteniendo la comparación de sus materiales. Ignora datos
  operativos de cabañas/estantes y componentes de textura obsoletos. Los estados
  diferentes siguen requiriendo reparación salvo una regla explícita del
  manejador; la regla existente de estantes puede ahora ejecutarse antes del
  rechazo general por interfaz de texturas.
- La estimación de construcción vuelve a aplicar su predicado de bloques que
  el constructor no debe tocar. Su manejador acepta las variantes dinámicas de
  estantes igual que el constructor del servidor.
- Al conectar un estante individual a una mitad doble se elige la variante
  complementaria. Antes siempre se elegía otra mitad visible, dependiendo del
  orden de colocación del esquema.
- La colocación y un tick programado al cargar cada estante normalizan los
  pares dobles recíprocos con dos mitades visibles u ocultas. Se elige una mitad
  visible de forma determinista y se conserva cualquier pareja ya válida.
  Solo cambian variantes del mismo bloque: se mantienen las entidades de bloque,
  inventarios, capacidad y propietario. Se invalida la vista de inventario
  combinada en caché para que refleje la pareja corregida.
- La normalización no genera ni carga chunks vecinos. Si la pareja cruza un
  límite, el tick del otro chunk permite atenderla cuando esté cargado. No hay
  exploración continua ni reparación de estantes individuales o no recíprocos.

## Conjunto de entrega

- MineColonies **1.0.11-port**, requiere Structurize 1.0.2 y Multi-Piston 0.0.3.
- Structurize **1.0.2-port**.
- Multi-Piston **0.0.3-port**, sin cambios de lógica, acepta Structurize
  1.0.0, 1.0.1 o 1.0.2. Compilación y ZIP de servidor referencian la cadena nueva.

Sustituir los tres JAR en cliente y servidor. BlockUI y Domum conservan sus
versiones. Los estantes existentes se corrigen al cargar la zona con la nueva
versión; no hace falta romperlos ni pedir reconstruir el edificio.

## Verificación y límites

Compilación y empaquetado mediante `remapJar --console=plain` por módulo.
No se añaden ni ejecutan pruebas automatizadas en esta corrección.
`MANUAL_VALIDATION_PENDING`: comprobar el par señalado y otros estantes tras
reiniciar, verificar acceso a ambos inventarios, reabrir las opciones del almacén
y observar una colocación nueva con estantes dobles. La estimación se hace en
el cliente y depende de sus chunks/datos recibidos; no es un certificado de
integridad de todo el edificio.

Los logs de compilación y las lecturas de NBT se conservan localmente bajo
`logs/`, excluidos de Git. La instalación del perfil y los hashes de entrega se
registran al terminar el empaquetado.
