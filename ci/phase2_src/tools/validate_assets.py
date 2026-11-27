#!/usr/bin/env python3
"""Static checks for MEDIEVAL FACTIONS resources. Does not replace an in-game test."""
from pathlib import Path
import json, re, sys, wave, subprocess
from PIL import Image
ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/medieval_factions"
errors=[]; warnings=[]
def read(p):
    try: return json.loads(p.read_text(encoding="utf-8"))
    except Exception as e: errors.append(f"JSON inválido {p.relative_to(ROOT)}: {e}"); return {}
# all JSON resources parse
json_files=list(ASSETS.rglob("*.json")) + list((ROOT/"src/main/resources/data").rglob("*.json"))
for p in json_files: read(p)
# geo bones and animation references
geos={}
for p in (ASSETS/"geo").rglob("*.geo.json"):
    d=read(p); names=set()
    for geometry in d.get("minecraft:geometry",[]):
        names.update(b.get("name") for b in geometry.get("bones",[]) if b.get("name"))
    geos[p.name.removesuffix(".geo.json")]=names
mapping={"guard":"guard","guard_combat":"guard","guard_magic":"guard","guard_professions":"guard","horse":"horse","dragon":"dragon"}
special={"centaur":"centaur","harpy":"harpy","merfolk":"merfolk","draconid":"draconid","wolf":"wolf","griffin":"griffin","phoenix":"phoenix"}
for p in (ASSETS/"animations/entities").glob("*.animation.json"):
    d=read(p)
    if p.stem in mapping:
        target=mapping[p.stem]
        for aname, anim in d.get("animations",{}).items():
            missing=set(anim.get("bones",{}))-geos.get(target,set())
            if missing: errors.append(f"{aname}: huesos inexistentes en {target}: {sorted(missing)}")
    if p.stem == "special_entities":
        for aname, anim in d.get("animations",{}).items():
            group=aname.split('.')[1]
            target=special.get(group)
            if target:
                missing=set(anim.get("bones",{}))-geos.get(target,set())
                if missing: errors.append(f"{aname}: huesos inexistentes en {target}: {sorted(missing)}")
# block animation bone check
block_map={'forge':'forge','arcane_workbench':'arcane_workbench','royal_banner':'royal_banner','windmill':'windmill'}
for p in (ASSETS/"animations/blocks").glob("*.animation.json"):
    d=read(p)
    for aname, anim in d.get("animations",{}).items():
        target=next((k for k in block_map if aname.startswith('animation.'+k+'.')),None)
        if target:
            missing=set(anim.get('bones',{}))-geos.get(target,set())
            if missing: errors.append(f"{aname}: huesos inexistentes en {target}: {sorted(missing)}")
# sound file refs and keyframe refs
sounds=read(ASSETS/"sounds.json")
for event, definition in sounds.items():
    for item in definition.get('sounds',[]):
        name=item if isinstance(item,str) else item.get('name','')
        ns, path=(name.split(':',1) if ':' in name else ('minecraft',name))
        if ns=='medieval_factions' and not (ASSETS/'sounds'/f'{path}.ogg').exists():
            errors.append(f"Sonido {event}: falta {path}.ogg")
for p in (ASSETS/"animations").rglob("*.animation.json"):
    d=read(p)
    for aname, anim in d.get('animations',{}).items():
        for marker in anim.get('sound_effects',{}).values():
            sid=marker.get('effect','').split('|')[0]
            ns, path=(sid.split(':',1) if ':' in sid else ('minecraft',sid))
            if ns=='medieval_factions' and path not in sounds:
                errors.append(f"Keyframe {aname}: SoundEvent inexistente {sid}")
# Minecraft 1.21+ singular data folder checks.
for required_tag in [ROOT/"src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json", ROOT/"src/main/resources/data/minecraft/tags/block/needs_iron_tool.json"]:
    if not required_tag.exists():
        errors.append(f"Falta tag 1.21.x: {required_tag.relative_to(ROOT)}")
