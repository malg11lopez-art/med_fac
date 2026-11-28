# Informe de validación — MEDIEVAL FACTIONS v0.5.0-alpha

Fecha: 25-11-2026.

## Pruebas ejecutadas y resultados

1. `./tools/run_domain_tests.sh`
   - Núcleo anterior: 130 aserciones superadas.
   - Sistemas avanzados: 54 aserciones superadas.
   - Total: **184 aserciones PASS**.
   - Cubre reglas NPC, caravanas, liquidación de emboscadas, banca/reservas, préstamos, asedios, efectos de habilidades, recursos de monturas/dragones, consentimiento/herencia, generación determinista de mazmorras/colocaciones, expediciones y blueprints de construcción.
2. `./tools/run_saveddata_stub_tests.sh`
   - **PASS:** smoke test de round-trip NBT para SavedData, personaje, asentamiento, ciudadanos, inventario, producción y ledger anti-farmeo de combate usando stubs mínimos.
   - No compila ni ejecuta toda la nueva `MFAdvancedSavedData` contra los JAR reales de NeoForge.
3. `python tools/validate_assets.py`
   - 104 JSON, 14 rigs, 75 SoundEvents, 75 archivos OGG.
   - **0 errores, 0 advertencias**.
4. Parser Java sin classpath
   - `javac -proc:none` recorrió las fuentes y no emitió diagnósticos de sintaxis (`expected`, `illegal start`, `reached end`, `not a statement`, etc.).
   - La invocación devuelve error por falta de paquetes y tipos de Minecraft/NeoForge/GeckoLib. Esto NO es compilación ni comprobación de compatibilidad API.

## Límites de la prueba
- No hay Gradle instalado ni wrapper dentro del proyecto.
- La descarga de `gradle-8.12-bin.zip` desde `services.gradle.org` falló en este entorno.
- No se ejecutó `gradle clean test build`, JUnit, GameTests, `runClient`, `runServer` ni servidor dedicado.
- No se verificó en Minecraft la colocación de bloques, acceso al cofre, render/animación, control de montura, aliento del dragón, daño, comandos, sincronización cliente-servidor, chunks o multijugador.
- Los stubs de SavedData/NBT son pruebas de lógica/estructura limitada; no reemplazan APIs reales.

## Dictamen
Los 184 tests puros y la validación de recursos pasan. La implementación incluye varios sistemas conectados al ciclo del servidor y registros persistentes, pero la compilación real y las pruebas dentro del juego siguen bloqueadas por la falta de Gradle y dependencias. **No se ha generado un JAR y esta alpha no puede considerarse final ni libre de errores.**
