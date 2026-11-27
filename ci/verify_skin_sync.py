#!/usr/bin/env python3
"""Static integration gate for authoritative multiplayer player-skin synchronization."""
from pathlib import Path
import re
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: verify_skin_sync.py PROJECT_DIR")
project = Path(sys.argv[1]).resolve()
java = project / "src/main/java/com/medievalfactions"
assets = project / "src/main/resources/assets/medieval_factions/textures/player"
files = {
    "rules": java / "network/MFPlayerSkinRules.java",
    "selection_payload": java / "network/MFPlayerSkinSelectionPayload.java",
    "sync_payload": java / "network/MFPlayerSkinSyncPayload.java",
    "server_network": java / "network/MFPlayerSkinServerNetwork.java",
    "server_events": java / "network/MFPlayerSkinServerEvents.java",
    "client": java / "client/MFPlayerSkinClient.java",
    "client_input": java / "client/MFPlayerSkinInput.java",
    "client_layer": java / "client/MFPlayerSkinLayer.java",
    "client_events": java / "client/MFClientEvents.java",
}
missing = [name for name, path in files.items() if not path.is_file()]
if missing:
    raise SystemExit(f"Missing skin-sync components: {missing}")
code = {name: path.read_text(encoding="utf-8") for name, path in files.items()}
skins = sorted(assets.glob("*.png")) if assets.is_dir() else []
count_match = re.search(r"SKIN_COUNT\s*=\s*(\d+)", code["rules"])
if not count_match:
    raise SystemExit("MFPlayerSkinRules does not define a numeric SKIN_COUNT")
expected = int(count_match.group(1))
if len(skins) != expected:
    raise SystemExit(f"Skin index catalogue mismatch: rules={expected}, PNG assets={len(skins)}")
checks = {
    "validated server-bound packet": "playToServer" in code["server_network"] and "isValidIndex(index)" in code["server_network"],
    "server-thread scheduling": "context.enqueueWork" in code["server_network"],
    "server registers clientbound skin sync": "playToClient(MFPlayerSkinSyncPayload.TYPE" in code["server_network"],
    "server registers clientbound management snapshot": "playToClient(MFManagementSnapshotPayload.TYPE" in code["server_network"],
    "server persistence": "getPersistentData()" in code["server_network"] and "putInt(MFPlayerSkinRules.DATA_KEY, index)" in code["server_network"],
    "broadcast to connected clients": "PacketDistributor.sendToAllPlayers" in code["server_network"],
    "client-bound packet registration": "playToClient" in code["client_events"],
    "client-thread scheduling": "context.enqueueWork" in code["client_events"],
    "local preference sent to server": "PacketDistributor.sendToServer" in code["client"],
    "remote-player render selection": "skinFor(player)" in code["client_layer"],
    "remote cache exists": "REMOTE_SKINS" in code["client"],
    "connection-aware first sync": "lastSyncedConnection" in code["client"] and "synchronizeWithServerIfNeeded" in code["client_input"],
    "join synchronization": "PlayerLoggedInEvent" in code["server_events"] and "PacketDistributor.sendToPlayer" in code["server_events"],
    "logout cache cleanup": "PlayerLoggedOutEvent" in code["server_events"] and "MFPlayerSkinSyncPayload(player.getUUID(), -1)" in code["server_events"],
    "respawn/death persistence": "PlayerRespawnEvent" in code["server_events"] and "PlayerEvent.Clone" in code["server_events"],
    "local disk persistence": "medieval_factions_skin.txt" in code["client"] and "saveSelection()" in code["client"],
}
failed = [name for name, passed in checks.items() if not passed]
if failed:
    raise SystemExit("Player skin synchronization checks failed: " + ", ".join(failed))
print(f"Player skin sync: {len(skins)}/{expected} textures; {len(checks)}/{len(checks)} integration checks passed.")