for recipe in (ROOT/"src/main/resources/data/medieval_factions/recipe").glob("*.json"):
    read(recipe)

# Java renderer resource references and sound registration parity.
java_root = ROOT / "src/main/java"
for source in java_root.rglob("*.java"):
    text = source.read_text(encoding="utf-8")
    # Model classes use namespace-relative paths in ResourceLocation.fromNamespaceAndPath.
    if source.name.endswith("Model.java"):
        for rel in re.findall(r'ResourceLocation\.fromNamespaceAndPath\(MedievalFactions\.MOD_ID, "([^"]+)"\)', text):
            if rel.startswith("textures/"):
                target = ASSETS / (rel if Path(rel).suffix else rel + ".png")
            else:
                target = ASSETS / rel
            if not target.exists():
                errors.append(f"Renderer {source.name}: falta recurso {rel}")
registry = java_root / "com/medievalfactions/registry/MFSounds.java"
if registry.exists():
    registered = set(re.findall(r'SOUND_EVENTS\.register\("([a-z0-9_]+)"', registry.read_text(encoding="utf-8")))
    for event in sounds:
        if event not in registered:
            errors.append(f"SoundEvent sin DeferredRegister: {event}")
    for event in registered:
        if event not in sounds:
            errors.append(f"Deferred SoundEvent sin sounds.json: {event}")

# textures decode
for p in ASSETS.rglob('*.png'):
    try:
        with Image.open(p) as im:
            im.verify()
    except Exception as e: errors.append(f"PNG corrupto {p.relative_to(ROOT)}: {e}")
# model texture references
for p in (ASSETS/'models').rglob('*.json'):
    d=read(p)
    for val in d.get('textures',{}).values():
        if ':' not in val: continue
        ns,path=val.split(':',1)
        if ns=='medieval_factions' and not (ASSETS/'textures'/f'{path}.png').exists():
            errors.append(f"Modelo {p.name}: falta textura {val}")

# Item/block model parent checks and all locale keys.
for p in (ASSETS/"models").rglob("*.json"):
    d=read(p)
    parent=d.get("parent", "")
    if parent.startswith("medieval_factions:"):
        rel=parent.split(":",1)[1]
        candidates=[ASSETS/"models"/(rel+".json")]
        if not any(c.exists() for c in candidates):
            warnings.append(f"Parent custom no encontrado (puede ser modelo vanilla): {p.relative_to(ROOT)} -> {parent}")
# Check texture paths in geo models.
for p in (ASSETS/"geo").rglob("*.geo.json"):
    d=read(p)
    for geometry in d.get("minecraft:geometry", []):
        desc=geometry.get("description", {})
        tw, th=desc.get("texture_width"), desc.get("texture_height")
        if not isinstance(tw, int) or not isinstance(th, int):
            errors.append(f"Geo sin dimensiones de textura válidas: {p.relative_to(ROOT)}")

# Required translation keys for every registered item and block model.
try:
    es = read(ASSETS/"lang/es_es.json")
    for model in (ASSETS/"models/item").glob("*.json"):
        item = model.stem
        if re.search(r"_pulling_[0-2]$", item):
            continue  # Bow draw-state models are variants of hunter_bow, not independent items.
        if f"item.medieval_factions.{item}" not in es and f"block.medieval_factions.{item}" not in es:
            errors.append(f"Falta traducción es_es para modelo de objeto: {item}")
    for state in (ASSETS/"blockstates").glob("*.json"):
        block = state.stem
        if f"block.medieval_factions.{block}" not in es:
            errors.append(f"Falta traducción es_es para bloque: {block}")
except Exception as ex:
    errors.append(f"No se pudieron validar traducciones: {ex}")
