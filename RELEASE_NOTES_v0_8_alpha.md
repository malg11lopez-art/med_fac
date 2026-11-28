# Notas de la versión — MEDIEVAL FACTIONS v0.8.0-alpha

## Resumen

v0.8 añade una capa de simulación para atacar las nueve áreas pendientes sin declarar completada una característica que todavía no tiene integración visual o prueba in-game. Se amplía el dominio, los buckets persistentes, las conexiones de comandos y el enlace con algunas entidades/inventarios reales.

## Nuevas capas de dominio

- **Habitantes:** horarios diarios, actividades (dormir, comer, trabajar, socializar, entrenar, guardia, viaje y médico), planes priorizados por supervivencia, enlaces ciudadano-entidad y búsqueda A* 3D acotada.
- **Ciudades:** parcela urbana, distritos, límites, detección de solapamiento, coste de material, fases de construcción y vínculo entre edificio planificado y obra física.
- **Guerra táctica:** unidades, formaciones, órdenes de movimiento/ataque/bloqueo/esquiva/reagrupación/retirada, turnos, columnas militares y ocupación.
- **Dragones:** genoma, herencia con semilla, huevo, incubación, eclosión y cría persistente enlazada a una entidad Companion/Dragon.
- **Familia/medicina:** propuesta y respuesta de matrimonio, disolución, casos médicos, camas, doctor y tratamiento que consume medicina real.
- **Aventura:** convoyes por waypoints, escrow, emboscadas, liquidación, expediciones con ruta física limitada y encuentros de jefe con fases/telegráficos. El progreso de ruta ahora depende de la llegada real de todos los NPC al waypoint; los días consumen provisiones y resuelven riesgos, pero no cambian la posición por sí solos.

## Integración de servidor

- Se añaden buckets SavedData para horarios/planes/enlaces de residentes, ciudades y enlaces de construcción, batallas/columnas/ocupaciones, genomas/huevos/crías, matrimonios/casos/clínicas, convoyes físicos, encuentros y expediciones.
- `/mf advanced npc spawn` materializa un residente para un ciudadano activo; `/npc schedule` guarda sus cuatro destinos básicos y `/npc plan` procesa decisiones diarias.
- `/mf advanced city plan/build/list` crea parcelas y coloca `small_house` o `watchtower` usando inventario existente.
- `/mf advanced caravan physical/resume/list-physical` despacha convoyes con stock y escrow, reanuda emboscadas y lista estados.
- `/mf advanced companion genome/breed/incubate/hatch` expone cría y eclosión de dragones; eclosionar crea una entidad Dragon.
- `/mf advanced family marriage propose/answer/divorce` añade proceso matrimonial; `clinic register/open/treat` exige doctor, camas y poción real.
- `/mf advanced expedition route` mueve NPC previamente materializados hacia un destino cargado, limitado a la dimensión actual.

## Correcciones y protecciones

- El plan urbano solo se inicia desde edificios planificados, no desde fases ya activas.
- El trabajo urbano reserva materiales una vez y se activa cuando acaba la colocación física.
- El tratamiento médico exige cama, médico activo, medicina física, paciente del asentamiento y disponibilidad; el consumo y la recuperación se procesan en servidor.
- Los viajes físicos tienen estado terminal y no deben volver a gastar provisiones por un doble avance.
- Se amplían pruebas para rutas, llegada, medicina, condiciones inseguras, liquidación y estados terminales.

## Límites que permanecen

- No hay GUI gráfica nueva.
- A* 3D no está conectado al `PathNavigation` del residente; la navegación que mueve la entidad es la navegación terrestre vanilla con waypoint.
- No hay carro visible, guardias de caravana, soldados visibles ni batalla táctica conectada a entidades.
- El motor de jefe no está unido todavía a un boss entity único de mazmorra.
- Las órdenes autónomas de vuelo y vida salvaje de dragones están pendientes; el vuelo implementado es pilotado por el jugador.
- Las nueve áreas no están verificadas in-game.
- No hay JAR: la compilación real NeoForge no pudo ejecutarse en el entorno de preparación.

## Validación

- Núcleo: 136 aserciones.
- Sistemas avanzados: 74 aserciones.
- Expansión de simulación: 49 aserciones.
- Total: **259 aserciones superadas**.
- Smoke test SavedData con stubs: PASS, con cobertura limitada.
- Recursos: 106 JSON, 14 rigs, 75 SoundEvents y 75 OGG; 0 errores y 0 advertencias.
- Parser Java (`javac` sin classpath): sin diagnósticos de sintaxis; resolución de tipos y compilación requieren dependencias reales.
