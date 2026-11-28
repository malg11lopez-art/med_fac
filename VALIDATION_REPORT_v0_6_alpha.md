# Informe de validación — MEDIEVAL FACTIONS v0.6.0-alpha

Fecha: 26-11-2026.

## Resultados ejecutados

1. `./tools/run_domain_tests.sh`
   - Núcleo: **134 aserciones PASS**.
   - Sistemas avanzados: **59 aserciones PASS**.
   - Total: **193 aserciones PASS**.
   - Incluye pruebas de reglas de NPC, caravanas, banca, préstamos, asedios, habilidades, compañeros, familia, mazmorras, expediciones, construcción y moneda física.
2. `./tools/run_saveddata_stub_tests.sh`
   - **PASS:** round-trip NBT y smoke tests de personaje, facción, asentamiento, ciudadanos, inventario, producción y ledger de XP con stubs mínimos.
   - No prueba `MFAdvancedSavedData` contra Minecraft real.
3. `python tools/validate_assets.py`
   - 106 JSON, 14 rigs, 75 SoundEvents y 75 OGG.
   - **0 errores, 0 advertencias**.

## No verificado
- JDK 21 está disponible, pero Gradle no está instalado ni hay wrapper dentro del ZIP.
- No se ha ejecutado `gradle clean test build`, JUnit, GameTests, `runClient`, `runServer` ni pruebas multijugador.
- No se ha compilado el código de integración de monedas contra las APIs reales de NeoForge/Minecraft.
- La banca física, los comandos, el inventario, la persistencia avanzada, las entidades y las animaciones no han sido probados dentro del juego.
- No se ha generado un `.jar`.

## Dictamen
La mejora económica pasa sus pruebas puras y el control de recursos sigue limpio. Esta entrega es una alpha de desarrollo; el build real y las pruebas dentro de Minecraft son bloqueantes antes de instalarla en un mundo importante o presentarla como terminada.
