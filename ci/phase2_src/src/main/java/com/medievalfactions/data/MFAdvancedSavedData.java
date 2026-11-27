package com.medievalfactions.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.medievalfactions.domain.MFAdvancedSystems;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.domain.MFSettlementState;
import com.medievalfactions.domain.MFPopulationIndex;
import com.medievalfactions.domain.MFRaceBiology;
import com.medievalfactions.domain.MFRecipeCatalog;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.medievalfactions.domain.MFAdvancedSystems.*;
import static com.medievalfactions.domain.MFGameCatalog.*;

/** Persistent storage and authoritative server-side orchestration for advanced world systems. */
public final class MFAdvancedSavedData extends SavedData {
    private static final String DATA_ID = "medieval_factions_advanced_v1";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final int MAX_RECORDS_PER_BUCKET = 25_000;
    private static final int MAX_RECORD_JSON_CHARS = 32_000;
    private static final long MAX_TOTAL_RECORD_JSON_CHARS = 64_000_000L;
    private static final Factory<MFAdvancedSavedData> FACTORY = new Factory<>(MFAdvancedSavedData::new, MFAdvancedSavedData::load);

    private static final Map<String, Class<?>> RECORD_TYPES = Map.ofEntries(
            Map.entry("npc_directives", NpcDirective.class),
            Map.entry("caravans", Caravan.class),
            Map.entry("bank_accounts", BankAccount.class),
            Map.entry("loans", Loan.class),
            Map.entry("sieges", Siege.class),
            Map.entry("companions", Companion.class),
            Map.entry("persons", Person.class),
            Map.entry("relationships", Relationship.class),
            Map.entry("pregnancies", Pregnancy.class),
            Map.entry("lineage", Lineage.class),
            Map.entry("expeditions", Expedition.class),
            Map.entry("construction", ConstructionProject.class),
            Map.entry("dungeons", DungeonProject.class),
            Map.entry("citizen_schedules", com.medievalfactions.domain.MFCitizenSimulation.CitizenSchedule.class),
            Map.entry("citizen_plans", com.medievalfactions.domain.MFCitizenSimulation.CitizenPlan.class),
            Map.entry("resident_links", com.medievalfactions.domain.MFCitizenSimulation.ResidentEntityLink.class),
            Map.entry("city_plans", com.medievalfactions.domain.MFCityPlanner.CityPlan.class),
            Map.entry("city_build_links", com.medievalfactions.domain.MFCityPlanner.ConstructionLink.class),
            Map.entry("tactical_battles", com.medievalfactions.domain.MFTacticalWarfare.Battle.class),
            Map.entry("army_columns", com.medievalfactions.domain.MFTacticalWarfare.ArmyColumn.class),
            Map.entry("occupations", com.medievalfactions.domain.MFTacticalWarfare.Occupation.class),
            Map.entry("dragon_genomes", com.medievalfactions.domain.MFDragonLifecycle.DragonGenomeRecord.class),
            Map.entry("dragon_eggs", com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class),
            Map.entry("dragon_hatchlings", com.medievalfactions.domain.MFDragonLifecycle.Hatchling.class),
            Map.entry("marriages", com.medievalfactions.domain.MFFamilyCare.Marriage.class),
            Map.entry("medical_cases", com.medievalfactions.domain.MFFamilyCare.MedicalCase.class),
            Map.entry("clinics", com.medievalfactions.domain.MFFamilyCare.Clinic.class),
            Map.entry("physical_convoys", com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy.class),
            Map.entry("boss_encounters", com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class),
            Map.entry("boss_entities", com.medievalfactions.domain.MFAdventureSystems.BossEntityLink.class),
            Map.entry("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class));

    private final Map<String, LinkedHashMap<UUID, Object>> buckets = new LinkedHashMap<>();
    private final Map<UUID, List<BlockPlacement>> dungeonPlacementCache = new LinkedHashMap<>();
    private long lastAdvancedDay = -1L;

    public MFAdvancedSavedData() {
        RECORD_TYPES.keySet().forEach(key -> buckets.put(key, new LinkedHashMap<>()));
    }

    public static MFAdvancedSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    public synchronized long lastAdvancedDay() { return lastAdvancedDay; }

    public synchronized int count(String bucket) {
        return buckets.getOrDefault(bucket, new LinkedHashMap<>()).size();
    }

    public synchronized <T> Optional<T> record(String bucket, UUID id, Class<T> type) {
        if (id == null || type == null) return Optional.empty();
        Object value = buckets.getOrDefault(bucket, new LinkedHashMap<>()).get(id);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    public synchronized <T> List<T> records(String bucket, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Object value : buckets.getOrDefault(bucket, new LinkedHashMap<>()).values())
            if (type.isInstance(value)) result.add(type.cast(value));
        return List.copyOf(result);
    }

    private synchronized <T> void put(String bucket, UUID id, T value) {
        if (!RECORD_TYPES.containsKey(bucket) || id == null || value == null || !RECORD_TYPES.get(bucket).isInstance(value))
            throw new IllegalArgumentException("Invalid advanced record " + bucket);
        LinkedHashMap<UUID, Object> records = buckets.get(bucket);
        if (!records.containsKey(id) && records.size() >= MAX_RECORDS_PER_BUCKET)
            throw new IllegalStateException("Advanced data bucket capacity reached: " + bucket);
        records.put(id, value);
        setDirty();
    }

    private synchronized void remove(String bucket, UUID id) {
        if (buckets.getOrDefault(bucket, new LinkedHashMap<>()).remove(id) != null) setDirty();
    }

    /**
     * Stores one of the typed simulation records defined by the domain layer.
     * The caller must use its stable record identity as the NBT key (citizenId for schedules,
     * id for all other records). Unknown buckets and mismatched types are rejected.
     */
    public synchronized <T> boolean storeSimulationRecord(String bucket, UUID id, T value, Class<T> type) {
        if (bucket == null || id == null || value == null || type == null
                || !type.isInstance(value) || RECORD_TYPES.get(bucket) != type) return false;
        put(bucket, id, type.cast(value));
        return true;
    }

    public synchronized boolean removeSimulationRecord(String bucket, UUID id) {
        if (bucket == null || id == null || !RECORD_TYPES.containsKey(bucket)) return false;
        boolean existed = buckets.get(bucket).containsKey(id);
        remove(bucket, id);
        return existed;
    }

    public synchronized boolean saveCitizenSchedule(MFWorldSavedData world,
            com.medievalfactions.domain.MFCitizenSimulation.CitizenSchedule schedule) {
        if (world == null || schedule == null
                || world.citizen(schedule.citizenId()).filter(c -> c.status() == CitizenStatus.ACTIVE).isEmpty()) return false;
        return storeSimulationRecord("citizen_schedules", schedule.citizenId(), schedule,
                com.medievalfactions.domain.MFCitizenSimulation.CitizenSchedule.class);
    }

    /**
     * Re-evaluates persisted resident timetables at the current Minecraft minute (0..1439).
     *
     * The old implementation scanned the entire population once per resident to count housing
     * pressure, then scanned expedition records once per scheduled resident. A single minute
     * transition can therefore become quadratic on a large settlement. This method takes one
     * population snapshot and builds O(1) indexes before planning any resident.
     */
    public synchronized int updateScheduledNpcPlans(MFWorldSavedData world, int minuteOfDay) {
        if (world == null) return 0;
        List<MFCitizenState> population = world.citizens(null);
        if (population.isEmpty()) return 0;
        MFPopulationIndex index = populationIndex(world, population);

        Map<UUID, MFSettlementState> settlementsById = new LinkedHashMap<>();
        for (MFSettlementState settlement : world.settlements()) settlementsById.put(settlement.id(), settlement);
        Map<UUID, com.medievalfactions.domain.MFCitizenSimulation.CitizenSchedule> schedulesByCitizen =
                new LinkedHashMap<>();
        for (var schedule : records("citizen_schedules",
                com.medievalfactions.domain.MFCitizenSimulation.CitizenSchedule.class)) {
            schedulesByCitizen.put(schedule.citizenId(), schedule);
        }

        int updated = 0;
        for (MFCitizenState citizen : population) {
            if (citizen.status() != CitizenStatus.ACTIVE || index.isTravelling(citizen.id())) continue;
            var schedule = schedulesByCitizen.get(citizen.id());
            if (schedule == null) continue;
            MFSettlementState settlement = settlementsById.get(citizen.settlementId());
            if (settlement == null) continue;

            long residents = index.activeCountAt(settlement.id());
            int threat = settlement.security() <= 10 ? 85
                    : settlement.security() <= 30 ? 60
                    : settlement.security() <= 50 ? 30 : 0;
            var entries = schedule.entries();
            var foodPoint = entries.stream()
                    .filter(e -> e.activity() == com.medievalfactions.domain.MFCitizenSimulation.Activity.EAT)
                    .map(com.medievalfactions.domain.MFCitizenSimulation.ScheduleEntry::destination).findFirst().orElse(null);
            var medicPoint = entries.stream()
                    .filter(e -> e.activity() == com.medievalfactions.domain.MFCitizenSimulation.Activity.VISIT_MEDIC)
                    .map(com.medievalfactions.domain.MFCitizenSimulation.ScheduleEntry::destination).findFirst().orElse(null);
            var shelterPoint = entries.stream()
                    .filter(e -> e.activity() == com.medievalfactions.domain.MFCitizenSimulation.Activity.SLEEP)
                    .map(com.medievalfactions.domain.MFCitizenSimulation.ScheduleEntry::destination).findFirst().orElse(null);
            boolean hasFood = settlement.foodRations() > 0;
            boolean safeHousing = residents <= settlement.housingCapacity();
            var plan = com.medievalfactions.domain.MFCitizenSimulation.plan(citizen, schedule, minuteOfDay, threat,
                    hasFood, safeHousing, foodPoint, medicPoint, shelterPoint);
            if (plan.isPresent()) {
                put("citizen_plans", citizen.id(), plan.get());
                updated++;
            } else {
                remove("citizen_plans", citizen.id());
            }
        }
        return updated;
    }

    /** Builds one immutable O(n) snapshot; tick/day systems reuse its constant-time queries. */
    private MFPopulationIndex populationIndex(MFWorldSavedData world, List<MFCitizenState> population) {
        if (world == null) return MFPopulationIndex.build(population, List.of(), List.of());
        List<MFWorkOrderState> orders = new ArrayList<>();
        for (MFSettlementState settlement : world.settlements()) orders.addAll(world.workOrders(settlement.id()));
        return MFPopulationIndex.build(population, orders,
                records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class));
    }

    public synchronized boolean createCityPlan(MFWorldSavedData world, com.medievalfactions.domain.MFCityPlanner.CityPlan plan) {
        if (world == null || plan == null || world.settlement(plan.settlementId()).isEmpty()
                || record("city_plans", plan.id(), com.medievalfactions.domain.MFCityPlanner.CityPlan.class).isPresent()) return false;
        put("city_plans", plan.id(), plan);
        return true;
    }

    /** City placement and settlement stock are committed together; a failed stock update leaves the plan unchanged. */
    public synchronized Optional<com.medievalfactions.domain.MFCityPlanner.Building> placeCityBuilding(
            MFWorldSavedData world, UUID planId, com.medievalfactions.domain.MFCityPlanner.BuildingSpec spec,
            String buildingId, int x, int y, int z, int rotation) {
        var plan = record("city_plans", planId, com.medievalfactions.domain.MFCityPlanner.CityPlan.class).orElse(null);
        MFSettlementState settlement = plan == null || world == null ? null : world.settlement(plan.settlementId()).orElse(null);
        if (plan == null || settlement == null) return Optional.empty();
        var placement = com.medievalfactions.domain.MFCityPlanner.place(plan, spec, buildingId, x, y, z, rotation, settlement.inventory());
        if (placement.isEmpty()) return Optional.empty();
        if (!world.replaceSettlement(settlement.withInventory(placement.get().remainingInventory()))) return Optional.empty();
        put("city_plans", planId, placement.get().plan());
        return Optional.of(placement.get().building());
    }

    /**
     * Queues a city-plan building into the existing safe block-placement engine.
     * Material reservation happened when the plot was placed; this method does not charge it twice.
     * Only shipped physical blueprints can be queued in this alpha.
     */
    public synchronized Optional<ConstructionProject> queueCityBuilding(UUID planId, String buildingId, UUID ownerId, long day) {
        var plan = record("city_plans", planId, com.medievalfactions.domain.MFCityPlanner.CityPlan.class).orElse(null);
        if (plan == null || buildingId == null || ownerId == null || day < 0
                || records("city_build_links", com.medievalfactions.domain.MFCityPlanner.ConstructionLink.class).stream()
                    .anyMatch(link -> link.cityPlanId().equals(planId) && link.buildingId().equals(buildingId))) return Optional.empty();
        var building = plan.buildings().stream().filter(b -> b.id().equals(buildingId)).findFirst().orElse(null);
        Blueprint blueprint = building == null ? null : starterBlueprints().get(building.blueprintId());
        if (building == null || building.stage() != com.medievalfactions.domain.MFCityPlanner.BuildStage.PLANNED
                || blueprint == null) return Optional.empty();
        int minX = blueprint.placements().stream().mapToInt(BlockPlacement::x).min().orElse(0);
        int maxX = blueprint.placements().stream().mapToInt(BlockPlacement::x).max().orElse(0);
        int minZ = blueprint.placements().stream().mapToInt(BlockPlacement::z).min().orElse(0);
        int maxZ = blueprint.placements().stream().mapToInt(BlockPlacement::z).max().orElse(0);
        int expectedWidth = maxX - minX + 1, expectedDepth = maxZ - minZ + 1;
        if (building.width() != expectedWidth || building.depth() != expectedDepth) return Optional.empty();
        int originX = building.x() - minX;
        int originZ = building.z() - minZ;
        ConstructionProject project = new ConstructionProject(UUID.randomUUID(), plan.settlementId(), ownerId,
                plan.dimension(), originX, building.y(), originZ, building.rotation(), building.blueprintId(),
                0, ConstructionStatus.FUNDED, day);
        put("construction", project.id(), project);
        put("city_build_links", project.id(), new com.medievalfactions.domain.MFCityPlanner.ConstructionLink(
                planId, buildingId, project.id()));
        var staged = com.medievalfactions.domain.MFCityPlanner.start(plan, buildingId);
        staged.ifPresent(value -> put("city_plans", planId, value));
        return Optional.of(project);
    }

    private void completeLinkedCityBuilding(ConstructionProject project) {
        var link = record("city_build_links", project.id(), com.medievalfactions.domain.MFCityPlanner.ConstructionLink.class).orElse(null);
        if (link == null) return;
        var plan = record("city_plans", link.cityPlanId(), com.medievalfactions.domain.MFCityPlanner.CityPlan.class).orElse(null);
        if (plan != null) {
            var completed = com.medievalfactions.domain.MFCityPlanner.advance(plan, link.buildingId(), Integer.MAX_VALUE);
            completed.ifPresent(value -> put("city_plans", link.cityPlanId(), value));
        }
        remove("city_build_links", project.id());
    }

    public synchronized Optional<com.medievalfactions.domain.MFTacticalWarfare.OrderResult> applyTacticalOrder(
            UUID battleId, com.medievalfactions.domain.MFTacticalWarfare.Order order) {
        var battle = record("tactical_battles", battleId, com.medievalfactions.domain.MFTacticalWarfare.Battle.class).orElse(null);
        var result = com.medievalfactions.domain.MFTacticalWarfare.apply(battle, order);
        result.ifPresent(r -> put("tactical_battles", battleId, r.battle()));
        return result;
    }

    public synchronized Optional<com.medievalfactions.domain.MFTacticalWarfare.Battle> endTacticalTurn(UUID battleId) {
        var battle = record("tactical_battles", battleId, com.medievalfactions.domain.MFTacticalWarfare.Battle.class).orElse(null);
        var next = com.medievalfactions.domain.MFTacticalWarfare.endTurn(battle);
        next.ifPresent(b -> put("tactical_battles", battleId, b));
        return next;
    }

    public synchronized Optional<com.medievalfactions.domain.MFDragonLifecycle.DragonEgg> updateDragonEggEnvironment(
            UUID eggId, int warmth, int care) {
        var egg = record("dragon_eggs", eggId, com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class).orElse(null);
        if (egg == null) return Optional.empty();
        var next = egg.withEnvironment(warmth, care);
        put("dragon_eggs", eggId, next);
        return Optional.of(next);
    }

    /** Atomically commits egg consumption, a genetic hatchling, a live companion ledger and its inherited genome. */
    public synchronized Optional<com.medievalfactions.domain.MFDragonLifecycle.Hatchling> hatchDragonIntoCompanion(
            UUID eggId, UUID hatchlingId, UUID entityId, UUID ownerId) {
        var egg = record("dragon_eggs", eggId, com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class).orElse(null);
        if (egg == null || hatchlingId == null || entityId == null || ownerId == null || !egg.ownerId().equals(ownerId)
                || record("companions", entityId, Companion.class).isPresent()) return Optional.empty();
        var hatchling = com.medievalfactions.domain.MFDragonLifecycle.createHatchling(egg, hatchlingId);
        if (hatchling.isEmpty()) return Optional.empty();
        int health = hatchling.get().maxHealth();
        Companion companion = new Companion(entityId, ownerId, CompanionSpecies.DRAGON, 10, 1, 0L,
                health, health, 100, 100, 20, true);
        var genome = new com.medievalfactions.domain.MFDragonLifecycle.DragonGenomeRecord(
                entityId, ownerId, hatchling.get().genome());
        put("dragon_eggs", eggId, egg.hatch());
        put("dragon_hatchlings", hatchlingId, hatchling.get());
        put("companions", entityId, companion);
        put("dragon_genomes", entityId, genome);
        return hatchling;
    }

    public synchronized Optional<com.medievalfactions.domain.MFDragonLifecycle.Hatchling> hatchDragonEgg(UUID eggId, UUID hatchlingId) {
        var egg = record("dragon_eggs", eggId, com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class).orElse(null);
        var hatchling = com.medievalfactions.domain.MFDragonLifecycle.createHatchling(egg, hatchlingId);
        if (egg == null || hatchling.isEmpty() || record("dragon_hatchlings", hatchlingId,
                com.medievalfactions.domain.MFDragonLifecycle.Hatchling.class).isPresent()) return Optional.empty();
        put("dragon_eggs", eggId, egg.hatch());
        put("dragon_hatchlings", hatchlingId, hatchling.get());
        return hatchling;
    }

    public synchronized Optional<com.medievalfactions.domain.MFFamilyCare.Marriage> proposeMarriage(
            Person a, Person b, UUID proposer, long day) {
        var relationship = records("relationships", Relationship.class).stream().filter(r ->
                (r.personA().equals(a == null ? null : a.id()) && r.personB().equals(b == null ? null : b.id()))
                || (r.personB().equals(a == null ? null : a.id()) && r.personA().equals(b == null ? null : b.id())))
                .findFirst().orElse(null);
        if (a == null || b == null || records("marriages", com.medievalfactions.domain.MFFamilyCare.Marriage.class).stream()
                .anyMatch(m -> m.status() == com.medievalfactions.domain.MFFamilyCare.MarriageStatus.MARRIED
                        && (m.personA().equals(a.id()) || m.personB().equals(a.id())
                            || m.personA().equals(b.id()) || m.personB().equals(b.id())))) return Optional.empty();
        var marriage = com.medievalfactions.domain.MFFamilyCare.propose(a, b, relationship, proposer, day);
        marriage.ifPresent(m -> put("marriages", m.id(), m));
        return marriage;
    }

    public synchronized Optional<com.medievalfactions.domain.MFFamilyCare.Marriage> answerMarriage(
            UUID marriageId, UUID respondent, boolean consent, long day) {
        var marriage = record("marriages", marriageId, com.medievalfactions.domain.MFFamilyCare.Marriage.class).orElse(null);
        if (marriage == null) return Optional.empty();
        try {
            var next = marriage.answer(respondent, consent, day);
            put("marriages", marriageId, next);
            if (next.status() == com.medievalfactions.domain.MFFamilyCare.MarriageStatus.MARRIED) {
                for (Relationship relationship : records("relationships", Relationship.class)) {
                    boolean samePair = (relationship.personA().equals(next.personA()) && relationship.personB().equals(next.personB()))
                            || (relationship.personA().equals(next.personB()) && relationship.personB().equals(next.personA()));
                    if (samePair) {
                        put("relationships", relationship.id(), new Relationship(relationship.id(), relationship.personA(),
                                relationship.personB(), relationship.trust(), RelationshipStatus.MARRIED, true, true,
                                relationship.createdDay()));
                        break;
                    }
                }
            }
            return Optional.of(next);
        } catch (IllegalStateException ex) { return Optional.empty(); }
    }


    public synchronized Optional<com.medievalfactions.domain.MFFamilyCare.Marriage> dissolveMarriage(UUID marriageId) {
        var marriage = record("marriages", marriageId, com.medievalfactions.domain.MFFamilyCare.Marriage.class).orElse(null);
        if (marriage == null || marriage.status() != com.medievalfactions.domain.MFFamilyCare.MarriageStatus.MARRIED)
            return Optional.empty();
        var dissolved = marriage.dissolve();
        put("marriages", marriageId, dissolved);
        for (Relationship relationship : records("relationships", Relationship.class)) {
            boolean samePair = (relationship.personA().equals(marriage.personA()) && relationship.personB().equals(marriage.personB()))
                    || (relationship.personA().equals(marriage.personB()) && relationship.personB().equals(marriage.personA()));
            if (samePair) {
                put("relationships", relationship.id(), new Relationship(relationship.id(), relationship.personA(),
                        relationship.personB(), relationship.trust(), RelationshipStatus.ENDED, false, false,
                        relationship.createdDay()));
                break;
            }
        }
        return Optional.of(dissolved);
    }

    /** Registers genetics only for a live, owned dragon already known to the companion system. */
    public synchronized boolean registerDragonGenome(UUID dragonId, UUID ownerId,
            com.medievalfactions.domain.MFDragonLifecycle.DragonGenome genome) {
        var companion = record("companions", dragonId, Companion.class).orElse(null);
        if (companion == null || !companion.dragon() || !companion.alive() || !companion.ownerId().equals(ownerId)
                || genome == null) return false;
        var value = new com.medievalfactions.domain.MFDragonLifecycle.DragonGenomeRecord(dragonId, ownerId, genome);
        return storeSimulationRecord("dragon_genomes", dragonId, value,
                com.medievalfactions.domain.MFDragonLifecycle.DragonGenomeRecord.class);
    }

    /** Creates and persists a genetic egg after validating both parent dragons and their owners. */
    public synchronized Optional<com.medievalfactions.domain.MFDragonLifecycle.DragonEgg> breedDragons(
            UUID ownerId, UUID parentAId, UUID parentBId, long seed, int day) {
        var parentA = record("companions", parentAId, Companion.class).orElse(null);
        var parentB = record("companions", parentBId, Companion.class).orElse(null);
        var genomeA = record("dragon_genomes", parentAId,
                com.medievalfactions.domain.MFDragonLifecycle.DragonGenomeRecord.class).orElse(null);
        var genomeB = record("dragon_genomes", parentBId,
                com.medievalfactions.domain.MFDragonLifecycle.DragonGenomeRecord.class).orElse(null);
        if (genomeA == null || genomeB == null || !genomeA.ownerId().equals(ownerId) || !genomeB.ownerId().equals(ownerId))
            return Optional.empty();
        var egg = com.medievalfactions.domain.MFDragonLifecycle.breed(ownerId, parentAId, parentBId, parentA, parentB,
                genomeA.genome(), genomeB.genome(), seed, day);
        egg.ifPresent(value -> put("dragon_eggs", value.id(), value));
        return egg;
    }

    /** Registers a clinic using only healing potions already present in the settlement inventory. */
    public synchronized boolean registerClinic(MFWorldSavedData world, UUID settlementId, int tier, int beds) {
        if (world == null || settlementId == null || tier < 1 || tier > 5 || beds < 1) return false;
        MFSettlementState settlement = world.settlement(settlementId).orElse(null);
        if (settlement == null) return false;
        int doctorSkill = world.citizens(settlementId).stream()
                .filter(c -> c.status() == CitizenStatus.ACTIVE && c.profession() == Profession.DOCTOR)
                .mapToInt(c -> Math.min(100, c.professionLevel() * 2)).max().orElse(0);
        if (doctorSkill == 0) return false;
        long stock = settlement.inventory().getOrDefault("medieval_factions:healing_potion", 0L);
        int boundedStock = (int)Math.min(100_000L, stock);
        var clinic = new com.medievalfactions.domain.MFFamilyCare.Clinic(tier, doctorSkill, boundedStock, beds);
        return storeSimulationRecord("clinics", settlementId, clinic, com.medievalfactions.domain.MFFamilyCare.Clinic.class);
    }

    public synchronized Optional<com.medievalfactions.domain.MFFamilyCare.TreatmentResult> treatMedicalCase(
            MFWorldSavedData world, UUID caseId, UUID clinicSettlementId, long day) {
        if (world == null) return Optional.empty();
        var medical = record("medical_cases", caseId, com.medievalfactions.domain.MFFamilyCare.MedicalCase.class).orElse(null);
        var clinic = record("clinics", clinicSettlementId, com.medievalfactions.domain.MFFamilyCare.Clinic.class).orElse(null);
        if (medical == null || clinic == null) return Optional.empty();
        MFSettlementState settlement = world.settlement(clinicSettlementId).orElse(null);
        if (settlement == null) return Optional.empty();
        MFCitizenState patient = world.citizen(medical.patientId()).orElse(null);
        if (patient == null || patient.status() != CitizenStatus.ACTIVE
                || !patient.settlementId().equals(clinicSettlementId) || !patient.factionId().equals(settlement.factionId()))
            return Optional.empty();
        long realMedicine = settlement.inventory().getOrDefault("medieval_factions:healing_potion", 0L);
        List<MFCitizenState> settlementPopulation = world.citizens(clinicSettlementId);
        int liveDoctorSkill = settlementPopulation.stream()
                .filter(c -> c.status() == CitizenStatus.ACTIVE && c.profession() == Profession.DOCTOR)
                .mapToInt(c -> Math.min(100, c.professionLevel() * 2)).max().orElse(0);
        var availableClinic = new com.medievalfactions.domain.MFFamilyCare.Clinic(clinic.tier(),
                Math.min(clinic.doctorSkill(), liveDoctorSkill),
                (int)Math.min(clinic.medicineStock(), Math.min(100_000L, realMedicine)), clinic.beds());
        if (availableClinic.doctorSkill() < 1) return Optional.empty();
        Set<UUID> settlementResidents = new LinkedHashSet<>();
        settlementPopulation.forEach(c -> settlementResidents.add(c.id()));
        List<com.medievalfactions.domain.MFFamilyCare.MedicalCase> activeCases = records("medical_cases",
                com.medievalfactions.domain.MFFamilyCare.MedicalCase.class).stream()
                .filter(c -> c.status() != com.medievalfactions.domain.MFFamilyCare.MedicalStatus.RESOLVED
                        && c.status() != com.medievalfactions.domain.MFFamilyCare.MedicalStatus.DECEASED)
                .filter(c -> settlementResidents.contains(c.patientId()))
                .sorted(java.util.Comparator.comparingLong(com.medievalfactions.domain.MFFamilyCare.MedicalCase::openedDay)
                        .thenComparing(com.medievalfactions.domain.MFFamilyCare.MedicalCase::id)).toList();
        int bedRank = activeCases.indexOf(medical);
        if (bedRank < 0 || bedRank >= availableClinic.beds()) return Optional.empty();
        var result = com.medievalfactions.domain.MFFamilyCare.treat(medical, availableClinic, day);
        if (result.isEmpty()) return Optional.empty();
        Map<String, Long> stock = new LinkedHashMap<>(settlement.inventory());
        long remainingMedicine = realMedicine - 1L;
        if (remainingMedicine == 0L) stock.remove("medieval_factions:healing_potion");
        else stock.put("medieval_factions:healing_potion", remainingMedicine);
        if (!world.replaceSettlement(settlement.withInventory(stock))) return Optional.empty();
        int healed = result.get().healthRestored();
        MFCitizenState updatedPatient = patient.withHealthAndHunger(patient.health() + healed, patient.hunger());
        if (!world.replaceCitizen(updatedPatient)) {
            world.replaceSettlement(settlement);
            return Optional.empty();
        }
        put("medical_cases", caseId, result.get().medicalCase());
        put("clinics", clinicSettlementId, new com.medievalfactions.domain.MFFamilyCare.Clinic(
                clinic.tier(), clinic.doctorSkill(), result.get().clinic().medicineStock(), clinic.beds()));
        return result;
    }

    public synchronized Optional<com.medievalfactions.domain.MFAdventureSystems.BossTurn> advanceBossEncounter(
            UUID encounterId, int partyDefence) {
        var boss = record("boss_encounters", encounterId, com.medievalfactions.domain.MFAdventureSystems.BossEncounter.class).orElse(null);
        if (boss == null || boss.phase() == com.medievalfactions.domain.MFAdventureSystems.EncounterPhase.DEFEATED) return Optional.empty();
        var turn = com.medievalfactions.domain.MFAdventureSystems.advanceBossTurn(boss, partyDefence);
        put("boss_encounters", encounterId, turn.encounter());
        return Optional.of(turn);
    }

    // ------------------------------------------------------------------
    // NPC decision layer
    // ------------------------------------------------------------------
    public synchronized int planNpcPopulation(MFWorldSavedData world, long day) {
        int planned = 0;
        if (world == null) return 0;
        List<MFCitizenState> population = world.citizens(null);
        MFPopulationIndex index = populationIndex(world, population);
        Map<UUID, MFSettlementState> settlementsById = new LinkedHashMap<>();
        for (MFSettlementState settlement : world.settlements()) settlementsById.put(settlement.id(), settlement);

        for (MFCitizenState citizen : population) {
            if (citizen.status() != CitizenStatus.ACTIVE) continue;
            MFSettlementState settlement = settlementsById.get(citizen.settlementId());
            if (settlement == null) continue;
            boolean activeWork = index.isWorking(citizen.id());
            long residents = index.activeCountAt(settlement.id());
            boolean enoughFoodForSettlement = settlement.foodRations() >= residents;
            boolean housingAvailable = residents <= settlement.housingCapacity();
            int threat = settlement.security() <= 10 ? 85 : settlement.security() <= 30 ? 60
                    : settlement.security() <= 50 ? 30 : 0;
            NpcDirective directive = MFAdvancedSystems.planNpc(citizen, threat,
                    enoughFoodForSettlement, housingAvailable, activeWork, Math.max(0L, day));
            // Autonomous workers start a suitable recipe when survival needs are stable and inputs exist.
            if (directive.intent() == NpcIntent.WORK && !activeWork) {
                var candidate = MFRecipeCatalog.all().stream()
                        .filter(recipe -> recipe.profession() == citizen.profession())
                        .filter(recipe -> recipe.inputs().entrySet().stream()
                                .allMatch(input -> settlement.inventory().getOrDefault(input.getKey(), 0L) >= input.getValue()))
                        .findFirst();
                if (candidate.isPresent() && world.startWorkOrder(settlement.id(), citizen.id(), candidate.get().id()).isPresent()) {
                    directive = new NpcDirective(citizen.id(), NpcIntent.CONTINUE_WORK, 70, Math.max(0L, day),
                            "La IA inició automáticamente " + candidate.get().id());
                }
            }
            put("npc_directives", citizen.id(), directive);
            planned++;
        }
        return planned;
    }

    // ------------------------------------------------------------------
    // Caravans and bank operations
    // ------------------------------------------------------------------
    public synchronized Optional<Caravan> dispatchCaravan(MFWorldSavedData world, UUID sellerId, UUID buyerId,
                                                            String itemId, long quantity, long unitPrice,
                                                            int travelDays, int routeRisk, long day) {
        MFSettlementState seller = world.settlement(sellerId).orElse(null);
        MFSettlementState buyer = world.settlement(buyerId).orElse(null);
        if (seller == null || buyer == null) return Optional.empty();
        Optional<CaravanStart> started = MFAdvancedSystems.startCaravan(seller, buyer, itemId, quantity,
                unitPrice, travelDays, routeRisk, day);
        if (started.isEmpty()) return Optional.empty();
        if (!world.replaceSettlementPair(started.get().seller(), started.get().buyer())) return Optional.empty();
        Caravan caravan = started.get().caravan();
        put("caravans", caravan.id(), caravan);
        return Optional.of(caravan);
    }

    public synchronized Optional<BankAccount> openBankAccount(UUID ownerId, UUID branchId, MFWorldSavedData world) {
        if (ownerId == null || world.settlement(branchId).isEmpty()) return Optional.empty();
        boolean exists = records("bank_accounts", BankAccount.class).stream()
                .anyMatch(a -> a.ownerId().equals(ownerId) && a.branchSettlement().equals(branchId));
        if (exists) return Optional.empty();
        BankAccount account = new BankAccount(UUID.randomUUID(), ownerId, branchId, 0L, false);
        put("bank_accounts", account.id(), account);
        return Optional.of(account);
    }

    public synchronized Optional<BankMovement> deposit(MFWorldSavedData world, UUID accountId, long amount) {
        BankAccount account = record("bank_accounts", accountId, BankAccount.class).orElse(null);
        MFSettlementState branch = account == null ? null : world.settlement(account.branchSettlement()).orElse(null);
        if (account == null || branch == null) return Optional.empty();
        Optional<BankMovement> result = MFAdvancedSystems.deposit(account, branch, amount);
        if (result.isEmpty()) return Optional.empty();
        if (!world.replaceSettlement(result.get().branch())) return Optional.empty();
        put("bank_accounts", accountId, result.get().account());
        return result;
    }

    public synchronized Optional<BankMovement> withdraw(MFWorldSavedData world, UUID accountId, long amount) {
        BankAccount account = record("bank_accounts", accountId, BankAccount.class).orElse(null);
        MFSettlementState branch = account == null ? null : world.settlement(account.branchSettlement()).orElse(null);
        if (account == null || branch == null) return Optional.empty();
        Optional<BankMovement> result = MFAdvancedSystems.withdraw(account, branch, amount);
        if (result.isEmpty()) return Optional.empty();
        if (!world.replaceSettlement(result.get().branch())) return Optional.empty();
        put("bank_accounts", accountId, result.get().account());
        return result;
    }

    public synchronized Optional<BankTransfer> transferBank(UUID fromId, UUID toId, long amount) {
        BankAccount from = record("bank_accounts", fromId, BankAccount.class).orElse(null);
        BankAccount to = record("bank_accounts", toId, BankAccount.class).orElse(null);
        Optional<BankTransfer> result = MFAdvancedSystems.transfer(from, to, amount);
        result.ifPresent(t -> { put("bank_accounts", t.source().id(), t.source()); put("bank_accounts", t.target().id(), t.target()); });
        return result;
    }

    public synchronized Optional<Loan> issueLoan(UUID accountId, UUID branchId, long principal, int interestBps, MFWorldSavedData world) {
        BankAccount account = record("bank_accounts", accountId, BankAccount.class).orElse(null);
        MFSettlementState branch = world.settlement(branchId).orElse(null);
        if (account == null || branch == null || !account.branchSettlement().equals(branchId)) return Optional.empty();
        Optional<LoanIssue> issue = MFAdvancedSystems.issueLoan(account, branch, principal, interestBps);
        if (issue.isEmpty() || !world.replaceSettlement(issue.get().branch())) return Optional.empty();
        put("bank_accounts", accountId, issue.get().account());
        put("loans", issue.get().loan().id(), issue.get().loan());
        return Optional.of(issue.get().loan());
    }

    public synchronized Optional<Loan> repayLoan(UUID loanId, long amount, MFWorldSavedData world) {
        Loan loan = record("loans", loanId, Loan.class).orElse(null);
        BankAccount account = loan == null ? null : record("bank_accounts", loan.borrowerAccount(), BankAccount.class).orElse(null);
        MFSettlementState branch = loan == null ? null : world.settlement(loan.lenderSettlement()).orElse(null);
        if (loan == null || account == null || branch == null || account.frozen() || amount <= 0
                || account.balanceCopper() < amount || amount > loan.outstandingCopper()) return Optional.empty();
        // Repayment moves an existing bank deposit from the customer's liability balance to extinguish
        // the loan asset. The branch reserve was increased when that cash was originally deposited,
        // so adding it again here would duplicate money in the treasury.
        Loan updatedLoan = loan.applyRepayment(amount);
        put("bank_accounts", account.id(), account.withBalance(account.balanceCopper() - amount));
        put("loans", loanId, updatedLoan);
        return Optional.of(updatedLoan);
    }

    // ------------------------------------------------------------------
    // Diplomacy's military escalation: siege records
    // ------------------------------------------------------------------
    public synchronized Optional<Siege> beginSiege(UUID attacker, UUID defender, UUID settlementId,
                                                    int attackers, int defenders, long attackSupply,
                                                    long defenceSupply, MFWorldSavedData world) {
        MFSettlementState settlement = world.settlement(settlementId).orElse(null);
        if (settlement == null || attacker == null || defender == null || attacker.equals(defender)
                || !settlement.factionId().equals(defender) || world.relation(attacker, defender).map(r -> !r.atWar()).orElse(true))
            return Optional.empty();
        boolean already = records("sieges", Siege.class).stream().anyMatch(s -> s.targetSettlement().equals(settlementId) && s.status() == SiegeStatus.ACTIVE);
        if (already) return Optional.empty();
        Siege siege = new Siege(UUID.randomUUID(), attacker, defender, settlementId, attackers, defenders,
                attackSupply, defenceSupply, settlement.security(), 65, 0, SiegeStatus.ACTIVE);
        put("sieges", siege.id(), siege);
        return Optional.of(siege);
    }

    // ------------------------------------------------------------------
    // Companion, family and expedition creation
    // ------------------------------------------------------------------
    public synchronized Optional<Companion> registerCompanion(UUID owner, CompanionSpecies species,
                                                               int health, int stamina) {
        return registerCompanion(UUID.randomUUID(), owner, species, health, stamina);
    }

    /** The companion record uses the actual Minecraft entity UUID so ownership and progression stay linked. */
    public synchronized Optional<Companion> registerCompanion(UUID entityId, UUID owner, CompanionSpecies species,
                                                               int health, int stamina) {
        if (entityId == null || owner == null || species == null || health < 1 || stamina < 1
                || record("companions", entityId, Companion.class).isPresent()) return Optional.empty();
        // The argument is the real entity's current maximum health, so the ledger never
        // disagrees with species attributes (wolf and phoenix prototypes differ from old defaults).
        int maxHealth = health;
        int maxStamina = species == CompanionSpecies.DRAGON ? 100 : 60;
        Companion companion = new Companion(entityId, owner, species, 0, 1, 0L,
                maxHealth, maxHealth, Math.min(stamina, maxStamina), maxStamina, 0, true);
        put("companions", companion.id(), companion);
        return Optional.of(companion);
    }

    public synchronized boolean updateCompanion(Companion companion) {
        if (companion == null || record("companions", companion.id(), Companion.class).isEmpty()) return false;
        put("companions", companion.id(), companion);
        return true;
    }

    /** One upkeep pulse: mounted companions gain bond/experience while stamina and food are consumed. */
    public synchronized void advanceCompanionUpkeep(Set<UUID> currentlyRiddenEntityIds) {
        Set<UUID> ridden = currentlyRiddenEntityIds == null ? Set.of() : Set.copyOf(currentlyRiddenEntityIds);
        for (Companion companion : records("companions", Companion.class)) {
            if (!companion.alive()) continue;
            Companion updated = ridden.contains(companion.id())
                    ? companion.ride(10).train(1L)
                    : companion.rest(400);
            put("companions", updated.id(), updated);
        }
    }

    public synchronized boolean registerPerson(Person person) {
        if (person == null || record("persons", person.id(), Person.class).isPresent()) return false;
        put("persons", person.id(), person);
        return true;
    }

    public synchronized Optional<Relationship> createRelationship(UUID aId, UUID bId, int trust,
                                                                   boolean consentA, boolean consentB, long day) {
        Person a = record("persons", aId, Person.class).orElse(null);
        Person b = record("persons", bId, Person.class).orElse(null);
        Optional<Relationship> relation = MFAdvancedSystems.createRelationship(a, b, trust, consentA, consentB, day);
        if (relation.isEmpty()) return Optional.empty();
        put("relationships", relation.get().id(), relation.get());
        return relation;
    }

    public synchronized Optional<Pregnancy> conceive(UUID relationshipId, long day, int fertilityRoll, long seed) {
        Relationship relationship = record("relationships", relationshipId, Relationship.class).orElse(null);
        if (relationship == null) return Optional.empty();
        Person a = record("persons", relationship.personA(), Person.class).orElse(null);
        Person b = record("persons", relationship.personB(), Person.class).orElse(null);
        boolean alreadyPregnant = records("pregnancies", Pregnancy.class).stream()
                .anyMatch(p -> p.status() == PregnancyStatus.ACTIVE && (p.motherId().equals(relationship.personA()) || p.motherId().equals(relationship.personB())));
        if (alreadyPregnant) return Optional.empty();
        Optional<Pregnancy> pregnancy = MFAdvancedSystems.conceive(relationship, a, b, day, fertilityRoll, seed);
        pregnancy.ifPresent(p -> put("pregnancies", p.id(), p));
        return pregnancy;
    }

    public synchronized Optional<Expedition> startExpedition(UUID faction, UUID settlement, List<UUID> members,
                                                               long seed, int tier, int days, long provisions,
                                                               int partyPower, MFWorldSavedData world) {
        MFSettlementState state = world.settlement(settlement).orElse(null);
        if (state == null || !state.factionId().equals(faction) || members == null || provisions <= 0
                || !MFAdvancedSystems.validExpeditionRoster(faction, settlement, members, world.citizens(settlement)))
            return Optional.empty();
        Set<UUID> assignedWorkers = new LinkedHashSet<>();
        for (var order : world.workOrders(settlement)) if (order.active()) assignedWorkers.add(order.workerId());
        if (members.stream().anyMatch(assignedWorkers::contains)) return Optional.empty();
        // Reserve the complete provisions cost at departure; no food is fabricated for the party.
        Map<String, Long> inventory = new LinkedHashMap<>(state.inventory());
        long available = inventory.getOrDefault("minecraft:bread", 0L);
        if (available < provisions) return Optional.empty();
        long remaining = available - provisions;
        if (remaining == 0L) inventory.remove("minecraft:bread"); else inventory.put("minecraft:bread", remaining);
        if (!world.replaceSettlement(state.withInventory(inventory))) return Optional.empty();
        Expedition expedition = MFAdvancedSystems.startExpedition(faction, settlement, members, seed, tier, days, provisions, partyPower);
        put("expeditions", expedition.id(), expedition);
        return Optional.of(expedition);
    }

    /**
     * Starts a physical expedition only when the abstract expedition exists, every member is an
     * active adult from the same settlement, no member has a reserved work order, and every member
     * has a linked in-world resident entity. Routes are deliberately single-dimension for this version.
     */
    /** Reserves finite goods and buyer treasury before a physical convoy starts. */
    public synchronized Optional<com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy> dispatchPhysicalConvoy(
            MFWorldSavedData world, UUID sellerId, UUID buyerId, String itemId, long quantity, long escrowCopper,
            List<com.medievalfactions.domain.MFAdventureSystems.RoutePoint> route, int escortStrength, int routeRisk, long seed) {
        if (world == null || sellerId == null || buyerId == null || sellerId.equals(buyerId) || itemId == null
                || !itemId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                || quantity < 1 || escrowCopper < 0 || route == null || route.size() < 2 || route.size() > 4096
                || escortStrength < 0 || escortStrength > 100 || routeRisk < 0 || routeRisk > 100) return Optional.empty();
        MFSettlementState seller = world.settlement(sellerId).orElse(null);
        MFSettlementState buyer = world.settlement(buyerId).orElse(null);
        if (seller == null || buyer == null || seller.inventory().getOrDefault(itemId, 0L) < quantity
                || buyer.treasuryCopper() < escrowCopper) return Optional.empty();
        String dimension = route.get(0).dimension();
        if (route.stream().anyMatch(point -> !dimension.equals(point.dimension()))) return Optional.empty();
        Map<String, Long> sellerStock = new LinkedHashMap<>(seller.inventory());
        long left = sellerStock.getOrDefault(itemId, 0L) - quantity;
        if (left == 0L) sellerStock.remove(itemId); else sellerStock.put(itemId, left);
        MFSettlementState updatedSeller = seller.withInventory(sellerStock);
        MFSettlementState updatedBuyer = buyer.withTreasury(buyer.treasuryCopper() - escrowCopper);
        if (!world.replaceSettlementPair(updatedSeller, updatedBuyer)) return Optional.empty();
        var convoy = new com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy(UUID.randomUUID(), sellerId, buyerId,
                itemId, quantity, quantity, escrowCopper, route, 0, escortStrength, routeRisk,
                com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.TRAVELLING, seed);
        put("physical_convoys", convoy.id(), convoy);
        return Optional.of(convoy);
    }

    /** Resumes a surviving convoy after the owner/administrator acknowledges an ambush. */
    public synchronized Optional<com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy> resumePhysicalConvoy(UUID convoyId) {
        var convoy = record("physical_convoys", convoyId, com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy.class).orElse(null);
        if (convoy == null || convoy.status() != com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.AMBUSHED)
            return Optional.empty();
        var resumed = convoy.resumeAfterAmbush();
        put("physical_convoys", convoyId, resumed);
        return Optional.of(resumed);
    }

    /** Final delivery/refund happens exactly once, after both settlement ledgers can be updated. */
    private boolean settlePhysicalConvoy(MFWorldSavedData world,
            com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy convoy) {
        if (world == null || convoy == null || (convoy.status() != com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DELIVERED
                && convoy.status() != com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DESTROYED)) return false;
        MFSettlementState seller = world.settlement(convoy.sellerSettlement()).orElse(null);
        MFSettlementState buyer = world.settlement(convoy.buyerSettlement()).orElse(null);
        if (seller == null || buyer == null) return false;
        long delivered = convoy.status() == com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DELIVERED
                ? convoy.remainingQuantity() : 0L;
        if (delivered > 0 && !buyer.canAccept(convoy.itemId(), delivered)) return false;
        long payout;
        try {
            payout = java.math.BigInteger.valueOf(convoy.escrowCopper())
                    .multiply(java.math.BigInteger.valueOf(delivered))
                    .divide(java.math.BigInteger.valueOf(convoy.originalQuantity())).longValueExact();
            long refund = convoy.escrowCopper() - payout;
            MFSettlementState updatedSeller = seller.withTreasury(Math.addExact(seller.treasuryCopper(), payout));
            Map<String, Long> buyerInventory = new LinkedHashMap<>(buyer.inventory());
            if (delivered > 0) buyerInventory.put(convoy.itemId(), Math.addExact(buyerInventory.getOrDefault(convoy.itemId(), 0L), delivered));
            MFSettlementState updatedBuyer = new MFSettlementState(buyer.id(), buyer.factionId(), buyer.name(), buyer.tier(),
                    buyer.townHallLevel(), buyer.population(), buyer.housingCapacity(), buyer.foodRations(),
                    Math.addExact(buyer.treasuryCopper(), refund), buyer.security(), buyer.infrastructure(),
                    buyer.dailyMaintenanceCopper(), buyerInventory);
            if (!world.replaceSettlementPair(updatedSeller, updatedBuyer)) return false;
            put("physical_convoys", convoy.id(), convoy.markSettled());
            return true;
        } catch (ArithmeticException ex) {
            return false;
        }
    }

    public synchronized Optional<com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney> startPhysicalExpedition(
            MFWorldSavedData world, com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney journey) {
        if (world == null || journey == null) return Optional.empty();
        Expedition expedition = record("expeditions", journey.expeditionId(), Expedition.class).orElse(null);
        if (expedition == null || (expedition.status() != ExpeditionStatus.TRAVELLING
                && expedition.status() != ExpeditionStatus.EXPLORING)
                || !new LinkedHashSet<>(expedition.memberIds()).equals(new LinkedHashSet<>(journey.memberIds()))
                || expedition.provisions() != journey.provisions()) return Optional.empty();
        String dimension = journey.route().get(0).dimension();
        if (journey.route().stream().anyMatch(point -> !dimension.equals(point.dimension()))) return Optional.empty();
        List<com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney> activeJourneys =
                records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class);
        if (activeJourneys.stream().anyMatch(existing -> existing.expeditionId().equals(journey.expeditionId())
                        && existing.status() != com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.COMPLETED
                        && existing.status() != com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.FAILED)
                || !com.medievalfactions.domain.MFAdventureSystems.membersAvailableForJourney(activeJourneys, journey.memberIds()))
            return Optional.empty();
        MFSettlementState settlement = world.settlement(expedition.settlementId()).orElse(null);
        if (settlement == null || !settlement.factionId().equals(expedition.factionId())) return Optional.empty();
        Set<UUID> working = new LinkedHashSet<>();
        for (var order : world.workOrders(settlement.id())) if (order.active()) working.add(order.workerId());
        for (UUID memberId : journey.memberIds()) {
            var residentLink = record("resident_links", memberId,
                    com.medievalfactions.domain.MFCitizenSimulation.ResidentEntityLink.class).orElse(null);
            if (residentLink == null || !residentLink.dimension().equals(dimension)) return Optional.empty();
            MFCitizenState citizen = world.citizens(settlement.id()).stream()
                    .filter(c -> c.id().equals(memberId) && c.status() == CitizenStatus.ACTIVE
                            && c.factionId().equals(expedition.factionId())
                            && c.ageStage() != AgeStage.BABY && c.ageStage() != AgeStage.CHILD && c.health() > 0)
                    .findFirst().orElse(null);
            if (citizen == null || working.contains(memberId)) return Optional.empty();
        }
        put("expedition_journeys", journey.id(), journey);
        var firstTravelPoint = journey.nextDestination();
        for (UUID memberId : journey.memberIds()) put("citizen_plans", memberId,
                new com.medievalfactions.domain.MFCitizenSimulation.CitizenPlan(memberId,
                        com.medievalfactions.domain.MFCitizenSimulation.Directive.TRAVEL,
                        com.medievalfactions.domain.MFCitizenSimulation.Activity.TRAVEL,
                        new com.medievalfactions.domain.MFCitizenSimulation.Waypoint(firstTravelPoint.dimension(),
                                firstTravelPoint.x(), firstTravelPoint.y(), firstTravelPoint.z()), 95, "Expedición física"));
        return Optional.of(journey);
    }

