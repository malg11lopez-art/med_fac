package com.medievalfactions;

import com.medievalfactions.domain.MFCalendar;
import com.medievalfactions.domain.MFCharacterProfile;
import com.medievalfactions.domain.MFCitizenState;
import com.medievalfactions.domain.MFCombatRewardRules;
import com.medievalfactions.domain.MFDiplomacyRelation;
import com.medievalfactions.domain.MFGameCatalog;
import com.medievalfactions.domain.MFRaceBiology;
import com.medievalfactions.domain.MFRecipeCatalog;
import com.medievalfactions.domain.MFSettlementRules;
import com.medievalfactions.domain.MFSettlementSimulation;
import com.medievalfactions.domain.MFSettlementState;
import com.medievalfactions.domain.MFSkillTreeCatalog;
import com.medievalfactions.domain.MFTradeEngine;
import com.medievalfactions.domain.MFWorkOrderState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainSystemsTest {
    @Test
    void affinityElementAndCharacterProgressionStayConsistent() {
        var profile = MFCharacterProfile.create(UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.CombatClass.BARD, MFGameCatalog.Profession.MINER, 12345L);
        assertEquals(profile.affinity(), profile.element().affinity());
        assertEquals(6, MFGameCatalog.elementsFor(profile.affinity()).size());
        assertEquals(32, MFCharacterProfile.earnedTalentPoints(65));
        assertEquals(0, MFCharacterProfile.earnedTalentPoints(2));
        assertEquals(7, MFCharacterProfile.earnedTalentPoints(15));
    }

    @Test
    void eachClassHasItsOwnBranchTreeAndBardHasTwoBranches() {
        assertEquals(15, MFGameCatalog.CombatClass.values().length);
        assertEquals(56, MFSkillTreeCatalog.nodes(MFGameCatalog.CombatClass.BARD).size());
        assertEquals(60, MFSkillTreeCatalog.nodes(MFGameCatalog.CombatClass.WARRIOR).size());
        int subclassCount = 0;
        for (var combatClass : MFGameCatalog.CombatClass.values())
            subclassCount += MFSkillTreeCatalog.branches(combatClass).size();
        assertEquals(44, subclassCount);
    }

    @Test
    void classLevelAndSubclassGateCannotBeBypassed() {
        var profile = MFCharacterProfile.create(UUID.randomUUID(), MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.CombatClass.BARD, MFGameCatalog.Profession.MINER, 9L);
        var initialProfile = profile;
        assertThrows(IllegalStateException.class, () -> initialProfile.chooseSubclass("bard.support"));
        long xp = 0L;
        for (int level = 1; level < 15; level++) xp += MFCharacterProfile.combatXpForNextLevel(level);
        profile = profile.awardCombatXp(xp);
        var gate = MFSkillTreeCatalog.subclassGate(MFGameCatalog.CombatClass.BARD, "bard.support").orElseThrow();
        var profileAtGate = profile;
        assertThrows(IllegalArgumentException.class, () -> profileAtGate.unlockNode(gate.id() + ".invalid"));
        profile = profile.unlockNode(gate.id()).completeSubclassTrial().chooseSubclass("bard.support");
        assertEquals("bard.support", profile.subclass());
        var finalProfile = profile;
        assertThrows(IllegalStateException.class, () -> finalProfile.chooseSubclass("bard.physical"));
    }

    @Test
    void allRaceBiologyProfilesHaveOrderedLifeMilestones() {
        for (var race : MFGameCatalog.RaceFamily.values()) {
            var biology = MFRaceBiology.profile(race);
            assertNotNull(biology);
            assertTrue(biology.adultAgeDays() < biology.elderAgeDays());
            assertTrue(biology.elderAgeDays() <= biology.lifeExpectancyDays());
            assertTrue(biology.lifeExpectancyDays() <= biology.maximumAgeDays());
            assertEquals(MFGameCatalog.AgeStage.ADULT, MFRaceBiology.stageAt(race, biology.adultAgeDays()));
        }
    }

    @Test
    void calendarAndCombatRewardsAreBounded() {
        var nextYear = MFCalendar.fromAbsoluteDay(378L);
        assertEquals(2L, nextYear.year());
        assertEquals("Aural", nextYear.month());
        assertEquals(100, MFCombatRewardRules.calculateAward(5, 0, 0));
        assertEquals(50, MFCombatRewardRules.calculateAward(5, 1, 0));
        assertEquals(25, MFCombatRewardRules.calculateAward(5, 2, 0));
        assertEquals(0, MFCombatRewardRules.calculateAward(5, 3, 0));
        assertEquals(50, MFCombatRewardRules.calculateAward(50, 0, 950));
    }

    @Test
    void settlementMaintenanceAndWagesCannotCreateMoney() {
        UUID faction = UUID.randomUUID();
        var settlement = new MFSettlementState(UUID.randomUUID(), faction, "Aldea", MFGameCatalog.SettlementTier.VILLAGE,
                1, 1, 5, 1L, 15L, 20, 20, 10L, Map.of());
        var citizen = MFCitizenState.create(faction, settlement.id(), "Edrin", MFGameCatalog.RaceFamily.HUMANS,
                MFGameCatalog.Profession.BAKER, 5L, 10, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
        var day = MFSettlementSimulation.advanceDay(settlement, List.of(citizen));
        assertEquals(1L, day.rationsConsumed());
        assertEquals(10L, day.maintenancePaidCopper());
        assertEquals(5L, day.wagesPaidCopper());
        assertEquals(0L, day.settlement().treasuryCopper());
        assertEquals(5L, day.residents().getFirst().purseCopper());
    }

    @Test
    void tradeAndSettlementProgressionRequireFiniteAssets() {
        UUID factionA = UUID.randomUUID(), factionB = UUID.randomUUID();
        var seller = new MFSettlementState(UUID.randomUUID(), factionA, "Seller", MFGameCatalog.SettlementTier.CAMP,
                1, 0, 5, 0L, 0L, 0, 0, 0L, Map.of("minecraft:iron_ingot", 2L));
        var buyer = new MFSettlementState(UUID.randomUUID(), factionB, "Buyer", MFGameCatalog.SettlementTier.CAMP,
                1, 0, 5, 0L, 100L, 0, 0, 0L, Map.of());
        var trade = MFTradeEngine.execute(seller, buyer, "minecraft:iron_ingot", 2L, 30L).orElseThrow();
        assertEquals(60L, trade.seller().treasuryCopper());
        assertEquals(40L, trade.buyer().treasuryCopper());
        assertEquals(2L, trade.buyer().inventory().get("minecraft:iron_ingot"));
        assertTrue(MFSettlementRules.requirements(MFGameCatalog.SettlementTier.VILLAGE).minimumTreasuryCopper() > 0L);
        assertEquals(MFGameCatalog.SettlementTier.CAPITAL, MFSettlementRules.next(MFGameCatalog.SettlementTier.CITY));
        assertNull(MFSettlementRules.next(MFGameCatalog.SettlementTier.CAPITAL));
    }

    @Test
    void productionOrderHasOneWayLifecycle() {
        assertEquals(MFGameCatalog.Profession.SMELTER, MFRecipeCatalog.recipe("smelt_iron").orElseThrow().profession());
        var order = MFWorkOrderState.start(UUID.randomUUID(), UUID.randomUUID(), "forge_iron_sword");
        assertTrue(order.active());
        var firstDay = order.advance();
        assertEquals(1, firstDay.elapsedDays());
        var ready = firstDay.advance();
        assertEquals(MFWorkOrderState.Status.READY_TO_OUTPUT, ready.status());
        assertEquals(MFWorkOrderState.Status.COMPLETED, ready.complete().status());
        assertThrows(IllegalStateException.class, () -> firstDay.cancel());
    }

    @Test
    void diplomacyWarIsSeparateFromOpinionScore() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        var relation = new MFDiplomacyRelation(a, b, 75, false, Set.of()).declareWar();
        assertTrue(relation.atWar());
        assertEquals("WAR", relation.band());
        relation = relation.makePeace(-10);
        assertFalse(relation.atWar());
        assertEquals("NEUTRAL", relation.band());
    }
    @Test
    void newbornIdentityAndAbandonedWorkOrdersRemainConsistent() {
        UUID newbornId = UUID.randomUUID();
        var newborn = MFCitizenState.createWithId(newbornId, UUID.randomUUID(), UUID.randomUUID(), "Newborn",
                MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 1, 0L);
        assertEquals(newbornId, newborn.id());
        assertEquals(MFGameCatalog.AgeStage.BABY, newborn.ageStage());

        var active = MFWorkOrderState.start(UUID.randomUUID(), UUID.randomUUID(), "forge_iron_sword").advance();
        var aborted = active.abort();
        assertEquals(MFWorkOrderState.Status.ABORTED, aborted.status());
        assertFalse(aborted.active());
    }

}
