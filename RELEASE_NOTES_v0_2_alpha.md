# MEDIEVAL FACTIONS — Dev Starter v0.2-alpha

## Novedades
- Base de proyecto NeoForge 1.21.1 / Java 21 con GeckoLib 4.9.4.
- Diez entidades de demostración renderizables y cuatro bloques animados con sus modelos, controladores y texturas.
- 75 SoundEvents registrados y 75 archivos OGG vinculados.
- 86 skins base, iconos, materiales, herramientas, armas y partículas.
- Persistencia inicial de identidad y tesorería de facciones mediante SavedData.
- Núcleo productivo Java probado: inventarios limitados, reservas de materiales, trabajo secuencial por trabajador y salida bloqueada cuando el almacén se llena.
- Política de recuperación temporal offline limitada y probada.
- Mineral de acero solar, loot Silk Touch/Fortuna, tags 1.21.x y recetas de fundición/alto horno.

## Correcciones e integridad
- Resolución diferida de BlockItems y registros.
- Metadatos TOML procesados desde plantillas.
- Rutas de tags singulares de Minecraft 1.21.x.
- Predicado de Silk Touch compatible con 1.21.1.
- Alturas absolutas de worldgen entre Y=-48 y Y=-8.
- Verificación de JSON, huesos, clips, sonidos, texturas y traducciones.

## Resultados de pruebas
- Validador de recursos: 0 errores / 0 advertencias.
- Núcleo Java puro: 28 aserciones superadas con JDK 21.
- Análisis sintáctico de los fuentes Java: sin errores de sintaxis.
- No se ha ejecutado Gradle, JUnit, ni el cliente/servidor real en este entorno. No se distribuye un JAR compilado.

## Alcance
Esta versión es un starter técnico y una vertical slice parcial. No es todavía la implementación completa del Documento Maestro v1.3. Las entidades son prototipos y el motor productivo y el reloj offline aún no están conectados al mundo Minecraft.
