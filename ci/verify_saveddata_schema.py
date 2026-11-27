#!/usr/bin/env python3
"""Static regression checks for the versioned SavedData load/save contract."""
from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: verify_saveddata_schema.py PROJECT_DIR")

root = Path(sys.argv[1]).resolve()
data = root / "src/main/java/com/medievalfactions/data"
checks = {
    "world schema checked during load": (
        data / "MFWorldSavedData.java",
        "MFSavedDataSchema.requireSupported(tag, SCHEMA_VERSION, DATA_ID);",
    ),
    "world schema written during save": (
        data / "MFWorldSavedData.java",
        'tag.putInt("schema_version", SCHEMA_VERSION);',
    ),
    "faction schema checked during load": (
        data / "MFFactionSavedData.java",
        "MFSavedDataSchema.requireSupported(tag, SCHEMA_VERSION, DATA_ID);",
    ),
    "faction schema written during save": (
        data / "MFFactionSavedData.java",
        'tag.putInt("schema_version", SCHEMA_VERSION);',
    ),
    "advanced schema checked during load": (
        data / "MFAdvancedSavedData.java",
        "MFSavedDataSchema.requireSupported(tag, SCHEMA_VERSION, DATA_ID);",
    ),
    "advanced schema written during save": (
        data / "MFAdvancedSavedData.java",
        'tag.putInt("schema_version", SCHEMA_VERSION);',
    ),
}
failed = []
for label, (path, marker) in checks.items():
    if not path.is_file() or marker not in path.read_text(encoding="utf-8"):
        failed.append(label)

helper = data / "MFSavedDataSchema.java"
if not helper.is_file():
    failed.append("shared schema guard exists")
else:
    helper_text = helper.read_text(encoding="utf-8")
    for label, marker in (
        ("legacy unversioned data accepted as schema zero", " ? tag.getInt(VERSION_KEY) : 0"),
        ("future schema versions rejected", "storedVersion > currentVersion"),
        ("invalid schema types rejected", "Invalid schema_version type"),
    ):
        if marker not in helper_text:
            failed.append(label)

test = root / "tools/SavedDataPersistenceSelfTest.java"
if not test.is_file():
    failed.append("SavedData schema regression tests installed")
else:
    test_text = test.read_text(encoding="utf-8")
    for marker in ("faction future schema", "world future schema", "legacy save remains readable"):
        if marker not in test_text:
            failed.append("SavedData test marker: " + marker)

if failed:
    raise SystemExit("SavedData schema verification failed: " + ", ".join(failed))
print(f"SavedData schema compatibility: {len(checks) + 4}/{len(checks) + 4} checks passed.")
