package com.medievalfactions.tools;

import com.medievalfactions.economy.MFCurrency;
import com.medievalfactions.production.MFProductionEngine;
import com.medievalfactions.simulation.MFOfflineSimulation;
import com.medievalfactions.domain.MFCalendar;
import com.medievalfactions.domain.MFCharacterProfile;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFDiplomacyRelation;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.domain.MFSettlementState;
import com.medievalfactions.domain.MFTradeEngine;
import com.medievalfactions.domain.MFSettlementSimulation;
import com.medievalfactions.domain.MFSkillTreeCatalog;
import com.medievalfactions.domain.MFRecipeCatalog;
import com.medievalfactions.domain.MFWorkOrderState;
import com.medievalfactions.domain.MFCombatRewardRules;
import com.medievalfactions.domain.MFRaceBiology;

import java.util.Set;
import java.util.UUID;

import java.util.Map;

/** Dependency-free smoke tests for the pure-Java domain core. */
public final class DomainCoreSelfTest {
    private static int assertions;
    private DomainCoreSelfTest() {}

    public static void main(String[] args) {
        currencyTests();
        productionTests();
        offlineSimulationTests();
        characterTests();
        calendarTests();
        settlementAndPopulationTests();
        diplomacyAndTradeTests();
        workOrderDomainTests();
        combatRewardTests();
        raceBiologyTests();
        System.out.println("Domain core tests passed: " + assertions + " assertions.");
    }

    private static void currencyTests() {
        check(MFCurrency.COPPER_PER_GOLD == 100L, "gold scale");
        check(MFCurrency.adjust(100L, -50L).orElseThrow() == 50L, "currency adjustment");
        check(MFCurrency.adjust(0L, -1L).isEmpty(), "no negative balance");
        check(MFCurrency.adjust(Long.MAX_VALUE, 1L).isEmpty(), "overflow rejected");
        var transfer = MFCurrency.transfer(500L, 200L, 125L).orElseThrow();
        check(transfer.sourceCopper() == 375L && transfer.targetCopper() == 325L, "balanced transfer");
        check(MFCurrency.transfer(10L, Long.MAX_VALUE, 1L).isEmpty(), "atomic overflow rejection");
        var exactCoins = MFCurrency.breakdown(111L).orElseThrow();
        check(exactCoins.gold() == 1L && exactCoins.silver() == 1L && exactCoins.copper() == 1L,
                "physical coin breakdown is exact");
        var payment = MFCurrency.payment(151L, 1L, 4L, 11L).orElseThrow();
        check(payment.totalCopper() == 151L, "inventory coin payment selects exact denominations");
        check(MFCurrency.payment(15L, 1L, 1L, 4L).isEmpty(),
                "payment never overpays or invents change for an unavailable denomination");
        check(MFCurrency.payment(Long.MAX_VALUE, 0L, 0L, 0L).isEmpty(), "cannot pay without physical coins");
    }

