package com.medievalfactions.data;

import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.economy.MFCurrency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Persistent faction identity, primary race, two-player membership and copper treasury. */
public final class MFFactionSavedData extends SavedData {
    private static final String DATA_ID = "medieval_factions_factions";
    private static final int SCHEMA_VERSION = 2;
    private static final Factory<MFFactionSavedData> FACTORY = new Factory<>(MFFactionSavedData::new, MFFactionSavedData::load);
    public static final int MAX_PLAYERS_PER_FACTION = 2;
    private final Map<UUID, FactionRecord> factions = new LinkedHashMap<>();

    public static MFFactionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    /** Admin-created faction, not initially assigned to a player. */
    public synchronized Optional<FactionRecord> createFaction(String rawName) {
        return createFaction(rawName, null, null);
    }

    /** Player-created faction with the founder as first member and a locked primary race family. */
    public synchronized Optional<FactionRecord> createFaction(String rawName, UUID founder, MFGameCatalog.RaceFamily primaryRace) {
        if (rawName == null || (founder == null) != (primaryRace == null)) return Optional.empty();
        String name = rawName.trim();
        if (name.length() < 3 || name.length() > 32) return Optional.empty();
        boolean duplicate = factions.values().stream().anyMatch(f -> f.name().equalsIgnoreCase(name));
        if (duplicate || (founder != null && isPlayerInAnyFaction(founder))) return Optional.empty();
        Set<UUID> members = founder == null ? Set.of() : Set.of(founder);
        FactionRecord record = new FactionRecord(UUID.randomUUID(), name, 0L, founder, members, primaryRace);
        factions.put(record.id(), record);
        setDirty();
        return Optional.of(record);
    }

    public synchronized boolean joinFaction(UUID factionId, UUID playerId) {
        FactionRecord faction = factions.get(factionId);
        if (faction == null || playerId == null || faction.playerMembers().contains(playerId)
                || faction.playerMembers().size() >= MAX_PLAYERS_PER_FACTION || isPlayerInAnyFaction(playerId)) return false;
        LinkedHashSet<UUID> members = new LinkedHashSet<>(faction.playerMembers());
        members.add(playerId);
        factions.put(factionId, faction.withMembers(members));
        setDirty();
        return true;
    }

    public synchronized boolean leaveFaction(UUID playerId) {
        if (playerId == null) return false;
        for (FactionRecord faction : factions.values()) {
            if (!faction.playerMembers().contains(playerId)) continue;
            LinkedHashSet<UUID> members = new LinkedHashSet<>(faction.playerMembers());
            members.remove(playerId);
            UUID leader = playerId.equals(faction.leaderPlayerId()) ? null : faction.leaderPlayerId();
            factions.put(faction.id(), faction.withMembersAndLeader(members, leader));
            setDirty();
            return true;
        }
        return false;
    }

    public synchronized Optional<FactionRecord> factionForPlayer(UUID playerId) {
        return factions.values().stream().filter(f -> f.playerMembers().contains(playerId)).findFirst();
    }

    public synchronized boolean isMember(UUID factionId, UUID playerId) {
        FactionRecord faction = factions.get(factionId);
        return faction != null && faction.playerMembers().contains(playerId);
    }

    private boolean isPlayerInAnyFaction(UUID playerId) {
        return factions.values().stream().anyMatch(f -> f.playerMembers().contains(playerId));
    }

    public synchronized Optional<FactionRecord> getFaction(UUID id) { return Optional.ofNullable(factions.get(id)); }
    public synchronized Map<UUID, FactionRecord> allFactions() { return Collections.unmodifiableMap(new LinkedHashMap<>(factions)); }

    /** Adds/subtracts copper atomically; a treasury can never become negative or overflow. */
    public synchronized boolean adjustTreasury(UUID id, long copperDelta) {
        FactionRecord old = factions.get(id);
        if (old == null) return false;
        var adjusted = MFCurrency.adjust(old.treasuryCopper(), copperDelta);
        if (adjusted.isEmpty()) return false;
        factions.put(id, old.withTreasury(adjusted.getAsLong()));
        setDirty();
        return true;
    }

