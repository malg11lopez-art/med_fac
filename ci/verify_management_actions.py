#!/usr/bin/env python3
"""Verify that the in-game management GUI exposes the guarded resident AI planner."""
from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: verify_management_actions.py PROJECT_DIR")

root = Path(sys.argv[1]).resolve()
java = root / "src/main/java/com/medievalfactions"
screen = java / "client/MFMainScreen.java"
commands = java / "command/MFAdvancedCommands.java"

missing = [str(p.relative_to(root)) for p in (screen, commands) if not p.is_file()]
if missing:
    raise SystemExit("Missing management GUI command components: " + ", ".join(missing))

ui = screen.read_text(encoding="utf-8")
cmd = commands.read_text(encoding="utf-8")
checks = {
    "management screen has resident planner button": 'Component.literal("Planificar IA")' in ui,
    "button invokes resident planner callback": "button -> replanResidents()" in ui,
    "callback sends the canonical server command": 'sendCommand("mf advanced npc plan")' in ui,
    "feedback confirms request submission": "recalculando horarios y trabajo de habitantes" in ui,
    "server registers npc plan command": 'Commands.literal("plan").executes(ctx -> {' in cmd[cmd.find("private static LiteralArgumentBuilder<CommandSourceStack> npcCommands()"):],
    "server restricts npc management to operators": 'Commands.literal("npc").requires(source -> source.hasPermission(2))' in cmd,
    "planner updates population and scheduled plans": "advanced.planNpcPopulation(world, day)" in cmd and "advanced.updateScheduledNpcPlans(world, minute)" in cmd,
}
failed = [label for label, ok in checks.items() if not ok]
if failed:
    raise SystemExit("Management GUI action verification failed: " + ", ".join(failed))
print(f"Management GUI resident planner action: {len(checks)}/{len(checks)} checks passed.")
