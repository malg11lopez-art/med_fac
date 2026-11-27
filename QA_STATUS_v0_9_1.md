# Auditoría general — MEDIEVAL FACTIONS v0.9.1-alpha

**Fecha:** 27-11-2026  
**Ejecución:** [GitHub Actions #142 — SUCCESS](https://github.com/malg11lopez-art/med_fac/actions/runs/46000855445)  
**Commit:** `2e47da2fd6efa2131de129ed3290beaa931ed90a`  
**JAR:** `medieval_factions-0.9.1-alpha.jar`  
**Tamaño:** 1.650.075 bytes  
**SHA-256:** `5acfc06c96c13aea83b761b9dc04b47a87f679e2fca66385e24a13a6ebd7b88e`

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
| SavedData stub smoke test | PASS, cobertura parcial |
| Gradle clean test build | PASS |
| NeoForge GameTests | PASS |
| Descriptor/JAR versión 0.9.1-alpha | PASS |
| Arranque de servidor dedicado | PASS |
| Arranque de cliente y recarga de recursos | PASS |
| Artefacto publicado | PASS |

## Fase 8 — cosecha autónoma validada en rama

La rama `feat/phase8-autonomous-crop-harvest` implementa cosecha física inicial por residentes agricultores: trigo, zanahorias, patatas y remolacha maduros; replantación; y abono del producto al almacén del asentamiento. El GameTest prepara el cultivo en el mundo NeoForge, asigna el trabajo al residente y verifica la interacción. La ejecución CI #142 pasó compilación, 9 GameTests, verificación del JAR, arranque de servidor dedicado y cliente, conexión real y sincronización de skin. Esta fase sigue pendiente de revisión y merge a `main`.

## Mejoras funcionales incluidas

1. Sincronización servidor-cliente y persistencia de las 86 skins. La selección se conserva en NBT por jugador, se difunde al cambiar y se reenvía al entrar/respawnear. El servidor valida índices y limita la frecuencia.
2. Audio integrado en pasos, espada, parry, armadura, puertas, crafting, fundición, ambientes contextuales, progreso, caballo y habilidades.
3. `elemental_burst` y `shadow_blink` con validación en servidor, costes, cooldown, daño/teletransporte seguro, partículas y sonidos.
4. Selección de objetivos mediante ray tracing de entidades limitado por la primera colisión de bloque.
5. Panel O con accesos a comandos existentes; CI comprueba la tecla y los comandos de destino.
6. Lookup indexado de ciudadano por UUID para reducir trabajo repetido por entidad residente.
7. Documentación de estado, matriz de cobertura, puertas de calidad y plan de implementación actualizadas dentro del ZIP de fuentes.

## Limitaciones y trabajo no demostrado

- La sincronización de skins no se ha observado todavía con dos clientes reales y no aplica skins a NPC.
- Los OGG se validan y el cliente headless arranca con audio nulo. Falta escuchar cada efecto, ajustar mezcla, repetición, espacialización y sincronía real.
- La carga de recursos y el registro de capas no certifican cada animación renderizada durante combate/movimiento ni cada skin en un mundo real.
- El panel O es un menú de acceso a comandos; faltan pantallas gráficas completas y contenedores cliente-servidor.
- El SavedData stub no sustituye el round-trip NBT real de todos los tipos, migraciones y reinicio de mundo.
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

**Dictamen:** CI #51 acredita build, GameTests existentes y smoke startup/recarga de recursos. No acredita una partida manual completa ni el cumplimiento del Documento Maestro. El proyecto sigue siendo alpha.
