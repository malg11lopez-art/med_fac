# Plan de pruebas dentro de Minecraft — v0.9.0-alpha

Estas pruebas deben ejecutarse en cuanto exista un build NeoForge válido. No están marcadas como superadas en esta entrega.

## Preparación

1. Ejecutar `gradle clean test build` con Java 21.
2. Iniciar `runClient` y crear un mundo de pruebas creativo.
3. Crear dos asentamientos y ciudadanos activos; generar un guardia, un residente y una montura dragón.
4. Guardar, cerrar el mundo y volver a abrirlo antes de probar la persistencia.

## Casos de aceptación

| ID | Escenario | Resultado esperado |
|---|---|---|
| RT-01 | Abrir/cerrar dos mundos integrados consecutivos en el mismo cliente | Los ticks y agenda del segundo mundo empiezan en estado independiente |
| RT-02 | Programar un residente y avanzar la hora del mundo | El plan se actualiza una vez por minuto de juego; no se repite en cada tick |
| EX-01 | Enviar a un residente en expedición A y luego intentar enviarlo a B | B se rechaza sin modificar provisiones ni plan de A |
| EX-02 | Completar o fallar A e intentar un nuevo viaje | El residente vuelve a ser elegible |
| SD-01 | Guardar y recargar schedules, planes, genomas, huevos, clínicas, convoyes y rutas | Datos conservados, sin duplicados ni pérdida de claves |
| SD-02 | Cargar registro NBT con JSON inválido, tamaño excesivo, UUID ausente o clave repetida | Se descarta solo el registro inválido; el mundo sigue cargando |
| AI-01 | Dejar un guardia cerca de un monstruo hostil | Adquiere objetivo, navega, ataca y muestra animación de corte |
| AI-02 | Dejar un dragón sin jinete cerca de monstruos hostiles | Adquiere objetivo y muerde; no ataca jugadores/animales neutrales |
| AI-03 | Montar el dragón durante una persecución | El control del jugador tiene prioridad y el goal melee se detiene |
| MP-01 | Repetir AI-01 y RT-02 en servidor dedicado con dos clientes | Autoridad en servidor, sin acciones duplicadas ni crash |
| PERF-01 | Probar 50, 100 y 250 residentes y 10 rutas simultáneas | Sin picos sostenidos de tick ni navegación fuera de presupuesto |

## Criterio de aprobación

Cada caso debe tener evidencia de logs, resultado en mundo y estado de guardado. Un test de dominio o una comprobación de recursos no sustituye estas pruebas.


## Cobertura automatizada de CI #90

El GameTestServer ejecuta cinco pruebas requeridas. La nueva prueba de ciudadanos usa la ruta diaria real (`MFAdvancedSavedData.advanceToDay`) y comprueba:

- Un carpintero con madera disponible recibe una directiva de continuar trabajo y una orden `cut_planks`.
- El material se reserva en el almacén al iniciar la orden.
- Repetir la planificación diaria no duplica órdenes activas para el mismo trabajador.
- Un segundo carpintero sin madera no inicia producción ni recibe productos gratuitos.

La prueba comprueba el contrato de dominio en un servidor NeoForge real; no demuestra aún que el NPC opere una mesa de trabajo física ni que recolecte materiales por sí mismo.
