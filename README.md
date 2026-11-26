# MEDIEVAL FACTIONS

Repositorio de integración y compilación de **MEDIEVAL FACTIONS v0.9.0-alpha** para Minecraft 1.21.1 / NeoForge.

## Estado de importación

La conexión con GitHub está activa y el pipeline de compilación ya está configurado. El código del mod todavía no se encuentra en este repositorio: debe añadirse el archivo fuente completo:

`MEDIEVAL_FACTIONS_v0_9_0_alpha.zip`

El archivo debe subirse a la **raíz** del repositorio. No hace falta descomprimirlo localmente: el workflow lo verifica y extrae automáticamente en GitHub Actions.

## Cómo importar el proyecto

1. Descarga el ZIP v0.9.0-alpha desde la conversación de ChatGPT.
2. Abre [Subir archivos al repositorio](https://github.com/malg11lopez-art/med_fac/upload/main).
3. Arrastra `MEDIEVAL_FACTIONS_v0_9_0_alpha.zip` a la zona de carga y confirma el commit en `main`.
4. Abre la pestaña [Actions](https://github.com/malg11lopez-art/med_fac/actions) para ver la compilación.

## Qué ejecuta CI

- Java 21 (Temurin).
- Gradle 8.12.
- Comprobación de integridad y estructura del ZIP.
- Validador de recursos.
- Pruebas de dominio.
- Smoke test de persistencia NBT con stubs.
- `gradle --no-daemon clean test build`.
- Publicación del JAR como artefacto descargable solo si todas las comprobaciones pasan.

Workflow: `.github/workflows/build.yml`.

## Criterio de aceptación

No se declara que el mod compila hasta que GitHub Actions termine con éxito la compilación NeoForge. Un JAR compilado tampoco equivale, por sí solo, a una certificación de funcionamiento dentro de Minecraft: después deben ejecutarse pruebas de cliente, servidor, GameTests, persistencia y multijugador.

## Requisitos del proyecto

- Minecraft Java 1.21.1
- NeoForge 21.1.267
- Java 21
- GeckoLib 4.9.4

## Documentación incluida dentro del ZIP

Al extraer el paquete se encuentran la guía en español, el estado del proyecto, la matriz de cobertura, los informes de validación, los mapas de animación/sonido y el plan de implementación restante.


## Primera compilación real

El pipeline ya ha completado correctamente una compilación de NeoForge con Java 21 y Gradle 8.12. También se ha comprobado que el JAR contiene `META-INF/neoforge.mods.toml` y declara las dependencias requeridas.

- [Ejecución de compilación satisfactoria](https://github.com/malg11lopez-art/med_fac/actions/runs/45898040349)
- [Informe de compilación](BUILD_VALIDATION_v0_9.md)
- [Acciones y última prueba de arranque de servidor](https://github.com/malg11lopez-art/med_fac/actions)

La compilación no sustituye las pruebas de cliente gráfico, mundo real y multijugador. El workflow añade un smoke test del servidor dedicado para avanzar en esa validación.
