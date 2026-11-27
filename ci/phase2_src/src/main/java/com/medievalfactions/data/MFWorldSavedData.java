package com.medievalfactions.data;

import com.medievalfactions.domain.MFCalendar;
import com.medievalfactions.domain.MFCharacterProfile;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFDiplomacyRelation;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.domain.MFRaceBiology;
import com.medievalfactions.domain.MFSettlementRules;
import com.medievalfactions.domain.MFSettlementState;
import com.medievalfactions.domain.MFSettlementSimulation;
import com.medievalfactions.domain.MFTradeEngine;
import com.medievalfactions.domain.MFRecipeCatalog;
import com.medievalfactions.domain.MFWorkOrderState;
import com.medievalfactions.domain.MFCombatRewardRules;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.medievalfactions.domain.MFGameCatalog.*;

/**
 * Versioned, overworld-global storage for the authoritative domain state.
 * All mutation paths are synchronized and mark SavedData dirty.
 */
public final class MFWorldSavedData extends SavedData {
    private static final String DATA_ID = "medieval_factions_world_v1";
    private static final int SCHEMA_VERSION = 2;
    private static final Factory<MFWorldSavedData> FACTORY = new Factory<>(MFWorldSavedData::new, MFWorldSavedData::load);

    private final Map<UUID, MFCharacterProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, MFSettlementState> settlements = new LinkedHashMap<>();
    private final Map<UUID, MFCitizenState> citizens = new LinkedHashMap<>();
    private final Map<String, MFDiplomacyRelation> diplomacy = new LinkedHashMap<>();
    private final Map<UUID, MFWorkOrderState> workOrders = new LinkedHashMap<>();
    private final Map<String, Integer> dailyCreatureKills = new LinkedHashMap<>();
    private final Map<UUID, Long> dailyCombatXp = new LinkedHashMap<>();
    private long combatRewardDay = -1L;
    private long simulatedDays;
    private long lastMinecraftDay = -1L;

    public static MFWorldSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public synchronized Optional<MFCharacterProfile> profile(UUID playerId) {
        return Optional.ofNullable(profiles.get(playerId));
    }

    public synchronized Optional<MFCharacterProfile> createProfile(UUID playerId, RaceFamily race,
                                                                     CombatClass combatClass, Profession profession,
                                                                     long seed) {
        if (playerId == null || profiles.containsKey(playerId) || race == null || combatClass == null || profession == null)
            return Optional.empty();
        MFCharacterProfile profile = MFCharacterProfile.create(playerId, race, combatClass, profession, seed);
        profiles.put(playerId, profile);
        setDirty();
        return Optional.of(profile);
    }

    public synchronized boolean updateProfile(MFCharacterProfile profile) {
        if (profile == null || !profiles.containsKey(profile.playerId())) return false;
        profiles.put(profile.playerId(), profile);
        setDirty();
        return true;
    }

    /** Grants class XP from actual vanilla mob XP drops, with same-type diminishing returns and a daily cap. */
    public synchronized int awardCombatExperience(UUID playerId, String creatureType, int vanillaExperience, long worldDay) {
        if (playerId == null || creatureType == null || creatureType.isBlank() || creatureType.length() > 256
                || vanillaExperience <= 0 || worldDay < 0 || !profiles.containsKey(playerId)) return 0;
        if (combatRewardDay < 0L || worldDay > combatRewardDay) {
            combatRewardDay = worldDay;
            dailyCreatureKills.clear();
            dailyCombatXp.clear();
        }
        if (worldDay < combatRewardDay) return 0;
        String key = playerId + "|" + creatureType;
        int previousKills = dailyCreatureKills.getOrDefault(key, 0);
        long earned = dailyCombatXp.getOrDefault(playerId, 0L);
        int award = MFCombatRewardRules.calculateAward(vanillaExperience, previousKills, earned);
        dailyCreatureKills.put(key, Math.min(10_000, previousKills + 1));
        if (award > 0) {
            MFCharacterProfile profile = profiles.get(playerId);
            profiles.put(playerId, profile.awardCombatXp(award));
            dailyCombatXp.put(playerId, earned + award);
            setDirty();
        } else {
            setDirty();
        }
        return award;
    }

    public synchronized List<MFSettlementState> settlements() {
        return List.copyOf(settlements.values());
    }

    public synchronized Optional<MFSettlementState> settlement(UUID id) {
        return Optional.ofNullable(settlements.get(id));
    }

    /** Replaces an existing settlement after an atomic domain operation such as banking or logistics. */
    public synchronized boolean replaceSettlement(MFSettlementState updated) {
        if (updated == null || !settlements.containsKey(updated.id())) return false;
        MFSettlementState old = settlements.get(updated.id());
        if (!old.factionId().equals(updated.factionId())) return false;
        settlements.put(updated.id(), updated);
        setDirty();
        return true;
    }

