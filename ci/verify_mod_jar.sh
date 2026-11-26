#!/usr/bin/env bash
set -euo pipefail

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

echo "Verified loadable mod descriptor: $JAR_FILE"
sha256sum "$JAR_FILE"
