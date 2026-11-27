package com.medievalfactions.domain;

import java.util.*;

/** Server-authoritative, deterministic tactical combat and campaign logistics rules. */
public final class MFTacticalWarfare {
    private MFTacticalWarfare() {}
    public enum Stance { READY, BLOCKING, EVADING }
    public enum Action { MOVE, ATTACK, BLOCK, DODGE, RALLY, RETREAT }
    public enum BattleStatus { ACTIVE, ATTACKER_VICTORY, DEFENDER_VICTORY, WITHDRAWN }
    public enum Formation { LINE, WEDGE, SQUARE, SKIRMISH, SHIELD_WALL }

    public record Unit(UUID id, UUID factionId, int x, int z, int health, int maxHealth, int armor,
                       int attack, int attackRange, int stamina, int morale, int actionPoints,
                       Stance stance, boolean retreated) {
        public Unit {
            Objects.requireNonNull(id); Objects.requireNonNull(factionId); Objects.requireNonNull(stance);
            if (health < 0 || maxHealth < 1 || health > maxHealth || armor < 0 || armor > 100 || attack < 0
                    || attackRange < 1 || attackRange > 8 || stamina < 0 || stamina > 100 || morale < 0 || morale > 100
                    || actionPoints < 0 || actionPoints > 4 || (retreated && health > 0))
                throw new IllegalArgumentException("Invalid tactical unit");
        }
        public boolean active() { return health > 0 && !retreated; }
        public Unit with(int nx,int nz,int hp,int st,int mr,int ap,Stance next,boolean escaped) {
            return new Unit(id,factionId,nx,nz,Math.max(0,Math.min(maxHealth,hp)),maxHealth,armor,attack,attackRange,
                    Math.max(0,Math.min(100,st)),Math.max(0,Math.min(100,mr)),Math.max(0,Math.min(4,ap)),next,escaped);
        }
    }
    public record Battle(UUID id, UUID attackerFaction, UUID defenderFaction, UUID activeFaction,
                         int turn, List<Unit> units, BattleStatus status, UUID winnerFaction) {
        public Battle {
            Objects.requireNonNull(id); Objects.requireNonNull(attackerFaction); Objects.requireNonNull(defenderFaction);
            Objects.requireNonNull(activeFaction); Objects.requireNonNull(status);
            if (attackerFaction.equals(defenderFaction) || (!activeFaction.equals(attackerFaction) && !activeFaction.equals(defenderFaction))
                    || turn < 1 || units == null || units.isEmpty() || units.size() > 256
                    || units.stream().map(Unit::id).distinct().count() != units.size()
                    || units.stream().anyMatch(u -> !u.factionId().equals(attackerFaction) && !u.factionId().equals(defenderFaction))
                    || units.stream().noneMatch(u -> u.factionId().equals(attackerFaction))
                    || units.stream().noneMatch(u -> u.factionId().equals(defenderFaction))) throw new IllegalArgumentException("Invalid battle");
            units = List.copyOf(units);
        }
    }
    public record Order(UUID actorId, Action action, UUID targetId, int destinationX, int destinationZ) {
        public Order { Objects.requireNonNull(actorId); Objects.requireNonNull(action); }
    }
    public record OrderResult(Battle battle, int damage, String message) {}

    public static Optional<OrderResult> apply(Battle battle, Order order) {
        if (battle == null || order == null || battle.status() != BattleStatus.ACTIVE) return Optional.empty();
        Unit actor = battle.units().stream().filter(u -> u.id().equals(order.actorId())).findFirst().orElse(null);
        if (actor == null || !actor.active() || !actor.factionId().equals(battle.activeFaction()) || actor.actionPoints() < 1)
            return Optional.empty();
        ArrayList<Unit> next = new ArrayList<>(battle.units()); int damage = 0; String message;
        int actorIndex = indexOf(next,actor.id());
        switch (order.action()) {
            case MOVE -> {
                int distance = Math.abs(actor.x()-order.destinationX()) + Math.abs(actor.z()-order.destinationZ());
                boolean occupied = next.stream().anyMatch(u -> u.active() && !u.id().equals(actor.id())
                        && u.x()==order.destinationX() && u.z()==order.destinationZ());
                if (distance < 1 || distance > 3 || occupied) return Optional.empty();
                next.set(actorIndex,actor.with(order.destinationX(),order.destinationZ(),actor.health(),actor.stamina(),actor.morale(),actor.actionPoints()-1,Stance.READY,false));
                message = "Unidad reposicionada";
            }
            case ATTACK -> {
                Unit target = next.stream().filter(u -> u.id().equals(order.targetId())).findFirst().orElse(null);
                if (target == null || !target.active() || target.factionId().equals(actor.factionId())) return Optional.empty();
                int distance = Math.abs(actor.x()-target.x()) + Math.abs(actor.z()-target.z());
                if (distance > actor.attackRange() || actor.stamina() < 3) return Optional.empty();
                int base = Math.max(1,actor.attack() - target.armor()/2);
                if (target.stance()==Stance.BLOCKING) base = Math.max(1,base/3);
                if (target.stance()==Stance.EVADING && Math.floorMod(battle.turn()*31 + actor.id().hashCode() + target.id().hashCode(),100) < 55) base = 0;
                damage = Math.min(target.health(),base);
                int ti = indexOf(next,target.id());
                next.set(ti,target.with(target.x(),target.z(),target.health()-damage,target.stamina(),target.morale(),target.actionPoints(),Stance.READY,false));
                next.set(actorIndex,actor.with(actor.x(),actor.z(),actor.health(),actor.stamina()-3,actor.morale(),actor.actionPoints()-1,Stance.READY,false));
                message = damage == 0 ? "El objetivo esquiva el ataque" : "Ataque inflige " + damage + " de daño";
            }
            case BLOCK -> {
                if (actor.stamina() < 2) return Optional.empty();
                next.set(actorIndex,actor.with(actor.x(),actor.z(),actor.health(),actor.stamina()-2,actor.morale(),actor.actionPoints()-1,Stance.BLOCKING,false));
                message = "La unidad adopta guardia defensiva";
            }
            case DODGE -> {
                if (actor.stamina() < 8) return Optional.empty();
                next.set(actorIndex,actor.with(actor.x(),actor.z(),actor.health(),actor.stamina()-8,actor.morale(),actor.actionPoints()-1,Stance.EVADING,false));
                message = "La unidad se prepara para esquivar";
            }
            case RALLY -> {
                next.set(actorIndex,actor.with(actor.x(),actor.z(),actor.health(),actor.stamina()+10,actor.morale()+20,actor.actionPoints()-1,Stance.READY,false));
                message = "La unidad recupera moral";
            }
            case RETREAT -> {
                next.set(actorIndex,actor.with(actor.x(),actor.z(),0,actor.stamina(),0,actor.actionPoints()-1,Stance.READY,true));
                message = "La unidad se retira del combate";
            }
            default -> { return Optional.empty(); }
        }
        long attackers = next.stream().filter(u -> u.factionId().equals(battle.attackerFaction()) && u.active()).count();
        long defenders = next.stream().filter(u -> u.factionId().equals(battle.defenderFaction()) && u.active()).count();
        BattleStatus status = attackers == 0 ? BattleStatus.DEFENDER_VICTORY : defenders == 0 ? BattleStatus.ATTACKER_VICTORY : BattleStatus.ACTIVE;
        UUID winner = status == BattleStatus.ATTACKER_VICTORY ? battle.attackerFaction()
                : status == BattleStatus.DEFENDER_VICTORY ? battle.defenderFaction() : null;
        return Optional.of(new OrderResult(new Battle(battle.id(),battle.attackerFaction(),battle.defenderFaction(),
                battle.activeFaction(),battle.turn(),next,status,winner),damage,message));
    }