    /** Commits a bilateral settlement change as one transaction. */
    public synchronized boolean replaceSettlementPair(MFSettlementState first, MFSettlementState second) {
        if (first == null || second == null || first.id().equals(second.id())) return false;
        MFSettlementState oldFirst = settlements.get(first.id());
        MFSettlementState oldSecond = settlements.get(second.id());
        if (oldFirst == null || oldSecond == null || !oldFirst.factionId().equals(first.factionId())
                || !oldSecond.factionId().equals(second.factionId())) return false;
        settlements.put(first.id(), first);
        settlements.put(second.id(), second);
        setDirty();
        return true;
    }

    public synchronized Optional<MFSettlementState> createSettlement(UUID factionId, String name) {
        if (factionId == null || name == null || name.trim().length() < 3 || name.trim().length() > 32) return Optional.empty();
        boolean duplicate = settlements.values().stream().anyMatch(s -> s.factionId().equals(factionId) && s.name().equalsIgnoreCase(name.trim()));
        if (duplicate) return Optional.empty();
        MFSettlementState state = MFSettlementState.found(factionId, name);
        settlements.put(state.id(), state);
        setDirty();
        return Optional.of(state);
    }

    public synchronized boolean upgradeSettlement(UUID settlementId) {
        MFSettlementState current = settlements.get(settlementId);
        if (current == null) return false;
        SettlementTier next = MFSettlementRules.next(current.tier());
        if (next == null) return false;
        if (next == SettlementTier.CAPITAL && settlements.values().stream()
                .anyMatch(s -> s.factionId().equals(current.factionId()) && s.tier() == SettlementTier.CAPITAL)) return false;
        MFSettlementRules.Requirements r = MFSettlementRules.requirements(next);
        int population = activeCitizenCount(settlementId);
        if (population < r.minimumPopulation() || current.townHallLevel() < r.minimumHallLevel()
                || current.housingCapacity() < r.minimumHousing() || current.security() < r.minimumSecurity()
                || current.infrastructure() < r.minimumInfrastructure() || current.foodDays() < r.minimumFoodDays()
                || current.treasuryCopper() < r.minimumTreasuryCopper()) return false;
        settlements.put(settlementId, current.withPopulation(population).upgrade(next));
        setDirty();
        return true;
    }

    public synchronized boolean addSettlementFood(UUID settlementId, long rations) {
        MFSettlementState current = settlements.get(settlementId);
        if (current == null || rations <= 0) return false;
        try { settlements.put(settlementId, current.withFoodRations(Math.addExact(current.foodRations(), rations))); }
        catch (ArithmeticException ex) { return false; }
        setDirty();
        return true;
    }

    /**
     * Applies a funded settlement project. Material costs are removed from the local store and copper
     * is deducted from the treasury before capacity, hall or civic ratings change.
     */
    public synchronized boolean applySettlementProject(UUID settlementId, Map<String, Long> materialCosts,
                                                        long copperCost, int housingIncrease, int hallIncrease,
                                                        int securityIncrease, int infrastructureIncrease) {
        MFSettlementState current = settlements.get(settlementId);
        if (current == null || copperCost < 0L || housingIncrease < 0 || hallIncrease < 0
                || securityIncrease < 0 || infrastructureIncrease < 0
                || housingIncrease + hallIncrease + securityIncrease + infrastructureIncrease == 0) return false;
        if (current.treasuryCopper() < copperCost) return false;
        Map<String, Long> costs = materialCosts == null ? Map.of() : materialCosts;
        if (costs.isEmpty() && copperCost == 0L) return false;
        LinkedHashMap<String, Long> nextStock = new LinkedHashMap<>(current.inventory());
        try {
            for (Map.Entry<String, Long> cost : costs.entrySet()) {
                if (cost.getKey() == null || !cost.getKey().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                        || cost.getValue() == null || cost.getValue() <= 0L) return false;
                long available = nextStock.getOrDefault(cost.getKey(), 0L);
                if (available < cost.getValue()) return false;
                long remaining = available - cost.getValue();
                if (remaining == 0L) nextStock.remove(cost.getKey()); else nextStock.put(cost.getKey(), remaining);
            }
            int housing = Math.addExact(current.housingCapacity(), housingIncrease);
            int hall = Math.addExact(current.townHallLevel(), hallIncrease);
            int security = Math.min(100, Math.addExact(current.security(), securityIncrease));
            int infrastructure = Math.min(100, Math.addExact(current.infrastructure(), infrastructureIncrease));
            if (housing > 10_000 || hall > 5) return false;
            MFSettlementState updated = current.withTreasury(current.treasuryCopper() - copperCost)
                    .withInventory(nextStock).withHousingCapacity(housing).withTownHallLevel(hall)
                    .withSecurityAndInfrastructure(security, infrastructure);
            settlements.put(settlementId, updated);
        } catch (ArithmeticException ex) { return false; }
        setDirty();
        return true;
    }

    public synchronized boolean addSettlementStock(UUID settlementId, String itemId, long amount) {
        MFSettlementState current = settlements.get(settlementId);
        if (current == null || amount <= 0 || !current.canAccept(itemId, amount)) return false;
        Map<String, Long> next = new LinkedHashMap<>(current.inventory());
        try { next.merge(itemId, amount, Math::addExact); }
        catch (ArithmeticException ex) { return false; }
        settlements.put(settlementId, current.withInventory(next));
        setDirty();
        return true;
    }

