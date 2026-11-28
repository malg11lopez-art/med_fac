# MEDIEVAL FACTIONS v0.7.0-alpha — Notas de versión

## Objetivo

Esta versión es una iteración de integridad y conexión de sistemas sobre v0.6.0-alpha. No se presenta como la versión final del Documento Maestro.

## Cambios principales

### Producción y habitantes
- Los trabajos en curso se abortan con estado `ABORTED` cuando el trabajador deja de existir o estar activo.
- Los ingredientes reservados se reembolsan al almacén de forma atómica; si la suma desborda la capacidad numérica, el reembolso queda pendiente y se reintenta, sin eliminar materiales.
- Los menores reciben directiva de descanso y no se les asignan tareas de adulto.
- La prioridad de supervivencia comprueba comida suficiente para la población activa y disponibilidad de alojamiento, en lugar de limitarse a comprobar que exista una unidad.

### Familias y expediciones
- El ciudadano recién nacido utiliza el mismo UUID que el registro de persona y el linaje.
- Las expediciones validan participantes únicos, residentes activos, adultos, sanos y pertenecientes a la facción/asentamiento de salida.
- No se envían ciudadanos con un trabajo activo.
- Una expedición en retorno ya no vuelve a consumir provisiones; la recompensa solo se completa al ser aceptada por el almacén.

### Caravanas y banca
- La caravana no se marca como llegada hasta que se confirma la transacción bilateral de asentamientos.
- Los préstamos incorporan 30 días de plazo y 30 días de gracia.
- El impago congela la cuenta; el saldo compensado reduce la deuda y se acredita por el mismo importe a la reserva de la sucursal. Ante un fallo de commit o desbordamiento, no se descuenta el saldo.

### Monturas y dragones
- La salud de la entidad se sincroniza con la ficha persistente; la muerte de la entidad marca el compañero como muerto.
- El pasajero debe ser el propietario registrado, y el registro debe permitir montar según salud, resistencia y hambre.
- Grifo, fénix y dragón incorporan vuelo manual tridimensional bajo control del jinete.
- El hambre del dragón se procesa por día simulado, no por cada pulso de mantenimiento de 20 segundos.

## Verificación

- Pruebas de dominio: **210 aserciones** (136 núcleo + 74 sistemas avanzados).
- Round-trip NBT de `MFWorldSavedData` con stubs mínimos: PASS. No es una prueba de `SavedData` real de Minecraft ni cubre todo `MFAdvancedSavedData`.
- Recursos: 106 JSON, 14 rigs, 75 SoundEvents, 75 OGG; 0 errores y 0 advertencias.
- Inspección de sintaxis mediante `javac -proc:none` sin classpath del juego: no aparecieron diagnósticos de sintaxis; sí se produjeron 2.102 diagnósticos por paquetes/tipos externos no disponibles. Esto no cuenta como compilación.

## Limitaciones conocidas

- No hay `gradlew` y Gradle no está instalado en el entorno. No se pudo resolver la distribución ni las dependencias externas.
- No se ejecutaron `gradle build`, JUnit, GameTests, `runClient`, `runServer` ni pruebas multijugador.
- No se ha generado ni verificado un JAR.
- Siguen incompletos los ciudadanos visibles con navegación/horarios/vida social, la GUI gráfica, el constructor integral de ciudades, el sistema completo de habilidades y combate, los mercados autónomos, las caravanas físicas, la guerra táctica, la reproducción y ciclo vital de dragones, y las expediciones físicas de NPC.

**Clasificación:** alpha de integración. No es una versión final certificada para un mundo principal.
