# MEDIEVAL FACTIONS
## Biblia visual, skins, modelos y animaciones

**Versión:** 1.0 — diseño visual integral  
**Proyecto base:** MEDIEVAL FACTIONS v1.3  
**Objetivo técnico:** Minecraft Java 1.21.1 · NeoForge · Java 21  
**Estado:** diseño conceptual y especificación de producción; no contiene todavía modelos, skins UV ni animaciones ejecutables dentro del mod.

---

## 0. Objetivo y alcance

Esta biblia define la identidad visual y el catálogo de animaciones que deberá tener MEDIEVAL FACTIONS. No es una colección de imágenes inconexas: fija reglas compartidas para que personajes, razas, profesiones, clases, armas, armaduras, criaturas, monturas, dragones, edificios, efectos y menús parezcan pertenecer al mismo universo.

El objetivo es que el mod tenga una presentación reconocible y espectacular sin perder la lectura visual, la estética cúbica ni la compatibilidad con Minecraft. La calidad debe venir de buenas siluetas, animación, materiales, iluminación, sonido y efectos sincronizados, no de saturar la pantalla con partículas.

### Alcance visual

1. Estilo artístico, paletas, materiales, proporciones y reglas de lectura.
2. Modelos y skins de personajes, las 16 familias raciales y sus variantes.
3. Animaciones generales del jugador y NPC.
4. Animaciones de combate para armas, defensa, movimiento y reacciones.
5. Animaciones y efectos de las 15 clases, sus subclases y habilidades.
6. Magia de los 12 elementos Solar/Lunar.
7. Animaciones de todas las familias profesionales y estaciones de trabajo.
8. Animales, criaturas, monturas, dragones y sus equipamientos.
9. Armas, armaduras, escudos, herramientas, accesorios y materiales.
10. Ciudades, edificios, construcción, estructuras, mazmorras y ambiente.
11. Efectos visuales, partículas, sonido, iluminación e interfaz.
12. Formatos, herramientas, nombres, control de calidad y fases de producción.

**Límite honesto:** una lámina conceptual no equivale a una skin 64×64 con UV correcta, un modelo Blockbench, un rig o un archivo de animación. Cada diseño descrito aquí deberá producirse como recurso técnico, probarse en Minecraft y validarse en cliente y servidor. La lámina generada que acompaña esta biblia sirve como dirección de arte, no como asset listo para instalar.

---

# PARTE I — DIRECCIÓN ARTÍSTICA

## 1. Estilo visual oficial

### 1.1 Identidad

**Nombre interno del estilo:** Fantasía medieval voxel premium.

- Siluetas cúbicas y proporciones compatibles con el lenguaje visual de Minecraft.
- Modelos con volumen adicional donde aporten lectura: cuernos, orejas, capas, hombreras, armas, alas, colas, cuernos de montura y piezas de armadura.
- Texturas pixel-art limpias, con sombreado por planos, detalle pintado y variación material controlada.
- Animaciones fluidas de huesos y articulaciones; los modelos no deben parecer muñecos rígidos.
- Iluminación cálida en aldeas y ciudades; fría y atmosférica en ruinas, mazmorras y regiones hostiles.
- Magia de color intenso, pero con formas y siluetas reconocibles a distancia.
- Decoración medieval funcional: remaches, cuero, costuras, madera, piedra, metal, heráldica, telas, runas y desgaste.

No se utilizará hiperrealismo, armas con detalles diminutos ilegibles, exceso de brillo metálico ni partículas permanentes que reduzcan FPS o visibilidad en combate.

### 1.2 Proporciones y legibilidad

- El jugador conserva la silueta y escala reconocibles de Minecraft.
- La cabeza, manos, pies y articulaciones no se deforman de forma extrema.
- Las piezas raciales se añaden por capas o rigs especializados cuando sea necesario.
- Armas de dos manos deben leerse claramente como tales; escudos no deben ocultar toda la silueta.
- Las armaduras tienen contorno distinto por peso: ligera, media, pesada y arcana.
- Las criaturas se reconocen por silueta antes que por color.
- La rareza no se comunica únicamente mediante partículas: también mediante forma, material, ornamentación y sonido.

### 1.3 Paleta global

| Uso | Paleta | Aplicación |
|---|---|---|
| Reino/civilización neutral | Piedra gris, madera nogal, hierro oscuro | Edificios comunes, herramientas y señalización |
| Solar | Oro viejo, marfil, cobre cálido, naranja | Emblemas, energía y ornamentación Solar |
| Lunar | Plata, azul noche, violeta, turquesa | Energía y ornamentación Lunar |
| Magia arcana | Índigo, violeta, cian | Éter, grimorios, runas y artefactos |
| Naturaleza | Musgo, jade, corteza, ámbar | Bosques, druidas y herbolaria |
| Peligro | Carmesí, negro carbón, naranja de brasa | Jefes, maldiciones, asedios y alertas |
| Curación | Marfil, verde suave, oro tenue | Medicina, bendiciones y protección |

Los colores de facción serán configurables. La paleta anterior define materiales comunes del mundo, no obliga a todas las facciones a usar los mismos estandartes.

### 1.4 Materiales y sombreado

Cada material deberá tener cuatro rasgos legibles: color base, respuesta a luz, patrón superficial y sonido asociado.

- **Madera:** veta pixelada orientada, bordes desgastados, tonos distintos por especie.
- **Piedra:** bloques grandes, grietas por desgaste, manchas minerales discretas.
- **Cuero:** costuras visibles, bordes cosidos, desgaste en articulaciones.
- **Tela:** pliegues mediante sombreado por planos; movimiento moderado en capas y estandartes.
- **Hierro/acero:** metal mate con bordes pulidos; no usar espejo realista.
- **Oro:** reflejo cálido y ornamentación limitada.
- **Cristal/runa:** transparencia o emisión controlada en puntos concretos.
- **Material elemental:** patrón y color de elemento, nunca un simple tinte uniforme.

### 1.5 Resoluciones objetivo

Son objetivos iniciales que se ajustarán en pruebas de atlas y mipmaps.

| Asset | Resolución inicial | Regla |
|---|---:|---|
| Skin humana de jugador | 64×64 | UV compatible; capas base y overlay |
| Cara/skin racial de NPC | 64×64 o 128×128 | Según complejidad y rig |
| Armas e iconos | 32×32 / 64×64 | Silueta visible en inventario |
| Bloques | 16×16 o 32×32 | Mantener lectura Minecraft |
| Criaturas pequeñas | 64×64 / 128×128 | Una textura por familia de rig con variantes |
| Criaturas grandes/dragones | 128×128 / 256×256 | Atlas por región corporal si hace falta |
| Armadura/modelo articulado | 64×64 / 128×128 | Capas por material, no duplicar modelo por color |
| Iconos de habilidades | 32×32 / 64×64 | Fondo transparente, símbolo central |
| Partículas | 8×8 a 32×32 | Siluetas simples y poco ruido |
| Interfaz | Escalable | Vector/atlas de interfaz, no texto rasterizado |

No crear una textura enorme para cada variación de color. Se reutilizarán atlas, paletas y capas.

## 2. Lenguaje de animación

### 2.1 Reglas de movimiento

- Anticipación breve antes de ataques, habilidades y trabajo físico.
- El movimiento principal debe verse primero; los efectos aparecen después del gesto y coinciden con el impacto.
- La recuperación del movimiento evita que todos los ataques parezcan instantáneos.
- El peso se comunica con postura, aceleración, frenado, sonido y tiempo de recuperación.
- Criaturas grandes tienen aceleraciones y giros más amplios; criaturas pequeñas reaccionan más rápido.
- La magia tiene preparación, emisión, impacto y disipación; no debe ser solo un proyectil de color.
- Animaciones jugables deben poder cancelarse cuando las reglas de combate permitan interrupción.
- No bloquear el control del jugador con cinemáticas largas para ataques ordinarios.

### 2.2 Capas y prioridades

El sistema combinará capas para que caminar y atacar no se anulen entre sí:

1. **Locomoción:** quieto, caminar, correr, saltar, nadar, trepar.
2. **Postura/equipo:** arma preparada, escudo, arco tensado, instrumento, herramienta.
3. **Acción:** ataque, lanzamiento, curación, recolección, trabajo, interacción.
4. **Reacción:** recibir golpe, empuje, bloqueo, derribo, muerte.
5. **Cosmética:** respiración, capa, orejas, cola, alas, accesorios y emotes.

Prioridad: muerte/derrumbe > reacción fuerte > acción comprometida > postura > locomoción > idle/cosmética. La lógica de combate decide cuándo una acción se puede cancelar.

### 2.3 Convención temporal

- Idles: bucles de 4–8 segundos con variaciones para evitar repetición sincronizada.
- Caminar/correr: ciclos ligados a velocidad real, no a un temporizador fijo.
- Ataques rápidos: preparación y recuperación cortas; impacto sincronizado con hitbox.
- Ataques pesados: anticipación visible, impacto fuerte y recuperación clara.
- Lanzamientos: ventana de salida del proyectil marcada por evento de animación.
- Trabajo: ciclos repetibles con pausas, sin que cada NPC esté perfectamente sincronizado.
- Animaciones de jefe: fases de lectura clara, telegráfos y ventanas de respuesta.

