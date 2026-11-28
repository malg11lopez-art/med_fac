# Matriz de cobertura de mecánicas — MEDIEVAL FACTIONS v0.9.1-alpha

Esta matriz describe el código que existe en esta entrega y, por separado, la experiencia que todavía no está implementada o no ha sido verificada dentro de Minecraft. **Un motor de dominio, un registro SavedData o un comando administrativo no equivalen por sí solos a una mecánica completa.**

| Área solicitada | Lo que sí está implementado en v0.9 | Conexión con el mundo | Lo que falta para considerarla completa | Estado honesto |
|---|---|---|---|---|
| IA de habitantes | Planes diarios, prioridades de supervivencia, horarios persistidos, asignación inicial de recetas, cosecha física, comidas y socialización inicial. | Residentes usan navegación vanilla a waypoints; agricultores cosechan/replantan y acreditan stock+XP; EAT consume una ración real y modifica necesidades; SOCIALIZE mejora satisfacción de dos residentes del mismo asentamiento. Las acciones tienen cooldown persistente, sonido/partículas y GameTests. | A* propio aún no conectado a `PathNavigation`; falta transporte de materiales, inventario individual, construcción/reparación/cocina/comercio/patrulla autónomas, memoria social, amistades/hostilidad, personalidad, vivienda y LOD. | **Parcial; ya hay trabajo de campo, comida y socialización reales, pero no IA integral.** |
| Interfaz gráfica | Panel rápido en tecla O con ocho páginas, comandos sugeridos, consulta familiar personal y dashboard de gestión. | El dashboard global requiere operador nivel 2 y limita peticiones; la consulta familiar se filtra por UUID del jugador. El panel O sigue siendo una capa de consulta/atajos y no un contenedor de inventario. | `MenuType`/`AbstractContainerMenu` en servidor, `Screen` en cliente, sincronización de datos, widgets de inventario/árbol/mapa/gestión, navegación y permisos; pruebas multijugador. | **Panel rápido implementado; GUI integral pendiente.** |
| Construcción de ciudades | Límites de parcela, distritos, detección de solapamientos, reserva de materiales, dos planos físicos (`small_house`, `watchtower`) y enlace de progreso de obra con el plan urbano. | Los proyectos colocan bloques por lotes, usan el motor físico existente y no deben sobrescribir terreno no reemplazable. | Editor visual, proyección fantasma, catálogo modular, carreteras/parcelas zonificadas, selección de terreno, nivelación, servicios urbanos, edificios con BlockEntity/inventario/menú y asignación de uso real. | **Prototipo de parcela + dos edificios, no generador de ciudad.** |
| Habilidades y combate táctico | Seis habilidades iniciales más elemental_burst y shadow_blink conectadas a acciones del servidor; pasivas iniciales, afinidades/resistencias; motor táctico determinista con órdenes MOVE/ATTACK/BLOCK/DODGE/RALLY/RETREAT, formaciones y turnos. | El combate de habilidades actúa sobre entidades vivas. Los registros de batalla táctica, columnas militares y ocupaciones pueden persistirse mediante API interna. | Catálogo de habilidades por las 15 clases y subclases, UI de árbol/barra, recursos sincronizados, estados de bloqueo/esquiva/combos, animaciones, creación/gestión de batallas desde comandos/juego, validación de alcance/cobertura y conexión a entidades físicas. | **Habilidades iniciales reales; táctica solo motor de dominio.** |
| Caravanas y economía autónoma | Comercio por días existente; convoy físico de waypoints, reserva previa de stock, escrow del comprador, emboscadas deterministas, pérdida parcial y liquidación bilateral/reembolso. | El convoy sigue coordenadas/waypoints y se procesa diariamente; comandos permiten despachar, reanudar y listar. No existe entidad de carro visible. | Carro/guardias como entidades, navegación y carga física, NPC comerciantes que negocien, precios por oferta/demanda, salarios/impuestos/consumo conectados, cámara de compensación bancaria, producción y rutas de mercado autónomas. | **Logística simulada sobre waypoints, sin caravana física visible ni mercado autónomo.** |
| Ejércitos, guerra y conquista | Simulación estratégica de asedios existente; motor táctico con unidades abstractas, turnos, formaciones, columnas, raciones, moral y ocupación persistente. | Las unidades tácticas siguen siendo abstractas; los `MFGuardEntity` que ya existen ahora atacan monstruos hostiles por IA melee, pero todavía no se materializan como unidades de ejército ni siguen órdenes de formación. | Reclutamiento, equipo/suministro individual, entidades soldado, formación en terreno, movimiento/colisión, órdenes en GUI, máquinas de asedio, daños de murallas, conquista con transición de propiedad, resistencia/ocupación, tratados y multijugador. | **Simulación y reglas tácticas abstractas; guerra visual pendiente.** |
| Dragones | Genomas con rasgos heredables, dos progenitores registrados, huevos, incubación por calor/cuidado, eclosión y vínculo persistente dragón-compañero. El comando de eclosión genera una entidad dragón real. Vuelo manual ya existe para monturas aéreas. | Huevo, genoma, eclosión y companion se persisten; la entidad nace en el mundo del propietario. La escala visual juvenil es provisional. | Vuelo autónomo/pathfinding aéreo, órdenes autónomas realmente ejecutadas, hábitat, búsqueda de comida, patrulla y defensa autónomas; crecimiento visual/atributos por etapas, temperatura ambiental, incubador físico, animaciones de huevo/eclosión, reproducción no administrativa y balance de herencia. El dragón sin jinete ya tiene defensa melee básica contra monstruos. | **Ciclo genético básico persistente; autonomía dracónica incompleta.** |
| Familias y medicina | Personas/relaciones con consentimiento, matrimonio, gestación, nacimiento/linaje, clínicas y casos médicos; consulta familiar personal; socialización inicial y comidas con necesidades reales. | SOCIALIZE aumenta satisfacción de ambos residentes del mismo asentamiento; EAT consume una ración y reduce hambre/restaura salud. `/mf advanced family status` solo muestra registros del jugador solicitante. | Memoria social, amistades/hostilidad, relaciones autónomas, ceremonias, embarazo/parto visual, educación, diagnóstico y tratamiento por IA y edificios médicos con interfaz. | **Mecánicas iniciales conectadas; sistema familiar/social completo pendiente.** |
| Mazmorras, jefes y expediciones | Mazmorras físicas deterministas con salas/cofres/encuentros iniciales; motor de jefe con fases y acciones telegráficas; expediciones abstractas con recursos/riesgos y viajes de grupo con waypoints. | Expedición física exige NPC residentes ya materializados y cargados en una sola dimensión; el servidor actualiza sus destinos y consume provisiones por día. Un ciudadano no puede quedar reservado en dos expediciones activas simultáneas, y se libera al completar/fallar. Los jefes del motor táctico no están unidos a un jefe único físico de mazmorra. | Jefes únicos con entidad/IA/habilidades/loot propio, fases sincronizadas con el encuentro real, puzzles/llaves/trampas, pathfinding multiwaypoint robusto, chunks/dimensiones, combate y retorno físico de los NPC, recuperación/rescate y GameTests. | **Mazmorra física básica + ruta física limitada de NPC; jefes y aventura completa pendientes.** |