    /**
     * Confirms a waypoint only when every resident in the expedition is loaded in the same level
     * and physically within range of the destination. This prevents the day simulator from
     * advancing route progress while its entities are still walking.
     */
    public synchronized boolean reportResidentWaypointArrival(ServerLevel level, UUID reportingCitizenId) {
        if (level == null || reportingCitizenId == null) return false;
        String dimension = level.dimension().location().toString();
        for (var journey : records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class)) {
            if (!journey.memberIds().contains(reportingCitizenId)
                    || (journey.status() != com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.OUTBOUND
                        && journey.status() != com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.RETURNING)) continue;
            var target = journey.nextDestination();
            if (!dimension.equals(target.dimension())) continue;
            double tx = target.x() + 0.5D, ty = target.y(), tz = target.z() + 0.5D;
            boolean allArrived = true;
            for (UUID memberId : journey.memberIds()) {
                var link = record("resident_links", memberId,
                        com.medievalfactions.domain.MFCitizenSimulation.ResidentEntityLink.class).orElse(null);
                if (link == null || !dimension.equals(link.dimension())) { allArrived = false; break; }
                var entity = level.getEntity(link.entityId());
                if (!(entity instanceof com.medievalfactions.entity.MFResidentEntity resident)
                        || !memberId.equals(resident.citizenId())
                        || resident.distanceToSqr(tx, ty, tz) > 2.25D) { allArrived = false; break; }
            }
            if (!allArrived) continue;
            var next = journey.arriveAtNextPoint();
            if (next == journey) continue;
            put("expedition_journeys", next.id(), next);
            var destination = next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.EXPLORING
                    || next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.COMPLETED
                    ? next.route().get(next.nextPoint()) : next.nextDestination();
            boolean resting = next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.EXPLORING
                    || next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.COMPLETED;
            for (UUID memberId : next.memberIds()) put("citizen_plans", memberId,
                    new com.medievalfactions.domain.MFCitizenSimulation.CitizenPlan(memberId,
                            resting ? com.medievalfactions.domain.MFCitizenSimulation.Directive.REST
                                    : com.medievalfactions.domain.MFCitizenSimulation.Directive.TRAVEL,
                            resting ? com.medievalfactions.domain.MFCitizenSimulation.Activity.REST
                                    : com.medievalfactions.domain.MFCitizenSimulation.Activity.TRAVEL,
                            new com.medievalfactions.domain.MFCitizenSimulation.Waypoint(destination.dimension(),
                                    destination.x(), destination.y(), destination.z()), 95,
                            resting ? "Parada de expedición" : "Siguiente waypoint de expedición"));
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Material-funded, world-placed construction projects
    // ------------------------------------------------------------------
    public synchronized Optional<ConstructionProject> startConstruction(MFWorldSavedData world, UUID settlementId,
                                                                          UUID ownerId, String dimension,
                                                                          int x, int y, int z, int rotation,
                                                                          String blueprintId, long day) {
        MFSettlementState settlement = world.settlement(settlementId).orElse(null);
        Blueprint blueprint = starterBlueprints().get(blueprintId);
        if (settlement == null || ownerId == null || blueprint == null || rotation < 0 || rotation > 3
                || settlement.tier().ordinal() < blueprint.minSettlementLevel()
                || settlement.treasuryCopper() < blueprint.copperCost()) return Optional.empty();
        Map<String, Long> stock = new LinkedHashMap<>(settlement.inventory());
        for (Map.Entry<String, Long> cost : blueprint.materialCosts().entrySet())
            if (stock.getOrDefault(cost.getKey(), 0L) < cost.getValue()) return Optional.empty();
        for (Map.Entry<String, Long> cost : blueprint.materialCosts().entrySet()) {
            long remaining = stock.get(cost.getKey()) - cost.getValue();
            if (remaining == 0L) stock.remove(cost.getKey()); else stock.put(cost.getKey(), remaining);
        }
        MFSettlementState funded = settlement.withTreasury(settlement.treasuryCopper() - blueprint.copperCost()).withInventory(stock);
        if (!world.replaceSettlement(funded)) return Optional.empty();
        ConstructionProject project = new ConstructionProject(UUID.randomUUID(), settlementId, ownerId, dimension,
                x, y, z, rotation, blueprintId, 0, ConstructionStatus.FUNDED, day);
        put("construction", project.id(), project);
        return Optional.of(project);
    }

    /** Places a maximum of two blocks per active project per server tick; occupied blocks are never overwritten. */
    public synchronized int tickConstruction(ServerLevel level) {
        String dimension = level.dimension().location().toString();
        int placedTotal = 0;
        for (ConstructionProject initial : records("construction", ConstructionProject.class)) {
            if (!(initial.status() == ConstructionStatus.FUNDED || initial.status() == ConstructionStatus.BUILDING)
                    || !initial.dimension().equals(dimension)) continue;
            Blueprint blueprint = starterBlueprints().get(initial.blueprintId());
            if (blueprint == null) continue;
            List<BlockPlacement> placements = MFAdvancedSystems.worldPlacements(blueprint,
                    initial.originX(), initial.originY(), initial.originZ(), initial.rotation());
            int index = initial.nextPlacement();
            int placed = 0;
            boolean blocked = false;
            while (index < placements.size() && placed < 2) {
                BlockPlacement placement = placements.get(index);
                BlockPos pos = new BlockPos(placement.x(), placement.y(), placement.z());
                if (placement.y() < level.getMinBuildHeight() || placement.y() >= level.getMaxBuildHeight() || !level.hasChunkAt(pos)) {
                    blocked = true; break;
                }
                ResourceLocation id = ResourceLocation.tryParse(placement.blockId());
                Block block = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
                if (block == null) { blocked = true; break; }
                var current = level.getBlockState(pos);
                if (current.getBlock() != block && !current.isAir() && !current.canBeReplaced()) {
                    blocked = true; break;
                }
                if (current.getBlock() != block) level.setBlock(pos, block.defaultBlockState(), 3);
                index++;
                placed++;
            }
            if (placed > 0) {
                ConstructionProject updated = initial.advance(placed, placements.size());
                put("construction", updated.id(), updated);
                if (updated.status() == ConstructionStatus.COMPLETED) completeLinkedCityBuilding(updated);
                placedTotal += placed;
            }
            // A blocked project remains paused at the conflicting block so a player can clear the site and resume it.
            if (blocked && initial.status() == ConstructionStatus.FUNDED) {
                put("construction", initial.id(), new ConstructionProject(initial.id(), initial.settlementId(),
                        initial.ownerId(), initial.dimension(), initial.originX(), initial.originY(), initial.originZ(),
                        initial.rotation(), initial.blueprintId(), index, ConstructionStatus.BUILDING, initial.startedDay()));
            }
        }
        return placedTotal;
    }

    // ------------------------------------------------------------------
    // Physical, seeded dungeon generation
    // ------------------------------------------------------------------
    public synchronized Optional<DungeonProject> startDungeonBuild(long seed, int tier, String dimension,
                                                                     int originX, int originY, int originZ) {
        if (tier < 1 || tier > 10 || dimension == null || dimension.isBlank()) return Optional.empty();
        long active = records("dungeons", DungeonProject.class).stream()
                .filter(p -> p.status() != DungeonBuildStatus.COMPLETED).count();
        if (active >= 3) return Optional.empty();
        DungeonProject project = new DungeonProject(UUID.randomUUID(), seed, tier, dimension,
                originX, originY, originZ, 0, DungeonBuildStatus.QUEUED);
        put("dungeons", project.id(), project);
        return Optional.of(project);
    }

    /** Generates at most 160 placements per server tick and refuses to overwrite non-replaceable terrain. */
    public synchronized int tickDungeonProjects(ServerLevel level) {
        String dimension = level.dimension().location().toString();
        int placedTotal = 0;
        for (DungeonProject initial : records("dungeons", DungeonProject.class)) {
            if (initial.status() == DungeonBuildStatus.COMPLETED || !initial.dimension().equals(dimension)) continue;
            List<BlockPlacement> placements = dungeonPlacementCache.computeIfAbsent(initial.id(), ignored ->
                    MFAdvancedSystems.dungeonWorldPlacements(initial.seed(), initial.tier(),
                            initial.originX(), initial.originY(), initial.originZ()));
            int index = initial.nextPlacement();
            int placed = 0;
            boolean blocked = false;
            while (index < placements.size() && placed < 160) {
                BlockPlacement placement = placements.get(index);
                BlockPos pos = new BlockPos(placement.x(), placement.y(), placement.z());
                if (placement.y() < level.getMinBuildHeight() || placement.y() >= level.getMaxBuildHeight() || !level.hasChunkAt(pos)) {
                    blocked = true; break;
                }
                ResourceLocation blockId = ResourceLocation.tryParse(placement.blockId());
                Block block = blockId == null ? null : BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
                if (block == null) { blocked = true; break; }
                var state = level.getBlockState(pos);
                if (state.getBlock() != block && !state.isAir() && !state.canBeReplaced()) { blocked = true; break; }
                if (state.getBlock() != block) {
                    level.setBlock(pos, block.defaultBlockState(), 3);
                    if (placement.blockId().equals("minecraft:chest")) fillDungeonChest(level, pos, initial);
                }
                index++; placed++;
            }
            DungeonProject updated = placed > 0 ? initial.advance(placed, placements.size())
                    : blocked ? initial.blocked() : initial;
            if (!updated.equals(initial)) {
                put("dungeons", updated.id(), updated);
                placedTotal += placed;
                if (updated.status() == DungeonBuildStatus.COMPLETED) spawnDungeonEncounters(level, updated);
            }
        }
        return placedTotal;
    }

    private void fillDungeonChest(ServerLevel level, BlockPos pos, DungeonProject project) {
        if (!(level.getBlockEntity(pos) instanceof Container container) || container.getContainerSize() < 9) return;
        java.util.SplittableRandom random = new java.util.SplittableRandom(project.seed() ^ pos.asLong());
        List<String> loot = project.tier() >= 8
                ? List.of("minecraft:netherite_scrap", "minecraft:diamond", "minecraft:echo_shard", "minecraft:golden_apple", "minecraft:amethyst_shard")
                : project.tier() >= 5
                    ? List.of("minecraft:diamond", "minecraft:emerald", "minecraft:ender_pearl", "minecraft:gold_ingot", "minecraft:golden_apple")
                    : project.tier() >= 3
                        ? List.of("minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:emerald", "minecraft:arrow", "minecraft:bread")
                        : List.of("minecraft:iron_ingot", "minecraft:bread", "minecraft:arrow", "minecraft:coal", "minecraft:gold_nugget");
        int entries = 2 + random.nextInt(3);
        for (int i = 0; i < entries; i++) {
            String itemId = loot.get(random.nextInt(loot.size()));
            ResourceLocation id = ResourceLocation.tryParse(itemId);
            var item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null) continue;
            int amount = itemId.endsWith("netherite_scrap") || itemId.endsWith("diamond") || itemId.endsWith("golden_apple") ? 1
                    : 1 + random.nextInt(Math.min(5, 1 + project.tier()));
            int slot = random.nextInt(container.getContainerSize());
            if (container.getItem(slot).isEmpty()) container.setItem(slot, new ItemStack(item, amount));
        }
        container.setChanged();
    }

