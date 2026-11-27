package com.medievalfactions.gametest;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.data.MFFactionSavedData;
import com.medievalfactions.data.MFWorldSavedData;
import com.medievalfactions.domain.MFCitizenSimulation;
import com.medievalfactions.domain.MFWorkstationRules;
import com.medievalfactions.domain.MFAdvancedSystems;
import com.medievalfactions.domain.MFAdventureSystems;
import com.medievalfactions.domain.MFCityPlanner;
import com.medievalfactions.domain.MFDragonLifecycle;
import com.medievalfactions.domain.MFFamilyCare;
import com.medievalfactions.domain.MFTacticalWarfare;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFRaceBiology;
import com.medievalfactions.domain.MFWorkOrderState;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.entity.MFResidentEntity;
import com.medievalfactions.registry.MFBlocks;
import com.medievalfactions.registry.MFEntities;
import com.medievalfactions.registry.MFItems;
import com.medievalfactions.registry.MFSounds;
import com.medievalfactions.item.MFPotionItem;
import com.medievalfactions.network.MFManagementAccessRules;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Runtime registry and persistent-data checks executed against a real NeoForge GameTestServer. */
@GameTestHolder(MedievalFactions.MOD_ID)
public final class MFRegistryGameTests {
    private MFRegistryGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void managementDashboardAccessAndThrottleAreEnforced(GameTestHelper helper) {
        helper.assertTrue(MFManagementAccessRules.canViewGlobalDashboard(true),
                "Operators must retain access to the global management dashboard");
        helper.assertTrue(!MFManagementAccessRules.canViewGlobalDashboard(false),
                "Non-operators must not receive world-wide faction and settlement data");
        helper.assertTrue(MFManagementAccessRules.mayRequest(100L, 95L),
                "A new dashboard request must be accepted after the five-tick cooldown");
        helper.assertTrue(!MFManagementAccessRules.mayRequest(99L, 95L),
                "Dashboard requests inside the cooldown must be rejected");
        helper.assertTrue(MFManagementAccessRules.mayRequest(94L, 95L),
                "A server time rollback must not lock dashboard access");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void allItemsHaveRuntimeRegistrations(GameTestHelper helper) {
        var items = MFItems.all();
        helper.assertTrue(items.size() == 44, "Expected 44 registered item models, found " + items.size());
        for (var entry : items.entrySet()) {
            var item = entry.getValue().get();
            helper.assertTrue(item != null, "Missing item registration: " + entry.getKey());
            helper.assertTrue(!new ItemStack(item).isEmpty(), "Could not construct item stack: " + entry.getKey());
        }
        helper.assertTrue(MFItems.all().get("hunter_bow").get() instanceof BowItem, "hunter_bow must be a BowItem");
        helper.assertTrue(MFItems.all().get("crossbow_bolt").get() instanceof ArrowItem, "crossbow_bolt must be ArrowItem ammunition");
        helper.assertTrue(MFItems.all().get("fishing_rod").get() instanceof FishingRodItem, "fishing_rod must be functional");
        helper.assertTrue(MFItems.all().get("iron_axe").get() instanceof AxeItem, "iron_axe must be an AxeItem");
        helper.assertTrue(MFItems.all().get("iron_pickaxe").get() instanceof PickaxeItem, "iron_pickaxe must be a PickaxeItem");
        helper.assertTrue(MFItems.all().get("stone_shovel").get() instanceof ShovelItem, "stone_shovel must be a ShovelItem");
        helper.assertTrue(MFItems.all().get("iron_sword").get() instanceof SwordItem, "iron_sword must be a SwordItem");
        helper.assertTrue(MFItems.all().get("orc_shield").get() instanceof ShieldItem, "orc_shield must be a ShieldItem");
        helper.assertTrue(MFItems.all().get("healing_potion").get() instanceof MFPotionItem, "healing_potion must be consumable");
        helper.assertTrue(MFItems.all().get("fire_potion").get() instanceof MFPotionItem, "fire_potion must be consumable");
        helper.assertTrue(MFItems.all().get("blacksmith_anvil").get() instanceof BlockItem, "block item must wrap its registered block");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void everyEntityTypeCanBeConstructed(GameTestHelper helper) {
        verifyEntity(helper, MFEntities.GUARD.get(), "guard");
        verifyEntity(helper, MFEntities.RESIDENT.get(), "resident");
        verifyEntity(helper, MFEntities.HORSE.get(), "horse_mount");
        verifyEntity(helper, MFEntities.DRAGON.get(), "dragon");
        verifyEntity(helper, MFEntities.CENTAUR.get(), "centaur");
        verifyEntity(helper, MFEntities.HARPY.get(), "harpy");
        verifyEntity(helper, MFEntities.MERFOLK.get(), "merfolk");
        verifyEntity(helper, MFEntities.DRACONID.get(), "draconid");
        verifyEntity(helper, MFEntities.WOLF.get(), "giant_wolf");
        verifyEntity(helper, MFEntities.GRIFFIN.get(), "griffin");
        verifyEntity(helper, MFEntities.PHOENIX.get(), "phoenix");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void blocksAndSoundsAreRegistered(GameTestHelper helper) {
        helper.assertTrue(MFBlocks.BLACKSMITH_ANVIL.get() != null, "blacksmith_anvil missing");
        helper.assertTrue(MFBlocks.TOWN_HALL_BRICK.get() != null, "town_hall_brick missing");
        helper.assertTrue(MFBlocks.FORGE_CONTROLLER.get() != null, "forge_controller missing");
        helper.assertTrue(MFBlocks.ARCANE_WORKBENCH.get() != null, "arcane_workbench missing");
        helper.assertTrue(MFBlocks.ROYAL_BANNERS.get() != null, "royal_banners missing");
        helper.assertTrue(MFBlocks.WINDMILL.get() != null, "windmill missing");
        helper.assertTrue(MFBlocks.SUNSTEEL_ORE.get() != null, "sunsteel_ore missing");
        helper.assertTrue(MFBlocks.DEEPSLATE_SUNSTEEL_ORE.get() != null, "deepslate_sunsteel_ore missing");
        helper.assertTrue(MFSounds.COMBAT_SWORD_SWING_LIGHT.get() != null, "combat sword sound missing");
        helper.assertTrue(MFSounds.PROFESSION_MINING_PICK.get() != null, "profession mining sound missing");
        helper.assertTrue(MFSounds.CREATURE_DRAGON_ROAR.get() != null, "dragon roar sound missing");
        helper.succeed();
    }

    /**
     * Uses Minecraft's real CompoundTag and registry provider to round-trip all three
     * SavedData stores. This complements the lightweight javac stub test and catches
     * serialization incompatibilities that are invisible outside a GameTestServer.
     */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void savedDataRoundTripsAndRejectsFutureSchemas(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        UUID founder = UUID.randomUUID();

        MFFactionSavedData factions = new MFFactionSavedData();
        var faction = factions.createFaction("Persisted Keep", founder, MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create GameTest faction"));
        CompoundTag factionTag = factions.save(new CompoundTag(), registries);
        MFFactionSavedData loadedFactions = MFFactionSavedData.load(factionTag, registries);
        helper.assertTrue(loadedFactions.getFaction(faction.id()).isPresent(),
                "Faction record was lost during real NBT round-trip");
        helper.assertTrue(loadedFactions.isMember(faction.id(), founder),
                "Faction membership was lost during real NBT round-trip");

        MFWorldSavedData world = new MFWorldSavedData();
        var profile = world.createProfile(founder, MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.CombatClass.WARRIOR, MFGameCatalog.Profession.MINER, 0x4D465F31L)
                .orElseThrow(() -> new AssertionError("Could not create GameTest profile"));
        var settlement = world.createSettlement(faction.id(), "Persisted Hold")
                .orElseThrow(() -> new AssertionError("Could not create GameTest settlement"));
        helper.assertTrue(world.addSettlementFood(settlement.id(), 37L), "Could not seed settlement food");
        helper.assertTrue(world.adjustSettlementTreasury(settlement.id(), 275L), "Could not seed settlement treasury");
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:oak_log", 2L),
                "Could not seed production ingredients");
        MFCitizenState carpenter = MFCitizenState.create(faction.id(), settlement.id(), "GameTest Carpenter",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(carpenter, faction.primaryRace()), "Could not register production worker");
        var workOrder = world.startWorkOrder(settlement.id(), carpenter.id(), "cut_planks")
                .orElseThrow(() -> new AssertionError("Could not reserve ingredients for production"));

        CompoundTag worldTag = world.save(new CompoundTag(), registries);
        MFWorldSavedData loadedWorld = MFWorldSavedData.load(worldTag, registries);
        helper.assertTrue(loadedWorld.profile(founder).map(p -> p.element() == profile.element()).orElse(false),
                "Character profile was lost during real NBT round-trip");
        helper.assertTrue(loadedWorld.settlement(settlement.id())
                        .map(s -> s.foodRations() == 37L && s.treasuryCopper() == 275L
                                && s.inventory().getOrDefault("minecraft:oak_log", 0L) == 1L)
                        .orElse(false),
                "Settlement stock/treasury or reserved input was lost during real NBT round-trip");
        helper.assertTrue(loadedWorld.citizen(carpenter.id()).isPresent(),
                "Production worker was lost during real NBT round-trip");
        helper.assertTrue(loadedWorld.workOrders(settlement.id()).stream()
                        .anyMatch(order -> order.id().equals(workOrder.id()) && order.status() == MFWorkOrderState.Status.RUNNING),
                "Running production order was lost during real NBT round-trip");
        loadedWorld.advanceOneDay();
        helper.assertTrue(loadedWorld.workOrders(settlement.id()).stream()
                        .anyMatch(order -> order.id().equals(workOrder.id()) && order.status() == MFWorkOrderState.Status.COMPLETED),
                "Restored production order did not resume to completion");
        helper.assertTrue(loadedWorld.settlement(settlement.id())
                        .map(value -> value.inventory().getOrDefault("minecraft:oak_planks", 0L) == 4L).orElse(false),
                "Restored production order did not create its output exactly once");

        MFAdvancedSavedData advanced = new MFAdvancedSavedData();
        UUID citizenId = UUID.randomUUID();
        var schedule = new MFCitizenSimulation.CitizenSchedule(citizenId, List.of(
                new MFCitizenSimulation.ScheduleEntry(480, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint("minecraft:overworld", 12, 64, -8)),
                new MFCitizenSimulation.ScheduleEntry(1080, MFCitizenSimulation.Activity.SLEEP,
                        new MFCitizenSimulation.Waypoint("minecraft:overworld", 2, 64, 3))));
        helper.assertTrue(advanced.storeSimulationRecord("citizen_schedules", citizenId, schedule,
                        MFCitizenSimulation.CitizenSchedule.class),
                "Could not store representative advanced record");
        CompoundTag advancedTag = advanced.save(new CompoundTag(), registries);
        MFAdvancedSavedData loadedAdvanced = MFAdvancedSavedData.load(advancedTag, registries);
        helper.assertTrue(loadedAdvanced.record("citizen_schedules", citizenId,
                        MFCitizenSimulation.CitizenSchedule.class).map(schedule::equals).orElse(false),
                "Nested advanced record was lost or changed during real NBT/JSON round-trip");

        // Old unversioned files are treated as schema zero and remain readable.
        helper.assertTrue(MFFactionSavedData.load(new CompoundTag(), registries).allFactions().isEmpty(),
                "Legacy unversioned faction data should load as empty");
        helper.assertTrue(MFWorldSavedData.load(new CompoundTag(), registries).profile(founder).isEmpty(),
                "Legacy unversioned world data should load as empty");
        helper.assertTrue(MFAdvancedSavedData.load(new CompoundTag(), registries).count("citizen_schedules") == 0,
                "Legacy unversioned advanced data should load as empty");

        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 999);
        helper.assertTrue(rejectsSchema(() -> MFFactionSavedData.load(future, registries)),
                "Faction SavedData accepted a future schema version");
        helper.assertTrue(rejectsSchema(() -> MFWorldSavedData.load(future, registries)),
                "World SavedData accepted a future schema version");
        helper.assertTrue(rejectsSchema(() -> MFAdvancedSavedData.load(future, registries)),
                "Advanced SavedData accepted a future schema version");

        CompoundTag malformed = new CompoundTag();
        malformed.putString("schema_version", "2");
        helper.assertTrue(rejectsSchema(() -> MFFactionSavedData.load(malformed, registries)),
                "Faction SavedData accepted a non-integer schema marker");
        helper.assertTrue(rejectsSchema(() -> MFWorldSavedData.load(malformed, registries)),
                "World SavedData accepted a non-integer schema marker");
        helper.assertTrue(rejectsSchema(() -> MFAdvancedSavedData.load(malformed, registries)),
                "Advanced SavedData accepted a non-integer schema marker");

        helper.succeed();
    }


    /** Residents must reach a compatible physical station before their recipe inputs are reserved. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void autonomousResidentsRequirePhysicalWorkstations(GameTestHelper helper) {
        UUID factionId = UUID.randomUUID();
        MFWorldSavedData world = new MFWorldSavedData();
        var settlement = world.createSettlement(factionId, "Worker Test Town")
                .orElseThrow(() -> new AssertionError("Could not create worker test settlement"));
        helper.assertTrue(world.addSettlementFood(settlement.id(), 10L), "Could not provide worker food");
        var secure = world.settlement(settlement.id()).orElseThrow().withSecurityAndInfrastructure(80, 50);
        helper.assertTrue(world.replaceSettlement(secure), "Could not secure worker test settlement");

        MFCitizenState firstWorker = MFCitizenState.create(factionId, settlement.id(), "Autonomous Carpenter",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(firstWorker, MFGameCatalog.RaceFamily.HUMANS),
                "Could not register autonomous carpenter");
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:oak_log", 1L),
                "Could not stock the funded recipe");

        MFAdvancedSavedData advanced = new MFAdvancedSavedData();
        MFFactionSavedData factions = new MFFactionSavedData();
        // The real server day creates the work directive, but does not reserve
        // inputs until the resident reaches a valid physical workstation.
        advanced.advanceToDay(world, factions, 0L); // first server tick establishes the advanced-data baseline
        world.advanceOneDay();                     // MFWorldEvents.observeMinecraftDay advances core world data
        advanced.advanceToDay(world, factions, 1L);
        var directive = advanced.record("npc_directives", firstWorker.id(),
                MFAdvancedSystems.NpcDirective.class).orElse(null);
        helper.assertTrue(directive != null && directive.intent() == MFAdvancedSystems.NpcIntent.WORK,
                "Daily simulation did not schedule the carpenter to work");
        helper.assertTrue(world.workOrders(settlement.id()).isEmpty(),
                "Daily planning started an order before the resident reached a workstation");

        helper.assertTrue(!MFWorkstationRules.supports("cut_planks", "minecraft:stone"),
                "A stone block was incorrectly accepted as a carpentry station");
        helper.assertTrue(MFWorkstationRules.supports("cut_planks", "minecraft:crafting_table"),
                "Crafting table was not accepted for cut_planks");
        helper.assertTrue(MFWorkstationRules.supports("smelt_iron", "minecraft:blast_furnace"),
                "Blast furnace was not accepted for smelt_iron");
        helper.assertTrue(MFWorkstationRules.supports("forge_iron_sword", "medieval_factions:blacksmith_anvil"),
                "Registered blacksmith anvil was not accepted for forging");
        helper.assertTrue(!MFWorkstationRules.supports("recipe_not_registered", "minecraft:crafting_table"),
                "Unknown recipes must not accept arbitrary stations");
        helper.assertTrue(advanced.startResidentWorkOrder(world, firstWorker.id(), "minecraft:stone", 1L).isEmpty(),
                "Resident started work at an incompatible block");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:oak_log", 0L) == 1L,
                "Rejected workstation consumed the resident's material");

        var started = advanced.startResidentWorkOrder(world, firstWorker.id(), "minecraft:crafting_table", 1L)
                .orElseThrow(() -> new AssertionError("Carpenter could not start at a compatible workstation"));
        helper.assertTrue(started.recipeId().equals("cut_planks")
                        && started.status() == MFWorkOrderState.Status.RUNNING,
                "Carpenter started an unexpected production order");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:oak_log", 0L) == 0L,
                "Starting at the physical station did not reserve the oak log");

        // Repeated station-arrival ticks return the same active work order instead of duplicating it.
        var repeated = advanced.startResidentWorkOrder(world, firstWorker.id(), "minecraft:crafting_table", 1L)
                .orElseThrow(() -> new AssertionError("Existing station work order was not recognized"));
        helper.assertTrue(repeated.id().equals(started.id())
                        && world.workOrders(settlement.id()).stream()
                            .filter(order -> order.workerId().equals(firstWorker.id()) && order.active()).count() == 1L,
                "Repeated workstation arrival duplicated the active order");

        MFCitizenState secondWorker = MFCitizenState.create(factionId, settlement.id(), "Unfunded Carpenter",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(secondWorker, MFGameCatalog.RaceFamily.HUMANS),
                "Could not register second carpenter");
        world.advanceOneDay();                     // advances the running production order exactly once
        advanced.advanceToDay(world, factions, 2L);
        helper.assertTrue(advanced.startResidentWorkOrder(world, secondWorker.id(), "minecraft:crafting_table", 2L).isEmpty(),
                "Carpenter without ingredients started a funded order");
        helper.assertTrue(world.workOrders(settlement.id()).stream()
                        .anyMatch(order -> order.id().equals(started.id())
                                && order.status() == MFWorkOrderState.Status.COMPLETED),
                "Station-funded production did not complete on the next simulation day");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:oak_planks", 0L) == 4L,
                "Completed physical-station job did not create exactly four planks");
        helper.succeed();
    }


    /**
     * Exercises the complete server entity path: a materialized resident reaches
     * its scheduled workstation waypoint and starts the recipe from MFResidentEntity.tick().
     */
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void residentEntityStartsRecipeOnPhysicalWorkstationArrival(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);

        UUID founder = UUID.randomUUID();
        var faction = factions.createFaction("Resident Work Test", founder, MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create entity-work faction"));
        var settlement = world.createSettlement(faction.id(), "Resident Worksite")
                .orElseThrow(() -> new AssertionError("Could not create entity-work settlement"));
        helper.assertTrue(world.addSettlementFood(settlement.id(), 50L), "Could not provide resident food");
        helper.assertTrue(world.replaceSettlement(world.settlement(settlement.id()).orElseThrow()
                .withSecurityAndInfrastructure(80, 50)), "Could not secure entity-work settlement");

        MFCitizenState worker = MFCitizenState.create(faction.id(), settlement.id(), "Physical Carpenter",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(worker, faction.primaryRace()), "Could not register physical carpenter");
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:oak_log", 1L),
                "Could not stock physical carpenter ingredients");

        BlockPos station = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos standingPosition = station.above();
        level.setBlock(station, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        String dimension = level.dimension().location().toString();
        var schedule = new MFCitizenSimulation.CitizenSchedule(worker.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(dimension,
                                standingPosition.getX(), standingPosition.getY(), standingPosition.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule),
                "Could not persist resident's physical workstation schedule");

        // Verify the citizen and timetable before evaluating the minute plan.
        helper.assertTrue(world.citizen(worker.id()).filter(c -> c.status() == MFGameCatalog.CitizenStatus.ACTIVE).isPresent(),
                "Carpenter must remain an active citizen before planning");
        helper.assertTrue(advanced.record("citizen_schedules", worker.id(),
                        MFCitizenSimulation.CitizenSchedule.class).isPresent(),
                "Resident schedule was not persisted");
        helper.assertTrue(advanced.storeSimulationRecord("citizen_plans", worker.id(),
                new MFCitizenSimulation.CitizenPlan(worker.id(), MFCitizenSimulation.Directive.WORK,
                        MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(dimension, standingPosition.getX(),
                                standingPosition.getY(), standingPosition.getZ()),
                        70, "GameTest physical workstation assignment"),
                MFCitizenSimulation.CitizenPlan.class),
                "Could not persist the explicit physical workstation plan");
        helper.assertTrue(advanced.storeSimulationRecord("npc_directives", worker.id(),
                new MFAdvancedSystems.NpcDirective(worker.id(), MFAdvancedSystems.NpcIntent.WORK,
                        70, 1L, "GameTest physical workstation assignment"),
                MFAdvancedSystems.NpcDirective.class),
                "Could not persist the worker's explicit WORK directive");

        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not create MFResidentEntity");
        if (resident == null) return;
        resident.bindCitizen(worker.id());
        resident.setPos(standingPosition.getX() + 0.5D, standingPosition.getY(),
                standingPosition.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add resident entity to GameTest world");

        // MFResidentEntity checks its persistent plan every 10 ticks.
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(world.workOrders(settlement.id()).stream()
                        .anyMatch(order -> order.workerId().equals(worker.id())
                                && order.recipeId().equals("cut_planks")
                                && order.status() == MFWorkOrderState.Status.RUNNING),
                "Resident entity arrival did not start the recipe at the physical crafting table");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:oak_log", 0L) == 0L,
                "Resident entity did not reserve the crafting ingredient");

        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.workOrders(settlement.id()).stream()
                        .filter(order -> order.workerId().equals(worker.id()) && order.active()).count() == 1L,
                "Repeated physical station ticks created duplicate production orders");
        helper.succeed();
    }

    /**
     * Serializes every registered advanced-record bucket with Minecraft's real NBT
     * implementation and Gson, then verifies exact record equality after reload.
     * This guards nested records, enums, UUIDs, collections and domain-specific buckets.
     */
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void everyAdvancedSavedDataBucketRoundTrips(GameTestHelper helper) {
        MFAdvancedSavedData source = new MFAdvancedSavedData();
        Map<String, Object> expected = new java.util.LinkedHashMap<>();
        Map<String, UUID> keys = new java.util.LinkedHashMap<>();

        UUID owner = UUID.randomUUID();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();
        UUID settlementA = UUID.randomUUID();
        UUID settlementB = UUID.randomUUID();
        UUID citizenId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID personAId = UUID.randomUUID();
        UUID personBId = UUID.randomUUID();
        UUID constructionId = UUID.randomUUID();
        UUID cityPlanId = UUID.randomUUID();
        UUID dragonId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        UUID expeditionId = UUID.randomUUID();

        saveAdvancedSample(helper, source, expected, keys, "npc_directives", citizenId,
                new MFAdvancedSystems.NpcDirective(citizenId, MFAdvancedSystems.NpcIntent.WORK, 40, 1L, "Persist test"),
                MFAdvancedSystems.NpcDirective.class);
        var caravan = new MFAdvancedSystems.Caravan(UUID.randomUUID(), settlementA, settlementB, "minecraft:iron_ingot",
                4L, 80L, 2, 15, MFAdvancedSystems.CaravanStatus.IN_TRANSIT, 1L);
        saveAdvancedSample(helper, source, expected, keys, "caravans", caravan.id(), caravan,
                MFAdvancedSystems.Caravan.class);

        UUID accountId = UUID.randomUUID();
        saveAdvancedSample(helper, source, expected, keys, "bank_accounts", accountId,
                new MFAdvancedSystems.BankAccount(accountId, owner, settlementA, 250L, false),
                MFAdvancedSystems.BankAccount.class);
        var loan = new MFAdvancedSystems.Loan(UUID.randomUUID(), accountId, settlementA, 100L, 110L,
                10L, 30, 3, 0, MFAdvancedSystems.LoanStatus.ACTIVE);
        saveAdvancedSample(helper, source, expected, keys, "loans", loan.id(), loan,
                MFAdvancedSystems.Loan.class);
        var siege = new MFAdvancedSystems.Siege(UUID.randomUUID(), factionA, factionB, settlementB,
                40, 25, 300L, 250L, 85, 70, 2, MFAdvancedSystems.SiegeStatus.ACTIVE);
        saveAdvancedSample(helper, source, expected, keys, "sieges", siege.id(), siege,
                MFAdvancedSystems.Siege.class);
        var companion = new MFAdvancedSystems.Companion(UUID.randomUUID(), owner, MFAdvancedSystems.CompanionSpecies.HORSE,
                45, 2, 120L, 30, 30, 25, 30, 10, true);
        saveAdvancedSample(helper, source, expected, keys, "companions", companion.id(), companion,
                MFAdvancedSystems.Companion.class);

        long adultAge = MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS) + 100L;
        var personA = new MFAdvancedSystems.Person(personAId, factionA, MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Affinity.SOLAR, MFGameCatalog.Element.FIRE, MFAdvancedSystems.Sex.FEMALE,
                adultAge, 95, "Ashford");
        var personB = new MFAdvancedSystems.Person(personBId, factionA, MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Affinity.SOLAR, MFGameCatalog.Element.FIRE, MFAdvancedSystems.Sex.MALE,
                adultAge, 90, "Ashford");
        saveAdvancedSample(helper, source, expected, keys, "persons", personAId,
                personA, MFAdvancedSystems.Person.class);
        var relationship = new MFAdvancedSystems.Relationship(UUID.randomUUID(), personAId, personBId, 75,
                MFAdvancedSystems.RelationshipStatus.COURTING, true, true, 1L);
        saveAdvancedSample(helper, source, expected, keys, "relationships", relationship.id(), relationship,
                MFAdvancedSystems.Relationship.class);
        var pregnancy = new MFAdvancedSystems.Pregnancy(UUID.randomUUID(), personAId, personBId, 2L, 120,
                0x4D465F32L, MFAdvancedSystems.PregnancyStatus.ACTIVE);
        saveAdvancedSample(helper, source, expected, keys, "pregnancies", pregnancy.id(), pregnancy,
                MFAdvancedSystems.Pregnancy.class);
        UUID childId = UUID.randomUUID();
        saveAdvancedSample(helper, source, expected, keys, "lineage", childId,
                new MFAdvancedSavedData.Lineage(childId, personAId, personBId, "Ashford"),
                MFAdvancedSavedData.Lineage.class);
        var expedition = new MFAdvancedSystems.Expedition(UUID.randomUUID(), factionA, settlementA, List.of(citizenId),
                41L, 2, 3, 24L, 18, MFAdvancedSystems.ExpeditionStatus.TRAVELLING,
                "minecraft:gold_ingot", 2L);
        saveAdvancedSample(helper, source, expected, keys, "expeditions", expedition.id(), expedition,
                MFAdvancedSystems.Expedition.class);
        saveAdvancedSample(helper, source, expected, keys, "construction", constructionId,
                new MFAdvancedSystems.ConstructionProject(constructionId, settlementA, owner,
                        "minecraft:overworld", 10, 64, 10, 0, "small_house", 2,
                        MFAdvancedSystems.ConstructionStatus.BUILDING, 1L),
                MFAdvancedSystems.ConstructionProject.class);
        var dungeon = new MFAdvancedSystems.DungeonProject(UUID.randomUUID(), 77L, 2, "minecraft:overworld",
                30, 64, 30, 4, MFAdvancedSystems.DungeonBuildStatus.BUILDING);
        saveAdvancedSample(helper, source, expected, keys, "dungeons", dungeon.id(), dungeon,
                MFAdvancedSystems.DungeonProject.class);

        var waypoint = new MFCitizenSimulation.Waypoint("minecraft:overworld", 12, 64, -8);
        saveAdvancedSample(helper, source, expected, keys, "citizen_schedules", citizenId,
                new MFCitizenSimulation.CitizenSchedule(citizenId, List.of(
                        new MFCitizenSimulation.ScheduleEntry(480, MFCitizenSimulation.Activity.WORK, waypoint),
                        new MFCitizenSimulation.ScheduleEntry(1080, MFCitizenSimulation.Activity.SLEEP, waypoint))),
                MFCitizenSimulation.CitizenSchedule.class);
        saveAdvancedSample(helper, source, expected, keys, "citizen_plans", citizenId,
                new MFCitizenSimulation.CitizenPlan(citizenId, MFCitizenSimulation.Directive.WORK,
                        MFCitizenSimulation.Activity.WORK, waypoint, 60, "Trabajo asignado"),
                MFCitizenSimulation.CitizenPlan.class);
        saveAdvancedSample(helper, source, expected, keys, "resident_links", citizenId,
                new MFCitizenSimulation.ResidentEntityLink(citizenId, entityId, "minecraft:overworld"),
                MFCitizenSimulation.ResidentEntityLink.class);

        var cityBuilding = new MFCityPlanner.Building("house-1", "small_house",
                MFCityPlanner.District.RESIDENTIAL, 4, 64, 4, 5, 5, 0,
                MFCityPlanner.BuildStage.FRAME, 2, 8);
        saveAdvancedSample(helper, source, expected, keys, "city_plans", cityPlanId,
                new MFCityPlanner.CityPlan(cityPlanId, settlementA, "minecraft:overworld",
                        new MFCityPlanner.Bounds(-32, 32, -32, 32), List.of(cityBuilding), 3L),
                MFCityPlanner.CityPlan.class);
        saveAdvancedSample(helper, source, expected, keys, "city_build_links", constructionId,
                new MFCityPlanner.ConstructionLink(cityPlanId, "house-1", constructionId),
                MFCityPlanner.ConstructionLink.class);

        UUID unitAId = UUID.randomUUID();
        UUID unitBId = UUID.randomUUID();
        var unitA = new MFTacticalWarfare.Unit(unitAId, factionA, 0, 0, 30, 30, 2, 8, 1,
                80, 75, 2, MFTacticalWarfare.Stance.READY, false);
        var unitB = new MFTacticalWarfare.Unit(unitBId, factionB, 1, 0, 28, 28, 1, 7, 1,
                70, 70, 2, MFTacticalWarfare.Stance.BLOCKING, false);
        UUID battleId = UUID.randomUUID();
        saveAdvancedSample(helper, source, expected, keys, "tactical_battles", battleId,
                new MFTacticalWarfare.Battle(battleId, factionA, factionB, factionA, 1,
                        List.of(unitA, unitB), MFTacticalWarfare.BattleStatus.ACTIVE, null),
                MFTacticalWarfare.Battle.class);
        var armyColumn = new MFTacticalWarfare.ArmyColumn(UUID.randomUUID(), factionA, owner, List.of(unitAId),
                MFTacticalWarfare.Formation.LINE, 25, 100L, 80,
                List.of(waypoint, new MFCitizenSimulation.Waypoint("minecraft:overworld", 24, 64, -8)),
                0, 1L);
        saveAdvancedSample(helper, source, expected, keys, "army_columns", armyColumn.id(), armyColumn,
                MFTacticalWarfare.ArmyColumn.class);
        saveAdvancedSample(helper, source, expected, keys, "occupations", settlementB,
                new MFTacticalWarfare.Occupation(settlementB, factionB, factionA, 1L, 0L, 35, false),
                MFTacticalWarfare.Occupation.class);

        var genome = new MFDragonLifecycle.DragonGenome(MFGameCatalog.Element.FIRE, 75, 68, 80, 55, 45);
        saveAdvancedSample(helper, source, expected, keys, "dragon_genomes", dragonId,
                new MFDragonLifecycle.DragonGenomeRecord(dragonId, owner, genome),
                MFDragonLifecycle.DragonGenomeRecord.class);
        var egg = new MFDragonLifecycle.DragonEgg(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), owner,
                genome, MFDragonLifecycle.EggStatus.INCUBATING, 8, 30, 2, 65, 70);
        saveAdvancedSample(helper, source, expected, keys, "dragon_eggs", egg.id(), egg,
                MFDragonLifecycle.DragonEgg.class);
        var hatchling = new MFDragonLifecycle.Hatchling(UUID.randomUUID(), owner, genome, MFDragonLifecycle.LifeStage.HATCHLING,
                12, 28, 35, 20, 40);
        saveAdvancedSample(helper, source, expected, keys, "dragon_hatchlings", hatchling.id(), hatchling,
                MFDragonLifecycle.Hatchling.class);