Los tiempos numéricos finales se medirán en prototipos. La animación no puede cambiar por sí sola el momento real del daño o el resultado del servidor.

---

# PARTE II — PERSONAJES, SKINS Y RIGS

## 3. Sistema de personajes

### 3.1 Capas de apariencia

Cada personaje se construye con capas independientes:

1. Cuerpo/base racial.
2. Cara, ojos, cabello, orejas y cuernos.
3. Rasgos especiales: cola, alas, escamas, pelaje, aletas, colmillos o placas.
4. Ropa cultural.
5. Armadura por piezas: casco, torso, brazos, piernas, botas.
6. Accesorios: capa, cinturón, mochila, joyas, insignias, escudo.
7. Heráldica: emblema, colores y rango.
8. Efectos de afinidad, elemento, estado o encantamiento.

La apariencia no debe revelar automáticamente estadísticas ocultas. Los elementos visibles deben proceder de la raza, equipo, profesión, cultura, facción o efecto real.

### 3.2 Rigs base reutilizables

| Rig | Uso | Huesos/partes esenciales |
|---|---|---|
| Humanoide estándar | Humanos, elfos, enanos, orcos, goblins, vampiros, no muertos, celestiales, demonios y parte de hombres bestia | pelvis, torso, cabeza, brazos, antebrazos, manos, muslos, piernas, pies |
| Humanoide con apéndices | Hombres bestia, dracónidos, arpías | estándar + orejas/cuerno/cola/alas/crestas |
| Humanoide grande | Gigantes y variantes enormes | proporciones mayores, inercia y alcance diferentes |
| Centauro | Centauros | torso humanoide + cuatro patas + cola |
| Sirénido acuático | Sirénidos | torso + cola/aletón; variante de piernas en tierra si aplica al lore |
| Cuadrúpedo | Caballos, lobos, lagartos y bestias | columna, cuello, cabeza, cuatro patas, cola |
| Ave/volador | Arpías, aves de monta, grifos, rocs | torso, cuello, cabeza, alas segmentadas, patas, cola |
| Acuático | Tortugas gigantes y criaturas nadadoras | cuerpo, aletas/patas, cola, mandíbula |
| Serpentino | Serpientes y criaturas alargadas | cadena de segmentos, cabeza, mandíbula |
| Dragón | Dragones | columna, cuello, cabeza, mandíbula, alas segmentadas, patas, cola, placas/cuernos |
| Constructo | Gólems y guardianes | torso, cabeza, brazos, piernas, núcleo y piezas desmontables |
| Espectral | fantasmas, espíritus y apariciones | cuerpo flotante, tela/estela, núcleo y partículas |

### 3.3 Variantes de skin por cultura

Cada familia racial tendrá una base de identidad y varias líneas culturales. La raza define rasgos anatómicos; la cultura define ropa, arquitectura, patrones, joyería, armas ceremoniales y colores. No generar una textura completamente nueva para cada combinación: usar piezas y overlays intercambiables.

## 4. Identidad visual de las 16 familias raciales

| Familia | Rasgos de modelo/skin | Vestimenta y ornamento | Animación distintiva |
|---|---|---|---|
| Humanos | Variedad amplia de tonos de piel, cabello y complexión | Adaptación cultural; ropa práctica, heráldica flexible | Sin gesto racial obligatorio; amplia biblioteca de emotes |
| Elfos | Orejas alargadas, silueta estilizada, facciones finas | Telas ligeras, bordados vegetales, cuero fino, arquería | Giros suaves de cabeza, pasos ligeros, postura de arco elegante |
| Enanos | Centro de gravedad bajo, manos robustas, barbas opcionales y variadas | Metal trabajado, cuero grueso, runas geométricas, trenzas | Pisada pesada, golpe de martillo con cuerpo completo, inspección de barba/equipo |
| Orcos | Mandíbula fuerte, colmillos, musculatura marcada | Cuero, placas, hueso, hierro oscuro, pinturas tribales culturales | Postura amplia, respiración fuerte, golpes con seguimiento pesado |
| Hombres bestia | Orejas/cola/facciones según linaje animal | Pieles, cuero, tejidos resistentes, adornos de clan | Orejas y cola reaccionan, olfateo/rastreo, carrera diferenciada |
| Goblins | Estatura baja, orejas grandes, nariz y manos expresivas | Ropa remendada, herramientas, cinturones, gafas y bolsas | Gestos rápidos, mirar objetos, postura inquieta y movimientos ágiles |
| Dracónidos | Escamas, cuernos, cola, crestas y placas según subraza | Armadura abierta para cola/cuello, metal y piezas ceremoniales | Cola contrapesa, respiración torácica, giro de cuello y preparación de aliento |
| Vampiros | Palidez/tonos propios del linaje, colmillos discretos, ojos variables | Capas, cuellos altos, guantes, joyería y telas nobles/oscuras | Movimiento elegante, mirada fija, gesto de alimentación solo en contexto no explícito |
| No muertos | Tejido deteriorado, hueso o piel conservada según subraza | Vendas, piezas reparadas, placas antiguas, amuletos | Movimiento por tipo: esqueleto articulado, zombi pesado, espectro flotante |
| Feéricos | Cuerpo pequeño o humanoide según subraza, rasgos vegetales/iridiscentes | Hojas, pétalos, telas livianas, gemas pequeñas | Flotación breve si la anatomía lo permite, giros y aleteo sutil |
| Celestiales | Rasgos luminosos moderados, alas solo en subrazas que las posean | Marfil, oro pálido, símbolos solares/sagrados | Postura erguida, despliegue de alas y gestos ceremoniales |
| Demonios | Cuernos, cola, ojos o placas según subraza | Carbón, hierro, cuero, motivos angulares y brasas | Cola reactiva, postura tensa, pequeños pulsos de energía |
| Sirénidos | Escamas, aletas, branquias y cola acuática | Conchas, coral, telas resistentes al agua y adornos marinos | Nado ondulatorio, giro de cola, transición agua/tierra según anatomía |
| Centauros | Torso humanoide unido a cuerpo equino | Arnés, mantas, placas y adornos ecuestres | Galope, trote, giro amplio, arrodillamiento adaptado y ataque montado propio |
| Gigantes | Escala superior, articulaciones y manos grandes | Piezas reforzadas, pieles, placas y cinturones anchos | Paso pesado, giro lento, golpe con anticipación y recuperación prolongada |
| Arpías | Alas/brazos fusionados o separados según subraza, garras y plumas | Arnés ligero, protecciones de hombro, plumas decorativas | Aleteo plegado/desplegado, salto planeado, aterrizaje con absorción |

### 4.1 Subrazas

Las subrazas no tendrán un rig nuevo por defecto. Cada una se definirá con:
- paleta de piel/escamas/plumas;
- una diferencia anatómica visible;
- una o dos piezas cosméticas propias;
- una variante de idle o movimiento si realmente cambia la anatomía;
- rasgos de cultura y vestimenta.

Solo se construirá un rig nuevo cuando una subraza tenga una anatomía funcionalmente distinta, por ejemplo centauro, sirénido con cola permanente o arpía voladora. No se crearán docenas de rigs casi idénticos.

## 5. Skins de jugador y NPC

### 5.1 Skin base de jugador

- Mantener UV reconocible y compatible con el renderizador de jugador.
- Separar textura de piel, cabello, rasgos raciales, ropa y armadura siempre que el sistema técnico lo permita.
- Ofrecer modelos estándar y slim/anchos cuando no rompan equipo ni animación.
- Accesorios raciales deben tener puntos de anclaje definidos.
- Capas de armadura no deben atravesar orejas, colas, alas ni cuernos; se necesitan variantes de casco/capa.

### 5.2 NPC

Los NPC deben variar sin requerir una textura exclusiva por individuo. La variación se genera con combinaciones de:
- paletas de piel/cabello/ojos;
- peinados y barbas;
- conjuntos de ropa por cultura y estación;
- desgaste por profesión y trabajo;
- rango, edad, estado civil o pertenencia a gremio si el juego lo muestra;
- equipo real del inventario.

Los NPC importantes podrán tener retrato y aspecto guardado; los NPC comunes usan combinaciones procedurales controladas.

### 5.3 Ropa por profesión y situación

- **Civil:** túnicas, camisas, pantalones, vestidos, capas, botas y cinturones.
- **Artesano:** delantal, guantes, protección de cuero, cinturones de herramientas.
- **Minero/cantero:** casco, lámpara, protección de hombros, botas robustas.
- **Agricultor/ganadero:** ropa resistente, sombrero, guantes, botas y bolsa.
- **Comerciante:** ropa limpia, bolsa de dinero, joyería discreta y emblema de tienda.
- **Guardia/soldado:** uniforme por facción, tabardo, protección y rango.
- **Académico/mago:** túnicas, capa, cinturón de componentes, grimorio o catalizador.
- **Médico:** túnica de trabajo, delantal limpio, bolsa médica y distintivo.
- **Religioso:** hábito/vestimenta de doctrina y símbolo del templo.
- **Nobleza:** tejidos caros, bordados, joyas y colores heráldicos.

