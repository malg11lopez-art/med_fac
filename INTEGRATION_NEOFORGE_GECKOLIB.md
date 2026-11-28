# Guía de integración Java — NeoForge 1.21.1

## Registro de sonidos
El archivo `integration/java/com/medievalfactions/registry/MFSounds.java` usa `DeferredRegister`, `BuiltInRegistries.SOUND_EVENT` y `SoundEvent.createVariableRangeEvent`. Llama `MFSounds.register(modBus);` desde el constructor del mod.

Ejemplo de reproducción desde lógica del servidor:
```java
level.playSound(null, pos, MFSounds.COMBAT_SWORD_SWING_LIGHT.get(),
    SoundSource.PLAYERS, 0.8F, 0.98F);
```

No reproduzcas el impacto de hoja desde un keyframe de swing: ejecuta el evento de impacto solo al confirmar una colisión real. La documentación NeoForge explica `SoundEvent`, `sounds.json`, la reproducción y la exigencia de mono para atenuación por distancia.

## GeckoLib
Para cada entidad:
- crea su clase `GeoEntity`/`GeoAnimatable`;
- crea un `GeoModel` que apunte a `geo/entities/<id>.geo.json`, `textures/entity/<id>.png` y `animations/entities/<id>.animation.json`;
- registra un `AnimationController` por capa lógica (locomoción y acción pueden estar separados si el rig lo permite);
- conecta el handler de sonido para que se procesen keyframes.

Ejemplo conceptual de listener:
```java
new AnimationController<>(this, "main", 5, state -> {
    // Seleccionar RawAnimation según estado sincronizado del mob.
    return PlayState.CONTINUE;
}).setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>());
```

Los nombres concretos de clase/import pueden variar con la API del artefacto GeckoLib instalado; compila contra la versión fijada antes de copiar el ejemplo literalmente.

## Player animations
Las animaciones de `guard_*.animation.json` son para rigs GeckoLib humanoides/NPC. No modificarán el jugador vanilla automáticamente. Para el jugador hace falta una integración específica de animación del modelo del jugador, sincronización multiplayer y pruebas de capas/armadura/primera persona.
