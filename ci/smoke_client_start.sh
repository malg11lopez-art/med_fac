#!/usr/bin/env bash
set -euo pipefail

if ! command -v xvfb-run >/dev/null 2>&1; then
  echo "::error::xvfb-run is required for the headless Minecraft client smoke test."
  exit 1
fi

LOG_FILE="${RUNNER_TEMP:-/tmp}/medieval-factions-client-smoke.log"
set +e
ALSOFT_DRIVERS=null ALSOFT_LOGLEVEL=0 timeout --signal=INT --kill-after=15s 180s xvfb-run -a gradle --no-daemon runClient > "$LOG_FILE" 2>&1
STATUS=$?
set -e
cat "$LOG_FILE"

# The client is intentionally interrupted after it has loaded the mod and client resources.
if ! grep -Fq 'Sound engine started' "$LOG_FILE"; then
  echo "::error::Minecraft client did not initialize its sound engine (exit code $STATUS)."
  exit 1
fi
if ! grep -Fq 'Reloading ResourceManager:' "$LOG_FILE"; then
  echo "::error::Minecraft client did not reload its resource manager (exit code $STATUS)."
  exit 1
fi
if ! grep -Fq 'Registered MEDIEVAL FACTIONS player skin layer on 2 renderer variants; 86 local skins available' "$LOG_FILE"; then
  echo "::error::Player skin layers were not registered for both vanilla player models."
  exit 1
fi
if grep -Eiq 'geckolib.*(Failed to load|Unable to find animation|Could not parse animation|Invalid animation json)' "$LOG_FILE"; then
  echo "::error::GeckoLib reported an animation resource/clip loading failure."
  exit 1
fi
if grep -Eiq 'Failed to load (model|texture|sound)|Unable to load sound|Could not load texture|Missing textures in model medieval_factions:|Missing sound for event: medieval_factions:|Animation file.*(missing|not found)' "$LOG_FILE"; then
  echo "::error::Client reported a resource/animation/sound loading failure."
  exit 1
fi

echo "Minecraft client resource-load smoke test passed."