    /**
     * Credits a real farmer harvest and its profession experience as one persistent transaction.
     * If the worker, settlement, item or inventory capacity is invalid, neither record changes.
     */
    public synchronized boolean creditResidentHarvest(UUID settlementId, UUID citizenId,
                                                        String itemId, long amount, long professionXp) {
        MFSettlementState settlement = settlements.get(settlementId);
        MFCitizenState farmer = citizens.get(citizenId);
        if (settlement == null || farmer == null || !settlement.id().equals(farmer.settlementId())
                || !settlement.factionId().equals(farmer.factionId())
                || farmer.profession() != Profession.FARMER || farmer.status() != CitizenStatus.ACTIVE
                || farmer.ageStage() == AgeStage.BABY || farmer.ageStage() == AgeStage.CHILD
                || amount <= 0L || amount > 64L || professionXp <= 0L || professionXp > 1_000L
                || !java.util.Set.of("minecraft:wheat", "minecraft:carrot", "minecraft:potato",
                        "minecraft:beetroot").contains(itemId)
                || !settlement.canAccept(itemId, amount)) return false;
        LinkedHashMap<String, Long> nextStock = new LinkedHashMap<>(settlement.inventory());
        MFCitizenState nextFarmer;
        try {
            nextStock.merge(itemId, amount, Math::addExact);
            nextFarmer = farmer.awardProfessionXp(professionXp);
        } catch (ArithmeticException ex) {
            return false;
        }
        settlements.put(settlementId, settlement.withInventory(nextStock));
        citizens.put(citizenId, nextFarmer);
        setDirty();
        return true;
    }

    /**
     * Consumes one real settlement ration to feed an active resident.
     * Hunger, health, satisfaction and stored food are committed together.
     */
    public synchronized boolean feedResident(UUID settlementId, UUID citizenId) {
        MFSettlementState settlement = settlements.get(settlementId);
        MFCitizenState citizen = citizens.get(citizenId);
        if (settlement == null || citizen == null || !settlement.id().equals(citizen.settlementId())
                || !settlement.factionId().equals(citizen.factionId())
                || citizen.status() != CitizenStatus.ACTIVE
                || citizen.ageStage() == AgeStage.BABY || citizen.ageStage() == AgeStage.CHILD
                || citizen.hunger() < 20 || settlement.foodRations() < 1L) return false;

        MFCitizenState fed = citizen.withHealthAndHunger(
                Math.min(100, citizen.health() + 1), Math.max(0, citizen.hunger() - 35));
        MFCitizenState rewarded = withSocialSatisfaction(fed, 2);
        MFSettlementState updatedSettlement = settlement.withFoodRations(settlement.foodRations() - 1L);
        settlements.put(settlementId, updatedSettlement);
        citizens.put(citizenId, rewarded);
        setDirty();
        return true;
    }

    /**
     * Applies one nearby social interaction to two active residents in the same settlement.
     * Both satisfaction updates are committed together; cross-settlement interactions are rejected.
     */
    public synchronized boolean socializeCitizens(UUID firstId, UUID secondId, int satisfactionGain) {
        if (firstId == null || secondId == null || firstId.equals(secondId)
                || satisfactionGain < 1 || satisfactionGain > 10) return false;
        MFCitizenState first = citizens.get(firstId);
        MFCitizenState second = citizens.get(secondId);
        if (first == null || second == null || first.status() != CitizenStatus.ACTIVE
                || second.status() != CitizenStatus.ACTIVE
                || !first.settlementId().equals(second.settlementId())
                || !first.factionId().equals(second.factionId())
                || !settlements.containsKey(first.settlementId())
                || (first.satisfaction() >= 100 && second.satisfaction() >= 100)) return false;

        MFCitizenState updatedFirst = withSocialSatisfaction(first, satisfactionGain);
        MFCitizenState updatedSecond = withSocialSatisfaction(second, satisfactionGain);
        citizens.put(firstId, updatedFirst);
        citizens.put(secondId, updatedSecond);
        setDirty();
        return true;
    }

    private static MFCitizenState withSocialSatisfaction(MFCitizenState citizen, int gain) {
        return new MFCitizenState(citizen.id(), citizen.factionId(), citizen.settlementId(), citizen.name(),
                citizen.race(), citizen.profession(), citizen.ageStage(), citizen.status(), citizen.health(),
                citizen.hunger(), Math.min(100, citizen.satisfaction() + gain), citizen.dailyWageCopper(),
                citizen.purseCopper(), citizen.unpaidDays(), citizen.ageDays(), citizen.professionLevel(),
                citizen.professionXp(), citizen.professionPotential());
    }

    public synchronized boolean adjustSettlementTreasury(UUID settlementId, long deltaCopper) {
        MFSettlementState current = settlements.get(settlementId);
        if (current == null) return false;
        try {
            long updated = Math.addExact(current.treasuryCopper(), deltaCopper);
            if (updated < 0) return false;
            settlements.put(settlementId, current.withTreasury(updated));
        } catch (ArithmeticException ex) { return false; }
        setDirty();
        return true;
    }

