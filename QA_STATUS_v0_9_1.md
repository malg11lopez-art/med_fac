# Informe de QA — MEDIEVAL FACTIONS 0.9.1-alpha

Fecha de ejecución: 26-11-2026  
Workflow: [Build MEDIEVAL FACTIONS #41](https://github.com/malg11lopez-art/med_fac/actions/runs/45908567151)  
Commit: `03354464a9f1f189a579c3ba1ab6ab09765e0f92`  
Resultado: **SUCCESS**

## Artefacto verificado

- Archivo: `medieval_factions-0.9.1-alpha.jar`
- Tamaño: 1.626.534 bytes
- SHA-256: `a832620ac521654e29ba70849047eb08da5ed7bf4e1e2466e68cf7f2210046e2`
- Descriptor: `modId=medieval_factions`, `version=0.9.1-alpha`
- Dependencias declaradas: NeoForge 21.1.267+, Minecraft 1.21.1, GeckoLib 4.9.4.

## Comprobaciones de CI superadas

| Comprobación | Resultado |
|---|---|
| ZIP de fuentes y extracción | PASS |
| Parches de compatibilidad/calidad | PASS |
| Validador de recursos | PASS |
| Catálogo de previsualización de sonido | 75/75 PASS |
| Pruebas de dominio | 271 aserciones PASS |
| SavedData stub smoke test | PASS, cobertura parcial |
| Gradle `clean test build` | PASS |
| NeoForge GameTests | PASS |
| Descriptor y checksum del JAR | PASS |
| Arranque de servidor dedicado | PASS |
| Cliente headless y recarga de recursos | PASS |
| Capa de skin default/slim | 2/2 variantes registradas |
| Artefacto JAR publicado | PASS |

## Inventario comprobado en el JAR

- 75 definiciones SoundEvent y 75 archivos OGG; ninguna referencia de `sounds.json` apunta a un archivo ausente.
- 86 texturas de skin de jugador.
- 9 bibliotecas JSON de animación GeckoLib.
- 14 modelos geométricos Geo.
- El descriptor empaquetado declara exactamente `0.9.1-alpha`.

## Corrección importante de la auditoría de audio

El registro de previsualización contiene referencias a los 75 eventos, pero eso solo demuestra que un operador puede probarlos. No demuestra que todos se activen de forma natural durante el juego. La inspección separada de keyframes y código de acciones detectó **43/75** eventos conectados a animaciones o acciones; **32/75** todavía no tienen disparador natural. El siguiente cambio del pipeline excluirá explícitamente `MFSoundPreview.java` y los GameTests del cálculo de cobertura para evitar falsos positivos.

## Límites de lo probado

- El cliente se ejecutó headless en Xvfb y con OpenAL nulo: valida inicio y carga de recursos, no escucha humana ni evaluación visual manual.
- El smoke test de servidor valida el marcador normal de arranque, no una sesión larga ni estrés.
- Los GameTests existentes verifican principalmente registros/constructibilidad de objetos, bloques, sonidos y tipos de entidad. No cubren todas las transacciones del mundo.
- El test de SavedData usa stubs y no sustituye una prueba de serialización NBT real de cada tipo.
- No se ha ejecutado una partida manual ni una sesión con dos clientes conectados.

## Limitaciones de producto aún abiertas

- Skins: selección local persistente, sin sincronización multijugador ni aplicación a residentes NPC.
- Falta GUI gráfica completa y sincronización de datos de gestión.
- Faltan tareas autónomas completas de NPC, constructor integral de ciudades, soldados visibles, carros físicos, mercados autónomos, vuelo autónomo de dragones y jefes únicos conectados a entidades.
- Persisten por hacer las pruebas de mundo nuevo/existente, chunks descargados, dimensiones, migraciones, dos clientes y rendimiento con poblaciones altas.

**Dictamen:** v0.9.1-alpha es una compilación funcionalmente arrancable y con recursos registrados, pero sigue siendo una alpha de integración; no es todavía la versión completa definida en el Documento Maestro.
