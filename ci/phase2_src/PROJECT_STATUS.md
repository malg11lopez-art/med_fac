# Estado del proyecto — MEDIEVAL FACTIONS v0.9.1-alpha

## Resumen de versión

v0.9 es una actualización de corrección e integración incremental; no declara completo el mod.

v0.8 añade una capa común de simulación para las nueve áreas que seguían abiertas en v0.7: IA de habitantes, construcción urbana, táctica, convoyes, ejército abstracto, dragones, familia/medicina, jefes y expediciones. Parte de los flujos ya afecta a entidades, inventarios y ubicaciones reales. No se presenta como mod completo ni como build certificado.

## Cambios específicos de v0.9

- Runtime de ticks/minutos aislado por instancia de `MinecraftServer`.
- Expediciones físicas con exclusividad de miembros y liberación al completar o fallar.
- Límites defensivos para registros NBT avanzados y tolerancia a registros individuales corruptos.
- Guardias y dragones sin jinete con combate melee básico contra monstruos hostiles.
- CI configurada para publicar el JAR como artefacto solo si el build de Gradle y las pruebas pasan.

Estos cambios estabilizan y amplían la base; no completan la simulación social, las interfaces gráficas, las ciudades integrales ni la guerra visual.

## Integraciones adicionales de v0.9.1-alpha

- Sincronización servidor-cliente de 86 skins con persistencia por jugador y manejo de login/logout/respawn; falta prueba real de dos clientes y aplicación a NPC.
- 75/75 sonidos referenciados por keyframes o acciones runtime; la calidad auditiva humana aún necesita prueba manual.
- Habilidades elemental_burst y shadow_blink, ray tracing de objetivos con obstáculos y panel rápido en tecla O.
- Búsqueda indexada de ciudadanos por UUID para evitar recorrer toda la población desde cada residente.

## Sistemas heredados de v0.8

- **Habitantes:** horarios diarios, directivas de supervivencia y navegación terrestre vanilla. El residente inicia recetas iniciales solo al alcanzar un bloque físico compatible (mesa de crafteo, forja/yunque u horno según receta); la orden reserva materiales y avanza en el ciclo diario. A* 3D sigue como motor puro sin conexión a `PathNavigation`; siguen pendientes recolección/transporte autónomo, uso interactivo de estaciones/contenedores, tareas de campo e IA social.
- **Ciudad:** parcelas, distritos, validación de límites/solapamiento, reserva de materiales y enlace al proceso físico de construcción. Los blueprints reales siguen limitados a `small_house` y `watchtower`.
- **Guerra táctica:** unidades abstractas, órdenes MOVE/ATTACK/BLOCK/DODGE/RALLY/RETREAT, formaciones, turnos, columnas logísticas y ocupación persistente. No crea soldados visibles todavía.
- **Caravanas:** convoyes físicos simulados por waypoints, mercancía y escrow reservados, emboscadas, carga perdida, reanudación y liquidación bilateral. No hay carro ni escolta como entidades.
- **Dragones:** genoma, herencia determinista, huevos, incubación, eclosión y enlace a Companion/entidad Dragon. Las crías todavía no tienen crecimiento visual completo ni IA aérea autónoma.
- **Familia:** propuesta/respuesta de matrimonio, divorcio y reglas de relación/consentimiento. Se conservan gestación, nacimiento y linaje.
- **Medicina:** clínicas ligadas a asentamiento, DOCTOR activo, camas y pociones reales; los tratamientos consumen una poción y la simulación diaria contempla alimentación/seguridad.
- **Expediciones:** rutas por waypoints que dirigen NPC materializados en la dimensión actual. El waypoint solo avanza cuando todos los integrantes cargados alcanzan físicamente su destino; la simulación diaria consume provisiones y resuelve riesgos.
- **Mazmorras/jefes:** motor de encuentros con fases y telegráficos. La mazmorra física sigue usando encuentros básicos; el boss abstracto no está enlazado todavía a una entidad de jefe única.

## Integraciones y comandos nuevos

- `/mf advanced npc spawn|schedule|plan|list`
- `/mf advanced city plan|build|list`
- `/mf advanced caravan physical|resume|list-physical`
- `/mf advanced companion genome|breed|incubate|hatch`
- `/mf advanced family marriage propose|answer|divorce`
- `/mf advanced family clinic register|open|treat`
- `/mf advanced expedition route`