    public synchronized boolean setSettlementDailyMaintenance(UUID settlementId, long copperPerDay) {
        MFSettlementState current = settlements.get(settlementId);
        if (current == null || copperPerDay < 0L) return false;
        settlements.put(settlementId, current.withDailyMaintenanceCopper(copperPerDay));
        setDirty();
        return true;
    }

    /** Enforces the faction's primary race when permanently incorporating a resident. */
    public synchronized boolean addCitizen(MFCitizenState citizen, RaceFamily factionPrimaryRace) {
        if (citizen == null || citizens.containsKey(citizen.id())
                || (factionPrimaryRace != null && citizen.race() != factionPrimaryRace)) return false;
        MFSettlementState settlement = settlements.get(citizen.settlementId());
        if (settlement == null || !settlement.factionId().equals(citizen.factionId())) return false;
        if (activeCitizenCount(settlement.id()) >= settlement.housingCapacity()) return false;
        boolean duplicateName = citizens.values().stream().anyMatch(c -> c.settlementId().equals(citizen.settlementId())
                && c.name().equalsIgnoreCase(citizen.name()) && c.status() == CitizenStatus.ACTIVE);
        if (duplicateName) return false;
        citizens.put(citizen.id(), citizen);
        settlements.put(settlement.id(), settlement.withPopulation(activeCitizenCount(settlement.id())));
        setDirty();
        return true;
    }

    public synchronized boolean awardCitizenProfessionXp(UUID citizenId, long xp) {
        MFCitizenState citizen = citizens.get(citizenId);
        if (citizen == null || xp <= 0L || citizen.status() != CitizenStatus.ACTIVE) return false;
        MFCitizenState updated = citizen.awardProfessionXp(xp);
        if (updated == citizen) return false;
        citizens.put(citizenId, updated);
        setDirty();
        return true;
    }

    /** Replaces one persistent citizen record while keeping settlement population counts consistent. */
    public synchronized boolean replaceCitizen(MFCitizenState updated) {
        if (updated == null) return false;
        MFCitizenState current = citizens.get(updated.id());
        if (current == null || !current.factionId().equals(updated.factionId())
                || !current.settlementId().equals(updated.settlementId()) || !current.name().equals(updated.name())) return false;
        MFSettlementState settlement = settlements.get(updated.settlementId());
        if (settlement == null) return false;
        boolean wasActive = current.status() == CitizenStatus.ACTIVE;
        boolean isActive = updated.status() == CitizenStatus.ACTIVE;
        citizens.put(updated.id(), updated);
        if (wasActive != isActive) settlements.put(settlement.id(), settlement.withPopulation(activeCitizenCount(settlement.id())));
        setDirty();
        return true;
    }

    /** Direct indexed lookup for a materialized resident; avoids allocating/scanning the full population list. */
    public synchronized Optional<MFCitizenState> citizen(UUID citizenId) {
        if (citizenId == null) return Optional.empty();
        return Optional.ofNullable(citizens.get(citizenId));
    }

    public synchronized List<MFCitizenState> citizens(UUID settlementId) {
        List<MFCitizenState> result = new ArrayList<>();
        for (MFCitizenState citizen : citizens.values())
            if (settlementId == null || settlementId.equals(citizen.settlementId())) result.add(citizen);
        return Collections.unmodifiableList(result);
    }

    public synchronized List<MFWorkOrderState> workOrders(UUID settlementId) {
        List<MFWorkOrderState> result = new ArrayList<>();
        for (MFWorkOrderState order : workOrders.values())
            if (settlementId == null || settlementId.equals(order.settlementId())) result.add(order);
        return List.copyOf(result);
    }

    /** Starts one profession-matched job and reserves all recipe inputs atomically. */
    public synchronized Optional<MFWorkOrderState> startWorkOrder(UUID settlementId, UUID workerId, String recipeId) {
        MFSettlementState settlement = settlements.get(settlementId);
        MFCitizenState worker = citizens.get(workerId);
        MFRecipeCatalog.Recipe recipe = MFRecipeCatalog.recipe(recipeId).orElse(null);
        if (settlement == null || worker == null || recipe == null || worker.status() != CitizenStatus.ACTIVE
                || !worker.settlementId().equals(settlementId) || worker.profession() != recipe.profession()
                || worker.ageStage() == AgeStage.BABY || worker.ageStage() == AgeStage.CHILD
                || workOrders.values().stream().anyMatch(o -> o.workerId().equals(workerId) && o.active())) return Optional.empty();
        LinkedHashMap<String, Long> nextStock = new LinkedHashMap<>(settlement.inventory());
        for (Map.Entry<String, Long> input : recipe.inputs().entrySet()) {
            long available = nextStock.getOrDefault(input.getKey(), 0L);
            if (available < input.getValue()) return Optional.empty();
            long remaining = available - input.getValue();
            if (remaining == 0L) nextStock.remove(input.getKey()); else nextStock.put(input.getKey(), remaining);
        }
        MFWorkOrderState order = MFWorkOrderState.start(settlementId, workerId, recipeId);
        settlements.put(settlementId, settlement.withInventory(nextStock));
        workOrders.put(order.id(), order);
        setDirty();
        return Optional.of(order);
    }

