package com.medievalfactions.domain;

import java.util.*;

import static com.medievalfactions.domain.MFGameCatalog.*;

/** Deterministic NPC schedules, needs overrides and bounded 3-D A* navigation rules. */
public final class MFCitizenSimulation {
    private MFCitizenSimulation() {}

    public enum Activity { SLEEP, WORK, EAT, SOCIALIZE, TRAIN, GUARD, TRAVEL, REST, VISIT_MEDIC }
    public enum Directive { SLEEP, WORK, SEEK_FOOD, SOCIALIZE, TRAIN, GUARD, TRAVEL, REST, SEEK_MEDIC, SEEK_SHELTER, FLEE }

    public record Waypoint(String dimension, int x, int y, int z) {
        public Waypoint {
            if (dimension == null || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalArgumentException("Invalid waypoint dimension");
        }
    }

    public record ScheduleEntry(int minuteOfDay, Activity activity, Waypoint destination) {
        public ScheduleEntry {
            if (minuteOfDay < 0 || minuteOfDay >= 1440 || activity == null || destination == null)
                throw new IllegalArgumentException("Invalid schedule entry");
        }
    }

    public record CitizenSchedule(UUID citizenId, List<ScheduleEntry> entries) {
        public CitizenSchedule {
            Objects.requireNonNull(citizenId);
            if (entries == null || entries.isEmpty() || entries.size() > 24)
                throw new IllegalArgumentException("A schedule must contain 1..24 entries");
            ArrayList<ScheduleEntry> sorted = new ArrayList<>(entries);
            sorted.sort(Comparator.comparingInt(ScheduleEntry::minuteOfDay));
            for (int i = 1; i < sorted.size(); i++)
                if (sorted.get(i - 1).minuteOfDay() == sorted.get(i).minuteOfDay())
                    throw new IllegalArgumentException("Duplicate schedule minute");
            entries = List.copyOf(sorted);
        }

        public ScheduleEntry at(int minuteOfDay) {
            int minute = Math.floorMod(minuteOfDay, 1440);
            ScheduleEntry selected = entries.get(entries.size() - 1); // schedule wraps over midnight
            for (ScheduleEntry entry : entries) {
                if (entry.minuteOfDay() > minute) break;
                selected = entry;
            }
            return selected;
        }
    }

    public record ResidentEntityLink(UUID citizenId, UUID entityId, String dimension) {
        public ResidentEntityLink {
            Objects.requireNonNull(citizenId); Objects.requireNonNull(entityId);
            if (dimension == null || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalArgumentException("Invalid resident entity link");
        }
    }

    public record CitizenPlan(UUID citizenId, Directive directive, Activity activity,
                              Waypoint destination, int priority, String reason) {
        public CitizenPlan {
            Objects.requireNonNull(citizenId); Objects.requireNonNull(directive);
            Objects.requireNonNull(activity); Objects.requireNonNull(destination);
            priority = Math.max(0, Math.min(100, priority));
            reason = reason == null ? "" : reason.substring(0, Math.min(160, reason.length()));
        }
    }