---

# PARTE III — ANIMACIONES DEL JUGADOR

## 6. Biblioteca universal de animaciones

### 6.1 Estados locomotores

| ID | Animación | Comportamiento |
|---|---|---|
| `idle_relaxed` | Quieto relajado | Respiración leve y microajustes |
| `idle_alert` | Quieto alerta | Cabeza/torso orientados a amenaza o punto de interés |
| `idle_tired` | Cansancio | Hombros y respiración afectados por fatiga |
| `idle_injured` | Herido | Protección de zona dañada y postura reducida |
| `walk_forward` | Caminar | Ciclo base |
| `walk_backward` | Retroceder | Paso corto, mirada al frente |
| `strafe_left/right` | Desplazamiento lateral | Mantiene orientación de combate |
| `run` | Correr | Brazos y torso con mayor amplitud |
| `sprint` | Esprintar | Inclinación y braceo, consumiendo resistencia según reglas |
| `crouch_idle` | Agachado | Centro de gravedad bajo |
| `crouch_walk` | Andar agachado | Pasos cortos y silenciosos |
| `jump_start` | Despegue | Compresión de piernas y extensión |
| `jump_air` | En el aire | Ajuste de brazos/piernas |
| `fall` | Caída | Brazos de equilibrio |
| `land_soft` | Aterrizaje leve | Flexión corta |
| `land_hard` | Aterrizaje fuerte | Flexión profunda y recuperación |
| `swim_surface` | Nado superficial | Brazada alterna |
| `swim_dive` | Nado sumergido | Brazada compacta |
| `climb_ladder` | Escalera | Alternancia de brazos/piernas |
| `climb_ledge` | Subir borde | Agarre y elevación del cuerpo si se habilita |
| `crawl` | Reptar | Movimiento bajo y lento, solo donde sea compatible |
| `sleep` | Dormir | Pose en cama o lugar de descanso |
| `sit` | Sentarse | Silla/banco con ajuste de altura |
| `mount` / `dismount` | Montar/desmontar | Secuencia adaptada a cada montura |
| `death` | Muerte | Caída y transición al estado de muerte, sin bloquear cámara excesivamente |

### 6.2 Interacciones y emotes

- Saludar, asentir, negar, señalar, aplaudir, celebrar, rendirse, amenazar y pedir ayuda.
- Conversar: gestos de mano, giro de cabeza, escucha y reacción.
- Leer libro, escribir, consultar mapa, inspeccionar objeto y mostrar mercancía.
- Abrir/cerrar puerta, cofre, baúl, cajón, portón, palanca y mecanismo.
- Recoger del suelo, guardar, sacar y entregar objeto.
- Comer, beber, brindar, descansar, rezar y tocar instrumento.
- Dar la mano, inclinarse, reverencia ceremonial, saludo militar y gesto de autoridad.
- Cortejo, abrazo y boda se tratarán como animaciones sociales breves, con consentimiento y sin contenido sexual explícito.

### 6.3 Reacciones

- `hurt_light`: impacto leve, apenas altera el control.
- `hurt_heavy`: retroceso visible y breve interrupción.
- `block_react`: absorbe golpe mediante arma/escudo.
- `parry_react`: desviación compacta y contraataque habilitado por mecánica.
- `stagger`: desequilibrio temporal.
- `knockback`: desplazamiento impuesto por el servidor.
- `knockdown`: caída y recuperación, reservada a ataques fuertes.
- `burn`, `freeze`, `shock`, `poison`, `curse`: overlays visuales no invasivos que comunican estado.

La animación de reacción nunca puede falsear la posición real del servidor. La interpolación visual debe reconciliarse con la autoridad del servidor.

## 7. Animaciones de combate

### 7.1 Sistema de arma

Cada arma define una familia de movimiento, no una animación totalmente nueva por material. El material cambia aspecto, partículas, sonido y peso percibido dentro de límites; la clase/estadística define velocidad y recuperación.

| Familia | Preparación | Ataque | Recuperación/variantes |
|---|---|---|---|
| Espada 1 mano | Guardia alta/media | Corte diagonal, horizontal, estocada | Retorno, combo 2–3 golpes |
| Espadón | Carga con torso y cadera | Barrido amplio, golpe descendente | Arrastre, recuperación larga |
| Katana | Guardia contenida | Desenvainado/corte rápido, estocada | Envainado opcional solo fuera de combate |
| Hacha 1 mano | Hombro hacia atrás | Corte diagonal | Rebote de impacto y vuelta a guardia |
| Hacha 2 manos | Elevación completa | Golpe pesado/barrido | Inercia visible |
| Martillo de guerra | Peso retrasado | Golpe descendente/lateral | Recolocación lenta |
| Lanza | Guardia extendida | Estocada, barrido, contra-carga | Retirada de punta y reposición |
| Daga/dual | Guardias compactas | Cortes cortos, alternancia y giro | Paso lateral y recuperación rápida |
| Escudo | Brazo adelantado | Bloqueo, golpe de escudo, parry | Sacudida según fuerza recibida |
| Arco | Sacar, colocar flecha, tensar | Soltar cuerda | Relajar, recargar |
| Ballesta | Preparar, cargar, bloquear mecanismo | Disparo | Recarga con dos manos |
| Bastón/catalizador | Apuntar y canalizar | Descarga o conjuro | Descenso del foco |
| Puños/guanteletes | Guardia cerrada | Jab, cruzado, gancho, golpe pesado | Recuperación de hombros |
| Instrumento de Bardo | Elevar instrumento | Tocar patrón de canción | Mantener interpretación y finalizar |

### 7.2 Combate cuerpo a cuerpo universal

- Guardia con arma equipada.
- Ataque ligero en cadena de hasta tres variantes por familia.
- Ataque cargado con anticipación y lectura visual.
- Ataque aéreo si el arma lo admite.
- Ataque en carrera para armas compatibles.
- Bloqueo, bloqueo perfecto/parry si la habilidad lo permite.
- Esquiva corta, paso lateral y rodar según clase/armadura.
- Cambio de postura con transición breve.
- Desenvainar/envainar fuera de combate cuando proceda.
- Remate contextual solo en enemigos derrotados y bajo regla de juego; no iniciar cinemática larga en PvP.

### 7.3 Combate a distancia

Arco y ballesta tienen preparación, tensión/carga, punto de disparo y recuperación. Las flechas/virotes se sincronizan con el evento de salida; proyectiles mágicos usan el mismo principio. Apuntar debe ser compatible con primera y tercera persona. La cámara no puede forzar ángulos que revelen información injusta en PvP.

### 7.4 Daño, impacto y feedback

- Golpe normal: sonido de material y pequeña chispa/impacto.
- Armadura pesada: sonido más grave, rebote metálico y menor reacción corporal.
- Golpe crítico: destello breve, sonido distintivo y reacción mayor.
- Bloqueo: chispas pequeñas y desplazamiento del arma/escudo.
- Parry: flash de 1 instante, sonido claro y breve ventana de respuesta.
- Daño elemental: overlay del elemento, no explosión excesiva en cada golpe.
- Jefe: barra/telegráfo, animación de fase y señal sonora específica.

---

# PARTE IV — CLASES, SUBCLASES Y HABILIDADES

## 8. Firma visual de las 15 clases

Cada clase tendrá una postura de idle, movimiento distintivo, animaciones de ataque principales, reacción y firma visual para habilidad máxima. Las subclases modifican gestos, efectos, equipo y prioridades de animación; no requieren un rig completo nuevo.

| Clase | Postura y movimiento | Firma de habilidades |
|---|---|---|
| Guerrero | Guardia firme, pies asentados, transiciones limpias | Arcos de acero, impacto pesado, choque de escudos y estela corta |
| Arquero | Postura lateral, equilibrio y control de respiración | Línea de mira, tensión de cuerda y flecha con estela fina |
| Ballestero | Preparación mecánica, apoyo de hombro | Recarga visible, disparo seco, virotes pesados y marcas de precisión |
| Mago | Mano/catalizador conductor, gestos amplios | Runas, círculos, trazos geométricos y descarga elemental |
| Paladín | Postura protectora, escudo alto | Sellos sagrados, barreras, destellos marfil/oro y golpes de juicio |
| Asesino | Perfil bajo, pasos cortos y cambios de dirección | Desvanecimiento breve, humo dirigido, cortes en ángulos controlados |
| Berserker | Centro adelantado, respiración fuerte | Acumulación de furia, golpe masivo y ondas de impacto rojas |
| Druida | Gestos circulares y contacto con suelo/naturaleza | Raíces, hojas, semillas luminosas y transformación por etapas |
| Nigromante | Gestos precisos, foco oscuro | Huesos que emergen, cadenas de alma, niebla y sellos umbríos |
| Monje | Guardia compacta y fluidez corporal | Golpes de palma, ondas cinéticas y secuencias rítmicas |
| Invocador | Postura de canalización y señalamiento | Portal/sello, ensamblaje de invocación y vínculo energético |
| Bardo | Instrumento y movimiento rítmico | Ondas musicales legibles, notas/runa y aura sincronizada con tempo |
| Espadachín arcano | Esgrima + gesto de mano libre | Trazos de espada rúnica y cortes con estela elemental breve |
| Clérigo | Gestos de bendición, protección y diagnóstico | Símbolos, luz suave, campanadas y sellos de purificación |
| Lancero | Base larga, punta alineada | Estocada de alcance, barrido, formación y carga lineal |

