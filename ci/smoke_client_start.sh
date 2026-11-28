#!/usr/bin/env bash
set -euo pipefail

if ! command -v xvfb-run >/dev/null 2>&1; then
  echo "::error::xvfb-run is required for the headless Minecraft client smoke test."
  exit 1
fi

LOG_FILE="${RUNNER_TEMP:-/tmp}/medieval-factions-client-smoke.log"
set +e
timeout --signal=INT --kill-after=15s 180s xvfb-run -a gradle --no-daemon runClient > "$LOG_FILE" 2>&1
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
if grep -Eiq 'Failed to load (model|texture|sound)|Unable to load sound|Could not load texture|Animation file.*(missing|not found)' "$LOG_FILE"; then
  echo "::error::Client reported a resource/animation/sound loading failure."
  exit 1
fi

echo "Minecraft client resource-load smoke test passed."