    private static void productionTests() {
        MFProductionEngine engine = new MFProductionEngine();
        engine.createInventory("ore_store", 32L);
        engine.createInventory("ingot_store", 1L);
        engine.registerRecipe(new MFProductionEngine.Recipe(
                "sunsteel_smelting",
                Map.of("medieval_factions:sunsteel_fragment", 1L),
                Map.of("medieval_factions:sunsteel_ingot", 1L),
                2L));
        check(engine.deposit("ore_store", "medieval_factions:sunsteel_fragment", 2L), "deposit inputs");
        var job = engine.startJob("smelter_01", "ore_store", "ingot_store", "sunsteel_smelting").orElseThrow();
        check(engine.inventory("ore_store").items().get("medieval_factions:sunsteel_fragment") == 1L, "inputs reserved at start");
        check(engine.startJob("smelter_01", "ore_store", "ingot_store", "sunsteel_smelting").isEmpty(), "one job per worker");
        engine.tick();
        check(engine.job(job.id()).orElseThrow().status() == MFProductionEngine.JobStatus.RUNNING, "job duration enforced");
        engine.tick();
        check(engine.job(job.id()).orElseThrow().status() == MFProductionEngine.JobStatus.COMPLETED, "job completes");
        check(engine.inventory("ingot_store").items().get("medieval_factions:sunsteel_ingot") == 1L, "output created once");

        // Full output storage blocks completion without losing the produced item.
        engine.createInventory("blocked_store", 1L);
        check(engine.deposit("blocked_store", "minecraft:stone", 1L), "fill output storage");
        check(engine.startJob("smelter_02", "ore_store", "blocked_store", "sunsteel_smelting").isPresent(), "second worker starts");
        engine.tick(); engine.tick();
        long secondJob = engine.jobs().get(1).id();
        check(engine.job(secondJob).orElseThrow().status() == MFProductionEngine.JobStatus.READY_TO_OUTPUT, "full storage blocks output");
        check(engine.withdraw("blocked_store", "minecraft:stone", 1L), "free output capacity");
        check(engine.job(secondJob).orElseThrow().status() == MFProductionEngine.JobStatus.COMPLETED, "output resumes after storage freed");
        check(engine.inventory("blocked_store").items().get("medieval_factions:sunsteel_ingot") == 1L, "blocked output delivered exactly once");

        // Unstarted cancellation returns the reserved input.
        check(engine.deposit("ore_store", "medieval_factions:sunsteel_fragment", 1L), "restock one fragment");
        check(engine.startJob("smelter_03", "ore_store", "ingot_store", "sunsteel_smelting").isPresent(), "third worker starts");
        long thirdJob = engine.jobs().get(2).id();
        check(engine.cancelUnstartedJob(thirdJob), "unstarted job cancels");
        check(engine.inventory("ore_store").items().get("medieval_factions:sunsteel_fragment") == 1L, "cancel refunds input");
        var inProgress = MFWorkOrderState.start(UUID.randomUUID(), UUID.randomUUID(), "forge_iron_sword").advance();
        var aborted = inProgress.abort();
        check(aborted.status() == MFWorkOrderState.Status.ABORTED && !aborted.active(),
                "worker loss can terminate an in-progress order without pretending it completed");
    }

    private static void offlineSimulationTests() {
        check(MFOfflineSimulation.boundedElapsedSeconds(100L, 200L, 50L) == 50L, "offline catch-up capped");
        check(MFOfflineSimulation.boundedElapsedSeconds(100L, 100_000L) == MFOfflineSimulation.DEFAULT_MAX_CATCH_UP_SECONDS, "default six-hour cap");
        check(MFOfflineSimulation.boundedElapsedSeconds(200L, 100L, 50L) == 0L, "clock rollback safe");
        check(MFOfflineSimulation.boundedElapsedSeconds(100L, 100L, 50L) == 0L, "zero elapsed");
        check(MFOfflineSimulation.simulationSteps(100L, 10L, 6L) == 6L, "simulation steps capped");
        check(MFOfflineSimulation.simulationSteps(99L, 10L, 20L) == 9L, "simulation step conversion");
    }

