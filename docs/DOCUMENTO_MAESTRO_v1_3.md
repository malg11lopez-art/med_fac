# MEDIEVAL FACTIONS
## Documento maestro de diseño

**Versión:** 1.3 — auditoría integral, normalización de sistemas y especificación previa a producción  
**Plataforma objetivo:** Minecraft Java Edition 1.21.1  
**Estado:** especificación auditada, ampliada y consolidada; no es código ni una promesa de funcionalidades ya implementadas.  
**Fecha de revisión:** 25 de noviembre de 2026

---

## Auditoría ejecutiva v1.3

Esta revisión no añade contenido por volumen: corrige reglas que podían producir exploits, contradicciones o costes técnicos inviables. La v1.3 es la autoridad vigente. La Parte V resuelve y prevalece sobre cualquier ambigüedad detectada en las versiones anteriores.

| Prioridad | Hallazgo de la v1.1 | Resolución de la v1.2 |
|---|---|---|
| Crítica | “El mundo sigue funcionando offline” no distinguía servidor vacío de servidor apagado | Simulación continua si el servidor está encendido; pausa al apagarlo; recuperación offline limitada y configurable |
| Crítica | No había una fuente única de verdad para dinero, inventario, botín y propiedad | Registro transaccional de servidor con transferencias atómicas e identificadores de objetos únicos |
| Crítica | La simulación distante podía interpretarse como NPC físicos activos en todo el mundo | NPC persistentes como datos; entidades físicas solo en proximidad; simulación por eventos lejos del jugador |
| Alta | No se fijaban límites de población, trabajo por tick ni degradación bajo carga | Capacidad de vivienda, población física limitada, presupuestos de simulación y colas de trabajo |
| Alta | El generador de edificios se llamaba “IA” sin separar IA generativa de generación procedural | Arquitecto procedural offline y verificable; no requiere LLM ni conexión externa |
| Alta | La generación propia podía competir con mods que reemplazan mazmorras o añaden estructuras grandes | Perfiles de compatibilidad, etiquetas, control de densidad y reglas de no duplicación por categoría |
| Alta | La progresión de estadísticas, razas, afinidades y elementos podía apilar bonificaciones sin límites | Separación de capas, fórmulas de atributos derivados y límites de acumulación |
| Alta | El roadmap mezclaba prototipo técnico con el catálogo final completo | Vertical slice obligatoria antes de ampliar las 16 familias, 15 clases y 44 subclases |
| Media | Algunas reglas de calendario, viajes, combate a distancia y desconexión no estaban operacionalizadas | Reloj de simulación, rutas abstractas, resolución por sectores y recuperación segura |
| Media | La aceptación de sistemas se describía en términos generales | Pruebas de integridad, rendimiento, compatibilidad, migraciones y exploits con criterios medibles |

**Estado de las decisiones:** “Cerrado” significa regla de diseño adoptada; “base de balance” significa valor inicial que deberá probarse; “contenido de fase posterior” significa que está diseñado, pero no forma parte del primer prototipo jugable. No se presentará ningún sistema como programado hasta que exista código y pruebas.

## 0. Propósito y criterio de diseño

MEDIEVAL FACTIONS será un mod de fantasía medieval, sandbox y RPG con simulación de población, facciones, asentamientos, economía, profesiones, combate, magia, diplomacia, construcción y exploración. Su característica principal no será una cantidad aislada de bloques o criaturas, sino que todos los sistemas compartirán reglas y se influirán entre sí.

El mundo continuará funcionando aunque el jugador se encuentre lejos o no esté conectado. Las facciones NPC producirán, comerciarán, construirán, reclutarán, explorarán, negociarán y combatirán. Los jugadores podrán participar en esa simulación como gobernantes, aventureros, artesanos, comerciantes, exploradores, soldados, diplomáticos o cualquier combinación compatible con las reglas de personaje.

### 0.1 Los diez principios que gobiernan el mod

1. **Mundo autónomo:** el mundo no espera al jugador mientras el servidor está ejecutándose, aunque no haya usuarios conectados. Si el proceso del servidor se apaga, el tiempo de simulación se detiene salvo recuperación offline limitada y configurable.
2. **Reglas simétricas:** jugadores y facciones Full NPC usan las mismas reglas económicas, territoriales, productivas y militares. Las diferencias serán de interfaz y de control, no de privilegios artificiales.
3. **Causalidad económica:** ningún trabajador crea recursos, objetos o dinero de la nada. Toda producción consume recursos, tiempo, herramientas, capacidad y mantenimiento.
4. **Identidad sin encasillamiento:** raza, afinidad, elemento, clase, subclase, profesión y equipo son sistemas distintos.
5. **Profundidad por interacción:** los sistemas deben generar decisiones y consecuencias, no microgestión sin valor.
6. **Autonomía con límites:** la IA puede resolver operaciones ordinarias, pero no tomar decisiones políticas irreversibles en nombre de un jugador.
7. **Construcción legible:** los edificios son estructuras reales del mundo y sus funciones se verifican mediante componentes y estaciones identificables.
8. **Persistencia responsable:** la simulación distante se abstrae para proteger rendimiento; al reanudar se recuperan costes, viajes, riesgos y resultados registrados, sin teletransportar ejércitos ni inventar recursos.
9. **Contenido extensible:** razas, profesiones, recetas, habilidades, edificios, eventos y encargos deben poder ampliarse mediante datos.
10. **Minecraft primero:** el mod debe conservar la exploración, construcción y libertad espacial de Minecraft.

### 0.2 Qué no será el mod

No será un juego de estrategia separado que sustituya Minecraft; no será una simulación urbana de alcantarillado, limpieza y servicios cotidianos; no será un MMO con servicios externos obligatorios; no tendrá generación ilimitada de dinero; no tendrá teletransporte logístico; no obligará al jugador a ser rey ni a gestionar una facción; no tendrá escalado automático de enemigos al nivel del jugador; no prometerá simulación real de cada NPC cuando esté lejos; no dependerá de un servicio de IA en la nube para construir o gestionar el mundo.

### 0.3 Cómo leer esta especificación

- **Regla cerrada:** forma parte del diseño de referencia y solo debe cambiarse mediante una revisión explícita del documento.
- **Base de balance:** número o parámetro inicial para pruebas; puede ajustarse sin cambiar la fantasía del sistema.
- **Objetivo técnico:** criterio de aceptación que debe medirse en prototipo; no es una garantía previa.
- **Fase posterior:** contenido diseñado para una expansión del núcleo, no requisito para el primer prototipo.
- **Integración opcional:** compatibilidad que se activa únicamente si el mod externo está instalado y supera pruebas.

Cuando dos apartados parezcan contradecirse, prevalece este orden: principios del proyecto → Parte V de auditoría v1.3 → reglas de auditoría v1.2 → sistema específico más reciente → catálogo de contenido. Las referencias externas sirven para verificar compatibilidad o inspirar principios; no definen por sí mismas las mecánicas de MEDIEVAL FACTIONS.

---

# PARTE I — LOS OCHO BLOQUES DE CIERRE

Los ocho bloques siguientes completan los apartados que necesitaban más definición para que el diseño sea coherente y pueda transformarse en un plan de producción. No sustituyen los sistemas ya acordados: los ordenan, completan sus huecos y fijan reglas comunes.

## 1. Catálogo racial y generación de personajes

### 1.1 Las 16 familias raciales iniciales

El catálogo inicial se fija en estas 16 familias: Humanos, Elfos, Enanos, Orcos, Hombres bestia, Goblins, Dracónidos, Vampiros, No muertos, Feéricos, Celestiales, Demonios, Sirénidos, Centauros, Gigantes y Arpías. Cada familia tendrá variantes y subrazas. La afinidad Solar o Lunar no será una raza ni una subraza: será una propiedad individual separada.

| Familia | Dos tendencias entre las siete estadísticas base | Rasgo/limitación racial | Afinidad profesional favorable | Afinidad profesional menos favorable |
|---|---|---|---|---|
| Humanos | Inteligencia y Suerte | Sin extremo físico o sensorial; perfil adaptable | Comercio y administración | Extracción pesada |
| Elfos | Destreza y Percepción | Menor Resistencia física | Arquería y conocimiento | Metalurgia pesada |
| Enanos | Resistencia e Inteligencia | Menor Velocidad | Herrería y minería | Mensajería rápida |
| Orcos | Fuerza y Resistencia | Menor Destreza en trabajos de precisión | Guardia y construcción | Joyería de precisión |
| Hombres bestia | Velocidad y Percepción | Menor comodidad/eficiencia con armadura muy pesada | Rastreo y caza | Contabilidad especializada |
| Goblins | Destreza y Suerte | Menor Fuerza física | Alquimia e ingeniería práctica | Caballería pesada |
| Dracónidos | Fuerza e Inteligencia | Las habilidades intensas consumen más resistencia | Metalurgia y artesanía elemental | Tareas de cuidado prolongado |
| Vampiros | Velocidad y Percepción | Necesidad sanguínea; la comida ordinaria no sustituye plenamente su sustento | Alquimia y diplomacia | Agricultura convencional |
| No muertos | Resistencia y Suerte | Curación biológica convencional reducida; necesitan métodos compatibles | Minería y vigilancia | Medicina convencional |
| Feéricos | Destreza e Inteligencia | Menor Fuerza física | Herboristería y magia | Cantería pesada |
| Celestiales | Inteligencia y Resistencia | Menor facilidad para el sigilo | Medicina y protección | Infiltración |
| Demonios | Fuerza e Inteligencia | Vulnerabilidad específica a ciertos efectos sagrados/lumínicos | Herrería arcana y combate | Oficios de cuidado pastoral |
| Sirénidos | Percepción y Velocidad | Movilidad terrestre prolongada penalizada sin equipo adaptado | Pesca y exploración acuática | Minería de montaña |
| Centauros | Fuerza y Velocidad | Limitaciones de espacio, escaleras y monturas | Transporte y exploración | Oficios de espacios muy reducidos |
| Gigantes | Fuerza y Resistencia | Menor Velocidad y restricciones en espacios pequeños | Cantería y construcción | Joyería de precisión |
| Arpías | Velocidad y Destreza | Menor resistencia a impactos físicos directos | Mensajería y exploración | Minería subterránea prolongada |

Las dos tendencias raciales son sesgos de generación y potencial, no bonificaciones porcentuales adicionales que se sumen a todo el equipo. Todos los personajes reciben un presupuesto de atributos base comparable; la raza distribuye ese presupuesto y orienta el potencial genético. La afinidad profesional favorable y la menos favorable modifican inicialmente la experiencia obtenida en esa familia profesional en +5 % y -5 %, respectivamente. Son diferencias leves: no bloquean profesiones, no cambian recetas y no sustituyen el nivel individual. Las limitaciones raciales son condiciones jugables separadas y deben tener contramedidas razonables.

### 1.2 Subrazas

Cada familia tendrá entre dos y cuatro subrazas oficiales en el lanzamiento completo. La subraza ajustará apariencia, cultura, ciclo vital y pequeños modificadores; no creará dieciséis árboles de reglas independientes. Los nombres y las historias de las subrazas se redactarán en los libros de lore, conservando una estructura de datos compartida.

Ejemplos de líneas que servirán como base de contenido: humanos de llanura, costa y montaña; elfos silvestres, elevados y umbríos; enanos de montaña, forja y profundidad; orcos de hierro, ceniza y estepa; hombres bestia lupinos, felinos y ursinos; goblins cavernarios, boscosos e inventores; dracónidos escamados, de sangre wyrm y dracos; vampiros aristocráticos, sanguíneos y nocturnos; no muertos retornados, esqueléticos y espectrales; feéricos silvanos, alados y feéricos del agua; celestiales del alba, astrales y serafines; demonios ígneos, abisales e infernales; sirénidos de arrecife, marea y profundidad; centauros de bosque, llanura y estepa; gigantes pétreos, glaciales y tormentosos; arpías de acantilado, cielo y crepúsculo.

Estas líneas son familias de subraza, no afinidades elementales. Un elfo umbrío, por ejemplo, puede ser Solar o Lunar de acuerdo con la generación individual.

### 1.3 Compatibilidad racial y pertenencia a facción

La facción tiene una **familia racial principal**, no una única subraza obligatoria. Por tanto, cualquier NPC de una subraza perteneciente a esa familia podrá solicitar incorporación o ser reclutado. Se evita así que una facción élfica tenga que rechazar a un elfo por pertenecer a otra comunidad élfica.

Los híbridos oficiales podrán incorporarse a una facción de una de sus familias de origen únicamente cuando su ficha de compatibilidad indique pertenencia válida. No se permitirá usar esta regla para saltarse incompatibilidades biológicas o restricciones de lore.

Las relaciones reproductivas usan tres niveles: compatibilidad alta (posible descendencia híbrida), limitada (descendiente de la raza principal con rasgos heredados) e incompatible (sin descendencia). Las combinaciones válidas se registrarán en una matriz de datos, no mediante excepciones improvisadas.

### 1.4 Generación individual

El jugador elige familia/subraza, clase inicial y profesión principal. La afinidad se sortea al 50 % entre Solar y Lunar; después se sortea uno de los seis elementos de esa afinidad. Cada una de las doce combinaciones tiene una probabilidad inicial del 8,33 %. El jugador no podrá elegir, cambiar o volver a tirar afinidad y elemento de forma ordinaria.

Los NPC usan el mismo procedimiento, ajustado por cultura, origen, rareza y contexto narrativo. La rareza podrá influir en la frecuencia de aparición de una familia, pero nunca alterará las reglas de afinidad.

### 1.5 Atributos Solar y Lunar

**Solar** tiene dos atributos:

- **Vitalidad:** durante el día aumenta la regeneración de vida y la resistencia al daño.
- **Energía:** aumenta la velocidad de ataque y el daño de habilidades.

**Lunar** tiene dos atributos:

- **Percepción:** durante la noche aumenta la velocidad de movimiento y concede visión nocturna mejorada.
- **Espiritualidad:** aumenta el sigilo y la evasión.

Los efectos dependientes del día/noche siguen el reloj del mundo, no la visibilidad del cielo. Funcionan bajo techo, en cuevas, con tormenta o en dimensiones cerradas. Solar/Lunar no equivale a bueno/malo.

### 1.6 Elementos y debilidades

Solar: Fuego, Luz, Rayo, Tierra, Metal y Éter.  
Lunar: Agua, Hielo, Aire, Oscuridad, Naturaleza y Espíritu.

Cada elemento aporta dos bonificaciones y una vulnerabilidad. Para evitar que se convierta en un counter absoluto, la primera base de balance será: bonificación de afinidad de +8 % en la estadística/efecto temático, resistencia temática de +8 % cuando la tabla la indique y vulnerabilidad de +10 % frente a la fuente indicada. Son valores de ensayo, no números finales. Las bonificaciones se suman antes de aplicar límites; no se multiplican en cadena. Ninguna vulnerabilidad elemental podrá eliminar de un golpe a un personaje de nivel equivalente.

| Elemento | Bonificaciones | Vulnerabilidad principal |
|---|---|---|
| Fuego | Daño de habilidades ígneas; resistencia al fuego | Recibe más daño de Agua |
| Luz | Potencia de curación; resistencia a miedo/maldiciones | Recibe más daño de Oscuridad |
| Rayo | Velocidad de ataque; velocidad de movimiento | Recibe más daño de Tierra |
| Tierra | Defensa física; resistencia al empuje | Recibe más daño de Aire |
| Metal | Penetración de armadura; resistencia física | Recibe más daño de Rayo |
| Éter | Regeneración de maná; reducción moderada de enfriamientos | Recibe más daño de Espíritu |
| Agua | Recuperación de resistencia; resistencia a calor | Recibe más daño de Rayo |
| Hielo | Defensa; resistencia a ralentizaciones | Recibe más daño de Fuego |
| Aire | Velocidad de movimiento; evasión | Recibe más daño de Tierra |
| Oscuridad | Sigilo; probabilidad de crítico | Recibe más daño de Luz |
| Naturaleza | Regeneración de vida; resistencia a veneno | Recibe más daño de Fuego |
| Espíritu | Poder espiritual; resistencia a control mental/espiritual | Recibe más daño de Éter |

No habrá simulación ambiental compleja para cada elemento. Su función principal será estadística, de afinidad, de compatibilidad de equipo y de interacciones de combate.

## 2. Profesiones, conocimientos y gremios

### 2.1 Reglas definitivas

Cada personaje jugador tendrá una profesión principal. Podrá realizar tareas sencillas de otras profesiones, pero con menor velocidad, peor calidad y menor probabilidad de éxito. Base inicial: 50 % de velocidad y una categoría de calidad menos frente a un profesional equivalente; las tareas avanzadas, certificadas o peligrosas exigirán profesión y conocimientos pertinentes. No se podrá sustituir un profesional experto con trabajo secundario. Los NPC tienen una profesión principal asignada, nivel, estadísticas y potencial individual.

Cada profesión progresa de nivel 1 a 50. Rangos: Aprendiz (1–10), Trabajador (11–20), Profesional (21–30), Experto (31–40) y Maestro (41–50). La experiencia se obtiene realizando trabajos relevantes, no repitiendo tareas triviales sin límite.

