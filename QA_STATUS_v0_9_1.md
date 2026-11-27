# Auditoría general — MEDIEVAL FACTIONS v0.9.1-alpha

**Fecha:** 27-11-2026  
**Rama auditada:** `feat/phase10-resident-meal-needs`  
**Commit:** `a4edb23a6986e49e56dbfe33ec847d6a2e841f69`  
**CI:** [run 46024843978 — SUCCESS](https://github.com/malg11lopez-art/med_fac/actions/runs/46024843978)

## Resumen ejecutivo

La rama compila para Minecraft 1.21.1 / NeoForge 21.1.267. La última CI valida recursos, compilación, 12 GameTests NeoForge, JAR, servidor dedicado, cliente headless, recarga de recursos y conexión real cliente-servidor. La continuación integra cosecha autónoma con experiencia de profesión, apariencia civil de residentes, seguridad del dashboard, consulta familiar personal, socialización y comidas con consumo real de reservas.

El trabajo sigue en PRs apiladas y **no está fusionado en `main`**. Una CI verde acredita los flujos probados, no la finalización de todas las verticales.

## Resultado de comprobaciones

| Comprobación | Resultado |
|---|---|
| Build ZIP y aplicación de parches | PASS |
| Recursos JSON | 111, cero errores/advertencias |
| Geo rigs | 14 PASS |
| SoundEvents / OGG | 75 / 75 PASS |
| Sincronización de skins | 86/86 texturas, checks estáticos PASS |
| Pruebas de dominio | PASS |
| SavedData stub smoke test | PASS, cobertura parcial |
| Compilación NeoForge | PASS |
| NeoForge GameTests | 12/12 PASS |
| Descriptor e integridad del JAR | PASS |
| Arranque de servidor dedicado | PASS |
| Arranque de cliente y recarga de recursos | PASS |
| Conexión cliente-servidor y skin packet round-trip | PASS |

## Fase 8 — cosecha autónoma y progresión

- Agricultores cosechan trigo, zanahorias, patatas y remolachas maduras en su waypoint.
- El cultivo se replanta a edad cero.
- El producto se acredita al inventario del asentamiento.
- La cosecha y los 12 XP de profesión se aplican en una transacción sincronizada.
- Si la transacción no puede completarse, se restaura el cultivo original.
- Se reproduce sonido vanilla de rotura de cultivo y se emiten partículas de éxito.
- GameTests comprueban replantación, inventario, experiencia y ausencia de duplicación.

## Fase 9 — gestión, seguridad y familia

- El panel global de gestión requiere permiso de operador nivel 2 porque contiene datos de todo el mundo.
- Las solicitudes del dashboard tienen un cooldown de cinco ticks por jugador.
- La GUI ofrece un botón contextual para preparar una consulta de la sección seleccionada y reserva espacio para el editor de comandos.
- `/mf advanced family status` muestra el registro familiar, relaciones consentidas, matrimonios, gestaciones y casos médicos del propio jugador. Los filtros se aplican por UUID del jugador.
- La interfaz sigue siendo principalmente de consulta y comandos; no es una GUI integral con menús e inventarios.

## Fase 10 — vida cotidiana inicial

- La actividad SOCIALIZE requiere dos residentes activos del mismo asentamiento.
- Una interacción incrementa la satisfacción de ambos en un punto, se limita a una cada 600 ticks y el cooldown se guarda en NBT.
- El orden de UUID evita que ambos residentes otorguen dos veces la misma interacción.
- Una interacción correcta reproduce audio de aldeano y partículas de satisfacción.
- La actividad EAT consume una ración real del almacén, reduce hambre en 35, restaura un punto de salud y aumenta satisfacción en dos.
- La comida tiene cooldown persistente de 600 ticks y solo reproduce sonido/animación tras consumir una ración.
- GameTests verifican el aumento bilateral de satisfacción, rechazo entre asentamientos, consumo exacto de alimento y rechazo cuando el almacén está vacío.

## Arte, animación y sonido

- Se añadió `textures/entity/resident.png`, textura civil 128×128 RGBA para el rig humanoide existente.
- `MFResidentModel` separa la apariencia de residente de la textura acorazada del guardia.
- Se reutilizan animaciones existentes de sueño, comida, socialización, entrenamiento y profesiones.
- La cosecha utiliza el sonido vanilla de cultivo; socialización y comida usan sonidos vanilla y partículas.
- La validación de PNG/JSON y la recarga headless pasan. **No se ha hecho todavía una revisión visual humana en un mundo renderizado ni una escucha humana de audio.**

## Defectos y limitaciones detectados

- La primera ejecución de la CI social falló en el GameTest de cosecha; el reintento completo y la CI posterior con las dos mecánicas nuevas pasaron. Conviene vigilar la estabilidad del batch de GameTests en futuras ejecuciones.
- La textura civil es una base común; faltan variantes por profesión, raza, equipo y estación.
- La socialización no crea todavía amistades persistentes, memoria social, personalidad, hostilidad ni relaciones familiares autónomas.
- Las comidas son una acción de necesidad básica; no hay todavía menú individual, dieta por raza, cocina autónoma ni transporte de alimentos.
- El panel O no implementa `AbstractContainerMenu`, inventarios gráficos ni gestión completa de ciudad/economía/guerra.
- El SavedData stub no sustituye pruebas de round-trip NBT real de todos los tipos y migraciones.
- Falta profiling con 50/100/250 residentes, rutas y convoyes simultáneos.

## Dictamen

CI run 46024843978 acredita compilación, 12 GameTests y smoke tests de servidor/cliente/conexión. Las fases 8–10 tienen mecánicas incrementales reales, pero siguen parciales. No se declara completo el mod ni una vertical que todavía no tenga todas sus interfaces, estados, pruebas y validación manual.
