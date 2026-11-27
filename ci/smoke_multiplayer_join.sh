#!/usr/bin/env bash
set -euo pipefail

if ! command -v xvfb-run >/dev/null 2>&1; then
  echo "::error::xvfb-run is required for the multiplayer headless smoke test."
  exit 1
fi

SERVER_LOG="${RUNNER_TEMP:-/tmp}/medieval-factions-network-server.log"
CLIENT_LOG="${RUNNER_TEMP:-/tmp}/medieval-factions-network-client.log"
PROJECT_DIR="$PWD"
BUILD_FILE="$PROJECT_DIR/build.gradle"
BUILD_BACKUP="${RUNNER_TEMP:-/tmp}/medieval-factions-network-build.gradle.bak"
PORT=25567
: > "$SERVER_LOG"
: > "$CLIENT_LOG"

if [ ! -f "$BUILD_FILE" ]; then
  echo "::error::build.gradle not found in the network smoke-test working directory."
  exit 1
fi
cp "$BUILD_FILE" "$BUILD_BACKUP"

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
  if [ -f "$BUILD_BACKUP" ]; then
    cp "$BUILD_BACKUP" "$BUILD_FILE"
    rm -f "$BUILD_BACKUP"
  fi
}
trap cleanup EXIT INT TERM

# ModDevGradle's run profile is the supported place for programArgument.
# Do not use Gradle --args or JavaExec args: these are treated as main-class
# arguments by the DevLaunch wrapper rather than Minecraft program arguments.
python - "$BUILD_FILE" "$PORT" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
port = int(sys.argv[2])
source = path.read_text(encoding="utf-8")
client_anchor = "        client {\n            client()\n"
server_anchor = "        server {\n            server()\n"
client_insert = (
    "        client {\n"
    "            client()\n"
    "            gameDirectory = project.file('run/network-client')\n"
    f"            systemProperty 'mf.ci.autoJoin', '127.0.0.1:{port}'\n"
)
server_insert = (
    "        server {\n"
    "            server()\n"
    "            gameDirectory = project.file('run/network-server')\n"
)
if source.count(client_anchor) != 1 or source.count(server_anchor) != 1:
    raise SystemExit("Could not identify unique ModDevGradle client/server run blocks")
source = source.replace(client_anchor, client_insert, 1)
source = source.replace(server_anchor, server_insert, 1)
path.write_text(source, encoding="utf-8")
print("Configured isolated client/server run directories and client quick-play program arguments.")
PY

SERVER_DIR="$PROJECT_DIR/run/network-server"
mkdir -p "$SERVER_DIR" "$PROJECT_DIR/run/network-client"
printf 'eula=true\n' > "$SERVER_DIR/eula.txt"
cat > "$SERVER_DIR/server.properties" <<PROPERTIES
motd=MEDIEVAL FACTIONS network smoke test
level-name=mf-network-smoke
server-port=$PORT
server-ip=127.0.0.1
max-players=2
online-mode=false
enable-command-block=true
PROPERTIES

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
if ! grep -Fq "Starting Minecraft server on 127.0.0.1:$PORT" "$SERVER_LOG" \
    && ! grep -Fq "Starting Minecraft server on /127.0.0.1:$PORT" "$SERVER_LOG" \
    && ! grep -Fq "Starting Minecraft server on *:$PORT" "$SERVER_LOG"; then
  cat "$SERVER_LOG"
  echo "::error::Dedicated server did not bind to configured port $PORT."
  exit 1
fi

setsid env ALSOFT_DRIVERS=null ALSOFT_LOGLEVEL=0 xvfb-run -a gradle --no-daemon runClient > "$CLIENT_LOG" 2>&1 &
CLIENT_PID=$!
JOINED=0
SKIN_ACCEPTED=0
SKIN_RECEIVED=0
CLIENT_READY=0
AUTOJOIN_TRIGGERED=0

for _ in $(seq 1 180); do
  grep -Fq 'Sound engine started' "$CLIENT_LOG" && CLIENT_READY=1 || true
  grep -Fq 'MF_CI_AUTOJOIN_CONNECT' "$CLIENT_LOG" && AUTOJOIN_TRIGGERED=1 || true
  grep -Eq 'joined the game|logged in with entity id' "$SERVER_LOG" && JOINED=1 || true
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

echo '--- Dedicated server log ---'
cat "$SERVER_LOG"
echo '--- Headless client log ---'
cat "$CLIENT_LOG"

if [ "$CLIENT_READY" -ne 1 ]; then
  echo "::error::Headless client did not initialize its sound engine."
  exit 1
fi
if [ "$AUTOJOIN_TRIGGERED" -ne 1 ]; then
  echo "::error::The CI-gated client connection probe did not run."
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
