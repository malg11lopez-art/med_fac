# Arquitectura de simulación e integración — v0.8.0-alpha

## Objetivo

Evitar que los sistemas se conviertan en subsistemas aislados. La autoridad del juego vive en el servidor; el mundo muestra y materializa el resultado. Las reglas puras se mantienen separadas de `net.minecraft.*` para poder probar invariantes sin arrancar Minecraft.

## Flujo común

```text
Reloj del mundo / eventos del servidor
                |
                v
      MFAdvancedSavedData
      (estado persistente autoritativo)
                |
                v
      Motores de dominio puros
  NPC | Ciudad | Táctica | Dragón
 Familia/Medicina | Convoy | Aventura
                |
                v
 Adaptadores de servidor y validación
 inventario | ciudadano | entidad | bloque
                |
                v
 Entidades, bloques y navegación Minecraft
                |
                v
     Cliente: render / animación / UI
```

El cliente no debe decidir posesión, pagos, daño, movimientos económicos, consentimiento ni resultados. Las pantallas enviarán intenciones al servidor y el servidor validará estado y permisos.

## Nueve verticales y contratos de integración

### 1. Habitantes

- Identidad: `citizenId` es la clave de dominio; `ResidentEntityLink` enlaza con `entityId` y dimensión.
- Agenda: `CitizenSchedule` contiene entradas por minuto de día; las necesidades urgentes tienen prioridad sobre la actividad de horario.
- Navegación: se guarda un `CitizenPlan` y la entidad usa `PathNavigation` terrestre hacia su waypoint.
- Límite actual: el A* 3D está probado como función pura pero no alimenta todavía la ruta del `PathNavigation` del mundo.
- Próximo contrato: cada tarea de trabajo debe tener un target de bloque validado, receta, estación, inventario/coste y resultado idempotente.

### 2. Interfaz

- La GUI debe ser cliente-servidor, no una capa que modifique directamente SavedData.
- Inventarios/edificios requieren menú servidor (`AbstractContainerMenu`) y pantalla cliente (`Screen`) con mensajes sincronizados.
- El árbol de talentos y mapa pueden ser pantallas informativas, pero desbloqueos y órdenes siempre deben validarse en servidor.
- Ninguna GUI real se incluye aún en esta rama.

### 3. Ciudad y construcción

- `CityPlan` delimita una parcela y contiene edificios con distrito, blueprint, fase y progreso.
- `placeCityBuilding` comprueba solapamiento y materiales; la reserva se descuenta una sola vez.
- `ConstructionLink` conecta el edificio urbano con la cola física de construcción.
- La obra se completa solo cuando el motor de colocación termina; no se debe activar por el simple hecho de reservar un plano.
- El catálogo físico actual se limita a `small_house` y `watchtower`.

### 4. Habilidades y guerra táctica

- `MFTacticalWarfare` representa unidades y acciones deterministas; valida turno, actor, objetivo y estado terminal.
- `ArmyColumn` representa una columna logística con formación, raciones y moral; `Occupation` modela control territorial.
- Los sistemas abstractos no crean soldados ni controlan navegación de entidades todavía.
- La futura capa física debe mapear una unidad de dominio a una entidad soldado con UUID persistente, sin duplicar la fuente de verdad.

### 5. Caravanas y economía

- Los convoyes reservan stock al vendedor y escrow al comprador antes de iniciar la ruta.
- Los eventos de emboscada alteran carga superviviente; el asentamiento comprador recibe solo la cantidad remanente.
- La liquidación actualiza ambos asentamientos como una sola operación lógica; el estado terminal se guarda después del commit.
- El comando `resume` retoma un convoy emboscado. No hay carro ni escolta representados por entidades.
- Próximo contrato: entidad `CaravanCart` + guardias vinculados al ID de convoy; el inventario visible solo debe ser una representación de la mercancía reservada, no una segunda copia.

### 6. Dragones

- `DragonGenomeRecord` enlaza rasgos con dragón y propietario.
- Dos padres válidos producen `DragonEgg`; incubación requiere calor y cuidado adecuados durante el tiempo de simulación.
- La eclosión coordina consumo del huevo, registro de cría, Companion y genoma heredado.
- El dragón aparece como entidad real; la autonomía de vuelo/caza/defensa todavía no implementa un agente aéreo completo.

### 7. Familia y medicina

- Matrimonio se crea por propuesta y requiere respuesta de la segunda persona; el sistema no debe saltarse el consentimiento.
- Embarazo y linaje usan IDs estables; el ciudadano recién nacido y la persona deben compartir identidad.
- La clínica deriva su médico de un ciudadano activo con profesión DOCTOR y el stock de medicinas del almacén.
- Tratamiento exige paciente del mismo asentamiento/facción, cama, médico, medicina real y consume una poción.
- Simulación diaria empeora casos si el ciudadano no está alimentado o el asentamiento no es seguro.

### 8. Mazmorras y jefes

- `DungeonProject` genera bloques en lotes pequeños y evita sobrescribir bloques no reemplazables.
- `BossEncounter` tiene fases y acciones con telegráficos, pero el motor de jefe no se considera conectado a entidad única hasta que la entidad y el encuentro compartan ID y salud autoritativa.

### 9. Expediciones

- `ExpeditionJourney` conserva roster, ruta, provisiones, progreso y estado.
- La ruta física actual requiere NPC ya materializados/cargados y una única dimensión; los planes de los residentes se actualizan para seguir el siguiente punto.
- Los siguientes pasos deben soportar rutas con varios waypoints, chunks descargados, retorno, NPC muertos/desconectados, rescate y pérdida controlada de provisiones.

## Reglas transversales

- Nunca generar moneda, material, medicina, comida o equipo gratis.
- Nunca cerrar una transacción antes de confirmar el cambio de todas las partes.
- Toda operación de inventario debe evitar duplicados/reintentos y tener idempotencia.
- Una entidad visible debe tener un único vínculo persistente con su identidad de dominio.
- Las simulaciones lejanas deben ser abstractas y limitadas por presupuesto; la población cargada usa entidades reales.
- Un cambio de versión del esquema SavedData debe contar con versión y migración, no confiar en el nombre del bucket.

## Arquitectura oficial de GUI y persistencia

Para la interfaz, la integración recomendada en NeoForge 1.21.1 es separar el menú autoritativo de servidor (`AbstractContainerMenu`) de la pantalla cliente (`Screen`) y sincronizar datos/acciones de forma explícita. Para estado persistente mundial, usar `SavedData` del nivel apropiado y marcarlo dirty al modificarlo. Esta arquitectura aún debe compilarse y probarse con los artefactos fijados antes de considerarla implementada.
