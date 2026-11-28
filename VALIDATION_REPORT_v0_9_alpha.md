# Informe de validación — MEDIEVAL FACTIONS v0.9.0-alpha

**Objetivo:** Minecraft Java 1.21.1 · NeoForge 21.1.267 · Java 21 · GeckoLib 4.9.4  
**Resultado:** las pruebas locales disponibles pasan; no fue posible realizar el build real de NeoForge.

## Resultados reproducidos

| Comprobación | Resultado | Alcance |
|---|---|---|
| `tools/run_domain_tests.sh` | PASS — 271 aserciones | Núcleo 136, sistemas avanzados 74, simulación extendida 61; incluye scheduler por servidor y reserva exclusiva de miembros de expedición |
| `tools/run_saveddata_stub_tests.sh` | PASS | Smoke test NBT con stubs de un subconjunto de SavedData anterior; no prueba el serializador real de `MFAdvancedSavedData` |
| `python tools/validate_assets.py` | PASS — 0 errores, 0 advertencias | 106 JSON, 14 rigs, 75 SoundEvents y 75 OGG |
| `javac` sin classpath de juego | No concluyente | Los paquetes Minecraft/NeoForge/GeckoLib no están disponibles; no se considera compilación ni type-check completo |
| `gradle clean test build` | No ejecutado | No existe `gradle` ni `gradlew`; DNS no resuelve la distribución |
| `runClient` / `runServer` / GameTests | No ejecutado | Requiere build y artefactos externos |

## Correcciones y ampliaciones verificadas

1. Cada instancia de MinecraftServer mantiene su propio contador de ticks y minuto de agenda; las instancias no comparten estado estático.
2. Los miembros no pueden iniciar una segunda expedición mientras exista una ruta física no terminal que los contenga.
3. La carga/guardado avanzado aplica límites de tamaño y número para evitar que registros NBT corruptos o desmesurados consuman recursos sin límite.
4. Guardias adquieren monstruos hostiles y usan combate melee con animación.
5. Dragones sin jinete adquieren monstruos hostiles y atacan con animación de mordida; con jinete se desactiva la persecución melee.
6. El flujo CI adjunta el JAR generado únicamente después de completar `clean test build`.
7. El script Windows ahora ejecuta los mismos tres conjuntos de tests del script Bash.

## Limitaciones que siguen sin probarse

- Las firmas Java de NeoForge/GeckoLib no se pudieron comprobar contra sus JAR exactos.
- No se probó la carga/guardado real de nuevos buckets con el NBT de Minecraft.
- No se verificaron animaciones en cliente ni comportamiento en servidor dedicado.
- No hay medición de rendimiento, multiplayer, GameTests o ciclos completos de mundo.

## Comandos ejecutados

```bash
bash tools/run_domain_tests.sh
bash tools/run_saveddata_stub_tests.sh
python tools/validate_assets.py
```

## Dictamen

v0.9 corrige defectos concretos y añade comportamiento autónomo básico en dos entidades. No es el mod completo ni existe un JAR validado en esta entrega.
