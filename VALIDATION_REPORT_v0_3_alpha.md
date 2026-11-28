# Informe de validación — v0.3.0-alpha

Fecha de trabajo: 25-11-2026.

## Correcto en este entorno
- JDK: OpenJDK 21.0.12.
- Pruebas Java puras: `tools/run_domain_tests.sh` → 52 aserciones superadas.
- Recursos: `python tools/validate_assets.py` → 104 JSON, 14 rigs, 75 SoundEvents, 75 OGG; cero errores y cero advertencias.
- Análisis sintáctico: `javac -proc:none` no informó errores de sintaxis después de corregir la composición de comandos.

## No verificado
- Compilación completa: `gradle` no está instalado; tampoco se pudo resolver la distribución desde `services.gradle.org` porque el entorno no resolvió DNS.
- Resolución de tipos NeoForge/Minecraft/GeckoLib: imposible sin classpath descargado.
- JUnit, `runClient`, `runServer`, GameTests, guardado/reinicio, chunks y multijugador: no ejecutados.
- JAR: no generado.

## Conclusión
El núcleo Java puro y los recursos indicados pasan las verificaciones ejecutadas. El proyecto todavía requiere compilación con dependencias reales y pruebas de juego antes de considerar estable cualquier adaptador NeoForge o publicarlo como JAR instalable.
