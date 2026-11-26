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
: > "$LOG_FILE"

# Run the dedicated server in its own process group. Stop it as soon as the
# normal "Done (...)" startup marker appears, rather than waiting for a server
# that is meant to remain open indefinitely.
setsid gradle --no-daemon runServer > "$LOG_FILE" 2>&1 &
SERVER_PID=$!
READY=0

for _ in $(seq 1 150); do
  if grep -Fq 'Done (' "$LOG_FILE"; then
    READY=1
    break
  fi
  if ! kill -0 "$SERVER_PID" 2>/dev/null; then
    break
  fi
  sleep 1
done

if [ "$READY" -eq 1 ]; then
  echo "NeoForge dedicated server reached startup-ready marker."
  kill -INT -- "-$SERVER_PID" 2>/dev/null || true
  for _ in $(seq 1 10); do
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then break; fi
    sleep 1
  done
  kill -TERM -- "-$SERVER_PID" 2>/dev/null || true
  sleep 2
  kill -KILL -- "-$SERVER_PID" 2>/dev/null || true
  wait "$SERVER_PID" 2>/dev/null || true
  cat "$LOG_FILE"
  echo "NeoForge dedicated-server startup smoke test passed."
  exit 0
fi

kill -TERM -- "-$SERVER_PID" 2>/dev/null || true
sleep 3
kill -KILL -- "-$SERVER_PID" 2>/dev/null || true
wait "$SERVER_PID" 2>/dev/null || true
cat "$LOG_FILE"
echo "::error::NeoForge dedicated server did not reach the startup-ready marker."
exit 1
