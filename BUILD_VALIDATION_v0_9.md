# Informe de compilación verificada — MEDIEVAL FACTIONS v0.9.0-alpha

Fecha: 26-11-2026

## Resultado de compilación

**GitHub Actions — ejecución 6: SUCCESS**

- Repositorio: https://github.com/malg11lopez-art/med_fac
- Ejecución: https://github.com/malg11lopez-art/med_fac/actions/runs/45898040349
- Commit de configuración: `516009f0e490a27d9d2104c6ca4503398b5097b2`
- Plataforma: Ubuntu
- Java: Temurin 21
- Gradle: 8.12
- Minecraft: 1.21.1
- NeoForge: 21.1.267
- GeckoLib: 4.9.4

## Pasos superados

1. Verificación e integridad del ZIP de fuentes.
2. Aplicación de correcciones para las API de Minecraft 1.21.1 / NeoForge.
3. Configuración de Java 21 y Gradle 8.12.
4. Validación de recursos.
5. Pruebas de dominio.
6. Smoke test de persistencia NBT con stubs.
7. `gradle --no-daemon clean test build`.
8. Verificación de integridad del JAR y presencia de `META-INF/neoforge.mods.toml`.
9. Verificación de `modId` y dependencias `neoforge`, `minecraft` y `geckolib`.
10. Subida del JAR como artefacto de Actions.

## Errores corregidos

- Visibilidad de `addAdditionalSaveData` y `readAdditionalSaveData` adaptada a la API pública de `Mob`.
- Límite de stacks obtenido desde `ItemStack`, no desde `Item`.
- Comprobación de comida mediante `getFoodProperties(player)`.
- Aplicación de fuego mediante `igniteForSeconds`.
- Conversión acotada del día simulado `long` a `int` al crear huevos genéticos.
- Captura estable del UUID de expedición dentro de una lambda.
- Eliminación de `GeckoLib.initialize()`, método no existente en GeckoLib 4.9.4.
- Eliminación de la exclusión global que quitaba el descriptor TOML generado del JAR.

## Artefacto

- Archivo: `medieval_factions-0.9.0-alpha.jar`
- Tamaño: 1.591.665 bytes
- SHA-256: `c529ddb4271dfea23a90b7337b7f796f2a26569f9da1c1ac8beac4388a7ed11c`
- ZIP de artefacto de Actions: `medieval-factions-jar-516009f0e490a27d9d2104c6ca4503398b5097b2`
- Digest SHA-256 del ZIP de Actions: `75f08ed93a1bfc7274741205e4d2d0025c60413639d55359a32f84cfcee48208`

## Límites

El resultado acredita la compilación Gradle, los tests automatizados configurados y la presencia del descriptor del mod. No acredita todavía la apertura de un mundo con cliente gráfico, GameTests, multijugador o validación visual de las mecánicas.

El workflow incluye ahora un paso adicional para intentar el arranque de un servidor dedicado NeoForge antes de volver a publicar el JAR. Su resultado se debe consultar en la ejecución más reciente de Actions.