## Puertas que faltan para cada área

1. Compilación real con la matriz exacta de Minecraft/NeoForge/GeckoLib.
2. Pruebas de integración con un mundo real: carga/descarga de chunks, guardado, reinicio y migración de datos.
3. Pruebas de cliente y servidor dedicado con dos o más jugadores.
4. Revisión de rendimiento con población, rutas, convoyes y batallas simultáneas.
5. Pruebas de aceptación funcional en juego, no solo aserciones de dominio.

## Criterio de estado

- **Implementado en dominio:** reglas deterministas cubiertas por pruebas unitarias propias.
- **Persistido:** datos se guardan en registros del SavedData avanzado; todavía debe probarse serialización real de esos nuevos tipos en Minecraft.
- **Conectado al mundo:** hay entidad/bloque/inventario/ubicación real que participa en el flujo.
- **Verificado in-game:** solo se marca después de `runClient`/`runServer`, pruebas de mundo y persistencia real. CI #90 compila NeoForge, ejecuta cinco GameTests (incluyendo NBT real y reanudación de producción) y arranca servidor dedicado/cliente headless con una conexión real. Esto acredita esos contratos de integración, no una partida manual ni una evaluación visual/auditiva humana.

## Cambios transversales de v0.9.1-alpha

- El scheduler de servidor mantiene el contador de ticks y minuto por instancia de `MinecraftServer`; no comparte estado entre mundos integrados.
- La carga/guardado NBT de registros avanzados aplica límites por bucket, registro y presupuesto total; los registros individuales corruptos se descartan sin abortar toda la carga.
- El guardia y el dragón sin jinete tienen combate melee básico contra monstruos; esto no constituye IA militar ni vuelo autónomo.
- Las pruebas de dominio suman 276 aserciones. La cobertura NBT real sigue siendo representativa, no exhaustiva para todos los tipos; la sesión de dos clientes y el profiling de población alta siguen pendientes.


### Cambios transversales verificados en CI #90

- Skins: sincronización servidor-cliente, validación de índices 0..85, persistencia de jugador, carga al entrar/respawnear y limpieza al desconectar. Falta prueba de dos clientes y aplicación a NPC.
- Audio: 75/75 eventos referenciados por keyframes o runtime; los OGG se validan, pero falta escucha humana y ajuste de mezcla.
- Habilidades: elemental_burst y shadow_blink integradas con coste/cooldown y validación en servidor.
- Residentes: consulta indexada de ciudadano por UUID y asignación diaria de recetas con reserva de inventario; GameTest valida deduplicación y rechazo de trabajos sin materiales. Falta estación física/IA de recolección y profiling de carga real.
- Interfaz: panel rápido de tecla O; no equivale a GUI de gestión.


## Validación adicional de CI run 46024843978

- 12 GameTests NeoForge superados.
- Cosecha y XP, socialización bilateral, rechazo entre asentamientos y comidas con consumo de raciones.
- Validación de 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG.
- Build NeoForge, JAR, servidor dedicado, cliente headless y conexión real cliente-servidor.
- Textura civil de residente incluida en la validación de recursos.

La CI acredita los flujos anteriores, no una revisión visual/auditiva humana ni la finalización de las verticales.
