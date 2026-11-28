#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
python tools/validate_assets.py
tools/run_domain_tests.sh
tools/run_saveddata_stub_tests.sh
if ! command -v gradle >/dev/null 2>&1; then
  echo "Validaciones locales superadas. Gradle 8.12+ no está instalado; no se pudo ejecutar clean test build." >&2
  exit 2
fi
gradle clean test build
