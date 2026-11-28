# AMPLIACIÓN v1.1 — GENERACIÓN DEL MUNDO, ESTRUCTURAS Y MAZMORRAS

**Esta ampliación forma parte del Documento Maestro de MEDIEVAL FACTIONS.** Desarrolla y prevalece sobre las descripciones resumidas de los apartados 4.1 y 4.5 de la versión 1.0. No cambia las reglas ya cerradas de razas, facciones, economía, profesiones, clases, población, dragones o calendario, salvo donde se especifica cómo se conectan con la exploración.

## 21. Filosofía de generación del mundo

### 21.1 Objetivo

El mundo debe invitar a viajar, descubrir y asentarse. No será un mapa lleno de mazmorras colocadas al azar ni una sucesión de biomas sin identidad. Cada región tendrá una lectura visual, recursos, fauna, peligros, historia y puntos de interés coherentes. La exploración debe recompensar tanto al jugador que recorre una colina y encuentra una torre en ruinas como al grupo preparado que se adentra en una ciudad subterránea.

La generación conservará la identidad de Minecraft: terreno reconocible, libertad para construir, cuevas transitables y biomas extensos. El mod ampliará la variedad con biomas, rasgos naturales, recursos, estructuras y redes subterráneas, sin reemplazar por completo el generador de terreno de Minecraft ni obligar a utilizar un único mundo personalizado.

### 21.2 Principios obligatorios

1. **Determinismo por semilla:** terreno, biomas, recursos y estructuras deben ser reproducibles desde la semilla y la versión del generador. No se deben elegir estructuras mediante tiradas distintas cada vez que un chunk se carga.
2. **Compatibilidad por etiquetas:** los contenidos se asignan a familias de biomas y etiquetas, no exclusivamente a identificadores concretos de biomas vanilla. Así pueden funcionar con Biomes O' Plenty y otros mods compatibles.
3. **Distribución por regiones:** los puntos importantes tendrán distancias mínimas, zonas de exclusión y grupos de generación. No se generarán todas las estructuras de forma independiente y superpuesta.
4. **Coherencia geográfica:** fortalezas en lugares defendibles, ruinas cerca de antiguas rutas o recursos, templos en enclaves apropiados y mazmorras subterráneas conectadas a geología plausible.
5. **Mundo no omnisciente:** los marcadores del mapa aparecen al descubrir el lugar, recibir un mapa, obtener información o realizar una expedición.
6. **Sin escalado automático:** las amenazas se determinan por región, estructura, población enemiga, defensas y preparación. No se ajustan al nivel del jugador.
7. **Sin botín infinito:** tesoros, artefactos únicos y reservas de recursos tienen origen y cantidad. Una mazmorra despejada no vuelve a llenar automáticamente sus cofres.
8. **Simulación ligera a distancia:** la generación del terreno ocurre al cargar chunks; la actividad de facciones, caravanas y expediciones se abstrae fuera de la proximidad del jugador.
9. **Sin dependencia externa obligatoria:** el mod puede generar un mundo completo sin Biomes O' Plenty, Terralith u otros mods. Las integraciones amplían la variedad, pero no son requisito.
10. **Mundo persistente:** añadir contenido nuevo no debe regenerar ni alterar silenciosamente las zonas ya exploradas.

### 21.3 Escala geográfica y regiones

El mundo se organiza en capas conceptuales:

- **Bioma:** determina clima, vegetación, suelo, fauna, bloques y compatibilidad de estructuras.
- **Zona ecológica:** conjunto de biomas cercanos con clima y relieve relacionados, como una cordillera fría, una cuenca boscosa o una costa templada.
- **Región de exploración:** área utilizada por el mapa, exploradores y expediciones para identificar rutas, fronteras y puntos de interés.
- **Dominio político:** territorio reclamado o patrullado por una facción. Puede incluir varios biomas y regiones naturales.

Las zonas ecológicas tendrán transiciones graduales. Un bosque no debería terminar siempre en una línea rígida que da paso a un desierto. La humedad, la temperatura, la elevación y la proximidad al mar ayudan a decidir qué biomas pueden encontrarse juntos.

Se mantendrán zonas amplias de naturaleza sin estructuras. No todo valle necesita una torre y no toda cueva necesita una mazmorra. El silencio geográfico es parte de la exploración.

### 21.4 Biomas y familias ambientales

El catálogo propio inicial será contenido selectivo, no una colección de cincuenta biomas similares. La base será Minecraft vanilla más un conjunto pequeño de biomas distintivos del mod. Cuando Biomes O' Plenty esté instalado, sus más de cincuenta biomas podrán aportar variedad adicional; el contenido del mod seguirá utilizando etiquetas y reglas de compatibilidad.

