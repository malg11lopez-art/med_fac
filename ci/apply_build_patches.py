#!/usr/bin/env python3
"""Apply verified NeoForge 1.21.1 / GeckoLib compile fixes to the v0.9 source tree."""
from pathlib import Path
import base64
import gzip
import subprocess
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

print("All CI compile and packaging patches applied successfully.")
