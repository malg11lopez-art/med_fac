# MEDIEVAL FACTIONS — v0.9.1-alpha

Repositorio de fuentes y compilación para **Minecraft Java 1.21.1**, **NeoForge 21.1.267**, **Java 21** y **GeckoLib 4.9.4**.

El proyecto se conserva en `MEDIEVAL_FACTIONS_v0_9_0_alpha.zip`. GitHub Actions verifica el archivo, lo extrae, aplica los parches de compatibilidad y calidad mantenidos en `ci/`, compila el mod y ejecuta pruebas de servidor y cliente headless.

## Última compilación confirmada

<badge color="success">CI #42 — SUCCESS</badge>

- Commit: `947999c8b4737c9c535375a0ea0b9c6ce101951d`
- JAR: `medieval_factions-0.9.1-alpha.jar`
- SHA-256: `cfaeede92cff0a4507a2c554922863fcde96d07f343e24ce2e191ae031adec26`
- Tamaño: 1.626.534 bytes
- [Ver GitHub Actions #42 y descargar los artefactos](https://github.com/malg11lopez-art/med_fac/actions/runs/45908989072)

## Qué se ha comprobado

- Integridad y estructura del ZIP de fuentes.
- Java 21 y Gradle 8.12.
- Validador de recursos: 111 JSON y 14 rigs, sin errores.
- 75 SoundEvents registrados y 75 archivos OGG presentes; los 75 aparecen en la previsualización de operador.
- Los 75 OGG se decodifican como Vorbis mono a 44,1 kHz; duración entre 0,16 y 4 s.
- 271 aserciones de dominio y pruebas de persistencia con stubs.
- `gradle clean test build`.
- NeoForge GameTests para registros de objetos, entidades, bloques y sonidos.
- Descriptor del mod y versión exacta `0.9.1-alpha` dentro del JAR.
- Arranque de servidor dedicado.
- Arranque de cliente headless, inicialización del motor de audio y recarga del ResourceManager.
- Registro de la capa de skin para los modelos vanilla `default` y `slim`, con 86 skins disponibles.
- El pipeline guarda también un artefacto ZIP con el código fuente ya parcheado.

## Estado real de sonido y skins

**Sonido:** los 75 eventos están registrados, todos sus archivos existen y se pueden reproducir mediante `/mf advanced sound preview <sound_id>`. La auditoría corregida excluye el catálogo de previsualización y los GameTests: **43/75** tienen un disparador en keyframes o acciones de runtime; **32/75** siguen sin disparador natural. El detalle se imprime en `tools/validate_assets.py`.

**Skins:** hay 86 texturas locales. La tecla `N` cambia la skin del jugador local y guarda la selección en `config/medieval_factions_skin.txt`. La capa se registra en los dos modelos de jugador vanilla. Esta selección todavía no se sincroniza con otros jugadores y no se aplica automáticamente a residentes NPC.

## Qué significa el resultado

La compilación y los smoke tests demuestran que el mod puede compilarse, iniciar un servidor NeoForge, iniciar el cliente headless y cargar los recursos comprobados. No equivalen a una prueba manual completa de una partida, ni certifican la calidad auditiva humana: el cliente CI utiliza Xvfb y el driver de audio nulo.

## Qué sigue pendiente antes de considerarlo completo

- Disparadores naturales para los 32 SoundEvents que aún solo están registrados/previsualizables.
- Sincronización multijugador y aplicación de skins a otros jugadores/NPC.
- GUI gráfica completa para facciones, ciudades, economía, personajes, talentos y órdenes.
- IA social y laboral autónoma de habitantes y tareas físicas de campo.
- Generación integral de ciudades, más blueprints y edificios funcionales.
- Ejércitos visibles, formaciones físicas, asedios y conquista jugable.
- Carros y escoltas como entidades, mercados autónomos y economía conectada.
- Vuelo autónomo y ciclo vital completo de dragones.
- Familia/medicina con interacciones y animaciones completas.
- Jefes físicos sincronizados con sus encuentros y expediciones robustas en chunks/dimensiones.
- Pruebas de persistencia real de todos los buckets, multijugador con dos clientes y profiling con poblaciones altas.

## Compilar

Abre [Actions](https://github.com/malg11lopez-art/med_fac/actions) y ejecuta el workflow `Build MEDIEVAL FACTIONS` con `workflow_dispatch`, o consulta la [última ejecución](https://github.com/malg11lopez-art/med_fac/actions).

La versión es **alpha**. No se debe tratar como una release final hasta superar las limitaciones de la lista anterior y las puertas de calidad documentadas en el proyecto.