| Familia ambiental | Biomas de referencia | Recursos y puntos de interés | Riesgos y fauna |
|---|---|---|---|
| Bosques templados | Bosque mixto, bosque antiguo, bosque de hayas, claros | Madera, plantas medicinales, ruinas élficas, cabañas, santuarios | Lobos, osos, bandidos, criaturas feéricas en enclaves raros |
| Bosques húmedos | Jungla, manglar, marjal feérico | Plantas alquímicas, madera resistente, ruinas hundidas, altares naturales | Depredadores, venenos, terreno difícil, criaturas de pantano |
| Llanuras y praderas | Llanura, pradera alta, sabana | Agricultura, caballos, caminos, aldeas, campos de batalla antiguos | Bandidos, lobos, patrullas y fauna de manada |
| Tierras áridas | Desierto, dunas, badlands, cañones | Minerales expuestos, oasis, tumbas, caravasares, ciudades enterradas | Escasez de agua, calor, emboscadas, criaturas excavadoras |
| Montañas y altiplanos | Montaña, picos, meseta rocosa, altiplano glacial | Hierro, gemas, canteras, fortalezas, minas enanas | Caídas, frío, criaturas de montaña y puestos defensivos |
| Regiones frías | Taiga nevada, tundra, bosque boreal, glaciares | Pieles, madera, hielo especial, criptas, observatorios | Tormentas de nieve, depredadores, no muertos y dificultades de viaje |
| Costas y archipiélagos | Costa rocosa, playa, arrecife, islas boscosas | Pesca, perlas, ruinas costeras, faros, santuarios marinos | Ahogamiento, criaturas acuáticas y tormentas locales |
| Zonas volcánicas | Meseta de ceniza, campos de basalto, fumarolas | Minerales refractarios, azufre, cristales térmicos, forjas antiguas | Fuego, gases/zonas dañinas muy localizadas, criaturas ígneas |
| Zonas mágicas raras | Bosque cristalino, claro astral, ruinas alteradas | Cristales, componentes arcanos, portales inactivos, observatorios | Anomalías localizadas, guardianes y fauna alterada |

**Biomas originales prioritarios del mod:**

- **Bosque Primigenio:** árboles monumentales, claros, madera ancestral poco frecuente y restos de asentamientos antiguos. No será una fuente ilimitada de madera superior: los árboles raros tardan en regenerarse o requieren replantación.
- **Marjal Feérico:** vegetación húmeda, flores luminosas, hierbas alquímicas y pequeños santuarios. Los peligros se concentran en zonas concretas.
- **Altiplano Glacial:** picos, valles de hielo, cuevas y ruinas de observación. Introduce rutas difíciles y recursos fríos especiales.
- **Meseta de Ceniza:** geología volcánica, basalto, depósitos minerales y vestigios de antiguas forjas.
- **Pradera de los Ancestros:** llanuras abiertas con monolitos, túmulos y caminos antiguos; conecta exploración, historia y caballería.
- **Bosque Cristalino:** bioma excepcional con formaciones minerales y una historia ligada a magia y geodas; los cristales de alto valor estarán muy limitados.

Estos seis biomas son la primera selección, no una obligación de que todos aparezcan en cada mundo. Su frecuencia será configurable y se verificará que no reduzcan en exceso la presencia de biomas vanilla.

### 21.5 Océanos, ríos, lagos y costas

Los océanos conservarán su función de separación geográfica y exploración, sin convertirse en un sistema de comercio naval en la primera versión. Se añadirán puntos de interés subacuáticos: ruinas, cuevas inundadas, santuarios sirénidos, barcos hundidos, depósitos de perlas y entradas a cavernas costeras.

Los ríos y lagos deberán tener continuidad geográfica razonable y servir como referencias para orientación y asentamientos. Los cruces importantes pueden generar vados, puentes arruinados o puestos de peaje. El mod no generará automáticamente puentes sobre cada río: la infraestructura seguirá siendo una decisión de construcción y mantenimiento.

### 21.6 Cuevas y mundo subterráneo

El subsuelo tendrá identidad propia. No todas las cavernas serán simples túneles con mineral. Habrá redes naturales y bolsillos geológicos, pero las grandes estructuras seguirán siendo reconocibles y localizables.

Capas de contenido:

- **Cavernas someras:** cuevas, grietas, pequeñas cámaras, fauna habitual, depósitos básicos y ruinas menores.
- **Cavernas profundas:** grandes galerías, ríos subterráneos, lagos, geodas, minerales raros y estructuras antiguas.
- **Zonas de profundidad extrema:** entornos infrecuentes con ruinas de civilizaciones perdidas, prisiones, santuarios y amenazas de alta peligrosidad.
- **Redes inundadas:** pasajes que requieren preparación acuática, equipo o rutas alternativas; no deben forzar una raza concreta.
- **Bolsas geotérmicas:** lava, basalto, minerales refractarios y antiguas cámaras de fundición.
- **Cavernas vivas:** sistemas fúngicos y vegetación subterránea con recursos alquímicos y riesgos biológicos localizados.

El mundo subterráneo debe ofrecer rutas y salidas alternativas. Evitaremos generar mazmorras que bloqueen grandes zonas de minería o creen volúmenes enormes de bloques artificiales bajo cada montaña.

### 21.7 Recursos, vetas y geología

Los minerales se generan en vetas o depósitos identificables, con tamaños y abundancias diferentes según profundidad, geología y entorno. El Minero extrae; el Prospector localiza y evalúa. Los recursos especiales no aparecen solo porque un edificio los solicite: la facción debe explorar, comprar, recuperar o conquistar una fuente real.

Las vetas del mundo son localizadas. No habrá una barra global que agote todos los minerales del mapa. Al agotarse un yacimiento, se debe buscar otro; nuevos chunks pueden contener nuevos yacimientos por la generación normal del terreno. Esto mantiene el mundo infinito sin convertir un único yacimiento en una fuente renovable ficticia.

Los materiales elementales y raros aparecerán en contextos temáticos: cristales en geodas, metales refractarios en zonas volcánicas, materiales glaciares en regiones frías y componentes espirituales en lugares asociados a almas o ruinas. Nunca se exigirá un bioma específico para toda la progresión de una profesión: deben existir rutas alternativas de comercio, exploración o expediciones.

### 21.8 Secuencia de generación y protección contra conflictos

