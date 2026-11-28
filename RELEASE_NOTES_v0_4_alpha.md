# MEDIEVAL FACTIONS — v0.4.0-alpha

## Sistemas añadidos sobre v0.3
- Árbol de talentos por clase: 60 nodos para las clases de tres ramas y 56 para Bardo (dos ramas). Incluye seis nodos centrales iniciales, progresión de rama, requisitos de nivel, nodos de atributo/pasiva/activa/mejora/espacio, compuerta de subclase y maestría.
- Catálogo de 44 especializaciones (tres para cada clase salvo Bardo, que tiene dos). La subclase tiene que corresponder con la clase, desbloquear su nodo y superar la prueba; no existe cambio normal de subclase.
- La experiencia de clase se concede en servidor cuando una criatura suelta experiencia vanilla y el atacante fue un jugador con perfil. Se aplica rendimiento decreciente para muertes repetidas de la misma especie y un límite de 1.000 XP de combate por día de mundo.
- Biología racial inicial para 16 familias, con cinco etapas de vida, madurez, vejez, esperanza de vida y edad máxima propias de cada linaje.
- Seis recetas iniciales para fundición, panadería, carpintería, herrería y armería.
- Encargos productivos persistentes conectados a ciudadanos y almacenes: profesión requerida, reserva atómica de entradas, un trabajo activo por trabajador, duración en días de mundo, producto al completar, XP profesional y reembolso solo antes del primer día.
- Mantenimiento diario configurable abonado desde la tesorería antes de salarios, y rechazo de salarios que desbordarían el monedero del habitante.
- Requisitos de ascenso de asentamientos ampliados para incluir infraestructura y reservas mínimas de tesorería; comandos administrativos de información, proyecto financiado y ascenso.
- Guardado/recarga de encargos y contadores diarios anti-farmeo.
- Las facciones administrativas ahora también requieren una familia racial principal; las incorporaciones permanentes rechazan habitantes de otra familia.
- Comandos de inspección/desbloqueo de árbol, selección de subclase, listado de recetas, habitantes y encargos, inicio/cancelación de producción.

## Verificación de esta alpha
- 130 aserciones Java puras superadas.
- Prueba de integración con stubs mínimos de SavedData/NBT: perfiles, facciones, almacenes, habitantes, producción, recompensas y encargos sobreviven a save/load; el contador de muertes repetidas también persiste.
- Comprobación de tipos/estructura de adaptadores de comandos y eventos con stubs mínimos de API: superada; esto no sustituye la compilación contra los JAR reales.
- Validador de recursos sin errores ni advertencias.
- No se ha ejecutado Gradle/NeoForge real ni Minecraft cliente/servidor; no se ha generado un JAR compilado.

## Límites
Es una alpha de sistemas base. Los árboles son estructuras de progresión y requisitos, no una interfaz visual ni un catálogo definitivo de efectos de habilidades. Las recetas son un primer conjunto de prueba; faltan estaciones funcionales, trabajo de campo, construcción real y la mayoría de sistemas del Documento Maestro.