## 9. Paquetes de animación por clase y subclase

### 9.1 Guerrero

- **Guardián:** guardia de escudo, interposición, bloqueo sostenido, impacto defensivo y desafío.
- **Maestro de armas:** transición fluida entre familias, cambio de agarre y combos encadenados.
- **Vanguardia:** carrera de entrada, golpe de apertura, empuje y postura agresiva.

### 9.2 Arquero

- **Ojo certero:** respiración contenida, estabilización del arco, disparo de precisión y enfoque de objetivo.
- **Explorador:** disparo en movimiento, rodilla en tierra, retirada y colocación de trampa.
- **Viento veloz:** doble disparo, cambio rápido de flecha y desplazamiento lateral.

### 9.3 Ballestero

- **Tirador de precisión:** apoyo firme, ajuste de mira y disparo único de gran preparación.
- **Ballestero de asedio:** postura pesada, recarga asistida y proyectil de gran impacto.
- **Repetidor:** ciclo mecánico rápido y ráfaga controlada.

### 9.4 Mago

- **Elementalista:** gestos amplios, canalización elemental y descarga de área.
- **Arcanista:** runas geométricas, sellos orbitantes y proyectiles precisos.
- **Mago de batalla:** conjuro con paso/ataque, escudo arcano y magia de corto alcance.

### 9.5 Paladín

- **Cruzado:** avance con escudo, golpe de juicio y embestida.
- **Templario:** canalización de aura, barrera y consagración de zona.
- **Guardián sagrado:** interceptar, proteger aliado y curación defensiva.

### 9.6 Asesino

- **Sombra:** ocultación breve, aparición lateral y ataque desde ángulo.
- **Envenenador:** aplicar toxina, gesto de frasco/hoja y nube pequeña.
- **Hoja nocturna:** secuencia de dos armas, corte cruzado y retirada oscura.

### 9.7 Berserker

- **Devastador:** golpe descendente y ruptura de guardia.
- **Furia sangrienta:** acumulación visual gradual, rugido corto y aceleración.
- **Coloso:** postura de anclaje, golpe de suelo y resistencia a empuje.

### 9.8 Druida

- **Guardián del bosque:** raíces defensivas, escudo de corteza y brote protector.
- **Llamabestias:** gesto de llamada, vínculo con criatura y orden de ataque.
- **Cambiapieles:** transición de forma en tres fases, silueta legible y retorno controlado.

### 9.9 Nigromante

- **Señor de huesos:** levantar esqueleto, ordenar formación y descarga ósea.
- **Vinculaalmas:** cadena espiritual, absorción y transferencia limitada.
- **Adepto lich:** sello de longevidad mágica, explosión de energía y aura de fase alta.

### 9.10 Monje

- **Puño de hierro:** puñetazos compactos, parry corporal y golpe concentrado.
- **Camino del flujo:** esquiva circular, redirección y combo continuo.
- **Asceta:** postura meditativa, barrera interna y pulso espiritual.

### 9.11 Invocador

- **Vinculapactos:** sello de contrato y manifestación de entidad vinculada.
- **Conjurador:** círculo de invocación y aparición por partes.
- **Comandante espiritual:** gestos de orden, formación y efecto de coordinación.

### 9.12 Bardo

- **Soporte:** interpretación dirigida al grupo, ondas que se expanden hacia aliados, gesto de protección y cierre de canción.
- **Potenciación física:** interpretación personal, aura compacta, transición instrumento/guanteletes y ataque rítmico.

Las canciones requieren inicio/casteo y luego mantienen el efecto sin que el personaje tenga que tocar constantemente, según las reglas del sistema Bardo ya acordadas. La animación debe mostrar claramente el inicio y la finalización del efecto. No se mostrarán tres instrumentos simultáneos ni efectos visuales que tapen al grupo.

### 9.13 Espadachín arcano

- **Hoja hechizada:** imbuir arma, corte elemental y disipación.
- **Duelista rúnico:** parry con sello, estocada y contraataque.
- **Hoja del vacío:** corte de espacio estilizado, desplazamiento corto y estela oscura.

### 9.14 Clérigo

- **Sanador:** diagnóstico, canalización de cura, círculo de recuperación.
- **Exorcista:** sello, expulsión de entidad y onda de purificación.
- **Inquisidor:** arma consagrada, marca y golpe de condena.

### 9.15 Lancero

- **Caballero de lanza:** bajar lanza, cargar, impacto montado y recuperación.
- **Falanx:** postura de formación, bloqueo de línea y estocada colectiva.
- **Hostigador:** lanzar, recuperar distancia y estocada rápida.

## 10. Gramática de animación de habilidades

Toda habilidad tendrá una secuencia compuesta por estados reutilizables:

1. **Preparación:** arma/manos/torso adoptan postura; aparece señal visual si el ataque puede esquivarse.
2. **Canalización:** foco, runa, postura o carga energética.
3. **Liberación:** evento exacto que activa proyectil, hitbox o efecto del servidor.
4. **Impacto:** efecto y sonido en el punto real de colisión.
5. **Recuperación:** vuelve a control, salvo que habilidad encadene otra.

Cada skill se especificará con: ID, clase/subclase, gesto, duración objetivo, prioridad, cancelación, anclaje de VFX, sonido, icono, condición de impacto, coste/enfriamiento y variantes de primera/tercera persona. No se creará una animación exclusiva de cero si una biblioteca existente sirve con ajustes de timing/gesto.

### 10.1 Arquetipos reutilizables para todas las skills

Cada nodo activo del Skill Tree se asignará a uno o una composición de estos arquetipos. Así, **todas las habilidades tendrán una secuencia completa**, pero no se duplicarán cientos de clips casi idénticos. Las habilidades emblemáticas y definitivas sí tendrán momentos visuales propios.

| Arquetipo | Secuencia | Uso típico |
|---|---|---|
| Golpe instantáneo | postura → impacto → retorno | cortes, puñetazos, estocadas y golpes de arma |
| Combo | preparación corta → golpe A/B/C → cierre | cadenas de Guerrero, Monje, Asesino y Espadachín arcano |
| Carga | bajar centro → cargar → colisión → recuperación | Berserker, Guerrero, Lancero y caballería |
| Salto/embestida | flexión → impulso → trayectoria → aterrizaje | cargas, saltos ofensivos y ataques de área |
| Proyectil | apuntar → cargar → liberar → disipar | flechas, virotes, bolas elementales y cuchillas arrojadas |
| Ráfaga | preparar → secuencia de salidas → pausa | Arquero, Ballestero, Mago y habilidades de repetición |
| Rayo/canal | conectar foco → canalizar → barrer o mantener → cortar | Éter, Rayo, Luz y conjuros sostenidos |
| Cono/onda frontal | abrir postura → emitir frente al personaje → disipar | gritos, aliento, choque y ondas de fuerza |
| Área de suelo | marcar suelo → expansión → pulso → desvanecer | raíces, hielo, fuego, runas y zonas de control |
| Marca/objetivo | señalar objetivo → sello adherido → activar/consumir | Asesino, Clérigo, cazadores y debuffs |
| Barrera/escudo | foco → panel/onda → mantener → romper/desactivar | Paladín, Mago, Clérigo y habilidades defensivas |
| Curación/limpieza | gesto de contacto o canalización → partículas viajan → cierre | Médico mágico, Clérigo y Druida |
| Invocación | círculo → ensamblaje por partes → vínculo → orden inicial | Invocador, Nigromante y Llamabestias |
| Transformación | postura → silueta energética → cambio de rig/forma → estabilizar | Druida y transformaciones de lore |
| Canción/aura | elevar instrumento → frase musical → onda a aliados/propio cuerpo → cierre | Bardo y auras persistentes |
| Trampa/objeto | colocar → activar → armar → disparar/expirar | Arquero explorador, Asesino e Ingeniero |
| Contra/parry | lectura → desvío → ventana → respuesta | Guerrero, Monje, Paladín y Duelista rúnico |
| Definitiva | anticipación exclusiva → secuencia propia → gran impacto → recuperación | una o varias skills emblemáticas por clase |

Para cada habilidad concreta se documentará su combinación de arquetipos y el punto exacto en que el servidor confirma el efecto. Los nodos pasivos no necesitan animación de lanzamiento: tendrán icono, feedback de desbloqueo y, cuando proceda, un efecto visual discreto al activarse.

---

# PARTE V — MAGIA Y EFECTOS ELEMENTALES

## 11. Sistema de efectos para los 12 elementos

Los elementos conservan su afinidad Solar/Lunar y sus reglas estadísticas. La dirección visual diferencia cada elemento por paleta, geometría, movimiento, sonido y forma de impacto.