La generación se organizará por etapas: relieve y bioma; carvers y cavidades; rasgos geológicos; minerales; vegetación y fauna; puntos de interés pequeños; estructuras grandes; asentamientos iniciales y sus zonas de protección. Cada contenido declarará sus etiquetas de bioma, dimensión, altura, pendiente, espacio, exclusiones y frecuencia.

Se utilizará selección determinista por regiones para evitar la acumulación de varias estructuras mayores en el mismo entorno. Las estructuras comprobarán terreno, líquidos, espacio, distancia a otras estructuras, proximidad a asentamientos y compatibilidad dimensional antes de colocarse.

### 21.9 Área inicial, seguridad y calendario

La región inicial será segura para empezar a sobrevivir, no una zona protegida de forma permanente. El punto de aparición se buscará en un bioma de dificultad ambiental moderada y con acceso razonable a madera, alimento básico, agua y una entrada natural al subsuelo. En un radio inicial aproximado de 600–800 bloques no aparecerán jefes, complejos legendarios ni campamentos de asaltantes de alta peligrosidad. La seguridad no impedirá que aparezcan fauna hostil habitual ni cuevas peligrosas fuera del área inmediata.

La exploración cercana debe ofrecer al menos una pista hacia la civilización: un camino antiguo, un mojón, una posada, una torre, una aldea o un mapa incompleto. Como objetivo de calibración, el primer asentamiento, puesto neutral o pista fiable de una facción debería encontrarse aproximadamente entre 900 y 2.400 bloques del punto inicial. No se garantiza una capital ni una facción aliada al lado del jugador; el propósito es evitar que una semilla lo deje aislado sin orientación durante demasiado tiempo. Si la zona candidata no cumple las condiciones básicas, el sistema probará otros puntos cercanos de forma determinista.

En multijugador, el servidor mantiene un punto de aparición compartido salvo que su administrador configure otra cosa. Ningún jugador recibe una región privada ni recursos exclusivos por aparecer primero. La zona de aparición no será un tutorial obligatorio ni limitará la libertad de marcharse y fundar un asentamiento lejos.

El calendario propio de 18 meses y seis estaciones se integra como una capa de simulación: afecta a cultivos, disponibilidad de algunas plantas, festividades, migración animal y eventos de región. No cambiará el identificador del bioma ni obligará a regenerar bloques cuando cambie la estación. Los efectos visuales estacionales serán moderados y respetarán árboles, plantas y bloques de otros mods.

## 22. Catálogo de estructuras del mundo

### 22.1 Clases de punto de interés

| Categoría | Tamaño orientativo | Función | Estado posible |
|---|---|---|---|
| Microestructura | Muy pequeña | Ambientación, pistas, recursos modestos | Intacta, saqueada, abandonada |
| Hito menor | Pequeño | Refugio, puesto, ruina, recurso | Neutral, ocupado, abandonado |
| Estructura mediana | Mediano | Campamento, torre, santuario, mina | Neutral, hostil, reclamable |
| Complejo mayor | Grande | Fortaleza, templo, ciudad en ruinas, mazmorra | Hostil, disputado, conquistable |
| Lugar legendario | Muy grande y muy raro | Historia, jefe, artefacto o evento excepcional | Sellado, activo, descubierto, resuelto |

Las siguientes distancias son **objetivos iniciales de calibración**, no valores definitivos de código. Se medirán en distancia aproximada entre centros y se ajustarán con pruebas de semillas:

| Tipo | Separación orientativa | Regla de aparición |
|---|---:|---|
| Microestructuras de camino/naturaleza | 250–500 bloques | Relativamente frecuentes, con agrupaciones temáticas |
| Hitos menores | 600–1.200 bloques | Comunes pero no visibles todos desde un mismo punto |
| Mazmorras pequeñas | 1.000–1.800 bloques | Una entrada reconocible, peligro local moderado |
| Estructuras medianas | 2.000–3.500 bloques | Distribuidas por región y bioma compatible |
| Complejos mayores | 4.500–8.000 bloques | Pocas apariciones, exclusión con otras grandes estructuras |
| Lugares legendarios | 10.000–20.000+ bloques | Muy raros, algunos ligados a pistas y expediciones |

Los asentamientos políticos tendrán su propio sistema de separación y densidad; no se tratarán como cofres o mazmorras. Las facciones construidas después de la generación podrán crear nuevos poblados según población, economía, territorio y logística.

### 22.2 Aldeas, ciudades y asentamientos iniciales

Las aldeas vanilla seguirán existiendo. El mod podrá crear aldeas compatibles con culturas y razas, campamentos de clanes, puestos mineros y asentamientos abandonados. No todas las estructuras tendrán una facción activa: algunas serán ruinas, otras refugios neutrales y otras comunidades autónomas.

Los asentamientos iniciales se construyen con módulos arquitectónicos funcionales: vivienda, almacén, granero, taller, pozo/abastecimiento básico, empalizada, mercado y Ayuntamiento cuando corresponda. Un Ayuntamiento de estructura generada debe tener espacios o bloques de función comprobables, no ser solo una fachada decorativa.

La cultura determina materiales, formas de tejado, decoración, distribución, vestimenta y variantes de edificio. La familia racial determina dimensiones y adaptaciones funcionales cuando sean necesarias. Las variantes visuales no deben cambiar la capacidad de trabajo o la economía sin una regla explícita.

Los asentamientos abandonados pueden contener viviendas, talleres, almacenes vacíos, defensas dañadas, tumbas, diarios y señales de por qué desapareció la comunidad. Recuperarlos puede convertirse en un objetivo de jugador o facción, pero no entregará una ciudad completa y gratuita: deben repararse, abastecerse y repoblarse.

