# Mapa de animaciones y sonidos MEDIEVAL FACTIONS v0.2

## Reglas de sincronización
- El estado de locomoción (idle/walk/run/fly/swim) se selecciona mediante velocidad real y movimiento.
- Las acciones (ataque, bloqueo, casteo, trabajo, daño, muerte) interrumpen o tienen prioridad sobre locomoción según el controlador.
- Los keyframes solo sincronizan efectos visuales/sonoros. El servidor sigue validando daño, consumo de maná, gasto de stamina y resultados.
- Sonido de swing/carga durante la animación; impacto de arma, armadura, escudo o criatura después de confirmar colisión.
- Pasos por superficie se disparan por distancia recorrida y bloque bajo el pie, no por un loop fijo.
- Variación de pitch sugerida: ±3–5 %, con cooldown para evitar ráfagas de sonidos simultáneos.

## Archivos y conjuntos
| Archivo | Conjunto | Acciones principales |
|---|---|---|
| `guard.animation.json` | Guardia humanoide | idle, walk, run, attack_light/heavy, block, cast, hurt, death, work_hammer |
| `guard_combat.animation.json` | Combate | combo, thrust, parry, dodge_roll, shield_bash, bow_draw/release, crossbow_reload, staff_strike y acciones de armas/clases |
| `guard_magic.animation.json` | Magia | 12 poses de casteo, una por elemento |
| `guard_professions.animation.json` | Oficios | mine, chop_wood, saw, fish_cast, write, alchemy, cook, build, carry, harvest, craft |
| `horse.animation.json` | Caballo | idle, walk, trot, gallop, rear, jump, swim, eat, fall |
| `dragon.animation.json` | Dragón | idle, fly, takeoff, breath_fire, bite, death, glide, land, ground_walk, roar, sleep |
| `special_entities.animation.json` | Razas/monturas | centaur gait, harpy glide/flap, merfolk swim, draconid wings/breath, wolf run/pounce/howl, griffin fly/dive, phoenix hover/dive/rebirth |
| `medieval_blocks.animation.json` | Bloques | brasas/fuelles de forja, cristal arcano orbital, estandarte ondeante, aspas de molino |

## Familias elementales
- Solar: Fuego, Luz, Rayo, Tierra, Metal, Éter.
- Lunar: Agua, Hielo, Aire, Oscuridad, Naturaleza, Espíritu.
- La animación de casteo es el gesto. El proyectil, área, impacto, aura, barrera y partículas se instancia desde el código de habilidad y su sistema de VFX.

## Eventos de audio
Consultar `assets/medieval_factions/sounds.json` y `audio_manifest.csv`. `MFSounds.java` registra los 75 SoundEvent con nombres coincidentes.