Cada trabajador mantiene una tarea activa y una cola ordenada de encargos. Un trabajo consume tiempo, materiales, herramientas, combustible cuando corresponda, mantenimiento, salario y comida. La falta de recursos o mantenimiento detiene el trabajo de forma visible; nunca produce objetos gratuitos.

### 2.2 Catálogo consolidado

| Familia profesional | Profesiones | Edificio principal |
|---|---|---|
| Extracción | Minero, Talador, Cantero, Prospector, Cazador, Recolector | Mina, campamento forestal, cantera, puesto de prospección |
| Metal y equipo | Fundidor, Herrero, Armero, Artesano de armas a distancia | Fundición, herrería, armería, taller de arquería |
| Artesanía | Carpintero, Curtidor, Sastre, Joyero, Artesano mágico, Alquimista | Carpintería, taller de cuero, sastrería, joyería, taller arcano, laboratorio |
| Construcción e ingeniería | Constructor, Arquitecto, Albañil, Ingeniero | Oficina de construcción, estudio, cantería, taller de ingeniería |
| Alimentación y animales | Agricultor, Ganadero, Apicultor, Pescador, Cocinero/Panadero, Carnicero, Molinero | Granja, establos/corrales, colmenar, muelle/pesquería, cocina, carnicería, molino |
| Comercio y finanzas | Comerciante, Tasador, Banquero, Tesorero, Maestro de la Moneda, Contable, Recaudador, Subastador | Mercado, casa comercial, banco, ayuntamiento, casa de moneda, casa de subastas |
| Logística | Transportista, Carretero, Caravanero, Mensajero, Cartógrafo | Almacén, establo de carga, oficina de caravanas, mensajería, cartografía |
| Conocimiento | Bibliotecario, Escriba, Maestro, Investigador, Investigador arcano, Maestro elemental | Biblioteca, archivo, escuela, centro de investigación, academia mágica |
| Salud y asistencia | Médico, Veterinario | Clínica/hospital, enfermería veterinaria |
| Gobierno y servicios | Diplomático, Juez, Guardia, Sacerdote, Administrador de asentamiento | Embajada, tribunal, cuartel, templo, ayuntamiento |

El cirujano será una especialización avanzada del Médico; Pastor será una especialización del Ganadero; Criador de caballos/monturas será una especialización del Ganadero; Mayorista, Tasador y Corredor serán especializaciones del Comerciante; Prestamista será especialización del Banquero. Arquitecto e Ingeniero serán especializaciones avanzadas del Constructor; Albañil seguirá siendo oficio propio de mampostería. Carretero y Caravanero serán especializaciones del Transportista; Cartógrafo será especialización de Exploración/Conocimiento. Herbolario será especialización del Recolector o Alquimista según su función. Capitán de guardia será un rango/cargo, no una profesión independiente. Soldado y mercenario serán carreras militares, no profesiones económicas.

Tesorero, Recaudador, Maestro de la Moneda y Administrador de asentamiento son **cargos funcionales** asignados a personajes con conocimientos pertinentes; no necesitan un nivel profesional paralelo. Diplomático, Juez, Guardia y Sacerdote sí pueden ser carreras principales con progresión propia, pero su autoridad depende de nombramiento, institución y legitimidad. Un personaje puede tener una profesión principal y ocupar un cargo compatible; el cargo no crea una segunda profesión ni concede automáticamente conocimientos.

### 2.3 Conocimientos compartidos

Las profesiones conservarán experiencia y nivel separados, pero determinadas técnicas se compartirán mediante un sistema de conocimientos:

- Herrero–Fundidor: metales, aleaciones y tratamientos térmicos.
- Herrero–Armero: componentes metálicos y resistencia de equipo.
- Carpintero–Constructor: estructuras, ensamblaje y componentes de madera.
- Constructor–Albañil–Ingeniero: cimientos, materiales, mecanismos y planificación.
- Minero–Prospector: minerales, vetas y evaluación de yacimientos.
- Agricultor–Ganadero: alimentación, cultivos y cuidado de recursos vivos.
- Ganadero–Veterinario: anatomía, reproducción y cuidado animal.
- Alquimista–Herbolario: ingredientes, plantas y preparados.
- Médico–Cirujano: anatomía, heridas y procedimientos.
- Comerciante–Tasador–Banquero: valoración, crédito y mercados.
- Bibliotecario–Escriba–Maestro: documentos, lectura y enseñanza.
- Investigador–Investigador arcano–Maestro elemental: metodología, teoría y experimentación.
- Transportista–Carretero–Caravanero–Mensajero: logística, cargas y rutas.

Un conocimiento compartido facilita tareas y desbloquea técnicas básicas, pero no sustituye la profesión principal ni permite fabricar productos expertos sin la capacitación exigida. Los libros enseñan conocimientos; un maestro transmite técnicas; la experiencia práctica eleva el nivel.

### 2.4 Gremios

Los gremios serán organizaciones opcionales de trabajadores de una profesión o grupo relacionado. Pueden contratar encargos, financiar herramientas, enseñar conocimientos, mantener aprendices, certificar maestros, fijar estándares de calidad y negociar contratos con ayuntamientos o facciones. No crean materiales ni experiencia. Un asentamiento pequeño puede tener un taller sin gremio; los gremios completos son más propios de pueblos grandes y ciudades.

### 2.5 Producción encadenada

Ejemplo: el Minero extrae hierro; el Transportista lo lleva al asentamiento; el Fundidor produce lingotes; el Carpintero prepara componentes de mango; el Herrero fabrica la espada; el Comerciante la vende o distribuye. Cada eslabón necesita inventario, trabajador, estación, tiempo y pago. Los NPC pueden colaborar mediante órdenes automáticas de producción y reposición.

## 3. Progresión, combate, magia y equipamiento

### 3.1 Clases

El personaje elige una clase principal permanente. Catálogo: Guerrero, Arquero, Ballestero, Mago, Paladín, Asesino, Berserker, Druida, Nigromante, Monje, Invocador, Bardo, Espadachín arcano, Clérigo y Lancero.

Cada clase tendrá tres subclases, excepto Bardo, que tendrá dos:

| Clase | Subclases iniciales |
|---|---|
| Guerrero | Guardián, Maestro de armas, Vanguardia |
| Arquero | Ojo certero, Explorador, Viento veloz |
| Ballestero | Tirador de precisión, Ballestero de asedio, Repetidor |
| Mago | Elementalista, Arcanista, Mago de batalla |
| Paladín | Cruzado, Templario, Guardián sagrado |
| Asesino | Sombra, Envenenador, Hoja nocturna |
| Berserker | Devastador, Furia sangrienta, Coloso |
| Druida | Guardián del bosque, Llamabestias, Cambiapieles |
| Nigromante | Señor de huesos, Vinculaalmas, Adepto lich |
| Monje | Puño de hierro, Camino del flujo, Asceta |
| Invocador | Vinculapactos, Conjurador, Comandante espiritual |
| Bardo | Soporte, Potenciación física |
| Espadachín arcano | Hoja hechizada, Duelista rúnico, Hoja del vacío |
| Clérigo | Sanador, Exorcista, Inquisidor |
| Lancero | Caballero de lanza, Falanx, Hostigador |

### 3.2 Árbol de habilidades

Cada clase tendrá un árbol de aproximadamente 55–80 nodos, con núcleo común y rutas de subclase. El catálogo final representa 44 rutas de especialización (tres por clase salvo Bardo); por escala, el primer prototipo implementará una sola clase completa y una subclase funcional antes de producir el resto. El número de nodos es capacidad de diseño, no promesa de que todos se puedan desbloquear en una partida. Tipos de nodos: estadísticas directas, pasivas, activas, mejoras de habilidad, desbloqueo de subclase, ranuras y maestría.

Nivel máximo 65. Se obtienen 32 puntos de talento, uno en cada nivel impar desde el 3 hasta el 65. Los hitos son: fundamentos (1–10), especialización (11–25), desarrollo de subclase (26–40), dominio (41–55) y maestría (56–65).

La subclase se desbloquea mediante nivel, progreso central, nodo específico y prueba/entrenamiento. No exige valores concretos de estadísticas. Solo puede estar activa una subclase. Se pueden explorar rutas generales de otras subclases, pero no activar habilidades exclusivas ni maestrías de otra. Cambiar de subclase exige un procedimiento excepcionalmente raro y costoso; la clase principal no cambia.

La barra de habilidades permite equipar activas, consultar costes y enfriamientos y guardar configuraciones. No se puede reorganizar durante combate. Las pasivas aprendidas permanecen activas sin ocupar ranura. Las habilidades pueden desbloquearse también por libros, objetos, maestros, jefes, misiones y expediciones.

Ranuras de activas: magia 4 inicial/5 máxima; distancia 3/5; cuerpo a cuerpo 3/4; híbridas según clase. Los nodos de ranura aumentan el máximo global de la categoría, no una reserva por cada subclase.

### 3.3 Recursos de combate y magia

El combate utiliza vida, resistencia y, para clases que corresponda, maná o energía especializada. La resistencia alimenta esquivas, bloqueos, carreras, golpes pesados y vuelo/montura cuando aplique. El maná alimenta hechizos y habilidades mágicas. Costes y enfriamientos deben impedir cadenas ilimitadas, sin convertir cada acción en una espera excesiva.

La magia se organiza por elementos y escuelas. Los hechizos pueden ser ofensivos, defensivos, de control, curación, apoyo, invocación o utilidad. Su potencia depende de clase, subclase, elemento, estadísticas, conocimientos, catalizador y calidad del equipo. Un hechizo no se vuelve exclusivo por poseer un objeto: se necesita cumplir los requisitos para activarlo.

Éter representa canalización de maná, runas y encantamiento arcano; Espíritu representa almas, esencia y vínculo espiritual. No se mezclan en una única escuela. No habrá simulación ambiental elemental compleja en la primera versión.

### 3.4 Bardo

El Bardo de Soporte potencia aliados y aporta protección grupal; el Bardo de Potenciación Física obtiene mejoras personales más potentes y mejoras menores para aliados. Las canciones se interpretan con instrumento y tiempo de lanzamiento; el efecto continúa cuando el Bardo deja de tocar. Se pueden mantener varias canciones diferentes simultáneamente, con un límite inicial de tres. Canciones poderosas tienen mayor tiempo de interpretación, intervalo o recuperación compartida; recibir daño puede interrumpir la interpretación. El Bardo puede combatir después con puños/guanteletes fabricados, conservando los efectos activos.

### 3.5 Equipamiento

Siete tiers de equipo: Rudimentario, Común, Refinado, Raro, Épico, Maestro y Reliquia. La calidad de fabricación es independiente del tier: defectuoso, normal, bueno, excelente y excepcional. Un arma de tier alto no tiene que ser automáticamente de calidad excepcional.

Armaduras: ligera, media, pesada y mágica. Las piezas individuales y accesorios pueden formar conjuntos con bonificaciones. Hay objetos normales, mágicos, elementales, únicos, raciales, de afinidad, de clase y de subclase. Los objetos restringidos pueden ser obtenidos, guardados o comerciados por cualquier jugador; solo quienes cumplen los requisitos activan sus propiedades especiales.

Guerrero usa espadas de una mano, espadones, katanas, hachas, martillos de guerra, lanzas y escudos; no usa mazas como arma característica. Paladín usa armas sagradas, martillos de guerra, mazas, escudos y martillos sagrados a dos manos.

### 3.6 Fabricación modular

La fabricación de armas y armaduras utiliza componentes combinables inspirados en la lógica de Tinkers' Construct, sin copiar su implementación. El Herrero fabrica armas y componentes; el Armero fabrica armaduras, escudos y protecciones; el Fundidor transforma mineral en lingotes, metal líquido y aleaciones mediante moldes y vertido. Herrero y Armero comparten materiales e infraestructura, pero conservan progreso independiente.

La Tool Station y las estaciones avanzadas son desbloqueos de progresión: requieren planos, herramientas de precisión, madera resistente, materiales refinados, nivel profesional y edificio adecuado. La fabricación visual puede representar calentamiento, martillado, conformado, templado y acabado. Un fallo reduce calidad y puede consumir parte de los materiales; un buen trabajo puede recuperar sobrantes. La probabilidad depende de nivel, estadísticas, herramientas, estación, materiales, receta y complejidad.

## 4. Mundo, exploración, lore y mazmorras

### 4.1 Mundo y asentamientos generados

El mundo utilizará una generación Vanilla+ determinista, organizada por biomas, zonas ecológicas y regiones de exploración. La selección de estructura nunca se realizará como consecuencia de un tick de entidad: la colocación inicial pertenece a la generación de chunks y el estado dinámico posterior se guarda por instancia. Se conservará la identidad del terreno de Minecraft y se añadirán biomas originales selectivos, rasgos geológicos, cuevas, recursos, estructuras culturales y puntos de interés con densidad controlada. Las facciones iniciales se ubicarán en regiones aptas; nuevas colonias aparecerán después por expansión autónoma y cumplirán reglas de población, economía, territorio y logística. La especificación completa está en los apartados 21–22.

### 4.2 Exploración y conocimiento del mapa

Mapa 3D interactivo disponible desde el comienzo. Muestra asentamientos y territorios conocidos, no una visión omnisciente. La información llega por exploración directa, exploradores, cartógrafos, mensajeros, patrullas, tratados y espionaje. Los datos pueden quedar anticuados: una frontera, ruta o puesto enemigo puede cambiar mientras el mapa no se actualiza.

El mapa puede representar caminos, zonas, edificios conocidos, recursos localizados, rutas, fronteras, presencia enemiga y lugares de interés. La precisión depende del nivel del cartógrafo, la información recibida y la seguridad de la ruta.

### 4.3 Lore

El lore se transmite mediante libros de familia, crónicas globales, documentos contradictorios, ruinas, inscripciones, rumores, secretos y leyendas. Ninguna facción tendrá toda la verdad. Los textos de una cultura pueden contradecir a los de otra; los hallazgos deben permitir al jugador construir una interpretación. La historia global compartida sirve de marco común, pero no elimina versiones locales.

### 4.4 Expediciones

El Ayuntamiento organiza expediciones a ruinas, mazmorras, yacimientos, territorios desconocidos y regiones peligrosas. Una expedición selecciona destino, objetivo, duración estimada, participantes, roles, suministros, herramientas, monturas, escolta y capacidad de carga. Roles posibles: explorador, escolta, cartógrafo, prospector, investigador, médico y transportista.

El informe distingue descubrimientos, pérdidas, heridos, recursos, información incompleta y objetivos fallidos. Expediciones Full NPC usan las mismas reglas de riesgo y coste. Algunas historias o jefes excepcionales podrán requerir intervención del jugador, pero no se bloqueará la actividad cotidiana de las facciones detrás del jugador.

### 4.5 Mazmorras y jefes

Las mazmorras son estructuras persistentes y compartidas, con generación modular, rutas legibles, encuentros, puzzles con pistas, llaves temáticas, mini-jefes, jefes por fases y tesoros finitos. Se clasifican por tamaño y peligrosidad; no escalan automáticamente con el nivel del jugador. Los NPC pueden organizar incursiones con costes, suministros, riesgos y resultados registrados. La especificación integral y el catálogo inicial se detallan en el apartado 23.

## 5. Asentamientos, construcción y logística

### 5.1 Niveles

Niveles independientes por asentamiento: Campamento, Aldea, Pueblo, Villa, Ciudad y Capital. Una facción puede tener muchos asentamientos y una sola Capital oficial. Cada nivel aumenta población, territorio, edificios permitidos, capacidad económica y complejidad administrativa. La promoción exige población, edificios, economía, recursos, seguridad e infraestructuras.

### 5.2 Ayuntamiento

El Ayuntamiento es obligatorio para fundar y desarrollar oficialmente un asentamiento. Gestiona población, edificios, trabajadores, solicitudes de incorporación, recursos, territorio, presupuesto, obras, envíos y expediciones. Su nivel controla capacidad administrativa y puestos municipales, no la cantidad de profesiones que pueden existir.

Toda ciudad dispone de un Ayuntamiento adaptado a su escala. Un Ayuntamiento puede recibir mejoras. El primer nivel incorpora una clínica básica; un hospital completo es una infraestructura avanzada.

### 5.3 Construcción

La Mesa de Arquitectura permite describir un edificio por función, tamaño, plantas, distribución, materiales y estilo. Un generador procedural interno y offline produce un boceto, dimensiones, lista de materiales, distribución y proyección fantasma. En v1.x, “IA arquitectónica” significa planificador procedural basado en reglas, gramáticas de salas, módulos y validación; no significa un modelo generativo de lenguaje ni una llamada a un servicio externo. La entrada libre se interpreta con un vocabulario acotado y controles estructurados; cuando no se pueda interpretar, la interfaz solicita elegir función, tamaño o estilo. La generación es reproducible y validable.

El jugador revisa, mueve, rota, coloca y aprueba el diseño. El sistema verifica terreno, espacio, recursos, requisitos funcionales, permisos y nivel del asentamiento. Después puede ordenar que los NPC construyan por etapas o seguir la proyección bloque a bloque. La obra es cancelable y los materiales no consumidos se recuperan; los colocados se recuperan según el estado y la política del edificio.

