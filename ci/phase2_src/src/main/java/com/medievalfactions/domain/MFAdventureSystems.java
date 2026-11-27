package com.medievalfactions.domain;

import java.util.*;

/** Physical route progress, expedition travel and phase-based dungeon boss encounters. */
public final class MFAdventureSystems {
    private MFAdventureSystems() {}
    public enum ConvoyStatus { TRAVELLING, AMBUSHED, DELIVERED, DESTROYED, RETURNING, SETTLED }
    public enum EncounterPhase { INTRO, PHASE_ONE, PHASE_TWO, ENRAGED, DEFEATED }
    public enum BossAction { STRIKE, AREA_ATTACK, SUMMON, SHIELD, RECOVER }
    public enum JourneyStatus { OUTBOUND, EXPLORING, RETURNING, COMPLETED, FAILED }

    public record RoutePoint(String dimension,int x,int y,int z) {
        public RoutePoint { if(dimension==null || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid route dimension"); }
    }
    public record PhysicalConvoy(UUID id, UUID sellerSettlement, UUID buyerSettlement, String itemId,
                                 long originalQuantity, long remainingQuantity, long escrowCopper,
                                 List<RoutePoint> route, int nextPoint, int escortStrength, int routeRisk,
                                 ConvoyStatus status, long seed) {
        public PhysicalConvoy {
            Objects.requireNonNull(id); Objects.requireNonNull(sellerSettlement); Objects.requireNonNull(buyerSettlement); Objects.requireNonNull(status);
            if(sellerSettlement.equals(buyerSettlement) || itemId==null || !itemId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                    || originalQuantity<1 || remainingQuantity<0 || remainingQuantity>originalQuantity || escrowCopper<0
                    || route==null || route.size()<2 || route.size()>4096 || nextPoint<0 || nextPoint>=route.size()
                    || escortStrength<0 || escortStrength>100 || routeRisk<0 || routeRisk>100)
                throw new IllegalArgumentException("Invalid physical convoy");
            route=List.copyOf(route);
        }
        public PhysicalConvoy advance() {
            if(status!=ConvoyStatus.TRAVELLING) return this;
            int next=nextPoint+1;
            boolean arrived=next>=route.size()-1;
            return new PhysicalConvoy(id,sellerSettlement,buyerSettlement,itemId,originalQuantity,remainingQuantity,escrowCopper,
                    route,arrived?route.size()-1:next,escortStrength,routeRisk,
                    arrived?ConvoyStatus.DELIVERED:status,seed);
        }
        public PhysicalConvoy ambush(int lossPercent) {
            if(status!=ConvoyStatus.TRAVELLING || lossPercent<1 || lossPercent>100) throw new IllegalStateException("Convoy cannot be ambushed");
            long lost=java.math.BigInteger.valueOf(remainingQuantity).multiply(java.math.BigInteger.valueOf(lossPercent))
                    .divide(java.math.BigInteger.valueOf(100)).longValue();
            lost=Math.max(1,Math.min(remainingQuantity,lost));
            long remaining=remainingQuantity-lost;
            ConvoyStatus next=remaining==0?ConvoyStatus.DESTROYED:ConvoyStatus.AMBUSHED;
            return new PhysicalConvoy(id,sellerSettlement,buyerSettlement,itemId,originalQuantity,remaining,escrowCopper,
                    route,nextPoint,escortStrength,routeRisk,next,seed);
        }
        public PhysicalConvoy resumeAfterAmbush() {
            if(status!=ConvoyStatus.AMBUSHED) throw new IllegalStateException("Convoy is not awaiting recovery");
            return new PhysicalConvoy(id,sellerSettlement,buyerSettlement,itemId,originalQuantity,remainingQuantity,escrowCopper,
                    route,nextPoint,escortStrength,routeRisk,ConvoyStatus.TRAVELLING,seed);
        }
        public PhysicalConvoy markSettled() {
            if(status!=ConvoyStatus.DELIVERED && status!=ConvoyStatus.DESTROYED)
                throw new IllegalStateException("Convoy is not ready for settlement");
            return new PhysicalConvoy(id,sellerSettlement,buyerSettlement,itemId,originalQuantity,remainingQuantity,escrowCopper,
                    route,nextPoint,escortStrength,routeRisk,ConvoyStatus.SETTLED,seed);
        }
        public int effectiveRisk() { return Math.max(0,routeRisk-escortStrength/2); }
    }

    public record BossEncounter(UUID id, String bossId, int tier, int health, int maxHealth, int shield,
                                int turn, EncounterPhase phase, long seed, int mechanicsResolved) {
        public BossEncounter {
            Objects.requireNonNull(id); Objects.requireNonNull(phase);
            if(bossId==null || !bossId.matches("[a-z0-9_.-]+") || tier<1 || tier>10 || health<0 || maxHealth<1 || health>maxHealth
                    || shield<0 || shield>maxHealth || turn<0 || mechanicsResolved<0) throw new IllegalArgumentException("Invalid boss encounter");
        }
    }
    /** Persisted link between the real Wither boss entity and its deterministic encounter state. */
    public record BossEntityLink(UUID encounterId, UUID entityId, String dimension, UUID ownerId, long spawnedDay) {
        public BossEntityLink {
            Objects.requireNonNull(encounterId);
            Objects.requireNonNull(entityId);
            Objects.requireNonNull(ownerId);
            if (dimension == null || dimension.isBlank() || dimension.length() > 128 || spawnedDay < 0)
                throw new IllegalArgumentException("Invalid boss entity link");
        }
    }

    public record BossTurn(BossEncounter encounter, BossAction action, int damageToParty, int shieldChange,
                           int bossHealing, int summonedMinions, String telegraph) {}

    public static BossEncounter createBoss(UUID id,String bossId,int tier,long seed) {
        int hp=100+tier*65;
        return new BossEncounter(id,bossId,tier,hp,hp,0,0,EncounterPhase.INTRO,seed,0);
    }

    /** Player damage is resolved on the server; phase thresholds and telegraphs are deterministic. */
    public static Optional<BossEncounter> damageBoss(BossEncounter boss,int damage,int armourPierce) {
        if(boss==null || damage<1 || armourPierce<0 || boss.phase()==EncounterPhase.DEFEATED) return Optional.empty();
        int absorbed=Math.min(boss.shield(),Math.max(0,damage-armourPierce));
        int remainingShield=boss.shield()-absorbed;
        int hpDamage=Math.max(0,damage-absorbed);
        int hp=Math.max(0,boss.health()-hpDamage);
        EncounterPhase phase=hp==0?EncounterPhase.DEFEATED:hp<=boss.maxHealth()/4?EncounterPhase.ENRAGED
                :hp<=boss.maxHealth()/2?EncounterPhase.PHASE_TWO:EncounterPhase.PHASE_ONE;
        return Optional.of(new BossEncounter(boss.id(),boss.bossId(),boss.tier(),hp,boss.maxHealth(),remainingShield,
                boss.turn(),phase,boss.seed(),boss.mechanicsResolved()));
    }

    public static BossTurn advanceBossTurn(BossEncounter boss,int partyDefence) {
        if(boss==null || boss.phase()==EncounterPhase.DEFEATED) throw new IllegalStateException("Boss already defeated");
        int phaseIndex=switch(boss.phase()){case INTRO,PHASE_ONE->0;case PHASE_TWO->1;case ENRAGED->2;case DEFEATED->3;};
        BossAction action=BossAction.values()[Math.floorMod((int)(boss.seed()+boss.turn()*17L+phaseIndex*11L),BossAction.values().length)];
        int raw=10+boss.tier()*3+phaseIndex*7+(action==BossAction.AREA_ATTACK?boss.tier()*2:0);
        int damage=Math.max(0,raw-Math.max(0,partyDefence));
        int shieldGain=action==BossAction.SHIELD?boss.tier()*8:0;
        int shield=Math.min(boss.maxHealth(),boss.shield()+shieldGain);
        int healing=action==BossAction.RECOVER?boss.tier()*5:0;
        int health=Math.min(boss.maxHealth(),boss.health()+healing);
        int summons=action==BossAction.SUMMON?1+boss.tier()/3:0;
        BossEncounter next=new BossEncounter(boss.id(),boss.bossId(),boss.tier(),health,boss.maxHealth(),shield,
                boss.turn()+1,boss.phase(),boss.seed(),boss.mechanicsResolved()+1);
        String telegraph=switch(action){case STRIKE->"El jefe prepara un golpe frontal";case AREA_ATTACK->"El jefe canaliza un ataque de área";
            case SUMMON->"El jefe invoca refuerzos";case SHIELD->"El jefe refuerza su escudo";case RECOVER->"El jefe busca recuperar el aliento";};
        return new BossTurn(next,action,damage,shieldGain,healing,summons,telegraph);
    }

    /**
     * Rejects a party if any member is already assigned to another unfinished physical journey.
     * This is intentionally a pure rule so command paths and automated tests share the same guard.
     */
    public static boolean membersAvailableForJourney(Collection<ExpeditionJourney> existingJourneys,
                                                     Collection<UUID> candidateMembers) {
        if (candidateMembers == null || candidateMembers.isEmpty() || candidateMembers.size() > 8
                || candidateMembers.stream().anyMatch(Objects::isNull)
                || candidateMembers.stream().distinct().count() != candidateMembers.size()) return false;
        if (existingJourneys == null || existingJourneys.isEmpty()) return true;
        Set<UUID> candidates = new HashSet<>(candidateMembers);
        for (ExpeditionJourney journey : existingJourneys) {
            if (journey == null || journey.status() == JourneyStatus.COMPLETED || journey.status() == JourneyStatus.FAILED) continue;
            if (journey.memberIds().stream().anyMatch(candidates::contains)) return false;
        }
        return true;
    }

    public record ExpeditionJourney(UUID id, UUID expeditionId, List<UUID> memberIds, List<RoutePoint> route,
                                    int nextPoint, long provisions, JourneyStatus status, int injuries,
                                    int discoveries, long lastAdvancedDay) {
        public ExpeditionJourney {
            Objects.requireNonNull(id); Objects.requireNonNull(expeditionId); Objects.requireNonNull(status);
            memberIds=List.copyOf(memberIds); route=List.copyOf(route);
            if(memberIds.isEmpty() || memberIds.size()>8 || memberIds.stream().distinct().count()!=memberIds.size()
                    || route.size()<2 || route.size()>4096 || nextPoint<0 || nextPoint>=route.size()
                    || provisions<0 || injuries<0 || injuries>memberIds.size() || discoveries<0 || lastAdvancedDay< -1)
                throw new IllegalArgumentException("Invalid expedition journey");
        }
        /** Target waypoint. The route index is the last waypoint reached by every party member. */
        public RoutePoint nextDestination() {
            if (status == JourneyStatus.OUTBOUND)
                return route.get(Math.min(route.size() - 1, nextPoint + 1));
            if (status == JourneyStatus.RETURNING)
                return route.get(Math.max(0, nextPoint - 1));
            return route.get(nextPoint);
        }

        /** Advances route progress only after the entire visible party reports arrival. */
        public ExpeditionJourney arriveAtNextPoint() {
            if (status == JourneyStatus.OUTBOUND) {
                int reached = Math.min(route.size() - 1, nextPoint + 1);
                return new ExpeditionJourney(id, expeditionId, memberIds, route, reached, provisions,
                        reached == route.size() - 1 ? JourneyStatus.EXPLORING : JourneyStatus.OUTBOUND,
                        injuries, discoveries, lastAdvancedDay);
            }
            if (status == JourneyStatus.RETURNING) {
                int reached = Math.max(0, nextPoint - 1);
                return new ExpeditionJourney(id, expeditionId, memberIds, route, reached, provisions,
                        reached == 0 ? JourneyStatus.COMPLETED : JourneyStatus.RETURNING,
                        injuries, discoveries, lastAdvancedDay);
            }
            return this;
        }

        /** Day simulation consumes provisions and resolves risks; it never teleports the party along its route. */
        public ExpeditionJourney advance(long day,boolean dangerousEncounter,boolean successfulDiscovery) {
            if(day<=lastAdvancedDay || status==JourneyStatus.COMPLETED || status==JourneyStatus.FAILED) return this;
            long cost=Math.max(1,memberIds.size());
            if(provisions<cost) return new ExpeditionJourney(id,expeditionId,memberIds,route,nextPoint,0,JourneyStatus.FAILED,
                    injuries,discoveries,day);
            long remaining=provisions-cost;
            int nextInjuries=Math.min(memberIds.size(),injuries+(dangerousEncounter?1:0));
            int nextDiscoveries=discoveries+(successfulDiscovery?1:0);
            JourneyStatus nextStatus = status == JourneyStatus.EXPLORING ? JourneyStatus.RETURNING : status;
            return new ExpeditionJourney(id,expeditionId,memberIds,route,nextPoint,remaining,nextStatus,
                    nextInjuries,nextDiscoveries,day);
        }
    }
}
