# Auditoría general — MEDIEVAL FACTIONS v0.9.1-alpha

**Fecha:** 27-11-2026  
**Ejecución:** [GitHub Actions #119 — SUCCESS](https://github.com/malg11lopez-art/med_fac/actions/runs/45992737433)  
**Commit:** `0579852f459c204abdc8a8fd69d9daf39b2dacb8`  
**JAR:** artefacto `medieval-factions-jar-0579852f459c204abdc8a8fd69d9daf39b2dacb8`  
**Tamaño del artefacto ZIP:** 1.599.799 bytes

## Resumen ejecutivo

La build actual compila para Minecraft 1.21.1 / NeoForge 21.1.267, ejecuta GameTests y arranca tanto servidor dedicado como cliente headless. La selección de skins tiene sincronización cliente-servidor y persistencia por jugador; se han añadido disparadores de audio, dos habilidades, ray tracing de entidades, un panel de acceso rápido y una ruta de búsqueda indexada para datos de residentes. El panel O no es una GUI de gestión integral.

## Resultado de comprobaciones

| Comprobación | Resultado |
|---|---|
| Build ZIP y aplicación de parches | PASS |
| Recursos JSON | 111, cero errores y cero advertencias |
| Geo rigs | 14 PASS |
| SoundEvents / OGG | 75 / 75 PASS |
| Referencias de audio en keyframes/runtime | 75/75 según auditor estático |
| Sincronización de skins | 86/86 texturas, 14/14 checks |
| Pruebas de dominio | 276 aserciones PASS |
| SavedData stub smoke test | PASS |\n| SavedData avanzado real NBT/JSON | 31/31 buckets tipados con round-trip GameTest |\n| Compatibilidad de esquemas SavedData | 12/12 checks estáticos + pruebas runtime de esquemas antiguos/futuros/malformados |
| Gradle clean test build | PASS |
| NeoForge GameTests | 7/7 pruebas obligatorias PASS |
| Descriptor/JAR versión 0.9.1-alpha | PASS |
| Arranque de servidor dedicado | PASS |
| Arranque de cliente y recarga de recursos | PASS |
| Artefacto publicado | PASS |

## Mejoras funcionales incluidas

1. Sincronización servidor-cliente y persistencia de las 86 skins. La selección se conserva en NBT por jugador, se difunde al cambiar y se reenvía al entrar/respawnear. El servidor valida índices y limita la frecuencia.
2. Audio integrado en pasos, espada, parry, armadura, puertas, crafting, fundición, ambientes contextuales, progreso, caballo y habilidades.
3. `elemental_burst` y `shadow_blink` con validación en servidor, costes, cooldown, daño/teletransporte seguro, partículas y sonidos.
4. Selección de objetivos mediante ray tracing de entidades limitado por la primera colisión de bloque.
5. Panel O con accesos a comandos existentes; CI comprueba la tecla y los comandos de destino.
6. Lookup indexado de ciudadano por UUID para reducir trabajo repetido por entidad residente.
7. Documentación de estado, matriz de cobertura, puertas de calidad y plan de implementación actualizadas dentro del ZIP de fuentes.\n8. GameTest de serialización/recarga de los 31 buckets avanzados (CI #119).

## Limitaciones y trabajo no demostrado

- La sincronización de skins no se ha observado todavía con dos clientes reales y no aplica skins a NPC.
- Los OGG se validan y el cliente headless arranca con audio nulo. Falta escuchar cada efecto, ajustar mezcla, repetición, espacialización y sincronía real.
- La carga de recursos y el registro de capas no certifican cada animación renderizada durante combate/movimiento ni cada skin en un mundo real.
- El panel O es un menú de acceso a comandos; faltan pantallas gráficas completas y contenedores cliente-servidor.
- El round-trip GameTest cubre un registro representativo por bucket, pero faltan worlds reales históricos de cada versión publicada, pruebas con volúmenes máximos, cortes de proceso durante guardado y migraciones entre versiones reales.
- Falta test prolongado de servidor y profiling con 50/100/250 NPC, rutas, convoyes y batallas simultáneas.
- Ciudad, ejército/conquista, caravanas, vuelo autónomo, vida familiar y jefes aún no forman experiencias completas de producto.

## Auditoría técnica

**Defectos corregidos durante esta continuación:**

- El selector de objetivos usaba `ServerPlayer.pick()` como si devolviera `EntityHitResult`; se sustituyó por ray tracing de entidades que respeta los obstáculos.
- Tres referencias locales obsoletas (`living`) se corrigieron a `livingTarget` tras detectar un fallo real de `compileJava`.
- Los paquetes de selección de skin se procesan con `enqueueWork` en los hilos lógicos de servidor y cliente.
- Los residentes consultan `MFWorldSavedData.citizen(UUID)` en vez de reconstruir/recorrer la lista completa cada actualización.
- Los smoke tests de cliente/servidor terminan al alcanzar marcadores de disponibilidad.
- El JAR debe declarar exactamente `0.9.1-alpha`; el atajo del panel se valida como tecla O (79).
- El texto del dashboard y la documentación del ZIP ya no contradicen las integraciones actuales.

**Dictamen:** CI #119 acredita build NeoForge, 276 aserciones de dominio, SavedData stub, compatibilidad de esquemas, round-trip NBT/JSON en los 31 buckets avanzados, 7 GameTests, arranque cliente/servidor y conexión real con sincronización de skin. No acredita una partida manual completa, evaluación visual/auditiva humana, pruebas prolongadas con varios jugadores ni el cumplimiento del Documento Maestro. El proyecto sigue siendo alpha.