    private static void characterTests() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var p = MFCharacterProfile.create(id, MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.CombatClass.BARD, MFGameCatalog.Profession.MINER, 7L);
        check(p.element().affinity() == p.affinity(), "random element follows random affinity");
        check(p.talentPointsEarned() == 0 && MFCharacterProfile.earnedTalentPoints(65) == 32, "talent points cap at 32");
        check(p.traitsAtDayTime(1000L).vitality() == (p.affinity() == MFGameCatalog.Affinity.SOLAR), "cycle daytime trait");
        check(p.traitsAtDayTime(13000L).spirituality() == (p.affinity() == MFGameCatalog.Affinity.LUNAR), "cycle nighttime trait");
        long xp = 0L;
        for (int level = 1; level < 17; level++) xp += MFCharacterProfile.combatXpForNextLevel(level);
        p = p.awardCombatXp(xp);
        check(p.classLevel() == 17, "combat progression reaches level 17");
        check(MFSkillTreeCatalog.nodes(MFGameCatalog.CombatClass.BARD).size() == 56, "Bard has a complete two-branch tree");
        check(MFSkillTreeCatalog.nodes(MFGameCatalog.CombatClass.WARRIOR).size() == 60, "three-branch classes have full trees");
        int subclassTotal = 0;
        for (var combatClass : MFGameCatalog.CombatClass.values())
            subclassTotal += MFSkillTreeCatalog.branches(combatClass).size();
        check(MFGameCatalog.CombatClass.values().length == 15 && subclassTotal == 44,
                "catalog defines fifteen classes and forty-four actual specialization paths");
        var gate = MFSkillTreeCatalog.subclassGate(MFGameCatalog.CombatClass.BARD, "bard.support").orElseThrow();
        var firstBranchFollowup = MFSkillTreeCatalog.nodes(MFGameCatalog.CombatClass.BARD).values().stream()
                .filter(n -> n.branchId().equals("support") && n.branchDepth() == 2).findFirst().orElseThrow();
        MFCharacterProfile beforeBranchGate = p;
        expectFailure(() -> beforeBranchGate.unlockNode(firstBranchFollowup.id()), "skill tree enforces branch prerequisite");
        p = p.unlockNode(gate.id());
        MFCharacterProfile afterGate = p;
        check(p.talentPointsSpent() == 1 && p.talentPointsAvailable() == 7, "free core and paid branch point accounting");
        expectFailure(() -> afterGate.chooseSubclass("warrior.vanguard"), "subclass must belong to main class");
        MFCharacterProfile beforeTrial = p;
        expectFailure(() -> beforeTrial.chooseSubclass("bard.support"), "subclass requires trial");
        p = p.completeSubclassTrial().chooseSubclass("bard.support");
        check("bard.support".equals(p.subclass()), "subclass unlock after level, branch node and trial");
        MFCharacterProfile afterSubclass = p;
        expectFailure(() -> afterSubclass.chooseSubclass("bard.physical"), "only one active subclass");
    }

    private static void calendarTests() {
        var first = MFCalendar.fromAbsoluteDay(0L);
        check(first.year() == 1L && first.month().equals("Aural") && first.dayOfMonth() == 1, "calendar epoch");
        check(first.weekday() == MFGameCatalog.DayName.AUREN, "calendar weekday origin");
        var nextYear = MFCalendar.fromAbsoluteDay(378L);
        check(nextYear.year() == 2L && nextYear.month().equals("Aural"), "378-day year");
        var last = MFCalendar.fromAbsoluteDay(377L);
        check(last.season() == MFGameCatalog.Season.DORMANCY && last.month().equals("Eterion") && last.dayOfMonth() == 21, "last calendar day");
    }

    private static void settlementAndPopulationTests() {
        UUID newbornId = UUID.randomUUID();
        UUID newbornFaction = UUID.randomUUID(), newbornSettlement = UUID.randomUUID();
        var newborn = MFCitizenState.createWithId(newbornId, newbornFaction, newbornSettlement, "Newborn",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 1, 0L);
        check(newborn.id().equals(newbornId) && newborn.ageStage() == MFGameCatalog.AgeStage.BABY,
                "newborn citizen preserves the family registry identity");
        UUID faction = UUID.randomUUID();
        var settlement = MFSettlementState.found(faction, "Alba");
        check(settlement.tier() == MFGameCatalog.SettlementTier.CAMP && settlement.townHallLevel() == 1, "founding starts as camp with town hall");
        var citizen = MFCitizenState.create(faction, settlement.id(), "Aldric", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.MINER, 5L, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        var unfed = citizen.advanceDay(false, 0L, false);
        check(unfed.hunger() == 18 && unfed.satisfaction() < citizen.satisfaction(), "hunger and safety affect wellbeing");
        var paidAndFed = citizen.advanceDay(true, 5L, true);
        check(paidAndFed.purseCopper() == 5L && paidAndFed.hunger() == 0, "wage transfers to citizen purse and ration is consumed");
        var trained = citizen.awardProfessionXp(100_000L);
        check(trained.professionLevel() == trained.professionPotential(), "NPC profession progression respects individual potential");
        check(settlement.foodDays() == Integer.MAX_VALUE, "empty settlement has no population food ratio");
        var villageRequirements = com.medievalfactions.domain.MFSettlementRules.requirements(MFGameCatalog.SettlementTier.VILLAGE);
        check(villageRequirements.minimumInfrastructure() > 0 && villageRequirements.minimumTreasuryCopper() > 0L,
                "settlement advancement requires infrastructure and a real treasury reserve");
        check(com.medievalfactions.domain.MFSettlementRules.next(MFGameCatalog.SettlementTier.CITY) == MFGameCatalog.SettlementTier.CAPITAL
                && com.medievalfactions.domain.MFSettlementRules.next(MFGameCatalog.SettlementTier.CAPITAL) == null,
                "settlement tiers advance sequentially to one terminal capital tier");

        var populated = new MFSettlementState(UUID.randomUUID(), faction, "Northwatch", MFGameCatalog.SettlementTier.CAMP,
                1, 2, 5, 3L, 5L, 20, 0, 0L, Map.of());
        var first = MFCitizenState.create(faction, populated.id(), "Beren", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.MINER, 5L, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        var second = MFCitizenState.create(faction, populated.id(), "Mira", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.BAKER, 5L, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        var day = MFSettlementSimulation.advanceDay(populated, java.util.List.of(first, second));
        check(day.rationsConsumed() == 2L && day.settlement().foodRations() == 1L, "daily tick consumes one ration per active resident");
        check(day.wagesPaidCopper() == 5L && day.settlement().treasuryCopper() == 0L, "daily tick pays wages only from available treasury");
        check(day.residents().get(0).purseCopper() == 5L && day.residents().get(1).purseCopper() == 0L, "wages are paid in insertion order without creating money");

        var fullPurse = new MFCitizenState(UUID.randomUUID(), faction, populated.id(), "Caldor", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.MINER, MFGameCatalog.AgeStage.ADULT, MFGameCatalog.CitizenStatus.ACTIVE,
                100, 0, 65, 5L, Long.MAX_VALUE, 0, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS), 1, 0L, 20);
        var noOverflow = MFSettlementSimulation.advanceDay(populated.withFoodRations(1L).withTreasury(5L), java.util.List.of(fullPurse));
        check(noOverflow.wagesPaidCopper() == 0L && noOverflow.residents().get(0).purseCopper() == Long.MAX_VALUE,
                "wage is not paid when citizen purse cannot receive it without overflow");

        var upkeepSettlement = new MFSettlementState(UUID.randomUUID(), faction, "Upkeep", MFGameCatalog.SettlementTier.VILLAGE,
                1, 1, 5, 1L, 15L, 20, 20, 10L, Map.of());
        var upkeepCitizen = MFCitizenState.create(faction, upkeepSettlement.id(), "Edrin", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.BAKER, 5L, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        var upkeepDay = MFSettlementSimulation.advanceDay(upkeepSettlement, java.util.List.of(upkeepCitizen));
        check(upkeepDay.maintenancePaidCopper() == 10L && upkeepDay.wagesPaidCopper() == 5L
                && upkeepDay.settlement().treasuryCopper() == 0L,
                "daily upkeep is paid from treasury before wages without creating money");

        var hungry = new MFSettlementState(UUID.randomUUID(), faction, "Dryfield", MFGameCatalog.SettlementTier.CAMP,
                1, 1, 5, 0L, 0L, 0, 0, 0L, Map.of());
        var starving = MFCitizenState.create(faction, hungry.id(), "Tarin", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.FARMER, 0L, 20, 100L);
        for (int i = 0; i < 30 && starving.status() == MFGameCatalog.CitizenStatus.ACTIVE; i++) starving = starving.advanceDay(false, 0L, true);
        check(starving.status() == MFGameCatalog.CitizenStatus.DEAD, "prolonged starvation can kill a citizen");
    }

    private static void diplomacyAndTradeTests() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        var relation = new MFDiplomacyRelation(a, b, 0, false, Set.of()).adjust(75);
        check(relation.band().equals("ALLY"), "diplomacy bands");
        relation = relation.declareWar();
        check(relation.atWar() && relation.band().equals("WAR"), "war separate from relation score");
        relation = relation.makePeace(-10);
        check(!relation.atWar() && relation.band().equals("NEUTRAL"), "peace ends war");

        var seller = new MFSettlementState(UUID.randomUUID(), a, "Seller", MFGameCatalog.SettlementTier.CAMP,
                1, 0, 5, 0L, 20L, 20, 0, 0L, Map.of("minecraft:iron_ingot", 5L));
        var buyer = new MFSettlementState(UUID.randomUUID(), b, "Buyer", MFGameCatalog.SettlementTier.CAMP,
                1, 0, 5, 0L, 100L, 20, 0, 0L, Map.of());
        var trade = MFTradeEngine.execute(seller, buyer, "minecraft:iron_ingot", 2L, 30L).orElseThrow();
        check(trade.seller().treasuryCopper() == 80L && trade.buyer().treasuryCopper() == 40L, "trade transfers money only");
        check(trade.seller().inventory().get("minecraft:iron_ingot") == 3L
                && trade.buyer().inventory().get("minecraft:iron_ingot") == 2L, "trade transfers finite goods");
        check(MFTradeEngine.execute(seller, buyer, "minecraft:diamond", 1L, 1L).isEmpty(), "trade rejects nonexistent stock");
    }

    private static void workOrderDomainTests() {
        var recipe = MFRecipeCatalog.recipe("smelt_iron").orElseThrow();
        check(recipe.profession() == MFGameCatalog.Profession.SMELTER, "recipe requires matching profession");
        check(recipe.inputs().get("minecraft:raw_iron") == 1L, "recipe reserves real ore input");
        var order = MFWorkOrderState.start(UUID.randomUUID(), UUID.randomUUID(), "forge_iron_sword");
        check(order.status() == MFWorkOrderState.Status.RUNNING && order.elapsedDays() == 0, "work order starts at zero elapsed days");
        check(order.advance().elapsedDays() == 1, "work order advances one day");
        check(order.advance().status() == MFWorkOrderState.Status.RUNNING, "two-day order runs after first day");
        var ready = order.advance().advance();
        check(ready.status() == MFWorkOrderState.Status.READY_TO_OUTPUT, "two-day recipe becomes ready to output");
        check(ready.complete().status() == MFWorkOrderState.Status.COMPLETED, "ready work order completes");
        expectFailure(() -> order.advance().cancel(), "started work cannot be cancelled for a full refund");
    }

    private static void combatRewardTests() {
        check(MFCombatRewardRules.calculateAward(5, 0, 0) == 100, "first same-type combat reward uses full base");
        check(MFCombatRewardRules.calculateAward(5, 1, 0) == 50, "second same-type reward is halved");
        check(MFCombatRewardRules.calculateAward(5, 2, 0) == 25, "third same-type reward is quartered");
        check(MFCombatRewardRules.calculateAward(5, 3, 0) == 0, "repeat farming same mob type is blocked for the day");
        check(MFCombatRewardRules.calculateAward(50, 0, 950) == 50, "daily combat XP cap is enforced");
        check(MFCombatRewardRules.calculateAward(5, 0, 1_000) == 0, "no combat XP above daily cap");
    }

    private static void raceBiologyTests() {
        for (var race : MFGameCatalog.RaceFamily.values()) {
            var biology = MFRaceBiology.profile(race);
            check(biology != null && biology.adultAgeDays() < biology.elderAgeDays()
                    && biology.elderAgeDays() <= biology.lifeExpectancyDays()
                    && biology.lifeExpectancyDays() <= biology.maximumAgeDays(),
                    "race biology ranges are ordered for " + race);
            check(MFRaceBiology.stageAt(race, biology.adultAgeDays()) == MFGameCatalog.AgeStage.ADULT,
                    "adult stage boundary for " + race);
        }
        for (var element : MFGameCatalog.Element.values())
            check(MFGameCatalog.elementsFor(element.affinity()).contains(element), "element matches its affinity: " + element);
        long humanAdult = MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS);
        var baby = MFCitizenState.create(UUID.randomUUID(), UUID.randomUUID(), "Baby", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.FARMER, 0L, 20, 1L);
        check(baby.ageStage() == MFGameCatalog.AgeStage.BABY, "human baby life stage");
        check(baby.advanceDay(true, 0L, true).ageStage() == MFGameCatalog.AgeStage.BABY, "baby ages within stage");
        var adolescent = MFCitizenState.create(UUID.randomUUID(), UUID.randomUUID(), "Teen", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.FARMER, 0L, 20, humanAdult - 1L);
        check(adolescent.ageStage() == MFGameCatalog.AgeStage.ADOLESCENT, "human adolescent before adulthood");
        check(adolescent.advanceDay(true, 0L, true).ageStage() == MFGameCatalog.AgeStage.ADULT, "human adulthood transition");
        var elf = MFCitizenState.create(UUID.randomUUID(), UUID.randomUUID(), "Elf", MFGameCatalog.RaceFamily.ELVES,
                MFGameCatalog.Profession.LIBRARIAN, 0L, 20, humanAdult);
        check(elf.ageStage() == MFGameCatalog.AgeStage.CHILD || elf.ageStage() == MFGameCatalog.AgeStage.ADOLESCENT,
                "same world age is younger for long-lived elf");
        var childXp = baby.awardProfessionXp(1_000L);
        check(childXp.professionLevel() == 1 && childXp.professionXp() == 0L, "children cannot receive professional training XP");
        var old = MFCitizenState.create(UUID.randomUUID(), UUID.randomUUID(), "Elder", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.FARMER, 0L, 20, MFRaceBiology.profile(MFGameCatalog.RaceFamily.HUMANS).maximumAgeDays() - 1L);
        check(old.advanceDay(true, 0L, true).status() == MFGameCatalog.CitizenStatus.DEAD, "maximum race lifespan causes natural death");
    }

    private static void expectFailure(Runnable action, String message) {
        assertions++;
        try { action.run(); throw new AssertionError("FAILED: " + message); }
        catch (IllegalStateException | IllegalArgumentException expected) { }
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError("FAILED: " + message);
    }
}