    private void spawnDungeonEncounters(ServerLevel level, DungeonProject project) {
        var layout = MFAdvancedSystems.generateDungeon(project.seed(), project.tier());
        int cursor = 0;
        for (var room : layout.rooms()) {
            if (room.kind() == RoomKind.MONSTER || room.kind() == RoomKind.BOSS) {
                int count = room.kind() == RoomKind.BOSS ? 1 : Math.min(3, 1 + project.tier() / 4);
                for (int i = 0; i < count; i++) {
                    var type = room.kind() == RoomKind.BOSS && project.tier() >= 6 ? net.minecraft.world.entity.EntityType.RAVAGER
                            : i % 2 == 0 ? net.minecraft.world.entity.EntityType.ZOMBIE : net.minecraft.world.entity.EntityType.SKELETON;
                    var mob = type.create(level);
                    if (mob != null) {
                        mob.moveTo(project.originX() + cursor + room.width() / 2.0D,
                                project.originY() + 1.0D, project.originZ() + (i - count / 2.0D) * 1.5D, 0.0F, 0.0F);
                        level.addFreshEntity(mob);
                    }
                }
            }
            cursor += room.width() + 4;
        }
    }

    // ------------------------------------------------------------------
    // Daily simulation orchestration
    // ------------------------------------------------------------------
    public synchronized void advanceToDay(MFWorldSavedData world, MFFactionSavedData factions, long targetDay) {
        if (targetDay < 0) return;
        if (lastAdvancedDay < 0) { lastAdvancedDay = targetDay; setDirty(); return; }
        if (targetDay <= lastAdvancedDay) return;
        long steps = Math.min(7L, targetDay - lastAdvancedDay);
        for (long step = 0; step < steps; step++) {
            long day = lastAdvancedDay + 1L;
            advanceCaravans(world, day);
            advanceSieges(world);
            advanceLoans(world);
            advanceCompanionDailyNeeds();
            advanceExpeditions(world);
            advanceSimulationRecords(world, day);
            syncNpcFamilyRecords(world);
            advancePregnancies(world, factions, day);
            planNpcPopulation(world, day);
            lastAdvancedDay = day;
        }
        setDirty();
    }