# Animation names hard-coded in Java controllers must exist in a GeckoLib animation file.
known_animations=set()
for p in (ASSETS/"animations").rglob("*.animation.json"):
    d=read(p)
    known_animations.update(d.get("animations",{}))
for source in (ROOT/"src/main/java").rglob("*.java"):
    code=source.read_text(encoding="utf-8")
    for clip in set(re.findall(r'"(animation\.[a-z0-9_.]+)"', code)):
        if clip not in known_animations:
            errors.append(f"Controlador Java {source.name}: animación inexistente {clip}")

# FULL_AUDIO_META_VALIDATION_V1
# Full OGG metadata validation: container/codec, mono positional audio, sample rate and duration.
try:
    subprocess.run(['ffprobe','-version'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,check=True)
    ogg_files = list((ASSETS/'sounds').rglob('*.ogg'))
    for sound_path in ogg_files:
        q=subprocess.run(
            ['ffprobe','-v','error','-show_entries','stream=codec_name,channels,sample_rate:format=duration','-of','json',str(sound_path)],
            capture_output=True,text=True)
        if q.returncode:
            errors.append(f"OGG ilegible: {sound_path.relative_to(ROOT)}")
            continue
        try:
            metadata=json.loads(q.stdout)
            streams=metadata.get('streams',[])
            duration=float(metadata.get('format',{}).get('duration',0))
            if not streams:
                errors.append(f"OGG sin stream de audio: {sound_path.relative_to(ROOT)}")
                continue
            stream=streams[0]
            if stream.get('codec_name') != 'vorbis':
                errors.append(f"Codec distinto de Vorbis: {sound_path.relative_to(ROOT)} ({stream.get('codec_name')})")
            if stream.get('channels') != 1:
                errors.append(f"Audio posicional debe ser mono: {sound_path.relative_to(ROOT)} ({stream.get('channels')} canales)")
            if stream.get('sample_rate') not in ('44100','48000'):
                errors.append(f"Frecuencia OGG no admitida: {sound_path.relative_to(ROOT)} ({stream.get('sample_rate')} Hz)")
            if not (0.05 <= duration <= 120.0):
                errors.append(f"Duración OGG fuera de rango (0.05–120 s): {sound_path.relative_to(ROOT)} ({duration:.3f} s)")
        except (ValueError, TypeError, KeyError) as exc:
            errors.append(f"Metadatos OGG incompletos: {sound_path.relative_to(ROOT)} ({exc})")
except (FileNotFoundError, subprocess.CalledProcessError):
    errors.append('ffprobe no instalado: no se puede certificar codec, canales ni duración de los OGG')

# Full registration/model parity: catch objects or blocks that have a texture/model but no runtime registration.
registry_dir = java_root / "com/medievalfactions/registry"
items_source = registry_dir / "MFItems.java"
if items_source.exists():
    item_code = items_source.read_text(encoding="utf-8")
    registered_items = set(re.findall(r'REGISTRY\.put\("([a-z0-9_]+)"', item_code))
    registered_items.update(re.findall(r'\bsword\("([a-z0-9_]+)"', item_code))
    registered_items.update(re.findall(r'registerBlockItem\("([a-z0-9_]+)"', item_code))
    materials_match = re.search(r'String\[\]\s+materials\s*=\s*\{([^}]*)\}', item_code, re.S)
    if materials_match:
        registered_items.update(re.findall(r'"([a-z0-9_]+)"', materials_match.group(1)))
    item_models = {f.stem for f in (ASSETS / "models/item").glob("*.json")
                   if not re.search(r'_pulling_[0-2]$', f.stem)}
    for item in sorted(registered_items - item_models):
        errors.append(f"Objeto registrado sin modelo de inventario: {item}")
    for item in sorted(item_models - registered_items):
        errors.append(f"Modelo de objeto huérfano sin registro (no es estado de arco): {item}")
    if "crossbow_bolt" in registered_items:
        arrow_tag = ROOT / "src/main/resources/data/minecraft/tags/item/arrows.json"
        if not arrow_tag.exists():
            errors.append("crossbow_bolt no está incluido en el tag minecraft:arrows")
        else:
            tag_data = read(arrow_tag)
            if "medieval_factions:crossbow_bolt" not in tag_data.get("values", []):
                errors.append("minecraft:arrows no contiene medieval_factions:crossbow_bolt")
    bow_model = read(ASSETS / "models/item/hunter_bow.json")
    for override in bow_model.get("overrides", []):
        model_ref = override.get("model", "")
        if model_ref.startswith("medieval_factions:"):
            model_path = ASSETS / "models" / (model_ref.split(":", 1)[1] + ".json")
            if not model_path.exists():
                errors.append(f"Estado de arco sin modelo: {model_ref}")

blocks_source = registry_dir / "MFBlocks.java"
if blocks_source.exists():
    registered_blocks = set(re.findall(r'BLOCKS\.register\("([a-z0-9_]+)"', blocks_source.read_text(encoding="utf-8")))
    blockstates = {f.stem for f in (ASSETS / "blockstates").glob("*.json")}
    for block in sorted(registered_blocks - blockstates):
        errors.append(f"Bloque registrado sin blockstate: {block}")
    for block in sorted(blockstates - registered_blocks):
        errors.append(f"Blockstate huérfano sin DeferredBlock: {block}")

# Every registered living entity must have a renderer; every renderer's model/texture/animation must exist.
entities_source = registry_dir / "MFEntities.java"
client_events = java_root / "com/medievalfactions/client/MFClientEvents.java"
if entities_source.exists() and client_events.exists():
    entity_code = entities_source.read_text(encoding="utf-8")
    renderer_code = client_events.read_text(encoding="utf-8")
    registered_entity_fields = set(re.findall(r'DeferredHolder<[^;]+?>\s+(\w+)\s*=\s*ENTITY_TYPES\.register\(', entity_code))
    renderer_fields = set(re.findall(r'event\.registerEntityRenderer\(MFEntities\.(\w+)\.get\(\)', renderer_code))
    for name in sorted(registered_entity_fields - renderer_fields):
        errors.append(f"Entidad registrada sin renderer cliente: {name}")
    for name in sorted(renderer_fields - registered_entity_fields):
        errors.append(f"Renderer apunta a entidad sin registrar: {name}")

# Skins are preserved as vanilla-compatible player skin sheets. Validate manifest entries and exact dimensions.
import csv
skin_manifest = ROOT / "skins_manifest.csv"
if skin_manifest.exists():
    try:
        with skin_manifest.open(encoding="utf-8", newline="") as handle:
            rows = list(csv.DictReader(handle))
        seen_skins = set()
        for row in rows:
            rel = row.get("texture", "")
            if not rel or rel in seen_skins:
                errors.append(f"Entrada de skin vacía o duplicada en skins_manifest.csv: {rel}")
                continue
            seen_skins.add(rel)
            image_path = ASSETS / rel
            if not image_path.exists():
                errors.append(f"Skin declarada pero ausente: {rel}")
                continue
            try:
                with Image.open(image_path) as image:
                    if image.size != (64, 64):
                        errors.append(f"Skin no compatible con formato 64x64: {rel} ({image.size})")
                    if image.mode != "RGBA":
                        errors.append(f"Skin no está en RGBA: {rel} ({image.mode})")
            except Exception as exc:
                errors.append(f"Skin ilegible {rel}: {exc}")
        actual_skins = {p.relative_to(ASSETS).as_posix() for p in (ASSETS / "textures/player").glob("*.png")}
        for rel in sorted(actual_skins - seen_skins):
            errors.append(f"Skin PNG no inventariada: {rel}")
        for rel in sorted(seen_skins - actual_skins):
            errors.append(f"Skin inventariada no localizada: {rel}")
        skin_client = java_root / "com/medievalfactions/client/MFPlayerSkinClient.java"
        skin_layer = java_root / "com/medievalfactions/client/MFPlayerSkinLayer.java"
        skin_input = java_root / "com/medievalfactions/client/MFPlayerSkinInput.java"
        client_events_file = java_root / "com/medievalfactions/client/MFClientEvents.java"
        required_client_files = [skin_client, skin_layer, skin_input, client_events_file]
        for required_file in required_client_files:
            if not required_file.exists():
                errors.append(f"Falta integración runtime de skins: {required_file.relative_to(ROOT)}")
        if all(required_file.exists() for required_file in required_client_files):
            client_skin_code = skin_client.read_text(encoding="utf-8")
            layer_code = skin_layer.read_text(encoding="utf-8")
            input_code = skin_input.read_text(encoding="utf-8")
            client_events_code = client_events_file.read_text(encoding="utf-8")
            for rel in sorted(seen_skins):
                skin_name = Path(rel).stem
                if skin_name not in client_skin_code:
                    errors.append(f"Skin inventariada no está incluida en el selector runtime: {skin_name}")
            if not re.search(r"renderer\.addLayer\(new MFPlayerSkinLayer\(renderer\)\)", client_events_code):
                errors.append("La capa de skins no está registrada en EntityRenderersEvent.AddLayers")
            if "RegisterKeyMappingsEvent" not in client_events_code or "event.register(MFPlayerSkinInput.CYCLE_SKIN)" not in client_events_code:
                errors.append("El selector de skins no registra su tecla en los controles de Minecraft")
            if "ClientTickEvent.Post" not in input_code or "consumeClick()" not in input_code:
                errors.append("La tecla del selector de skins no se procesa en ClientTickEvent.Post")
            if "textures/player/" not in client_skin_code or "skinFor(player)" not in layer_code:
                errors.append("El renderer no utiliza la textura local/remota elegida en el selector de skins")
            # MULTIPLAYER_SKIN_SYNC_VALIDATION_V1
            if "REMOTE_SKINS" not in client_skin_code or "PacketDistributor.sendToServer" not in client_skin_code:
                errors.append("El cliente no mantiene ni envía la selección de skin al servidor")
            rules_file = java_root / "com/medievalfactions/network/MFPlayerSkinRules.java"
            server_network_file = java_root / "com/medievalfactions/network/MFPlayerSkinServerNetwork.java"
            server_events_file = java_root / "com/medievalfactions/network/MFPlayerSkinServerEvents.java"
            if not all(path.is_file() for path in (rules_file, server_network_file, server_events_file)):
                errors.append("Falta la integración de sincronización/persistencia de skins multijugador")
            else:
                rules_code = rules_file.read_text(encoding="utf-8")
                server_network_code = server_network_file.read_text(encoding="utf-8")
                server_events_code = server_events_file.read_text(encoding="utf-8")
                if "SKIN_COUNT = 86" not in rules_code or "isValidIndex" not in server_network_code:
                    errors.append("El servidor no valida los límites del catálogo de 86 skins")
                if "playToServer" not in server_network_code or "context.enqueueWork" not in server_network_code:
                    errors.append("La selección de skin no se procesa de forma segura en el hilo del servidor")
                if "PacketDistributor.sendToAllPlayers" not in server_network_code:
                    errors.append("La selección de skin no se difunde a los demás clientes")
                if "playToClient" not in client_events_code or "context.enqueueWork" not in client_events_code:
                    errors.append("La sincronización de skin no se aplica en el hilo de cliente")
                if "PlayerLoggedInEvent" not in server_events_code or "PlayerLoggedOutEvent" not in server_events_code:
                    errors.append("El caché de skins remotas no se sincroniza al entrar/salir de jugadores")
                if "PlayerRespawnEvent" not in server_events_code or "PlayerEvent.Clone" not in server_events_code:
                    errors.append("La skin no se conserva al morir/respawnear el jugador")
            if "medieval_factions_skin.txt" not in client_skin_code:
                errors.append("La elección de skin no se guarda entre sesiones")
            if rows and len(rows) != len(seen_skins):
                errors.append("El inventario de skins contiene entradas inconsistentes")
            elif len(rows) != 86:
                warnings.append(f"Se esperaban 86 skins de jugador, hay {len(rows)}")
    except Exception as exc:
        errors.append(f"No se pudo leer skins_manifest.csv: {exc}")
else:
    errors.append("Falta skins_manifest.csv")

# Validate the animation library actually loaded by each GeoModel, not only the existence of loose JSON clips.
model_to_entity = {
    "MFGuardModel.java": "MFGuardEntity.java", "MFResidentModel.java": "MFResidentEntity.java",
    "MFDragonModel.java": "MFDragonEntity.java", "MFHorseModel.java": "MFHorseEntity.java",
    "MFCentaurModel.java": "MFCentaurEntity.java", "MFHarpyModel.java": "MFHarpyEntity.java",
    "MFMerfolkModel.java": "MFMerfolkEntity.java", "MFDraconidModel.java": "MFDraconidEntity.java",
    "MFWolfModel.java": "MFWolfEntity.java", "MFGriffinModel.java": "MFGriffinEntity.java",
    "MFPhoenixModel.java": "MFPhoenixEntity.java"
}
for model_name, entity_name in model_to_entity.items():
    model_file = java_root / "com/medievalfactions/entity/model" / model_name
    entity_file = java_root / "com/medievalfactions/entity" / entity_name
    if not model_file.exists() or not entity_file.exists():
        errors.append(f"Falta pareja entidad/modelo: {entity_name} / {model_name}")
        continue
    model_code = model_file.read_text(encoding="utf-8")
    match = re.search(r'ResourceLocation\.fromNamespaceAndPath\(MedievalFactions\.MOD_ID,\s*"(animations/[^\"]+\.animation\.json)"\)', model_code)
    if not match:
        errors.append(f"Modelo sin ruta de animación declarada: {model_name}")
        continue
    animation_path = ASSETS / match.group(1)
    if not animation_path.exists():
        errors.append(f"Modelo {model_name} carga una biblioteca inexistente: {match.group(1)}")
        continue
    loaded_clips = set(read(animation_path).get("animations", {}))
    entity_code = entity_file.read_text(encoding="utf-8")
    for clip in set(re.findall(r'"(animation\.[a-z0-9_.]+)"', entity_code)):
        if clip not in loaded_clips:
            errors.append(f"Entidad {entity_name} intenta reproducir {clip}, ausente de la biblioteca cargada por {model_name}")

# Guard and resident animation fragments must be merged into the exact runtime resource used by both models.
guard_full = read(ASSETS / "animations/entities/guard_full.animation.json")
guard_merged = guard_full.get("animations", {})
for fragment in ["guard.animation.json", "guard_combat.animation.json", "guard_magic.animation.json", "guard_professions.animation.json"]:
    fragment_data = read(ASSETS / "animations/entities" / fragment).get("animations", {})
    for clip in fragment_data:
        if clip not in guard_merged:
            errors.append(f"Clip guard no está integrado en guard_full.animation.json: {clip}")
for clip in ["animation.guard.resident_sleep", "animation.guard.resident_eat", "animation.guard.resident_socialize", "animation.guard.resident_train"]:
    if clip not in guard_merged:
        errors.append(f"Animación civil ausente: {clip}")

# Report SoundEvent coverage: keyframe sounds are auto-played by GeckoLib's handler;
# selected gameplay effects are emitted directly by server-side game actions.
keyframe_sound_ids = set()
for animation_file in (ASSETS / "animations").rglob("*.animation.json"):
    animation_data = read(animation_file)
    for animation in animation_data.get("animations", {}).values():
        for marker in animation.get("sound_effects", {}).values():
            sound_id = marker.get("effect", "").split("|", 1)[0]
            if ":" in sound_id:
                namespace, path = sound_id.split(":", 1)
                if namespace == "medieval_factions": keyframe_sound_ids.add(path)
            elif sound_id:
                keyframe_sound_ids.add(sound_id)
# EXCLUDE_PREVIEW_AND_GAMETEST_SOUND_REFERENCES_V1
# A SoundEvent listed in the operator audition catalogue or referenced only by
# a GameTest is not a natural gameplay trigger. Exclude those files from coverage.
java_sound_ids = set()
for java_file in java_root.rglob("*.java"):
    relative_parts = java_file.relative_to(java_root).parts
    if java_file.name == "MFSoundPreview.java" or "gametest" in relative_parts:
        continue
    java_code = java_file.read_text(encoding="utf-8")
    java_sound_ids.update(name.lower() for name in re.findall(r"MFSounds\.([A-Z0-9_]+)", java_code) if name != "SOUND_EVENTS")
connected_sound_ids = keyframe_sound_ids | java_sound_ids
missing_runtime_sounds = connected_sound_ids - set(sounds)
if missing_runtime_sounds:
    errors.append(f"Acciones referencian SoundEvents no registrados: {sorted(missing_runtime_sounds)}")
unused_sound_ids = set(sounds) - connected_sound_ids

print(f"Gameplay sound-trigger coverage: {len(connected_sound_ids & set(sounds))}/{len(sounds)} events referenced by animation keyframes or runtime gameplay code")
print(f"Registered sounds without a natural gameplay trigger: {len(unused_sound_ids)}")
if unused_sound_ids:
    print("Preview-only / untriggered SoundEvents: " + ", ".join(sorted(unused_sound_ids)))
# RESIDENT_INDEXED_LOOKUP_VALIDATION_V1
resident_file = java_root / "com/medievalfactions/entity/MFResidentEntity.java"
world_data_file = java_root / "com/medievalfactions/data/MFWorldSavedData.java"
if resident_file.is_file() and world_data_file.is_file():
    resident_code = resident_file.read_text(encoding="utf-8")
    world_data_code = world_data_file.read_text(encoding="utf-8")
    if "worldData.citizen(citizenId).orElse(null)" not in resident_code:
        errors.append("Los residentes deben recuperar su ciudadano mediante búsqueda indexada")
    if "Optional<MFCitizenState> citizen(UUID citizenId)" not in world_data_code:
        errors.append("MFWorldSavedData no expone búsqueda indexada de ciudadanos")
    if "citizens(null).stream()" in resident_code:
        errors.append("MFResidentEntity sigue escaneando y asignando la población completa en cada tick")
else:
    errors.append("Falta validación de búsqueda indexada de residentes")

# MAIN_GAMEPLAY_COMMAND_HUB_VALIDATION_V1
ui_file = java_root / "com/medievalfactions/client/MFMainScreen.java"
ui_input = java_root / "com/medievalfactions/client/MFPlayerSkinInput.java"
ui_events = java_root / "com/medievalfactions/client/MFClientEvents.java"
if not all(path.is_file() for path in (ui_file, ui_input, ui_events)):
    errors.append("Falta el panel principal de acceso a mecánicas")
else:
    ui_code = ui_file.read_text(encoding="utf-8")
    input_code = ui_input.read_text(encoding="utf-8")
    events_code = ui_events.read_text(encoding="utf-8")
    client_state_file = java_root / "com/medievalfactions/client/MFManagementClientState.java"
    request_file = java_root / "com/medievalfactions/network/MFManagementRequestPayload.java"
    response_file = java_root / "com/medievalfactions/network/MFManagementSnapshotPayload.java"
    dashboard_file = java_root / "com/medievalfactions/data/MFManagementDashboard.java"
    server_network_file = java_root / "com/medievalfactions/network/MFPlayerSkinServerNetwork.java"
    required = (client_state_file, request_file, response_file, dashboard_file, server_network_file)
    if not all(path.is_file() for path in required):
        errors.append("La GUI integral carece de su canal cliente-servidor o de su read-model")
    else:
        client_state = client_state_file.read_text(encoding="utf-8")
        request_code = request_file.read_text(encoding="utf-8")
        response_code = response_file.read_text(encoding="utf-8")
        dashboard_code = dashboard_file.read_text(encoding="utf-8")
        server_network = server_network_file.read_text(encoding="utf-8")
        gui_checks = (
            all(label in ui_code for label in ("Reino", "Habitantes", "Ciudades", "Ejército",
                                                "Comercio", "Dragones", "Familia / salud", "Aventuras")),
            'MFManagementClientState.body()' in ui_code and 'requestPage(selectedPage)' in ui_code,
            'Component.literal("Actualizar")' in ui_code and 'Component.literal("Cerrar")' in ui_code,
            'Component.literal("Cambiar skin")' in ui_code,
            'PacketDistributor.sendToServer(new MFManagementRequestPayload(safePage))' in client_state,
            'PAGE_COUNT = 8' in request_code,
            'MAX_BODY_CHARS = 3_600' in response_code,
            'MFManagementDashboard.render(player, page)' in server_network,
            'PacketDistributor.sendToPlayer(player' in server_network,
            all(section in dashboard_code for section in ("Habitantes e IA", "Ciudades y obras",
                "Ejércitos y asedios", "Comercio y caravanas", "Dragones y compañeros",
                "Familia y medicina", "Aventuras y jefes"))
        )
        if not all(gui_checks):
            errors.append("La GUI integral debe mostrar ocho pestañas conectadas a un snapshot de servidor acotado")

    if "OPEN_MENU.consumeClick()" not in input_code or "event.register(MFPlayerSkinInput.OPEN_MENU)" not in events_code:
        errors.append("El panel principal no está enlazado a su tecla de acceso")
    if '            79,' not in input_code:
        errors.append("El panel principal debe abrirse con la tecla O (GLFW_KEY_O)")
    commands_file = java_root / "com/medievalfactions/command/MFCommands.java"
    advanced_file = java_root / "com/medievalfactions/command/MFAdvancedCommands.java"
    if not commands_file.is_file() or not advanced_file.is_file():
        errors.append("Faltan los comandos que conectan el panel de jugador")
    else:
        commands_text = commands_file.read_text(encoding="utf-8")
        advanced_text = advanced_file.read_text(encoding="utf-8")
        wired = (
            'Commands.literal("character")' in commands_text
            and 'Commands.literal("info")' in commands_text
            and 'Commands.literal("tree")' in commands_text
            and 'Commands.literal("faction")' in commands_text
            and "calendarCommand()" in commands_text
            and 'Commands.literal("skill")' in advanced_text
            and 'Commands.literal("companion")' in advanced_text
            and "dashboardCommand()" in advanced_text
        )
        if not wired:
            errors.append("Uno o más comandos del panel no están registrados en el árbol de comandos")
        # DASHBOARD_STATUS_VALIDATION_V1
        if "Panel rápido disponible (O)" not in advanced_text:
            errors.append("El dashboard debe reflejar el panel rápido y el estado actual de las skins")
        if "sincronización final aún pendientes" in advanced_text:
            errors.append("El dashboard contiene texto obsoleto sobre la sincronización de skins")

print(f"JSON: {len(json_files)} | Geo rigs: {len(geos)} | SoundEvents: {len(sounds)} | OGG: {len(list((ASSETS/'sounds').rglob('*.ogg')))}")
print(f"Errores: {len(errors)} | Advertencias: {len(warnings)}")
for x in errors: print('ERROR:',x)
for x in warnings: print('WARN:',x)
sys.exit(1 if errors else 0)