Los edificios profesionales utilizan plantillas funcionales verificables: deben tener estación, componentes, acceso y capacidad requeridos. Los edificios personalizados pueden ser visualmente libres, pero tienen que cumplir los requisitos de función. No se considerará que un edificio es una herrería por su aspecto si no dispone de las estaciones reconocidas.

### 5.4 Capacidad y puestos avanzados

Los edificios especializados tienen capacidad de trabajadores, estaciones, almacenamiento y mejoras. El Ayuntamiento no sustituye a los talleres. Una profesión puede compartir edificio con otra relacionada cuando existan estaciones separadas.

Desde Ciudad se pueden construir puestos avanzados. Capacidad inicial: dos; mejoras: hasta cuatro. Tipos iniciales: vigilancia y minería. Necesitan constructor, trabajadores, comida, suministros, mantenimiento y logística. Son vulnerables a ataques y pueden ser desmantelados. El desmontaje recupera parte de los materiales según desgaste y daños. Si los trabajadores pasan hambre durante demasiado tiempo pueden morir y perder equipo/recursos.

### 5.5 Territorios y caminos

Los territorios muestran zonas controladas, reclamadas, disputadas y fronteras. Las facciones de jugadores pueden ampliar territorio con mayor facilidad que Full NPC, pero cada expansión eleva mantenimiento y riesgo de conflictos. Fuera de territorio propio la construcción profesional requiere permiso, derechos o una excepción aprobada; los puestos temporales y expediciones siguen reglas específicas.

Caminos: senderos, caminos de tierra, pavimentados y carreteras principales. Mejoran desplazamiento según terreno y mantenimiento. Puentes y túneles pueden ser construcciones especiales. No se introduce una red municipal compleja de alcantarillado, iluminación o limpieza.

### 5.6 Logística y mensajería

Los mensajeros llevan cartas, mensajes, documentos y paquetes pequeños. Cargas grandes requieren transportistas, caballos, carretas, caravanas y eventualmente escoltas. El Ayuntamiento permite seleccionar destino, mercancía, cantidades, transporte, personal y protección. Los envíos recorren físicamente la ruta; su estado puede ser pendiente, en tránsito, entregado, retrasado o interceptado.

El peso, volumen, distancia, velocidad, resistencia, camino, clima, peligro y escolta afectan la duración y el riesgo. No hay transporte marítimo comercial en la primera versión. Las rutas comerciales permanentes usan caravanas automáticas y oferta, demanda, precios, recursos, acuerdos, seguridad y distancia.

## 6. Simulación de población, economía, política y sociedad

### 6.1 Identidad y ciclo vital

Cada NPC tiene nombre, raza/subraza, cultura, afinidad, elemento, edad, sexo, apariencia, estadísticas, potencial, personalidad, familia, relaciones, profesión, nivel, inventario, necesidades, historial y vínculos. Etapas: bebé, niño, adolescente, adulto y anciano. El calendario del mundo usa seis estaciones de tres meses, 21 días por mes y 8 días por semana; un año suma 378 días. La base temporal será un día de simulación de 24.000 ticks de mundo (20 minutos reales a 20 TPS), salvo que la configuración del servidor defina otra escala. El reloj de simulación se guarda aparte del reloj visual del cielo para evitar que comandos de administración o cambios de día dupliquen envejecimiento, salarios o cosechas.

Las razas tienen ciclos biológicos diferentes: maduración, longevidad, fertilidad, gestación, recuperación y envejecimiento. Las estadísticas físicas pueden declinar en vejez; inteligencia, destreza y conocimiento pueden crecer con experiencia. Las herencias transmiten predisposiciones, no niveles ni experiencia.

### 6.2 Familia y relaciones

NPC pueden formar parejas, casarse, tener hijos y construir linajes. El matrimonio formaliza la relación, pero no es requisito biológico. La intimidad requiere aceptación mutua y no garantiza embarazo. La gestación tiene duración por raza, etapas, necesidades médicas, riesgos y parto. La madre no trabaja durante gestación y recuperación posparto. La atención médica reduce riesgos, no los elimina.

Los hijos de jugadores con NPC son NPC autónomos. El jugador puede orientarles u ordenarles tareas dentro de límites de personalidad, relación y circunstancias. No tienen estadísticas superiores por ser hijos del jugador.

### 6.3 Personalidad y necesidades

Personalidad afecta lealtad, ambición, prudencia, agresividad, sociabilidad, generosidad, disciplina, confianza, preferencias y aceptación de órdenes. Necesidades básicas: alimento, agua, vivienda, salario/ingresos, descanso, salud, seguridad y relaciones. Satisfacción individual y agregada influye en productividad, migración, crecimiento, estabilidad, protestas, huelgas, desobediencia y rebelión.

### 6.4 Salud y muerte

NPC pueden morir por edad, enfermedad, hambre, sed, guerra, asaltantes, accidentes, criaturas, heridas y ambiente. La muerte afecta familias, empleo, patrimonio, población y estabilidad. Se admiten funerales, herencias y pérdida de equipo. Las clínicas ofrecen atención básica; hospitales avanzados reducen mortalidad. Médicos NPC progresan por nivel profesional, sin árbol de combate complejo.

### 6.5 Herencia y linajes

La herencia es automática y puede complementarse con testamento. El gobernante interviene en títulos, sucesión política y disputas públicas, pero no confisca arbitrariamente patrimonio privado. Linajes conservan árbol genealógico, miembros, matrimonios, descendencia, patrimonio, reputación, títulos, historia, escudo, colores, símbolo y lema. La heráldica puede heredarse, combinarse por matrimonio y crear ramas.

### 6.6 Ascensión híbrida del jugador

En el contenido avanzado, un jugador podrá transformarse permanentemente en una subraza híbrida oficial. La opción solo estará disponible si su raza original figura como compatible con esa subraza y si mantiene una relación sólida con una pareja perteneciente exactamente a la subraza híbrida deseada. La pareja debe aceptar y participar voluntariamente; no puede ser forzada ni sustituida por un objeto. El proceso exige una cadena excepcional de misiones, materiales raros, conocimientos y un ritual o procedimiento específico de esa combinación racial. No será una transformación cosmética ni un cambio libre de raza: solo podrá realizarse una vez y será irreversible salvo una excepción extraordinaria justificada por el lore. La transformación sustituye los rasgos raciales anteriores por los de la subraza híbrida, sin acumular ambas bonificaciones. El jugador conserva clase, subclase, profesión, experiencia, afinidad Solar/Lunar, elemento, habilidades, pertenencias y relaciones. Las estadísticas se recalculan según el nuevo perfil racial y el potencial individual, sin conceder niveles ni equipo gratuitos. Los NPC no podrán realizar esta ascensión: los híbridos NPC solo nacen mediante combinaciones reproductivas compatibles.

### 6.7 Economía

La economía será cerrada y trazable. Todo dinero tendrá origen. Fuentes: capital inicial, acuñación autorizada, tesoros finitos, mazmorras, comercio con compradores que dispongan de presupuesto y contratos financiados. Impuestos son transferencias a tesorería, no destrucción de moneda. Bancos no crean dinero ni pueden prestar depósitos a la vista como si fueran capital propio.

Moneda común: 10 cobres = 1 plata; 10 platas = 1 oro; 100 cobres = 1 oro. Puede existir moneda física y saldo bancario. Las monedas pueden llevar sello de facción, manteniendo el mismo estándar de valor.

Los precios dependen de oferta, demanda, inventario, transporte, escasez, guerra, bloqueos y necesidades locales; cambian gradualmente y dentro de límites. Las tiendas NPC compran solo cuando tienen presupuesto, demanda e inventario compatible. La producción y venta están limitadas por capacidad y consumo real.

Negocios privados, públicos y de facción tienen propietario, capital, inventario, trabajadores, salarios, gastos, mantenimiento, ventas, beneficio/pérdida y riesgo de insolvencia. Bancos gestionan cuentas, depósitos, retiros, transferencias y préstamos respaldados por reservas/capital real. Los préstamos tienen principal, interés, plazo, cuotas, garantía, aval y penalización. Una quiebra puede impedir retiros y causar pérdidas según las reglas del contrato; nunca se crea dinero para cubrirla.

### 6.8 Casa de Moneda

La Casa de Moneda es un privilegio de facción, disponible a partir de Ciudad, con autorización de gobernante/consejo/clanes, seguridad, propiedad de facción y un máximo de una casa oficial por facción. Produce moneda lentamente con metales, combustible, materiales y un Maestro de la Moneda. El señoreaje permite un valor nominal superior al coste material, pero una emisión excesiva puede generar inflación.

La emisión tiene cuota según población, actividad, reservas, circulante, demanda y estabilidad. Puede reducirse o detenerse por falta de materiales, exceso de circulante, inflación o mantenimiento. Se registra acuñación, retirada y destrucción. La acuñación privada no autorizada es falsificación.

### 6.9 Política, diplomacia y religión

Modelos políticos iniciales: monarquía, consejo, clanes, oligarquía comercial y teocracia. Cargos: gobernante, consejo/representantes, tesorero, responsable militar, diplomático y administradores. Los jugadores y NPC pueden ocupar cargos. Las decisiones de gobierno abarcan presupuesto, impuestos, obras, comercio, defensa, población, religión, diplomacia y territorio.

La estabilidad depende de satisfacción, abastecimiento, seguridad, impuestos, legitimidad, economía, conflicto interno y sucesión. La inestabilidad puede provocar protestas, migración, rebelión y cambio de gobierno.

Diplomacia de -100 a +100: Hostil (-100 a -61), Rival (-60 a -21), Neutral (-20 a +20), Amistosa (+21 a +60), Aliada (+61 a +100). Guerra es un estado especial. Tratados: no agresión, alianza, comercio, tránsito, escolta, ayuda militar, tregua, paz, tributos y acuerdos económicos. La IA negocia según recursos, ejército, seguridad, relaciones, personalidad del gobernante, objetivos e intereses.

Religiones configurables con nombre, símbolo, doctrina, entidades/principios, lugares sagrados, clero, seguidores, rituales, festividades, textos y relación con culturas. Edificios: santuario, templo, gran templo/catedral, monasterio y lugares sagrados. La religión afecta cohesión, moral, cultura, festividades, donaciones, educación, diplomacia y conflictos; no obliga a creer y no otorga poderes ilimitados.

## 7. Guerra, monturas, dragones y eventos

### 7.1 Ejército

Ejércitos mixtos de soldados profesionales, milicia y mercenarios. Cada ciudad elige una estructura: profesional, milicia o mixta; las facciones NPC deciden según recursos y riesgos. El reclutamiento es voluntario por defecto, pero el gobierno puede imponer servicio militar con costes sociales y económicos.

Rangos militares: Soldado, Cabo, Sargento, Teniente, Capitán, Comandante y General. Ascensos por experiencia, méritos, liderazgo, estadísticas, lealtad, reputación y formación; los puestos altos son limitados. Los jugadores pueden dirigir escuadras, compañías y ejércitos.

Las batallas combinan combate individual, órdenes tácticas en tiempo real y simulación automatizada cuando el jugador está ausente o lejos. Asedios incluyen murallas, puertas, torres, puentes levadizos, fortificaciones, catapultas, trebuchets, arietes, torres de asedio, balistas y posibles máquinas mágicas. Se puede cortar suministro, agotar moral, negociar rendición o conquistar por asalto, infiltración o tratado.

### 7.2 Mercenarios y prisioneros

Mercenarios son combatientes temporales externos, caros y no ciudadanos automáticos. Contratos especifican número, calidad, especialización, duración, precio, objetivo, ubicación y condiciones. Calidad: básicos, veteranos, élite y compañías especializadas.

Los prisioneros pueden ser soldados, oficiales, mercenarios, exploradores, nobles o gobernantes. Opciones: liberar, intercambiar, pedir rescate, incorporar si aceptan, retener o deportar. Las cárceles consumen comida, vigilancia y presupuesto; puede haber fugas o rescates.

### 7.3 Espionaje

Solo disponible desde Capital con Oficina de Inteligencia. Acciones: reconocimiento, inteligencia militar, investigación económica, infiltración, sabotaje menor y obtención de secretos. Es caro, lento e incierto; puede fracasar, dejar pistas, provocar incidentes diplomáticos o causar captura. Existe contraespionaje.

### 7.4 Monturas

Categorías: comunes, especiales y legendarias. Comunes: caballo, burro, mula y camello. Especiales: lobo gigante, lagarto de monta, ave mensajera y criatura acuática como tortuga gigante. Legendarias: grifo, pesadilla, fénix y roc. Dragones son un sistema separado.

Las monturas normales pueden subir niveles (progresión prevista 1–20) y vínculo en cuatro etapas: sin vínculo, familiar, leal y confianza plena. Suben mediante viajes, saltos, carga, entrenamiento, exploración y combate; el potencial individual limita el crecimiento. El vínculo mejora control y coordinación, no multiplica estadísticas.

Vida y daño siguen un modelo sencillo: puntos de vida, peligros y curación por comida/mecánicas compatibles. No envejecen ni tienen reproducción compleja. Equipo por especie: silla, armadura, alforjas y arnés cuando proceda. Peso afecta velocidad, vuelo y resistencia. Propiedad transferible y comerciable; cada jugador controla una montura activa y puede poseer varias.

Voladoras: normalmente un jinete, algunas dos personas; no transportan grupos de soldados. Vuelo consume resistencia, carga y maniobrabilidad. Monturas acuáticas facilitan exploración y desplazamiento submarino, no comercio naval. NPC usan monturas por función y capacidad económica.

### 7.5 Dragones

Los dragones son extremadamente raros y símbolo de prestigio. Solo jugadores y reyes NPC pueden montarlos. No se domestican adultos salvajes: se consiguen robando huevos de nidos o criando descendencia de dragones propios. La población no tiene un máximo global fijo, pero sí densidad local y reproducción lenta. Un dragón muerto no reaparece automáticamente.

Tienen territorio, guarida, personalidad, elemento, estadísticas, potencial, edad, linaje e historial. Etapas: cría, joven, adulto y anciano. Las crías son dependientes; los jóvenes entrenan y vuelan sin pasajeros; los adultos pueden montarse y combatir; los ancianos conservan gran experiencia y sufren deterioro. Son longevos y no enferman; pueden morir por vejez, hambre, heridas, combate o accidentes.

Los doce elementos son los ya definidos. Estadísticas: vida, fuerza, defensa, velocidad, resistencia, poder elemental, precisión y potencial. Genética transmite predisposiciones, elemento, coloración, rasgos físicos y temperamento; no transmite niveles. La afinidad sigue la regla Solar×Solar = Solar; Lunar×Lunar = Lunar; mezcla = sorteo. El elemento del hijo se sortea entre los seis de su afinidad.

La incubación puede ser natural o artificial de late game. La artificial controla condiciones pero no mejora genética. Antes de eclosionar se asigna propietario; la persona responsable/presente genera la impronta inicial. Vínculo: desconfiado, familiarizado, confiado, leal y vínculo profundo. El dueño es permanente; no se vende, roba ni transfiere. Si el jugador muere, reaparece y conserva el vínculo. Si el rey NPC muere definitivamente, el dragón queda sin dueño y no acepta otro rey.

Máximo dos pasajeros contando propietario; aliado jugador solo con permiso del dueño; rey NPC viaja solo. Puede cargar suministros mediante alforjas/arnés, con penalización de peso. No transporta ejércitos. Combate aéreo admite contramedidas: arqueros, ballestas pesadas, balistas, defensas, resistencia, energía, enfriamientos y heridas. Un dragón no conquista una ciudad por sí solo. No se fabrican armas para dragón; sí protecciones y armaduras de late game.

### 7.6 Eventos y calendario

Seis estaciones, tres meses por estación, 21 días por mes y 378 días por año. Semana de ocho días: Auren, Valdor, Myrien, Kaelor, Tharion, Elyndra, Vaelis y Noxar.

| Estación | Meses |
|---|---|
| Renacimiento | Aural, Floren, Verdalia |
| Ascensión | Solkar, Ignara, Bravium |
| Esplendor | Caldor, Fervia, Dorania |
| Declive | Ocren, Hojaria, Vesper |
| Escarcha | Nivaria, Glacien, Umbriel |
| Letargo | Sombria, Noctara, Eterion |

Eventos dinámicos: festivales, epidemias, invasiones, sequías, inundaciones, incendios, hambrunas, descubrimientos, disputas internas, rebeliones y acontecimientos históricos. También suceden sin jugador conectado, usando simulación abstraída. Cumpleaños y aniversarios registran fecha y pueden originar pequeños eventos de felicidad.

## 8. Arquitectura técnica, interfaz, equilibrio y producción

### 8.1 Plataforma técnica elegida

**Loader inicial: NeoForge para Minecraft 1.21.1.** Se construirá una sola versión principal, no dos loaders en paralelo. La arquitectura interna mantendrá una separación clara entre lógica del mod y adaptadores del loader para facilitar un port futuro, pero no se promete compatibilidad multiloader en el lanzamiento.