    private void advanceCaravans(MFWorldSavedData world, long day) {
        for (Caravan original : records("caravans", Caravan.class)) {
            if (original.status() != CaravanStatus.IN_TRANSIT) continue;
            // Stable deterministic ambush roll; risk is reduced by roads/escorts in a later logistics upgrade.
            int ambushRoll = Math.floorMod(original.id().hashCode() + Long.hashCode(day * 31L), 100);
            if (original.routeRisk() > 0 && ambushRoll < original.routeRisk() / 4) {
                MFSettlementState seller = world.settlement(original.sellerSettlement()).orElse(null);
                MFSettlementState buyer = world.settlement(original.buyerSettlement()).orElse(null);
                int lossPercent = MFAdvancedSystems.caravanAmbushLossPercent(original, day);
                Optional<CaravanRaid> raid = MFAdvancedSystems.resolveCaravanRaid(original, seller, buyer, lossPercent);
                if (raid.isPresent() && world.replaceSettlementPair(raid.get().seller(), raid.get().buyer()))
                    put("caravans", original.id(), raid.get().caravan());
                continue;
            }
            Caravan moving = original.advance();
            put("caravans", moving.id(), moving);
            if (moving.daysRemaining() == 0) {
                MFSettlementState seller = world.settlement(moving.sellerSettlement()).orElse(null);
                MFSettlementState buyer = world.settlement(moving.buyerSettlement()).orElse(null);
                Optional<CaravanArrival> arrival = MFAdvancedSystems.arriveCaravan(moving, seller, buyer);
                if (arrival.isPresent() && world.replaceSettlementPair(arrival.get().seller(), arrival.get().buyer())) {
                    // Complete escrow only after both settlement ledgers have committed atomically.
                    put("caravans", moving.id(), arrival.get().caravan());
                }
            }
        }
    }

