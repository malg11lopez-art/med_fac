# Plan de implementación hasta la versión completa — MEDIEVAL FACTIONS v0.9.1-alpha

Este plan prioriza dependencias reales y entregas comprobables. Una mecánica solo podrá declararse completa cuando tenga reglas, estado persistente, integración de mundo, UI/controles adecuados, pruebas automatizadas y validación dentro de Minecraft cliente/servidor.

## Estado de v0.9

## Correcciones entregadas en v0.9

- [x] Scheduler runtime aislado por instancia de `MinecraftServer`.
- [x] Bloqueo de expediciones físicas con miembros ya reservados por otro viaje activo.
- [x] Límites de carga/guardado para registros JSON dentro de SavedData avanzado.
- [x] Combate melee básico de guardias y dragones contra monstruos hostiles, con animaciones.
- [x] CI publica el JAR como artefacto solo tras build satisfactorio; scripts Bash/Windows ejecutan las mismas suites.
- [x] 276 aserciones de dominio pasan.


- [x] Añadir motores de dominio para agendas y navegación de habitantes, parcelas de ciudad, órdenes tácticas, genética/incubación, matrimonio/medicina, convoyes, jefe por fases y viajes de expedición.
- [x] Conectar schedules/planes y enlaces de entidades residentes a SavedData.
- [x] Conectar parcela de ciudad con los dos blueprints físicos disponibles.
- [x] Conectar despacho/liquidación de convoy a inventarios y tesorerías de asentamientos.
- [x] Conectar huevo/genoma/eclosión con Companion y entidad dragón.
- [x] Conectar clínica con DOCTOR activo, cama y pociones reales.
- [x] Conectar ruta de expedición a NPC residentes materializados y cargados en la misma dimensión; el avance depende de la llegada de todos los miembros a cada waypoint.
- [x] 276 aserciones de dominio, SavedData stub smoke test y validación de recursos pasan.
- [x] Compilación real NeoForge, cinco GameTests y smoke startup de servidor/cliente headless más conexión cliente-servidor (CI #90).
- [ ] Partida manual, audio/animaciones visuales y sesión de dos clientes.

## P0 — Plataforma y compilación reproducible (completado en CI #50)

- [x] ZIP de fuentes verificado y parches aplicados desde GitHub Actions.
- [x] Java 21 y Gradle 8.12; clean test build correcto.
- [x] NeoForge GameTests correctos.
- [x] JAR con descriptor y versión exacta 0.9.1-alpha.
- [x] Servidor dedicado alcanza marcador de arranque.
- [x] Cliente headless inicializa sonido, recarga recursos y registra capa de skins.
- [ ] Añadir Gradle Wrapper versionado con checksum para builds locales.
- [ ] Añadir más GameTests de transacciones, construcción, persistencia y habilidades.

## P1 — Contratos de datos y seguridad de servidor

1. Añadir versión de esquema para cada bucket y migraciones explícitas.
2. Serializar/deserializar todos los tipos `MFAdvancedSavedData` con codecs reales NBT.
3. Hacer idempotentes las transacciones de convoy, clínica, nacimiento/eclosión y construcción frente a reintentos y crash entre pasos.
4. Validar dimensión, chunks cargados, límites, permisos, distancia y propietario en todos los comandos/eventos.
5. Añadir telemetría de fallos y pruebas de overflow/corrupción de datos.

## P2 — IA integral de ciudadanos

1. Conectar el A* 3D al mundo (colisión, suelo, puertas, escalera, agua, peligros y chunks) o retirar ese A* si se decide usar navegación vanilla; no mantener dos caminos contradictorios.
2. [x] Asignación autónoma inicial de recetas por profesión desde el ciclo diario, con comprobación de stock y reserva atómica de ingredientes (GameTest real CI #90).
3. Vincular el trabajo a estaciones físicas y añadir tareas de campo visibles: recolectar, transportar, construir, reparar, cocinar, comerciar, patrullar y descansar; evitar que la producción sea solo una orden económica abstracta.
4. Añadir necesidades persistentes, personalidad, memoria social, amistad/hostilidad, parejas y encuentros con cooldown.
5. Añadir LOD regional para NPC no cargados, catch-up acotado y presupuesto de ticks configurable.
6. Validar navegación en diferentes biomas, desniveles, interiores, puertas y chunks descargados.

## P3 — GUI completa

1. Arquitectura de servidor con `AbstractContainerMenu` y cliente con `Screen`.
2. Pantalla de personaje/estadísticas, inventario de facción y almacenes.
3. Árbol visual de talentos, habilidades equipadas, cooldowns y recursos sincronizados.
4. Ayuntamiento/constructor, profesión/talleres, banco, diplomacia y gestión familiar/clínica.
5. Mapa regional y panel táctico con órdenes confirmadas por el servidor.
6. Navegación por teclado, tooltips, traducciones, escalado de GUI y pruebas de latencia/multijugador.

## P4 — Constructor de ciudades

1. Editor de parcelas y proyección fantasma; preview debe derivarse del mismo blueprint que la obra real.
2. Más blueprints por distrito y tamaño; caminos, murallas, granjas, talleres, almacenes, mercados, ayuntamiento y clínicas.
3. Validación de altura, agua, suelo, protección de regiones, espacio y bloques existentes.
4. Fases de obra con materiales, salarios, trabajadores, herramientas, daño, pausa y reembolso seguro.
5. Edificios activos con inventarios/menús y servicios para el asentamiento.

## P5 — Habilidades y combate táctico

1. Catálogo por clase/subclase con progresión, prerequisitos, pasivas y árbol visual.
2. Sincronizar recursos/cooldowns/estados y soportar input/animación.
3. Implementar bloqueo, esquiva, parry, stagger, combos, cobertura, altura, alcance, proyectiles y estados elementales.
4. Convertir unidad táctica en entidad de soldado identificada y sincronizada con la unidad persistente.
5. Añadir creación de batalla, órdenes, formaciones, IA de comandante, moral, retirada, bajas y curación.
6. Conquista con cambio de control, guarnición, resistencia, suministro, ocupación y diplomacia.

## P6 — Economía autónoma y caravanas

1. Entidad de carro con inventario/manifest persistente y guardias vinculados al convoy.
2. Navegación de rutas, puntos de parada, peajes, riesgo por bioma, escolta, asalto y recuperación.
3. Mercados con oferta/demanda, producción/consumo, precios deterministas y límites por stock.
4. Conectar salarios, impuestos, tesorerías, banca y compras de NPC a un ledger único.
5. Compensación interbancaria y auditoría de transacciones.

## P7 — Dragones y compañeros

1. Agente aéreo con navegación 3D, obstáculos, aterrizaje, regreso al hogar y órdenes autónomas.
2. Biomas/hábitat/nidos y temperatura ambiente; incubadoras como bloque funcional.
3. Crecimiento visual y numérico por etapa, personalidad/temperamento, genética balanceada y reproducción natural.
4. Alimentación/caza/defensa/patrulla reales; montura, equipo, animaciones, daño y muerte.
5. Pruebas de reproducción, herencia, eclosión, chunk reload y dueño desconectado.

## P8 — Familias, medicina y vida cotidiana

1. Interacción social visible y relación entre ciudadanos basada en tiempo, consentimiento y personalidad.
2. Matrimonio/ceremonia, embarazo visual, parto, parentesco, herencia, custodia, educación y sucesión.
3. Clínicas físicas, camas, camas ocupadas, médico autónomo, recetas y stock médico real.
4. Sistema de enfermedades, diagnóstico, recuperación, epidemias y mortalidad verificable.
5. Integración de estados con población, economía, vivienda y UI.

## P9 — Mazmorras, jefes y expediciones

1. Salas modulares, puertas, llaves, trampas, puzzles, eventos, loot y dificultad escalable.
2. Entidades boss únicas con atributos, IA, ataques telegráficos, fases y loot; BossEncounter debe compartir salud/estado con la entidad.
3. Expediciones NPC con rutas multiwaypoint, grupos, provisiones, formación, combate, heridas, bajas, rescate y regreso físico.
4. Soportar chunks descargados, dimensiones, desconexión, pérdida de líder y persistencia del viaje.

## P10 — Calidad y publicación

- Test de mundo limpio y mundo preexistente.
- Servidor dedicado con dos o más clientes.
- Pruebas de 50/100/250 ciudadanos y rutas/convoyes/batallas simultáneos.
- Profiling de ticks y memoria, migración de mundos y rollback.
- Licencias, changelog, manifest, hash, build reproducible, JAR y guía de servidor.

## Regla de release

No se utilizará la palabra **terminado** para una vertical hasta que todos los checks relevantes de P0/P1 y su etapa estén superados. Esta versión es una alpha con mejoras importantes, no la final.
