package com.medievalfactions.tools;

import com.medievalfactions.domain.*;
import java.util.*;

import static com.medievalfactions.domain.MFGameCatalog.*;
import static com.medievalfactions.domain.MFAdvancedSystems.*;

/** Dependency-free contract tests for the advanced mechanics domain. */
public final class MFAdvancedSystemsSelfTest {
    private static int assertions;
    private MFAdvancedSystemsSelfTest() {}

    public static void main(String[] args) {
        npcPlanning(); caravanLogistics(); banking(); sieges(); abilities(); companions(); family(); dungeonsAndExpeditions(); construction();
        System.out.println("Advanced systems tests passed: " + assertions + " assertions.");
    }
    private static void npcPlanning() {
        UUID f=UUID.randomUUID(), s=UUID.randomUUID();
        var citizen=MFCitizenState.create(f,s,"Aldric",RaceFamily.HUMANS,Profession.MINER,5,30,MFRaceBiology.adultAgeDays(RaceFamily.HUMANS));
        check(planNpc(citizen,90,true,true,false,10).intent()==NpcIntent.SEEK_SHELTER,"citizen seeks shelter under major threat");
        var injured=new MFCitizenState(citizen.id(),f,s,citizen.name(),citizen.race(),citizen.profession(),citizen.ageStage(),citizen.status(),20,0,50,5,0,0,citizen.ageDays(),1,0,30);
        check(planNpc(injured,0,true,true,false,10).intent()==NpcIntent.SEEK_MEDIC,"injury outranks work");
        var hungry=new MFCitizenState(citizen.id(),f,s,citizen.name(),citizen.race(),citizen.profession(),citizen.ageStage(),citizen.status(),100,85,50,5,0,0,citizen.ageDays(),1,0,30);
        check(planNpc(hungry,0,true,true,false,10).intent()==NpcIntent.SEEK_FOOD,"hunger triggers food intent");
        check(planNpc(citizen,0,true,true,true,10).intent()==NpcIntent.CONTINUE_WORK,"active job continues");
        var child=MFCitizenState.create(f,s,"Nell",RaceFamily.HUMANS,Profession.FARMER,0,1,0);
        check(planNpc(child,0,true,true,false,10).intent()==NpcIntent.REST,
                "dependent children are not assigned adult work");
        check(planNpc(citizen,0,false,false,false,10).intent()==NpcIntent.SEEK_SHELTER,
                "housing shortage takes priority over production");
    }
    private static void caravanLogistics() {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        var seller=new MFSettlementState(UUID.randomUUID(),a,"Seller",SettlementTier.CAMP,1,0,5,0,10,10,10,0,Map.of("minecraft:iron_ingot",8L));
        var buyer=new MFSettlementState(UUID.randomUUID(),b,"Buyer",SettlementTier.CAMP,1,0,5,0,100,10,10,0,Map.of());
        var start=startCaravan(seller,buyer,"minecraft:iron_ingot",3,20,2,10,5).orElseThrow();
        check(start.seller().inventory().get("minecraft:iron_ingot")==5,"caravan reserves cargo at departure");
        check(start.buyer().treasuryCopper()==40,"buyer funds are escrowed at departure");
        var moving=start.caravan().advance();
        check(moving.daysRemaining()==1 && moving.status()==CaravanStatus.IN_TRANSIT,"caravan travel is delayed");
        moving=moving.advance();
        var arrival=arriveCaravan(moving,start.seller(),start.buyer()).orElseThrow();
        check(arrival.buyer().inventory().get("minecraft:iron_ingot")==3,"cargo delivered on arrival");
        check(arrival.seller().treasuryCopper()==70 && arrival.caravan().status()==CaravanStatus.ARRIVED,"seller receives escrow exactly once");
        check(arriveCaravan(arrival.caravan(),arrival.seller(),arrival.buyer()).isEmpty(),"duplicate delivery rejected");
        check(startCaravan(seller,buyer,"minecraft:diamond",1,1,1,0,1).isEmpty(),"missing cargo cannot be invented");
        var raidedStart=startCaravan(seller,buyer,"minecraft:iron_ingot",4,10,1,90,6).orElseThrow();
        var raid=resolveCaravanRaid(raidedStart.caravan(),raidedStart.seller(),raidedStart.buyer(),50).orElseThrow();
        check(raid.deliveredQuantity()==2 && raid.refundedCopper()==20,"partial ambush delivers remaining cargo and refunds unused escrow");
        check(raid.buyer().inventory().get("minecraft:iron_ingot")==2 && raid.seller().treasuryCopper()==30,
                "partial ambush settles stock and payment without creating money");
        int lossA=caravanAmbushLossPercent(raidedStart.caravan(),6);
        check(lossA>=25 && lossA<=100 && lossA==caravanAmbushLossPercent(raidedStart.caravan(),6),
                "ambush severity is deterministic and bounded");
    }
    private static void banking() {
        UUID branchId=UUID.randomUUID();
        var branch=new MFSettlementState(branchId,UUID.randomUUID(),"Bank",SettlementTier.TOWN,2,0,20,0,1000,60,60,0,Map.of());
        UUID owner=UUID.randomUUID();
        var account=new BankAccount(UUID.randomUUID(),owner,branchId,0,false);
        var deposit=deposit(account,branch,300).orElseThrow();
        check(deposit.account().balanceCopper()==300 && deposit.branch().treasuryCopper()==1300,"physical deposit increases account claim and branch reserve");
        var withdrawal=withdraw(deposit.account(),deposit.branch(),100).orElseThrow();
        check(withdrawal.account().balanceCopper()==200 && withdrawal.branch().treasuryCopper()==1200,"cash withdrawal reduces account claim and branch reserve");
        var account2=new BankAccount(UUID.randomUUID(),UUID.randomUUID(),branchId,50,false);
        var transfer=transfer(withdrawal.account(),account2,150).orElseThrow();
        check(transfer.source().balanceCopper()==50 && transfer.target().balanceCopper()==200,"same-branch account transfer conserves balances");
        check(transfer(withdrawal.account(),account2,500).isEmpty(),"overdraft rejected");
        var otherBranchAccount=new BankAccount(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),0,false);
        check(transfer(withdrawal.account(),otherBranchAccount,10).isEmpty(),"cross-branch transfer requires settlement and is rejected");
        var issue=issueLoan(transfer.target(),withdrawal.branch(),200,500).orElseThrow();
        check(issue.branch().treasuryCopper()==1000 && issue.account().balanceCopper()==200,"loan is disbursed from branch reserves as cash, not credited twice to the account");
        check(issue.loan().totalDueCopper()==210,"loan adds explicit fixed interest");
        var paid=issue.loan().applyRepayment(210);
        check(paid.status()==LoanStatus.PAID && paid.outstandingCopper()==0,"loan closes after repayment");
        Loan aging=issue.loan();
        for(int i=0;i<30;i++) aging=aging.advanceDay();
        check(aging.status()==LoanStatus.ACTIVE && aging.overdueDays()==0 && aging.ageDays()==30,
                "loan receives its full 30-day term before becoming overdue");
        for(int i=0;i<29;i++) aging=aging.advanceDay();
        check(aging.status()==LoanStatus.ACTIVE && aging.overdueDays()==29,
                "loan has a bounded grace period after maturity");
        aging=aging.advanceDay();
        check(aging.status()==LoanStatus.DEFAULTED && aging.overdueDays()==30,
                "loan defaults after the 30-day grace period");
        var debtor=new BankAccount(transfer.target().id(),owner,branchId,80,false);
        var recovery=settleLoanDefault(aging,debtor,branch.withTreasury(1_000));
        check(recovery.account().frozen() && recovery.account().balanceCopper()==0
                        && recovery.recoveredCopper()==80 && recovery.branch().treasuryCopper()==1_080,
                "loan default seizes only the remaining deposit and credits the lending reserve equally");
        check(deposit(account.freeze(true),branch,10).isEmpty(),"frozen accounts cannot accept deposits");
        check(withdraw(account,branch.withTreasury(0),1).isEmpty(),"branch cannot dispense more cash than its reserve");
        check(deposit(new BankAccount(UUID.randomUUID(),owner,branchId,Long.MAX_VALUE,false),branch,1).isEmpty(),
                "deposit overflow is rejected atomically");
        check(deposit(account,branch.withTreasury(Long.MAX_VALUE),1).isEmpty(),
                "branch reserve overflow is rejected atomically");
    }
    private static void sieges() {
        var siege=new Siege(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),120,60,100,100,30,70,0,SiegeStatus.ACTIVE);
        var next=siege.advance();
        check(next.days()==1 && next.wallIntegrity()<30,"siege advances walls and time");
        Siege current=next;
        for(int i=0;i<100 && current.status()==SiegeStatus.ACTIVE;i++) current=current.advance();
        check(current.status()!=SiegeStatus.ACTIVE,"siege reaches a terminal state");
        check(siege.withdraw().status()==SiegeStatus.WITHDRAWN,"army can withdraw");
    }
    private static void abilities() {
        var ability=starterAbilities().stream().filter(a->a.id().equals("mage_fire_bolt")).findFirst().orElseThrow();
        UUID casterId=UUID.randomUUID(), targetId=UUID.randomUUID();
        var caster=new Combatant(casterId,CombatClass.MAGE,Element.FIRE,30,30,50,50,20,20,Map.of());
        var target=new Combatant(targetId,CombatClass.WARRIOR,Element.METAL,40,40,0,0,0,20,Map.of());
        var cast=cast(caster,target,ability).orElseThrow();
        check(cast.caster().mana()==30 && cast.target().health()==26,"fire skill spends mana and deals damage");
        check(cast(cast.caster(),target,ability).isEmpty(),"cooldown prevents skill spam");
        check(cast(caster,target,starterAbilities().get(0)).isEmpty(),"wrong class cannot cast warrior skill");
        var healed=cast(new Combatant(casterId,CombatClass.CLERIC,Element.LIGHT,20,30,50,50,0,0,Map.of()),null,
                starterAbilities().stream().filter(a->a.id().equals("cleric_mend")).findFirst().orElseThrow()).orElseThrow();
        check(healed.caster().health()==30,"healing skill applies to caster");
    }
    private static void companions() {
        UUID owner=UUID.randomUUID();
        var horse=new Companion(UUID.randomUUID(),owner,CompanionSpecies.HORSE,24,1,0,30,30,40,40,20,true);
        check(horse.canRide(owner) && !horse.canRide(UUID.randomUUID()),"mount ownership gates riding");
        var trained=horse.train(250);
        check(trained.level()==2 && trained.bond()==25 && trained.bondLevel()==BondLevel.FAMILIAR,"mount gains level and bond from training");
        check(horse.ride(100).stamina()<horse.stamina(),"travel consumes stamina");
        check(horse.ride(100).hunger()==horse.hunger() && horse.rest(400).hunger()==horse.hunger(),
                "ordinary mounts do not use dragon-style hunger");
        check(horse.feed(50).hunger()==0,"feeding leaves ordinary mount hunger unchanged");
        var dragon=new Companion(UUID.randomUUID(),owner,CompanionSpecies.DRAGON,80,1,0,180,180,100,100,0,true);
        expectFailure(()->dragon.transfer(UUID.randomUUID()),"dragon ownership is permanent");
        check(dragon.ride(100).hunger()>dragon.hunger() && dragon.ride(100).stamina()<dragon.stamina(),
                "dragon flight/breath activity consumes provisions and stamina");
        check(dragon.rest(400).hunger()==dragon.hunger() && dragon.advanceDailyNeeds().hunger()==5,
                "dragon hunger advances by game days rather than each short server upkeep pulse");
        var damaged=dragon.withEntityHealth(75);
        check(damaged.health()==75 && damaged.alive(),"persistent companion ledger follows real entity health");
        var dead=damaged.withEntityHealth(0);
        check(!dead.alive() && dead.health()==0,"death of the real mount marks its persistent record dead");
    }
    private static void family() {
        UUID f=UUID.randomUUID();
        long adult=MFRaceBiology.adultAgeDays(RaceFamily.HUMANS);
        var mother=new Person(UUID.randomUUID(),f,RaceFamily.HUMANS,Affinity.SOLAR,Element.FIRE,Sex.FEMALE,adult,100,"Valen");
        var father=new Person(UUID.randomUUID(),f,RaceFamily.HUMANS,Affinity.LUNAR,Element.WATER,Sex.MALE,adult,100,"Thorne");
        var relation=createRelationship(mother,father,70,true,true,10).orElseThrow();
        check(createRelationship(mother,father,70,true,false,10).isEmpty(),"both partners must consent");
        var pregnancy=conceive(relation,mother,father,20,1,777).orElseThrow();
        check(conceive(relation,mother,father,20,99,778).isEmpty(),"conception is not guaranteed");
        check(giveBirth(pregnancy,mother,father,20+pregnancy.gestationDays()-1L).isEmpty(),"birth cannot occur early");
        var birth=giveBirth(pregnancy,mother,father,20+pregnancy.gestationDays()).orElseThrow();
        check(birth.child().ageDays()==0 && birth.child().race()==RaceFamily.HUMANS,"baby starts at newborn stage");
        UUID childSettlement=UUID.randomUUID();
        var childCitizen=MFCitizenState.createWithId(birth.child().id(),f,childSettlement,"Child",birth.child().race(),
                Profession.FARMER,0L,1,birth.child().ageDays());
        check(childCitizen.id().equals(birth.child().id()),"born resident and family person share one persistent identity");
        check(birth.child().affinity()==Affinity.SOLAR || birth.child().affinity()==Affinity.LUNAR,"mixed affinity is inherited by random rule");
        check(elementsFor(birth.child().affinity()).contains(birth.child().element()),"child element belongs to inherited affinity");
        var other=new Person(UUID.randomUUID(),f,RaceFamily.DWARVES,Affinity.SOLAR,Element.METAL,Sex.MALE,adult,100,"Stone");
        check(createRelationship(mother,other,80,true,true,10).isEmpty(),"unsupported race hybrid is rejected rather than improvised");
    }
    private static void dungeonsAndExpeditions() {
        UUID faction=UUID.randomUUID(), settlement=UUID.randomUUID();
        var eligible=MFCitizenState.create(faction,settlement,"Scout",RaceFamily.HUMANS,Profession.GUARD,0L,10,
                MFRaceBiology.adultAgeDays(RaceFamily.HUMANS));
        check(validExpeditionRoster(faction,settlement,List.of(eligible.id()),List.of(eligible)),
                "expedition roster accepts an active adult resident from the sending settlement");
        check(!validExpeditionRoster(faction,settlement,List.of(UUID.randomUUID()),List.of(eligible)),
                "expedition roster rejects unknown members");
        check(!validExpeditionRoster(faction,settlement,List.of(eligible.id(),eligible.id()),List.of(eligible)),
                "expedition roster rejects duplicate members");
        var child=MFCitizenState.create(faction,settlement,"Child",RaceFamily.HUMANS,Profession.FARMER,0L,1,0L);
        check(!validExpeditionRoster(faction,settlement,List.of(child.id()),List.of(child)),
                "children cannot be dispatched as expedition combatants");
        var one=generateDungeon(123,3); var two=generateDungeon(123,3);
        check(one.equals(two),"dungeon generation deterministic by seed");
        check(one.rooms().size()==11 && one.rooms().get(0).kind()==RoomKind.ENTRANCE
                && one.rooms().get(10).kind()==RoomKind.BOSS,"dungeon has entrance and boss progression");
        var physical=dungeonWorldPlacements(123,3,100,20,100);
        check(physical.size()>1_000 && physical.stream().anyMatch(p->p.blockId().equals("minecraft:chest")),
                "seeded dungeon produces a physical block layout with treasure rooms");
        check(physical.equals(dungeonWorldPlacements(123,3,100,20,100)),"physical dungeon layout is deterministic");
        var weak=startExpedition(UUID.randomUUID(),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID()),123,3,3,20,1);
        var moving=weak;
        for(int i=0;i<3;i++) moving=moving.advance();
        check(moving.status()==ExpeditionStatus.RETURNING,"expedition progresses through travel/exploration/return");
        long returnFood=moving.provisions();
        moving=moving.advance();
        check(moving.provisions()==returnFood,"returning expedition does not consume provisions again while awaiting settlement");
        check(resolveExpedition(moving,3).expedition().status()==ExpeditionStatus.FAILED,"underpowered expedition can fail");
        var strong=startExpedition(UUID.randomUUID(),UUID.randomUUID(),List.of(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID()),123,3,1,100,1000);
        var returned=strong.advance();
        var result=resolveExpedition(returned,3);
        check(result.expedition().status()==ExpeditionStatus.COMPLETED && result.expedition().rewardAmount()>0,"successful expedition earns generated loot");
    }
    private static void construction() {
        var blueprints=starterBlueprints();
        var tower=blueprints.get("watchtower");
        check(tower.placements().size()>90 && tower.materialCosts().get("minecraft:stone_bricks")>0,"watchtower has real block plan and finite cost");
        var normal=worldPlacements(tower,10,64,10,0);
        var rotated=worldPlacements(tower,10,64,10,1);
        check(normal.size()==tower.placements().size() && !normal.equals(rotated),"rotation transforms physical block positions");
        var house=blueprints.get("small_house");
        check(house.materialCosts().get("minecraft:oak_planks")==81,"house material cost is explicitly declared");
        var project=new ConstructionProject(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),"minecraft:overworld",0,64,0,0,
                "watchtower",0,ConstructionStatus.FUNDED,5);
        var half=project.advance(10,tower.placements().size());
        check(half.nextPlacement()==10 && half.status()==ConstructionStatus.BUILDING,"construction advances in bounded batches");
        var complete=half.advance(tower.placements().size(),tower.placements().size());
        check(complete.status()==ConstructionStatus.COMPLETED,"construction project has completion state");
    }
    private static void check(boolean condition,String message){assertions++; if(!condition)throw new AssertionError(message);}
    private static void expectFailure(Runnable r,String message){assertions++;try{r.run();}catch(RuntimeException expected){return;}throw new AssertionError(message);}
}