### 22.3 Estructuras del Overworld

**Carreteras y lugares de tránsito:** mojones, puentes antiguos, posadas, tabernas aisladas, puestos de vigilancia, refugios de viajeros, estaciones de carretas, campamentos de comerciantes y peajes abandonados. Pueden contener mapas, contratos, rumores y pistas de rutas.

**Estructuras civiles:** granjas abandonadas, molinos, aserraderos, canteras, herrerías en ruinas, almacenes, casas de curandero, bibliotecas pequeñas y barrios de antiguas aldeas. Son espacios de exploración, recuperación de planos o reactivación productiva.

**Estructuras militares:** torres de vigilancia, fortines, fuertes de frontera, campamentos de bandidos, fortalezas de clanes, campos de batalla y fortalezas en ruinas. Las defensas pueden ser reutilizadas por una facción si se reclaman y mantienen.

**Estructuras religiosas y culturales:** santuarios solares, santuarios lunares, círculos druídicos, monasterios, templos raciales, mausoleos, observatorios, menhires y lugares de peregrinación. La afinidad puede influir en la historia o recompensas, pero no bloquear la entrada a un jugador de otra afinidad.

**Ruinas y civilizaciones perdidas:** ciudades enterradas, complejos subterráneos, bibliotecas selladas, torres arcanas, puentes colosales, acueductos antiguos y fortalezas parcialmente sepultadas. Deben comunicar una historia a través de arquitectura, objetos y documentos.

**Puntos naturales especiales:** grandes árboles huecos, cascadas con cuevas, geodas expuestas, cañones, cráteres, arrecifes y cuevas de hielo. No todos necesitan enemigos; algunos son lugares de recolección, lore o refugio.

### 22.4 Estructuras del Nether

El Nether será una región hostil de recursos y ruinas industriales, no solo una versión con más enemigos del Overworld. Se añadirán variantes de fortalezas, fundiciones infernales, bastiones menores, criptas de almas, campamentos piglin, minas de basalto y cámaras de forja. Las estructuras usarán los biomas de Nether vanilla y, cuando estén disponibles, los biomas añadidos por Biomes O' Plenty.

Los materiales raros de esta dimensión alimentarán metalurgia, alquimia y fabricación avanzada, pero habrá rutas alternativas de obtención mediante comercio, expediciones muy difíciles o tesoros de alto nivel. No se exigirá que todo personaje visite el Nether para terminar su profesión.

### 22.5 Estructuras del End

El End será el espacio de exploración excepcional del juego tardío. Se respetarán el acceso y el enfrentamiento vanilla con el Ender Dragon; las nuevas estructuras ampliarán la exploración de islas exteriores y no sustituirán el papel central del dragón vanilla.

Se introducirán observatorios del vacío, santuarios astrales, archivos de una civilización perdida, fortalezas derruidas, jardines cristalinos y complejos de guardianes. Algunas estructuras contendrán tecnología o magia antigua, no necesariamente objetos de daño bruto superior. El End debe ofrecer secretos, materiales y lore únicos, además de desafíos de combate.

### 22.6 Distribución cultural y racial

Las estructuras culturales se seleccionan por familia racial, subraza y cultura histórica. No se debe generar una aldea enana de forma aleatoria en un pantano tropical si no existe una explicación. Aun así, una cultura puede establecer enclaves fuera de su bioma tradicional por migración, comercio, conquista o exilio.

Las estructuras abandonadas no se asignan necesariamente a la raza actual del territorio. La superposición de ruinas de épocas diferentes será una herramienta de narrativa ambiental. Libros, escudos, monedas y estilos arquitectónicos permitirán relacionar hallazgos con las crónicas globales.

## 23. Sistema integral de mazmorras

### 23.1 Categorías por escala

| Tipo | Duración aproximada de una incursión | Composición | Propósito |
|---|---|---|---|
| Encuentro de exploración | 5–15 min | 1–4 salas, pequeño peligro o secreto | Descubrimiento y recursos básicos |
| Mazmorra menor | 15–30 min | 5–10 salas, una ruta corta y mini-jefe opcional | Materiales, mapas y primeras recetas |
| Mazmorra estándar | 30–60 min | 8–18 salas, rutas alternativas y jefe | Equipo especializado, conocimiento y progreso |
| Complejo mayor | 60–120 min o más | 15–30 salas, alas, llaves y varios encuentros | Recompensas avanzadas y lore importante |
| Incursión legendaria | Variable, muy rara | Varias secciones, jefes y una mecánica central | Artefactos excepcionales y descubrimientos del mundo |

Los tiempos son referencias de diseño para pruebas, no límites. Una mazmorra puede completarse con más rapidez por un grupo coordinado o tardar más si explora secretos.

### 23.2 Generación modular y estructura legible

Las mazmorras combinarán salas diseñadas a mano con módulos reutilizables. Cada módulo define conectores, tamaño, altura, tema, requisitos de terreno, encuentros posibles y función. La generación selecciona una composición coherente desde una semilla estable.

La estructura de una mazmorra puede incluir:

- Entrada, patio o vestíbulo de orientación.
- Sala central que conecta varias alas.
- Corredores de combate y habitaciones de descanso parcial.
- Cámaras de puzzle, mecanismos, trampas y puertas cerradas.
- Salas de historia con murales, libros, restos y pistas.
- Almacenes o cámaras de tesoro secundarios.
- Una ruta principal clara y, normalmente, una o dos rutas opcionales.
- Cámara final del jefe y salida o atajo de regreso.