    /** Survival and danger override the timetable; normal life follows the citizen's authored schedule. */
    public static Optional<CitizenPlan> plan(MFCitizenState citizen, CitizenSchedule schedule, int minuteOfDay,
                                              int threat, boolean foodAvailable, boolean safeHousing,
                                              Waypoint foodPoint, Waypoint medicPoint, Waypoint shelterPoint) {
        if (citizen == null || schedule == null || !citizen.id().equals(schedule.citizenId())
                || citizen.status() != CitizenStatus.ACTIVE) return Optional.empty();
        ScheduleEntry scheduled = schedule.at(minuteOfDay);
        if (citizen.health() <= 25 && medicPoint != null)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.SEEK_MEDIC, Activity.VISIT_MEDIC, medicPoint, 100, "Salud crítica"));
        if (threat >= 80 && citizen.health() <= 55 && shelterPoint != null)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.FLEE, Activity.TRAVEL, shelterPoint, 100, "Huye de una amenaza letal"));
        if (threat >= 55 && citizen.profession() != Profession.GUARD && shelterPoint != null)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.SEEK_SHELTER, Activity.TRAVEL, shelterPoint, 92, "Busca refugio"));
        if (citizen.hunger() >= 70 && foodAvailable && foodPoint != null)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.SEEK_FOOD, Activity.EAT, foodPoint, 90, "Necesita alimentarse"));
        if (!safeHousing && shelterPoint != null)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.SEEK_SHELTER, Activity.TRAVEL, shelterPoint, 82, "No dispone de refugio seguro"));
        if (citizen.ageStage() == AgeStage.BABY || citizen.ageStage() == AgeStage.CHILD)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.REST, Activity.REST, scheduled.destination(), 80, "Menor dependiente"));
        Directive directive = switch (scheduled.activity()) {
            case SLEEP -> Directive.SLEEP;
            case WORK -> Directive.WORK;
            case EAT -> Directive.SEEK_FOOD;
            case SOCIALIZE -> Directive.SOCIALIZE;
            case TRAIN -> Directive.TRAIN;
            case GUARD -> Directive.GUARD;
            case TRAVEL -> Directive.TRAVEL;
            case REST -> Directive.REST;
            case VISIT_MEDIC -> Directive.SEEK_MEDIC;
        };
        if (threat >= 25 && citizen.profession() == Profession.GUARD && scheduled.activity() != Activity.SLEEP)
            return Optional.of(new CitizenPlan(citizen.id(), Directive.GUARD, Activity.GUARD, scheduled.destination(), 85, "Guardia responde a una amenaza"));
        return Optional.of(new CitizenPlan(citizen.id(), directive, scheduled.activity(), scheduled.destination(), 50, "Horario diario"));
    }

    public record NavCell(int x, int y, int z) {}
    public record NavBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        public NavBounds {
            if (minX > maxX || minY > maxY || minZ > maxZ) throw new IllegalArgumentException("Invalid navigation bounds");
            long volume = ((long)maxX-minX+1L) * ((long)maxY-minY+1L) * ((long)maxZ-minZ+1L);
            if (volume > 2_000_000L) throw new IllegalArgumentException("Navigation bounds too large");
        }
        public boolean contains(NavCell c) {
            return c.x() >= minX && c.x() <= maxX && c.y() >= minY && c.y() <= maxY && c.z() >= minZ && c.z() <= maxZ;
        }
    }
    public record NavigationPath(List<NavCell> cells, int visitedNodes) {
        public NavigationPath { cells = List.copyOf(cells); }
    }

    /** A bounded A* suitable for translating a chunk-local collision snapshot into NPC navigation. */
    public static Optional<NavigationPath> findPath(NavCell start, NavCell goal, Set<NavCell> blocked,
                                                     NavBounds bounds, int maxVisitedNodes) {
        if (start == null || goal == null || blocked == null || bounds == null || maxVisitedNodes < 1
                || !bounds.contains(start) || !bounds.contains(goal) || blocked.contains(start) || blocked.contains(goal))
            return Optional.empty();
        record Node(NavCell cell, int g, int f) {}
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::f).thenComparingInt(Node::g));
        Map<NavCell, Integer> cost = new HashMap<>();
        Map<NavCell, NavCell> cameFrom = new HashMap<>();
        cost.put(start, 0); open.add(new Node(start, 0, heuristic(start, goal)));
        int visited = 0;
        int[][] offsets = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
        while (!open.isEmpty() && visited < maxVisitedNodes) {
            Node current = open.poll();
            if (current.g() != cost.getOrDefault(current.cell(), Integer.MAX_VALUE)) continue;
            visited++;
            if (current.cell().equals(goal)) {
                ArrayList<NavCell> path = new ArrayList<>();
                NavCell cursor = goal; path.add(cursor);
                while (!cursor.equals(start)) { cursor = cameFrom.get(cursor); if (cursor == null) return Optional.empty(); path.add(cursor); }
                Collections.reverse(path);
                return Optional.of(new NavigationPath(path, visited));
            }
            for (int[] d : offsets) {
                NavCell next = new NavCell(current.cell().x()+d[0], current.cell().y()+d[1], current.cell().z()+d[2]);
                if (!bounds.contains(next) || blocked.contains(next)) continue;
                int stepCost = d[1] == 0 ? 10 : 14;
                int nextCost = current.g() + stepCost;
                if (nextCost < cost.getOrDefault(next, Integer.MAX_VALUE)) {
                    cost.put(next, nextCost); cameFrom.put(next, current.cell());
                    open.add(new Node(next, nextCost, nextCost + heuristic(next, goal)));
                }
            }
        }
        return Optional.empty();
    }

    private static int heuristic(NavCell a, NavCell b) {
        return 10 * (Math.abs(a.x()-b.x()) + Math.abs(a.z()-b.z())) + 14 * Math.abs(a.y()-b.y());
    }
}