        var marriage = new MFFamilyCare.Marriage(UUID.randomUUID(), personAId, personBId, personAId,
                true, false, MFFamilyCare.MarriageStatus.PROPOSED, 1L, -1L);
        saveAdvancedSample(helper, source, expected, keys, "marriages", marriage.id(), marriage,
                MFFamilyCare.Marriage.class);
        var medicalCase = new MFFamilyCare.MedicalCase(UUID.randomUUID(), citizenId, MFFamilyCare.Condition.INJURY,
                45, 5, MFFamilyCare.MedicalStatus.OPEN, 1L, -1L);
        saveAdvancedSample(helper, source, expected, keys, "medical_cases", medicalCase.id(), medicalCase,
                MFFamilyCare.MedicalCase.class);
        saveAdvancedSample(helper, source, expected, keys, "clinics", settlementA,
                new MFFamilyCare.Clinic(2, 65, 7, 4),
                MFFamilyCare.Clinic.class);

        var route = List.of(new MFAdventureSystems.RoutePoint("minecraft:overworld", 0, 64, 0),
                new MFAdventureSystems.RoutePoint("minecraft:overworld", 20, 64, 0));
        var physicalConvoy = new MFAdventureSystems.PhysicalConvoy(UUID.randomUUID(), settlementA, settlementB,
                "minecraft:iron_ingot", 12L, 10L, 120L, route, 0, 20, 15,
                MFAdventureSystems.ConvoyStatus.TRAVELLING, 91L);
        saveAdvancedSample(helper, source, expected, keys, "physical_convoys", physicalConvoy.id(), physicalConvoy,
                MFAdventureSystems.PhysicalConvoy.class);
        var boss = MFAdventureSystems.createBoss(encounterId, "test_boss", 2, 123L);
        saveAdvancedSample(helper, source, expected, keys, "boss_encounters", encounterId, boss,
                MFAdventureSystems.BossEncounter.class);
        saveAdvancedSample(helper, source, expected, keys, "boss_entities", encounterId,
                new MFAdventureSystems.BossEntityLink(encounterId, UUID.randomUUID(),
                        "minecraft:overworld", owner, 2L),
                MFAdventureSystems.BossEntityLink.class);
        var journey = new MFAdventureSystems.ExpeditionJourney(UUID.randomUUID(), expeditionId, List.of(citizenId),
                route, 0, 20L, MFAdventureSystems.JourneyStatus.OUTBOUND, 0, 1, -1L);
        saveAdvancedSample(helper, source, expected, keys, "expedition_journeys", journey.id(), journey,
                MFAdventureSystems.ExpeditionJourney.class);

