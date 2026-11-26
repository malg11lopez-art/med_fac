#!/usr/bin/env python3
"""Fail CI if any registered MEDIEVAL FACTIONS SoundEvent is absent from the in-game preview command."""
from pathlib import Path
import re
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: verify_sound_preview.py PROJECT_DIR")
project = Path(sys.argv[1]).resolve()
java_root = project / "src/main/java/com/medievalfactions"
sounds_file = java_root / "registry/MFSounds.java"
preview_file = java_root / "registry/MFSoundPreview.java"
commands_file = java_root / "command/MFAdvancedCommands.java"
for path in (sounds_file, preview_file, commands_file):
    if not path.is_file():
        raise SystemExit(f"Required sound-preview file is missing: {path}")

registered = set(re.findall(
    r"DeferredHolder<SoundEvent,\\s*SoundEvent>\\s+(\\w+)\\s*=\\s*SOUND_EVENTS\\.register\\(",
    sounds_file.read_text(encoding="utf-8")
))
preview = set(re.findall(
    r'Map\\.entry\\("[a-z0-9_]+",\\s*MFSounds\\.(\\w+)\\)',
    preview_file.read_text(encoding="utf-8")
))
command_text = commands_file.read_text(encoding="utf-8")
if len(registered) != 75:
    raise SystemExit(f"Expected 75 registered SoundEvents, found {len(registered)}")
missing = sorted(registered - preview)
extra = sorted(preview - registered)
if missing or extra:
    raise SystemExit(f"Sound preview mismatch; missing={missing}, extra={extra}")
if "root.then(soundCommands());" not in command_text or "MFSoundPreview.find(id)" not in command_text:
    raise SystemExit("Sound preview command is not connected to the advanced command tree")
print(f"Sound preview registry: {len(preview)}/{len(registered)} events available to operators.")