**Java objetivo: Java 21.** La documentación oficial de NeoForge para la rama 1.21–1.21.1 establece JDK 21 de 64 bits como requisito y describe el flujo de desarrollo y prueba en cliente y servidor dedicado. La guía oficial de Fabric para 1.21.1 también indica JDK 21, pero Fabric no será el loader de lanzamiento. Fuentes: [NeoForge 1.21.1 — Getting Started](https://docs.neoforged.net/docs/1.21.1/gettingstarted/) y [Fabric 1.21.1 — Development Environment](https://github.com/FabricMC/fabric-docs/blob/main/versions/1.21.1/develop/getting-started/setting-up-a-development-environment.md).

La versión exacta de NeoForge y las herramientas Gradle se fijarán al crear el proyecto y se mantendrán bloqueadas por versión en CI. No se actualizarán automáticamente en mitad de una versión publicada.

### 8.2 Servidor autoritativo y persistencia

Toda regla que cambie el mundo se decide en servidor: inventarios, propiedad, dinero, empleo, combate, diplomacia, construcción, reproducción, rutas, IA y recompensas. El cliente presenta UI, mapas, efectos y animaciones; no decide resultados. Las acciones de interfaz envían órdenes validadas, nunca el resultado ya calculado.

La persistencia se divide por ámbito:

- **Datos globales de mundo:** registro de facciones, relaciones, calendario de simulación, políticas globales, economía agregada, eventos, identificadores de reliquias únicas y migraciones. Se guardan en datos persistentes del nivel principal.
- **Datos de entidad:** identidad, raza, personalidad, profesión, familia, clase y estado de NPC/jugadores. Se adjuntan a la entidad o se guardan en un registro de agentes si el NPC está abstraído y no existe como entidad física.
- **Datos de bloque/edificio:** inventario del taller, trabajo en curso, cola, estado de mantenimiento, permisos y funciones. Permanecen con el bloque/estructura correspondiente.
- **Datos de chunk:** descubrimiento cartográfico, reclamación territorial y marcas locales que deban viajar con el chunk.
- **Datos de objeto:** calidad, componentes modulares, desgaste, dueño cuando corresponda, identificador de artefacto y propiedades de material. Se usarán componentes de datos de ItemStack de Minecraft, no adjuntos de ItemStack obsoletos.

NeoForge 1.21.1 documenta SavedData para información de nivel y recomienda adjuntos para datos específicos de entidades, chunks o block entities; los datos de ItemStack se gestionan mediante data components. La elección concreta se respetará en la implementación. [Fuentes: Saved Data; Data Attachments, NeoForge 1.21.1.]

Todos los registros tendrán `schema_version`, identificadores estables y migraciones idempotentes. Guardar una modificación debe marcar el dato como sucio; la escritura se hará por lotes y con puntos de recuperación para reducir riesgo de corrupción. Ninguna entidad importante dependerá exclusivamente de que su chunk permanezca cargado.

### 8.3 Simulación escalonada y ciclo de vida del servidor

La simulación tiene tres estados distintos:

1. **Servidor en ejecución con jugadores conectados:** simulación completa en proximidad y abstracta lejos del jugador.
2. **Servidor dedicado en ejecución sin jugadores:** el mundo continúa avanzando. Facciones, producción, salarios, rutas, población, diplomacia y eventos se procesan en segundo plano; no necesitan jugadores conectados.
3. **Proceso del servidor apagado:** no hay ticks, pathfinding ni IA. Por defecto el tiempo se pausa. Al reiniciar puede ejecutarse una recuperación offline limitada (por defecto, máximo tres días de calendario del mod) y desactivable; nunca se simula todo el tiempo real transcurrido sin límite.

La recuperación offline no ejecuta una simulación NPC por NPC. Procesa lotes resumidos, limita muertes/eventos extremos por ventana, registra sus causas y permite desactivarla en servidores competitivos.

**Niveles de simulación:**

- **Proximidad activa:** entidades físicas, navegación, animaciones, combate y construcción visible.
- **Región no visible:** agentes persistentes actualizan tareas, necesidades, colas, rutas y sucesos con intervalos más largos.
- **Región distante:** población agrupada por asentamiento; producción, viajes, precios y conflictos se calculan por lotes/eventos.
- **Servidor detenido:** pausa, salvo recuperación limitada al arrancar.

El sistema no cargará chunks continuamente para simular caravanas o ejércitos. Las rutas distantes se representan como tramos entre nodos de una red de caminos; un NPC/caravana se convierte en entidad física al entrar en el área activa. Al volver a descargarse, su estado y posición lógica se guardan.

El planificador utiliza colas de prioridad y presupuesto por tick. Necesidades rutinarias, economía, envejecimiento y eventos no se recalculan cada tick. Se escalonan por ventanas de tiempo y por asentamiento. Si el servidor entra en retraso, se reduce la frecuencia de decisiones no críticas, nunca se saltan pagos, pérdidas, transacciones ni resultados ya comprometidos.

### 8.4 Datos y extensibilidad

Razas, subrazas, elementos, profesiones, conocimientos, recetas, planos, edificios, habilidades, subclases, eventos, tratados, estructuras, biomas compatibles, módulos de mazmorra, contratos y tablas de recompensa deben definirse con datos siempre que sea viable. Las definiciones incluyen identificador estable, versión, requisitos, costes, etiquetas, traducciones, reglas de colocación, validadores y migración. La lógica de contenido se mantiene separada de la interfaz y de la capa de loader; no se permitirá que una receta o una raza requiera acceder a clases internas de un mod opcional. Las integraciones de otros mods serán adaptadores opcionales; un bioma desconocido nunca debe provocar un fallo de carga.

### 8.5 Interfaz

La interfaz se organiza en pantallas contextuales y paneles, no en una única pantalla gigante:

- Panel de personaje: raza, afinidad, elemento, clase, subclase, estadísticas, profesión, necesidades y equipo.
- Árbol de clase y barra de habilidades.
- Libro de profesiones: tareas, recetas, conocimientos, estación, materiales y progreso.
- Ayuntamiento: población, edificios, trabajadores, almacenes, territorio, presupuesto, rutas, expediciones y permisos.
- Panel económico: ingresos, gastos, balances, precios, deudas y origen/destino del dinero.
- Mapa 3D: asentamientos, caminos, fronteras, recursos conocidos, rutas y presencia enemiga.
- Libro de facción: miembros, cultura, raza principal, cargos, diplomacia, tratados, ejército e historial.
- Registro de eventos: decisiones de IA, obras, incidentes, guerras y resultados importantes.

Cada encargo debe explicar requisitos, coste, duración estimada, riesgos, estado y motivo de bloqueo. La IA no debe limitarse a mostrar “no se puede”: debe indicar qué recurso, permiso, trabajador o condición falta.

### 8.6 Seguridad, rendimiento y multijugador

Máximo dos jugadores por facción. El mundo es compatible con servidor dedicado y juego multijugador. Las transacciones sensibles son atómicas, se validan contra el estado del servidor y no aceptan cantidades, precios, costes, permisos o resultados calculados por el cliente. Un jugador conectado puede administrar la facción; si ambos propietarios están desconectados, el segundo al mando NPC ejecuta solo políticas preaprobadas y operaciones ordinarias.

El segundo al mando puede mantener presupuesto ordinario, obras ya aprobadas, abastecimiento, guardias, rutas y producción. No puede declarar guerra, firmar tratados importantes, cambiar leyes fundamentales, cambiar gobernante, ordenar gastos extraordinarios ni ejecutar grandes expansiones sin autorización.

Permisos por propietario, miembro, trabajador NPC, constructor, aliado, visitante y jugador externo. Protección de asentamientos impide destruir bloques ajenos, modificar edificios, robar cofres protegidos o sabotear instalaciones sin permiso. En guerra se habilitan excepciones de asedio configuradas, manteniendo protección reforzada de civiles. Servidores pueden ajustar PvP, protección de zonas, territorios neutrales y reglas de guerra.

### 8.7 Equilibrio

No hay escalado automático universal. La dificultad depende de territorio, preparación, equipamiento, composición, suministro, terreno, inteligencia y objetivos. La progresión debe abrir opciones, no convertir al jugador en invulnerable. Las razas no deben dominar todas las profesiones; los elementos no deben tener un counter universal; los dragones son poderosos pero vulnerables a contramedidas y costes; los edificios avanzados exigen mantenimiento, no solo materiales de construcción.

Los valores numéricos se balancean con pruebas repetibles. Cada receta, recompensa, contrato, edificio y evento debe tener presupuesto de recursos y efecto económico evaluable. La economía no se equilibra únicamente con precios fijos, sino con fuentes, sinks, inventario, demanda y costes de operación.

### 8.8 Configuración del servidor

Opciones configurables: densidad de facciones; capacidad de vivienda y población máxima por asentamiento; velocidad de simulación distante; recuperación offline y su límite; presupuesto de operaciones por tick; frecuencia de eventos; agresividad de asaltantes; dificultad de mazmorras; PvP; protección territorial; pérdidas al morir; ritmo de envejecimiento y reproducción; intensidad de necesidades; mantenimiento; límites de puestos avanzados; rareza de dragones y huevos; permisos de guerra; automatización del segundo al mando. Los valores predeterminados deben mantener el diseño previsto; la configuración no debe ser necesaria para que el mod funcione.

### 8.9 Roadmap de desarrollo

1. **Prueba técnica mínima:** carga NeoForge, servidor dedicado, persistencia, red, migraciones y registro de datos.
2. **Vertical slice verificable:** un personaje, una raza, una clase, una profesión, un taller, un NPC trabajador y un asentamiento pequeño con economía real.
3. **Producción y construcción:** cadenas cortas, edificios funcionales, órdenes de trabajo, recursos reservados y recuperación tras reinicio.
4. **Simulación y facciones:** calendario, población abstracta, vivienda, salarios, rutas, gobierno delegado y economía de dos facciones.
5. **Generación base:** biomas Vanilla+, estructuras menores, asentamientos iniciales, reglas de separación y compatibilidad mínima.
6. **Aventura jugable:** una mazmorra completa con persistencia, mini-jefe, jefe, botín finito y expedición NPC.
7. **Expansión de contenido:** catálogo gradual de razas, clases, profesiones, materiales, culturas y estructuras.
8. **Guerra y criaturas:** ejércitos abstractos, combate local, asedios, prisioneros, monturas y finalmente dragones.
9. **Beta y lanzamiento:** migraciones, rendimiento, accesibilidad, compatibilidad, configuración, documentación y pruebas multijugador.

La primera versión jugable debe priorizar un bucle estable: personaje → asentamiento → profesiones → producción → economía → facción. No se debe intentar implementar simultáneamente las dieciséis familias al máximo detalle, todos los árboles, todos los dragones y toda la IA antes de tener guardado, inventarios, trabajadores y simulación probados.

---

# PARTE II — REGLAS TRANSVERSALES YA CONSOLIDADAS

## 9. Facciones y jugadores

Las facciones pueden ser generadas, prediseñadas, fundadas por jugadores o autónomas Full NPC. Un jugador puede crear o unirse a una facción. Máximo dos jugadores por facción; los NPC no cuentan para ese límite. Cada facción tiene familia racial principal, cultura, identidad, territorio, economía, gobierno, ejército, relaciones e historia.

El jugador puede ser gobernante, aventurero, artesano, comerciante, explorador, diplomático, guerrero o trabajador independiente. No tiene que administrar toda la facción. Los sistemas de gestión deben admitir delegación.

## 10. Economía pública, privada y de asentamiento

La tesorería paga empleados públicos, guardias, obras, puestos, celebraciones e infraestructuras. Los negocios privados pagan sus propios trabajadores y gastos. Las facciones mantienen ingresos, gastos, salarios, reservas, deuda y presupuesto. Si falta dinero, el orden de respuesta será: usar reservas, reducir gastos secundarios, suspender mejoras, reducir actividad no esencial y priorizar seguridad/abastecimiento. No se destruye una economía mediante una subida repentina e imposible de mantener.

Los indicadores clave son riqueza, prosperidad, empleo, producción, abastecimiento, comercio, satisfacción, deuda y estabilidad. La riqueza y el prestigio histórico son distintos: un linaje puede ser famoso sin ser rico, o rico sin tener prestigio.

## 11. Inmigración y cultura

La inmigración depende de salario, vivienda, seguridad, familia y oportunidades. La familia racial principal limita la incorporación permanente según compatibilidad. La afinidad no es una barrera automática, aunque puede afectar relaciones y costumbres. Los inmigrantes mantienen su cultura original y adoptan progresivamente costumbres locales; los hijos pueden crecer con ambas influencias.

## 12. Asaltantes y seguridad

Asaltantes independientes aparecen fuera de ciudades, forman bandas y campamentos, atacan caravanas, mensajeros y asentamientos vulnerables, roban y emboscan. Eligen rutas menos protegidas, evitan fuerzas superiores y pueden retirarse. Algunos aceptan contratos; otros no. Patrullas, torres, escoltas, caminos y puestos avanzados reducen el riesgo, pero no lo eliminan.

## 13. Traslado médico y atención de NPC

Una NPC embarazada puede trasladarse a otra ciudad para recibir mejor atención si hay hospital y capacidad. Debe contar con transporte y guardia; diplomacia permite solicitar aceptación de otra facción. Puede aceptar o rechazar según personalidad, confianza, salud y circunstancias. Una orden oficial puede obligarla, pero puede generar miedo y descontento. Durante viaje, atención y recuperación hay riesgos reales. La NPC vuelve con su hijo al recuperarse.

## 14. Muerte del jugador y protección

El jugador no envejece; puede morir en combate y reaparece. Pierde una pequeña cantidad de experiencia de combate acumulada hacia el siguiente nivel de su clase activa (base de balance: 5 % del progreso parcial de ese nivel), sin bajar de nivel ni perder puntos de talento o nodos desbloqueados. No pierde experiencia profesional ni niveles de profesión. No existe un nivel global de personaje que mezcle combate y oficio. El inventario cae o queda en una tumba. La tumba se puede abrir y saquear si las reglas del servidor lo permiten; desaparece vacía. En multijugador el PvP es libre salvo configuración del servidor y la diplomacia afecta reputación y relaciones.

## 15. Construcción y mantenimiento

Todos los edificios tienen mantenimiento periódico proporcional a tamaño, nivel, estado, trabajadores, combustible y actividad. Básicos baratos, especializados moderados y avanzados caros. La capacidad y los ingresos aumentan con el desarrollo, compensando parte del coste. No se introducen servicios urbanos complejos fuera de caminos, edificios funcionales, almacenes, graneros, murallas y transporte.

## 16. Decisiones de contenido reservadas para expansiones

No bloquean el núcleo del mod y se incorporarán solo cuando las reglas comunes estén estables:

- Transporte marítimo, comercio naval y construcción de grandes flotas.
- Panteón universal y cosmología final; la religión inicial es modular.
- Nuevas familias raciales fuera de las 16 iniciales.
- Más elementos, afinidades o sistemas de transformación adicionales.
- Mecánicas profundas de estaciones climáticas, agricultura estacional avanzada y servicios urbanos.
- Compatibilidad automática con otros loaders o port a nuevas versiones de Minecraft.
- Integraciones obligatorias con mods externos; el diseño base no dependerá de ellas.

---

# PARTE III — CRITERIOS DE CALIDAD Y ACEPTACIÓN

## 17. Condiciones para considerar un sistema terminado

Un sistema no se considera completo por tener una interfaz o una animación. Debe cumplir todos los puntos que correspondan:

1. Tiene datos persistentes y se recupera correctamente tras guardar/cargar.
2. Tiene reglas de coste, origen, propiedad y pérdida cuando maneja recursos.
3. Funciona para jugadores y NPC con reglas equivalentes.
4. Tiene límites y fallos previstos, no solo el camino ideal.
5. Explica al usuario los requisitos y las razones de bloqueo.
6. Funciona en servidor dedicado y multijugador.
7. Se puede simular a distancia sin crear resultados gratuitos.
8. Tiene pruebas de casos normales, extremos, interrupciones y recuperación.
9. Sus datos pueden ampliarse sin romper partidas anteriores.
10. No hace que el rendimiento empeore de forma descontrolada al crecer población o mundo explorado.

## 18. Pruebas de sistema imprescindibles

- Una facción NPC debe sobrevivir, producir, comerciar y mantener trabajadores sin jugadores conectados mientras el servidor siga en ejecución; el apagado del proceso debe pausar el reloj o aplicar solo recuperación offline limitada.
- Un asentamiento debe poder quedarse sin dinero y aplicar un plan de reducción de gastos sin crear fondos nuevos.
- Una caravana debe consumir tiempo, suministros, capacidad y riesgo; no puede teleportarse ni entregar mercancía dos veces.
- Un NPC muerto debe dejar consecuencias familiares, laborales y económicas coherentes.
- Un edificio profesional debe fallar si no tiene la estación o componentes funcionales exigidos.
- Un jugador no debe activar una habilidad restringida por subclase sin cumplir sus requisitos.
- Una facción debe admitir subrazas compatibles de su familia racial y rechazar incorporaciones incompatibles.
- Un dragón no debe poder cambiar de dueño ni ser montado por un NPC común.
- El segundo al mando no debe iniciar una guerra ni firmar un tratado importante sin permiso.
- Dos jugadores de una misma facción deben ver el mismo estado de inventarios, dinero, permisos y obras.
- La simulación distante debe conservar costes y registros al volver a cargar la región.
- Una partida guardada en una versión anterior debe recibir migraciones seguras o advertir claramente de incompatibilidad.

## 19. Glosario

- **Facción:** organización política, económica, militar y territorial.
- **Reino:** forma de organización política de una facción, no sinónimo de asentamiento.
- **Asentamiento:** lugar poblado con nivel propio: Campamento, Aldea, Pueblo, Villa, Ciudad o Capital.
- **Familia racial:** grupo racial principal que contiene varias subrazas.
- **Afinidad:** Solar o Lunar; propiedad independiente de la raza.
- **Elemento:** uno de seis elementos posibles dentro de la afinidad del individuo.
- **Clase:** identidad de combate elegida por el personaje.
- **Subclase:** especialización activa dentro del árbol de clase.
- **Profesión:** actividad económica/productiva principal.
- **Conocimiento:** técnica o saber que puede compartirse sin transferir nivel profesional.
- **Full NPC:** facción controlada autónomamente por IA con reglas económicas y sociales normales.
- **Simulación distante:** representación abstracta de actividad fuera de la proximidad del jugador.

## 20. Declaración final de diseño

MEDIEVAL FACTIONS queda definido como un mod de simulación medieval fantástica persistente, con progresión RPG y construcción real dentro de Minecraft. Su diferenciador será la interdependencia entre población, profesiones, economía, asentamientos, política y guerra. El contenido espectacular —dragones, jefes, artefactos y magia— se apoya en esa base, no la sustituye.

La prioridad de implementación será construir primero los sistemas que generan consecuencias verificables: persistencia, inventarios, economía, trabajadores, edificios funcionales, IA y red. Después se amplía el catálogo de razas, clases, objetos, criaturas y lore. Así se evita una enorme colección de contenido sin simulación real detrás.

**Este documento es la referencia maestra para las futuras decisiones de diseño y para la posterior fase de programación.** Una modificación futura deberá actualizar este documento y sus reglas relacionadas, en vez de añadir excepciones aisladas.


---

# AMPLIACIÓN COMPLEMENTARIA — GENERACIÓN DEL MUNDO, ESTRUCTURAS Y MAZMORRAS

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

La generación se organizará por etapas: relieve y bioma; carvers y cavidades; rasgos geológicos; minerales; vegetación y fauna; puntos de interés pequeños; estructuras grandes; asentamientos iniciales y sus zonas de protección. Las características de bioma se añadirán mediante recursos de worldgen y biome modifiers etiquetados, evitando reinsertar un mismo PlacedFeature en órdenes incompatibles, una fuente conocida de feature-cycle violations en NeoForge. Cada contenido declarará sus etiquetas de bioma, dimensión, altura, pendiente, espacio, exclusiones y frecuencia.

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

Las siguientes distancias son **objetivos iniciales de calibración**, no valores definitivos de código ni garantías contra estructuras de terceros. Se medirán en distancia aproximada entre centros y se ajustarán con pruebas de semillas. Los structure sets controlarán la distribución de estructuras propias; un registro determinista de reservas de región reducirá la competencia interna entre categorías. No se prometerá evitar toda intersección con estructuras de otros mods sin un perfil de compatibilidad probado:

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

Las cámaras principales se saquean una sola vez por mundo/instancia persistente, salvo estructuras declaradas repetibles. Los cofres y jefes no generarán el botín principal otra vez por descargar el chunk, morir, desconectarse, reiniciar el servidor o repetir una animación de apertura. Los enemigos pueden reaparecer únicamente en localizaciones configuradas como campamentos activos o guaridas dinámicas y sus recompensas no deben equivaler al tesoro principal. No hay cofres que regeneran artefactos únicos ni recompensas finales ilimitadas.

Los objetos únicos tendrán un identificador de mundo o de instancia. El servidor registra su generación, propietario actual si está registrada y estado. El registro debe impedir que un objeto único se clone al dividir stacks, moverlo entre inventarios, guardar/cargar o ejecutar recuperación de mazmorra. Así se evita que una misma reliquia histórica se genere sin límite y destruya su valor económico.

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

MEDIEVAL FACTIONS no copiará bloques, planos, estructuras, arte, código ni tablas de botín de estos proyectos. Se tomarán principios de variedad, navegación, silueta, ritmo de exploración y compatibilidad. No se declarará una combinación “compatible” por el mero hecho de que ambos mods carguen: cada perfil debe superar pruebas de varias semillas y verificar duplicación, separación, accesibilidad y frecuencia de estructuras.

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

Estas fuentes se consultaron para comprobar disponibilidad y tomar inspiración de diseño. No son requisitos de instalación ni autorización para reutilizar contenido de terceros. Comprobación revisada: 25 de noviembre de 2026.

1. **Biomes O' Plenty — Modrinth:** catálogo de biomas, dimensiones soportadas, loaders y requisitos TerraBlender/GlitchCore. https://modrinth.com/mod/biomes-o-plenty
2. **NeoForge 1.21.1 — Biome Modifiers:** sistema de datos para modificar características y apariciones por bioma. https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/
3. **YUNG's Better Dungeons — NeoForge:** rediseño de mazmorras vanilla y estructuras temáticas; versión 1.21.1. https://www.curseforge.com/minecraft/mc-mods/yungs-better-dungeons-neoforge
4. **When Dungeons Arise — 2.1.68:** versión NeoForge 1.21–1.21.1 y revisión de grandes complejos, vaults y llaves. https://modrinth.com/mod/when-dungeons-arise/version/XIRJSFQ0
5. **Dungeons and Taverns — FAQ/descargas:** filosofía de estructuras de estilo vanilla; el archivo exacto debe comprobarse por loader y versión. https://dungeonsandtaverns.net/faq/
6. **Tinkers' Construct — Modrinth:** filosofía de herramientas modulares y versiones oficiales enumeradas. https://modrinth.com/mod/tinkers-construct
7. **Tinkers' Construct Unofficial Port — Modrinth:** ejemplo de port no oficial para NeoForge 1.21.1; no aprobado por SlimeKnights. https://modrinth.com/mod/hexa-tinkers-construct-unofficial-port

## 27. Decisión final de worldgen

MEDIEVAL FACTIONS tendrá un mundo **Vanilla+ de fantasía medieval**, con biomas propios limitados pero distintivos, soporte opcional para Biomes O' Plenty, generación geográfica por regiones, una red de puntos de interés de densidad controlada, asentamientos culturales funcionales y mazmorras persistentes con arquitectura modular, jefes y botín finito.

La prioridad no será colocar el máximo número de estructuras, sino hacer que cada descubrimiento tenga sentido, que las distancias permitan viajar y orientarse, que el botín alimente la economía y la progresión, y que las facciones NPC puedan descubrir, reclamar y disputar lugares sin romper las reglas del mundo.


# PARTE IV — ESPECIFICACIÓN NORMATIVA DE AUDITORÍA v1.2

Los capítulos 28–42 resuelven los puntos que no estaban suficientemente definidos en las partes anteriores. Si una descripción previa no encaja con estas reglas, se aplica esta parte.

## 28. Modelo de datos, propiedad y trazabilidad

Las reglas de este capítulo son normativas salvo donde la Parte V v1.3 las precise o corrija.

### 28.1 Una fuente de verdad por dominio

El servidor será la única autoridad para el estado persistente. No se mantendrá una copia editable de dinero, inventario o propiedad en el cliente. La interfaz puede mostrar una proyección local, pero toda operación se confirma en servidor.

Dominios principales:

| Dominio | Fuente de verdad | Identificador estable |
|---|---|---|
| Personaje jugador | UUID del jugador + datos persistentes | UUID |
| NPC | Registro de agente; entidad física opcional | Agent ID persistente |
| Facción | Registro de mundo | Faction ID |
| Asentamiento | Registro de facción/mundo | Settlement ID |
| Edificio | Registro de estructura + bloque funcional | Building ID |
| Cuenta/tesorería | Libro mayor del mundo | Account ID |
| Trabajo/orden | Cola de producción | Job ID |
| Ruta/envío | Registro logístico | Shipment ID |
| Mazmorra | Registro de instancia de estructura | Dungeon Instance ID |
| Reliquia única | Registro de objetos únicos | Artifact ID |
| Reclamación territorial | Mapa lógico de celdas | Claim Cell ID |

Los identificadores nunca dependerán de nombres traducidos, coordenadas mostradas al jugador ni índices de listas que puedan cambiar.

### 28.2 Propiedad y transferencias

Todo activo relevante tiene un estado de propiedad: mundo/neutral, facción, asentamiento, negocio, jugador, NPC o contenedor protegido. Una transferencia sigue este orden: validar permiso y capacidad → reservar activo → registrar movimiento → actualizar origen y destino → confirmar transacción. Si falla un paso, se revierte toda la operación.

No se puede transferir una cantidad superior a la disponible. No se puede pagar dos veces una misma orden. Un objeto no puede existir simultáneamente en dos inventarios. La recuperación después de un cierre debe ser idempotente: procesar dos veces el mismo evento no duplica ni cobra dos veces.

La trazabilidad se aplica a transacciones económicas, materiales estratégicos, lotes de fabricación y objetos únicos. No es necesario registrar individualmente cada bloque de tierra o cada flecha corriente si no tiene impacto económico o de propiedad; el nivel de detalle debe ser proporcional al valor del sistema.

### 28.3 Registro de eventos

Cada acontecimiento relevante conserva: fecha del calendario del mod, actor, objetivo, localización, causa, coste, resultado, consecuencias y referencia a la transacción. Se distinguen eventos informativos de eventos que cambian el estado. Los eventos transaccionales tienen ID único y no se ejecutan dos veces.

El historial del Ayuntamiento será un resumen legible; el registro técnico conserva suficiente detalle para detectar duplicación, pérdida de datos y abuso.

## 29. Simulación global y población escalable

### 29.1 NPC con identidad sin necesidad de entidad física

Cada ciudadano mantiene identidad, familia, cultura, profesión, habilidades, necesidades, relaciones, inventario lógico, propiedad, ubicación y estado. La entidad Minecraft se materializa cuando el NPC está en proximidad, participa en una actividad visible o necesita interacción directa. Al alejarse, se serializa a su estado abstracto.

No se mantendrán cientos o miles de entidades vivas caminando en chunks descargados. La simulación abstracta tampoco debe fingir movimiento con teletransporte: utiliza rutas, nodos y tiempo estimado.

### 29.2 Capacidad residencial de referencia

La población depende primero de vivienda funcional, alimentos, agua, empleo y seguridad. Como base inicial de calibración, las capacidades residenciales orientativas serán:

| Nivel de asentamiento | Residentes de referencia |
|---|---:|
| Campamento | 8 |
| Aldea | 24 |
| Pueblo | 60 |
| Villa | 120 |
| Ciudad | 250 |
| Capital | 500 |

Son capacidades de referencia para pruebas, no topes universales. Viviendas, ampliaciones, almacenes, abastecimiento y administración pueden aumentarlas; falta de espacio o servicios básicos puede reducir la capacidad efectiva. El servidor puede establecer un máximo duro por asentamiento y un máximo global de agentes registrados.

### 29.3 Presupuesto de simulación

El planificador no actualiza a todos los NPC simultáneamente. Cada tarea declara prioridad, coste estimado, próximo momento de ejecución y condición de interrupción. La resolución sigue este orden:

1. Seguridad e incidentes que amenazan vidas o asentamientos.
2. Transacciones ya reservadas, salarios, consumo y entregas.
3. Trabajos y cadenas productivas.
4. Viajes, patrullas, expediciones y mantenimiento.
5. Relaciones, envejecimiento, reproducción y eventos de baja urgencia.
6. Decoración, rumores y actividad no esencial.

Si existe retraso de servidor, se espacian las tareas informativas o decorativas; nunca se pierden pagos, inventarios, resultados confirmados ni muertes ya resueltas.

### 29.4 Decisión de IA

La IA de un agente usa objetivos ponderados (necesidades, órdenes, seguridad, lealtad, preferencias, recursos y oportunidad), no una conversación generativa. La decisión se documenta con motivo resumido. Las órdenes superiores tienen prioridad si son legales y compatibles con la seguridad; la personalidad puede influir en la aceptación o en el riesgo de desobediencia.

Las decisiones de gobierno tienen reglas separadas. Un trabajador no puede decidir gastar la tesorería; un administrador no puede declarar guerra si no tiene ese cargo y autorización.

## 30. Reloj, calendario, ciclos vitales y mundo detenido

### 30.1 Reloj canónico

El calendario del mod usa 378 días por año: seis estaciones, tres meses por estación, 21 días por mes; semana de ocho días. La hora del día del juego sigue el ciclo Minecraft estándar. Un día del calendario equivale a 24.000 ticks de simulación en la escala estándar. El año completo equivale, por tanto, a 9.072.000 ticks, y la escala podrá ajustarse en servidor sin cambiar el orden de meses.

El reloj de simulación no se basa en la hora del sistema operativo. Los cambios administrativos de hora no deben adelantar salarios, embarazos, envejecimiento, rutas ni cosechas de manera accidental. Si el administrador adelanta el día visual, el mundo registra la operación y aplica únicamente los efectos explícitos que correspondan.

### 30.2 Día/noche y afinidades

Las bonificaciones Solar/Lunar se calculan a partir de la fase de día/noche del mundo, no del clima ni de la línea de visión al cielo. Se respeta la definición ya acordada: Solar activo de día y Lunar activo de noche. Las transiciones tienen una ventana clara de activación; no parpadean con cambios de dimensión, sueño o comando.

### 30.3 Servidor vacío y servidor apagado

Un servidor dedicado encendido continúa simulando facciones y regiones sin jugadores conectados. Si el proceso se apaga, el mundo no ejecuta ticks. La recuperación offline al reiniciar está limitada por defecto a tres días del calendario del mod, puede desactivarse y nunca debe resolver una guerra total o una extinción poblacional a partir de un salto ilimitado de tiempo.

Los servidores públicos de larga duración podrán mantener el proceso encendido sin jugadores; los propietarios de servidores pequeños decidirán si habilitan recuperación limitada.

### 30.4 Envejecimiento y ciclo de vida

La edad avanza usando el reloj canónico, no el tiempo real del sistema. Las razas definen edad de madurez, longevidad y ritmos de desarrollo. En una primera fase se implementan curvas de edad simplificadas por familia; las subrazas pueden aplicar modificadores pequeños. No se crearán 64 simuladores biológicos independientes.

## 31. Economía cerrada, contabilidad y mercados

### 31.1 Libro mayor de dinero

Toda moneda usa unidades enteras de cobre como representación interna: 1 plata = 10 cobres; 1 oro = 100 cobres. Se prohíbe usar coma flotante para saldos. La moneda física y el saldo bancario son dos formas de representar el mismo valor; convertir, ingresar, retirar y transferir mueve valor, no lo duplica.

Cada transacción registra cuenta de origen, cuenta de destino o destrucción, importe, concepto, actor, fecha y referencia. Impuestos son transferencias a tesorería. Salarios son transferencias del empleador al trabajador. Una comisión puede transferirse a un proveedor o destruirse como sumidero; el sistema indica cuál es.

### 31.2 Fuentes y sumideros

Fuentes permitidas: capital inicial explícito, acuñación autorizada bajo cuota, tesoros finitos, recompensas con presupuesto, contratos financiados y ventas a compradores con dinero real. Fuentes prohibidas: tiendas NPC con saldo infinito, bancos que creen depósitos, misiones sin financiador, venta automática de objetos sin comprador y recompensas repetibles sin coste.

Sumideros monetarios: comisiones destruidas, tasas declaradas como destruidas, servicios del sistema, multas destruidas y pérdidas explícitas por reglas. Mantenimiento, salarios, impuestos y compras normales son transferencias, no destrucción de moneda. Los materiales pueden consumirse por fabricación, reparación, alimentación o mantenimiento conforme a sus recetas.

### 31.3 Precios y estabilidad

Los precios son locales. Se calculan por inventario disponible, demanda, necesidad, distancia, riesgo, tiempo de entrega, competencia y acuerdos. Se actualizan por ventanas, no cada tick. Los cambios tienen límites graduales para evitar oscilaciones y manipulación mediante ventas repetidas en un solo instante.

Un mercado no debe pagar más por un objeto solo porque el jugador lo vuelva a vender de inmediato. Los inventarios, reservas de consumo, capacidad de almacenamiento y presupuestos limitan las compras.

### 31.4 Crédito y bancos

Un depósito es un pasivo del banco frente al cliente y no se puede volver a gastar como si fuera capital adicional. Un préstamo utiliza capital propio o fondos asignados; el banco puede rechazarlo si las reservas son insuficientes. Los intereses son transferencias entre deudor y acreedor. Contratos registran garantías, aval, calendario, atrasos, refinanciación y ejecución de garantías.

La quiebra se resuelve según los contratos: congelación temporal, liquidación de activos y pérdidas distribuidas conforme a reglas. No se rescata a un banco con dinero infinito del sistema.

### 31.5 Acuñación e inflación

La Casa de Moneda no será una máquina de ingresos pasivos ilimitados. Su cuota depende de demanda de efectivo, actividad económica, reservas, estabilidad de precios y circulante. El señoreaje no elimina el riesgo de inflación. El sistema debe registrar moneda emitida, retirada, fundida y destruida; las monedas de sello de facción mantienen valor nominal común.

## 32. Facciones, asentamientos, territorio y diplomacia

### 32.1 Estructura de facción

Una facción es la entidad política y económica; un reino es una forma de gobierno; un asentamiento es una unidad habitada. Una facción tiene como máximo dos jugadores miembros. La facción puede designar un gobernante y un segundo al mando, con reglas de sucesión y permisos.

El jugador puede abandonar una facción, pero debe resolver propiedad, cargos, contratos, inventarios compartidos y acceso a territorios. No puede llevarse objetos pertenecientes a la tesorería o al negocio sin una transferencia autorizada.

### 32.2 Capital y asentamientos

Cada facción posee como máximo una Capital oficial a la vez. El cambio de capital exige decisión política, coste administrativo y periodo de transición. El resto de asentamientos mantiene su propio nivel y Ayuntamiento. La pérdida de una capital no destruye automáticamente la facción: puede reubicar su capital si conserva condiciones mínimas.

### 32.3 Celdas territoriales

El control se representa con celdas lógicas de territorio alineadas a la cuadrícula de chunks, no mediante una lista infinita de coordenadas individuales. Una celda reclama un área definida y tiene propietario, estado, coste, valor estratégico y fecha. Las zonas de asentamiento son contiguas o están conectadas mediante derechos de paso. Los territorios disputados no se convierten en propiedad por cruzarlos.

Un jugador puede escoger manualmente una expansión; una facción NPC evalúa las mismas condiciones y costes mediante su IA. La ventaja del jugador es la selección directa, no un descuento de reglas ni recursos gratuitos.

### 32.4 Derechos y permisos

Los permisos distinguen: entrar, comerciar, usar instalaciones, construir, modificar, retirar inventario, reclutar, patrullar, cruzar con caravanas, fortificar y atacar. Un aliado no recibe automáticamente derecho a retirar bienes ni a modificar edificios. Los permisos tienen propietario, alcance y posibilidad de revocación, respetando contratos y seguridad.

### 32.5 Diplomacia y guerra

La escala de -100 a +100 se conserva. Las decisiones se basan en objetivos, recursos, compromisos, amenazas, historial y personalidad del gobernante. Una acción diplomática puede crear un tratado, pero no sustituye sus cláusulas ejecutables: fechas, pagos, rutas, contingentes, duración, incumplimiento y salida.

La guerra requiere causa y objetivo registrados: territorio, compensación, liberación, defensa, captura de puesto o rendición. Los servidores pueden configurar guerra total, guerra limitada, PvP y protección de asentamientos. El combate PvP no da permiso automático para destruir bloques, cofres o edificios fuera de las reglas de guerra.

## 33. Estadísticas, afinidad, elemento y equilibrio

### 33.1 Capas independientes

La ficha final separa: (1) estadísticas base, (2) rasgos raciales, (3) afinidad Solar/Lunar, (4) elemento, (5) clase/subclase, (6) profesión, (7) equipo, (8) estados temporales. Ningún sistema escribe directamente sobre el nivel de otro. Una raza no otorga una subclase; un elemento no cambia de profesión; un objeto de clase no otorga la clase.

### 33.2 Estadísticas base

Las siete estadísticas generales son Fuerza, Velocidad, Resistencia, Inteligencia, Destreza, Percepción y Suerte. Fuerza influye principalmente en daño físico y capacidad de carga; Velocidad en desplazamiento, iniciativa y maniobras; Resistencia en salud, aguante y tolerancia al esfuerzo; Inteligencia en aprendizaje analítico e interacción mágica; Destreza en precisión, control fino y fabricación; Percepción en detección, lectura de señales y precisión contextual; Suerte en variaciones pequeñas y limitadas, nunca en la creación de dinero o botín garantizado. Los valores derivados incluyen salud, aguante, daño físico, defensa, resistencias a estados, maná, poder mágico, precisión, crítico, evasión, velocidad de ataque y carga. La resistencia a estados derivada no es la estadística base Resistencia. Cada estadística tendrá una tabla central explícita de derivaciones, límites y curvas de rendimiento decreciente.

Los rasgos de afinidad Solar/Lunar son paquetes de efectos, no estadísticas base ni sustitutos de las siete estadísticas. Para evitar ambigüedad, el panel y los datos internos los muestran con nombres cualificados: “Vitalidad Solar”, “Energía Solar”, “Percepción Lunar” y “Espiritualidad Lunar”. La Percepción Lunar es el rasgo de afinidad que concede velocidad y visión nocturna; la estadística general Percepción rige detección, lectura del entorno y precisión contextual. No comparten identificador ni se sustituyen.

### 33.3 Apilamiento

Los efectos permanentes de raza, elemento, clase, subclase y equipo se suman por categoría antes de aplicar límites. Los multiplicadores no se multiplican unos sobre otros sin control. Como base de pruebas, las bonificaciones pasivas acumuladas de daño o velocidad tendrán un límite blando y la mitigación de daño un límite duro. El valor concreto se fijará en pruebas de combate y se mantendrá en una tabla central para evitar que cada habilidad cree su propio límite.

Los efectos temporales de canciones, pociones, aura, clima o día/noche se aplican después de los valores permanentes y comparten límites de categoría. Dos efectos iguales no duplican sin límite; el sistema define reemplazo, suma parcial o prioridad.

### 33.4 Debilidades

Las debilidades raciales y elementales son penalizaciones reales, pero no bloqueos absolutos. La armadura, resistencia, conocimiento o preparación pueden mitigarlas. Ninguna combinación de raza+elemento deberá dejar al personaje sin una vía viable para realizar una profesión o enfrentar a un jefe de su rango.

## 34. Profesiones, talleres y construcción verificable

### 34.1 Profesión y tarea

Un NPC tiene una profesión principal y puede conocer técnicas relacionadas; el conocimiento compartido no le da dos niveles profesionales. Una orden de trabajo reserva recursos, capacidad de estación y tiempo. Si se interrumpe, conserva progreso y devuelve materiales no consumidos según el estado de la operación.

### 34.2 Edificios funcionales

Un edificio no se reconoce solo por volumen o apariencia. Cada plano funcional declara bloques/estaciones necesarias, número mínimo, acceso, espacio de trabajo, almacén y capacidad de trabajador. Los bloques requeridos tienen identificadores semánticos o tags. Una copia decorativa no cuenta como herrería si no tiene yunque/estación reconocida, combustible y área de trabajo.

La validación se hace al registrar el edificio y cuando cambia materialmente su configuración; no se escanea cada bloque cada tick. La pérdida de una estación reduce o suspende la capacidad del edificio y genera un aviso.

### 34.3 Planificador arquitectónico

El generador produce planos usando módulos validados y reglas de conexión, no bloques aleatorios. Un plano informa: función, huella, altura, habitaciones, puertas, estaciones, almacenamiento, lista de materiales, coste, capacidad, acceso y requisitos de terreno. Debe permitir previsualizar, rotar, mover, validar y aprobar antes de consumir materiales.

La herramienta puede aceptar una frase breve (“herrería pequeña de piedra, dos plantas, estilo enano”) y convertirla en parámetros reconocidos. La interfaz muestra qué interpretó y permite corregirlo. No se promete comprensión semántica ilimitada ni generación mediante un servicio de IA.

### 34.4 Construcción por NPC

La obra se divide en fases: preparar terreno → cimientos → estructura → cubierta → estaciones/función → inspección. Cada fase tiene materiales reservados, trabajadores, herramientas, tiempo y riesgo de interrupción. Los constructores no pueden colocar bloques fuera del plano aprobado. Si el terreno cambia, la obra pausa y solicita corrección.

Los materiales se guardan en una reserva de obra identificada. Al cancelar, se devuelven los no colocados; los bloques colocados se recuperan según estado, daño y regla de demolición. No puede reclamarse material dos veces por cancelar y luego destruir el mismo bloque.

## 35. Generación del mundo y compatibilidad sin conflictos

### 35.1 Arquitectura de worldgen

La distribución de estructuras usa recursos declarativos de worldgen: estructura, estructura-set y reglas de colocación; las salas modulares usan template pools/jigsaw cuando proceda. Los rasgos de bioma se añaden con biome modifiers y etiquetas. La selección de bioma nunca asume que todos los biomas externos contienen las mismas etiquetas.

Las estructuras mayores declaran dimensión, biomas permitidos, rango de altura, pendiente, espacio, exclusiones, separación propia y categorías de proximidad prohibidas. Las estructuras de terceros no se pueden inspeccionar siempre antes de su colocación: por eso la compatibilidad se valida por perfil y no se garantiza ausencia absoluta de solapamientos.

### 35.2 Perfiles de compatibilidad

| Perfil | Mods externos | Regla de diseño |
|---|---|---|
| Vanilla+ (predeterminado) | Ninguno | Catálogo completo propio de MEDIEVAL FACTIONS |
| BOP Biomes | Biomes O' Plenty + dependencias requeridas | Las estructuras propias usan tags de bioma; no sustituyen biomas BOP ni copian sus assets |
| Dungeons extendidas | YUNG's Better Dungeons | Se evita duplicar la función de las mazmorras vanilla; se conservan nuestras mazmorras narrativas únicas |
| Estructuras colosales | When Dungeons Arise | Se reduce o desactiva la categoría propia de megaestructuras en el perfil probado |
| Vanilla+ con D&T | Dungeons and Taverns | Integración experimental; no declarada estable hasta probar build exacta y densidad |
| Perfil combinado | BOP + un proveedor externo de estructuras | Solo se publica como recomendado después de pruebas de semillas, solapamientos y rendimiento |

La instalación de un mod de biomas/estructuras no garantiza por sí misma compatibilidad. La compatibilidad oficial se refiere a un conjunto exacto de versiones y configuración.

### 35.3 BOP

Biomes O' Plenty dispone de publicaciones para Minecraft 1.21–1.21.1 y loaders que incluyen NeoForge; su ficha indica dependencias como GlitchCore y TerraBlender para las ramas modernas. El mod base no lo exige. La compatibilidad se implementa mediante etiquetas y adaptadores, no mediante referencias estáticas a clases internas de BOP. [Fuente: ficha de Biomes O' Plenty en Modrinth, consultada el 25-11-2026.]