| Elemento | Paleta | Forma/movimiento | Sonido | Impacto/estado visual |
|---|---|---|---|---|
| Fuego | Ámbar, naranja, rojo | Lenguas ascendentes, chispas y núcleo caliente | Crepitar y golpe seco | Llamarada breve, brasas y calor distorsionado |
| Luz | Marfil, oro pálido | Rayos radiales, símbolos y partículas ordenadas | Campana cristalina | Destello corto, sello protector y brillo suave |
| Rayo | Amarillo eléctrico, blanco | Trazos quebrados, ramificaciones rápidas | Chasquido eléctrico | Flash breve y arcos que desaparecen rápido |
| Tierra | Ocre, verde mineral, gris | Placas, fragmentos y ondas desde el suelo | Roca quebrándose | Polvo, grietas temporales y fragmentos |
| Metal | Acero, cobre, plata | Fragmentos geométricos y líneas de filo | Campanada metálica | Chispas, placas/filos que se ensamblan |
| Éter | Violeta, índigo, cian | Líneas geométricas, nodos y fractales | Resonancia arcana | Símbolo suspendido y distorsión controlada |
| Agua | Azul, turquesa | Cintas fluidas, gotas y remolinos | Flujo y burbujeo | Salpicadura, onda y gotas suspendidas |
| Hielo | Cian pálido, blanco | Cristales angulares y agujas | Crujido cristalino | Escarcha, cristales y vapor frío |
| Aire | Blanco, plata, azul claro | Espirales, curvas y hojas desplazadas | Silbido y ráfaga | Anillo de viento y polvo/hojas |
| Oscuridad | Violeta oscuro, negro, magenta tenue | Humo dirigido, sombra y eclipse | Susurro grave y pulso | Mancha de sombra que se contrae y disipa |
| Naturaleza | Verde, jade, ámbar | Raíces, hojas, brotes y lianas | Madera y hojas | Brote, raíces o polvo de esporas |
| Espíritu | Turquesa espectral, azul profundo | Wisps, partículas ascendentes y ondas concéntricas | Coro suave y eco | Silueta espiritual breve y rastro de alma |

### 11.1 Animaciones comunes a toda magia

- `magic_idle`: foco/artefacto con brillo de reposo.
- `magic_prepare`: manos se separan, arma/catalizador se alinea.
- `magic_channel_short`: conjuro rápido.
- `magic_channel_long`: canalización sostenida.
- `magic_release_projectile`: libera proyectil.
- `magic_release_beam`: inicia rayo continuo.
- `magic_release_area`: círculo/onda desde suelo.
- `magic_release_self`: aura aplicada al propio personaje.
- `magic_impact`: impacto en objetivo.
- `magic_barrier`: barrera aparece desde núcleo a borde.
- `magic_heal`: luz/partículas viajan hacia objetivo.
- `magic_summon`: círculo + partículas + materialización.
- `magic_dispel`: efecto se rompe en fragmentos y desaparece.
- `magic_fail`: el foco se apaga o el sello se deshace si la habilidad falla.

### 11.2 Reglas de VFX

- Los efectos usan anclajes en mano, arma, torso, suelo o posición de impacto.
- Las partículas de un mismo conjuro tienen un máximo y un presupuesto por jugador/área.
- Los efectos de combate duran lo mínimo necesario para informar.
- Las auras persistentes deben ser pequeñas y configurables.
- Jefes pueden usar VFX grandes, pero con telegráfo y opción de reducir efectos.
- El color no es el único indicador: cada elemento tiene silueta, sonido y patrón propio.
- VFX del cliente nunca decide daño, colisión o estado del efecto.

---

# PARTE VI — PROFESIONES Y ANIMACIONES DE TRABAJO

## 12. Biblioteca de trabajo

Las animaciones profesionales deben ser visibles a distancia y reconocibles de cerca. Cada oficio utiliza una biblioteca común más gestos especializados. El trabajo consume tiempo y recursos según las reglas económicas del mod; la animación no produce el objeto por sí sola.

| Profesión/actividad | Animación principal | Animaciones secundarias / props |
|---|---|---|
| Minero | Golpe de pico con anticipación y rebote | Examinar veta, cargar mineral, limpiar polvo |
| Talador | Hachazo alterno con impacto | Marcar árbol, retirar ramas, plantar retoño |
| Cantero | Cincelar piedra y girar pieza | Medir bloque, pulir superficie, apilar |
| Prospector | Examinar suelo con herramienta | Tomar muestra, mirar mapa, marcar hallazgo |
| Cazador | Tensar arco/colocar trampa | Rastrear, desollar, recoger presa |
| Recolector/herbolario | Cortar o recoger planta | Examinar hoja, guardar frasco, clasificar |
| Fundidor | Alimentar horno y remover crisol | Abrir válvula, verter metal, retirar escoria |
| Herrero | Calentar, martillar, girar pieza, templar | Medir hoja, afilar, inspeccionar calidad |
| Armero | Ajustar placas y remaches | Montar coraza, ajustar correas, probar encaje |
| Carpintero | Aserrar, cepillar y ensamblar | Clavar, lijar, comprobar escuadra |
| Artesano de armas a distancia | Curvar arco y montar cuerda | Fabricar flecha, tensar ballesta, probar virote |
| Curtidor | Raspar piel y tratar cuero | Estirar, teñir, coser |
| Sastre | Cortar tela y coser | Medir, bordar, doblar prendas |
| Joyero | Tallar gema y engastar | Pulir, pesar, comprobar engaste |
| Artesano mágico | Inscribir runa y ensamblar artefacto | Cargar núcleo, alinear cristales, estabilizar |
| Alquimista | Triturar, medir y mezclar | Calentar matraz, agitar, filtrar, sellar frasco |
| Constructor | Medir, colocar bloque y consultar plano | Señalar, transportar, revisar proyección |
| Albañil | Colocar mortero y asentar piedra | Nivelar, cincelar, rejuntar |
| Ingeniero | Montar mecanismo y calibrar | Girar engranaje, activar prueba, ajustar palanca |
| Agricultor | Arar, sembrar, regar, cosechar | Atar haz, cargar cesta, inspeccionar cultivo |
| Ganadero/pastor | Alimentar y revisar animal | Cepillar, ordeñar si especie, conducir rebaño |
| Criador de monturas | Cepillar, ensillar y entrenar | Guiar, dar premio, revisar cascos/arnés |
| Apicultor | Revisar colmena y extraer panal | Ahumar, recoger miel, cerrar caja |
| Pescador | Lanzar caña/red y recoger | Reparar red, clasificar captura, limpiar pescado |
| Molinero | Introducir grano y activar molino | Revisar piedras, recoger harina |
| Panadero/cocinero | Amasar y preparar | Meter al horno, girar alimento, emplatar |
| Carnicero/procesador | Preparar corte y empaquetar | Afilar cuchillo, pesar, almacenar |
| Comerciante | Exponer mercancía y negociar | Pesar, contar monedas, entregar bolsa |
| Tasador | Inspeccionar producto | Usar balanza, lupa y libro de registro |
| Banquero | Abrir libro de cuentas y sellar | Recibir depósito, entregar recibo, cerrar caja |
| Maestro de la Moneda | Alimentar prensa y acuñar | Revisar cuño, contar monedas, registrar lote |
| Tesorero/contable | Escribir libro mayor | Contar reservas, firmar autorización |
| Recaudador | Presentar registro y recibir pago | Sellar recibo, guardar bolsa |
| Transportista | Cargar, levantar y asegurar mercancía | Atar bultos, ajustar carreta, descargar |
| Caravanero | Comprobar ruta y organizar salida | Revisar animales, dar orden, montar guardia |
| Mensajero | Sellar carta y entregar paquete | Consultar mapa, montar/salir, tocar aldaba |
| Cartógrafo | Dibujar y marcar mapa | Medir, orientar brújula, señalar ruta |
| Bibliotecario | Colocar y retirar libros | Ordenar, limpiar, consultar catálogo |
| Escriba | Escribir y secar tinta | Sellar documento, enrollar pergamino |
| Maestro | Explicar y señalar pizarra | Corregir, entregar libro, escuchar aprendiz |
| Investigador | Examinar objeto y registrar | Microscopio/lupa estilizada, tomar muestra |
| Investigador arcano/maestro elemental | Trazar runas y probar foco | Activar círculo, medir respuesta, cerrar experimento |
| Médico/veterinario | Examinar y vendar | Preparar instrumental, administrar remedio, registrar caso |
| Diplomático/juez | Saludo formal y lectura de decreto | Firmar, escuchar, entregar sello |
| Guardia | Patrullar y vigilar | Inspeccionar, montar guardia, llamar refuerzo |
| Sacerdote | Rezar y realizar bendición | Encender vela, abrir texto, dirigir ceremonia |

### 12.1 Reglas de variación

- El nivel profesional cambia precisión, ritmo, postura y calidad aparente, no la mecánica real.
- Aprendiz puede cometer pequeños errores visuales seguros (pieza se recoloca, herramienta se le cae de forma controlada); no debe desperdiciar materiales sin regla económica.
- Maestro tiene gestos más fluidos, menos movimientos innecesarios y postura experta.
- Un mismo edificio puede alojar más de una profesión si sus estaciones están presentes.
- No crear un set de animación entero por NPC: se reutilizan rigs y clips por familia de herramienta.

