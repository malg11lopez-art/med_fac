#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${TMPDIR:-/tmp}/mf-saveddata-stub-classes"
rm -rf "$OUT"
mkdir -p "$OUT"
mapfile -t STUBS < <(find "$ROOT/tools/test_support/src" -name '*.java' -print | sort)
mapfile -t DOMAIN < <(find "$ROOT/src/main/java/com/medievalfactions/domain" -name '*.java' -print | sort)
javac -encoding UTF-8 -d "$OUT" \
  "${STUBS[@]}" \
  "$ROOT/src/main/java/com/medievalfactions/economy/MFCurrency.java" \
  "${DOMAIN[@]}" \
  "$ROOT/src/main/java/com/medievalfactions/data/MFSavedDataSchema.java" \
  "$ROOT/src/main/java/com/medievalfactions/data/MFFactionSavedData.java" \
  "$ROOT/src/main/java/com/medievalfactions/data/MFWorldSavedData.java" \
  "$ROOT/tools/SavedDataPersistenceSelfTest.java"
java -ea -cp "$OUT" SavedDataPersistenceSelfTest
