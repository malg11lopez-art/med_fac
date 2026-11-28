# Informe de validación — MEDIEVAL FACTIONS v0.7.0-alpha

**Plataforma objetivo:** Minecraft Java 1.21.1 · NeoForge 21.1.267 · Java 21 · GeckoLib 4.9.4  
**Versión:** 0.7.0-alpha  
**Resultado:** las pruebas ejecutables disponibles pasan; la compilación real de NeoForge permanece sin verificar.

## 1. Cambios incluidos en esta revisión

- Reembolso de ingredientes de producción cuando un trabajador ya no está activo.
- UUID compartido entre persona, linaje y ciudadano al producirse un nacimiento.
- Validación estricta de listas de expedición y protección contra el doble consumo de provisiones.
- Entrega de botín pendiente mientras el almacén no tenga capacidad numérica.
- Confirmación de caravanas solo tras commit bilateral.
- Préstamos con vencimiento/gracia y recuperación contable simétrica de depósitos impagados.
- Sincronización de salud y muerte de monturas; comprobación del registro persistente antes de admitir un pasajero.
- Vuelo manual tridimensional de grifo, fénix y dragón; hambre diaria del dragón.
- Pruebas ampliadas para identidad de nacimientos, abortos de trabajo, impago/compensación bancaria, hambre, roster de expedición y sincronización de monturas.

## 2. Pruebas ejecutadas

| Comprobación | Resultado |
|---|---:|
| `tools/run_domain_tests.sh` — núcleo | 136 aserciones superadas |
| `tools/run_domain_tests.sh` — sistemas avanzados | 74 aserciones superadas |
| Total de aserciones de dominio | **210 superadas** |
| `tools/run_saveddata_stub_tests.sh` | PASS |
| Validador de recursos JSON | 106 JSON, 0 errores, 0 advertencias |
| Validación de rigs | 14 rigs |
| SoundEvents / audio | 75 SoundEvents y 75 OGG |
| Integridad del ZIP | Se valida después de empaquetar |

El smoke test NBT usa stubs mínimos para un subconjunto del SavedData antiguo (personaje/facción/asentamiento/población/inventario/trabajo/XP). No simula un mundo Minecraft completo ni certifica todo `MFAdvancedSavedData`.

## 3. Estado de compilación

- JDK disponible: OpenJDK 21.
- Gradle: no instalado.
- Gradle Wrapper (`gradlew`): no incluido.
- Se inspeccionó el código con `javac -proc:none` sin classpath de Minecraft/NeoForge/GeckoLib. El parser no notificó errores de sintaxis, pero `javac` produjo 2.102 diagnósticos de tipos/paquetes externos ausentes. El resultado **no es una compilación**.
- No se pudieron resolver las dependencias externas desde este entorno.
- No se ejecutaron `clean test build`, JUnit, GameTests, `runClient`, `runServer` ni validaciones multijugador.
- No se generó un archivo `.jar`.

## 4. Mecánicas aún no completas

La presencia de un motor de dominio o comando no equivale a una mecánica completa. Permanecen pendientes, entre otros:

1. Ciudadanos visibles con pathfinding, horarios, tareas de mundo, transporte, relaciones y vida social.
2. Interfaces gráficas reales sincronizadas cliente/servidor para personaje, árbol de talentos, talleres, bancos, diplomacia y gestión.
3. Constructor de ciudades con editor, proyección, selección de parcela, validación integral y catálogo de edificios.
4. Todas las habilidades y ramas de las 15 clases/44 subclases, junto con combate táctico, equipo modular, bloqueo, esquiva, parry y animaciones sincronizadas.
5. Caravanas físicas con carros/escoltas, rutas y mercados autónomos.
6. Guerra táctica con unidades visibles, máquinas de asedio, daños estructurales, captura, ocupación y tratados.
7. Ciclo vital de dragones: IA autónoma, huevos, incubación, genética, reproducción, crecimiento y equipo.
8. Familias jugables y visuales: interacción social real, matrimonio, embarazo visible, parto, medicina, escuela, custodia y sucesión.
9. Mazmorras con generación natural completa, jefes propios, puzzles, llaves y expediciones físicas con NPC.
10. Pruebas de rendimiento, persistencia real en Minecraft, servidor dedicado y multijugador.

## 5. Dictamen

La v0.7.0-alpha mejora la coherencia de varios sistemas y pasa las pruebas de dominio, persistencia simulada y recursos disponibles. **No se puede certificar como proyecto terminado** mientras no haya compilación NeoForge reproducible y pruebas dentro de Minecraft, y mientras los sistemas enumerados sigan incompletos.