---

# PARTE VII — ANIMALES, MONTURAS Y DRAGONES

## 13. Animaciones universales de criatura

Cada criatura requiere como mínimo: idle, mirar/interés, andar, correr, giro, recibir daño, ataque, comer/interacción, dormir/descansar y muerte cuando corresponda. Las acuáticas y voladoras añaden nado/vuelo y transiciones.

### 13.1 Monturas comunes

| Especie | Identidad visual | Animaciones |
|---|---|---|
| Caballo | Razas, pelaje, crin, marcas, arnés | Idle con orejas, pastar, trotar, galope, salto, freno, encabritarse, montar y desmontar |
| Burro | Orejas largas, cuerpo compacto, alforjas | Paso, trote, rebuzno, sacudir cabeza, carga, descanso |
| Mula | Cuerpo robusto y equipo de carga | Caminar cargada, frenar, girar, carga/descarga |
| Camello | Joroba, cuello largo, silla y carga | Caminar, trotar, arrodillarse para montar/cargar, levantarse, sentarse |

### 13.2 Monturas especiales

| Especie | Animaciones distintivas |
|---|---|
| Lobo gigante | Olfatear, acechar, correr, salto, mordida, proteger jinete |
| Lagarto de monta | Caminar reptiliano, carrera baja, giro de cola, mordida, trepar si diseño lo permite |
| Ave mensajera | Aleteo, despegue, vuelo corto, aterrizaje, picoteo, entregar mensaje |
| Tortuga gigante | Caminar pesado, retraer cabeza, nadar, girar y emerger |
| Grifo | Idle de alas plegadas, desplegar, carrera, salto, batir alas, vuelo, planeo, picado, aterrizaje, rugido |
| Pesadilla | Casco en llamas, trote, galope, encabritarse, bruma de fuego y salto oscuro |
| Fénix | Reposo plegado, desplegar alas, despegar, vuelo, llamarada, regeneración visual y aterrizaje |
| Roc | Aleteo masivo, despegue, planeo, giro amplio, picado y aterrizaje con viento |

Las monturas acuáticas y voladoras no simulan transporte naval comercial. Su uso sigue el sistema de monturas del documento maestro.

## 14. Dragones

Los dragones son criaturas raras con rig y animación de máxima prioridad visual.

### 14.1 Estados completos

- Dormir enrollado o apoyado.
- Respiración lenta con movimiento de pecho, cuello y cola.
- Despertar con apertura de ojos y levantamiento de cabeza.
- Curiosidad: seguir sonido, inclinar cabeza y olfatear.
- Amenaza: extender cuello, desplegar alas y rugir.
- Comer: agarrar presa, masticar y tragar.
- Caminar pesado, correr breve y girar con cola contrapesando.
- Preparar vuelo: agacharse, abrir alas, batir para ganar impulso.
- Despegar, vuelo de crucero, planeo, giro, ascenso, descenso y frenado.
- Aterrizar con amortiguación de patas y alas.
- Transportar jinete: pose de silla/arnés y ajuste de cuello.
- Recibir daño: proteger zona, rugido y pérdida temporal de estabilidad.
- Atacar con mordida, garra, coletazo, embestida y aliento elemental.
- Retirarse herido, descansar, buscar refugio y defender nido.
- Cortejo y reproducción con animaciones discretas de pareja/nido; puesta de huevo sin detalle explícito.
- Eclosión: grietas, vibración, rotura por etapas y primera reacción de cría.

### 14.2 Aliento elemental

Cada dragón usa la gramática de VFX de su elemento, pero con escala, densidad y sonido de dragón. La cabeza/mandíbula se anticipa, el cuello se expande, el núcleo se carga y el chorro sigue la orientación real de la criatura. El servidor decide colisión y daño; el cliente sincroniza partículas con el evento.

### 14.3 Variantes visuales

- Cuernos, crestas, placas, cuernos de mandíbula, espinas, barbas, aletas o penachos según linaje.
- Color base y gradiente de vientre, membrana alar, iris y brillo elemental.
- Variantes de tamaño por edad: cría, joven, adulto y anciano.
- Cicatrices persistentes como cosmética de historial, si se conserva tras guardado.
- Arnés/armadura dracónica con anclajes propios que no atraviesen alas ni placas.

---

# PARTE VIII — ARMAS, ARMADURAS, HERRAMIENTAS Y ASPECTOS

## 15. Sistema visual de armas modulares

Las armas creadas en la forja modular tendrán componentes visuales intercambiables:

- Cabeza/hoja: perfil, punta, filo, muescas y decoración.
- Guarda: forma y ornamentación.
- Empuñadura: material, envoltura y longitud.
- Pomo: peso, silueta y sello del artesano.
- Asta/shaft: madera, metal, refuerzos y bandas.
- Núcleo/incrustación: gema, runa, cristal o material elemental.
- Acabado: bruñido, mate, grabado, desgastado, ceremonial.

Cada combinación debe conservar una silueta coherente y una sombra/colisión razonable. El sistema puede tener cientos de resultados visuales a partir de piezas limitadas, sin modelar cada arma como una entidad independiente.

### 15.1 Familias

Espadas de una mano, espadones, katanas, hachas, martillos de guerra, lanzas, dagas, armas de asta, arcos, ballestas, guanteletes, bastones, catalizadores, focos, grimorios y armas sagradas. Se mantiene la distinción de clase del documento maestro: Guerrero no recibe mazas como arma principal; Paladín sí utiliza mazas y martillos sagrados.

## 16. Armaduras y sets

### 16.1 Pesos

- **Ligera:** cuero, telas reforzadas, placas pequeñas; movimiento libre.
- **Media:** cuero/metal segmentado, gambesón y placas; equilibrio.
- **Pesada:** placas completas, cota y escamas; postura sólida y sonido pesado.
- **Arcana:** telas y piezas ligeras con focos, cristales y runas; movimientos de capa y elementos.

### 16.2 Piezas y variantes

Casco, torso, hombros, brazos, guantes, piernas, botas, capa y accesorios. Los sets raciales y culturales modifican ornamento y corte, pero reutilizan estructuras base. El casco puede tener variantes para cuernos/orejas; capas y mochilas deben ocultarse o adaptarse cuando clippeen.

### 16.3 Siete familias visuales de tier

Los nombres definitivos de materiales vienen del sistema de fabricación; estos tiers ordenan su lectura visual:

| Tier | Aspecto | Señal visual |
|---|---|---|
| I — Básico | Hierro, cuero, madera, cobre simple | Forma funcional, desgaste leve, poca ornamentación |
| II — Refinado | Acero, cuero tratado, aleación fiable | Mejor simetría, remaches y acabados pulidos |
| III — Maestro | Materiales seleccionados y fabricación experta | Grabados, ajuste fino, sello del artesano |
| IV — Raro | Mithril, gema o material excepcional | Silueta singular y detalle de material |
| V — Épico | Adamantita, runas o cristal especial | Ornamentación visible, brillo contenido |
| VI — Legendario | Acero Solar, cristal glacial, madera ancestral, piedra rúnica | Identidad de lore, efectos discretos y piezas únicas |
| VII — Mítico | Materiales irrepetibles de dragón/ruina/entidad antigua | Forma distintiva, animación/sonido exclusivo y registro histórico |

### 16.4 Calidad de fabricación

Fallido, defectuoso, normal, bueno, excelente y maestro/excepcional se expresan mediante acabado, marca de herramienta, simetría, pulido, costura y sello del artesano. Calidad no debe confundirse con tier: un arma común puede ser excepcional; una reliquia puede ser defectuosa si se dañó.

### 16.5 Aspectos cosméticos

Los aspectos cosméticos no alteran estadísticas. Se pueden desbloquear por cultura, facción, profesión, evento, reputación, linaje o logro. No se venderá ni permitirá una skin que oculte rareza, estado o identificación de un objeto cuando esa información importe para PvP o economía.

---

# PARTE IX — CIUDADES, EDIFICIOS Y AMBIENTE

## 17. Animación de ciudad viva

La ciudad debe transmitir actividad, no ser un decorado estático. Sin embargo, el ambiente visual será local y ligero: no se necesita animar a toda la población del mundo en cada tick.

### 17.1 Habitantes

- Caminar por rutas, detenerse, mirar escaparates, conversar, saludar.
- Trabajar en estación correspondiente.
- Transportar cajas, sacos, madera, piedra y herramientas.
- Abrir/cerrar puertas y portones según rutina.
- Comprar, vender, intercambiar y revisar mercancía.
- Comer, beber, sentarse, dormir y visitar lugares públicos.
- Patrullar, vigilar, entrenar, montar guardia y reaccionar a alarma.
- Celebrar festividad, procesión, boda o funeral según evento.
- Reaccionar a fuego, ataque, muerte, alarma, hambre y órdenes públicas.

### 17.2 Edificios y estaciones animadas

