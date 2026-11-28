# Notas de versión — MEDIEVAL FACTIONS v0.9.0-alpha

## Enfoque

Esta entrega corrige aislamiento de estado de servidor, conflictos de expediciones y protección de registros persistentes; además conecta combate autónomo básico a guardias y dragones. No declara completa ninguna vertical que no esté verificada dentro de Minecraft.

## Cambios de código

- **Scheduler por servidor:** `MFServerRuntimeScheduler` mantiene ticks e instante de agenda por mundo/servidor. `MFWorldEvents` conserva el estado en un `WeakHashMap<MinecraftServer,...>`, de modo que mundos integrados diferentes no comparten contadores y las claves pueden liberarse al cerrar el servidor.
- **Expediciones exclusivas:** `MFAdventureSystems.membersAvailableForJourney` rechaza miembros duplicados o que ya participan en otro viaje no terminal. `MFAdvancedSavedData.startPhysicalExpedition` aplica la regla antes de reservar la ruta. Los miembros quedan disponibles al completar o fallar.
- **Persistencia defensiva:** `MFAdvancedSavedData` limita registros por bucket, caracteres por JSON y presupuesto total al cargar/guardar; también descarta entradas sin UUID, tipos inesperados y claves duplicadas. Se mantiene el principio de descartar un registro corrupto sin abortar toda la carga.
- **Guardias autónomos:** `MFGuardEntity` usa `MeleeAttackGoal`, `HurtByTargetGoal` y `NearestAttackableTargetGoal` frente a monstruos hostiles; el golpe reproduce la animación guard slash. Se fijan ataque y armadura de la entidad.
- **Defensa básica de dragones:** el dragón sin jinete adquiere monstruos hostiles y usa navegación melee; al estar montado no ejecuta su persecución melee. La mordida utiliza `animation.dragon.bite`; se añaden ataque, armadura y resistencia al retroceso.
- **CI:** al superar build y tests, GitHub Actions publica el JAR como artefacto de workflow.
- **Windows:** `run_domain_tests.bat` ejecuta los tres conjuntos de tests, igual que el script Bash.

## Validación ejecutada en este entorno

- Núcleo: 136 aserciones PASS.
- Sistemas avanzados: 74 aserciones PASS.
- Simulación extendida: 61 aserciones PASS.
- Total: **271 aserciones de dominio PASS**.
- Smoke test NBT con stubs: PASS; solo cubre el subconjunto anterior documentado.
- Recursos: 106 JSON, 14 rigs, 75 SoundEvents y 75 OGG; 0 errores y 0 advertencias.

## Límites conocidos

- No se pudo ejecutar `gradle clean test build`: no hay Gradle ni wrapper instalado y el entorno no resuelve DNS para descargar la distribución.
- La CI compilará en una máquina con dependencias disponibles; el JAR solo se publicará si esa compilación pasa.
- Guardias/dragones tienen defensa/ataque melee básico contra monstruos; no son un sistema de ejército, no tienen órdenes de campaña ni vuelo autónomo.
- Continúan pendientes GUI, vida social autónoma, trabajos físicos, generador de ciudades, soldados visibles, carros/mercados, vuelo dracónico autónomo, ciclo familiar completo y jefes físicos únicos.

## Dictamen

v0.9 mejora estabilidad e introduce dos comportamientos físicos adicionales. Sigue siendo una alpha; la compilación y validación in-game son puertas obligatorias antes de considerarlo terminado.