No se generarán laberintos aleatorios imposibles. El jugador debe poder comprender la arquitectura, tomar decisiones y orientarse con señales visuales. Los mapas, brújulas de explorador y habilidades de cartógrafo ayudarán, pero no serán obligatorios para moverse.

### 23.3 Encuentros y desafíos

Cada sala peligrosa se construye a partir de un presupuesto de encuentro: número y tipo de enemigos, espacio, cobertura, trampas, rutas de retirada y posibles refuerzos. No todas las salas deben ser arenas cerradas. Habrá emboscadas, patrullas, enemigos que protegen una posición, criaturas que huyen, defensas automáticas y combates con objetivos.

Los desafíos se dividirán en:

- **Combate:** oleadas limitadas, patrullas, élites, defensores y jefes.
- **Travesía:** saltos, puentes rotos, agua, lava, hielo, corrientes y rutas verticales.
- **Mecanismos:** palancas, engranajes, puertas de presión, contrapesos, runas y circuitos.
- **Puzzles de conocimiento:** símbolos, astronomía, historia, elementos, música o alquimia.
- **Sigilo e infiltración:** evitar patrullas, desactivar alarmas o acceder a una cámara.
- **Gestión de recursos:** puertas que exigen herramientas, suministros o consumibles; nunca una única clase obligatoria.

Los puzzles tendrán pistas dentro del propio entorno o en diarios cercanos. Ninguna puerta crítica dependerá exclusivamente de adivinar una combinación sin información.

### 23.4 Llaves, sellos y progreso dentro de la mazmorra

Las llaves se vinculan a la historia y a las alas de la estructura: emblemas de guardia, sellos de templo, fragmentos de runa, llaves mecánicas o insignias de una orden. Algunas se encuentran en guardianes, otras en rutas secundarias o resolviendo un puzzle.

No se crearán llaves universales de un solo uso que obliguen a abandonar una incursión por una caída accidental. Las llaves importantes se guardarán en el inventario de incursión o se podrán recuperar en una ubicación consistente. Si un grupo se retira, el estado de puertas y guardianes se mantiene de forma predecible.

### 23.5 Peligrosidad, señales y preparación

La peligrosidad se clasifica de 1 a 6. Es una clasificación fija de la mazmorra y de sus sectores, no del jugador. El nivel de peligro se comunica mediante señales: arquitectura, restos de expediciones, enemigos visibles, mapas, rumores y observaciones de exploradores.

La dificultad real depende de equipo, composición, consumibles, habilidades, conocimiento, terreno y ejecución. Una clase puede aportar soluciones ventajosas, pero no deberá ser obligatoria. Por ejemplo, un Mago puede abrir una ruta mediante un mecanismo arcano, pero un ingeniero puede reparar el mecanismo y un grupo puede encontrar una llave física alternativa.

### 23.6 Jefes y mini-jefes

Cada jefe importante tendrá identidad propia, patrón de ataque, señales visuales/sonoras, fases y condiciones de vulnerabilidad legibles. Las fases no consistirán únicamente en aumentar vida y daño. Algunas cambian el terreno, invocan apoyo, activan mecanismos o obligan a proteger un objetivo.

El jefe no escala por el nivel del jugador. Su ficha define vida, daño, resistencia, inmunidades, fases, entorno favorable, recompensas y condiciones de reaparición si corresponde. Los jugadores deben poder estudiar al enemigo, preparar equipo y volver cuando estén listos.

Los mini-jefes protegen llaves, alas o tesoros. Los jefes finales protegen el núcleo narrativo o la recompensa principal. No todos los complejos mayores necesitan varias barras de jefe; el ritmo debe alternar exploración, combate y descubrimiento.

### 23.7 Botín y economía

Las recompensas se dividen en seis familias:

1. **Suministros:** comida, pociones, materiales corrientes, monedas y herramientas de expedición.
2. **Materiales de oficio:** aleaciones, componentes, gemas, ingredientes alquímicos y materiales de construcción.
3. **Conocimiento:** planos, patrones, libros de técnicas, mapas, recetas y documentos históricos.
4. **Equipo:** armas, armaduras, accesorios y piezas especializadas con requisitos claros.
5. **Artefactos:** objetos raros con efectos únicos, límites o coste de uso.
6. **Recompensa política:** pruebas, estandartes, títulos, reputación, mapas territoriales o legitimidad histórica.

La calidad y el tier se mantienen separados. Un material raro no garantiza una espada excepcional: el Herrero debe fabricarla. Una receta avanzada no crea materiales; solo habilita su transformación.

Las cámaras principales se saquean una sola vez por mundo/instancia persistente, salvo estructuras declaradas repetibles. Los enemigos pueden reaparecer únicamente en localizaciones configuradas como campamentos activos o guaridas dinámicas y sus recompensas no deben equivaler al tesoro principal. No hay cofres que regeneran artefactos únicos ni recompensas finales ilimitadas.

Los objetos únicos tendrán un identificador de mundo o de instancia. El servidor registra su generación, propietario actual si está registrada y estado. Así se evita que una misma reliquia histórica se genere sin límite y destruya su valor económico.

### 23.8 Estado persistente de una mazmorra

Estados: **no descubierta → conocida → explorada → activa → parcialmente despejada → despejada**. Algunos lugares pueden pasar a **reocupados**, **reclamados** o **en disputa** si pertenecen a una facción, banda o criatura. El estado incluye puertas, llaves, jefes, cofres, trampas, destrucción por guerra, propietario del botín y registro de descubrimiento.