| Edificio/objeto | Animaciones/efectos |
|---|---|
| Ayuntamiento | Banderas, puertas, escribas y guardias; mapa administrativo al interactuar |
| Herrería | Fuego, fuelle, martillo, chispas, yunque y vapor de templado |
| Fundición | Horno, respiradero, crisol, metal líquido, válvulas y vertido |
| Carpintería | Sierra, cepillo, virutas y banco de montaje |
| Molino | Aspas, engranajes y flujo de grano/harina |
| Granja | Cultivos por etapa, espantapájaros, trabajadores y herramientas |
| Establo | Animales, puertas, pesebres, cepillado y equipo colgado |
| Mercado | Toldos, mercancía, balanzas, cajas y NPC vendedores |
| Banco | Ventanilla, caja, libros y sello de documentos |
| Casa de Moneda | Prensa, cuños, monedas, hornos y caja fuerte |
| Biblioteca | Libro que se abre, escalera, estantería y escribas |
| Escuela | Pizarra, pupitres, maestro y alumnado |
| Hospital | Camillas, botiquines, médico y movimiento reducido de pacientes |
| Academia mágica | Cristales, círculos de prueba y dispositivos arcanos |
| Templo | Velas, incensario, estandartes, campanas y rito |
| Cuartel | Entrenamiento, armas colgadas, patrulla y relevos |
| Torre de vigilancia | Guardias, señales, campana, humo/banderas de alerta |
| Puertas de muralla | Bisagras, cadenas, contrapesos, rastrillo y portcullis |
| Puente levadizo | Cadenas, poleas y contrapeso sincronizados |
| Almacén/granero | Apertura de portones, carga y descarga |
| Oficina de mensajería | Clasificar cartas, sellar paquetes y salir de ruta |
| Taller de ingeniería | Engranajes, pistones, mecanismos de prueba y chispas |

### 17.3 Construcción por etapas

- Colocar señal de obra y proyección fantasma.
- Delimitar espacio y mostrar requisitos faltantes.
- Entregar materiales y apilarlos en zona de obra.
- Montar andamios/estructura de apoyo.
- Construir por etapas visibles: cimientos, estructura, paredes, techo, interiores y acabado.
- Constructor inspecciona plano, mide, coloca, ajusta y marca fase terminada.
- Sonido y polvo de construcción sincronizados con cada grupo de bloques, no con cada bloque individual.
- Pausa por falta de material, peligro, mal tiempo si aplica, trabajador ausente o presupuesto insuficiente.
- Cancelación/desmontaje con recuperación de materiales según el estado de la obra.

### 17.4 Ambiente urbano

- Banderas reaccionan a viento simulado local.
- Chimeneas generan humo de baja densidad.
- Lámparas se encienden al anochecer y según estado de seguridad.
- Ventanas cálidas, luces interiores y sombras ayudan a dar profundidad.
- Campanas, mercado y sonidos de trabajo aparecen por horarios/actividad.
- Lluvia y nieve afectan visualmente superficies cuando Minecraft lo permita, sin añadir simulación de clima compleja.
- Murallas, torres y puertas comunican nivel y defensa.

## 18. Mazmorras, ruinas y estructuras

### 18.1 Animaciones de entorno

- Puertas antiguas se desbloquean con giro de mecanismo, runas o cadenas.
- Puentes, plataformas, ascensores, engranajes y trampas se mueven con anticipación y sonido.
- Runas se iluminan por secuencia; cristales vibran/rotan; altares reaccionan al objeto correcto.
- Trampas: flechas, placas, cuchillas, gas, columnas, caída de techo y fuego con telegráfo visual/sonoro.
- Cofres y cámaras secretas tienen apertura/descubrimiento distintivos, sin duplicar botín.
- Puertas de jefe muestran sellos y se abren cuando se cumplen condiciones.

### 18.2 Jefes

Cada jefe debe tener:
- idle/respiración y recorrido de arena;
- alerta al detectar objetivo;
- 2–4 ataques reconocibles;
- preparación/telegráfo por ataque;
- impacto/área de peligro;
- reacción de daño y stagger en ventanas válidas;
- cambio de fase con señal audiovisual;
- ataque especial con área y respuesta posible;
- derrota y recompensa con secuencia breve;
- recuperación/reaparición solo si la lógica de mundo lo permite.

No habrá ataques sin señal visual legible, salvo excepciones muy pequeñas y justificadas por diseño. La animación no puede convertir un ataque de jefe en una cinemática que impida responder.

---

# PARTE X — INTERFAZ, SONIDO Y EXPERIENCIA

## 19. Interfaz visual

### 19.1 Dirección de UI

- Marco de pergamino, cuero, madera oscura, metal envejecido o piedra según sistema.
- Texto de alta legibilidad y contraste; tipografía decorativa solo en títulos, nunca en información crítica.
- Iconos 32×32 o 64×64, con silueta central y color por categoría.
- Paletas Solar/Lunar y de elemento coherentes con VFX.
- Iconos de clase, subclase, profesión, raza, afinidad, elemento, estado, reputación y rareza.
- Paneles de gestión con tablas limpias, filtros y resúmenes; no imitar una interfaz de estrategia con texto diminuto.

### 19.2 Animaciones de UI

- Abrir/cerrar panel mediante deslizamiento/fade breve.
- Seleccionar nodo del Skill Tree con brillo y línea que se dibuja por ruta.
- Nodo desbloqueado muestra pulso corto y sonido.
- Barra de habilidad muestra enfriamiento con máscara radial o barra simple.
- Mapa 3D mueve cámara con inercia suave y resalta rutas conocidas.
- Confirmación económica muestra transacción y nuevo saldo tras confirmación del servidor.
- Construcción muestra proyección fantasma, rotación, validación y requisitos.
- Alertas de guerra/amenaza usan prioridad y no parpadean continuamente.

### 19.3 Accesibilidad y rendimiento

- Opción para reducir partículas, destellos, sacudida de cámara y blur.
- Intensidad de VFX baja/media/alta.
- Mostrar colores alternativos/íconos además de color elemental.
- Subtítulos de sonidos importantes.
- Animaciones de UI con opción reducir movimiento.
- No ocultar texto o barras críticas con VFX.

## 20. Sonido y sincronización

Cada familia de animación tendrá categorías de sonido: inicio, movimiento, impacto, material, recuperación y ambiente.

- Arma: silbido/aire, impacto y material golpeado.
- Armadura: placas, cuero y tela según movimiento.
- Magia: preparación, pulso, liberación, impacto y disipación.
- Profesiones: herramienta, material, estación y finalización.
- Criaturas: paso, respiración, vocalización, daño, ataque y reposo.
- Ciudad: voces, herramientas, ruedas, campana, viento, mercados y animales.
- Mazmorra: mecanismos, eco, trampas, sello y jefe.

Evitar reproducir el mismo sonido en cada frame o para cada NPC simultáneamente. Usar variaciones aleatorias controladas y límites por zona.

---

# PARTE XI — PRODUCCIÓN TÉCNICA DE LOS ASSETS

## 21. Herramientas y formatos

### 21.1 Herramientas de producción

- **Blockbench:** modelos cúbicos, UV, rigs, animaciones y exportación.
- **Editor pixel-art:** texturas, paletas, overlays e iconos.
- **Editor de audio:** recorte, normalización y exportación de sonidos.
- **Git/LFS o repositorio equivalente:** versionado de fuentes y binarios grandes.
- **Pruebas en cliente y servidor dedicado:** validar carga, sincronización, render y rendimiento.

### 21.2 Librerías previstas

