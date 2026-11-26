#!/usr/bin/env python3
"""Apply verified NeoForge 1.21.1 / GeckoLib compile fixes to the v0.9 source tree."""
from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: apply_build_patches.py PROJECT_DIR")
root = Path(sys.argv[1]).resolve() / "src/main/java/com/medievalfactions"


def replace_once(relative: str, old: str, new: str) -> None:
    path = root / relative
    source = path.read_text(encoding="utf-8")
    count = source.count(old)
    if count != 1:
        raise SystemExit(f"Patch precondition failed for {relative}: expected 1 match, found {count}")
    path.write_text(source.replace(old, new, 1), encoding="utf-8")
    print(f"Patched {relative}")


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
replace_once("command/MFAdvancedCommands.java",
    "    private static void addCoins(Inventory inventory, Item item, long amount) {\n        long remaining = amount;",
    "    private static void addCoins(Inventory inventory, Item item, long amount) {\n        long remaining = amount;\n        int defaultStackLimit = new ItemStack(item).getMaxStackSize();")
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

print("All CI compile patches applied successfully.")
