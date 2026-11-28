package com.medievalfactions.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Deterministic daily settlement loop. Inputs are finite stocks and the server is the authority. */
public final class MFSettlementSimulation {
    private MFSettlementSimulation() {}

    public static DayResult advanceDay(MFSettlementState settlement, List<MFCitizenState> residents) {
        if (settlement == null || residents == null) throw new IllegalArgumentException("Settlement and residents are required");
        List<MFCitizenState> updated = new ArrayList<>(residents.size());
        long foodRemaining = settlement.foodRations();
        long treasuryRemaining = settlement.treasuryCopper();
        long rationsConsumed = 0L;
        long maintenancePaid = settlement.dailyMaintenanceCopper() <= treasuryRemaining
                ? settlement.dailyMaintenanceCopper() : 0L;
        treasuryRemaining -= maintenancePaid;
        long wagesPaid = 0L;
        int active = 0;
        int deaths = 0;
        int departures = 0;
        for (MFCitizenState citizen : residents) {
            if (!citizen.settlementId().equals(settlement.id()) || !citizen.factionId().equals(settlement.factionId()))
                throw new IllegalArgumentException("Resident belongs to a different settlement or faction");
            if (citizen.status() != MFGameCatalog.CitizenStatus.ACTIVE) {
                updated.add(citizen);
                continue;
            }
            boolean fed = foodRemaining > 0L;
            if (fed) { foodRemaining--; rationsConsumed++; }
            boolean canWork = citizen.ageStage() != MFGameCatalog.AgeStage.BABY && citizen.ageStage() != MFGameCatalog.AgeStage.CHILD;
            long wage = canWork && citizen.dailyWageCopper() > 0L && citizen.canReceiveWage(citizen.dailyWageCopper())
                    && treasuryRemaining >= citizen.dailyWageCopper()
                    ? citizen.dailyWageCopper() : 0L;
            if (wage > 0L) {
                treasuryRemaining -= wage;
                wagesPaid = Math.addExact(wagesPaid, wage);
            }
            // A quiet day does not injure residents just because a settlement's guard rating is low.
            // Actual raids/battles will pass a danger result through a separate event pipeline.
            MFCitizenState next = citizen.advanceDay(fed, wage, true);
            updated.add(next);
            if (next.status() == MFGameCatalog.CitizenStatus.DEAD) deaths++;
            else if (next.status() == MFGameCatalog.CitizenStatus.LEFT) departures++;
            else if (next.status() == MFGameCatalog.CitizenStatus.ACTIVE) active++;
        }
        MFSettlementState nextSettlement = settlement.withFoodRations(foodRemaining)
                .withTreasury(treasuryRemaining).withPopulation(active);
        return new DayResult(nextSettlement, List.copyOf(updated), rationsConsumed, maintenancePaid,
                wagesPaid, active, deaths, departures);
    }

    public record DayResult(MFSettlementState settlement, List<MFCitizenState> residents,
                            long rationsConsumed, long maintenancePaidCopper, long wagesPaidCopper, int activePopulation,
                            int deaths, int departures) {}
}
