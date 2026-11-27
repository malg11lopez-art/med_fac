# MEDIEVAL FACTIONS — v0.9.1-alpha

Minecraft Java 1.21.1 · NeoForge 21.1.267 · Java 21 · GeckoLib 4.9.4.

## Última compilación verificada

**CI #49 — SUCCESS** · commit `18d7b50156ffc89256e81c32550dd0b2eb7d4811`.

- JAR: `medieval_factions-0.9.1-alpha.jar`
- SHA-256: `12c0c6849bb083b7768855947eabd705337ec9190845ed9406f3dfb9f40f583d`
- [Ver ejecución #49 y descargar los artefactos](https://github.com/malg11lopez-art/med_fac/actions/runs/45915911949)

## Funciones integradas en esta alpha

**Sincronización de skins:** 86 skins, selección persistente por jugador y sincronización servidor-cliente con actualización al entrar, respawnear y desconectarse. Está compilada y validada estáticamente; falta una sesión real de dos clientes y todavía no se aplica a NPC.

**Audio:** 75 SoundEvents y 75 OGG validados, con referencias detectadas en keyframes o código de gameplay. Se han añadido disparadores para pasos, espada, parry, armadura, puertas, crafting, fundición, ambientes, progreso, caballo y habilidades. El cliente headless no acredita mezcla ni escucha humana.

**Mecánicas:** `elemental_burst`, `shadow_blink`, selección de objetivos mediante ray tracing de entidades que respeta obstáculos y búsqueda indexada de ciudadanos por UUID para reducir trabajo repetido por residente.

**Interfaz:** panel rápido en la tecla **O** con acceso a ficha, talentos, habilidades, compañeros, facciones, calendario, dashboard y skins. Es un panel de acceso a comandos, no una GUI integral de gestión.

## Validación automatizada #49

- 276 aserciones de dominio superadas.
- 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG: cero errores y advertencias.
- 86/86 skins y 14 comprobaciones de sincronización.
- `gradle clean test build` y NeoForge GameTests: PASS.
- Servidor dedicado y cliente headless: arranque y recarga de recursos correctos.
- JAR `0.9.1-alpha`: descriptor e integridad verificados.

## Pendiente antes de una versión completa

- GUI gráfica de gestión para personaje, inventario, facción, ciudad, economía, diplomacia y guerra.
- Pruebas manuales visuales/auditivas de animaciones, objetos, sonidos y skins; sesión multijugador con dos clientes.
- Persistencia completa NBT, migraciones, mundo limpio/existente, reinicios, chunks y dimensiones.
- IA social/laboral autónoma, navegación integral de habitantes y tareas físicas.
- Constructor integral de ciudades, soldados y asedios visibles, carros físicos y mercados autónomos.
- Vuelo autónomo y ciclo de vida completo de dragones, vida familiar/medicina y jefes físicos.
- Profiling con poblaciones altas, rutas, convoyes y batallas simultáneas.

**Estado:** build funcionalmente arrancable con integraciones reales, pero sigue siendo una **alpha**, no un juego terminado. El informe detallado está en `QA_STATUS_v0_9_1.md`; el paquete fuente conserva la documentación técnica en español.