        helper.assertTrue(expected.size() == 31, "Expected one representative record for all 31 advanced buckets");
        helper.assertTrue(source.count("caravans") == 1 && source.count("expedition_journeys") == 1,
                "Advanced record samples must be indexed by their stable record IDs");
        CompoundTag saved = source.save(new CompoundTag(), helper.getLevel().registryAccess());
        MFAdvancedSavedData restored = MFAdvancedSavedData.load(saved, helper.getLevel().registryAccess());
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            String bucket = entry.getKey();
            Object value = entry.getValue();
            helper.assertTrue(restored.count(bucket) == 1,
                    "Advanced SavedData bucket count changed after reload: " + bucket);
            helper.assertTrue(advancedRecordEquals(restored, bucket, keys.get(bucket), value),
                    "Advanced SavedData record changed after reload: " + bucket + " (" + value.getClass().getSimpleName() + ")");
        }
        helper.succeed();
    }

    private static <T> void saveAdvancedSample(GameTestHelper helper, MFAdvancedSavedData data,
            Map<String, Object> expected, Map<String, UUID> keys, String bucket, UUID id, T value, Class<T> type) {
        helper.assertTrue(data.storeSimulationRecord(bucket, id, value, type),
                "Could not insert advanced persistence sample: " + bucket);
        expected.put(bucket, value);
        keys.put(bucket, id);
    }

    @SuppressWarnings("unchecked")
    private static boolean advancedRecordEquals(MFAdvancedSavedData data, String bucket, UUID id, Object expected) {
        if (expected == null) return false;
        Class<Object> type = (Class<Object>) expected.getClass();
        return data.record(bucket, id, type).map(expected::equals).orElse(false);
    }


    /**
     * Corrupt advanced records are isolated: valid neighbours survive while malformed,
     * oversized, missing-ID and duplicate-ID entries are discarded independently.
     */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void advancedSavedDataSkipsCorruptRecordsWithoutLosingValidOnes(GameTestHelper helper) {
        MFAdvancedSavedData source = new MFAdvancedSavedData();
        UUID validId = UUID.randomUUID();
        var valid = new MFAdvancedSystems.NpcDirective(validId,
                MFAdvancedSystems.NpcIntent.WORK, 65, 3L, "Valid record survives");
        helper.assertTrue(source.storeSimulationRecord("npc_directives", validId, valid,
                        MFAdvancedSystems.NpcDirective.class),
                "Could not create the valid control record");

        CompoundTag saved = source.save(new CompoundTag(), helper.getLevel().registryAccess());
        CompoundTag root = saved.getCompound("advanced_records");
        var records = root.getList("npc_directives", Tag.TAG_COMPOUND);
        helper.assertTrue(records.size() == 1, "Expected exactly one valid control record before corruption injection");
        String validJson = records.getCompound(0).getString("json");

        CompoundTag malformedJson = new CompoundTag();
        malformedJson.putUUID("id", UUID.randomUUID());
        malformedJson.putString("json", "{ this is not valid JSON");
        records.add(malformedJson);

        CompoundTag oversized = new CompoundTag();
        oversized.putUUID("id", UUID.randomUUID());
        oversized.putString("json", "x".repeat(32_001));
        records.add(oversized);

        CompoundTag missingId = new CompoundTag();
        missingId.putString("json", validJson);
        records.add(missingId);

        CompoundTag duplicateId = new CompoundTag();
        duplicateId.putUUID("id", validId);
        duplicateId.putString("json", validJson);
        records.add(duplicateId);

        root.put("npc_directives", records);
        saved.put("advanced_records", root);
        MFAdvancedSavedData restored = MFAdvancedSavedData.load(saved, helper.getLevel().registryAccess());

        helper.assertTrue(restored.count("npc_directives") == 1,
                "Malformed, oversized, missing-ID or duplicate-ID entries were not isolated");
        helper.assertTrue(restored.record("npc_directives", validId, MFAdvancedSystems.NpcDirective.class)
                        .map(valid::equals).orElse(false),
                "A valid advanced record was lost while neighbouring corrupt records were discarded");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void residentMealConsumesFoodAndImprovesNeedsAtomically(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var factions = MFFactionSavedData.get(server);
        var world = MFWorldSavedData.get(server);
        var faction = factions.createFaction("Meal Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create meal test faction"));
        var fedSettlement = world.createSettlement(faction.id(), "Well Stocked")
                .orElseThrow(() -> new AssertionError("Could not create stocked settlement"));
        var emptySettlement = world.createSettlement(faction.id(), "Empty Pantry")
                .orElseThrow(() -> new AssertionError("Could not create empty settlement"));
        helper.assertTrue(world.addSettlementFood(fedSettlement.id(), 3L), "Could not stock meal test settlement");

        MFCitizenState hungry = MFCitizenState.create(faction.id(), fedSettlement.id(), "Hungry Resident",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS)).withHealthAndHunger(80, 60);
        MFCitizenState unfed = MFCitizenState.create(faction.id(), emptySettlement.id(), "Unfed Resident",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS)).withHealthAndHunger(80, 60);
        helper.assertTrue(world.addCitizen(hungry, faction.primaryRace()), "Could not add hungry resident");
        helper.assertTrue(world.addCitizen(unfed, faction.primaryRace()), "Could not add unfed resident");

        helper.assertTrue(world.feedResident(fedSettlement.id(), hungry.id()),
                "Resident with hunger and available food should receive a meal");
        helper.assertTrue(world.settlement(fedSettlement.id()).map(s -> s.foodRations() == 2L).orElse(false),
                "Meal must consume exactly one stored food ration");
        helper.assertTrue(world.citizen(hungry.id()).map(c -> c.hunger() == 25
                        && c.health() == 81 && c.satisfaction() == 67).orElse(false),
                "Meal must reduce hunger, restore a small amount of health and improve satisfaction");
        helper.assertTrue(!world.feedResident(emptySettlement.id(), unfed.id()),
                "A meal must not be fabricated when the settlement has no food");
        helper.assertTrue(world.citizen(unfed.id()).map(c -> c.hunger() == 60
                        && c.health() == 80 && c.satisfaction() == 65).orElse(false),
                "Rejected meal must leave resident needs unchanged");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void residentSocializationImprovesBothCitizensAtomically(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var factions = MFFactionSavedData.get(server);
        var world = MFWorldSavedData.get(server);
        var faction = factions.createFaction("Social Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create social test faction"));
        var firstSettlement = world.createSettlement(faction.id(), "Friendly Village")
                .orElseThrow(() -> new AssertionError("Could not create first social settlement"));
        var secondSettlement = world.createSettlement(faction.id(), "Distant Village")
                .orElseThrow(() -> new AssertionError("Could not create second social settlement"));

        MFCitizenState first = MFCitizenState.create(faction.id(), firstSettlement.id(), "Ari",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        MFCitizenState second = MFCitizenState.create(faction.id(), firstSettlement.id(), "Bram",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        MFCitizenState distant = MFCitizenState.create(faction.id(), secondSettlement.id(), "Cora",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.BAKER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(first, faction.primaryRace()), "Could not add first social resident");
        helper.assertTrue(world.addCitizen(second, faction.primaryRace()), "Could not add second social resident");
        helper.assertTrue(world.addCitizen(distant, faction.primaryRace()), "Could not add distant resident");

        helper.assertTrue(world.socializeCitizens(first.id(), second.id(), 1),
                "Nearby residents in one settlement should be able to socialize");
        helper.assertTrue(world.citizen(first.id()).map(c -> c.satisfaction() == 66).orElse(false),
                "First resident did not receive the social satisfaction gain");
        helper.assertTrue(world.citizen(second.id()).map(c -> c.satisfaction() == 66).orElse(false),
                "Second resident did not receive the social satisfaction gain");
        helper.assertTrue(!world.socializeCitizens(first.id(), distant.id(), 1),
                "Residents in different settlements must not receive a social interaction");
        helper.assertTrue(world.citizen(distant.id()).map(c -> c.satisfaction() == 65).orElse(false),
                "Rejected cross-settlement interaction changed the distant resident");
        helper.assertTrue(!world.socializeCitizens(first.id(), first.id(), 1),
                "A resident cannot socialize with itself");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "residentResources", timeoutTicks = 40)
    public static void lumberjackAndMinerResourceCreditsAreProfessionBound(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        UUID founder = UUID.randomUUID();
        var faction = factions.createFaction("Resource Workers", founder, MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create resource test faction"));
        var settlement = world.createSettlement(faction.id(), "Timber and Ore")
                .orElseThrow(() -> new AssertionError("Could not create resource test settlement"));
        long adultAge = MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS);
        MFCitizenState lumberjack = MFCitizenState.create(faction.id(), settlement.id(), "Lumberjack",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.LUMBERJACK, 0L, 20, adultAge);
        MFCitizenState miner = MFCitizenState.create(faction.id(), settlement.id(), "Miner",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.MINER, 0L, 20, adultAge);
        helper.assertTrue(world.addCitizen(lumberjack, faction.primaryRace()), "Could not register lumberjack");
        helper.assertTrue(world.addCitizen(miner, faction.primaryRace()), "Could not register miner");

        helper.assertTrue(world.creditResidentResource(settlement.id(), lumberjack.id(),
                "minecraft:oak_log", 1L, 8L), "Lumberjack log credit should succeed");
        helper.assertTrue(world.creditResidentResource(settlement.id(), miner.id(),
                "minecraft:raw_iron", 1L, 8L), "Miner ore credit should succeed");
        helper.assertTrue(!world.creditResidentResource(settlement.id(), lumberjack.id(),
                "minecraft:diamond", 1L, 8L), "Lumberjack must not receive mining output");
        helper.assertTrue(!world.creditResidentResource(settlement.id(), miner.id(),
                "minecraft:oak_log", 1L, 8L), "Miner must not receive forestry output");
        var stock = world.settlement(settlement.id()).orElseThrow().inventory();
        helper.assertTrue(stock.getOrDefault("minecraft:oak_log", 0L) == 1L
                        && stock.getOrDefault("minecraft:raw_iron", 0L) == 1L,
                "Rejected profession/resource combinations must not alter settlement stock");
        helper.assertTrue(world.citizen(lumberjack.id()).map(c -> c.professionXp() == 8L).orElse(false)
                        && world.citizen(miner.id()).map(c -> c.professionXp() == 8L).orElse(false),
                "Successful resource work must award profession XP exactly once");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zzResidentAnimalWork", timeoutTicks = 80)
    public static void shepherdShearsNearbySheepAndStoresWool(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = factions.createFaction("Shepherd Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create shepherd test faction"));
        var settlement = world.createSettlement(faction.id(), "Sheep Pasture")
                .orElseThrow(() -> new AssertionError("Could not create shepherd test settlement"));
        MFCitizenState shepherd = MFCitizenState.create(faction.id(), settlement.id(), "Test Shepherd",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.SHEPHERD, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(shepherd, faction.primaryRace()), "Could not register shepherd");
        BlockPos standing = helper.absolutePos(new BlockPos(2, 3, 2));
        var sheep = net.minecraft.world.entity.EntityType.SHEEP.create(level);
        helper.assertTrue(sheep != null, "Could not create test sheep");
        if (sheep == null) return;
        sheep.setPos(standing.getX() + 1.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(sheep), "Could not add test sheep");
        var schedule = new MFCitizenSimulation.CitizenSchedule(shepherd.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(level.dimension().location().toString(),
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule), "Could not save shepherd schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not create shepherd resident");
        if (resident == null) return;
        resident.bindCitizen(shepherd.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add shepherd resident");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(sheep.isSheared(), "Shepherd did not shear the nearby sheep");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:white_wool", 0L) == 1L,
                "Sheared wool was not credited to the settlement store");
        helper.assertTrue(world.citizen(shepherd.id()).map(citizen -> citizen.professionXp() == 10L).orElse(false),
                "Successful shearing did not award exactly ten profession XP");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:white_wool", 0L) == 1L,
                "Repeated resident ticks duplicated the same fleece");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zzzzResidentApiaryWork", timeoutTicks = 80)
    public static void apiculturistHarvestsHoneyWithSmokeAndStoresBottle(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = factions.createFaction("Apiary Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create apiary test faction"));
        var settlement = world.createSettlement(faction.id(), "Honey Yard")
                .orElseThrow(() -> new AssertionError("Could not create apiary test settlement"));
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:glass_bottle", 1L),
                "Could not stock one glass bottle for the apiculturist");
        MFCitizenState apiculturist = MFCitizenState.create(faction.id(), settlement.id(), "Test Apiculturist",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.APICULTURIST, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(apiculturist, faction.primaryRace()),
                "Could not register apiculturist");
        BlockPos standing = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos hive = standing.east();
        BlockPos smoke = hive.below();
        helper.assertTrue(level.setBlock(smoke, Blocks.CAMPFIRE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true), 3),
                "Could not place lit smoke source below bee hive");
        helper.assertTrue(level.setBlock(hive, Blocks.BEEHIVE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL, 5), 3),
                "Could not place a full bee hive");
        var schedule = new MFCitizenSimulation.CitizenSchedule(apiculturist.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(level.dimension().location().toString(),
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule), "Could not save apiculturist schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not create apiculturist resident");
        if (resident == null) return;
        resident.bindCitizen(apiculturist.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add apiculturist resident");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(level.getBlockState(hive).is(Blocks.BEEHIVE)
                        && level.getBlockState(hive).getValue(
                                net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL) == 0,
                "Apiculturist did not reset the full hive after harvesting");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:glass_bottle", 0L) == 0L
                        && world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:honey_bottle", 0L) == 1L,
                "Apiculturist did not exchange one glass bottle for one honey bottle");
        helper.assertTrue(world.citizen(apiculturist.id()).map(c -> c.professionXp() == 10L).orElse(false),
                "Successful honey collection did not award ten profession XP");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:honey_bottle", 0L) == 1L,
                "Repeated resident ticks duplicated the honey bottle");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zzzResidentRancherWork", timeoutTicks = 80)
    public static void rancherMilksNearbyCowUsingSettlementBucket(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = factions.createFaction("Rancher Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create rancher test faction"));
        var settlement = world.createSettlement(faction.id(), "Dairy Farm")
                .orElseThrow(() -> new AssertionError("Could not create rancher test settlement"));
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:bucket", 1L),
                "Could not stock one empty bucket for the rancher");
        MFCitizenState rancher = MFCitizenState.create(faction.id(), settlement.id(), "Test Rancher",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.RANCHER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(rancher, faction.primaryRace()), "Could not register rancher");
        BlockPos standing = helper.absolutePos(new BlockPos(2, 3, 2));
        var cow = EntityType.COW.create(level);
        helper.assertTrue(cow != null, "Could not create test cow");
        if (cow == null) return;
        cow.setPos(standing.getX() + 1.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(cow), "Could not add test cow");
        var schedule = new MFCitizenSimulation.CitizenSchedule(rancher.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(level.dimension().location().toString(),
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule), "Could not save rancher schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not create rancher resident");
        if (resident == null) return;
        resident.bindCitizen(rancher.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add rancher resident");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:bucket", 0L) == 0L
                        && world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:milk_bucket", 0L) == 1L,
                "Rancher did not exchange the stocked empty bucket for one milk bucket");
        helper.assertTrue(world.citizen(rancher.id()).map(c -> c.professionXp() == 10L).orElse(false),
                "Successful milking did not award exactly ten profession XP");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:milk_bucket", 0L) == 1L,
                "Repeated resident ticks duplicated the same milk collection");

        MFCitizenState nonRancher = MFCitizenState.create(faction.id(), settlement.id(), "Test Farmer",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(nonRancher, faction.primaryRace()),
                "Could not register non-rancher transaction guard");
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:bucket", 1L),
                "Could not stock guard-test bucket");
        helper.assertTrue(!world.creditResidentMilk(settlement.id(), nonRancher.id(), 1L, 10L),
                "A non-rancher was allowed to create milk through the rancher transaction");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:bucket", 0L) == 1L,
                "Rejected milking transaction consumed the empty bucket");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "zResidentProfessionActions", timeoutTicks = 80)
    public static void fisherCatchesFishAtRealWaterWorksite(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = factions.createFaction("Fisher Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create fisher test faction"));
        var settlement = world.createSettlement(faction.id(), "Harbor Village")
                .orElseThrow(() -> new AssertionError("Could not create fisher test settlement"));
        MFCitizenState fisher = MFCitizenState.create(faction.id(), settlement.id(), "Test Fisher",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FISHER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(fisher, faction.primaryRace()), "Could not register fisher");
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:fishing_rod", 1L),
                "Could not stock reusable fishing rod");
        BlockPos standing = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos water = standing.east();
        helper.assertTrue(level.setBlock(water, Blocks.WATER.defaultBlockState(), 3),
                "Could not place real water source for fishing");
        var schedule = new MFCitizenSimulation.CitizenSchedule(fisher.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(level.dimension().location().toString(),
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule), "Could not save fisher schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not construct fisher entity");
        if (resident == null) return;
        resident.bindCitizen(fisher.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add fisher to test world");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(level.getBlockState(water).is(Blocks.WATER),
                "Fishing must not consume or replace the water source");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:cod", 0L) == 1L,
                "Fisher did not credit one cod to settlement inventory");
        helper.assertTrue(world.citizen(fisher.id()).map(c -> c.professionXp() == 10L).orElse(false),
                "Successful fishing did not award exactly ten profession XP");
        helper.assertTrue(resident.getMainHandItem().is(net.minecraft.world.item.Items.FISHING_ROD),
                "Fisher must visibly equip the reusable settlement fishing rod");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:fishing_rod", 0L) == 1L,
                "Reusable fishing rod must remain in settlement stock after work");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:cod", 0L) == 1L,
                "Repeated resident ticks duplicated the fish catch");

        var untooledSettlement = world.createSettlement(faction.id(), "Untooled Harbor")
                .orElseThrow(() -> new AssertionError("Could not create untooled fisher settlement"));
        MFCitizenState untooledFisher = MFCitizenState.create(faction.id(), untooledSettlement.id(),
                "Unequipped Fisher", MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FISHER,
                0L, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(untooledFisher, faction.primaryRace()),
                "Could not register untooled fisher");
        helper.assertTrue(!world.creditResidentFish(untooledSettlement.id(), untooledFisher.id(),
                        "minecraft:cod", 1L, 10L),
                "Fisher without a stocked fishing rod must not create fish");
        helper.assertTrue(world.settlement(untooledSettlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:cod", 0L) == 0L
                        && world.citizen(untooledFisher.id()).map(c -> c.professionXp() == 0L).orElse(false),
                "Rejected untooled fishing must not credit output or XP");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "residentResourceActions", timeoutTicks = 80)
    public static void lumberjackPhysicallyFellsLogAndCreditsSettlement(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = factions.createFaction("Forestry Action Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create forestry test faction"));
        var settlement = world.createSettlement(faction.id(), "Timber Camp")
                .orElseThrow(() -> new AssertionError("Could not create forestry test settlement"));
        MFCitizenState worker = MFCitizenState.create(faction.id(), settlement.id(), "Test Lumberjack",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.LUMBERJACK, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(worker, faction.primaryRace()), "Could not register lumberjack");
        BlockPos standing = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos log = standing.east();
        helper.assertTrue(level.setBlock(log, Blocks.OAK_LOG.defaultBlockState(), 3), "Could not place test log");
        var schedule = new MFCitizenSimulation.CitizenSchedule(worker.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(level.dimension().location().toString(),
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule), "Could not save lumberjack schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not construct lumberjack entity");
        if (resident == null) return;
        resident.bindCitizen(worker.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add lumberjack to test world");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(level.getBlockState(log).isAir(), "Lumberjack did not physically remove the log");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:oak_log", 0L) == 1L,
                "Felled log was not credited to settlement inventory");
        helper.assertTrue(world.citizen(worker.id()).map(c -> c.professionXp() == 8L).orElse(false),
                "Successful felling did not award exactly eight profession XP");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:oak_log", 0L) == 1L,
                "Repeated ticks duplicated the felled log credit");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "residentResourceActions", timeoutTicks = 80)
    public static void minerPhysicallyMinesOreAndCreditsSettlement(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        var faction = factions.createFaction("Mining Action Test", UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create mining test faction"));
        var settlement = world.createSettlement(faction.id(), "Ore Camp")
                .orElseThrow(() -> new AssertionError("Could not create mining test settlement"));
        MFCitizenState worker = MFCitizenState.create(faction.id(), settlement.id(), "Test Miner",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.MINER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(worker, faction.primaryRace()), "Could not register miner");
        BlockPos standing = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos ore = standing.east();
        helper.assertTrue(level.setBlock(ore, Blocks.IRON_ORE.defaultBlockState(), 3), "Could not place test ore");
        var schedule = new MFCitizenSimulation.CitizenSchedule(worker.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(level.dimension().location().toString(),
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule), "Could not save miner schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not construct miner entity");
        if (resident == null) return;
        resident.bindCitizen(worker.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add miner to test world");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(level.getBlockState(ore).isAir(), "Miner did not physically remove the iron ore");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:raw_iron", 0L) == 1L,
                "Mined ore was not converted to raw iron in settlement inventory");
        helper.assertTrue(world.citizen(worker.id()).map(c -> c.professionXp() == 8L).orElse(false),
                "Successful mining did not award exactly eight profession XP");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:raw_iron", 0L) == 1L,
                "Repeated ticks duplicated the mined ore credit");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "residentHarvesting", timeoutTicks = 80)
    public static void residentFarmerHarvestsMatureCropIntoSettlementStock(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);

        UUID founder = UUID.randomUUID();
        var faction = factions.createFaction("Farmer Test", founder, MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create farmer test faction"));
        var settlement = world.createSettlement(faction.id(), "Harvest Fields")
                .orElseThrow(() -> new AssertionError("Could not create farmer test settlement"));
        helper.assertTrue(world.addSettlementFood(settlement.id(), 30L), "Could not provide farmer food");
        helper.assertTrue(world.replaceSettlement(world.settlement(settlement.id()).orElseThrow()
                .withSecurityAndInfrastructure(80, 50)), "Could not secure farmer test settlement");

        MFCitizenState farmer = MFCitizenState.create(faction.id(), settlement.id(), "Mature Crop Farmer",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(farmer, faction.primaryRace()), "Could not register farmer");

        BlockPos crop = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos standing = crop.above();
        helper.assertTrue(level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), 3),
                "Could not place farmer test farmland");
        helper.assertTrue(level.setBlock(crop, Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 7), 3),
                "Could not place mature carrot crop");
        helper.assertTrue(level.getBlockState(crop).is(Blocks.CARROTS),
                "Mature carrot crop was not present before resident arrival");

        String dimension = level.dimension().location().toString();
        var schedule = new MFCitizenSimulation.CitizenSchedule(farmer.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(dimension,
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule),
                "Could not save farmer work schedule");
        // Initialize the daily plan using the same progression as the server tick.
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        helper.assertTrue(advanced.record("citizen_plans", farmer.id(),
                        MFCitizenSimulation.CitizenPlan.class)
                        .map(plan -> plan.activity() == MFCitizenSimulation.Activity.WORK).orElse(false),
                "Daily scheduler did not create farmer work plan");

        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not construct farmer resident entity");
        if (resident == null) return;
        resident.bindCitizen(farmer.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add farmer entity to GameTest world");

        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(resident.visualRole() == 1,
                "Farmer profession was not synchronized to the resident renderer");
        helper.assertTrue(level.getBlockState(crop).is(Blocks.CARROTS)
                        && level.getBlockState(crop).getValue(CropBlock.AGE) == 0,
                "Mature crop was not replanted to age zero after harvest; actual block: "
                        + level.getBlockState(crop));
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:carrot", 0L) == 2L,
                "Harvest output was not credited to settlement inventory");
        helper.assertTrue(world.citizen(farmer.id()).map(value -> value.professionXp() == 12L).orElse(false),
                "A successful physical harvest must award farmer profession experience");

        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:carrot", 0L) == 2L,
                "Repeated resident ticks duplicated the same crop harvest");
        helper.assertTrue(world.citizen(farmer.id()).map(value -> value.professionXp() == 12L).orElse(false),
                "Repeated resident ticks duplicated farmer profession experience");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "residentPlanting", timeoutTicks = 80)
    public static void residentFarmerPlantsCropUsingSettlementSeeds(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        MFFactionSavedData factions = MFFactionSavedData.get(server);
        MFWorldSavedData world = MFWorldSavedData.get(server);
        MFAdvancedSavedData advanced = MFAdvancedSavedData.get(server);
        UUID founder = UUID.randomUUID();
        var faction = factions.createFaction("Planter Test", founder, MFGameCatalog.RaceFamily.HUMANS)
                .orElseThrow(() -> new AssertionError("Could not create planter test faction"));
        var settlement = world.createSettlement(faction.id(), "Seed Fields")
                .orElseThrow(() -> new AssertionError("Could not create planter test settlement"));
        helper.assertTrue(world.addSettlementFood(settlement.id(), 30L), "Could not provide farmer food");
        helper.assertTrue(world.replaceSettlement(world.settlement(settlement.id()).orElseThrow()
                .withSecurityAndInfrastructure(80, 50)), "Could not secure planter test settlement");
        helper.assertTrue(world.addSettlementStock(settlement.id(), "minecraft:wheat_seeds", 1L),
                "Could not stock one wheat seed for the farmer");
        MFCitizenState farmer = MFCitizenState.create(faction.id(), settlement.id(), "Seed Planter",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 20,
                MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        helper.assertTrue(world.addCitizen(farmer, faction.primaryRace()), "Could not register planter");
        BlockPos farmland = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos crop = farmland.above();
        BlockPos standing = crop.above();
        helper.assertTrue(level.setBlock(farmland, Blocks.FARMLAND.defaultBlockState(), 3),
                "Could not place planter farmland");
        helper.assertTrue(level.getBlockState(crop).isAir(), "Planter crop space must start empty");
        String dimension = level.dimension().location().toString();
        var schedule = new MFCitizenSimulation.CitizenSchedule(farmer.id(), List.of(
                new MFCitizenSimulation.ScheduleEntry(0, MFCitizenSimulation.Activity.WORK,
                        new MFCitizenSimulation.Waypoint(dimension,
                                standing.getX(), standing.getY(), standing.getZ()))));
        helper.assertTrue(advanced.saveCitizenSchedule(world, schedule),
                "Could not save planter work schedule");
        advanced.advanceToDay(world, factions, 0L);
        advanced.advanceToDay(world, factions, 1L);
        advanced.updateScheduledNpcPlans(world, 0);
        helper.assertTrue(advanced.record("citizen_plans", farmer.id(),
                        MFCitizenSimulation.CitizenPlan.class)
                        .map(plan -> plan.activity() == MFCitizenSimulation.Activity.WORK).orElse(false),
                "Daily scheduler did not create planter work plan");
        MFResidentEntity resident = MFEntities.RESIDENT.get().create(level);
        helper.assertTrue(resident != null, "Could not construct planter resident entity");
        if (resident == null) return;
        resident.bindCitizen(farmer.id());
        resident.setPos(standing.getX() + 0.5D, standing.getY(), standing.getZ() + 0.5D);
        helper.assertTrue(level.addFreshEntity(resident), "Could not add planter resident to GameTest world");
        for (int i = 0; i < 10; i++) resident.tick();
        helper.assertTrue(level.getBlockState(crop).is(Blocks.WHEAT),
                "Farmer did not plant wheat on the empty farmland");
        helper.assertTrue(level.getBlockState(crop).getValue(CropBlock.AGE) == 0,
                "Newly planted wheat must start at age zero");
        helper.assertTrue(world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:wheat_seeds", 0L) == 0L,
                "Planting did not consume exactly one settlement seed");
        for (int i = 0; i < 20; i++) resident.tick();
        helper.assertTrue(level.getBlockState(crop).is(Blocks.WHEAT)
                        && world.settlement(settlement.id()).orElseThrow().inventory()
                        .getOrDefault("minecraft:wheat_seeds", 0L) == 0L,
                "Repeated resident ticks duplicated or removed the planted crop");
        helper.succeed();
    }

    private static boolean rejectsSchema(Runnable loader) {
        try {
            loader.run();
            return false;
        } catch (IllegalStateException expected) {
            return true;
        }
    }

    private static <T extends Entity> void verifyEntity(GameTestHelper helper, EntityType<T> type, String id) {
        T entity = type.create(helper.getLevel());
        helper.assertTrue(entity != null, "Could not construct entity type: " + id);
        if (entity != null) entity.discard();
    }
}