    /** A job may be refunded only before its first simulation day. */
    public synchronized boolean cancelUnstartedWorkOrder(UUID orderId) {
        MFWorkOrderState order = workOrders.get(orderId);
        if (order == null || order.status() != MFWorkOrderState.Status.RUNNING || order.elapsedDays() != 0) return false;
        MFSettlementState settlement = settlements.get(order.settlementId());
        MFRecipeCatalog.Recipe recipe = MFRecipeCatalog.recipe(order.recipeId()).orElse(null);
        if (settlement == null || recipe == null) return false;
        LinkedHashMap<String, Long> nextStock = new LinkedHashMap<>(settlement.inventory());
        try {
            for (Map.Entry<String, Long> input : recipe.inputs().entrySet())
                nextStock.merge(input.getKey(), input.getValue(), Math::addExact);
        } catch (ArithmeticException ex) { return false; }
        settlements.put(settlement.id(), settlement.withInventory(nextStock));
        workOrders.put(orderId, order.cancel());
        setDirty();
        return true;
    }

    private void advanceWorkOrders() {
        for (MFWorkOrderState original : new ArrayList<>(workOrders.values())) {
            if (!original.active()) continue;
            MFWorkOrderState current = original;
            if (current.status() == MFWorkOrderState.Status.RUNNING) {
                MFCitizenState worker = citizens.get(current.workerId());
                if (worker == null || worker.status() != CitizenStatus.ACTIVE) {
                    // Inputs were escrowed when the order started. An abandoned order must release
                    // them rather than strand the settlement's stock forever.
                    MFSettlementState settlement = settlements.get(current.settlementId());
                    MFRecipeCatalog.Recipe recipe = MFRecipeCatalog.recipe(current.recipeId()).orElse(null);
                    if (settlement != null && recipe != null) {
                        LinkedHashMap<String, Long> restored = new LinkedHashMap<>(settlement.inventory());
                        try {
                            for (Map.Entry<String, Long> input : recipe.inputs().entrySet())
                                restored.merge(input.getKey(), input.getValue(), Math::addExact);
                            settlements.put(settlement.id(), settlement.withInventory(restored));
                            workOrders.put(current.id(), current.abort());
                        } catch (ArithmeticException ignored) {
                            // Keep the order active and retry the exact refund next day; never discard reserved goods.
                        }
                    }
                    continue;
                }
                current = current.advance();
                workOrders.put(current.id(), current);
            }
            if (current.status() == MFWorkOrderState.Status.READY_TO_OUTPUT) {
                MFSettlementState settlement = settlements.get(current.settlementId());
                MFRecipeCatalog.Recipe recipe = MFRecipeCatalog.recipe(current.recipeId()).orElse(null);
                if (settlement == null || recipe == null) continue;
                LinkedHashMap<String, Long> nextStock = new LinkedHashMap<>(settlement.inventory());
                try {
                    for (Map.Entry<String, Long> output : recipe.outputs().entrySet())
                        nextStock.merge(output.getKey(), output.getValue(), Math::addExact);
                    settlements.put(settlement.id(), settlement.withInventory(nextStock));
                    MFCitizenState worker = citizens.get(current.workerId());
                    if (worker != null) citizens.put(worker.id(), worker.awardProfessionXp(recipe.professionXp()));
                    workOrders.put(current.id(), current.complete());
                } catch (ArithmeticException ignored) {
                    // Keep READY_TO_OUTPUT until the destination can accept the entire batch.
                }
            }
        }
    }

    public synchronized long simulatedDays() { return simulatedDays; }

    /** Advances all settlements by one abstract day using finite food and treasury reserves. */
    public synchronized void advanceOneDay() {
        Map<UUID, List<MFCitizenState>> residentsBySettlement = new LinkedHashMap<>();
        for (MFCitizenState citizen : citizens.values())
            residentsBySettlement.computeIfAbsent(citizen.settlementId(), ignored -> new ArrayList<>()).add(citizen);
        for (MFSettlementState settlement : new ArrayList<>(settlements.values())) {
            List<MFCitizenState> residents = residentsBySettlement.getOrDefault(settlement.id(), List.of());
            MFSettlementSimulation.DayResult result = MFSettlementSimulation.advanceDay(settlement, residents);
            settlements.put(settlement.id(), result.settlement());
            for (MFCitizenState resident : result.residents()) citizens.put(resident.id(), resident);
        }
        advanceWorkOrders();
        simulatedDays++;
        setDirty();
    }

    /** Processes a world day boundary once, including safe catch-up for administrative time jumps. */
    public synchronized void observeMinecraftDay(long currentDay) {
        if (currentDay < 0L) return;
        if (lastMinecraftDay < 0L) {
            lastMinecraftDay = currentDay;
            setDirty();
            return;
        }
        if (currentDay <= lastMinecraftDay) return;
        long elapsed = Math.min(7L, currentDay - lastMinecraftDay);
        for (long i = 0; i < elapsed; i++) advanceOneDay();
        lastMinecraftDay = currentDay;
        setDirty();
    }

