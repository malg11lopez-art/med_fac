# Plan de pruebas dentro de Minecraft — v0.9.0-alpha

Este plan separa las pruebas automatizadas ya acreditadas por CI de las pruebas manuales que todavía requieren una partida real.

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
| SD-01 | Round-trip NBT de todos los buckets `MFAdvancedSavedData` | Un registro representativo por cada uno de los 31 buckets vuelve idéntico bajo su UUID (automatizado en CI #126) |
| SD-02 | Cargar registro NBT con JSON inválido, más de 32.000 caracteres, UUID ausente o UUID duplicado | Se descarta cada entrada dañada y se conserva el registro válido vecino (automatizado en CI #126) |
| AI-01 | Dejar un guardia cerca de un monstruo hostil | Adquiere objetivo, navega, ataca y muestra animación de corte |
| AI-02 | Dejar un dragón sin jinete cerca de monstruos hostiles | Adquiere objetivo y muerde; no ataca jugadores/animales neutrales |
| AI-03 | Montar el dragón durante una persecución | El control del jugador tiene prioridad y el goal melee se detiene |
| MP-01 | Repetir AI-01 y RT-02 en servidor dedicado con dos clientes | Autoridad en servidor, sin acciones duplicadas ni crash |
| PERF-01 | Probar 50, 100 y 250 residentes y 10 rutas simultáneas | Sin picos sostenidos de tick ni navegación fuera de presupuesto |

## Criterio de aprobación

Cada caso debe tener evidencia de logs, resultado en mundo y estado de guardado. Un test de dominio o una comprobación de recursos no sustituye estas pruebas.


## Cobertura automatizada de CI #126

El GameTestServer ejecuta ocho pruebas requeridas. La nueva prueba de ciudadanos usa la ruta diaria real (`MFAdvancedSavedData.advanceToDay`) y comprueba:

- Un carpintero con madera disponible recibe una directiva de continuar trabajo y una orden `cut_planks`.
- El material se reserva en el almacén al iniciar la orden.
- Repetir la planificación diaria no duplica órdenes activas para el mismo trabajador.
- Un segundo carpintero sin madera no inicia producción ni recibe productos gratuitos.

La prueba comprueba el contrato de dominio en un servidor NeoForge real; no demuestra aún que el NPC opere una mesa de trabajo física ni que recolecte materiales por sí mismo.


### SavedData avanzada y recuperación de corrupción (CI #126)

- Se serializa un registro representativo para cada uno de los 31 buckets avanzados mediante el CompoundTag real de Minecraft y Gson; después se vuelve a cargar y se compara cada valor bajo su UUID.
- Se inyecta en el mismo bucket un JSON inválido, un JSON por encima de 32.000 caracteres, una entrada sin UUID y una entrada que duplica una clave existente.
- El cargador descarta las cuatro entradas dañadas y mantiene el registro válido original.
- Esta cobertura prueba corrupción por entrada; todavía falta una prueba específica del límite agregado de 64 MB y pruebas de interrupción del proceso durante guardado.
