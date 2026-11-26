# MEDIEVAL FACTIONS — importación pendiente

Este repositorio está preparado para alojar el proyecto MEDIEVAL FACTIONS.

## Estado al 26-11-2026

El repositorio se encuentra vacío (sin commits ni archivos). El código fuente de la versión v0.9.0-alpha se encuentra actualmente en un paquete ZIP generado en la sesión de trabajo, pero todavía no ha sido importado aquí.

## Siguiente paso obligatorio

Subir el contenido completo del ZIP de MEDIEVAL FACTIONS v0.9.0-alpha a la raíz del repositorio, conservando:
- `build.gradle` y configuración Gradle
- `gradle.properties` y Gradle Wrapper si está disponible
- `src/main/java`
- `src/main/resources`
- scripts de validación y pruebas
- documentación y manifiestos

No se debe declarar una compilación satisfactoria hasta que GitHub Actions ejecute `clean test build` y publique un JAR verificable.

## Puerta de calidad prevista

1. Java 21
2. Wrapper Gradle fijado a una versión compatible con NeoForge ModDevGradle
3. Validación de recursos
4. Pruebas de dominio
5. Pruebas de persistencia
6. Compilación NeoForge
7. Publicación del JAR solo si todos los pasos anteriores terminan correctamente

## Aviso

Este README no sustituye al código fuente ni acredita que el mod compile.