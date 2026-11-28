package com.medievalfactions.domain;

import java.util.*;

/** Parcel, zoning and material-reservation rules for settlement-scale city planning. */
public final class MFCityPlanner {
    private MFCityPlanner() {}
    public enum District { RESIDENTIAL, COMMERCIAL, INDUSTRIAL, CIVIC, MILITARY, AGRICULTURAL, SACRED }
    public enum BuildStage { PLANNED, FOUNDATION, FRAME, ROOF, FINISHING, ACTIVE, PAUSED }

    public record Bounds(int minX, int maxX, int minZ, int maxZ) {
        public Bounds { if (minX > maxX || minZ > maxZ) throw new IllegalArgumentException("Invalid city bounds"); }
        public boolean contains(int x, int z, int width, int depth) {
            return width > 0 && depth > 0 && x >= minX && z >= minZ && (long)x + width - 1 <= maxX && (long)z + depth - 1 <= maxZ;
        }
    }
    public record Building(String id, String blueprintId, District district, int x, int y, int z,
                           int width, int depth, int rotation, BuildStage stage, int progress, int requiredProgress) {
        public Building {
            if (id == null || id.isBlank() || blueprintId == null || blueprintId.isBlank() || district == null || stage == null
                    || width < 1 || depth < 1 || width > 128 || depth > 128 || rotation < 0 || rotation > 3
                    || progress < 0 || requiredProgress < 1 || progress > requiredProgress)
                throw new IllegalArgumentException("Invalid city building");
        }
        public Building advance(int amount) {
            int next = (int)Math.min((long)requiredProgress, (long)progress + Math.max(0, amount));
            BuildStage nextStage = next == requiredProgress ? BuildStage.ACTIVE
                    : next >= requiredProgress * 3 / 4 ? BuildStage.FINISHING
                    : next >= requiredProgress / 2 ? BuildStage.ROOF
                    : next >= requiredProgress / 4 ? BuildStage.FRAME : BuildStage.FOUNDATION;
            return new Building(id, blueprintId, district, x, y, z, width, depth, rotation, nextStage, next, requiredProgress);
        }
    }
    public record CityPlan(UUID id, UUID settlementId, String dimension, Bounds bounds, List<Building> buildings, long revision) {
        public CityPlan {
            Objects.requireNonNull(id); Objects.requireNonNull(settlementId); Objects.requireNonNull(bounds);
            if (dimension == null || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || revision < 0)
                throw new IllegalArgumentException("Invalid city plan");
            buildings = buildings == null ? List.of() : List.copyOf(buildings);
            if (buildings.size() > 4096 || buildings.stream().map(Building::id).distinct().count() != buildings.size())
                throw new IllegalArgumentException("Duplicate or excessive buildings");
        }
    }
    public record BuildingSpec(String blueprintId, District district, int width, int depth, int requiredProgress,
                               Map<String, Long> materialCost) {
        public BuildingSpec {
            if (blueprintId == null || blueprintId.isBlank() || district == null || width < 1 || depth < 1
                    || width > 128 || depth > 128 || requiredProgress < 1)
                throw new IllegalArgumentException("Invalid building spec");
            LinkedHashMap<String,Long> copy = new LinkedHashMap<>();
            if (materialCost != null) materialCost.forEach((k,v) -> {
                if (k == null || !k.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || v == null || v < 0) throw new IllegalArgumentException("Invalid material cost");
                if (v > 0) copy.put(k,v);
            });
            materialCost = Collections.unmodifiableMap(copy);
        }
    }
    /** Persistent join between a parcel/building identity and its physical block-placement job. */
    public record ConstructionLink(UUID cityPlanId, String buildingId, UUID constructionId) {
        public ConstructionLink {
            Objects.requireNonNull(cityPlanId); Objects.requireNonNull(constructionId);
            if (buildingId == null || buildingId.isBlank()) throw new IllegalArgumentException("Invalid city construction link");
        }
    }