Los parámetros exactos y precondiciones se describen en `README_ES.md`.

## Base heredada

- Minecraft Java 1.21.1, NeoForge 21.1.267, Java 21, GeckoLib 4.9.4.
- Perfiles, razas, clases, profesiones, árboles data-driven, calendario, facciones, asentamientos, economía, producción, banca, préstamos, comercio y diplomacia inicial.
- Recursos y entidades prototipo de versiones anteriores.

## Límites que siguen abiertos

- GUI gráfica real (pantallas y menús cliente-servidor).
- Agentes NPC completos: pathfinding de mundo integrado con A*, selección autónoma de trabajo, tareas de campo, relaciones y vida social.
- Constructor de ciudad integral, editor fantasma, servicios y edificios especializados.
- Árbol completo de habilidades y táctica conectada a soldados visibles.
- Carros, escoltas, mercado autónomo y economía global.
- Ejércitos visibles, máquinas de asedio y captura de territorio jugable.
- Vuelo autónomo dracónico, crecimiento visual y reproducción natural con hábitat/temperatura.
- Matrimonio y medicina desde interacciones normales de NPC, embarazo/parto visual y vida familiar completa.
- Jefes físicos únicos sincronizados con su motor de encuentros y expediciones robustas con chunks/dimensiones y rescate.

## Validación ejecutada — CI #50

- tools/run_domain_tests.sh: 276 aserciones superadas.
- tools/run_saveddata_stub_tests.sh: PASS, smoke test limitado a un subconjunto de SavedData.
- python tools/validate_assets.py: 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG; 0 errores/advertencias.
- Sincronización de skins: 86/86 texturas y 14 checks estáticos PASS.
- gradle clean test build: PASS.
- NeoForge GameTests: 5/5 PASS en CI #90, incluyendo ciclo real de NBT/producción y planificación diaria de trabajo autónomo.
- Descriptor del JAR medieval_factions-0.9.1-alpha: PASS.
- Arranque de servidor dedicado y cliente headless con recarga de recursos: PASS.

## No verificado manualmente

- Partida de Minecraft con dos clientes, skin remota visible y sincronización durante cambios de dimensión.
- Escucha humana de los 75 sonidos, mezcla, espacialización y sincronía real.
- Partidas históricas reales de cada versión anterior, reinicios de mundo y migraciones entre versiones publicadas; CI #126 sí cubre un registro por cada uno de los 31 buckets avanzados y la recuperación de cuatro clases de entrada corrupta.
- Rendimiento prolongado y profiling con población alta.

Dictamen: v0.9.1-alpha compila y arranca en CI y ya contiene nuevas integraciones de gameplay; no implementa todavía todas las verticales del Documento Maestro ni se considera release final.

## Validación adicional — CI #90

- Cinco GameTests NeoForge ejecutados en el servidor de pruebas.
- La persistencia real de NBT conserva facciones, perfiles, asentamientos, ciudadanos y órdenes activas.
- Tras recargar, una orden de carpintería reanuda el trabajo y entrega el producto una sola vez.
- La simulación diaria asigna una receta al residente con profesión compatible solo cuando hay materiales; no duplica órdenes ni crea salidas sin ingredientes.
- El build, el arranque del servidor y cliente, y el smoke test de conexión/sincronización multijugador pasan.

Estos checks cubren la capa de simulación y persistencia. No certifican todavía la interacción de trabajadores con estaciones físicas ni el comportamiento social autónomo.


## Validación adicional — CI #126

- Ocho GameTests NeoForge pasan en el servidor de pruebas.
- Un registro de cada uno de los 31 buckets avanzados sobrevive a un round-trip mediante CompoundTag real y Gson, conservando UUID y contenido.
- Un JSON inválido, un JSON de más de 32.000 caracteres, una entrada sin UUID y una entrada con UUID duplicado se descartan sin perder el registro válido vecino.
- Se mantienen los tests de producción que reanudan una orden tras NBT, asignación de trabajo, llegada a estación física y verificación de acciones de gestión.
- El build, el arranque del servidor y cliente, y el smoke test de conexión/sincronización multijugador pasan; el JAR se publica como artefacto de CI.

La cobertura por registro no sustituye todavía pruebas de volúmenes máximos, presupuesto agregado de 64 MB, interrupción durante guardado ni migración de mundos publicados anteriores.