    public synchronized Optional<MFDiplomacyRelation> relation(UUID factionA, UUID factionB) {
        return Optional.ofNullable(diplomacy.get(relationKey(factionA, factionB)));
    }

    public synchronized boolean adjustRelation(UUID factionA, UUID factionB, int delta) {
        if (!validFactionPair(factionA, factionB)) return false;
        String key = relationKey(factionA, factionB);
        MFDiplomacyRelation old = diplomacy.getOrDefault(key, new MFDiplomacyRelation(factionA, factionB, 0, false, Set.of()));
        diplomacy.put(key, old.adjust(delta));
        setDirty();
        return true;
    }

    public synchronized boolean declareWar(UUID factionA, UUID factionB) {
        if (!validFactionPair(factionA, factionB)) return false;
        String key = relationKey(factionA, factionB);
        MFDiplomacyRelation old = diplomacy.getOrDefault(key, new MFDiplomacyRelation(factionA, factionB, 0, false, Set.of()));
        diplomacy.put(key, old.declareWar());
        setDirty();
        return true;
    }

    public synchronized boolean makePeace(UUID factionA, UUID factionB, int postWarScore) {
        if (!validFactionPair(factionA, factionB)) return false;
        String key = relationKey(factionA, factionB);
        MFDiplomacyRelation old = diplomacy.getOrDefault(key, new MFDiplomacyRelation(factionA, factionB, 0, false, Set.of()));
        diplomacy.put(key, old.makePeace(postWarScore));
        setDirty();
        return true;
    }

    public synchronized Optional<MFTradeEngine.TradeResult> trade(UUID sellerId, UUID buyerId,
                                                                    String itemId, long quantity, long unitPriceCopper) {
        MFSettlementState seller = settlements.get(sellerId), buyer = settlements.get(buyerId);
        if (seller == null || buyer == null) return Optional.empty();
        Optional<MFTradeEngine.TradeResult> trade = MFTradeEngine.execute(seller, buyer, itemId, quantity, unitPriceCopper);
        trade.ifPresent(result -> {
            settlements.put(sellerId, result.seller());
            settlements.put(buyerId, result.buyer());
            setDirty();
        });
        return trade;
    }

    private int activeCitizenCount(UUID settlementId) {
        int count = 0;
        for (MFCitizenState citizen : citizens.values())
            if (citizen.settlementId().equals(settlementId) && citizen.status() == CitizenStatus.ACTIVE) count++;
        return count;
    }

    private static boolean validFactionPair(UUID a, UUID b) { return a != null && b != null && !a.equals(b); }
    private static String relationKey(UUID a, UUID b) {
        return a.toString().compareTo(b.toString()) <= 0 ? a + "|" + b : b + "|" + a;
    }

