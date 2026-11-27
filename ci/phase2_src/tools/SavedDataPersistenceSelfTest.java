import com.medievalfactions.data.*;
import com.medievalfactions.domain.*;
import net.minecraft.nbt.*;
import net.minecraft.core.HolderLookup;
import java.util.*;
public final class SavedDataPersistenceSelfTest {
 public static void main(String[] args) {
  HolderLookup.Provider provider = new HolderLookup.Provider(){};
  var factions = new MFFactionSavedData();
  UUID player = UUID.randomUUID();
  var faction = factions.createFaction("Alba", player, MFGameCatalog.RaceFamily.HUMANS).orElseThrow();
  UUID player2 = UUID.randomUUID();
  if (!factions.joinFaction(faction.id(), player2)) throw new AssertionError("join");
  if (factions.joinFaction(faction.id(), UUID.randomUUID())) throw new AssertionError("third player accepted");
  var fTag = factions.save(new CompoundTag(), provider);
  if (fTag.getInt("schema_version") != 2) throw new AssertionError("faction schema version not written");
  MFFactionSavedData.load(new CompoundTag(), provider); // unversioned legacy save remains readable
  expectSchemaRejection("faction future schema", () -> MFFactionSavedData.load(schemaTag(3), provider));
  expectSchemaRejection("faction wrong schema type", () -> MFFactionSavedData.load(stringSchemaTag("2"), provider));
  var factions2 = MFFactionSavedData.load(fTag, provider);
  if (factions2.getFaction(faction.id()).orElseThrow().playerMembers().size()!=2) throw new AssertionError("faction persistence");

  var world = new MFWorldSavedData();
  var profile = world.createProfile(player, MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.CombatClass.WARRIOR, MFGameCatalog.Profession.MINER, 99L).orElseThrow();
  if (world.awardCombatExperience(player, "minecraft:zombie", 5, 0L) != 100) throw new AssertionError("first combat award");
  if (world.awardCombatExperience(player, "minecraft:zombie", 5, 0L) != 50) throw new AssertionError("second combat award");
  if (world.awardCombatExperience(player, "minecraft:zombie", 5, 0L) != 25) throw new AssertionError("third combat award");
  if (world.awardCombatExperience(player, "minecraft:zombie", 5, 0L) != 0) throw new AssertionError("repeat kill farming blocked");
  var settlement = world.createSettlement(faction.id(), "Alba Camp").orElseThrow();
  world.addSettlementFood(settlement.id(), 15);
  world.adjustSettlementTreasury(settlement.id(), 100);
  if (!world.setSettlementDailyMaintenance(settlement.id(), 2L)) throw new AssertionError("maintenance configuration");
  world.addSettlementStock(settlement.id(), "minecraft:iron_ingot", 3);
  var foreignCitizen = MFCitizenState.create(faction.id(), settlement.id(), "Foreign", MFGameCatalog.RaceFamily.DWARVES,
    MFGameCatalog.Profession.MINER, 5, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.DWARVES));
  if (world.addCitizen(foreignCitizen, faction.primaryRace())) throw new AssertionError("foreign permanent resident accepted");
  var miner = MFCitizenState.create(faction.id(), settlement.id(), "Aldric", MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.MINER, 5, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
  if (!world.addCitizen(miner, faction.primaryRace())) throw new AssertionError("primary-race resident rejected");
  if (!world.citizen(miner.id()).orElseThrow().equals(miner)) throw new AssertionError("indexed citizen lookup returned a different resident");
  if (world.citizen(UUID.randomUUID()).isPresent() || world.citizen(null).isPresent()) throw new AssertionError("unknown/null citizen lookup must be empty");
  var carpenter = MFCitizenState.create(faction.id(), settlement.id(), "Mira", MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.CARPENTER, 0, 20, MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
  if (!world.addCitizen(carpenter, faction.primaryRace())) throw new AssertionError("primary-race carpenter rejected");
  world.addSettlementStock(settlement.id(), "minecraft:oak_log", 2);
  var order = world.startWorkOrder(settlement.id(), carpenter.id(), "cut_planks").orElseThrow();
  if (world.startWorkOrder(settlement.id(), carpenter.id(), "cut_planks").isPresent()) throw new AssertionError("worker accepted concurrent job");
  if (world.settlement(settlement.id()).orElseThrow().inventory().get("minecraft:oak_log") != 1L) throw new AssertionError("inputs not reserved");
  if (!world.cancelUnstartedWorkOrder(order.id())) throw new AssertionError("cancel unstarted job");
  if (world.settlement(settlement.id()).orElseThrow().inventory().get("minecraft:oak_log") != 2L) throw new AssertionError("cancel did not refund");

  var activeOrder = world.startWorkOrder(settlement.id(), carpenter.id(), "cut_planks").orElseThrow();
  world.advanceOneDay();
  if (world.workOrders(settlement.id()).stream().filter(o -> o.id().equals(activeOrder.id())).findFirst().orElseThrow().status()!=MFWorkOrderState.Status.COMPLETED) throw new AssertionError("production did not complete");
  var stock = world.settlement(settlement.id()).orElseThrow().inventory();
  if (stock.get("minecraft:oak_planks") != 4L) throw new AssertionError("production output missing");
  if (world.citizens(settlement.id()).stream().filter(c->c.id().equals(carpenter.id())).findFirst().orElseThrow().professionXp() != 25L) throw new AssertionError("profession XP missing");
  // Fund a real settlement expansion before allowing its first tier promotion.
  var frontier = world.createSettlement(faction.id(), "Frontier Hall").orElseThrow();
  if (!world.adjustSettlementTreasury(frontier.id(), 1_000L)) throw new AssertionError("frontier seed treasury");
  if (!world.addSettlementFood(frontier.id(), 70L)) throw new AssertionError("frontier food supplies");
  if (!world.applySettlementProject(frontier.id(), Map.of(), 500L, 5, 0, 20, 10))
   throw new AssertionError("funded housing/security/infrastructure project");
  for (int i = 0; i < 10; i++) {
   var resident = MFCitizenState.create(faction.id(), frontier.id(), "Frontier_" + i,
     MFGameCatalog.RaceFamily.HUMANS, MFGameCatalog.Profession.FARMER, 0L, 10,
     MFRaceBiology.adultAgeDays(MFGameCatalog.RaceFamily.HUMANS));
   if (!world.addCitizen(resident, faction.primaryRace())) throw new AssertionError("frontier resident " + i);
  }
  if (!world.upgradeSettlement(frontier.id())
      || world.settlement(frontier.id()).orElseThrow().tier() != MFGameCatalog.SettlementTier.VILLAGE)
   throw new AssertionError("settlement tier promotion gates");

  world.adjustRelation(faction.id(), UUID.randomUUID(), 35);
  var wTag = world.save(new CompoundTag(), provider);
  if (wTag.getInt("schema_version") != 2) throw new AssertionError("world schema version not written");
  MFWorldSavedData.load(new CompoundTag(), provider); // unversioned legacy save remains readable
  expectSchemaRejection("world future schema", () -> MFWorldSavedData.load(schemaTag(3), provider));
  expectSchemaRejection("world wrong schema type", () -> MFWorldSavedData.load(stringSchemaTag("2"), provider));
  var world2 = MFWorldSavedData.load(wTag, provider);
  if (world2.profile(player).orElseThrow().element()!=profile.element()) throw new AssertionError("profile persistence");
  var s2 = world2.settlement(settlement.id()).orElseThrow();
  if (s2.foodRations()!=13 || s2.treasuryCopper()!=93 || s2.dailyMaintenanceCopper()!=2L
      || s2.inventory().get("minecraft:iron_ingot")!=3L || s2.inventory().get("minecraft:oak_planks")!=4L) throw new AssertionError("settlement persistence");
  if (world2.citizens(settlement.id()).size()!=2) throw new AssertionError("citizen persistence");
  if (world2.workOrders(settlement.id()).stream().noneMatch(o->o.id().equals(activeOrder.id()) && o.status()==MFWorkOrderState.Status.COMPLETED)) throw new AssertionError("work order persistence");
  if (world2.simulatedDays()!=1) throw new AssertionError("day count persistence");
  var frontierAfterLoad = world2.settlements().stream().filter(s -> s.name().equals("Frontier Hall")).findFirst().orElseThrow();
  if (frontierAfterLoad.tier() != MFGameCatalog.SettlementTier.VILLAGE
      || frontierAfterLoad.treasuryCopper() != 500L || frontierAfterLoad.infrastructure() != 10
      || frontierAfterLoad.security() != 20 || frontierAfterLoad.population() != 10)
   throw new AssertionError("settlement project and promotion persistence");
  if (world2.awardCombatExperience(player, "minecraft:zombie", 5, 0L) != 0) throw new AssertionError("combat ledger persistence");
  if (world2.awardCombatExperience(player, "minecraft:zombie", 5, 1L) != 100) throw new AssertionError("new-day reward reset");
  for (int i = 0; i < 4; i++)
   if (world2.awardCombatExperience(player, "test:creature_" + i, 50, 2L) != 250) throw new AssertionError("daily combat cap setup " + i);
  if (world2.awardCombatExperience(player, "test:creature_final", 50, 2L) != 0) throw new AssertionError("daily combat XP cap");
  System.out.println("SavedData NBT round-trip + production + combat-ledger integration smoke test: PASS");
 }
 private static CompoundTag schemaTag(int version) {
  CompoundTag tag = new CompoundTag(); tag.putInt("schema_version", version); return tag;
 }
 private static CompoundTag stringSchemaTag(String version) {
  CompoundTag tag = new CompoundTag(); tag.putString("schema_version", version); return tag;
 }
 private static void expectSchemaRejection(String label, Runnable load) {
  try { load.run(); } catch (IllegalStateException expected) { return; }
  throw new AssertionError(label + " was silently accepted");
 }
}