    public record Placement(CityPlan plan, Map<String,Long> remainingInventory, Building building) {
        public Placement { remainingInventory = Collections.unmodifiableMap(new LinkedHashMap<>(remainingInventory)); }
    }

    /** Reserves every required material atomically and rejects out-of-bounds or overlapping footprints. */
    public static Optional<Placement> place(CityPlan plan, BuildingSpec spec, String buildingId,
                                              int x, int y, int z, int rotation, Map<String,Long> inventory) {
        if (plan == null || spec == null || buildingId == null || buildingId.isBlank() || inventory == null
                || plan.buildings().stream().anyMatch(b -> b.id().equals(buildingId))) return Optional.empty();
        int width = rotation % 2 == 0 ? spec.width() : spec.depth();
        int depth = rotation % 2 == 0 ? spec.depth() : spec.width();
        if (!plan.bounds().contains(x,z,width,depth)) return Optional.empty();
        for (Building b : plan.buildings()) {
            if (overlaps(x,z,width,depth,b.x(),b.z(),b.width(),b.depth())) return Optional.empty();
        }
        LinkedHashMap<String,Long> remaining = new LinkedHashMap<>(inventory);
        for (var cost : spec.materialCost().entrySet()) {
            long available = remaining.getOrDefault(cost.getKey(),0L);
            if (available < cost.getValue()) return Optional.empty();
            long next = available - cost.getValue();
            if (next == 0) remaining.remove(cost.getKey()); else remaining.put(cost.getKey(),next);
        }
        Building building = new Building(buildingId,spec.blueprintId(),spec.district(),x,y,z,width,depth,rotation,
                BuildStage.PLANNED,0,spec.requiredProgress());
        ArrayList<Building> buildings = new ArrayList<>(plan.buildings()); buildings.add(building);
        CityPlan updated = new CityPlan(plan.id(),plan.settlementId(),plan.dimension(),plan.bounds(),buildings,plan.revision()+1);
        return Optional.of(new Placement(updated,remaining,building));
    }

    public static Optional<CityPlan> start(CityPlan plan, String buildingId) {
        if (plan == null || buildingId == null) return Optional.empty();
        ArrayList<Building> next = new ArrayList<>(plan.buildings());
        for (int i = 0; i < next.size(); i++) {
            Building old = next.get(i);
            if (!old.id().equals(buildingId) || old.stage() != BuildStage.PLANNED) continue;
            next.set(i, new Building(old.id(), old.blueprintId(), old.district(), old.x(), old.y(), old.z(),
                    old.width(), old.depth(), old.rotation(), BuildStage.FOUNDATION, old.progress(), old.requiredProgress()));
            return Optional.of(new CityPlan(plan.id(), plan.settlementId(), plan.dimension(), plan.bounds(), next, plan.revision()+1));
        }
        return Optional.empty();
    }

    public static Optional<CityPlan> advance(CityPlan plan, String buildingId, int workPoints) {
        if (plan == null || buildingId == null || workPoints < 1) return Optional.empty();
        ArrayList<Building> next = new ArrayList<>(plan.buildings());
        for (int i=0;i<next.size();i++) if (next.get(i).id().equals(buildingId)) {
            Building old = next.get(i);
            if (old.stage() == BuildStage.ACTIVE) return Optional.empty();
            next.set(i,old.advance(workPoints));
            return Optional.of(new CityPlan(plan.id(),plan.settlementId(),plan.dimension(),plan.bounds(),next,plan.revision()+1));
        }
        return Optional.empty();
    }

    private static boolean overlaps(int ax,int az,int aw,int ad,int bx,int bz,int bw,int bd) {
        return (long)ax <= (long)bx+bw-1L && (long)ax+aw-1L >= bx
                && (long)az <= (long)bz+bd-1L && (long)az+ad-1L >= bz;
    }
}
