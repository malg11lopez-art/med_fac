#!/usr/bin/env bash
set -euo pipefail

if ! command -v xvfb-run >/dev/null 2>&1; then
  echo "::error::xvfb-run is required for the headless Minecraft client smoke test."
  exit 1
fi

LOG_FILE="${RUNNER_TEMP:-/tmp}/medieval-factions-client-smoke.log"
: > "$LOG_FILE"

# Run Minecraft in an isolated process group. Stop immediately after the sound
# engine, resource reload and both player renderer skin layers are confirmed.
setsid env ALSOFT_DRIVERS=null ALSOFT_LOGLEVEL=0 xvfb-run -a gradle --no-daemon runClient > "$LOG_FILE" 2>&1 &
CLIENT_PID=$!
READY=0

for _ in $(seq 1 150); do
  if grep -Fq 'Sound engine started' "$LOG_FILE" \
      && grep -Fq 'Reloading ResourceManager:' "$LOG_FILE" \
      && grep -Fq 'Registered MEDIEVAL FACTIONS player skin layer on 2 renderer variants; 86 local skins available' "$LOG_FILE"; then
    READY=1
    break
  fi
  if ! kill -0 "$CLIENT_PID" 2>/dev/null; then
    break
  fi
  sleep 1
done

stop_client() {
  kill -INT -- "-$CLIENT_PID" 2>/dev/null || true
  for _ in $(seq 1 10); do
    if ! kill -0 -- "-$CLIENT_PID" 2>/dev/null; then
      break
    fi
    sleep 1
  done
  kill -TERM -- "-$CLIENT_PID" 2>/dev/null || true
  sleep 2
  kill -KILL -- "-$CLIENT_PID" 2>/dev/null || true
  wait "$CLIENT_PID" 2>/dev/null || true
}

if [ "$READY" -ne 1 ]; then
  stop_client
  cat "$LOG_FILE"
  echo "::error::Minecraft client did not reach all required startup/resource/skin markers within 150 seconds."
  exit 1
fi

echo "Minecraft client reached sound, resource reload and skin-renderer ready markers."
stop_client
cat "$LOG_FILE"

if grep -Eiq 'geckolib.*(Failed to load|Unable to find animation|Could not parse animation|Invalid animation json)' "$LOG_FILE"; then
  echo "::error::GeckoLib reported an animation resource/clip loading failure."
  exit 1
fi
if grep -Eiq 'Failed to load (model|texture|sound)|Unable to load sound|Could not load texture|Missing textures in model medieval_factions:|Missing sound for event: medieval_factions:|Animation file.*(missing|not found)' "$LOG_FILE"; then
  echo "::error::Client reported a resource/animation/sound loading failure."
  exit 1
fi

echo "Minecraft client resource-load smoke test passed."