- **GeckoLib:** candidato principal para animaciones de entidades, armaduras, items y block entities mediante huesos/keyframes, con eventos de sonido/partículas. Su sitio describe soporte para NeoForge, animación ósea e integración con Blockbench. La versión exacta se fijará y probará en el build real de 1.21.1. [GeckoLib](https://www.geckolib.org/) [CurseForge 1.21.1](https://www.curseforge.com/minecraft/mc-mods/geckolib/files/9748637).
- **Player Animation Library:** candidato para animaciones del modelo de jugador, con compatibilidad listada para Minecraft 1.21.1 y NeoForge. Debe probarse con armaduras, skins raciales, primera/tercera persona y otros mods de animación. [Modrinth](https://modrinth.com/mod/player-animation-library?l=neoforge) [Documentación](https://docs.zigythebird.com/).
- **Animaciones vanilla/NeoForge:** preferibles para movimientos simples y entidades sin rig complejo; usar el sistema más ligero que cumpla el requisito. [Documentación de modelos 1.21.1](https://docs.neoforged.net/docs/1.21.1/resources/client/models/).

Las librerías no sustituyen las pruebas de versiones, dependencias, licencias, compatibilidad de modpacks ni fallback. No se fijará una dependencia solo por aparecer en una página; se hará un prototipo técnico con las versiones exactas.

### 21.3 Estructura de archivos objetivo

```text
assets/medieval_factions/
  animations/{player,combat,classes,professions,creatures,mounts,dragons,blocks}/
  geo/{characters,creatures,mounts,dragons,armor,workstations}/
  textures/{entity,player,armor,weapons,tools,blocks,particles,gui,icons}/
  sounds/{combat,magic,professions,creatures,city,dungeon}/
```

### 21.4 Convención de nombres

`mf_[familia]_[asset]_[variante]_[estado]`

Ejemplos:
- `mf_player_warrior_greatsword_attack_heavy_01`
- `mf_magic_solar_fire_projectile_release`
- `mf_profession_blacksmith_hammer_strike_loop`
- `mf_mount_griffin_flight_glide`
- `mf_dragon_lunar_ice_breath_phase_02`
- `mf_city_forge_bellows_active`
- `mf_dungeon_ancient_gate_unlock`

Los IDs se mantienen estables aunque cambie la textura o la animación. No nombrar assets por nombres personales temporales ni por fecha.

### 21.5 Ficha obligatoria por asset

Cada recurso tendrá:
- ID estable y nombre legible;
- categoría y sistema de juego;
- rig/modelo base;
- textura, resolución y UV;
- animaciones y estados;
- anclajes de partículas/sonido;
- condición de activación;
- prioridad y posibilidad de cancelación;
- variante de primera/tercera persona si aplica;
- coste de render/partículas esperado;
- responsable, versión y estado de QA;
- licencia/procedencia de fuentes o referencia.

### 21.6 Control de calidad

- Sin caras UV invertidas, solapamientos no intencionales ni texturas faltantes.
- Sin clipping de armas en manos, armadura en cuerpo, alas en edificios o monturas en jinete.
- Todas las animaciones vuelven correctamente a idle.
- No hay huesos que queden fuera de estado al morir, desmontar o cambiar de dimensión.
- Se prueban framerate bajo, ping alto, servidor dedicado y modpack con dependencias.
- VFX se prueba en calidad baja y alta.
- Cada textura se revisa en tamaño real y a distancia.
- Los assets deben poder sustituirse por resource pack siempre que sea técnicamente posible.

---

# PARTE XII — CATÁLOGO GLOBAL Y PLAN DE PRODUCCIÓN

## 22. Inventario de bibliotecas que deben existir

Este inventario es la lista maestra de cobertura visual. Las variaciones derivadas de materiales, culturas, tintes y piezas modulares se generan por sistema; no se producirán manualmente como assets duplicados.

| Familia | Cobertura mínima de producción |
|---|---|
| Jugador | Rig base, locomoción completa, combate, interacciones, magia, montura, reacciones, muerte y emotes |
| 16 familias raciales | 16 bases visuales; rigs reutilizados; rasgos, paletas, accesorios y al menos una variante cultural inicial por familia |
| NPC | Variantes de edad/rol, conversación, necesidades, trabajo, patrulla, huida, reunión y eventos sociales |
| 15 clases | Postura, ataque principal, acción característica, VFX y skill máxima de clase |
| 44 subclases | Variantes visuales/efectos y animaciones diferenciales; no 44 rigs completos |
| 12 elementos | Paleta, partículas, sonido, proyectil, área, aura, impacto y disipación |
| Profesiones | Un loop principal por estación/actividad y acciones secundarias agrupadas por familia de herramienta |
| Armas | Modelos base y componentes modulares para todas las familias |
| Armaduras | 4 pesos, 7 tiers visuales y capas culturales/raciales |
| Monturas | 8 especies/categorías iniciales con estados completos de suelo/vuelo/agua según corresponda |
| Dragones | Rig completo, edades, vuelos, combate, aliento, vínculo, nido y muerte |
| Criaturas | Rigs por familia, estados de combate, locomoción y variantes de aspecto |
| Ciudades | Estaciones activas, puertas, banderas, luz, humo, trabajadores, mercados y construcción por fases |
| Mazmorras | Puertas, mecanismos, trampas, tesoros, jefe, fases, derrota y activación de sala |
| UI | Iconos de sistemas, pantallas, transiciones, Skill Tree, mapa, gestión, alertas y accesibilidad |

### 22.1 Cobertura de animaciones por clase

Para cada una de las 15 clases, la biblioteca tendrá: idle de combate, caminar/correr con postura, preparar arma/catalizador, ataque ligero, ataque pesado, defensa/esquiva si aplica, habilidad rápida, habilidad de control/defensa, habilidad emblemática, habilidad máxima, reacción y transición de equipo. Las subclases añaden al menos un gesto distintivo, una variante de VFX y una animación de habilidad que las haga reconocibles.

### 22.2 Cobertura por elemento

Cada uno de los 12 elementos dispone de: idle de foco, preparación, proyectil, rayo/chorro cuando aplique, zona/área, aura propia, barrera, impacto, estado aplicado y disipación. Las habilidades que no necesiten todas estas formas reutilizan la variante pertinente; no se obliga a crear un beam de cada elemento si no hay una habilidad que lo use.

### 22.3 Cobertura de profesiones

Cada profesión tendrá al menos una animación loop principal y un gesto de finalización. Los oficios que comparten estación pueden compartir rig/clip base con herramientas diferentes. No se diseñará una animación completa por receta individual.

---

## 23. Orden de producción recomendado

### Fase A — Identidad visual y prototipo de personaje

1. Paleta global, materiales y guía de silueta.
2. Rig humanoide estándar y animaciones locomotoras.
3. Una raza base con una variante cosmética.
4. Un set de ropa civil y un set de armadura ligera/media/pesada.
5. Una espada, escudo, arco y bastón.
6. Animaciones base de combate y reacción.
7. Probar primera y tercera persona, skins, armadura y multijugador.

### Fase B — Combate y magia

1. Las 15 posturas de clase usando rigs y animaciones compartidas.
2. Tres familias de armas cuerpo a cuerpo y dos a distancia.
3. Sistema de eventos de hitbox/proyectil sincronizados.
4. Los 12 sets de VFX elementales con límite de partículas.
5. Una habilidad rápida y una máxima por clase para validar el pipeline.
6. Probar cancelación, bloqueo, parry, ping alto y PvP.

### Fase C — NPC y profesiones

1. Rig NPC/humanoide y locomoción/gestos sociales.
2. Tres estaciones de trabajo representativas: mina, herrería y granja.
3. Biblioteca compartida de herramienta y trabajo.
4. Ropa civil, artesanal y militar.
5. Variaciones raciales y por cultura.

### Fase D — Animales, monturas y dragones

1. Rig cuadrúpedo y caballo completo.
2. Rig volador y grifo.
3. Rig acuático y tortuga gigante.
4. Rig de dragón y estados de cría/adulto.
5. Montar/desmontar, vuelos, combate y equipamiento.

### Fase E — Ciudades, estructuras y mazmorras

1. Herrería/fundición y molino animados.
2. Puertas, puentes, banderas y luces.
3. Construcción por fases y trabajadores.
4. Mazmorra modular, mecanismos y trampas.
5. Jefe con dos fases y ataques telegráfiados.

### Fase F — Variantes y acabado

1. 16 familias raciales completas y sus subrazas definidas.
2. 7 tiers visuales y variantes de material.
3. Variantes culturales, de facción, evento y rango.
4. Resto de habilidades y animaciones de subclases.
5. UI definitiva, sonido, accesibilidad y optimización.

---

## 24. Criterios de aceptación

Un asset se considera terminado solo cuando:

- su apariencia se reconoce a escala normal de Minecraft;
- sus materiales se distinguen sin depender únicamente del color;
- la animación no rompe el modelo ni el equipo;
- su inicio, impacto y recuperación coinciden con la lógica de juego;
- tiene sonidos y efectos colocados donde corresponden;
- no genera partículas ni luces excesivas;
- funciona en cliente y servidor dedicado;
- se comporta correctamente al morir, desmontar, cambiar dimensión, desconectarse y volver;
- su rendimiento ha sido medido con varios personajes/entidades visibles;
- se ha revisado la licencia y procedencia de cualquier referencia externa.

## 25. Decisiones de dirección de arte cerradas

1. Fantasía medieval voxel premium: no hiperrealista.
2. Siluetas reconocibles y legibles primero; detalle después.
3. Skins modulares por raza, cultura, profesión, facción y equipo.
4. Rigs compartidos por familia anatómica; rigs exclusivos solo cuando la anatomía lo exige.
5. Animaciones por capas con prioridades; el servidor conserva autoridad sobre daño y estados.
6. Magia diferenciada por geometría, sonido y paleta de cada elemento.
7. Profesiones y ciudades siempre muestran actividad y función, sin simular visualmente cada entidad lejana.
8. Las armas modulares reutilizan piezas y variantes de material.
9. Las monturas y dragones tienen ciclos completos, no solo locomoción.
10. VFX escalables y opciones de accesibilidad son obligatorios.
11. GeckoLib y Player Animation Library son candidatos técnicos que se fijarán tras prototipos en la versión exacta de NeoForge/Minecraft.
12. Ninguna ilustración conceptual se declarará skin UV, modelo o animación implementada hasta superar pruebas dentro del juego.

---

## Referencias técnicas consultadas

- GeckoLib, biblioteca de animación basada en huesos/keyframes para entidades, armaduras, objetos y bloques: https://www.geckolib.org/ y https://www.curseforge.com/minecraft/mc-mods/geckolib/files/9748637
- Player Animation Library, compatibilidad publicada para Minecraft 1.21.1 y NeoForge: https://modrinth.com/mod/player-animation-library?l=neoforge y https://docs.zigythebird.com/
- Documentación NeoForge 1.21.1 para modelos, texturas y recursos de cliente: https://docs.neoforged.net/docs/1.21.1/resources/client/models/

