#!/usr/bin/env bash
set -euo pipefail

EXPECTED_MOD_VERSION="0.9.1-alpha"
JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -n 1)"
if [[ -z "$JAR_FILE" ]]; then
  echo "::error::No compiled mod JAR was produced."
  exit 1
fi

unzip -tq "$JAR_FILE"
MOD_TOML="$(unzip -p "$JAR_FILE" META-INF/neoforge.mods.toml)"
if [[ -z "$MOD_TOML" ]]; then
  echo "::error::The JAR is missing META-INF/neoforge.mods.toml and cannot be discovered by NeoForge."
  exit 1
fi
grep -Fq 'modId="medieval_factions"' <<< "$MOD_TOML"
grep -Fq 'modId="neoforge"' <<< "$MOD_TOML"
grep -Fq 'modId="minecraft"' <<< "$MOD_TOML"
grep -Fq 'modId="geckolib"' <<< "$MOD_TOML"

if ! grep -Fxq "version=\"$EXPECTED_MOD_VERSION\"" <<< "$MOD_TOML"; then
  ACTUAL_MOD_VERSION="$(sed -n 's/^version="\([^"]*\)"$/\1/p' <<< "$MOD_TOML" | head -n 1)"
  echo "::error::Expected mod version $EXPECTED_MOD_VERSION but JAR contains ${ACTUAL_MOD_VERSION:-<missing>}."
  exit 1
fi

echo "Verified MEDIEVAL FACTIONS $EXPECTED_MOD_VERSION descriptor: $JAR_FILE"
sha256sum "$JAR_FILE"