Una mazmorra despejada no se reconstruye mágicamente. Una facción puede reparar o fortificar una estructura si dispone de trabajadores, recursos y permisos. Una mazmorra asociada a criaturas puede recuperar presencia con el tiempo si su ficha declara una reocupación; el botín histórico no vuelve a aparecer.

### 23.9 Incursiones de NPC y facciones

Los NPC podrán explorar las mismas mazmorras que los jugadores. El Ayuntamiento crea una expedición con líder, combatientes, roles, suministros, equipo, transporte, objetivo, presupuesto y límite de riesgo. Los participantes consumen comida, gastan durabilidad, pueden resultar heridos, retirarse o morir.

La simulación distante no debe resolver una mazmorra con una tirada aislada. Calcula por sectores a partir de peligrosidad, inteligencia del mapa, composición, experiencia, equipo, consumibles, liderazgo y rutas de retirada. Las pérdidas y recompensas se registran; un informe explica dónde fracasó o tuvo éxito la expedición.

Los objetos únicos compiten en el mismo mundo. Si un grupo NPC obtiene una reliquia antes que el jugador, esa reliquia existe y puede pasar a manos de su facción. El jugador podrá comerciar, negociar, robarla dentro de las reglas, capturar a su portador o buscar otra recompensa. No habrá duplicación artificial solo para garantizar que el jugador tenga todos los objetos.

### 23.10 Catálogo de mazmorras y complejos prioritarios

| Nombre de trabajo | Bioma/ubicación | Identidad y mecánica | Recompensa temática |
|---|---|---|---|
| Catacumbas del Reino Caído | Bajo colinas, cementerios y ruinas | Criptas, no muertos, sellos funerarios y jefe guardián | Reliquias históricas, componentes oscuros y lore de dinastías |
| Fortaleza de los Sin Corona | Llanuras o colinas | Fortaleza militar, patios, arqueros, comandante y rutas de asedio | Planos defensivos, armas militares y estandartes |
| Santuario de Raíces | Bosques y marjales | Rutas vegetales, guardianes, acertijos de estaciones y jefe natural | Semillas raras, madera ancestral y técnicas de herboristería |
| Templo de la Marea Hundida | Costa y cuevas inundadas | Navegación subacuática, cámaras anegadas y mecanismos hidráulicos | Perlas, equipo acuático y conocimiento sirénido |
| Forja Abisal | Nether y zonas volcánicas | Metal líquido, válvulas, calor, aleaciones y guardián de forja | Planos de fundición, aleaciones y componentes refractarios |
| Mina de los Reyes Profundos | Montaña y subsuelo | Pozos, rieles, cámaras de extracción y derrumbes controlados | Planos mineros, minerales y herramientas avanzadas |
| Observatorio de las Dos Coronas | Altiplanos o cimas | Alineaciones astrales y mecanismos Solar/Lunar | Libros de astrología, mapas y artefactos de afinidad |
| Biblioteca Sellada de Éter | Ruinas arcanas | Salas móviles, runas, custodios y pruebas de conocimiento | Hechizos, catalizadores y saber arcano |
| Necrópolis de Cristal | Cavernas profundas | Geodas, espejos, trampas de luz y jefe cristalino | Cristales raros y componentes mágicos |
| Ciudad Sepultada de los Primeros | Desiertos, mesetas o profundidad | Distrito urbano, archivos, cisternas y varios sectores | Lore global, planos arquitectónicos y tesoros de civilización |
| Bastión de Ceniza | Nether | Cuartel, forjas, zonas de lava y jefe militar | Metalurgia ígnea y materiales del Nether |
| Archivo del Vacío | Islas exteriores del End | Plataformas, gravedad/teletransporte local controlado y guardianes | Artefactos del End, conocimiento antiguo y componentes excepcionales |
| Nido del Dragón Antiguo | Guarida rara en región compatible | Zona territorial, rastros, huevos y peligros dracónicos | Información, materiales de guarida y posibilidad excepcional de hallar un huevo; nunca botín garantizado |

Los nombres son nombres de trabajo para el documento de diseño; el lore final podrá sustituirlos sin cambiar la función de cada familia.

## 24. Compatibilidad con otros mods y modpacks

### 24.1 Biomes O' Plenty

Biomes O' Plenty será una integración opcional prioritaria, no una dependencia obligatoria. Su catálogo añade más de cincuenta biomas con vegetación, flora y bloques para Overworld, Nether y End, y su ficha publica compatibilidad con NeoForge y Minecraft 1.21–1.21.1. Para las versiones modernas requiere TerraBlender y GlitchCore; debe utilizarse la combinación exacta de archivos compatible con Minecraft y NeoForge 1.21.1.

Reglas de integración:

- El mod reconoce biomas por familias y etiquetas compatibles: bosque, frío, seco, montaña, pantano, costa, Nether, End y océano.
- Los árboles y plantas de BOP podrán utilizarse en construcción, carpintería, decoración, agricultura o herboristería cuando exista una definición de recurso compatible.
- Las estructuras se asignan a biomas compatibles por ambiente y no únicamente por el nombre de un bioma vanilla.
- Las ruinas y mazmorras importantes tendrán adaptaciones de superficie y reglas de altura para evitar aparecer flotando, enterradas de forma imposible o en agua no prevista.
- Si un bioma no está clasificado, el mod utilizará una categoría de compatibilidad genérica o evitará colocar estructuras grandes; nunca debe provocar un crash por un bioma desconocido.
- La integración deberá probarse en un mundo nuevo con BOP, TerraBlender y GlitchCore de la misma versión objetivo.

