package com.medievalfactions.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable, one-pass population index shared by daily simulation systems.
 *
 * Building this index is O(citizens + work orders + active journeys); subsequent
 * lookups for medical cases, housing pressure, worker assignments and travelling
 * parties are O(1). It contains domain data only and can be exercised without
 * booting Minecraft.
 */
public record MFPopulationIndex(
        Map<UUID, MFCitizenState> citizensById,
        Map<UUID, Long> activeBySettlement,
        Set<UUID> activeWorkers,
        Set<UUID> travellingCitizens) {

    public MFPopulationIndex {
        citizensById = Map.copyOf(citizensById == null ? Map.of() : citizensById);
        activeBySettlement = Map.copyOf(activeBySettlement == null ? Map.of() : activeBySettlement);
        activeWorkers = Set.copyOf(activeWorkers == null ? Set.of() : activeWorkers);
        travellingCitizens = Set.copyOf(travellingCitizens == null ? Set.of() : travellingCitizens);
    }

    public static MFPopulationIndex build(
            Collection<MFCitizenState> citizens,
            Collection<MFWorkOrderState> workOrders,
            Collection<MFAdventureSystems.ExpeditionJourney> journeys) {
        Map<UUID, MFCitizenState> byId = new LinkedHashMap<>();
        Map<UUID, Long> active = new LinkedHashMap<>();
        Set<UUID> workers = new LinkedHashSet<>();
        Set<UUID> travelling = new LinkedHashSet<>();

        if (citizens != null) {
            for (MFCitizenState citizen : citizens) {
                if (citizen == null) continue;
                byId.put(citizen.id(), citizen);
                if (citizen.status() == MFGameCatalog.CitizenStatus.ACTIVE) {
                    active.merge(citizen.settlementId(), 1L, Long::sum);
                }
            }
        }
        if (workOrders != null) {
            for (MFWorkOrderState order : workOrders) {
                if (order != null && order.active()) workers.add(order.workerId());
            }
        }
        if (journeys != null) {
            for (MFAdventureSystems.ExpeditionJourney journey : journeys) {
                if (journey == null
                        || journey.status() == MFAdventureSystems.JourneyStatus.COMPLETED
                        || journey.status() == MFAdventureSystems.JourneyStatus.FAILED) continue;
                travelling.addAll(journey.memberIds());
            }
        }
        return new MFPopulationIndex(byId, active, workers, travelling);
    }

    public Optional<MFCitizenState> citizen(UUID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(citizensById.get(id));
    }

    public long activeCountAt(UUID settlementId) {
        return settlementId == null ? 0L : activeBySettlement.getOrDefault(settlementId, 0L);
    }

    public boolean isWorking(UUID citizenId) {
        return citizenId != null && activeWorkers.contains(citizenId);
    }

    public boolean isTravelling(UUID citizenId) {
        return citizenId != null && travellingCitizens.contains(citizenId);
    }

    public boolean isActive(UUID citizenId) {
        return citizen(citizenId).filter(c -> c.status() == MFGameCatalog.CitizenStatus.ACTIVE).isPresent();
    }
}