    /** Enforces a 30-day repayment term plus a 30-day grace period; defaults freeze the borrower's account. */
    private void advanceLoans(MFWorldSavedData world) {
        for (Loan original : records("loans", Loan.class)) {
            if (original.status() != LoanStatus.ACTIVE) continue;
            Loan next = original.advanceDay();
            if (next.status() == LoanStatus.DEFAULTED) {
                BankAccount account = record("bank_accounts", next.borrowerAccount(), BankAccount.class).orElse(null);
                MFSettlementState branch = world.settlement(next.lenderSettlement()).orElse(null);
                if (account != null && !account.frozen()) {
                    LoanDefaultSettlement recovery = settleLoanDefault(next, account, branch);
                    // Commit the reserve recovery before updating the account liability.
                    // If the branch no longer exists, the account is frozen without inventing a recovery.
                    if (branch != null && recovery.recoveredCopper() > 0
                            && !world.replaceSettlement(recovery.branch())) {
                        recovery = new LoanDefaultSettlement(account.freeze(true), branch, 0L);
                    }
                    put("bank_accounts", account.id(), recovery.account());
                }
            }
            if (next != original) put("loans", next.id(), next);
        }
    }

    /** Passive dragon hunger advances once per simulated game day, never every server upkeep pulse. */
    private void advanceCompanionDailyNeeds() {
        for (Companion companion : records("companions", Companion.class)) {
            if (!companion.alive() || !companion.dragon()) continue;
            Companion updated = companion.advanceDailyNeeds();
            if (updated != companion) put("companions", updated.id(), updated);
        }
    }