Si BOP falta, el mundo Vanilla+ sigue funcionando. Si BOP se instala después de generar chunks, los biomas nuevos aparecerán principalmente en chunks no generados; nunca se prometerá convertir de forma limpia el terreno ya generado.

### 35.4 YUNG's Better Dungeons y When Dungeons Arise

YUNG's Better Dungeons tiene builds NeoForge para 1.21.1 y rediseña las mazmorras vanilla además de añadir catacumbas, fortalezas de no muertos y cuevas de arañas. MEDIEVAL FACTIONS no necesita reemplazar la mazmorra vanilla para cumplir su función; sus familias únicas se mantienen separadas y su densidad se ajusta si YUNG está activo.

When Dungeons Arise tiene una release NeoForge para 1.21–1.21.1. Su papel de grandes estructuras hostiles se solapa con nuestras fortalezas y complejos colosales. En el perfil de compatibilidad, las megaestructuras propias se reducen o se desactivan selectivamente para evitar saturación. No se distribuye ni se incorpora su contenido.

### 35.5 Dungeons and Taverns y Tinkers' Construct

Dungeons and Taverns se conserva como referencia estética y candidato de compatibilidad; su lista de descargas cambia, por lo que se probará un archivo exacto antes de aparecer como combinación validada.

La ficha oficial consultada de Tinkers' Construct enumera versiones oficiales hasta Minecraft 1.20.1 y no incluye 1.21.1. Por tanto, nuestro sistema de fabricación modular será propio y no dependerá del mod oficial ni de ports no oficiales. Se tomarán como inspiración funcional la modularidad, los rasgos de materiales, la fundición, los moldes, las aleaciones y la reparación; arte, nombres, código y contenido serán originales.

