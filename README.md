# MEDIEVAL FACTIONS — v0.9.1-alpha

Repositorio de fuentes y compilación para **Minecraft Java 1.21.1**, **NeoForge 21.1.267**, **Java 21** y **GeckoLib 4.9.4**.

El proyecto se conserva en `MEDIEVAL_FACTIONS_v0_9_0_alpha.zip`. GitHub Actions verifica el archivo, lo extrae, aplica los parches de compatibilidad y calidad mantenidos en `ci/`, compila el mod y ejecuta pruebas de servidor y cliente headless.

## Última compilación confirmada

<badge color="success">CI #41 — SUCCESS</badge>

- Commit: `03354464a9f1f189a579c3ba1ab6ab09765e0f92`
- JAR: `medieval_factions-0.9.1-alpha.jar`
- SHA-256: `a832620ac521654e29ba70849047eb08da5ed7bf4e1e2466e68cf7f2210046e2`
- Tamaño: 1.626.534 bytes
- [Ver GitHub Actions #41 y descargar los artefactos](https://github.com/malg11lopez-art/med_fac/actions/runs/45908567151)

## Qué se ha comprobado

- Integridad y estructura del ZIP de fuentes.
- Java 21 y Gradle 8.12.
- Validador de recursos JSON, modelos, texturas y animaciones.
- 75 SoundEvents registrados y 75 archivos OGG presentes; los 75 aparecen en la previsualización de operador.
- 271 aserciones de dominio y pruebas de persistencia con stubs.
- `gradle clean test build`.
- NeoForge GameTests para registros de objetos, entidades, bloques y sonidos.
- Descriptor del mod y versión exacta `0.9.1-alpha` dentro del JAR.
- Arranque de servidor dedicado.
- Arranque de cliente headless, inicialización del motor de audio y recarga del ResourceManager.
- Registro de la capa de skin para los modelos vanilla `default` y `slim`, con 86 skins disponibles.
- El pipeline guarda también un artefacto ZIP con el código fuente ya parcheado.

## Estado real de sonido y skins

**Sonido:** 75/75 eventos están registrados y se pueden reproducir mediante `/mf advanced sound preview <sound_id>`. La auditoría manual del código y keyframes detectó 43/75 con disparadores de gameplay o animación; 32 siguen sin disparador natural. La comprobación automática de cobertura se está corrigiendo para excluir el catálogo de previsualización y los GameTests, que no cuentan como uso en partida.

**Skins:** hay 86 texturas locales. La tecla `N` cambia la skin del jugador local y guarda la selección en `config/medieval_factions_skin.txt`. La capa se registra en los dos modelos de jugador vanilla. Esta selección todavía no se sincroniza con otros jugadores y no se aplica automáticamente a residentes NPC.

## Qué significa el resultado

La compilación y los smoke tests demuestran que el mod puede compilarse, iniciar un servidor NeoForge, iniciar el cliente headless y cargar los recursos comprobados. No equivalen a una prueba manual completa de una partida, ni certifican la calidad auditiva humana: el cliente CI utiliza Xvfb y el driver de audio nulo.

## Pendiente antes de considerarlo completo

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