    /** Double-entry transfer: both updates happen under the same monitor or neither happens. */
    public synchronized boolean transfer(UUID from, UUID to, long copper) {
        if (from == null || to == null || copper <= 0 || from.equals(to)) return false;
        FactionRecord source = factions.get(from), target = factions.get(to);
        if (source == null || target == null || source.treasuryCopper() < copper) return false;
        var result = MFCurrency.transfer(source.treasuryCopper(), target.treasuryCopper(), copper);
        if (result.isEmpty()) return false;
        factions.put(from, source.withTreasury(result.get().sourceCopper()));
        factions.put(to, target.withTreasury(result.get().targetCopper()));
        setDirty();
        return true;
    }

    public static MFFactionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MFSavedDataSchema.requireSupported(tag, SCHEMA_VERSION, DATA_ID);
        MFFactionSavedData data = new MFFactionSavedData();
        ListTag list = tag.getList("factions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            try {
                UUID id = entry.getUUID("id");
                String name = entry.getString("name");
                long treasury = Math.max(0L, entry.getLong("treasury_copper"));
                Set<UUID> members = new LinkedHashSet<>();
                ListTag memberList = entry.getList("players", Tag.TAG_COMPOUND);
                for (int m = 0; m < memberList.size(); m++) members.add(memberList.getCompound(m).getUUID("id"));
                UUID leader = entry.hasUUID("leader") ? entry.getUUID("leader") : null;
                MFGameCatalog.RaceFamily primaryRace = entry.contains("primary_race", Tag.TAG_STRING)
                        ? MFGameCatalog.parseEnum(MFGameCatalog.RaceFamily.class, entry.getString("primary_race")) : null;
                if (!name.isBlank() && name.length() <= 32 && members.size() <= MAX_PLAYERS_PER_FACTION) {
                    if (leader != null && !members.contains(leader)) leader = null;
                    data.factions.put(id, new FactionRecord(id, name, treasury, leader, members, primaryRace));
                }
            } catch (Exception ignored) {
                // Corrupt single records are skipped rather than preventing the entire world from loading.
            }
        }
        return data;
    }

    @Override public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (FactionRecord faction : factions.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", faction.id());
            entry.putString("name", faction.name());
            entry.putLong("treasury_copper", faction.treasuryCopper());
            if (faction.leaderPlayerId() != null) entry.putUUID("leader", faction.leaderPlayerId());
            if (faction.primaryRace() != null) entry.putString("primary_race", faction.primaryRace().name());
            ListTag members = new ListTag();
            for (UUID playerId : faction.playerMembers()) { CompoundTag member = new CompoundTag(); member.putUUID("id", playerId); members.add(member); }
            entry.put("players", members);
            list.add(entry);
        }
        tag.put("factions", list);
        tag.putInt("schema_version", SCHEMA_VERSION);
        return tag;
    }

    public record FactionRecord(UUID id, String name, long treasuryCopper, UUID leaderPlayerId,
                                Set<UUID> playerMembers, MFGameCatalog.RaceFamily primaryRace) {
        public FactionRecord {
            if (id == null || name == null || name.isBlank() || treasuryCopper < 0 || playerMembers == null
                    || playerMembers.size() > MAX_PLAYERS_PER_FACTION) throw new IllegalArgumentException("Invalid faction record");
            playerMembers = Collections.unmodifiableSet(new LinkedHashSet<>(playerMembers));
        }
        FactionRecord withTreasury(long amount) { return new FactionRecord(id, name, amount, leaderPlayerId, playerMembers, primaryRace); }
        FactionRecord withMembers(Set<UUID> members) { return new FactionRecord(id, name, treasuryCopper, leaderPlayerId, members, primaryRace); }
        FactionRecord withMembersAndLeader(Set<UUID> members, UUID leader) { return new FactionRecord(id, name, treasuryCopper, leader, members, primaryRace); }
    }
}