## 36. Mazmorras, recompensas y contenido único

### 36.1 Mazmorras públicas, no instancias invisibles

Las mazmorras del mundo son localizaciones persistentes compartidas. Un grupo no crea una copia separada por entrar. La entrada, puertas, guardianes, botín histórico y estado de exploración pertenecen a una instancia física del mundo. Si un servidor desea mazmorras instanciadas en una expansión futura, deberá ser una opción separada y no la regla base.

### 36.2 Encuentro en curso

Al iniciar un jefe, se registra un encuentro con participantes, hora, área y estado. Si todos los participantes se retiran, mueren o desconectan, el jefe vuelve a su estado de guardia después de un tiempo de recuperación; la salud y las fases pueden reiniciarse según la ficha del jefe. La recompensa no se genera hasta que la condición de victoria se confirma en el servidor.

### 36.3 Categorías de recompensa

- **Consumible/corriente:** repetible y disponible en producción, comercio o botín común.
- **Raro de temporada o región:** limitado por dificultad y disponibilidad, pero no necesariamente único en el mundo.
- **Plano/receta:** desbloquea conocimiento; puede haber varias copias si su origen histórico lo permite.
- **Artefacto de tirada limitada:** el mundo puede generar un número limitado definido por diseño.
- **Reliquia única:** un solo ejemplar por mundo; su existencia, propietario, ubicación conocida y estado se registran.

No todas las mazmorras contienen reliquias únicas. La progresión de una clase o profesión nunca dependerá de un solo objeto que un NPC pueda perder para siempre. Si un objeto único desaparece, se destruye o sale del mundo por una regla de servidor, el registro cambia a destruido/retirado; no reaparece silenciosamente.

### 36.4 Reocupación

Los enemigos pueden regresar a una mazmorra solo si su ficha permite reocupación. El sistema repuebla patrullas o guardianes, no los cofres históricos ni los premios únicos. La facción que reclame una ruina puede fortificarla, pagar guardias, cerrar accesos y reparar habitaciones mediante el sistema de construcción.

### 36.5 Exploración NPC

Una facción NPC no despeja instantáneamente un complejo desde cientos de kilómetros. La expedición progresa por sectores, consume suministros y puede retirarse. Las misiones visibles para el jugador no son invulnerables a la competencia NPC, pero el sistema no debe reservar cada gran contenido para los NPC antes de que un jugador pueda conocerlo: la frecuencia de expediciones y el riesgo se balancean por distancia, información, presupuesto y disponibilidad real.

## 37. Combate, ejércitos y reglas de guerra

### 37.1 Dos escalas de combate

Cerca del jugador se utilizan entidades, proyectiles, colisiones, pathfinding y habilidades reales. Lejos del área activa, el combate se resuelve por escuadras y sectores usando estadísticas, formación, liderazgo, moral, terreno, suministros, visibilidad, equipo, heridas y retirada. El resultado deja registro y no genera loot no justificado.

### 37.2 Unidad militar

La unidad operativa es una escuadra con líder, tipo, número de efectivos, experiencia, equipo, moral, suministros y órdenes. Las escuadras pueden integrarse en compañías y ejércitos. La IA elige objetivos según órdenes, seguridad, alcance y capacidad logística. No se exige una entidad por soldado durante una batalla lejana.

### 37.3 Órdenes y suministros

Las órdenes tienen prioridad, objetivo, área, duración, reglas de enfrentamiento y condición de retirada. El ejército debe consumir comida, munición o materiales de asedio cuando corresponda. Sin suministro cae la eficacia y aumenta la deserción; no se crea munición infinita.

### 37.4 Civiles y asentamientos

El combate en guerra respeta zonas, objetivos y reglas del servidor. La destrucción de edificios civiles no es una orden automática de conquista. Se priorizan fortificaciones, almacenes militares y objetivos declarados; el daño colateral se registra y afecta legitimidad, diplomacia, estabilidad y reparación.

### 37.5 PvP, asedio y permisos

PvP y destrucción de propiedad son dos permisos distintos. Un servidor puede permitir combate entre jugadores y mantener protegidos cofres, talleres y edificios; o habilitar asedio únicamente cuando existe estado de guerra y objetivo. Un aliado que combate en territorio ajeno no recibe derecho automático a modificarlo.

## 38. Monturas y dragones: coherencia de propiedad y progresión

### 38.1 Monturas normales

Las monturas normales pueden subir del nivel 1 al 20 como base de diseño y desarrollar vínculo; no envejecen ni tienen reproducción compleja. La supervivencia sigue puntos de vida y daño. La alimentación cura o recupera según especie; no hay hambre mortal para monturas normales salvo que una expansión lo defina.

La propiedad es transferible mediante venta, regalo o reasignación válida. Una montura solo puede tener un propietario activo. Los pasajeros y permisos se validan en servidor.

### 38.2 Dragones

Los dragones mantienen reglas separadas: rareza local, territorio, hambre, crecimiento, reproducción, huevo, impronta, vínculo profundo y propiedad permanente. No hay límite global fijo, pero cada macroregión tiene densidad/capacidad ecológica y las guaridas no generan huevos de forma continua. La muerte no crea automáticamente otro dragón.

El huevo tiene ID, nido de origen, estado de incubación y propietario/encargado registrado antes de eclosionar. La impronta inicial no se puede usar para duplicar propiedad: una sola persona puede ser dueño. En multijugador, el consentimiento del dueño controla pasajeros y órdenes de vuelo. Si el rey NPC propietario muere definitivamente, el dragón queda sin dueño según la regla ya adoptada.

### 38.3 Equilibrio del poder dracónico