### 24.2 Terreno y otros mods de biomas

El mod no impondrá una revisión total de terreno. Su generación debe funcionar con biomas vanilla, BOP y futuros perfiles de worldgen probados. Mods que cambien fuertemente montañas, continentes o tamaño de biomas requieren una prueba independiente porque pueden alterar pendientes, altura libre y distancias entre estructuras.

El sistema tendrá un perfil **Vanilla+** por defecto y perfiles opcionales de compatibilidad. No se debe recomendar combinar varios grandes generadores de biomas a ciegas: el usuario podrá instalar alternativas, pero el equipo de desarrollo certificará combinaciones concretas y dejará otras como experimentales.

### 24.3 Inspiración en mods de mazmorras

Se estudiarán como referencias de diseño, no como dependencias obligatorias ni como contenido que se copie:

- **YUNG's Better Dungeons:** referencia para rediseñar las mazmorras vanilla y añadir familias temáticas como catacumbas, fortalezas de no muertos y cuevas de arañas. Su ficha de NeoForge incluye una versión para 1.21.1.
- **When Dungeons Arise:** referencia para grandes estructuras de aventura, rutas de exploración y complejos con cámaras, llaves y recompensas; su versión 2.1.68 declara compatibilidad con NeoForge 1.21–1.21.1.
- **Dungeons and Taverns:** referencia para integrar estructuras de escala variada con una estética coherente con Minecraft, desde pequeños hitos del camino hasta complejos grandes.

MEDIEVAL FACTIONS no copiará bloques, planos, estructuras, arte, código ni tablas de botín de estos proyectos. Se tomarán principios de variedad, navegación, silueta, ritmo de exploración y compatibilidad.

### 24.4 Inspiración en Tinkers' Construct

La filosofía de fabricación modular se desarrollará dentro del propio mod: piezas independientes, estaciones especializadas, materiales con rasgos, modificadores, reparación y evolución de herramientas, fundición, aleaciones, moldes y vertido. La identidad y balance de los materiales serán propios.

La versión oficial de Tinkers' Construct que figura en su ficha pública no enumera Minecraft 1.21.1 entre sus versiones soportadas, mientras que existen ports no oficiales de NeoForge 1.21.1 que expresamente no están aprobados ni mantenidos por el equipo original. Por estabilidad y derechos, MEDIEVAL FACTIONS no dependerá de esos ports. Se puede desarrollar un adaptador opcional en el futuro si las licencias, las versiones y la estabilidad lo permiten; el sistema central funcionará por sí mismo.

### 24.5 Arquitectura de compatibilidad

- Todas las integraciones son módulos opcionales detectados al iniciar el mundo.
- Las definiciones centrales no deben referenciar directamente clases internas de otros mods.
- Los adaptadores traducen etiquetas de bioma, materiales, estaciones, recursos y estructuras a categorías internas.
- Las recetas, biomas y reglas complementarias se distribuyen en datos/paquetes cuando sea viable.
- Si una dependencia opcional falta, se desactiva solo esa integración y se registra un aviso claro; no se impide cargar el mundo.
- Los identificadores de objetos de otros mods no se guardan como única fuente de verdad para una propiedad esencial del personaje o de la facción.

## 25. Configuración de worldgen y pruebas

### 25.1 Opciones del mundo

Al crear mundo o desde configuración del servidor se podrán ajustar:

- Perfil de generación: Vanilla+, Vanilla con biomas ampliados, o integración BOP.
- Activación de biomas originales del mod y frecuencia relativa.
- Densidad de puntos de interés menores, mazmorras pequeñas, complejos mayores y lugares legendarios.
- Generación de asentamientos iniciales y distancia mínima entre facciones.
- Activación por dimensión: Overworld, Nether y End.
- Frecuencia de vetas y recursos especiales, dentro de rangos de balance.
- Generación de estructuras subterráneas y profundidad mínima/máxima.
- Inclusión de ruinas, campamentos de asaltantes, guaridas y estructuras religiosas.
- Integraciones detectadas y paquetes de compatibilidad activos.

El perfil predeterminado debe ser completo y equilibrado; el usuario no necesita configurar diez opciones para que el mundo funcione. La configuración avanzada será opcional.

### 25.2 Reglas de mundo nuevo y mundos existentes

La mayor parte de los biomas y las estructuras se generan al crear chunks nuevos. Instalar o quitar mods de generación en un mundo existente puede crear fronteras visibles entre terreno antiguo y nuevo, o dejar recursos y estructuras ausentes en las zonas ya generadas. El mod advertirá antes de cambiar un perfil de worldgen en un mundo existente y recomendará copia de seguridad.

Los cambios de contenido que no alteran generación —nuevos libros, recetas, profesiones o habilidades— podrán aplicarse con migraciones de datos. Un cambio importante de ruido, biomas o distribución de estructuras requerirá un nuevo mundo o se limitará a chunks no generados.

### 25.3 Matriz de pruebas obligatoria

| Perfil | Contenido de prueba | Criterio de aceptación |
|---|---|---|
| A. Vanilla+ limpio | Solo MEDIEVAL FACTIONS | Inicio, biomas, estructuras y mazmorras funcionan sin dependencias extra |
| B. BOP | BOP + TerraBlender + GlitchCore | Biomas mezclados, estructuras bien colocadas y recursos funcionales |
| C. YUNG's Better Dungeons | MEDIEVAL FACTIONS + YUNG's Better Dungeons | Sin duplicación excesiva de mazmorras pequeñas ni solapamientos críticos |
| D. When Dungeons Arise | MEDIEVAL FACTIONS + When Dungeons Arise | Los complejos mayores respetan distancias y no saturan el mapa |
| E. Generación combinada | Perfil aprobado con BOP y un mod de estructuras | Sin crashes, estructuras flotantes, enterramientos imposibles o saturación |
| F. Servidor dedicado | Mismos perfiles en cliente y servidor | Semilla, estructura, loot y estado coinciden; no hay autoridad de cliente |
| G. Mundo existente | Chunks antiguos y frontera de chunks nuevos | No se corrompen datos; se emite advertencia de cambios de worldgen |