    public static Optional<Battle> endTurn(Battle battle) {
        if (battle == null || battle.status()!=BattleStatus.ACTIVE) return Optional.empty();
        UUID nextFaction = battle.activeFaction().equals(battle.attackerFaction()) ? battle.defenderFaction() : battle.attackerFaction();
        ArrayList<Unit> units = new ArrayList<>();
        for (Unit u : battle.units()) units.add(u.factionId().equals(nextFaction) && u.active()
                ? u.with(u.x(),u.z(),u.health(),u.stamina()+5,u.morale(),2,Stance.READY,false) : u);
        return Optional.of(new Battle(battle.id(),battle.attackerFaction(),battle.defenderFaction(),nextFaction,
                battle.turn()+1,units,battle.status(),battle.winnerFaction()));
    }
    private static int indexOf(List<Unit> units, UUID id) { for(int i=0;i<units.size();i++) if(units.get(i).id().equals(id)) return i; return -1; }

    public record ArmyColumn(UUID id, UUID factionId, UUID commanderId, List<UUID> unitIds, Formation formation,
                             int troopCount, long provisions, int morale, List<MFCitizenSimulation.Waypoint> route,
                             int nextWaypoint, long lastAdvancedDay) {
        public ArmyColumn {
            Objects.requireNonNull(id); Objects.requireNonNull(factionId); Objects.requireNonNull(commanderId); Objects.requireNonNull(formation);
            unitIds = List.copyOf(unitIds); route = List.copyOf(route);
            if (unitIds.isEmpty() || unitIds.size()>256 || unitIds.stream().distinct().count()!=unitIds.size()
                    || troopCount < 1 || troopCount > 100_000 || provisions < 0 || morale < 0 || morale > 100
                    || route.size()<2 || nextWaypoint<0 || nextWaypoint>=route.size() || lastAdvancedDay<0)
                throw new IllegalArgumentException("Invalid army column");
        }
        public ArmyColumn advanceDay(long day) {
            if (day<=lastAdvancedDay || nextWaypoint>=route.size()-1) return this;
            long rationCost = Math.max(1L,troopCount/20L);
            if (provisions < rationCost) {
                int casualties = Math.max(1, troopCount / 100);
                return new ArmyColumn(id,factionId,commanderId,unitIds,formation,Math.max(1,troopCount-casualties),0,
                        Math.max(0,morale-8),route,nextWaypoint,day);
            }
            return new ArmyColumn(id,factionId,commanderId,unitIds,formation,troopCount,provisions-rationCost,
                    Math.min(100,morale+1),route,nextWaypoint+1,day);
        }
    }

    public record Occupation(UUID settlementId, UUID formerFaction, UUID occupyingFaction, long startedDay,
                             long lastAdvancedDay, int control, boolean resolved) {
        public Occupation {
            Objects.requireNonNull(settlementId); Objects.requireNonNull(formerFaction); Objects.requireNonNull(occupyingFaction);
            if (formerFaction.equals(occupyingFaction) || startedDay<0 || lastAdvancedDay<startedDay-1
                    || control<0 || control>100) throw new IllegalArgumentException("Invalid occupation");
        }
        public Occupation advance(long day, boolean supplied, boolean resistance) {
            if (resolved || day<=lastAdvancedDay || day<startedDay) return this;
            int next = Math.max(0,Math.min(100,control + (supplied?12:-15) + (resistance?-10:0)));
            return new Occupation(settlementId,formerFaction,occupyingFaction,startedDay,day,next,next>=100 || next==0);
        }
        public boolean captured() { return resolved && control>=100; }
    }
}
