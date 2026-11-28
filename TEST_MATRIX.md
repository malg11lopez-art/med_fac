# Matriz de pruebas — MEDIEVAL FACTIONS v0.9.0-alpha

> Ningún test real de Minecraft se marca como superado sin ejecutar los artefactos exactos de NeoForge y probar mundo/cliente/servidor.

| Área | Verificación | Resultado v0.9 |
|---|---|---|
| Núcleo de dominio | calendario, razas, personaje, economía, población, producción | 136 aserciones PASS |
| Sistemas avanzados | banca, caravanas estratégicas, asedios, habilidades iniciales, monturas, familia, mazmorras y expediciones abstractas | 74 aserciones PASS |
| Simulación extendida | horarios y rutas de NPC, parcelas/obra urbana, órdenes tácticas/formaciones, genética/incubación, matrimonio/medicina, convoy, boss phases y expedición | 61 aserciones PASS |
| Total de dominio | `./tools/run_domain_tests.sh` | **271 aserciones PASS** |
| Persistencia stub | `./tools/run_saveddata_stub_tests.sh` | PASS; subset antiguo, no valida todos los buckets nuevos |
| Recursos JSON | `python tools/validate_assets.py` | 106 JSON; 0 errores, 0 advertencias |
| Rig/animaciones | validador de recursos | 14 rigs; nombres/referencias estáticas |
| Audio | SoundEvents y archivos OGG | 75 SoundEvents, 75 OGG; reproducción in-game pendiente |
| Parser de Java | `javac -proc:none` sin classpath MC | No concluyente: faltan los JAR de Minecraft/NeoForge/GeckoLib; no es build ni type-check completo |
| Compilación NeoForge | `gradle clean test build` | No ejecutada: Gradle/Wrapper y dependencias no disponibles |
| Cliente y servidor | `runClient`, `runServer` | Pendiente |
| GameTests | mundo real y ticks del servidor | Pendiente |
| SavedData avanzado | round-trip de schedules, rutas, city plans, huevos, clínicas y convoyes con Minecraft NBT real | Pendiente |
| Habitantes | pathfinding, agenda, chunk unload, trabajo, vida social | Solo reglas y navegación terrestre waypoint conectadas; validación in-game pendiente |
| GUI | pantallas y menús de cliente-servidor | No implementada |
| Ciudad | parcela, reserva y colocación física con dos blueprints | Dominio + flujo de construcción conectado; prueba real pendiente |
| Combate | habilidades reales + motor táctico abstracto | Seis habilidades iniciales; guerra táctica con entidades pendiente |
| Caravanas | reservas, escrow y liquidación | Convoy simulado por waypoints, no hay entidad de carro visible |
| Ejército/conquista | unidades/columnas/ocupación | Motor abstracto; soldados visibles/captura jugable pendientes |
| Dragón | genoma, huevo, incubación y eclosión | Dominio + entidad dragón eclosionada; IA aérea autónoma pendiente |
| Familia/medicina | matrimonio, caso, clínica y tratamiento | Reglas persistentes y poción real consumida; interacción social/GUI pendiente |
| Mazmorra/jefe | salas físicas + boss phases | Mazmorra básica; jefe único no conectado a entidad física |
| Expedición | waypoints y grupo de NPC existentes | Ruta limitada: todos los NPC deben estar cargados y llegar realmente a cada waypoint; una dimensión |
| Multiplayer | dos clientes + servidor dedicado | Pendiente |
| Rendimiento | carga 50/100/250 ciudadanos, rutas/batallas simultáneas | Pendiente |

## Comandos reproducibles ejecutados

```bash
./tools/run_domain_tests.sh
./tools/run_saveddata_stub_tests.sh
python tools/validate_assets.py
```

No ejecutar `javac` sin classpath como sustituto de `gradle build`; solo es una comprobación sintáctica parcial.
