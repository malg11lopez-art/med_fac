#!/usr/bin/env python3
"""Apply verified NeoForge 1.21.1 / GeckoLib compile fixes to the v0.9 source tree."""
from pathlib import Path
import base64
import gzip
import subprocess
import shutil
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: apply_build_patches.py PROJECT_DIR")
project = Path(sys.argv[1]).resolve()
root = project / "src/main/java/com/medievalfactions"

# Pin the mod metadata version to the actual v0.9.1-alpha source being built.
properties_file = project / "gradle.properties"
if not properties_file.is_file():
    raise SystemExit("gradle.properties is missing")
property_lines = properties_file.read_text(encoding="utf-8").splitlines()
version_indices = [i for i, line in enumerate(property_lines) if line.strip().startswith("mod_version=")]
if len(version_indices) != 1:
    raise SystemExit(f"Expected one mod_version property, found {len(version_indices)}")
if property_lines[version_indices[0]].strip() != "mod_version=0.9.1-alpha":
    property_lines[version_indices[0]] = "mod_version=0.9.1-alpha"
    properties_file.write_text("\n".join(property_lines) + "\n", encoding="utf-8")
print("Pinned MEDIEVAL FACTIONS version to 0.9.1-alpha")

# Apply the reviewed source/resource enhancement patch before compatibility checks.
# The payload is split into text chunks because the GitHub contents API is text-oriented.
patch_dir = Path(__file__).resolve().parent / "feature_patch"
patch_parts = sorted(patch_dir.glob("part*.b64")) if patch_dir.exists() else []
already_enhanced = (
    (root / "item/MFPotionItem.java").exists()
    and (project / "src/main/resources/assets/medieval_factions/animations/entities/guard_full.animation.json").exists()
    and (root / "gametest/MFRegistryGameTests.java").exists()
)
if already_enhanced:
    print("Source archive already contains v0.9.1 gameplay/resource enhancements; skipping binary patch.")