    public static MFWorldSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MFSavedDataSchema.requireSupported(tag, SCHEMA_VERSION, DATA_ID);
        MFWorldSavedData data = new MFWorldSavedData();
        data.simulatedDays = Math.max(0L, tag.getLong("simulated_days"));
        data.combatRewardDay = tag.contains("combat_reward_day", Tag.TAG_LONG) ? tag.getLong("combat_reward_day") : -1L;
        loadCombatLedger(data, tag);
        data.lastMinecraftDay = tag.contains("last_minecraft_day", Tag.TAG_LONG) ? tag.getLong("last_minecraft_day") : -1L;
        loadProfiles(data, tag.getList("profiles", Tag.TAG_COMPOUND));
        loadSettlements(data, tag.getList("settlements", Tag.TAG_COMPOUND));
        loadCitizens(data, tag.getList("citizens", Tag.TAG_COMPOUND));
        loadDiplomacy(data, tag.getList("diplomacy", Tag.TAG_COMPOUND));
        loadWorkOrders(data, tag.getList("work_orders", Tag.TAG_COMPOUND));
        return data;
    }

    @Override public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("schema_version", SCHEMA_VERSION);
        tag.putLong("simulated_days", simulatedDays);
        tag.putLong("combat_reward_day", combatRewardDay);
        tag.putLong("last_minecraft_day", lastMinecraftDay);
        ListTag profileList = new ListTag();
        for (MFCharacterProfile p : profiles.values()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("player", p.playerId()); e.putString("race", p.race().name());
            e.putString("affinity", p.affinity().name()); e.putString("element", p.element().name());
            e.putString("class", p.mainClass().name()); e.putString("profession", p.mainProfession().name());
            if (p.subclass() != null) e.putString("subclass", p.subclass());
            e.putInt("class_level", p.classLevel()); e.putLong("combat_xp", p.combatXp());
            e.putInt("profession_level", p.professionLevel()); e.putLong("profession_xp", p.professionXp());
            e.putBoolean("subclass_trial", p.subclassTrialCompleted());
            ListTag nodes = new ListTag();
            for (String node : p.unlockedNodes()) { CompoundTag n = new CompoundTag(); n.putString("id", node); nodes.add(n); }
            e.put("nodes", nodes); profileList.add(e);
        }
        tag.put("profiles", profileList);

        ListTag settlementList = new ListTag();
        for (MFSettlementState s : settlements.values()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("id", s.id()); e.putUUID("faction", s.factionId()); e.putString("name", s.name());
            e.putString("tier", s.tier().name()); e.putInt("hall", s.townHallLevel());
            e.putInt("population", s.population()); e.putInt("housing", s.housingCapacity());
            e.putLong("food", s.foodRations()); e.putLong("treasury", s.treasuryCopper());
            e.putInt("security", s.security()); e.putInt("infrastructure", s.infrastructure());
            e.putLong("maintenance", s.dailyMaintenanceCopper());
            ListTag items = new ListTag();
            s.inventory().forEach((item, amount) -> { CompoundTag i = new CompoundTag(); i.putString("item", item); i.putLong("amount", amount); items.add(i); });
            e.put("inventory", items); settlementList.add(e);
        }
        tag.put("settlements", settlementList);

        ListTag citizenList = new ListTag();
        for (MFCitizenState c : citizens.values()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("id", c.id()); e.putUUID("faction", c.factionId()); e.putUUID("settlement", c.settlementId());
            e.putString("name", c.name()); e.putString("race", c.race().name()); e.putString("profession", c.profession().name());
            e.putString("age_stage", c.ageStage().name()); e.putString("status", c.status().name());
            e.putInt("health", c.health()); e.putInt("hunger", c.hunger()); e.putInt("satisfaction", c.satisfaction());
            e.putLong("wage", c.dailyWageCopper()); e.putLong("purse", c.purseCopper()); e.putInt("unpaid", c.unpaidDays());
            e.putLong("age_days", c.ageDays()); e.putInt("profession_level", c.professionLevel());
            e.putLong("profession_xp", c.professionXp()); e.putInt("profession_potential", c.professionPotential()); citizenList.add(e);
        }
        tag.put("citizens", citizenList);

        ListTag relationList = new ListTag();
        for (MFDiplomacyRelation r : diplomacy.values()) {
            CompoundTag e = new CompoundTag(); e.putUUID("a", r.factionA()); e.putUUID("b", r.factionB());
            e.putInt("score", r.score()); e.putBoolean("war", r.atWar());
            ListTag treaties = new ListTag();
            for (Treaty treaty : r.treaties()) { CompoundTag t = new CompoundTag(); t.putString("id", treaty.name()); treaties.add(t); }
            e.put("treaties", treaties); relationList.add(e);
        }
        tag.put("diplomacy", relationList);

        ListTag workList = new ListTag();
        for (MFWorkOrderState order : workOrders.values()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("id", order.id()); e.putUUID("settlement", order.settlementId());
            e.putUUID("worker", order.workerId()); e.putString("recipe", order.recipeId());
            e.putInt("elapsed_days", order.elapsedDays()); e.putString("status", order.status().name());
            workList.add(e);
        }
        tag.put("work_orders", workList);

        ListTag combatKills = new ListTag();
        dailyCreatureKills.forEach((key, count) -> { CompoundTag e = new CompoundTag(); e.putString("key", key); e.putInt("count", count); combatKills.add(e); });
        tag.put("combat_kills", combatKills);
        ListTag combatXp = new ListTag();
        dailyCombatXp.forEach((player, amount) -> { CompoundTag e = new CompoundTag(); e.putUUID("player", player); e.putLong("amount", amount); combatXp.add(e); });
        tag.put("combat_xp_today", combatXp);
        return tag;
    }

    private static void loadProfiles(MFWorldSavedData data, ListTag list) {
        for (int i = 0; i < list.size(); i++) try {
            CompoundTag e = list.getCompound(i);
            UUID id = e.getUUID("player");
            ListTag nodeList = e.getList("nodes", Tag.TAG_COMPOUND); Set<String> nodes = new LinkedHashSet<>();
            for (int n = 0; n < nodeList.size(); n++) nodes.add(nodeList.getCompound(n).getString("id"));
            MFCharacterProfile p = new MFCharacterProfile(id, enumValue(RaceFamily.class, e.getString("race")),
                    enumValue(Affinity.class, e.getString("affinity")), enumValue(Element.class, e.getString("element")),
                    enumValue(CombatClass.class, e.getString("class")), enumValue(Profession.class, e.getString("profession")),
                    e.contains("subclass", Tag.TAG_STRING) ? e.getString("subclass") : null,
                    e.getInt("class_level"), Math.max(0L, e.getLong("combat_xp")), nodes,
                    e.getInt("profession_level"), Math.max(0L, e.getLong("profession_xp")), e.getBoolean("subclass_trial"));
            data.profiles.put(id, p);
        } catch (Exception ignored) { /* Skip malformed individual records. */ }
    }

    private static void loadSettlements(MFWorldSavedData data, ListTag list) {
        for (int i = 0; i < list.size(); i++) try {
            CompoundTag e = list.getCompound(i); Map<String, Long> items = new LinkedHashMap<>();
            ListTag itemList = e.getList("inventory", Tag.TAG_COMPOUND);
            for (int n = 0; n < itemList.size(); n++) { CompoundTag item = itemList.getCompound(n); items.put(item.getString("item"), item.getLong("amount")); }
            MFSettlementState s = new MFSettlementState(e.getUUID("id"), e.getUUID("faction"), e.getString("name"),
                    enumValue(SettlementTier.class, e.getString("tier")), Math.max(1, e.getInt("hall")),
                    Math.max(0, e.getInt("population")), Math.max(0, e.getInt("housing")), Math.max(0L, e.getLong("food")),
                    Math.max(0L, e.getLong("treasury")), clamp(e.getInt("security"), 0, 100),
                    clamp(e.getInt("infrastructure"), 0, 100), Math.max(0L, e.getLong("maintenance")), items);
            data.settlements.put(s.id(), s);
        } catch (Exception ignored) { /* Skip malformed individual records. */ }
    }

    private static void loadCitizens(MFWorldSavedData data, ListTag list) {
        for (int i = 0; i < list.size(); i++) try {
            CompoundTag e = list.getCompound(i);
            RaceFamily race = enumValue(RaceFamily.class, e.getString("race"));
            long ageDays = Math.max(0L, e.getLong("age_days"));
            MFCitizenState c = new MFCitizenState(e.getUUID("id"), e.getUUID("faction"), e.getUUID("settlement"), e.getString("name"),
                    race, enumValue(Profession.class, e.getString("profession")),
                    MFRaceBiology.stageAt(race, ageDays), enumValue(CitizenStatus.class, e.getString("status")),
                    clamp(e.getInt("health"), 0, 100), clamp(e.getInt("hunger"), 0, 100), clamp(e.getInt("satisfaction"), 0, 100),
                    Math.max(0L, e.getLong("wage")), Math.max(0L, e.getLong("purse")), Math.max(0, e.getInt("unpaid")),
                    ageDays, clamp(e.getInt("profession_level"), 1, 50),
                    Math.max(0L, e.getLong("profession_xp")), clamp(e.getInt("profession_potential"), 1, 50));
            data.citizens.put(c.id(), c);
        } catch (Exception ignored) { /* Skip malformed individual records. */ }
    }

    private static void loadDiplomacy(MFWorldSavedData data, ListTag list) {
        for (int i = 0; i < list.size(); i++) try {
            CompoundTag e = list.getCompound(i); Set<Treaty> treaties = new LinkedHashSet<>();
            ListTag treatyList = e.getList("treaties", Tag.TAG_COMPOUND);
            for (int n = 0; n < treatyList.size(); n++) {
                Treaty treaty = enumValue(Treaty.class, treatyList.getCompound(n).getString("id"));
                if (treaty != null) treaties.add(treaty);
            }
            MFDiplomacyRelation r = new MFDiplomacyRelation(e.getUUID("a"), e.getUUID("b"), clamp(e.getInt("score"), -100, 100), e.getBoolean("war"), treaties);
            data.diplomacy.put(relationKey(r.factionA(), r.factionB()), r);
        } catch (Exception ignored) { /* Skip malformed individual records. */ }
    }

    private static void loadCombatLedger(MFWorldSavedData data, CompoundTag tag) {
        ListTag kills = tag.getList("combat_kills", Tag.TAG_COMPOUND);
        for (int i = 0; i < kills.size(); i++) try {
            CompoundTag e = kills.getCompound(i);
            String key = e.getString("key"); int count = e.getInt("count");
            if (key.length() <= 300 && count >= 0 && count <= 10_000) data.dailyCreatureKills.put(key, count);
        } catch (Exception ignored) { }
        ListTag xp = tag.getList("combat_xp_today", Tag.TAG_COMPOUND);
        for (int i = 0; i < xp.size(); i++) try {
            CompoundTag e = xp.getCompound(i); long amount = e.getLong("amount");
            if (amount >= 0L && amount <= MFCombatRewardRules.DAILY_XP_CAP)
                data.dailyCombatXp.put(e.getUUID("player"), amount);
        } catch (Exception ignored) { }
    }

    private static void loadWorkOrders(MFWorldSavedData data, ListTag list) {
        for (int i = 0; i < list.size(); i++) try {
            CompoundTag e = list.getCompound(i);
            MFWorkOrderState order = new MFWorkOrderState(e.getUUID("id"), e.getUUID("settlement"),
                    e.getUUID("worker"), e.getString("recipe"), Math.max(0, e.getInt("elapsed_days")),
                    enumValue(MFWorkOrderState.Status.class, e.getString("status")));
            if (data.settlements.containsKey(order.settlementId()) && data.citizens.containsKey(order.workerId()))
                data.workOrders.put(order.id(), order);
        } catch (Exception ignored) { /* Skip malformed orders; reserved inputs remain in storage state. */ }
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String name) {
        E value = MFGameCatalog.parseEnum(type, name);
        if (value == null) throw new IllegalArgumentException("Unknown enum value " + name + " for " + type.getSimpleName());
        return value;
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
