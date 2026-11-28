package com.medievalfactions.tools;

import com.medievalfactions.domain.*;
import java.util.*;
import static com.medievalfactions.domain.MFGameCatalog.*;
import static com.medievalfactions.domain.MFAdvancedSystems.*;

/** Dependency-free tests for the new integrated simulation rules. */
public final class MFSimulationSystemsSelfTest {
    private static int checks;
    private MFSimulationSystemsSelfTest() {}
    public static void main(String[] args) {
        citizenSchedulesAndNavigation(); cityPlanning(); tacticalCombatAndCampaigns(); dragonLifecycle(); familyAndMedicine(); physicalCaravansAndExpeditions(); dungeonBosses();
        populationIndexStress();
        serverRuntimeScheduling();
        System.out.println("Simulation expansion tests passed: "+checks+" assertions.");
    }
    private static void citizenSchedulesAndNavigation() {
        UUID f=UUID.randomUUID(),s=UUID.randomUUID();
        var citizen=MFCitizenState.create(f,s,"Mira",RaceFamily.HUMANS,Profession.FARMER,4,20,MFRaceBiology.adultAgeDays(RaceFamily.HUMANS));
        var home=new MFCitizenSimulation.Waypoint("minecraft:overworld",0,64,0);
        var field=new MFCitizenSimulation.Waypoint("minecraft:overworld",8,64,0);
        var schedule=new MFCitizenSimulation.CitizenSchedule(citizen.id(),List.of(
                new MFCitizenSimulation.ScheduleEntry(360,MFCitizenSimulation.Activity.WORK,field),
                new MFCitizenSimulation.ScheduleEntry(1200,MFCitizenSimulation.Activity.SLEEP,home)));
        check(schedule.at(500).activity()==MFCitizenSimulation.Activity.WORK,"schedule selects the current work activity");
        check(schedule.at(100).activity()==MFCitizenSimulation.Activity.SLEEP,"schedule wraps correctly before first entry");
        check(MFCitizenSimulation.plan(citizen,schedule,500,0,true,true,home,home,home).orElseThrow().directive()==MFCitizenSimulation.Directive.WORK,
                "NPC follows timetable when survival needs are stable");
        var hungry=new MFCitizenState(citizen.id(),f,s,citizen.name(),citizen.race(),citizen.profession(),citizen.ageStage(),citizen.status(),
                citizen.health(),85,citizen.satisfaction(),citizen.dailyWageCopper(),citizen.purseCopper(),citizen.unpaidDays(),citizen.ageDays(),
                citizen.professionLevel(),citizen.professionXp(),citizen.professionPotential());
        check(MFCitizenSimulation.plan(hungry,schedule,500,0,true,true,home,home,home).orElseThrow().directive()==MFCitizenSimulation.Directive.SEEK_FOOD,
                "survival overrides work schedule");
        var start=new MFCitizenSimulation.NavCell(0,64,0); var goal=new MFCitizenSimulation.NavCell(4,64,0);
        Set<MFCitizenSimulation.NavCell> blocked=Set.of(new MFCitizenSimulation.NavCell(1,64,0),new MFCitizenSimulation.NavCell(2,64,0),new MFCitizenSimulation.NavCell(3,64,0));
        var path=MFCitizenSimulation.findPath(start,goal,blocked,new MFCitizenSimulation.NavBounds(-2,6,63,65,-3,3),500).orElseThrow();
        check(path.cells().get(0).equals(start)&&path.cells().get(path.cells().size()-1).equals(goal),"A* path includes exact start and goal");
        check(path.cells().stream().noneMatch(blocked::contains),"A* path avoids obstacles");
        check(MFCitizenSimulation.findPath(start,goal,blocked,new MFCitizenSimulation.NavBounds(0,4,64,64,0,0),100).isEmpty(),
                "A* safely reports unreachable paths");
    }
    private static void cityPlanning() {
        var plan=new MFCityPlanner.CityPlan(UUID.randomUUID(),UUID.randomUUID(),"minecraft:overworld",
                new MFCityPlanner.Bounds(0,30,0,30),List.of(),0);
        var spec=new MFCityPlanner.BuildingSpec("house_large",MFCityPlanner.District.RESIDENTIAL,6,5,100,Map.of("minecraft:oak_planks",20L,"minecraft:stone",10L));
        var placed=MFCityPlanner.place(plan,spec,"house-1",2,70,2,0,Map.of("minecraft:oak_planks",20L,"minecraft:stone",10L)).orElseThrow();
        check(placed.remainingInventory().isEmpty(),"city planner reserves all construction materials");
        check(MFCityPlanner.place(placed.plan(),spec,"house-overlap",5,70,4,0,Map.of("minecraft:oak_planks",20L,"minecraft:stone",10L)).isEmpty(),
                "city planner rejects overlapping footprints");
        check(MFCityPlanner.place(plan,spec,"outside",28,70,28,0,Map.of("minecraft:oak_planks",20L,"minecraft:stone",10L)).isEmpty(),
                "city planner rejects buildings outside town bounds");
        var started=MFCityPlanner.start(placed.plan(),"house-1").orElseThrow();
        check(started.buildings().get(0).stage()==MFCityPlanner.BuildStage.FOUNDATION,"city plot enters foundation only when physical construction is queued");
        var built=MFCityPlanner.advance(started,"house-1",100).orElseThrow();
        check(built.buildings().get(0).stage()==MFCityPlanner.BuildStage.ACTIVE,"construction progresses into an active building");
    }
    private static void tacticalCombatAndCampaigns() {
        UUID a=UUID.randomUUID(),d=UUID.randomUUID();
        var attacker=new MFTacticalWarfare.Unit(UUID.randomUUID(),a,0,0,40,40,4,12,1,50,65,2,MFTacticalWarfare.Stance.READY,false);
        var defender=new MFTacticalWarfare.Unit(UUID.randomUUID(),d,1,0,35,35,6,8,1,45,60,2,MFTacticalWarfare.Stance.READY,false);
        var battle=new MFTacticalWarfare.Battle(UUID.randomUUID(),a,d,a,1,List.of(attacker,defender),MFTacticalWarfare.BattleStatus.ACTIVE,null);
        battle=MFTacticalWarfare.apply(battle,new MFTacticalWarfare.Order(attacker.id(),MFTacticalWarfare.Action.BLOCK,null,0,0)).orElseThrow().battle();
        check(battle.units().get(0).stance()==MFTacticalWarfare.Stance.BLOCKING,"battle supports defensive stance");
        battle=MFTacticalWarfare.endTurn(battle).orElseThrow();
        check(battle.activeFaction().equals(d),"battle turn changes faction");
        var blockedAttack=MFTacticalWarfare.apply(battle,new MFTacticalWarfare.Order(defender.id(),MFTacticalWarfare.Action.ATTACK,attacker.id(),0,0)).orElseThrow();
        check(blockedAttack.damage()>=1 && blockedAttack.battle().units().get(0).health()<40,"enemy attack resolves against a target");
        check(MFTacticalWarfare.apply(blockedAttack.battle(),new MFTacticalWarfare.Order(attacker.id(),MFTacticalWarfare.Action.ATTACK,defender.id(),0,0)).isEmpty(),
                "opposing faction cannot act during enemy turn");
        var route=List.of(new MFCitizenSimulation.Waypoint("minecraft:overworld",0,64,0),new MFCitizenSimulation.Waypoint("minecraft:overworld",5,64,0));
        var army=new MFTacticalWarfare.ArmyColumn(UUID.randomUUID(),a,UUID.randomUUID(),List.of(UUID.randomUUID()),
                MFTacticalWarfare.Formation.SHIELD_WALL,100,20,60,route,0,1);
        var marched=army.advanceDay(2);
        check(marched.nextWaypoint()==1 && marched.provisions()==15,"army column marches and consumes real provisions");
        var outOfFood=new MFTacticalWarfare.ArmyColumn(army.id(),a,army.commanderId(),army.unitIds(),army.formation(),100,0,60,route,0,1).advanceDay(2);
        check(outOfFood.morale()==52 && outOfFood.troopCount()==99,"army starvation reduces morale and causes attrition");
        var occupation=new MFTacticalWarfare.Occupation(UUID.randomUUID(),d,a,10,9,90,false);
        check(occupation.advance(9,true,false).control()==90,"occupation cannot advance before its start day");
        occupation=occupation.advance(10,true,false);
        check(occupation.captured() && occupation.advance(10,false,true)==occupation,"occupation is day-gated and capture is terminal");
    }
    private static void dragonLifecycle() {
        UUID owner=UUID.randomUUID();
        var a=new Companion(UUID.randomUUID(),owner,CompanionSpecies.DRAGON,80,12,0,100,100,80,100,20,true);
        var b=new Companion(UUID.randomUUID(),owner,CompanionSpecies.DRAGON,70,14,0,110,110,80,100,20,true);
        var ga=new MFDragonLifecycle.DragonGenome(Element.FIRE,80,60,90,55,65);
        var gb=new MFDragonLifecycle.DragonGenome(Element.AIR,70,90,65,75,40);
        var egg=MFDragonLifecycle.breed(owner,a.id(),b.id(),a,b,ga,gb,991,5).orElseThrow();
        check(MFDragonLifecycle.breed(owner,a.id(),b.id(),a,b,ga,gb,991,5).orElseThrow().genome().equals(egg.genome()),
                "dragon inheritance is deterministic for a fixed seed");
        egg=egg.withEnvironment(90,100);
        for(int day=1;day<=30;day++) egg=egg.advanceDay(day);
        check(egg.status()==MFDragonLifecycle.EggStatus.READY_TO_HATCH,"well cared eggs complete incubation");
        var hatchling=MFDragonLifecycle.createHatchling(egg,UUID.randomUUID()).orElseThrow();
        check(hatchling.stage()==MFDragonLifecycle.LifeStage.HATCHLING && hatchling.maxHealth()>=40,"egg produces a genetic hatchling");
        check(MFDragonLifecycle.chooseIntent(MFDragonLifecycle.AutonomyOrder.PATROL,90,100,false,false,true,true)
                ==MFDragonLifecycle.DragonIntent.SEEK_FOOD,"dragon survival overrides its patrol order");
    }
    private static void familyAndMedicine() {
        UUID f=UUID.randomUUID();
        var p1=new Person(UUID.randomUUID(),f,RaceFamily.HUMANS,Affinity.SOLAR,Element.FIRE,Sex.FEMALE,10000,90,"Ashford");
        var p2=new Person(UUID.randomUUID(),f,RaceFamily.HUMANS,Affinity.SOLAR,Element.LIGHT,Sex.MALE,11000,90,"Grey");
        var relationship=new Relationship(UUID.randomUUID(),p1.id(),p2.id(),85,RelationshipStatus.COURTING,true,true,1);
        var proposed=MFFamilyCare.propose(p1,p2,relationship,p1.id(),20).orElseThrow();
        var married=proposed.answer(p2.id(),true,21);
        check(married.status()==MFFamilyCare.MarriageStatus.MARRIED,"marriage requires and records second explicit consent");
        check(MFFamilyCare.propose(p1,p2,relationship,p1.id(),20).orElseThrow().answer(p2.id(),false,21).status()==MFFamilyCare.MarriageStatus.DECLINED,
                "marriage refusal is final and recorded");
        var medical=new MFFamilyCare.MedicalCase(UUID.randomUUID(),p1.id(),MFFamilyCare.Condition.INFECTION,80,90,
                MFFamilyCare.MedicalStatus.OPEN,10,-1);
        var clinic=new MFFamilyCare.Clinic(3,75,2,4);
        var treated=MFFamilyCare.treat(medical,clinic,11).orElseThrow();
        check(treated.clinic().medicineStock()==1 && treated.medicalCase().severity()<80,"medical care consumes medicine and reduces severity");
        check(MFFamilyCare.treat(treated.medicalCase(),treated.clinic(),11).isEmpty(),"clinic cannot double-treat on the same day");
        var patient = MFCitizenState.create(f,UUID.randomUUID(),"Patient",RaceFamily.HUMANS,Profession.DOCTOR,0,2,
                MFRaceBiology.adultAgeDays(RaceFamily.HUMANS));
        var recovered = patient.withHealthAndHunger(Math.min(100,patient.health()+treated.healthRestored()),patient.hunger());
        check(recovered.health() >= patient.health(),"medical recovery can be applied to persistent citizen health");
        var naturalRecovery = medical.advanceDay(true, true);
        check(naturalRecovery.severity() < medical.severity() && naturalRecovery.infection() < medical.infection(),
                "safe and fed patients recover gradually without fabricated medicine");
        var untreatedDecline = medical.advanceDay(false, false);
        check(untreatedDecline.severity() > medical.severity() && untreatedDecline.infection() > medical.infection(),
                "unsafe and unfed patients worsen instead of being magically healed");
        var fatalCase = new MFFamilyCare.MedicalCase(UUID.randomUUID(), p1.id(), MFFamilyCare.Condition.INFECTION,
                97, 97, MFFamilyCare.MedicalStatus.OPEN, 1, -1).advanceDay(false, false);
        check(fatalCase.status() == MFFamilyCare.MedicalStatus.DECEASED,
                "critical untreated infection can become fatal in an unsafe, unfed settlement");
    }
    private static void physicalCaravansAndExpeditions() {
        UUID seller=UUID.randomUUID(),buyer=UUID.randomUUID();
        var route=List.of(new MFAdventureSystems.RoutePoint("minecraft:overworld",0,64,0),
                new MFAdventureSystems.RoutePoint("minecraft:overworld",3,64,0),
                new MFAdventureSystems.RoutePoint("minecraft:overworld",6,64,0));
        var convoy=new MFAdventureSystems.PhysicalConvoy(UUID.randomUUID(),seller,buyer,"minecraft:wheat",20,20,100,route,0,30,70,
                MFAdventureSystems.ConvoyStatus.TRAVELLING,19);
        check(convoy.effectiveRisk()==55,"escort strength lowers route danger");
        check(convoy.advance().nextPoint()==1,"physical caravan advances waypoint-by-waypoint");
        var attacked=convoy.ambush(25);
        check(attacked.remainingQuantity()==15 && attacked.status()==MFAdventureSystems.ConvoyStatus.AMBUSHED,
                "caravan ambush removes cargo without creating it");
        check(attacked.resumeAfterAmbush().advance().nextPoint()==1,"surviving caravan can resume its route");
        var delivered = convoy.advance().advance();
        check(delivered.status() == MFAdventureSystems.ConvoyStatus.DELIVERED
                        && delivered.markSettled().status() == MFAdventureSystems.ConvoyStatus.SETTLED,
                "delivered physical convoy becomes a single-settlement state after its ledgers are paid");
        var party=new MFAdventureSystems.ExpeditionJourney(UUID.randomUUID(),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID()),route,0,20,
                MFAdventureSystems.JourneyStatus.OUTBOUND,0,0,-1);
        check(party.nextDestination().equals(route.get(1)), "expedition targets the next physical waypoint");
        party=party.advance(1,false,true);
        check(party.nextPoint()==0 && party.status()==MFAdventureSystems.JourneyStatus.OUTBOUND,
                "daily simulation cannot move a physical party before its entities arrive");
        party=party.arriveAtNextPoint();
        check(party.nextPoint()==1 && party.status()==MFAdventureSystems.JourneyStatus.OUTBOUND
                        && party.nextDestination().equals(route.get(2)), "entire party arrival advances to the next waypoint");
        party=party.arriveAtNextPoint();
        check(party.status()==MFAdventureSystems.JourneyStatus.EXPLORING && party.nextPoint()==2,
                "the party enters exploration only after reaching its destination");
        party=party.advance(2,true,true);
        check(party.status()==MFAdventureSystems.JourneyStatus.RETURNING && party.injuries()==1 && party.discoveries()==2,
                "daily expedition simulation consumes supplies and resolves encounters without teleporting members");
        check(party.nextDestination().equals(route.get(1)), "return journey targets the previous real waypoint");
        party=party.arriveAtNextPoint().arriveAtNextPoint();
        check(party.status()==MFAdventureSystems.JourneyStatus.COMPLETED && party.nextPoint()==0,
                "expedition completes only after the party physically reaches its origin");
        check(party.advance(3, true, true) == party, "completed expedition cannot advance or consume provisions again");
        var activeParty = new MFAdventureSystems.ExpeditionJourney(UUID.randomUUID(), UUID.randomUUID(),
                List.of(party.memberIds().get(0)), route, 0, 10, MFAdventureSystems.JourneyStatus.OUTBOUND, 0, 0, -1);
        check(!MFAdventureSystems.membersAvailableForJourney(List.of(activeParty), List.of(party.memberIds().get(0))),
                "a resident cannot be committed to two active physical expeditions");
        check(MFAdventureSystems.membersAvailableForJourney(List.of(activeParty), List.of(UUID.randomUUID())),
                "unassigned residents remain eligible for another expedition");
        check(!MFAdventureSystems.membersAvailableForJourney(List.of(activeParty),
                        List.of(party.memberIds().get(0), party.memberIds().get(0))),
                "duplicate member IDs are rejected before dispatch");
        var failedParty = new MFAdventureSystems.ExpeditionJourney(activeParty.id(), activeParty.expeditionId(),
                activeParty.memberIds(), route, 0, 0, MFAdventureSystems.JourneyStatus.FAILED, 0, 0, 1);
        check(MFAdventureSystems.membersAvailableForJourney(List.of(failedParty), activeParty.memberIds()),
                "residents are released after a failed journey");
        var completedParty = new MFAdventureSystems.ExpeditionJourney(activeParty.id(), activeParty.expeditionId(),
                activeParty.memberIds(), route, 0, 0, MFAdventureSystems.JourneyStatus.COMPLETED, 0, 0, 1);
        check(MFAdventureSystems.membersAvailableForJourney(List.of(completedParty), activeParty.memberIds()),
                "residents are released after a completed journey");
    }
    private static void populationIndexStress() {
        UUID faction = UUID.randomUUID();
        List<MFCitizenState> population = new ArrayList<>();
        List<MFWorkOrderState> orders = new ArrayList<>();
        List<MFAdventureSystems.ExpeditionJourney> journeys = new ArrayList<>();
        UUID[] settlementIds = new UUID[20];
        for (int i=0;i<settlementIds.length;i++) settlementIds[i]=UUID.randomUUID();
        for (int i=0;i<3_000;i++) {
            MFCitizenState citizen = MFCitizenState.create(faction, settlementIds[i % settlementIds.length],
                    "Resident-"+i, RaceFamily.HUMANS, Profession.FARMER, i, 20,
                    MFRaceBiology.adultAgeDays(RaceFamily.HUMANS));
            population.add(citizen);
            if (i < 400) orders.add(MFWorkOrderState.start(citizen.settlementId(), citizen.id(), "forge_iron_sword").advance());
        }
        for (int i=0;i<80;i++) {
            List<UUID> members = new ArrayList<>();
            for (int n=0;n<5;n++) members.add(population.get(i*5+n).id());
            journeys.add(new MFAdventureSystems.ExpeditionJourney(UUID.randomUUID(), UUID.randomUUID(), members,
                    List.of(new MFAdventureSystems.RoutePoint("minecraft:overworld",0,64,0),
                            new MFAdventureSystems.RoutePoint("minecraft:overworld",20,64,20)),
                    0, 10L, MFAdventureSystems.JourneyStatus.OUTBOUND, 0, 0, -1));
        }
        MFPopulationIndex index = MFPopulationIndex.build(population, orders, journeys);
        check(index.citizensById().size()==3_000,"population index stores every citizen exactly once");
        check(index.activeCountAt(settlementIds[0])==150L,"population index counts residents once per settlement");
        check(index.isWorking(population.get(0).id()),"population index marks active workers");
        check(index.isTravelling(population.get(10).id()),"population index marks members of active expeditions");
        check(!index.isTravelling(population.get(500).id()),"population index excludes non-travelling citizens");
        check(index.isActive(population.get(100).id()),"population index resolves active citizen state");
        check(index.citizen(UUID.randomUUID()).isEmpty() && index.citizen(null).isEmpty(),
                "population index safely handles missing and null identities");
    }

    private static void serverRuntimeScheduling() {
        MFServerRuntimeScheduler first = new MFServerRuntimeScheduler();
        MFServerRuntimeScheduler second = new MFServerRuntimeScheduler();
        check(first.observeMinute(0), "a new server evaluates its initial NPC schedule immediately");
        check(!first.observeMinute(0) && first.observeMinute(1), "NPC schedule refreshes once per changed game minute");
        check(first.observeMinute(1439) && first.observeMinute(0), "NPC schedule updates across the midnight boundary");
        first.advanceTick();
        check(!first.dueEvery(5), "five-tick work does not run before its interval");
        for (int i = 0; i < 4; i++) first.advanceTick();
        check(first.dueEvery(5) && !first.dueEvery(20), "server tick intervals are exact");
        check(second.tickCount() == 0L && second.observeMinute(0), "separate worlds never share runtime tick or minute state");
        boolean invalid = false;
        try { first.observeMinute(1440); } catch (IllegalArgumentException expected) { invalid = true; }
        check(invalid, "invalid minute values are rejected");
    }
    private static void dungeonBosses() {
        var boss=MFAdventureSystems.createBoss(UUID.randomUUID(),"crypt_king",5,42);
        var turn=MFAdventureSystems.advanceBossTurn(boss,4);
        check(turn.encounter().turn()==1 && turn.damageToParty()>0 && !turn.telegraph().isBlank(),"boss executes a telegraphed mechanic");
        check(turn.bossHealing()>=0 && turn.summonedMinions()>=0,"boss action reports healing and summoned reinforcements");
        boss=MFAdventureSystems.damageBoss(boss,220,0).orElseThrow();
        check(boss.phase()==MFAdventureSystems.EncounterPhase.PHASE_TWO,"boss changes phase at half health");
        boss=MFAdventureSystems.damageBoss(boss,10000,0).orElseThrow();
        check(boss.phase()==MFAdventureSystems.EncounterPhase.DEFEATED,"boss reaches a terminal defeated state");
    }
    private static void check(boolean condition,String message) { checks++; if(!condition) throw new AssertionError(message); }
}
