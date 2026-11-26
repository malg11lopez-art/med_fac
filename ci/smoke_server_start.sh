#!/usr/bin/env bash
set -euo pipefail

mkdir -p run/server
printf 'eula=true\n' > run/server/eula.txt
cat > run/server/server.properties <<'PROPERTIES'
motd=MEDIEVAL FACTIONS dedicated server smoke test
level-name=mf-ci-smoke
server-port=25565
max-players=1
online-mode=false
enable-command-block=true
PROPERTIES
LOG_FILE="${RUNNER_TEMP:-/tmp}/medieval-factions-server-smoke.log"

set +e
timeout --signal=INT --kill-after=15s 120s gradle --no-daemon runServer > "$LOG_FILE" 2>&1
STATUS=$?
set -e

cat "$LOG_FILE"

# A healthy dedicated server reaches its normal "Done (...)" startup marker.
if ! grep -Fq 'Done (' "$LOG_FILE"; then
  echo "::error::NeoForge dedicated server did not reach the startup-ready marker (exit code $STATUS)."
  exit 1
fi

echo "NeoForge dedicated-server startup smoke test passed."
