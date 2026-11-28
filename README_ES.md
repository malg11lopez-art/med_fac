# MEDIEVAL FACTIONS — Alpha v0.9.1

**Plataforma objetivo:** Minecraft Java 1.21.1 · NeoForge 21.1.267 · Java 21  
**Animaciones:** GeckoLib 4.9.4  
**Namespace:** `medieval_factions`

## Estado de esta entrega — v0.9.1-alpha (CI #50)

La compilación CI #50 valida build NeoForge, GameTests, arranque de servidor dedicado, cliente headless y recarga de recursos. Sigue siendo una alpha: estas pruebas no equivalen a una partida manual ni a una certificación audiovisual/multijugador completa.

- 276 aserciones de dominio superadas.
- 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG validados: 0 errores/advertencias.
- 86 skins con sincronización servidor-cliente; pendiente probar dos clientes reales y aplicación a NPC.
- Habilidades elemental_burst y shadow_blink, selección de objetivos con ray tracing de entidades y búsqueda indexada de residentes por UUID.
- Panel rápido en tecla O; conecta comandos existentes, pero no sustituye la GUI de gestión completa.

La matriz exacta por mecánica está en SYSTEM_COMPLETION_MATRIX.md. La arquitectura está en SIMULATION_ARCHITECTURE.md, las pruebas en TEST_MATRIX.md y el plan restante en NEXT_IMPLEMENTATION_PLAN.md.

## Novedades de v0.9

- El scheduler de ticks y cambios de minuto ahora es independiente por instancia de MinecraftServer; evita que mundos integrados compartan intervalos.
- Los miembros de una expedición física no pueden quedar reservados por dos viajes activos a la vez; los miembros se liberan al completar o fallar.
- La lectura/escritura avanzada NBT limita registros por bucket, tamaño por registro y presupuesto total JSON para reducir el riesgo de datos corruptos excesivos.
- Los guardias adquieren monstruos hostiles y atacan con navegación melee; la animación de corte acompaña el golpe.
- Los dragones sin jinete priorizan defensa/ataque contra monstruos hostiles y reproducen la animación de mordida; al ser montados se desactiva su persecución melee.
- La CI adjunta el JAR compilado como artefacto solo cuando todas las puertas de build pasan.

## Historial: novedades de v0.8

### Habitantes y horarios

- Horarios persistentes con actividades de dormir, comer, trabajar, socializar, entrenar, guardia, viaje, descanso y visita médica.
- Los planes diarios priorizan supervivencia sobre trabajo.
- Puedes materializar un residente de un ciudadano existente y guardar waypoints para sus actividades.
- La entidad usa `PathNavigation` terrestre vanilla. El A* 3D añadido es todavía una utilidad de dominio y no está conectado al navegador del mundo.

### Ciudades y construcción

- Planes urbanos con límites, distritos, control de solapamiento y reserva de materiales.
- Edificios enlazados con el motor de colocación por lotes.
- Solo se incluyen físicamente `small_house` y `watchtower`; no es un generador completo de ciudades.

### Táctica, ejército y conquista

- Motor de turnos, órdenes MOVE/ATTACK/BLOCK/DODGE/RALLY/RETREAT, formaciones, columnas logísticas y ocupación abstracta.
- Sigue faltando materializar unidades como soldados visibles, las interfaces de órdenes y la conquista jugable.

### Caravanas y economía

- Convoy con ruta por waypoints, reserva de stock, escrow, emboscada, reanudación y liquidación bilateral/reembolso.
- Es una simulación logística persistente; no se crea un carro visible ni escolta como entidades.

### Dragones

- Genoma heredable, huevos, incubación, eclosión y vínculo persistente con el compañero.
- La eclosión genera una entidad dragón real. El vuelo existente es pilotado por jugador; no hay autonomía aérea completa.

### Familias y medicina

- Propuesta/respuesta de matrimonio, divorcio, medicina y casos persistentes.
- Registrar clínica requiere asentamiento, médico activo y camas. Tratar exige stock real de `medieval_factions:healing_potion` y consume una poción.
- No hay todavía una red social autónoma de NPC ni GUI hospitalaria.

### Mazmorras y expediciones

- Mazmorra física determinista, salas/cofres/encuentros iniciales y motor de jefe por fases.
- Expediciones pueden mover residentes materializados/cargados hacia un waypoint en la dimensión actual; no es aún un grupo de aventureros completo con jefes únicos conectados.

## Comandos v0.8

Los comandos `advanced` que modifican estado del mundo requieren normalmente nivel de operador (2). Los argumentos UUID se copian de las respuestas de comandos de facción, asentamiento, ciudadano, compañero o sistema.

### Habitantes

```text
/mf advanced npc spawn <citizen_uuid>
/mf advanced npc schedule <citizen_uuid> <home_x home_y home_z> <work_x work_y work_z> <food_x food_y food_z> <clinic_x clinic_y clinic_z>
/mf advanced npc plan
/mf advanced npc list
```

Los cuatro bloques deben estar cargados y en la dimensión actual. El horario de prueba define sueño, comidas, trabajo, socialización y visita médica. Para mover NPC de expedición, deben existir entidades cargadas vinculadas a los ciudadanos. El itinerario solo avanza al waypoint siguiente cuando todos los residentes del grupo han llegado físicamente.

### Planes urbanos

