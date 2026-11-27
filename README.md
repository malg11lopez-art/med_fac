# MEDIEVAL FACTIONS — v0.9.1-alpha

Minecraft Java 1.21.1 · NeoForge 21.1.267 · Java 21 · GeckoLib 4.9.4.

## Última validación automatizada

**CI run 46024843978 — SUCCESS** · commit `a4edb23a6986e49e56dbfe33ec847d6a2e841f69`.

- 12 GameTests NeoForge superados.
- Validación de recursos, compilación, descriptor/JAR, servidor dedicado y cliente headless superados.
- Conexión cliente-servidor y round-trip de paquetes de skin superados.
- [Ver ejecución CI y artefactos](https://github.com/malg11lopez-art/med_fac/actions/runs/46024843978).

Esta validación corresponde a la rama de trabajo encadenada de las PR #9–#15; **todavía no está integrada en `main`**.

## Funciones añadidas y validadas en esta rama

**Cosecha autónoma:** los agricultores cosechan trigo, zanahorias, patatas y remolachas maduras en su waypoint de trabajo, replantan el cultivo y acreditan la producción al almacén del asentamiento. Cada cosecha correcta otorga 12 XP de profesión; reproduce el sonido vanilla de romper cultivo y emite partículas. El inventario y la experiencia se guardan en una transacción sincronizada.

**Habitantes civiles:** se añade una textura pixel-art civil de 128×128 y un modelo específico para residentes. Los guardias conservan su textura acorazada. Se reutilizan el rig y las animaciones existentes; todavía no hay variantes visuales por profesión.

**Gestión y familia:** el panel O presenta páginas de estado y comandos sugeridos. El dashboard global exige nivel de operador 2 y limita las peticiones a una cada cinco ticks. `/mf advanced family status` muestra únicamente los datos familiares y médicos del jugador que lo ejecuta.

**Vida cotidiana:** los residentes pueden socializar con otro habitante activo del mismo asentamiento, aumentando la satisfacción de ambos con cooldown persistente de 600 ticks, sonido y partículas. También pueden consumir una ración real del almacén durante la actividad EAT: hambre −35, salud +1 y satisfacción +2. El cooldown de comida también se persiste.

## Validación CI más reciente

- 12/12 GameTests NeoForge.
- 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG validados.
- 86 skins y comprobaciones estáticas de sincronización.
- Compilación NeoForge y JAR íntegro.
- Servidor dedicado y cliente headless con recarga de recursos.
- Conexión real cliente-servidor y round-trip de skin.

## Lo que todavía falta

- GUI integral con menús e inventarios cliente-servidor; el panel O es una consola gráfica de consulta/atajos.
- Variantes de textura por profesión y revisión visual manual de cada entidad/animación.
- Pruebas auditivas humanas de mezcla, volumen, espacialización y sincronía.
- IA social completa: memoria, amistades persistentes, conflictos, relaciones autónomas, personalidad y rutinas más ricas.
- Tareas físicas autónomas adicionales: transportar, construir, reparar, cocinar, comerciar y patrullar.
- Constructor integral de ciudades, soldados y asedios visibles, carros físicos y mercados autónomos.
- Vuelo autónomo y ciclo de vida completo de dragones; familias/medicina NPC completas; jefes físicos.
- Profiling con poblaciones altas, rutas, convoyes y batallas simultáneas.

**Estado:** alpha con mecánicas nuevas conectadas al mundo y pruebas automatizadas satisfactorias. No es el mod terminado ni se ha fusionado todavía en `main`. Consulta `QA_STATUS_v0_9_1.md`, `SYSTEM_COMPLETION_MATRIX.md` y `NEXT_IMPLEMENTATION_PLAN.md` para el estado detallado.