El dragón no conquista por sí solo una ciudad ni sustituye a ejército y asedio. Sus contramedidas incluyen proyectiles especializados, defensas, terreno, fatiga, peso, heridas, energía elemental y necesidad de descanso/comida. El daño estructural se limita por objetivos de guerra y protección del servidor.

## 39. Roadmap de producción y alcance realista

### 39.1 La vertical slice no es una reducción del diseño final

El diseño objetivo mantiene las 16 familias raciales, 15 clases, 44 subclases, catálogo profesional amplio, asentamientos, economía, diplomacia, mundo generado, mazmorras, monturas y dragones. Sin embargo, la primera demostración debe validar que los sistemas se sostienen entre sí, no intentar incluir todo el contenido.

### 39.2 Vertical slice obligatoria

Antes de ampliar contenido debe funcionar de principio a fin:

1. Un jugador crea personaje con raza, afinidad y elemento.
2. Un jugador funda un campamento con Ayuntamiento.
3. Se incorpora un NPC compatible con identidad y profesión.
4. El NPC recibe una orden, consume materiales y completa un trabajo real.
5. El edificio tiene estaciones funcionales y capacidad verificable.
6. El inventario, la tesorería y el salario se guardan sin duplicación.
7. El servidor se reinicia y recupera el estado correctamente.
8. El servidor permanece encendido sin jugadores y el asentamiento continúa simulándose.
9. El jugador explora una estructura y una mazmorra menor persistente.
10. La misma secuencia funciona en servidor dedicado con dos jugadores.

### 39.3 Fases de contenido propuestas

| Fase | Contenido | Condición para avanzar |
|---|---|---|
| Prototipo técnico | Guardado, red, una entidad NPC, inventario, transacción y edificio | Sin pérdida/duplicación tras reinicios y pruebas de servidor |
| Vertical slice | 1–2 familias, 1 clase, 1 profesión central, 1 asentamiento y una cadena productiva | Bucle completo jugable y rendimiento medido |
| Alpha social/económica | Primeras facciones NPC, 4 familias, 4 clases, profesiones básicas y mapa | Población y economía estables sin jugadores |
| Alpha de aventura | Mazmorras menores/estándar, jefes, loot persistente, expediciones | Sin duplicación de botín; derrota/desconexión recuperables |
| Beta estratégica | 16 familias en catálogo gradual, clases/subclases, diplomacia, ejércitos y asedios | Pruebas de equilibrio y simulación en servidor prolongado |
| Lanzamiento completo | Dragones, contenido legendario, lore amplio, compatibilidad y pulido | Compatibilidad documentada, migraciones y rendimiento aceptados |

Los hitos no son fechas. Si una fase no supera la condición de salida, no se amplía el catálogo a costa de la estabilidad.

### 39.4 Fuera de alcance del primer lanzamiento

IA conversacional generativa, simulación física de todos los NPC distantes, flotas navales, economía interservidor, panteón global cerrado, construcción libre de estructuras sin validación, compatibilidad garantizada con cualquier modpack y migración automática de biomas en chunks ya generados quedan fuera del primer lanzamiento.

## 40. Criterios técnicos y de calidad adicionales

### 40.1 Rendimiento

Se medirá el coste incremental de MEDIEVAL FACTIONS sobre un servidor de referencia declarado. Objetivo de aceptación: el mod no debe introducir picos sostenidos que impidan mantener 20 TPS en el escenario de población y actividad de referencia. Se registran tiempo medio, p95, p99, asignaciones de memoria, operaciones de IA, chunks generados y tamaño de guardado.

El presupuesto se prueba con varias facciones, asentamientos de varios tamaños, caravanas, guerra y un usuario activo; no se mide únicamente en un mundo vacío. Si falla, se reduce frecuencia de tareas abstractas y se optimizan consultas antes de eliminar mecánicas importantes.

### 40.2 Integridad y explotación

Pruebas obligatorias contra: doble clic de pago, reconexión durante transferencia, caída del servidor durante producción, duplicación por tombstone, duplicación de reliquia, robo de cofre en zona protegida, petición de habilidad no desbloqueada, orden falsa de cliente, spam de rutas, reproducción repetida de loot y reversión de una transacción ya confirmada.

### 40.3 Guardado y migraciones

Cada versión del esquema tiene número. La migración debe ejecutarse una sola vez, hacer copia lógica antes de modificar y dejar un informe si faltan datos. Si no existe migración segura, se bloquea la carga con advertencia recuperable en vez de corromper silenciosamente la partida.

### 40.4 Compatibilidad

La matriz de compatibilidad debe incluir versión exacta de Minecraft, NeoForge, mod externo, dependencias, configuración y resultados de semillas probadas. “Carga en pantalla principal” no equivale a compatibilidad de worldgen. Se deben probar mundo nuevo, chunks existentes, servidor dedicado, reinicio y carga/descarga de dimensiones.

### 40.5 Accesibilidad y claridad

La UI debe mostrar requisitos, coste, progreso, motivo de bloqueo y consecuencias. Los colores nunca serán el único indicador; estados deben tener icono y texto. Las señales de peligro de mazmorras deben ser legibles sin depender de un color concreto. La información de combates y economía tendrá resúmenes y registros consultables.

## 41. Registro de decisiones cerradas por auditoría

| Área | Decisión de referencia v1.2 |
|---|---|
| Loader | NeoForge 1.21.1, Java 21, una línea de lanzamiento |
| Simulación vacía | Sigue si el proceso del servidor está encendido aunque no haya jugadores |
| Servidor apagado | Pausa por defecto; recuperación de hasta 3 días configurable |
| NPC distantes | Agentes persistentes abstractos; entidades físicas solo cerca |
| Inventarios/dinero | Estado de servidor y transacciones atómicas; nunca confiar en cliente |
| Datos | SavedData para mundo/nivel; attachments para entidad/chunk/BE; data components para ItemStack |
| Calendario | 378 días; 24.000 ticks por día de simulación en escala estándar |
| Profesión secundaria | Solo tareas sencillas; base inicial 50 % velocidad y calidad penalizada |
| Arquitectura generada | Planificador procedural offline, sin LLM obligatorio |
| Territorio | Celdas lógicas alineadas a chunks; costes/reglas comunes para jugadores y NPC |
| Mazmorras | Públicas y persistentes; botín histórico finito; reocupación no repone tesoro único |
| Reliquias | ID único y registro mundial; no se duplican al morir/reiniciar/desconectar |
| Worldgen | Structure sets + biome tags/modifiers + jigsaw/template pools cuando proceda |
| BOP | Integración opcional, nunca dependencia del núcleo |
| Tinkers | Inspiración funcional; sistema propio sin dependencia de 1.21.1 no oficial |
| Prototipo | Una vertical slice completa antes de desplegar todos los catálogos |

## 42. Fuentes técnicas y de compatibilidad revisadas

Comprobadas el 25 de noviembre de 2026. Se deben volver a verificar al fijar la build de NeoForge y al preparar cada release; las páginas de mods pueden cambiar.

1. **NeoForge 1.21.1 — Getting Started:** JDK 21 de 64 bits y entorno de desarrollo. https://docs.neoforged.net/docs/1.21.1/gettingstarted/
2. **NeoForge 1.21.1 — Biome Modifiers:** modificaciones data-driven de biomas, características y apariciones; advierte sobre ciclos de features al reutilizar configuraciones de forma incompatible. https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier/
3. **NeoForge 1.21.1 — Saved Data:** datos persistentes de nivel y obligación de marcar datos modificados para guardado. https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/
4. **NeoForge 1.21.1 — Data Attachments:** datos ligados a entidades, chunks y block entities; ItemStack data usa data components. https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
5. **NeoForge 1.21.1 — Registering Payloads:** paquetes de red cliente-servidor y registro de payloads. https://docs.neoforged.net/docs/1.21.1/networking/payload/
6. **Biomes O' Plenty — Modrinth:** versiones que incluyen Minecraft 1.21–1.21.1, loaders y dependencias modernas. https://modrinth.com/mod/biomes-o-plenty
7. **YUNG's Better Dungeons — Modrinth:** build NeoForge para 1.21.1 y catálogo de mazmorras. https://modrinth.com/mod/yungs-better-dungeons
8. **When Dungeons Arise — Modrinth 2.1.68:** build NeoForge para Minecraft 1.21–1.21.1. https://modrinth.com/mod/when-dungeons-arise/version/XIRJSFQ0
9. **Tinkers' Construct — Modrinth versions:** la ficha consultada enumera builds oficiales para Minecraft hasta 1.20.1, no 1.21.1. https://modrinth.com/mod/tinkers-construct/versions
10. **Dungeons and Taverns — página de descargas:** catálogo de builds por loader y versión; la compatibilidad exacta debe comprobarse al seleccionar archivo. https://dungeonsandtaverns.net/download/

## 43. Dictamen de auditoría

MEDIEVAL FACTIONS conserva su ambición original: un mundo medieval persistente, autónomo y simétrico donde población, profesiones, economía, asentamientos, exploración, guerra y progresión se sostienen mutuamente. La auditoría no recomienda reducir su identidad ni convertirlo en un mod de mazmorras o de combate convencional.

Sí recomienda imponer una disciplina de producción: primero una simulación mínima que pueda guardar, cargar, cobrar, pagar, construir, consumir y recuperarse sin duplicaciones; luego se añade contenido. La arquitectura y los datos deben ser extensibles desde el inicio, pero la cantidad de contenido se libera por fases.

**La v1.2 queda como base histórica; la v1.3 y su Parte V constituyen la especificación maestra vigente para continuar el diseño. No es código, no demuestra rendimiento real y no declara compatibles combinaciones de mods que todavía no se hayan probado en una build exacta.**


---

# PARTE V — CORRECCIONES FINALES Y NORMALIZACIÓN DE SISTEMAS v1.3

Esta parte es la resolución final de la auditoría general solicitada. Su función es eliminar ambigüedades y evitar que dos sistemas reclamen la misma responsabilidad. Si una regla anterior difiere de esta parte, prevalece la v1.3. Las decisiones marcadas como “base de balance” deben medirse en prototipo; no se consideran verdades numéricas definitivas.

## 44. Contrato definitivo de personaje, atributos y progresión

### 44.1 Tres capas distintas

El personaje se compone de tres niveles que nunca deben confundirse:

1. **Estadísticas base:** las siete magnitudes generales: Fuerza, Velocidad, Resistencia, Inteligencia, Destreza, Percepción y Suerte.
2. **Rasgos y condiciones:** tendencia racial, rasgos Solar/Lunar, bonificaciones y vulnerabilidades elementales, estados temporales, edad, heridas y condiciones de equipo.
3. **Valores derivados:** salud, aguante, daño, defensa, maná, precisión, evasión, carga, resistencias y velocidades finales.

Un rasgo aplica un modificador a un valor derivado o a una regla concreta; no crea una estadística duplicada. Los identificadores internos de afinidad deben ser distintos de los atributos base aunque una palabra coincida. En interfaz, la Percepción base y la Percepción Lunar siempre aparecen con calificativo.

### 44.2 Presupuesto racial y potencial individual

Las tendencias raciales modifican la distribución inicial de atributos y el potencial genético, no el presupuesto total de puntos. Cada NPC y jugador conserva variación individual por semilla/identidad. Dos personajes de la misma subraza pueden tener valores, personalidad y potencial diferentes.

Los rasgos raciales no pueden acumularse dos veces al transformarse en híbrido. La ascensión reemplaza el perfil racial anterior por el perfil híbrido válido, recalcula el potencial restante y conserva clase, experiencia, profesión, afinidad, elemento, objetos y relaciones. No aumenta niveles de forma gratuita.

### 44.3 Afinidad, elemento y compatibilidad

La afinidad Solar/Lunar y el elemento son asignaciones permanentes ordinarias. La herencia de afinidad usa las reglas ya fijadas; el elemento de descendientes se sortea dentro de los seis elementos de la afinidad heredada. Las bonificaciones elementales tienen exactamente dos efectos positivos y una vulnerabilidad definida. Las compatibilidades de armas y armaduras se expresan por etiquetas y reglas de material; no habrá una penalización universal por equipar un objeto distinto sin comprobar primero la clase de objeto y su afinidad.

Las bonificaciones de raza, afinidad, elemento, clase, subclase, profesión, canción, poción y equipo se agrupan por categoría. Cada categoría define si suma, reemplaza o aplica el mayor efecto. La mitigación, el daño adicional, la velocidad, evasión, crítico y regeneración tendrán límites centrales; ninguna habilidad puede eludirlos mediante una etiqueta distinta.

### 44.4 Progresión separada

No existe un nivel global que mezcle oficio y combate. La progresión queda definida así:

- **Nivel de clase:** 1–65; determina acceso al árbol de combate y concede 32 puntos de talento según la secuencia adoptada.
- **Nivel de profesión:** 1–50 para la profesión principal; experiencia y desbloqueos separados del combate.
- **Conocimientos:** técnicas compartidas o aprendidas mediante libros, maestros, gremios, planos e investigación; no conceden niveles.
- **Estadísticas y potencial:** progresan por práctica, edad, educación, salud y genética; no se sustituyen por el nivel de clase.

La subclase exige nivel 15 como mínimo, progreso en el núcleo, nodo de especialización y una prueba/entrenamiento. Solo hay una subclase activa. La clase principal es permanente en la partida normal. Cambiar subclase requiere un procedimiento excepcional y no devuelve automáticamente todos los puntos como si fuese una interfaz de selección libre.

La muerte del jugador no elimina niveles. La pequeña penalización recae únicamente sobre el progreso parcial de experiencia hacia el siguiente nivel de clase; jamás reduce un nivel, punto de talento, desbloqueo de subclase o nivel profesional.

## 45. Arquitectura profesional y especializaciones

### 45.1 Vocabulario común

Para evitar una lista inmanejable de profesiones casi idénticas, el sistema distingue:

- **Profesión:** carrera principal con nivel 1–50, experiencia, herramientas, trabajos y conocimientos propios.
- **Especialización:** rama de una profesión; desbloquea recetas, tareas y técnicas concretas, pero no tiene un nivel separado.
- **Cargo:** responsabilidad asignada por un Ayuntamiento, gremio, facción, templo o negocio; otorga permisos y obligaciones, no una segunda profesión.
- **Rango:** jerarquía dentro de una organización (por ejemplo, sargento o capitán); se basa en méritos, confianza y plazas disponibles.

### 45.2 Normalización de profesiones

Se mantienen las profesiones de extracción, metalurgia, artesanía, construcción, alimentación, comercio, logística, conocimiento, salud y servicio público. Se aplican estas consolidaciones:

| Profesión principal | Especializaciones o tareas que absorbe |
|---|---|
| Constructor | Arquitectura, ingeniería civil, fortificaciones y planificación de obra |
| Albañil | Mampostería, ladrillo, piedra labrada y reparación de muros |
| Transportista | Carretero, transporte de carga y caravanero |
| Mensajero | Cartas, paquetes ligeros, avisos y rutas rápidas |
| Comerciante | Mayorista, tasador, corredor y comercio exterior |
| Banquero | Préstamos, evaluación de riesgo y garantías |
| Ganadero | Pastoreo, cría y monturas |
| Médico | Medicina general, cirugía y obstetricia |
| Recolector/Alquimista | Herbolario, según sea recolección de ingredientes o transformación |
| Investigador | Investigación científica; Investigación arcana y Maestro elemental son ramas avanzadas diferenciadas por contenido |
| Bibliotecario/Escriba/Maestro | Archivo, escritura, docencia y transmisión de conocimiento; conservan tareas diferenciadas, con conocimientos comunes |

Esta consolidación no elimina el contenido ni los edificios temáticos. Evita crear una profesión independiente por cada pequeña variante de tarea. El catálogo de edificios puede tener talleres separados aunque algunas especializaciones compartan profesión.

### 45.3 Carreras cívicas y cargos

Guardia, Diplomático, Juez y Sacerdote pueden ser carreras principales con entrenamiento y progresión. Tesorero, Recaudador, Maestro de la Moneda y Administrador de asentamiento son cargos institucionales que requieren competencia apropiada, confianza y nombramiento. Un personaje no puede autoconcederse autoridad por subir un nivel profesional.

Los cargos tienen mandato, presupuesto, ámbito de permiso, duración, suplente y procedimiento de destitución. Una vacante no transfiere automáticamente dinero, tierras o propiedad privada al sustituto.

### 45.4 Colaboración y conocimiento

Un trabajo coordinado puede tener un responsable y colaboradores. El responsable recibe el resultado y el progreso principal; los colaboradores reciben experiencia proporcional a la tarea realmente ejecutada. No se concede XP por estar asignado a una cola sin trabajar. Las recetas multioficio exigen que cada estación y trabajador realice su etapa; un solo NPC no simula toda la cadena por comodidad.

Los conocimientos compartidos se dividen en básico, aplicado y experto. El nivel básico puede enseñarse entre profesiones relacionadas; el aplicado requiere práctica/maestro; el experto exige profesión, especialización y estación apropiadas. Los libros pueden revelar teoría o receta, pero no reemplazan experiencia práctica ni los materiales necesarios.

## 46. Contrato económico, fabricación y objetos

### 46.1 Calidad frente a tier

