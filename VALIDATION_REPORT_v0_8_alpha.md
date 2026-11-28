# Informe de validación — MEDIEVAL FACTIONS v0.8.0-alpha

**Objetivo:** Minecraft Java 1.21.1 · NeoForge 21.1.267 · Java 21 · GeckoLib 4.9.4  
**Resultado:** las pruebas disponibles pasan; la compilación real de NeoForge y la ejecución in-game no se pudieron realizar.

## 1. Cambios probados en dominio

| Conjunto | Aserciones | Cobertura principal |
|---|---:|---|
| Núcleo | 136 | calendario, raza, personaje, economía, población, producción y progresión base |
| Sistemas avanzados | 74 | banca, caravanas estratégicas, asedios, habilidades iniciales, monturas, familias, mazmorras y expediciones abstractas |
| Expansión de simulación | 49 | agendas/rutas NPC, planificación urbana, órdenes tácticas, genética e incubación, medicina, convoy físico, jefe y expedición por waypoints |
| **Total** | **259** | Aserciones puras de dominio; no equivalen a pruebas de Minecraft |

## 2. Resultados del entorno

| Comprobación | Resultado | Alcance |
|---|---|---|
| `./tools/run_domain_tests.sh` | PASS — 259 aserciones | Compilación y ejecución de reglas puras con JDK 21 |
| `./tools/run_saveddata_stub_tests.sh` | PASS | Smoke test de round-trip NBT con stubs de un subconjunto SavedData antiguo; no valida todo `MFAdvancedSavedData` ni el serializador real de Minecraft |
| `python tools/validate_assets.py` | PASS — 0 errores, 0 advertencias | 106 JSON, 14 rigs, 75 SoundEvents y 75 OGG |
| `javac -proc:none` sin classpath de juego | Sin errores de sintaxis detectados | La compilación finaliza con errores por paquetes/tipos de Minecraft, NeoForge, GeckoLib y otras dependencias ausentes; no cuenta como build |
| `gradle clean test build` | No ejecutado | No hay `gradle` ni `gradlew` disponible y no se pudieron descargar dependencias |
| `runClient` / `runServer` / GameTests | No ejecutado | Requiere distribución Gradle y dependencias externas |

## 3. Integraciones añadidas

- Registros SavedData nuevos para schedules/planes/enlaces NPC, city plans/build links, tactical battles/army columns/occupations, dragon genomes/eggs/hatchlings, marriages/medical cases/clinics, physical convoys/boss encounters/expedition journeys.
- Residentes materializables y con destinos de agenda; movimiento real usa el `PathNavigation` terrestre del juego.
- Parcela urbana vinculada a construcción física segura (`small_house`, `watchtower`).
- Convoy físico con reserva de mercancía y escrow, ruta de waypoints, emboscada y liquidación entre asentamientos.
- Genética e incubación persistentes; la eclosión materializa entidad dragón y actualiza el ledger de compañero.
- Clínica vinculada a ciudadano DOCTOR, camas, stock real de poción y tratamiento autoritativo.
- Expedición con ruta de waypoints y NPC ya materializados/cargados en una dimensión.

## 4. Límites de las pruebas

No se ha verificado:

- Compatibilidad real de firmas/API Java con los JAR fijados de NeoForge/GeckoLib.
- Registro de pantallas/menús, sincronización de red, render de entidades o animaciones.
- Guardar/cerrar/reabrir mundo con todos los nuevos tipos `MFAdvancedSavedData`.
- Carga y descarga de chunks durante rutas; teletransporte/cambio de dimensión; muerte/reconexión de residentes.
- Colocación de ciudad/mazmorra en terreno real y rollback si el mundo o inventario cambia.
- Dos clientes simultáneos, servidor dedicado, explotación de comandos y presupuesto de ticks.
- AI de vuelo autónomo, soldados/carros visibles o boss entity vinculado al motor de encuentro.

## 5. Bloqueo del build

El entorno dispone de Java 21, pero no de Gradle ni del Gradle Wrapper. La distribución de Gradle y las dependencias externas no pudieron descargarse por falta de acceso de red/DNS. No se generó un JAR. Para cerrar este gate en una máquina con acceso a dependencias:

```bash
./gradlew clean test build
./gradlew runClient
./gradlew runServer
```

Si se ejecuta con Gradle instalado globalmente, sustituir `./gradlew` por `gradle`.

## 6. Dictamen

v0.8 amplía y conecta reglas de simulación y algunos flujos autoritativos al mundo (residentes, parcelas, convoyes, eclosión, clínica y expediciones). **No es la versión final del mod ni completa todas las mecánicas solicitadas.** La GUI, la IA social completa, los ejércitos visibles, las caravanas con carros, el combate táctico in-game, la autonomía de dragones y los jefes físicos únicos todavía no pueden considerarse terminados.
