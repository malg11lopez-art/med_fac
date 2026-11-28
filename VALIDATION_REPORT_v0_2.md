# Informe de validación — MEDIEVAL FACTIONS Dev Starter v0.2-alpha

## Herramientas y entorno
- OpenJDK 21.0.12.1.
- Gradle: no instalado en el entorno de creación.
- Python asset validator y ffprobe disponibles.

## Validación de recursos
Comando: `python tools/validate_assets.py`

Resultado: **0 errores, 0 advertencias**.

- 104 archivos JSON parseados.
- 14 rigs Geo validables.
- 75 SoundEvents encontrados y vinculados.
- 75 OGG Vorbis leíbles y mono.
- Texturas PNG verificadas.
- Referencias de huesos, animaciones, sonidos y traducciones verificadas.

## Pruebas de núcleo Java
Comando: `tools/run_domain_tests.sh`

Resultado: **28 aserciones superadas**.

Incluye aritmética monetaria sin overflow, transferencias equilibradas, reserva de materiales, exclusión de trabajos simultáneos por trabajador, duración, bloqueo por falta de capacidad del almacén, entrega diferida, cancelación segura y límite temporal offline.

## Análisis Java
- 45 archivos Java de `src/main/java` analizados con el parser de `javac -proc:none`.
- No se encontraron diagnósticos de sintaxis (diagnósticos sintácticos detectados: 0).
- La compilación de clases que dependen de Minecraft/NeoForge/GeckoLib no puede confirmarse sin resolver las dependencias en el classpath.

## No ejecutado / pendiente
- `gradle clean test build` y generación del JAR.
- Tests JUnit en Gradle.
- `runClient`, `runServer` y GameTests de NeoForge.
- Renderizado visual y sincronización de animaciones.
- Pruebas de chunks, guardado/reinicio, worldgen, loot e integración multijugador.

## Conclusión
Los recursos y el núcleo de dominio independiente superan las validaciones que sí se pudieron ejecutar. El proyecto **no está certificado como compilable ni jugable** hasta completar el build de Gradle y las pruebas dentro de Minecraft 1.21.1 con NeoForge 21.1.267 y GeckoLib 4.9.4.