Cada prueba se ejecutará sobre varias semillas y tamaños de región. Se medirán: cantidad de estructuras por área; porcentaje de estructuras válidas; frecuencia de estructuras solapadas; porcentaje de mazmorras accesibles; diversidad de biomas; distancia a asentamientos; tiempo de generación; memoria y tiempo de carga.

### 25.4 Pruebas jugables de exploración

- Un jugador nuevo debe encontrar recursos básicos y un refugio sin tener que localizar una mazmorra.
- Debe existir un asentamiento o pista de asentamiento razonablemente cercano al área inicial, sin obligar a que todas las semillas generen exactamente el mismo mapa.
- En un viaje largo deben alternarse tramos naturales, hitos pequeños y espacios vacíos.
- Los complejos mayores no deben ser visibles o encontrables todos desde una misma zona.
- La entrada de una mazmorra debe ser localizable y no aparecer dentro de una casa, en mitad de una carretera funcional o bajo un edificio clave.
- Los recursos de profesión deben poder obtenerse en más de un lugar o mediante una vía alternativa razonable.
- Los jefes deben poder ser descubiertos por pistas y no solo por caminar aleatoriamente.
- Una mazmorra debe ser recuperable tras muerte, desconexión o fallo de servidor sin duplicar llaves o recompensas.
- Las facciones NPC no deben consumir toda la exploración importante antes de que el jugador pueda intervenir, pero tampoco se congelarán artificialmente para reservarle el mundo.

### 25.5 Revisión del roadmap

La generación de mundo se desarrollará en fases:

1. **Worldgen estable:** compatibilidad con vanilla, registro de biomas/rasgos, tags, minerales y configuración determinista.
2. **Puntos de interés:** ruinas menores, campamentos, torres, posadas, puentes y estructuras de camino.
3. **Asentamientos iniciales:** módulos culturales, funcionalidad del Ayuntamiento, población y separación regional.
4. **Mazmorras menores y estándar:** sistema modular, estados, botín limitado, enemigos y primeros jefes.
5. **Complejos mayores:** fortalezas, templos, ciudades sepultadas, Nether y End.
6. **Compatibilidad ampliada:** BOP como primera integración, seguida de pruebas con mods de biomas y estructuras seleccionados.
7. **Expediciones NPC:** incursiones automatizadas con costes, riesgos, informes y resultados persistentes.
8. **Pulido:** balance de densidad, tiempos de recorrido, accesibilidad, rendimiento y calidad narrativa.

La generación de mundo debe tener una versión jugable antes de intentar poblarla con todo el catálogo racial. Una buena región con una mazmorra memorable y una facción funcional vale más que cien estructuras vacías.

## 26. Fuentes de referencia consultadas

Estas fuentes se consultaron para comprobar disponibilidad y tomar inspiración de diseño. No son requisitos de instalación ni autorización para reutilizar contenido de terceros. Comprobación: 24 de noviembre de 2026.

1. **Biomes O' Plenty — Modrinth:** catálogo de biomas, dimensiones soportadas, loaders y requisitos TerraBlender/GlitchCore. https://modrinth.com/mod/biomes-o-plenty
2. **NeoForge 1.21.1 — Biome Modifiers:** sistema de datos para modificar características y apariciones por bioma. https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/
3. **YUNG's Better Dungeons — NeoForge:** rediseño de mazmorras vanilla y estructuras temáticas; versión 1.21.1. https://www.curseforge.com/minecraft/mc-mods/yungs-better-dungeons-neoforge
4. **When Dungeons Arise — 2.1.68:** versión NeoForge 1.21–1.21.1 y revisión de grandes complejos, vaults y llaves. https://modrinth.com/mod/when-dungeons-arise/version/XIRJSFQ0
5. **Dungeons and Taverns — FAQ:** filosofía de estructuras de estilo vanilla y variedad de escalas. https://dungeonsandtaverns.net/faq/
6. **Tinkers' Construct — Modrinth:** filosofía de herramientas modulares y versiones oficiales enumeradas. https://modrinth.com/mod/tinkers-construct
7. **Tinkers' Construct Unofficial Port — Modrinth:** ejemplo de port no oficial para NeoForge 1.21.1; no aprobado por SlimeKnights. https://modrinth.com/mod/hexa-tinkers-construct-unofficial-port

## 27. Decisión final de worldgen

MEDIEVAL FACTIONS tendrá un mundo **Vanilla+ de fantasía medieval**, con biomas propios limitados pero distintivos, soporte opcional para Biomes O' Plenty, generación geográfica por regiones, una red de puntos de interés de densidad controlada, asentamientos culturales funcionales y mazmorras persistentes con arquitectura modular, jefes y botín finito.

La prioridad no será colocar el máximo número de estructuras, sino hacer que cada descubrimiento tenga sentido, que las distancias permitan viajar y orientarse, que el botín alimente la economía y la progresión, y que las facciones NPC puedan descubrir, reclamar y disputar lugares sin romper las reglas del mundo.
