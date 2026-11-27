package com.medievalfactions.gametest;

import com.medievalfactions.MedievalFactions;
import com.medievalfactions.data.MFAdvancedSavedData;
import com.medievalfactions.data.MFFactionSavedData;
import com.medievalfactions.data.MFWorldSavedData;
import com.medievalfactions.domain.MFCitizenSimulation;
import com.medievalfactions.domain.MFWorkstationRules;
import com.medievalfactions.domain.MFAdvancedSystems;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFRaceBiology;
import com.medievalfactions.domain.MFWorkOrderState;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.registry.MFBlocks;
import com.medievalfactions.registry.MFEntities;
import com.medievalfactions.registry.MFItems;
import com.medievalfactions.registry.MFSounds;
import com.medievalfactions.item.MFPotionItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import java.util.UUID;

/** Runtime registry and persistent-data checks executed against a real NeoForge GameTestServer. */
@GameTestHolder(MedievalFactions.MOD_ID)
public final class MFRegistryGameTests {
    private MFRegistryGameTests() {}

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
