# Informe de validación — MEDIEVAL FACTIONS v0.4.0-alpha

Fecha de trabajo: 25-11-2026.

## Pruebas superadas en este entorno
- JDK: OpenJDK 21.0.12.
- `tools/run_domain_tests.sh`: **130 aserciones superadas** (moneda, producción base, XP anti-farmeo, árbol de talentos, subclases, calendario, biología racial, ciudadanos, mantenimiento, comercio y diplomacia).
- `python tools/validate_assets.py`: **104 JSON, 14 rigs, 75 SoundEvents, 75 OGG; 0 errores y 0 advertencias**.
- Compilación Java de clases de dominio + adaptadores `MFFactionSavedData` / `MFWorldSavedData` contra stubs mínimos de las API de SavedData/NBT: superada.
- `tools/run_saveddata_stub_tests.sh`: prueba de ida y vuelta NBT con stubs: perfil, facción con límite de dos jugadores y familia racial, rechazo de residentes de otra familia, asentamiento, ciudadano, inventario, tesorería, encargo productivo, mantenimiento configurable, promoción de asentamiento financiada y ledger anti-farmeo persistente: superada.
- Compilación de forma/tipos de `MFCommands` y `MFWorldEvents` contra stubs mínimos de Brigadier/NeoForge/Minecraft: superada.

## No verificado
- No existe Gradle en este entorno y no se pudo descargar la distribución por fallo de resolución DNS.
- No se ejecutó `gradle clean test build`, Gradle no pudo resolver las dependencias reales NeoForge/Minecraft/GeckoLib.
- Los stubs no son las API reales y no garantizan compatibilidad binaria o de comportamiento.
- No se ejecutaron JUnit, GameTests, `runClient`, servidor dedicado, pruebas de chunks, guardado de un mundo real ni multijugador.
- No se ha generado ni certificado un JAR.

## Dictamen
El núcleo de dominio y los casos probados pasan. Los adaptadores Java compilan contra interfaces simuladas, pero hace falta compilar contra las dependencias reales y probar dentro de Minecraft antes de distribuir el mod. Esta alpha no equivale al Documento Maestro implementado al 100 %.