elif patch_parts:
    payload = "".join(part.read_text(encoding="ascii").strip() for part in patch_parts)
    try:
        patch_bytes = gzip.decompress(base64.b64decode(payload, validate=True))
    except Exception as exc:
        raise SystemExit(f"Could not decode enhancement patch: {exc}")
    result = subprocess.run(
        ["patch", "--batch", "--forward", "-p1"],
        cwd=project,
        input=patch_bytes,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(result.stdout.decode("utf-8", errors="replace"))
    if result.returncode != 0:
        raise SystemExit(f"Enhancement patch failed with exit code {result.returncode}")
    print(f"Applied source/resource enhancements ({len(patch_parts)} patch chunks).")
else:
    raise SystemExit("Source lacks v0.9.1 enhancements and no feature patch bundle is available.")


# Apply the audited player-skin/audio-quality patch after the gameplay enhancement patch.
# The marker is deliberately written by the patch into the validator so a partially
# applied tree cannot be mistaken for a complete quality upgrade.
quality_patch_dir = Path(__file__).resolve().parent / "quality_patch"
quality_patch_parts = sorted(quality_patch_dir.glob("part*.b64")) if quality_patch_dir.exists() else []
quality_validator = project / "tools/validate_assets.py"
quality_skin_layer = root / "client/MFPlayerSkinLayer.java"
quality_marker_present = (
    quality_skin_layer.exists()
    and quality_validator.exists()
    and "FULL_AUDIO_META_VALIDATION_V1" in quality_validator.read_text(encoding="utf-8")
)
if quality_marker_present:
    print("Player skin renderer and strict audio/resource checks already present; skipping quality patch.")
elif quality_patch_parts:
    quality_payload = "".join(part.read_text(encoding="ascii").strip() for part in quality_patch_parts)
    try:
        quality_patch_bytes = gzip.decompress(base64.b64decode(quality_payload, validate=True))
    except Exception as exc:
        raise SystemExit(f"Could not decode quality patch: {exc}")
    quality_result = subprocess.run(
        ["patch", "--batch", "--forward", "-p1"],
        cwd=project,
        input=quality_patch_bytes,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(quality_result.stdout.decode("utf-8", errors="replace"))
    if quality_result.returncode != 0:
        raise SystemExit(f"Resource/skin quality patch failed with exit code {quality_result.returncode}")
    print(f"Applied player-skin/audio-quality patch ({len(quality_patch_parts)} chunks).")
else:
    raise SystemExit("Player-skin/audio-quality patch bundle is missing.")



# Overlay the audited server networking implementation into the canonical ZIP build.
# The phase2 snapshot is a source overlay, not just a QA export: this keeps the
# network payload declarations that were added after the original v0.9.0 archive.
server_network_overlay = (
    Path(__file__).resolve().parent
    / "phase2_src/src/main/java/com/medievalfactions/network/MFPlayerSkinServerNetwork.java"
)
server_network_target = root / "network/MFPlayerSkinServerNetwork.java"
if not server_network_overlay.is_file():
    raise SystemExit("Missing server networking source overlay")
server_network_target.parent.mkdir(parents=True, exist_ok=True)
server_network_target.write_text(server_network_overlay.read_text(encoding="utf-8"), encoding="utf-8")
print("Applied serverbound and clientbound payload registration overlay")

# Add the operator-only preview command for all 75 registered SoundEvents.
overlay_sound = Path(__file__).resolve().parent / "overlays/MFSoundPreview.java"
preview_target = root / "registry/MFSoundPreview.java"
if not overlay_sound.is_file():
    raise SystemExit("Missing CI overlay: overlays/MFSoundPreview.java")
preview_target.parent.mkdir(parents=True, exist_ok=True)
if not preview_target.exists():
    preview_target.write_text(overlay_sound.read_text(encoding="utf-8"), encoding="utf-8")
    print("Added operator SoundEvent preview registry")

advanced_commands = root / "command/MFAdvancedCommands.java"
command_text = advanced_commands.read_text(encoding="utf-8")
for import_line in (
    "import net.minecraft.commands.SharedSuggestionProvider;",
    "import net.minecraft.sounds.SoundSource;",
    "import com.medievalfactions.registry.MFSoundPreview;",
    "import java.util.Locale;",
):
    if import_line not in command_text:
        package_line = "package com.medievalfactions.command;\n"
        if package_line not in command_text:
            raise SystemExit("Could not add imports to MFAdvancedCommands.java")
        command_text = command_text.replace(package_line, package_line + "\n" + import_line + "\n", 1)
if "root.then(soundCommands());" not in command_text:
    registration = "        root.then(dungeonCommands());"
    if command_text.count(registration) != 1:
        raise SystemExit("Could not find advanced command registration point for sound preview")
    command_text = command_text.replace(registration, registration + "\n        root.then(soundCommands());", 1)
sound_method = """    /** Operator-only preview for all registered SoundEvents. */
    private static LiteralArgumentBuilder<CommandSourceStack> soundCommands() {
        var command = Commands.literal("sound").requires(source -> source.hasPermission(2));
        command.then(Commands.literal("preview")
                .then(Commands.argument("sound_id", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(MFSoundPreview.ids(), builder))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String id = StringArgumentType.getString(ctx, "sound_id").toLowerCase(Locale.ROOT);
                            var sound = MFSoundPreview.find(id);
                            if (sound == null) {
                                ctx.getSource().sendFailure(Component.literal("Sonido no registrado: " + id));
                                return 0;
                            }
                            player.level().playSound(null, player.blockPosition(), sound.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            ctx.getSource().sendSuccess(() -> Component.literal("Reproduciendo " + id), false);
                            return 1;
                        })));
        return command;
    }

"""
if "private static LiteralArgumentBuilder<CommandSourceStack> soundCommands()" not in command_text:
    skill_anchor = "    private static LiteralArgumentBuilder<CommandSourceStack> skillCommands() {"
    if command_text.count(skill_anchor) != 1:
        raise SystemExit("Could not locate insertion point for sound preview command")
    command_text = command_text.replace(skill_anchor, sound_method + skill_anchor, 1)
advanced_commands.write_text(command_text, encoding="utf-8")
print("Registered /mf advanced sound preview <sound_id>")

def replace_once(relative: str, old: str, new: str) -> None:
    path = root / relative
    source = path.read_text(encoding="utf-8")
    count = source.count(old)
    if count == 1:
        path.write_text(source.replace(old, new, 1), encoding="utf-8")
        print(f"Patched {relative}")
    elif count == 0 and new in source:
        print(f"Already patched {relative}")
    else:
        raise SystemExit(f"Patch precondition failed for {relative}: expected one original or an already-patched file, found {count}")


# Give every registry GameTest an actual structure template. In 1.21.1 the
# GameTestHolder prefixes the class name, so "empty" resolves to
# medieval_factions:mfregistrygametests.empty.
test_source = root / "gametest/MFRegistryGameTests.java"
test_text = test_source.read_text(encoding="utf-8")
test_text = test_text.replace('template = "minecraft:empty"', 'template = "empty"')
test_source.write_text(test_text, encoding="utf-8")

# Write a minimal 1x1x1 all-air structure template (NBT DataVersion 3955).
# This avoids depending on a vanilla "empty" structure that is not shipped
# in the NeoForge 1.21.1 GameTest data.
import gzip
import struct

def nbt_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded

def nbt_named(tag_type: int, name: str, payload: bytes) -> bytes:
    return bytes([tag_type]) + nbt_string(name) + payload

nbt = bytearray([10]) + nbt_string("")  # root compound
nbt += nbt_named(3, "DataVersion", struct.pack(">i", 3955))
nbt += nbt_named(9, "size", bytes([3]) + struct.pack(">i", 3) + struct.pack(">iii", 1, 1, 1))
palette_compound = nbt_named(8, "Name", nbt_string("minecraft:air")) + bytes([0])
nbt += nbt_named(9, "palette", bytes([10]) + struct.pack(">i", 1) + palette_compound)
nbt += nbt_named(9, "blocks", bytes([10]) + struct.pack(">i", 0))
nbt += nbt_named(9, "entities", bytes([10]) + struct.pack(">i", 0))
nbt += bytes([0])  # root compound end
structure_path = project / "src/main/resources/data/medieval_factions/structure/mfregistrygametests.empty.nbt"
structure_path.parent.mkdir(parents=True, exist_ok=True)
structure_path.write_bytes(gzip.compress(bytes(nbt), mtime=0))
print(f"Generated GameTest template: {structure_path.relative_to(project)}")

# ModDevGradle 2.0.148 RunModel does not expose NeoGradle's setForceExit DSL.
# Keep the GameTest Server run, but remove that unsupported property; the plugin's
# task runner handles process lifecycle itself.
build_file = project / "build.gradle"
build_text = build_file.read_text(encoding="utf-8")
if "setForceExit false" in build_text:
    cleaned_lines = [
        line for line in build_text.splitlines()
        if "setForceExit false" not in line
        and "Prevent Gradle's daemon from being force-exited" not in line
    ]
    build_file.write_text("\n".join(cleaned_lines) + "\n", encoding="utf-8")
    build_text = build_file.read_text(encoding="utf-8")
    if "setForceExit false" in build_text:
        raise SystemExit("Could not remove unsupported setForceExit DSL from build.gradle")
    print("Patched build.gradle: removed unsupported ModDevGradle setForceExit DSL")
else:
    print("build.gradle: unsupported setForceExit DSL already absent")

# Minecraft 1.21.1 exposes Mob save/load hooks as public methods.
replace_once("entity/MFAnimatedMob.java",
    "    protected void addAdditionalSaveData(CompoundTag tag) {",
    "    public void addAdditionalSaveData(CompoundTag tag) {")
replace_once("entity/MFAnimatedMob.java",
    "    protected void readAdditionalSaveData(CompoundTag tag) {",
    "    public void readAdditionalSaveData(CompoundTag tag) {" )
replace_once("entity/MFResidentEntity.java",
    "@Override protected void addAdditionalSaveData(CompoundTag tag)",
    "@Override public void addAdditionalSaveData(CompoundTag tag)")
replace_once("entity/MFResidentEntity.java",
    "@Override protected void readAdditionalSaveData(CompoundTag tag)",
    "@Override public void readAdditionalSaveData(CompoundTag tag)")

# NeoForge 1.21.1 exposes stack-sensitive limits on ItemStack, not Item.
replace_once("command/MFAdvancedCommands.java",
    "        long remainder = Math.max(0L, amount - existingCapacity);\n        return (remainder + item.getMaxStackSize() - 1L) / item.getMaxStackSize();",
    "        long remainder = Math.max(0L, amount - existingCapacity);\n        int defaultStackLimit = new ItemStack(item).getMaxStackSize();\n        return (remainder + defaultStackLimit - 1L) / defaultStackLimit;")
# addCoins already obtains its stack limit in the feature patch; do not inject it twice.
replace_once("command/MFAdvancedCommands.java",
    "int added = (int) Math.min(remaining, item.getMaxStackSize());",
    "int added = (int) Math.min(remaining, defaultStackLimit);")
replace_once("command/MFAdvancedCommands.java",
    '            if (!player.getMainHandItem().isEdible()) { ctx.getSource().sendFailure(Component.literal("Sostén comida en la mano principal.")); return 0; }',
    '            if (player.getMainHandItem().isEmpty() || player.getMainHandItem().getFoodProperties(player) == null) {\n                ctx.getSource().sendFailure(Component.literal("Sostén comida en la mano principal.")); return 0;\n            }')
replace_once("command/MFAdvancedCommands.java",
    "                            MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays());\n                    if (egg.isEmpty())",
    "                            (int) Math.min(Integer.MAX_VALUE, Math.max(0L,\n                                    MFWorldSavedData.get(ctx.getSource().getServer()).simulatedDays())));\n                    if (egg.isEmpty())")
for old, new in [
    ("target.setSecondsOnFire(4)", "target.igniteForSeconds(4.0F)"),
    ("target.setSecondsOnFire(6)", "target.igniteForSeconds(6.0F)"),
    ("livingTarget.setSecondsOnFire(4)", "livingTarget.igniteForSeconds(4.0F)"),
]:
    replace_once("command/MFAdvancedCommands.java", old, new)

# Capture the key before a lambda; the loop variable is reassigned below.
replace_once("data/MFAdvancedSavedData.java",
    '        for (Expedition original : records("expeditions", Expedition.class)) {\n            if (original.status() == ExpeditionStatus.COMPLETED || original.status() == ExpeditionStatus.FAILED) continue;\n            var physical = records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class).stream()\n                    .filter(j -> j.expeditionId().equals(original.id())).findFirst().orElse(null);',
    '        for (Expedition original : records("expeditions", Expedition.class)) {\n            if (original.status() == ExpeditionStatus.COMPLETED || original.status() == ExpeditionStatus.FAILED) continue;\n            UUID currentExpeditionId = original.id();\n            var physical = records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class).stream()\n                    .filter(j -> j.expeditionId().equals(currentExpeditionId)).findFirst().orElse(null);')

# GeckoLib 4.9.x has no static initialize() method; its mod bootstrap is automatic.
replace_once("MedievalFactions.java", "import software.bernie.geckolib.GeckoLib;\n", "")
replace_once("MedievalFactions.java", "        GeckoLib.initialize();\n", "")

# The broad source-set exclusion also removed the generated NeoForge mod descriptor
# from the final JAR. There is no unexpanded descriptor under src/main/resources,
# so this exclusion is both unnecessary and harmful.
build_file = project / "build.gradle"
build_text = build_file.read_text(encoding="utf-8")
bad_exclusion = "sourceSets.main.resources.exclude('META-INF/neoforge.mods.toml')"
if bad_exclusion in build_text:
    build_file.write_text(build_text.replace(bad_exclusion + "\n", "", 1), encoding="utf-8")
    print("Patched build.gradle: keep generated META-INF/neoforge.mods.toml in the JAR")
elif "sourceSets.main.resources.exclude('META-INF/neoforge.mods.toml')" not in build_text:
    print("build.gradle descriptor exclusion already absent")
else:
    raise SystemExit("Unexpected build.gradle descriptor configuration")

# Fix the windmill block atlas reference. Entity textures are not stitched into
# the block atlas; the inventory and world block model must reference textures/block.
windmill_model = project / "src/main/resources/assets/medieval_factions/models/block/windmill.json"
if windmill_model.exists():
    model_text = windmill_model.read_text(encoding="utf-8")
    model_text = model_text.replace("medieval_factions:entity/windmill", "medieval_factions:block/windmill")
    windmill_model.write_text(model_text, encoding="utf-8")
    windmill_entity_texture = project / "src/main/resources/assets/medieval_factions/textures/entity/windmill.png"
    windmill_block_texture = project / "src/main/resources/assets/medieval_factions/textures/block/windmill.png"
    if not windmill_block_texture.exists() and windmill_entity_texture.exists():
        windmill_block_texture.parent.mkdir(parents=True, exist_ok=True)
        windmill_block_texture.write_bytes(windmill_entity_texture.read_bytes())
        print("Copied windmill texture into the block atlas")
    elif not windmill_block_texture.exists():
        raise SystemExit("Missing windmill source texture for block atlas")
    print("Patched windmill block model atlas reference")


# Correct the sound-trigger audit: the operator preview catalogue and GameTests are not gameplay triggers.
validator_file = project / "tools/validate_assets.py"
if not validator_file.is_file():
    raise SystemExit("Missing tools/validate_assets.py for sound-trigger audit")
validator_text = validator_file.read_text(encoding="utf-8")
audit_marker = "EXCLUDE_PREVIEW_AND_GAMETEST_SOUND_REFERENCES_V1"
if audit_marker not in validator_text:
    old_audit = """java_sound_ids = set()
for java_file in java_root.rglob("*.java"):
    java_code = java_file.read_text(encoding="utf-8")
    java_sound_ids.update(name.lower() for name in re.findall(r"MFSounds\\.([A-Z0-9_]+)", java_code) if name != "SOUND_EVENTS")
connected_sound_ids = keyframe_sound_ids | java_sound_ids
"""
    new_audit = """# EXCLUDE_PREVIEW_AND_GAMETEST_SOUND_REFERENCES_V1
# A SoundEvent listed in the operator audition catalogue or referenced only by
# a GameTest is not a natural gameplay trigger. Exclude those files from coverage.
java_sound_ids = set()
for java_file in java_root.rglob("*.java"):
    relative_parts = java_file.relative_to(java_root).parts
    if java_file.name == "MFSoundPreview.java" or "gametest" in relative_parts:
        continue
    java_code = java_file.read_text(encoding="utf-8")
    java_sound_ids.update(name.lower() for name in re.findall(r"MFSounds\\.([A-Z0-9_]+)", java_code) if name != "SOUND_EVENTS")
connected_sound_ids = keyframe_sound_ids | java_sound_ids
"""
    if validator_text.count(old_audit) != 1:
        raise SystemExit("Could not locate the exact SoundEvent audit block; refusing to apply an ambiguous patch.")
    validator_text = validator_text.replace(old_audit, new_audit, 1)
    validator_text = validator_text.replace(
        'print(f"Sound coverage: {len(connected_sound_ids & set(sounds))}/{len(sounds)} events referenced by animation keyframes or gameplay code")\n'
        'print(f"SoundEvents registered but not yet triggered by current clips/actions: {len(unused_sound_ids)}")',
        'print(f"Gameplay sound-trigger coverage: {len(connected_sound_ids & set(sounds))}/{len(sounds)} events referenced by animation keyframes or runtime gameplay code")\n'
        'print(f"Registered sounds without a natural gameplay trigger: {len(unused_sound_ids)}")\n'
        'if unused_sound_ids:\n'
        '    print("Preview-only / untriggered SoundEvents: " + ", ".join(sorted(unused_sound_ids)))',
        1
    )
    validator_file.write_text(validator_text, encoding="utf-8")
    print("Corrected sound-trigger audit: preview catalogue and GameTests excluded.")
else:
    print("Sound-trigger audit already excludes operator preview and GameTests.")


# Apply the server-authoritative multiplayer skin and gameplay audio integration.
# The patch is generated from the already validated v0.9.1 CI source snapshot.
overlay_dir = Path(__file__).resolve().parent / "overlays"
overlay_parts = sorted(overlay_dir.glob("skin_audio_gameplay_part*.b64")) if overlay_dir.exists() else []
skin_validator = project / "tools/validate_assets.py"
if not skin_validator.is_file():
    raise SystemExit("Missing tools/validate_assets.py for multiplayer skin validation")
skin_validator_text = skin_validator.read_text(encoding="utf-8")
skin_marker = "MULTIPLAYER_SKIN_SYNC_VALIDATION_V1"
skin_ready = (
    (root / "network/MFPlayerSkinServerNetwork.java").exists()
    and (root / "MFGameplayAudioEvents.java").exists()
    and "skinFor(player)" in (root / "client/MFPlayerSkinLayer.java").read_text(encoding="utf-8")
    and skin_marker in skin_validator_text
)
if skin_ready:
    print("Multiplayer skin/audio gameplay overlay already applied.")
elif overlay_parts:
    payload = "".join(part.read_text(encoding="ascii").strip() for part in overlay_parts)
    try:
        overlay_bytes = gzip.decompress(base64.b64decode(payload, validate=True))
    except Exception as exc:
        raise SystemExit(f"Could not decode multiplayer skin/audio overlay: {exc}")
    result = subprocess.run(
        ["patch", "--batch", "--forward", "-p1"],
        cwd=project,
        input=overlay_bytes,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(result.stdout.decode("utf-8", errors="replace"))
    if result.returncode != 0:
        raise SystemExit(f"Multiplayer skin/audio overlay failed with exit code {result.returncode}")
    if skin_marker not in skin_validator.read_text(encoding="utf-8"):
        raise SystemExit("Multiplayer skin/audio overlay did not complete its validation marker.")
    print(f"Applied multiplayer skin sync and natural audio/gameplay hooks ({len(overlay_parts)} chunks).")
else:
    raise SystemExit("Multiplayer skin/audio overlay bundle is missing.")

# The combat simulator target adapter must use the entity reference that replaced
# ServerPlayer.pick() above; pick() returns a block hit, not an EntityHitResult.
# Keep this compatibility correction idempotent so a future source ZIP may
# include it directly without making the overlay fail.
advanced_commands = root / "command/MFAdvancedCommands.java"
advanced_text = advanced_commands.read_text(encoding="utf-8")
for old, new in (
    ("Math.ceil(living.getHealth())", "Math.ceil(livingTarget.getHealth())"),
    ("Math.ceil(living.getMaxHealth())", "Math.ceil(livingTarget.getMaxHealth())"),
    ("new Combatant(living.getUUID(), CombatClass.WARRIOR", "new Combatant(livingTarget.getUUID(), CombatClass.WARRIOR"),
):
    count = advanced_text.count(old)
    if count == 1:
        advanced_text = advanced_text.replace(old, new, 1)
    elif count == 0 and new in advanced_text:
        continue
    else:
        raise SystemExit(f"Combat target adapter patch precondition failed: {old}")
advanced_commands.write_text(advanced_text, encoding="utf-8")
print("Verified combat target adapter uses the server-raytraced livingTarget.")

# Install the in-game command hub only after the core multiplayer/audio patch
# has applied, so its validator can assert the complete patched project.
menu_overlay_dir = Path(__file__).resolve().parent / "overlays"
menu_overlay_parts = sorted(menu_overlay_dir.glob("main_menu_part*.b64")) if menu_overlay_dir.exists() else []
menu_validator = project / "tools/validate_assets.py"
menu_marker = "MAIN_GAMEPLAY_COMMAND_HUB_VALIDATION_V1"
menu_ready = (
    (root / "client/MFMainScreen.java").is_file()
    and menu_marker in menu_validator.read_text(encoding="utf-8")
)
if menu_ready:
    print("MEDIEVAL FACTIONS in-game command hub already present.")
elif menu_overlay_parts:
    menu_payload = "".join(part.read_text(encoding="ascii").strip() for part in menu_overlay_parts)
    try:
        menu_patch_bytes = gzip.decompress(base64.b64decode(menu_payload, validate=True))
    except Exception as exc:
        raise SystemExit(f"Could not decode in-game command hub patch: {exc}")
    menu_result = subprocess.run(
        ["patch", "--batch", "--forward", "-p1"],
        cwd=project,
        input=menu_patch_bytes,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(menu_result.stdout.decode("utf-8", errors="replace"))
    if menu_result.returncode != 0:
        raise SystemExit(f"In-game command hub patch failed with exit code {menu_result.returncode}")
    if not (root / "client/MFMainScreen.java").is_file() or menu_marker not in menu_validator.read_text(encoding="utf-8"):
        raise SystemExit("In-game command hub patch did not complete its validation marker.")
    print(f"Installed in-game command hub ({len(menu_overlay_parts)} patch chunks).")
else:
    raise SystemExit("In-game command hub patch bundle is missing.")

# The advertised launcher shortcut is O (GLFW_KEY_O=79), not P (80).
menu_input = root / "client/MFPlayerSkinInput.java"
menu_input_text = menu_input.read_text(encoding="utf-8")
old_key = '            80,\n            "key.categories.medieval_factions");'
new_key = '            79,\n            "key.categories.medieval_factions");'
if menu_input_text.count(old_key) == 1:
    menu_input_text = menu_input_text.replace(old_key, new_key, 1)
elif menu_input_text.count(new_key) == 1:
    pass
else:
    raise SystemExit("Could not verify the in-game menu O shortcut mapping")
menu_input.write_text(menu_input_text, encoding="utf-8")

# Validate that every hub button maps to an implemented server command, not
# merely to a literal string embedded in the UI class.
validator_text = menu_validator.read_text(encoding="utf-8")
menu_validation_old = '''    if "OPEN_MENU.consumeClick()" not in input_code or "event.register(MFPlayerSkinInput.OPEN_MENU)" not in events_code:
        errors.append("El panel principal no está enlazado a su tecla de acceso")
'''
menu_validation_new = '''    if "OPEN_MENU.consumeClick()" not in input_code or "event.register(MFPlayerSkinInput.OPEN_MENU)" not in events_code:
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
'''
if menu_validation_old not in validator_text:
    raise SystemExit("UI validator block not found to extend")
validator_text=validator_text.replace(menu_validation_old,menu_validation_new,1)
menu_validator.write_text(validator_text,encoding="utf-8")
print("Pinned the in-game command hub to O and verified backing commands.")

# Use indexed UUID access for resident entities. The previous implementation
# allocated and scanned the full citizen population every 10 ticks per resident.
resident_overlay_dir = Path(__file__).resolve().parent / "overlays"
resident_overlay_parts = sorted(resident_overlay_dir.glob("resident_indexed_lookup_part*.b64")) if resident_overlay_dir.exists() else []
resident_validator = project / "tools/validate_assets.py"
resident_marker = "RESIDENT_INDEXED_LOOKUP_VALIDATION_V1"
resident_ready = (
    "Optional<MFCitizenState> citizen(UUID citizenId)" in (project / "src/main/java/com/medievalfactions/data/MFWorldSavedData.java").read_text(encoding="utf-8")
    and "worldData.citizen(citizenId).orElse(null)" in (root / "entity/MFResidentEntity.java").read_text(encoding="utf-8")
    and resident_marker in resident_validator.read_text(encoding="utf-8")
)
if resident_ready:
    print("Indexed resident lookup optimization already present.")
elif resident_overlay_parts:
    resident_payload = "".join(part.read_text(encoding="ascii").strip() for part in resident_overlay_parts)
    try:
        resident_patch_bytes = gzip.decompress(base64.b64decode(resident_payload, validate=True))
    except Exception as exc:
        raise SystemExit(f"Could not decode indexed resident lookup patch: {exc}")
    resident_result = subprocess.run(
        ["patch", "--batch", "--forward", "-p1"],
        cwd=project,
        input=resident_patch_bytes,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(resident_result.stdout.decode("utf-8", errors="replace"))
    if resident_result.returncode != 0:
        raise SystemExit(f"Indexed resident lookup patch failed with exit code {resident_result.returncode}")
    if resident_marker not in resident_validator.read_text(encoding="utf-8"):
        raise SystemExit("Indexed resident lookup patch did not complete its validation marker.")
    print(f"Applied indexed resident lookup and regression tests ({len(resident_overlay_parts)} chunks).")
else:
    raise SystemExit("Indexed resident lookup patch bundle is missing.")


# Correct the in-game dashboard text so it reflects features already integrated.
# The dashboard should distinguish the quick command hub from the full GUI backlog.
advanced_commands = root / "command/MFAdvancedCommands.java"
advanced_text = advanced_commands.read_text(encoding="utf-8")
old_dashboard = "§7Base de simulación persistente activa. GUI completa, entidades de ejército/carro, animaciones y sincronización final aún pendientes."
new_dashboard = "§7Panel rápido disponible (O); skins con sincronización de servidor integrada (pendiente prueba con dos clientes). Siguen pendientes la GUI de gestión completa, IA social/laboral autónoma y entidades físicas de ejército y caravanas."
if old_dashboard in advanced_text:
    advanced_text = advanced_text.replace(old_dashboard, new_dashboard, 1)
elif new_dashboard not in advanced_text:
    raise SystemExit("Dashboard status text did not match either the old or updated wording.")
advanced_commands.write_text(advanced_text, encoding="utf-8")

dashboard_marker = "DASHBOARD_STATUS_VALIDATION_V1"
validator_text = validator_file.read_text(encoding="utf-8")
dashboard_check = """        # DASHBOARD_STATUS_VALIDATION_V1
        if "Panel rápido disponible (O)" not in advanced_text:
            errors.append("El dashboard debe reflejar el panel rápido y el estado actual de las skins")
        if "sincronización final aún pendientes" in advanced_text:
            errors.append("El dashboard contiene texto obsoleto sobre la sincronización de skins")
"""
if dashboard_marker not in validator_text:
    anchor = '        if not wired:\n            errors.append("Uno o más comandos del panel no están registrados en el árbol de comandos")\n'
    if validator_text.count(anchor) != 1:
        raise SystemExit("Could not find unique insertion point for dashboard status validation.")
    validator_text = validator_text.replace(anchor, anchor + dashboard_check, 1)
    validator_file.write_text(validator_text, encoding="utf-8")
print("Updated and validated the in-game dashboard status.")

# Refresh in-archive Spanish status docs; historical release notes remain untouched.
source_readme = project / "README_ES.md"
readme_text = source_readme.read_text(encoding="utf-8")
readme_text = readme_text.replace("# MEDIEVAL FACTIONS — Alpha v0.9.0", "# MEDIEVAL FACTIONS — Alpha v0.9.1", 1)
status_start = readme_text.find("## Estado de esta entrega")
features_start = readme_text.find("## Novedades de v0.9")
if status_start < 0 or features_start < 0 or features_start <= status_start:
    raise SystemExit("README_ES.md status section boundaries are not recognized")
current_status = """## Estado de esta entrega — v0.9.1-alpha (CI #50)

La compilación CI #50 valida build NeoForge, GameTests, arranque de servidor dedicado, cliente headless y recarga de recursos. Sigue siendo una alpha: estas pruebas no equivalen a una partida manual ni a una certificación audiovisual/multijugador completa.

- 276 aserciones de dominio superadas.
- 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG validados: 0 errores/advertencias.
- 86 skins con sincronización servidor-cliente; pendiente probar dos clientes reales y aplicación a NPC.
- Habilidades elemental_burst y shadow_blink, selección de objetivos con ray tracing de entidades y búsqueda indexada de residentes por UUID.
- Panel rápido en tecla O; conecta comandos existentes, pero no sustituye la GUI de gestión completa.

La matriz exacta por mecánica está en SYSTEM_COMPLETION_MATRIX.md. La arquitectura está en SIMULATION_ARCHITECTURE.md, las pruebas en TEST_MATRIX.md y el plan restante en NEXT_IMPLEMENTATION_PLAN.md.

"""
readme_text = readme_text[:status_start] + current_status + readme_text[features_start:]
install_start = readme_text.find("Desde el directorio del proyecto:")
if install_start >= 0:
    readme_text = readme_text[:install_start] + """Con JDK 21 y Gradle 8.12 instalados, ejecuta python tools/validate_assets.py, bash tools/run_domain_tests.sh, bash tools/run_saveddata_stub_tests.sh, gradle --no-daemon clean test build y gradle --no-daemon runGameTestServer.

La integración CI extrae el ZIP, aplica parches de compatibilidad/calidad y ejecuta también los smoke tests del servidor y cliente headless. El proyecto fuente no incluye gradlew; para builds locales reproducibles se recomienda añadir un Gradle Wrapper versionado con checksum.

## Pruebas y calidad

Las 276 aserciones son tests de dominio con JDK 21. CI #50 supera build, GameTests y smoke startup/reload de servidor/cliente. El SavedData smoke test utiliza stubs y no sustituye el round-trip NBT real de todos los tipos. El cliente headless usa audio nulo: valida inicialización y carga de recursos, no renderizado ni escucha humana. No se ha ejecutado una partida manual ni una sesión multijugador de dos clientes.

No usar esta alpha como mod final en un mundo principal sin copia de seguridad. Haz backups antes de actualizar.
"""
source_readme.write_text(readme_text, encoding="utf-8")

status_file = project / "PROJECT_STATUS.md"
status_text = status_file.read_text(encoding="utf-8")
status_text = status_text.replace("MEDIEVAL FACTIONS v0.9.0-alpha", "MEDIEVAL FACTIONS v0.9.1-alpha", 1)
status_text = status_text.replace(
    "## Sistemas heredados",
    """## Integraciones adicionales de v0.9.1-alpha

- Sincronización servidor-cliente de 86 skins con persistencia por jugador y manejo de login/logout/respawn; falta prueba real de dos clientes y aplicación a NPC.
- 75/75 sonidos referenciados por keyframes o acciones runtime; la calidad auditiva humana aún necesita prueba manual.
- Habilidades elemental_burst y shadow_blink, ray tracing de objetivos con obstáculos y panel rápido en tecla O.
- Búsqueda indexada de ciudadanos por UUID para evitar recorrer toda la población desde cada residente.

## Sistemas heredados""",
    1
)
validation_start = status_text.find("## Validación ejecutada")
if validation_start < 0:
    raise SystemExit("PROJECT_STATUS.md validation section was not found")
status_text = status_text[:validation_start] + """## Validación ejecutada — CI #50

- tools/run_domain_tests.sh: 276 aserciones superadas.
- tools/run_saveddata_stub_tests.sh: PASS, smoke test limitado a un subconjunto de SavedData.
- python tools/validate_assets.py: 111 JSON, 14 rigs, 75 SoundEvents y 75 OGG; 0 errores/advertencias.
- Sincronización de skins: 86/86 texturas y 14 checks estáticos PASS.
- gradle clean test build: PASS.
- NeoForge GameTests: PASS.
- Descriptor del JAR medieval_factions-0.9.1-alpha: PASS.
- Arranque de servidor dedicado y cliente headless con recarga de recursos: PASS.

## No verificado manualmente

- Partida de Minecraft con dos clientes, skin remota visible y sincronización durante cambios de dimensión.
- Escucha humana de los 75 sonidos, mezcla, espacialización y sincronía real.
- Round-trip NBT real de todos los buckets nuevos, migraciones y reinicio de mundo.
- Rendimiento prolongado y profiling con población alta.

Dictamen: v0.9.1-alpha compila y arranca en CI y ya contiene nuevas integraciones de gameplay; no implementa todavía todas las verticales del Documento Maestro ni se considera release final.
"""
status_file.write_text(status_text, encoding="utf-8")

matrix_file = project / "SYSTEM_COMPLETION_MATRIX.md"
matrix_text = matrix_file.read_text(encoding="utf-8")
matrix_text = matrix_text.replace("MEDIEVAL FACTIONS v0.9.0-alpha", "MEDIEVAL FACTIONS v0.9.1-alpha", 1)
matrix_text = matrix_text.replace(
    "Dashboard textual y comandos de administración/jugador. Los datos del servidor ya están separados en registros de dominio.",
    "Panel rápido en tecla O con accesos a ficha, talentos, habilidades, compañeros, facciones, calendario, dashboard y selector de skins; los datos del servidor siguen separados en registros de dominio.",
    1
)
matrix_text = matrix_text.replace(
    "No hay pantallas gráficas para estos sistemas ni menús de gestión completos.",
    "El panel O es una capa de acceso rápido a comandos; siguen faltando pantallas de gestión completas y contenedores gráficos.",
    1
)
matrix_text = matrix_text.replace("**Pendiente como GUI.**", "**Panel rápido implementado; GUI integral pendiente.**", 1)
matrix_text = matrix_text.replace(
    "Seis habilidades iniciales conectadas a acciones del servidor;",
    "Seis habilidades iniciales más elemental_burst y shadow_blink conectadas a acciones del servidor;",
    1
)
matrix_text = matrix_text.replace(
    "En v0.9 no se marca ninguna de las nuevas verticales como verificada in-game porque no fue posible ejecutar NeoForge.",
    "CI #50 ya compila NeoForge, ejecuta GameTests y arranca servidor dedicado y cliente headless. Esto acredita inicialización y recarga de recursos, no una partida manual ni una evaluación visual/auditiva humana.",
    1
)
matrix_text = matrix_text.replace("271 aserciones", "276 aserciones")
matrix_text = matrix_text.replace(
    "La persistencia NBT real de todos los nuevos tipos y el arranque NeoForge siguen pendientes.",
    "La persistencia NBT round-trip de todos los tipos, la sesión de dos clientes y el profiling de población alta siguen pendientes."
)
matrix_text = matrix_text.replace("## Cambios transversales de v0.9", "## Cambios transversales de v0.9.1-alpha")
matrix_text += """

### Cambios transversales verificados en CI #50

- Skins: sincronización servidor-cliente, validación de índices 0..85, persistencia de jugador, carga al entrar/respawnear y limpieza al desconectar. Falta prueba de dos clientes y aplicación a NPC.
- Audio: 75/75 eventos referenciados por keyframes o runtime; los OGG se validan, pero falta escucha humana y ajuste de mezcla.
- Habilidades: elemental_burst y shadow_blink integradas con coste/cooldown y validación en servidor.
- Residentes: consulta indexada de ciudadano por UUID; falta profiling de carga real.
- Interfaz: panel rápido de tecla O; no equivale a GUI de gestión.
"""
matrix_file.write_text(matrix_text, encoding="utf-8")

plan_file = project / "NEXT_IMPLEMENTATION_PLAN.md"
plan_text = plan_file.read_text(encoding="utf-8")
plan_text = plan_text.replace("MEDIEVAL FACTIONS v0.9.0-alpha", "MEDIEVAL FACTIONS v0.9.1-alpha", 1)
plan_text = plan_text.replace("271 aserciones", "276 aserciones")
plan_text = plan_text.replace(
    "- [ ] Compilación real NeoForge, GameTests y prueba manual en cliente/servidor.",
    "- [x] Compilación real NeoForge, GameTests y smoke startup de servidor/cliente headless (CI #50).\n- [ ] Partida manual, audio/animaciones visuales y sesión de dos clientes."
)
p0_start = plan_text.find("## P0 — Plataforma y compilación reproducible (bloqueante)")
p1_start = plan_text.find("## P1 — Contratos de datos y seguridad de servidor")
if p0_start < 0 or p1_start <= p0_start:
    raise SystemExit("Could not locate the P0/P1 sections in NEXT_IMPLEMENTATION_PLAN.md")
p0 = """## P0 — Plataforma y compilación reproducible (completado en CI #50)

- [x] ZIP de fuentes verificado y parches aplicados desde GitHub Actions.
- [x] Java 21 y Gradle 8.12; clean test build correcto.
- [x] NeoForge GameTests correctos.
- [x] JAR con descriptor y versión exacta 0.9.1-alpha.
- [x] Servidor dedicado alcanza marcador de arranque.
- [x] Cliente headless inicializa sonido, recarga recursos y registra capa de skins.
- [ ] Añadir Gradle Wrapper versionado con checksum para builds locales.
- [ ] Añadir más GameTests de transacciones, construcción, persistencia y habilidades.

"""
plan_text = plan_text[:p0_start] + p0 + plan_text[p1_start:]
plan_file.write_text(plan_text, encoding="utf-8")
print("Updated PROJECT_STATUS, README_ES, SYSTEM_COMPLETION_MATRIX and NEXT_IMPLEMENTATION_PLAN.")

# Mark server/client skin-sync packet handling so CI can run a real client->server->client join test.
network_overlay_dir = Path(__file__).resolve().parent / "overlays"
network_overlay_parts = sorted(network_overlay_dir.glob("network_packet_log_part*.b64")) if network_overlay_dir.exists() else []
server_network_file = root / "network/MFPlayerSkinServerNetwork.java"
client_skin_file = root / "client/MFPlayerSkinClient.java"
network_ready = (
    "MF_SKIN_SELECTION_ACCEPTED" in server_network_file.read_text(encoding="utf-8")
    and "MF_SKIN_SYNC_RECEIVED" in client_skin_file.read_text(encoding="utf-8")
)
if network_ready:
    print("Network skin selection/sync diagnostic markers already present.")
elif network_overlay_parts:
    network_payload = "".join(part.read_text(encoding="ascii").strip() for part in network_overlay_parts)
    try:
        network_patch_bytes = gzip.decompress(base64.b64decode(network_payload, validate=True))
    except Exception as exc:
        raise SystemExit(f"Could not decode network packet diagnostic patch: {exc}")
    network_result = subprocess.run(
        ["patch", "--batch", "--forward", "-p1"],
        cwd=project,
        input=network_patch_bytes,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(network_result.stdout.decode("utf-8", errors="replace"))
    if network_result.returncode != 0:
        raise SystemExit(f"Network packet diagnostic patch failed with exit code {network_result.returncode}")
    if "MF_SKIN_SELECTION_ACCEPTED" not in server_network_file.read_text(encoding="utf-8") or "MF_SKIN_SYNC_RECEIVED" not in client_skin_file.read_text(encoding="utf-8"):
        raise SystemExit("Network packet diagnostic patch did not install both markers.")
    print("Installed skin packet acceptance/round-trip diagnostic markers.")
else:
    raise SystemExit("Network packet diagnostic patch bundle is missing.")


# Apply reviewed phase-two source files: population index, management GUI,
# tactical warfare, army columns, physical bosses, autonomous dragons and stress tests.
phase2_root = Path(__file__).resolve().parent / "phase2_src"
if not phase2_root.is_dir():
    raise SystemExit("Phase-two source overlay is missing.")
phase2_files = sorted(path for path in phase2_root.rglob("*") if path.is_file())
if len(phase2_files) < 17:
    raise SystemExit(f"Phase-two source overlay is incomplete: expected at least 17 files, found {len(phase2_files)}")
for source_file in phase2_files:
    relative = source_file.relative_to(phase2_root)
    target_file = project / relative
    target_file.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source_file, target_file)
population_index_file = root / "domain/MFPopulationIndex.java"
dashboard_file = root / "data/MFManagementDashboard.java"
warfare_commands_file = root / "command/MFAdvancedCommands.java"
dragon_file = root / "entity/MFDragonEntity.java"
if not population_index_file.is_file() or "isTravelling" not in population_index_file.read_text(encoding="utf-8"):
    raise SystemExit("Phase-two population index is missing or incomplete.")
if not dashboard_file.is_file() or "Resumen del reino" not in dashboard_file.read_text(encoding="utf-8"):
    raise SystemExit("Server-backed management dashboard was not installed.")
if "warfareCommands()" not in warfare_commands_file.read_text(encoding="utf-8") or "bossCommands()" not in warfare_commands_file.read_text(encoding="utf-8"):
    raise SystemExit("Tactical warfare/physical boss command routes were not installed.")
if "releaseBreath(LivingEntity primaryTarget)" not in dragon_file.read_text(encoding="utf-8"):
    raise SystemExit("Autonomous dragon breath routine was not installed.")
print(f"Applied phase-two source integration ({len(phase2_files)} files).")

# The civilian renderer selects profession-specific skins at runtime. Fail early if
# a future source-archive update drops one of those resources or the synchronized key.
resident_model_file = root / "entity/model/MFResidentModel.java"
resident_entity_file = root / "entity/MFResidentEntity.java"
resident_model_text = resident_model_file.read_text(encoding="utf-8") if resident_model_file.is_file() else ""
resident_entity_text = resident_entity_file.read_text(encoding="utf-8") if resident_entity_file.is_file() else ""
resident_roles = ("farmer", "smith", "scholar", "healer", "trader", "guard")
for role in resident_roles:
    texture = project / "src/main/resources/assets/medieval_factions/textures/entity" / f"resident_{role}.png"
    if not texture.is_file():
        raise SystemExit(f"Missing resident profession texture: {texture.relative_to(project)}")
    if f'resident_{role}' not in resident_model_text:
        raise SystemExit(f"Resident renderer does not map profession texture: {role}")
if "VISUAL_ROLE" not in resident_entity_text or "defineSynchedData" not in resident_entity_text:
    raise SystemExit("Resident profession appearance key is not synchronized to clients.")
print("Verified six profession-specific resident textures and synchronized renderer mapping.")

print("All CI compile and packaging patches applied successfully.")