El **tier** indica la categoría intrínseca del material, receta o equipo (por ejemplo, hierro, acero o material arcano). La **calidad** indica qué tan bien se fabricó un objeto concreto. No son la misma escala. Un objeto de tier bajo y calidad excepcional puede ser fiable, pero no supera automáticamente las propiedades de un material de tier superior.

La calidad depende de nivel, conocimiento, herramientas, estación, materiales, tiempo, temperatura/proceso y dificultad de receta. El resultado usa rangos legibles: fallido, defectuoso, normal, bueno, excelente y maestro. Los límites dependen de la receta; un aprendiz no puede producir calidad maestra solo con suerte.

### 46.2 Reserva y consumo de materiales

Toda orden de fabricación separa materiales disponibles, materiales reservados y materiales consumidos. Al crear un trabajo, los recursos se reservan de manera atómica; otra orden no puede utilizarlos. Al finalizar, solo se consumen los ingredientes declarados. Al cancelar, los materiales no consumidos regresan a su origen, y los consumidos solo se recuperan si la receta/estado de trabajo lo permite. La repetición del evento de finalización no duplica el producto.

### 46.3 Identidad y trazabilidad proporcional

No se asignará un UUID individual a cada bloque o flecha común. Los recursos ordinarios pueden agruparse por lote: tipo, cantidad, origen general y propietario. Los objetos únicos, reliquias, huevos de dragón, equipo de nombre propio, contratos, materiales de misión y activos de alto valor sí tienen ID persistente. Una transferencia mantiene historial y propiedad; una división de lote genera dos cantidades cuya suma no supera la original.

### 46.4 Contratos, encargos y recompensas

Los contratos y tablones de encargos conectan economía, profesiones y aventura. Cada encargo declara emisor, objetivo, cantidad, destino, plazo, riesgo, pago, garantía y condiciones de cumplimiento. El pago proviene del presupuesto del emisor o de un fondo de contrato depositado; no aparece dinero del sistema. Si el encargo se cancela o fracasa, se aplican las cláusulas conocidas y el registro deja constancia.

Tipos iniciales: transporte, escolta, recolección, fabricación, construcción, exploración, caza de criaturas, investigación, recuperación de objeto, defensa y diplomacia. Los encargos repetibles tienen presupuesto, disponibilidad y límite; no pueden ser una fuente infinita de dinero o materiales. Los contratos de mazmorra se publican con advertencia de amenaza y no prometen que el botín histórico siga disponible.

### 46.5 Comercio y precios

Un negocio vende desde inventario propio, compra hasta su presupuesto, guarda reservas para sus operaciones y paga trabajadores. Las ventas entre facciones requieren comprador, vendedor, ruta, capacidad y entrega confirmada. Una caravana interceptada cambia físicamente el propietario de la carga mediante el sistema de botín; nunca crea una copia de la mercancía.

## 47. Asentamientos, población, construcción y logística

### 47.1 Capacidad y población

Las capacidades de referencia por nivel son Campamento 8, Aldea 24, Pueblo 60, Villa 120, Ciudad 250 y Capital 500 residentes. Son objetivos iniciales configurables y sujetos a pruebas, no límites universales del motor. El número de residentes virtuales puede exceder el número de entidades físicas activas; la vivienda, alimento, puestos de trabajo, seguridad y servicios disponibles determinan la capacidad efectiva.

No se permite que un asentamiento mantenga residentes indefinidos sin vivienda ni sustento. La falta de vivienda puede reducir satisfacción, generar hacinamiento y provocar migración; el hambre y la falta de atención pueden causar enfermedad o muerte según duración y gravedad, nunca como un evento aleatorio sin causa registrada.

### 47.2 Necesidades manejables

Las necesidades prioritarias son comida, agua accesible, vivienda, descanso, salario/ingreso, seguridad y salud. El agua se simplifica en el primer lanzamiento: el ciudadano necesita acceso a fuente, pozo o suministro local seguro; no se simula una red urbana de tuberías. El mod no añadirá alcantarillado, recogida de basura ni iluminación municipal obligatoria.

Cada necesidad tiene nivel, tendencia, tiempo antes de consecuencia y vías de resolución. La UI debe explicar por qué baja la satisfacción y qué recurso o servicio falta. Las necesidades no deben cambiar con aleatoriedad opaca ni forzar microgestión individual de cada ciudadano.

### 47.3 Territorio y expansión

Las celdas territoriales lógicas alineadas a chunks usan un coste de mantenimiento por área y valor estratégico. La expansión prioriza continuidad, acceso a recursos, defensa y conexión logística. Reclamaciones desconectadas son posibles solo mediante un permiso o puesto avanzado válido y tienen coste superior. No se permite que un jugador rodee completamente un asentamiento neutral de forma instantánea sin consecuencias diplomáticas.

### 47.4 Construcción y validación funcional

La huella y forma visual no bastan para validar una estación. Un edificio funcional debe cumplir requisitos semánticos: bloques/estaciones reconocidos, acceso seguro, área útil, almacén, ruta de trabajador, combustible y capacidad. Una estación dañada o inaccesible suspende la función correspondiente y genera un informe; no borra toda la identidad del edificio.

Las obras reservan materiales y espacio antes de comenzar. Una orden de obra tiene ID y versión de plano. Si el usuario mueve/rota/modifica el plano, crea una nueva revisión y vuelve a validar espacio, materiales y requisitos. No puede haber dos obras reclamando el mismo espacio incompatible ni consumir dos veces los mismos recursos.

### 47.5 Logística física y recuperación

Los envíos usan estados pendiente, preparado, en tránsito, retrasado, interceptado, entregado o cancelado. El estado intermedio guarda posición lógica, carga, responsable, ruta y último evento confirmado. Si se reinicia el servidor, el envío se reanuda desde su último estado válido. No se materializan caravanas en chunks lejanos ni se fuerza la carga de ruta; se usa desplazamiento abstracto fuera de proximidad y entidad física al acercarse.

## 48. Generación mundial, biomas, dimensiones y mazmorras

### 48.1 Overworld

El Overworld es el espacio principal de asentamientos, comercio y vida civilizada. La generación propia se basa en Vanilla+ y añade biomas, regiones, recursos, estructuras menores, ruinas, rutas, puestos, fortalezas y mazmorras. La densidad se controla por región; los lugares importantes tienen separación mínima y presupuesto de frecuencia. No se debe convertir cada bioma en un lugar con diez estructuras.

### 48.2 Nether y End

El Nether y el End conservan su identidad y progresión vanilla. MEDIEVAL FACTIONS añadirá contenido compatible —ruinas, fortalezas secundarias, refugios, cámaras, templos o complejos temáticos— sin sustituir las fortalezas y bastiones vanilla ni bloquear el acceso al contenido base. Las estructuras avanzadas de estas dimensiones se tratan como expediciones peligrosas y contenido de fase posterior.

### 48.3 Determinismo y reserva de espacio

La colocación de una estructura depende de semilla, versión del generador, dimensión, región y configuración guardada. Los grupos de estructuras mayores usan una reserva lógica regional para evitar solapamientos y mantener distancias. Si una ubicación no cumple terreno, bioma, separación o condiciones de estructura, se descarta o se busca una alternativa determinista; no se reintenta de forma aleatoria cada vez que se carga el chunk.

La actualización del generador no vuelve a colocar estructuras en chunks ya generados. El mundo existente conserva su contenido; las nuevas estructuras aparecen solo en chunks aún no generados o mediante una herramienta administrativa explícita de migración, siempre con copia de seguridad.

### 48.4 Compatibilidad con mods de worldgen

La compatibilidad se declara por perfil y versiones exactas: Vanilla+, Biomes O' Plenty, YUNG's Better Dungeons, When Dungeons Arise y otros candidatos. Un mod externo puede añadir biomas o estructuras sin que MEDIEVAL FACTIONS asuma que controla su frecuencia. Las reglas de generación propias usan etiquetas/familias compatibles y evitan reutilizar features de forma que produzca ciclos de generación.

No se garantiza compatibilidad con todos los modpacks. Si dos mods generan una estructura de la misma categoría, el perfil permite elegir MEDIEVAL FACTIONS, mod externo o ambos con densidades reducidas; el comportamiento predeterminado evita duplicación temática en la misma región. Cada perfil se prueba en mundo nuevo, servidor dedicado y chunks preexistentes.

### 48.5 Mazmorras como lugares persistentes

Las mazmorras son estructuras físicas compartidas en el mundo, no instancias invisibles creadas por jugador. Cada complejo tiene ID, estado, tema, nivel de amenaza base, salas, mecanismos, encuentros, jefe, botín y registro de exploración. La dificultad no escala automáticamente al nivel de quien entra. Puede variar entre variantes del mismo complejo, no dentro de una misma visita por tener un jugador poderoso.

Las salas despejadas, puertas abiertas, llaves consumidas y botín histórico se guardan. Un reinicio, muerte o desconexión no restablece el tesoro. Una nueva ocupación puede introducir criaturas comunes o peligros nuevos después de tiempo y con una causa plausible, pero no repone reliquias únicas ni cofres históricos ya reclamados.

### 48.6 Jefes y recompensas

Cada jefe dispone de telegráficos, fases, ventanas de vulnerabilidad, habilidades, resistencias, límites de control y reglas de recuperación. Los ataques deben tener señales visuales/sonoras y opciones contrarias; el daño no se resuelve con una sola estadística. Los jefes importantes no reaparecen para duplicar artefactos únicos. Si hay repetición de combate por progreso de mundo, solo puede conceder recompensas ordinarias presupuestadas.

Las recompensas se dividen en comunes, raras, limitadas por mazmorra y únicas de mundo. Un objeto único tiene Artifact ID y registro de propiedad. La muerte del poseedor no crea otra copia. No se bloqueará una cadena esencial de progreso detrás de una reliquia única que pueda perderse por destrucción del mundo; siempre habrá una vía alternativa difícil o una solución administrativa documentada.

### 48.7 Exploración y expediciones NPC

Las expediciones NPC reciben destino, objetivo, equipo, suministros, líder, escolta, capacidad de carga y presupuesto. El resultado depende de preparación, conocimiento, amenaza, ruta, clima si aplica, heridas y decisiones. Pueden retirarse o fracasar. El jugador recibe informe; no se le concede información omnisciente de estructuras que su facción no conoce.

## 49. Combate, IA, seguridad y juego multijugador

### 49.1 Dos escalas con reglas compatibles

Cerca del jugador, el combate usa entidades, proyectiles, animaciones, colisiones, equipo y habilidades en tiempo real. Lejos del jugador, combate entre facciones se resuelve por sectores/unidades con composición, terreno, mando, suministro, moral, fatiga, heridas y órdenes. No se simula cada flecha individual fuera de proximidad. La resolución abstracta debe preservar resultados plausibles y permitir informes auditables.

### 49.2 Unidad militar

Una unidad tiene ID, número de efectivos, tipo, nivel de preparación, calidad, equipo agregado, moral, resistencia, suministros, ubicación lógica, mando, órdenes y heridas/pérdidas. Cuando entra en proximidad, puede materializar miembros individuales. Al alejarse, se vuelve a abstraer sin duplicar ni cambiar cantidad de tropas.

### 49.3 Orden y autoridad

Los jugadores con permisos pueden ordenar unidades; los oficiales NPC ejecutan dentro de su mandato. La IA puede recomendar retirada, refuerzo o defensa, pero no iniciar guerras, modificar tratados o comprometer tesorerías importantes sin autoridad. El segundo al mando mantiene únicamente las facultades ya aprobadas por el propietario.

### 49.4 PvP, propiedad y asedio

El PvP no anula la propiedad del mundo. Matar a un jugador puede permitir reclamar el inventario que realmente cae según configuración, pero no otorga automáticamente acceso a cofres, talleres, edificios ni territorios. En asedio, la destrucción se limita a objetivos y ventanas de guerra definidas; los civiles tienen protección reforzada y el daño a edificios estratégicos tiene consecuencias diplomáticas y económicas.

### 49.5 Seguridad del servidor

Toda orden, transacción, daño, permiso, habilidad, reclutamiento y movimiento crítico se valida en servidor. El cliente nunca decide precio, resultado de crafteo, cantidad de inventario, XP, botín, daño final o propiedad. Las peticiones repetidas usan ID idempotente. Se aplican límites de frecuencia a mensajes, rutas, órdenes de construcción y comercio para evitar spam y lag.

## 50. Arquitectura de producción, pruebas y alcance

### 50.1 Línea técnica congelada

La primera línea de lanzamiento se fija en Minecraft Java 1.21.1, NeoForge y Java 21. Antes de abrir una fase de programación se congelarán las versiones exactas de Minecraft, NeoForge MDK, Gradle, JDK, mappings, librerías y mods de prueba. No se actualizará una dependencia en mitad de un hito sin ejecutar la matriz de regresión.

### 50.2 Capas de implementación

El código se separará en dominios con interfaces claras:

1. **Core de datos:** IDs, serialización, migración, registros y eventos.
2. **Servidor/autoridad:** validación de órdenes, economía, propiedad y reglas de juego.
3. **Simulación:** agentes NPC, facciones, rutas, eventos y resolución distante.
4. **Mundo:** biomas, estructuras, recursos, edificios y mazmorras.
5. **Progresión:** razas, stats, clases, profesiones, recetas y habilidades.
6. **Presentación:** UI, mapa, informes, partículas, sonidos y animaciones.
7. **Compatibilidad:** adaptadores opcionales por mod/versión, nunca acceso directo a clases de mod externo desde el núcleo.

Una falla de compatibilidad de un mod externo no debe impedir iniciar un mundo Vanilla+ salvo que el servidor haya marcado ese mod como dependencia obligatoria de su configuración.

### 50.3 Pruebas de aceptación por dominio

- **Persistencia:** guardar, cerrar, reiniciar y comparar estado; migrar datos antiguos dos veces sin duplicar ni perder operaciones.
- **Economía:** reconciliar saldos, objetos físicos, préstamos, acuñación, impuestos, pagos y sumideros con un balance contable exacto.
- **NPC:** simular facción pequeña, mediana y grande; verificar necesidades, mortalidad, empleo, colas y decisiones delegadas.
- **Construcción:** cancelar en cada etapa, desconectar trabajador, destruir estación, cambiar plano y recuperar materiales sin duplicarlos.
- **Combate:** probar contraataques, resistencias, estados, latencia, reconexión, muerte y resolución lejana/cercana coherente.
- **Worldgen:** ejecutar múltiples semillas con mapas de densidad, separación, chunks de borde, biomas externos y dimensiones.
- **Mazmorras:** entrar con uno y varios jugadores, desconectar durante jefe, reiniciar servidor, reclamar botín único y comprobar que no se duplica.
- **Multijugador:** dos propietarios de una facción, permisos simultáneos, desconexión en transacción y disputa de un mismo recurso.

### 50.4 Rendimiento y degradación elegante

El servidor medirá tiempo de tick medio, p95 y p99, memoria, tareas de IA, número de agentes físicos, longitud de colas, operaciones de persistencia y chunks activos. Los límites objetivo se establecen por una máquina de referencia documentada, no por una cifra universal sin hardware.

Cuando se exceda el presupuesto, el sistema degrada en este orden: aplazar decisiones de agentes distantes, reducir frecuencia de informes cosméticos, agrupar cálculos de mercado, reducir detalle de encuentros lejanos y posponer eventos no críticos. Nunca elimina una transacción confirmada, cambia propiedad ni duplica producción para “recuperar” tiempo.

### 50.5 Política de compatibilidad

Una integración externa solo se marca como compatible si una combinación de versiones concreta supera pruebas de carga, mundo nuevo, generación, servidor dedicado, reinicio y multijugador. “Arranca sin crash” no es criterio suficiente. El núcleo debe seguir jugable sin mods de terceros.

### 50.6 Hoja de ruta de decisión

La vertical slice debe incluir: un personaje, una familia racial, una afinidad/elemento, una clase, una profesión, una cadena de producción de tres pasos, un NPC, una vivienda, un taller, un asentamiento, una transacción monetaria, una estructura exploratoria y una mazmorra pequeña. Solo después de probar persistencia y rendimiento se amplían las familias, clases, razas, ciudades, guerras y dragones.

## 51. Dictamen final de auditoría v1.3

La auditoría no cambia la fantasía central de MEDIEVAL FACTIONS. La hace más ejecutable. Se han normalizado estadísticas y rasgos, la diferencia entre profesión/especialización/cargo/rango, la progresión de combate y oficio, la identidad de objetos, los contratos, las necesidades de población, la construcción funcional, la generación por dimensiones, la persistencia de mazmorras y la resolución de combate en dos escalas.

**La prioridad absoluta sigue siendo la simulación verificable:** un trabajador debe consumir recursos y cobrar; un edificio debe funcionar por sus estaciones; una caravana debe transportar una carga real; un NPC debe tener consecuencias familiares y económicas; un botín único no puede duplicarse; una facción debe poder prosperar o fracasar sin que el jugador la controle directamente.

El documento v1.3 queda como referencia maestra de diseño. Ninguna funcionalidad se considera implementada hasta que exista código probado, se valide en servidor dedicado y supere los criterios de aceptación. Las cifras de población, bonificaciones, tiempos, densidades y rendimiento siguen sujetas a prototipos y pruebas de equilibrio.
