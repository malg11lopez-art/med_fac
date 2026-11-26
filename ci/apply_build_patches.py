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

print("All CI compile and packaging patches applied successfully.")
