#!/usr/bin/env bash
set -euo pipefail

if ! command -v xvfb-run >/dev/null 2>&1; then
  echo "::error::xvfb-run is required for the multiplayer headless smoke test."
  exit 1
fi

SERVER_LOG="${RUNNER_TEMP:-/tmp}/medieval-factions-network-server.log"
CLIENT_LOG="${RUNNER_TEMP:-/tmp}/medieval-factions-network-client.log"
PORT=25567
: > "$SERVER_LOG"
: > "$CLIENT_LOG"

mkdir -p run/server
printf 'eula=true\n' > run/server/eula.txt
cat > run/server/server.properties <<PROPERTIES
motd=MEDIEVAL FACTIONS network smoke test
level-name=mf-network-smoke
server-port=$PORT
server-ip=127.0.0.1
max-players=2
online-mode=false
enable-command-block=true
PROPERTIES

SERVER_PID=""
CLIENT_PID=""
stop_group() {
  local pid="$1"
  [ -n "$pid" ] || return 0
  kill -INT -- "-$pid" 2>/dev/null || true
  for _ in $(seq 1 10); do
    kill -0 -- "-$pid" 2>/dev/null || break
    sleep 1
  done
  kill -TERM -- "-$pid" 2>/dev/null || true
  sleep 1
  kill -KILL -- "-$pid" 2>/dev/null || true
  wait "$pid" 2>/dev/null || true
}
cleanup() {
  stop_group "$CLIENT_PID"
  stop_group "$SERVER_PID"
}
trap cleanup EXIT INT TERM

# Boot a real dedicated server and wait for its network-ready marker.
setsid gradle --no-daemon runServer > "$SERVER_LOG" 2>&1 &
SERVER_PID=$!
SERVER_READY=0
for _ in $(seq 1 150); do
  if grep -Fq 'Done (' "$SERVER_LOG"; then
    SERVER_READY=1
    break
  fi
  if ! kill -0 "$SERVER_PID" 2>/dev/null; then break; fi
  sleep 1
done
if [ "$SERVER_READY" -ne 1 ]; then
  cat "$SERVER_LOG"
  echo "::error::Dedicated server did not become ready for the network join test."
  exit 1
fi

# Launch a real headless client and join the local dedicated server.
setsid env ALSOFT_DRIVERS=null ALSOFT_LOGLEVEL=0 xvfb-run -a gradle --no-daemon runClient --args="--quickPlayMultiplayer 127.0.0.1:$PORT" > "$CLIENT_LOG" 2>&1 &
CLIENT_PID=$!
JOINED=0
SKIN_ACCEPTED=0
SKIN_RECEIVED=0
CLIENT_READY=0

for _ in $(seq 1 180); do
  grep -Fq 'Sound engine started' "$CLIENT_LOG" && CLIENT_READY=1 || true
  grep -Fq 'joined the game' "$SERVER_LOG" && JOINED=1 || true
  grep -Fq 'MF_SKIN_SELECTION_ACCEPTED' "$SERVER_LOG" && SKIN_ACCEPTED=1 || true
  grep -Fq 'MF_SKIN_SYNC_RECEIVED' "$CLIENT_LOG" && SKIN_RECEIVED=1 || true
  if [ "$JOINED" -eq 1 ] && [ "$SKIN_ACCEPTED" -eq 1 ] && [ "$SKIN_RECEIVED" -eq 1 ]; then
    break
  fi
  if ! kill -0 "$CLIENT_PID" 2>/dev/null; then break; fi
  sleep 1
done

stop_group "$CLIENT_PID"
CLIENT_PID=""
stop_group "$SERVER_PID"
SERVER_PID=""

echo '--- Dedicated server network log ---'
cat "$SERVER_LOG"
echo '--- Headless client network log ---'
cat "$CLIENT_LOG"

if [ "$CLIENT_READY" -ne 1 ]; then
  echo "::error::Headless client did not initialize its sound engine."
  exit 1
fi
if [ "$JOINED" -ne 1 ]; then
  echo "::error::Headless client did not join the dedicated server."
  exit 1
fi
if [ "$SKIN_ACCEPTED" -ne 1 ]; then
  echo "::error::Server did not accept the client's skin-selection payload."
  exit 1
fi
if [ "$SKIN_RECEIVED" -ne 1 ]; then
  echo "::error::Client did not receive the server's skin-sync payload."
  exit 1
fi

echo "NeoForge network smoke test passed: client joined, server accepted skin selection and client received skin sync."
