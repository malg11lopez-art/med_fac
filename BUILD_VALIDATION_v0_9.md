# Informe de build verificado — MEDIEVAL FACTIONS v0.9.0-alpha

Fecha: 26-11-2026

## Resultado

**GitHub Actions: SUCCESS**

- Repositorio: https://github.com/malg11lopez-art/med_fac
- Ejecución: https://github.com/malg11lopez-art/med_fac/actions/runs/45898040349
- Commit CI: `516009f0e490a27d9d2104c6ca4503398b5097b2`
- Entorno: Ubuntu, Eclipse Temurin Java 21, Gradle 8.12
- Dependencias de plataforma: Minecraft 1.21.1, NeoForge 21.1.267, GeckoLib 4.9.4

## Etapas que terminaron correctamente

1. Verificación e integridad del ZIP de fuentes.
2. Aplicación de correcciones de compatibilidad para Minecraft 1.21.1.
3. Configuración de Java 21 y Gradle 8.12.
4. Validación de recursos.
5. Pruebas de dominio.
6. Smoke test de persistencia NBT con stubs.
7. `gradle --no-daemon clean test build`.
8. Inspección del JAR para comprobar `META-INF/neoforge.mods.toml`, identificador del mod y dependencias NeoForge/Minecraft/GeckoLib.
9. Carga del JAR como artefacto de GitHub Actions.

## Errores corregidos

- Los métodos `addAdditionalSaveData` y `readAdditionalSaveData` tenían visibilidad `protected`, mientras que en la API de destino son `public`.
- Las llamadas a `Item.getMaxStackSize()` no correspondían a la firma de NeoForge; se cambió a límites obtenidos desde `ItemStack`.
- `ItemStack.isEdible()` no existe en la API de destino; se consulta `getFoodProperties(player)`.
- La API de fuego se actualizó a `igniteForSeconds`.
- Se adaptó el día simulado `long` a la API que recibe `int`.
- Se corrigió una captura lambda sobre variable local reasignada.
- Se eliminó `GeckoLib.initialize()`, método inexistente en GeckoLib 4.9.4.
- Se eliminó una exclusión global de recursos que quitaba `META-INF/neoforge.mods.toml` del JAR.

## Artefacto generado

- Nombre interno: `medieval_factions-0.9.0-alpha.jar`
- Tamaño: 1.591.665 bytes
- SHA-256: `c529ddb4271dfea23a90b7337b7f796f2a26569f9da1c1ac8beac4388a7ed11c`
- Digest del ZIP de artefacto de GitHub: `75f08ed93a1bfc7274741205e4d2d0025c60413639d55359a32f84cfcee48208`

## Límites de esta validación

Este resultado acredita compilación, pruebas automatizadas configuradas y empaquetado del descriptor. No acredita todavía que se haya iniciado una instancia real de Minecraft con cliente gráfico, que se haya probado un mundo nuevo o existente, ni que se hayan validado visualmente las mecánicas, las animaciones o el multijugador. Esas pruebas siguen siendo necesarias antes de considerar el mod listo para publicación.