```text
/mf advanced city plan <settlement_uuid> <min_x> <max_x> <min_z> <max_z>
/mf advanced city build <plan_uuid> <building_id> <small_house|watchtower> <x> <y> <z> <rotation_0_to_3>
/mf advanced city list <plan_uuid>
```

La posición se valida contra los límites de la parcela y el inventario. Coloca el edificio en espacio libre; la obra se ejecuta por lotes. Es una herramienta administrativa, no una interfaz de construcción para supervivencia.

### Caravanas físicas simuladas

```text
/mf advanced caravan physical <seller_uuid> <buyer_uuid> <item_id> <quantity> <escrow_copper> <escort_strength> <route_risk> <seed> <dest_x dest_y dest_z>
/mf advanced caravan resume <convoy_uuid>
/mf advanced caravan list-physical
```

La posición actual del jugador es el origen; el destino debe estar cargado y en la misma dimensión. La mercancía se reserva del vendedor y el escrow del comprador. El trayecto se avanza por días. No aparece una entidad de carro.

### Dragones: genética y ciclo de huevo

```text
/mf advanced companion genome <dragon_uuid> <element> <vitality> <agility> <flame> <intelligence> <temperament>
/mf advanced companion breed <parent_a_uuid> <parent_b_uuid> <seed>
/mf advanced companion incubate <egg_uuid> <warmth_0_to_100> <care_0_to_100>
/mf advanced companion hatch <egg_uuid>
```

Los dos progenitores deben ser dragones adultos vivos, propiedad del mismo jugador y con genomas registrados. Incubar actualiza calor/cuidado del huevo; el tiempo de simulación avanza la incubación. La eclosión crea una entidad dragón y registra la cría.

### Familia y matrimonio

```text
/mf advanced family register <FEMALE|MALE> <family_name>
/mf advanced family relation <person_a_uuid> <person_b_uuid> <trust_0_to_100> <consent_a> <consent_b>
/mf advanced family marriage propose <person_a_uuid> <person_b_uuid> <proposer_uuid>
/mf advanced family marriage answer <marriage_uuid> <respondent_uuid> <consent>
/mf advanced family marriage divorce <marriage_uuid>
/mf advanced family conceive <relationship_uuid> <fertility_roll_0_to_99> <seed>
```

Relación y matrimonio están protegidos por reglas de consentimiento y edad/compatibilidad. Algunos comandos siguen siendo controles administrativos de prueba y no sustituyen interacciones sociales dentro del juego.

### Clínica y tratamiento

```text
/mf advanced family clinic register <settlement_uuid> <tier_1_to_5> <beds>
/mf advanced family clinic open <patient_uuid> <INJURY|INFECTION|EXHAUSTION|CHILDBIRTH|CHRONIC> <severity_0_to_100> <infection_0_to_100>
/mf advanced family clinic treat <case_uuid> <clinic_settlement_uuid>
```

La clínica obtiene médico de los ciudadanos activos DOCTOR del asentamiento. Su medicina se toma del inventario real `medieval_factions:healing_potion`; el tratamiento consume una unidad y requiere cama disponible y paciente activo del mismo asentamiento/facción.

### Expediciones

```text
/mf advanced expedition send <faction_uuid> <settlement_uuid> <tier> <days> <provisions> <party_power> <seed> <member_uuid_csv>
/mf advanced expedition list
/mf advanced expedition route <expedition_uuid> <dest_x dest_y dest_z>
```

`route` requiere que todos los integrantes tengan un `MFResidentEntity` materializado, cargado y en la misma dimensión; destino cargado. La ruta de esta alpha es simple y no maneja todavía multi-dimensión ni expediciones con jefe único físico.

### Mazmorras, habilidades y estado

```text
/mf advanced dungeon generate <seed> <tier_1_to_10>
/mf advanced dungeon build <seed> <tier_1_to_10>
/mf advanced dungeon list
/mf advanced skill list
/mf advanced skill cast <ability_id>
/mf advanced dashboard
/mf advanced status
```

Otros comandos heredados de personaje, facción, asentamiento, banca, monturas, producción y asedios siguen disponibles. Consulta los comandos de la versión previa en el código `MFCommands` / `MFAdvancedCommands`.

## Instalación y compilación

Requisitos de desarrollo:

- JDK 21.
- Gradle 8.12.
- Minecraft Java 1.21.1.
- NeoForge 21.1.267.
- GeckoLib 4.9.4 para NeoForge 1.21.1.

Con JDK 21 y Gradle 8.12 instalados, ejecuta python tools/validate_assets.py, bash tools/run_domain_tests.sh, bash tools/run_saveddata_stub_tests.sh, gradle --no-daemon clean test build y gradle --no-daemon runGameTestServer.

La integración CI extrae el ZIP, aplica parches de compatibilidad/calidad y ejecuta también los smoke tests del servidor y cliente headless. El proyecto fuente no incluye gradlew; para builds locales reproducibles se recomienda añadir un Gradle Wrapper versionado con checksum.

## Pruebas y calidad

Las 276 aserciones son tests de dominio con JDK 21. CI #50 supera build, GameTests y smoke startup/reload de servidor/cliente. El SavedData smoke test utiliza stubs y no sustituye el round-trip NBT real de todos los tipos. El cliente headless usa audio nulo: valida inicialización y carga de recursos, no renderizado ni escucha humana. No se ha ejecutado una partida manual ni una sesión multijugador de dos clientes.

No usar esta alpha como mod final en un mundo principal sin copia de seguridad. Haz backups antes de actualizar.