    private void advanceSieges(MFWorldSavedData world) {
        for (Siege original : records("sieges", Siege.class)) {
            if (original.status() != SiegeStatus.ACTIVE) continue;
            Siege next = original.advance();
            put("sieges", next.id(), next);
            if (next.status() != SiegeStatus.ACTIVE) {
                MFSettlementState settlement = world.settlement(next.targetSettlement()).orElse(null);
                if (settlement != null) {
                    int security = next.status() == SiegeStatus.ATTACKER_VICTORY ? 0
                            : next.status() == SiegeStatus.DEFENDER_VICTORY ? Math.min(100, settlement.security() + 5) : settlement.security();
                    world.replaceSettlement(settlement.withSecurityAndInfrastructure(security, settlement.infrastructure()));
                }
            }
        }
    }

    /** Advances the new simulation layer once per game day; physical world interaction remains server-event driven. */
    private void advanceSimulationRecords(MFWorldSavedData world, long day) {
        // Reuse one indexed citizen snapshot for all medical and expedition checks in this day.
        List<MFCitizenState> population = world == null ? List.of() : world.citizens(null);
        MFPopulationIndex index = populationIndex(world, population);

        for (com.medievalfactions.domain.MFTacticalWarfare.ArmyColumn army : records("army_columns", com.medievalfactions.domain.MFTacticalWarfare.ArmyColumn.class)) {
            var next = army.advanceDay(day);
            if (next != army) put("army_columns", next.id(), next);
        }
        for (com.medievalfactions.domain.MFDragonLifecycle.DragonEgg egg : records("dragon_eggs", com.medievalfactions.domain.MFDragonLifecycle.DragonEgg.class)) {
            var next = egg.advanceDay((int)Math.min(Integer.MAX_VALUE, day));
            if (next != egg) put("dragon_eggs", next.id(), next);
        }
        for (com.medievalfactions.domain.MFDragonLifecycle.Hatchling hatchling : records("dragon_hatchlings", com.medievalfactions.domain.MFDragonLifecycle.Hatchling.class)) {
            var next = hatchling.advanceDay();
            if (next != hatchling) put("dragon_hatchlings", next.id(), next);
        }
        for (com.medievalfactions.domain.MFFamilyCare.MedicalCase medical : records("medical_cases", com.medievalfactions.domain.MFFamilyCare.MedicalCase.class)) {
            if (medical.status() == com.medievalfactions.domain.MFFamilyCare.MedicalStatus.RESOLVED
                    || medical.status() == com.medievalfactions.domain.MFFamilyCare.MedicalStatus.DECEASED) continue;
            MFCitizenState patient = index.citizen(medical.patientId()).orElse(null);
            if (patient == null || patient.status() != CitizenStatus.ACTIVE) continue;
            MFSettlementState settlement = world.settlement(patient.settlementId()).orElse(null);
            boolean safe = settlement != null && settlement.security() >= 40;
            boolean fed = settlement != null && settlement.foodRations() > 0 && patient.hunger() < 80;
            var next = medical.advanceDay(safe, fed);
            if (next != medical) {
                if (next.status() == com.medievalfactions.domain.MFFamilyCare.MedicalStatus.DECEASED) {
                    world.replaceCitizen(patient.withHealthAndHunger(0, patient.hunger()));
                } else if (safe && fed && next.status() == com.medievalfactions.domain.MFFamilyCare.MedicalStatus.RECOVERING) {
                    world.replaceCitizen(patient.withHealthAndHunger(Math.min(100, patient.health() + 1), Math.max(0, patient.hunger() - 2)));
                }
                put("medical_cases", next.id(), next);
            }
        }
        for (com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy convoy : records("physical_convoys", com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy.class)) {
            if (convoy.status() == com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DELIVERED
                    || convoy.status() == com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DESTROYED) {
                settlePhysicalConvoy(world, convoy);
                continue;
            }
            if (convoy.status() != com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.TRAVELLING) continue;
            long rollSeed = convoy.seed() ^ convoy.id().getMostSignificantBits()
                    ^ convoy.id().getLeastSignificantBits() ^ (day * 0x9E3779B97F4A7C15L);
            int ambushRoll = Math.floorMod(Long.hashCode(rollSeed), 100);
            com.medievalfactions.domain.MFAdventureSystems.PhysicalConvoy next;
            if (convoy.effectiveRisk() > 0 && ambushRoll < convoy.effectiveRisk() / 12)
                next = convoy.ambush(Math.max(10, Math.min(65, convoy.effectiveRisk() / 2)));
            else next = convoy.advance();
            if (next != convoy) put("physical_convoys", next.id(), next);
            if (next.status() == com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DELIVERED
                    || next.status() == com.medievalfactions.domain.MFAdventureSystems.ConvoyStatus.DESTROYED)
                settlePhysicalConvoy(world, next);
        }
        for (com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney journey : records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class)) {
            if (journey.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.COMPLETED
                    || journey.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.FAILED) continue;
            long seed = journey.expeditionId().getMostSignificantBits() ^ journey.expeditionId().getLeastSignificantBits() ^ (day * 0x9E3779B97F4A7C15L);
            int dangerRoll = Math.floorMod(Long.hashCode(seed), 100);
            int discoveryRoll = Math.floorMod(Long.hashCode(seed ^ 0xD1B54A32D192ED03L), 100);
            var next = journey.advance(day, dangerRoll < 9, discoveryRoll < 22);
            if (next != journey) {
                put("expedition_journeys", next.id(), next);
                if (world != null && next.status() != com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.FAILED) {
                    var point = next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.OUTBOUND
                            || next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.RETURNING
                            ? next.nextDestination() : next.route().get(next.nextPoint());
                    boolean resting = next.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.EXPLORING;
                    for (UUID memberId : next.memberIds()) {
                        if (!index.isActive(memberId)) continue;
                        put("citizen_plans", memberId, new com.medievalfactions.domain.MFCitizenSimulation.CitizenPlan(memberId,
                                resting ? com.medievalfactions.domain.MFCitizenSimulation.Directive.REST
                                        : com.medievalfactions.domain.MFCitizenSimulation.Directive.TRAVEL,
                                resting ? com.medievalfactions.domain.MFCitizenSimulation.Activity.REST
                                        : com.medievalfactions.domain.MFCitizenSimulation.Activity.TRAVEL,
                                new com.medievalfactions.domain.MFCitizenSimulation.Waypoint(point.dimension(), point.x(), point.y(), point.z()),
                                95, "Ruta de expedición"));
                    }
                }
            }
        }
    }

    private void advanceExpeditions(MFWorldSavedData world) {
        for (Expedition original : records("expeditions", Expedition.class)) {
            if (original.status() == ExpeditionStatus.COMPLETED || original.status() == ExpeditionStatus.FAILED) continue;
            UUID currentExpeditionId = original.id();
            var physical = records("expedition_journeys", com.medievalfactions.domain.MFAdventureSystems.ExpeditionJourney.class).stream()
                    .filter(j -> j.expeditionId().equals(currentExpeditionId)).findFirst().orElse(null);
            if (physical != null) {
                if (physical.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.OUTBOUND
                        || physical.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.EXPLORING
                        || physical.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.RETURNING) continue;
                if (physical.status() == com.medievalfactions.domain.MFAdventureSystems.JourneyStatus.FAILED) {
                    put("expeditions", original.id(), new Expedition(original.id(), original.factionId(), original.settlementId(),
                            original.memberIds(), original.dungeonSeed(), original.dungeonTier(), 0, physical.provisions(),
                            original.partyPower(), ExpeditionStatus.FAILED, null, 0));
                    continue;
                }
                original = new Expedition(original.id(), original.factionId(), original.settlementId(), original.memberIds(),
                        original.dungeonSeed(), original.dungeonTier(), 0, physical.provisions(), original.partyPower(),
                        ExpeditionStatus.RETURNING, null, 0);
            }
            Expedition next = original.status() == ExpeditionStatus.RETURNING ? original : original.advance();
            if (next.status() == ExpeditionStatus.FAILED) { put("expeditions", next.id(), next); continue; }
            if (next.status() == ExpeditionStatus.RETURNING) {
                ExpeditionResolution resolution = MFAdvancedSystems.resolveExpedition(next, next.dungeonTier());
                Expedition resolved = resolution.expedition();
                if (resolved.status() == ExpeditionStatus.COMPLETED && resolved.rewardItem() != null
                        && !world.addSettlementStock(resolved.settlementId(), resolved.rewardItem(), resolved.rewardAmount())) {
                    // Keep the returning party pending. It will retry delivery without consuming more provisions.
                    put("expeditions", next.id(), next);
                    continue;
                }
                next = resolved;
            }
            put("expeditions", next.id(), next);
        }
    }

    /** Gives each persistent NPC a stable sex, affinity, element and family name, then keeps age/health in sync. */
    private void syncNpcFamilyRecords(MFWorldSavedData world) {
        for (MFCitizenState citizen : world.citizens(null)) {
            Person old = record("persons", citizen.id(), Person.class).orElse(null);
            if (old == null) {
                long seed = citizen.id().getMostSignificantBits() ^ citizen.id().getLeastSignificantBits();
                java.util.SplittableRandom random = new java.util.SplittableRandom(seed);
                Affinity affinity = random.nextBoolean() ? Affinity.SOLAR : Affinity.LUNAR;
                Element element = MFGameCatalog.elementsFor(affinity).get(random.nextInt(MFGameCatalog.elementsFor(affinity).size()));
                Sex sex = random.nextBoolean() ? Sex.FEMALE : Sex.MALE;
                String[] parts = citizen.name().trim().split("\\s+");
                String surname = parts.length > 1 ? parts[parts.length - 1] : citizen.name();
                put("persons", citizen.id(), new Person(citizen.id(), citizen.factionId(), citizen.race(), affinity, element,
                        sex, citizen.ageDays(), citizen.health(), surname));
            } else if (old.ageDays() != citizen.ageDays() || old.health() != citizen.health()) {
                put("persons", citizen.id(), new Person(old.id(), old.factionId(), old.race(), old.affinity(), old.element(),
                        old.sex(), citizen.ageDays(), citizen.health(), old.familyName()));
            }
        }
    }

    private void advancePregnancies(MFWorldSavedData world, MFFactionSavedData factions, long day) {
        MFPopulationIndex index = populationIndex(world, world == null ? List.of() : world.citizens(null));
        for (Pregnancy pregnancy : records("pregnancies", Pregnancy.class)) {
            if (!pregnancy.due(day)) continue;
            Person mother = record("persons", pregnancy.motherId(), Person.class).orElse(null);
            Person father = record("persons", pregnancy.fatherId(), Person.class).orElse(null);
            Optional<Birth> birth = MFAdvancedSystems.giveBirth(pregnancy, mother, father, day);
            if (birth.isEmpty()) { put("pregnancies", pregnancy.id(), pregnancy.lose()); continue; }
            Person child = birth.get().child();
            put("persons", child.id(), child);
            put("lineage", child.id(), new Lineage(child.id(), pregnancy.motherId(), pregnancy.fatherId(), birth.get().inheritedFamilyName()));
            // When the mother is a registered citizen, add the baby to her settlement as a dependent NPC.
            MFCitizenState motherCitizen = index.citizen(mother.id()).orElse(null);
            if (motherCitizen != null) {
                RaceFamily primaryRace = factions.getFaction(motherCitizen.factionId()).map(f -> f.primaryRace()).orElse(null);
                MFCitizenState baby = MFCitizenState.createWithId(child.id(), motherCitizen.factionId(), motherCitizen.settlementId(),
                        "Child-" + child.id().toString().substring(0, 6), child.race(), Profession.FARMER, 0L, 1, 0L);
                world.addCitizen(baby, primaryRace);
            }
            put("pregnancies", pregnancy.id(), pregnancy.complete());
        }
    }

    public record Lineage(UUID personId, UUID motherId, UUID fatherId, String familyName) {}

    // ------------------------------------------------------------------
    // SavedData serialization. Each typed record is stored as bounded JSON in its own NBT entry.
    // ------------------------------------------------------------------
    public static MFAdvancedSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        MFAdvancedSavedData data = new MFAdvancedSavedData();
        CompoundTag root = tag.getCompound("advanced_records");
        long totalJsonChars = 0L;
        for (String bucket : RECORD_TYPES.keySet()) {
            if (!root.contains(bucket, Tag.TAG_LIST)) continue;
            ListTag list = root.getList(bucket, Tag.TAG_COMPOUND);
            Class<?> type = RECORD_TYPES.get(bucket);
            int limit = Math.min(list.size(), MAX_RECORDS_PER_BUCKET);
            for (int i = 0; i < limit; i++) try {
                CompoundTag item = list.getCompound(i);
                if (!item.hasUUID("id")) continue;
                UUID id = item.getUUID("id");
                String json = item.getString("json");
                if (json.isBlank() || json.length() > MAX_RECORD_JSON_CHARS) continue;
                if (totalJsonChars + json.length() > MAX_TOTAL_RECORD_JSON_CHARS) break;
                totalJsonChars += json.length();
                Object value = GSON.fromJson(json, type);
                if (id != null && value != null && type.isInstance(value)
                        && !data.buckets.get(bucket).containsKey(id)) data.buckets.get(bucket).put(id, value);
            } catch (Exception ignored) { /* Drop malformed individual advanced records, not the world. */ }
        }
        data.lastAdvancedDay = tag.contains("last_advanced_day", Tag.TAG_LONG) ? Math.max(-1L, tag.getLong("last_advanced_day")) : -1L;
        return data;
    }

    @Override public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("last_advanced_day", lastAdvancedDay);
        CompoundTag root = new CompoundTag();
        long totalJsonChars = 0L;
        for (String bucket : RECORD_TYPES.keySet()) {
            ListTag list = new ListTag();
            int written = 0;
            for (Map.Entry<UUID, Object> entry : buckets.get(bucket).entrySet()) {
                if (written >= MAX_RECORDS_PER_BUCKET) break;
                try {
                    String json = GSON.toJson(entry.getValue());
                    if (json.length() > MAX_RECORD_JSON_CHARS
                            || totalJsonChars + json.length() > MAX_TOTAL_RECORD_JSON_CHARS) continue;
                    CompoundTag item = new CompoundTag(); item.putUUID("id", entry.getKey()); item.putString("json", json); list.add(item);
                    totalJsonChars += json.length();
                    written++;
                } catch (RuntimeException ignored) { /* Keep saving the rest of the world if one optional record is damaged. */ }
            }
            root.put(bucket, list);
        }
        tag.put("advanced_records", root);
        return tag;
    }
}
