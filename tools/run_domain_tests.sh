#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${TMPDIR:-/tmp}/mf-domain-core-classes"
rm -rf "$OUT"
mkdir -p "$OUT"
javac -encoding UTF-8 -d "$OUT" \
  "$ROOT/src/main/java/com/medievalfactions/economy/MFCurrency.java" \
  "$ROOT/src/main/java/com/medievalfactions/network/MFPlayerSkinRules.java" \
  "$ROOT/src/main/java/com/medievalfactions/production/MFProductionEngine.java" \
  "$ROOT/src/main/java/com/medievalfactions/simulation/MFOfflineSimulation.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFGameCatalog.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFCharacterProfile.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFSkillTreeCatalog.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFRecipeCatalog.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFWorkOrderState.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFCombatRewardRules.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFRaceBiology.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFCalendar.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFDiplomacyRelation.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFSettlementRules.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFSettlementState.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFSettlementSimulation.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFCitizenState.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFPopulationIndex.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFTradeEngine.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFAdvancedSystems.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFCitizenSimulation.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFCityPlanner.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFTacticalWarfare.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFDragonLifecycle.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFFamilyCare.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFAdventureSystems.java" \
  "$ROOT/src/main/java/com/medievalfactions/domain/MFServerRuntimeScheduler.java" \
  "$ROOT/tools/DomainCoreSelfTest.java" \
  "$ROOT/tools/MFAdvancedSystemsSelfTest.java" \
  "$ROOT/tools/MFSimulationSystemsSelfTest.java" \
  "$ROOT/tools/MFPlayerSkinRulesSelfTest.java"
java -ea -cp "$OUT" com.medievalfactions.tools.DomainCoreSelfTest
java -ea -cp "$OUT" com.medievalfactions.tools.MFAdvancedSystemsSelfTest
java -ea -cp "$OUT" com.medievalfactions.tools.MFSimulationSystemsSelfTest
java -ea -cp "$OUT" com.medievalfactions.tools.MFPlayerSkinRulesSelfTest
