package com.medievalfactions.domain;

import java.util.*;
import static com.medievalfactions.domain.MFGameCatalog.*;
import static com.medievalfactions.domain.MFAdvancedSystems.*;

/** Genetic inheritance, egg incubation, growth and autonomous dragon decision rules. */
public final class MFDragonLifecycle {
    private MFDragonLifecycle() {}
    public enum EggStatus { FERTILE, INCUBATING, READY_TO_HATCH, HATCHED, FAILED }
    public enum LifeStage { HATCHLING, JUVENILE, ADULT, ELDER }
    public enum AutonomyOrder { FOLLOW_OWNER, DEFEND_HOME, PATROL, HUNT, SEEK_FOOD, REST, RETURN_HOME }
    public enum DragonIntent { FOLLOW_OWNER, DEFEND, PATROL, HUNT, SEEK_FOOD, REST, RETURN_HOME }

    /** Persistable link between a registered owned dragon and its inherited genetic profile. */
    public record DragonGenomeRecord(UUID dragonId, UUID ownerId, DragonGenome genome) {
        public DragonGenomeRecord {
            Objects.requireNonNull(dragonId); Objects.requireNonNull(ownerId); Objects.requireNonNull(genome);
        }
    }

    public record DragonGenome(Element element, int vitality, int agility, int flamePower, int intelligence, int temperament) {
        public DragonGenome {
            Objects.requireNonNull(element);
            if (vitality<1 || vitality>100 || agility<1 || agility>100 || flamePower<1 || flamePower>100
                    || intelligence<1 || intelligence>100 || temperament<1 || temperament>100)
                throw new IllegalArgumentException("Invalid dragon genome");
        }
        public int healthBonus() { return vitality/5; }
        public int flightSpeed() { return 5 + agility/20; }
    }

    public record DragonEgg(UUID id, UUID parentA, UUID parentB, UUID ownerId, DragonGenome genome,
                            EggStatus status, int incubationProgress, int requiredProgress, int lastIncubatedDay,
                            int warmth, int care) {
        public DragonEgg {
            Objects.requireNonNull(id); Objects.requireNonNull(parentA); Objects.requireNonNull(parentB);
            Objects.requireNonNull(ownerId); Objects.requireNonNull(genome); Objects.requireNonNull(status);
            if(parentA.equals(parentB) || incubationProgress<0 || requiredProgress<1 || incubationProgress>requiredProgress
                    || lastIncubatedDay< -1 || warmth<0 || warmth>100 || care<0 || care>100)
                throw new IllegalArgumentException("Invalid dragon egg");
        }
        public DragonEgg withEnvironment(int warmthScore,int careScore) {
            if(status==EggStatus.HATCHED || status==EggStatus.FAILED) return this;
            return new DragonEgg(id,parentA,parentB,ownerId,genome,status,incubationProgress,requiredProgress,lastIncubatedDay,
                    clamp(warmthScore),clamp(careScore));
        }
        public DragonEgg advanceDay(int day) {
            if(day<=lastIncubatedDay || status==EggStatus.HATCHED || status==EggStatus.FAILED) return this;
            if(warmth<30) return new DragonEgg(id,parentA,parentB,ownerId,genome,status,Math.max(0,incubationProgress-1),
                    requiredProgress,day,warmth,care);
            int gain = Math.max(1,Math.min(4,(warmth-25)/20 + care/50));
            int progress = Math.min(requiredProgress,incubationProgress+gain);
            EggStatus next = progress>=requiredProgress ? EggStatus.READY_TO_HATCH : EggStatus.INCUBATING;
            return new DragonEgg(id,parentA,parentB,ownerId,genome,next,progress,requiredProgress,day,warmth,care);
        }
        public DragonEgg hatch() {
            if(status!=EggStatus.READY_TO_HATCH) throw new IllegalStateException("Egg is not ready to hatch");
            return new DragonEgg(id,parentA,parentB,ownerId,genome,EggStatus.HATCHED,incubationProgress,requiredProgress,
                    lastIncubatedDay,warmth,care);
        }
        private static int clamp(int n) { return Math.max(0,Math.min(100,n)); }
    }
    public record Hatchling(UUID id, UUID ownerId, DragonGenome genome, LifeStage stage, int ageDays,
                            int health, int maxHealth, int hunger, int bond) {
        public Hatchling {
            Objects.requireNonNull(id); Objects.requireNonNull(ownerId); Objects.requireNonNull(genome); Objects.requireNonNull(stage);
            if(ageDays<0 || health<0 || maxHealth<1 || health>maxHealth || hunger<0 || hunger>100 || bond<0 || bond>100)
                throw new IllegalArgumentException("Invalid hatchling");
        }
        public Hatchling advanceDay() {
            int age=ageDays==Integer.MAX_VALUE?Integer.MAX_VALUE:ageDays+1;
            LifeStage next=age<30?LifeStage.HATCHLING:age<180?LifeStage.JUVENILE:age<900?LifeStage.ADULT:LifeStage.ELDER;
            return new Hatchling(id,ownerId,genome,next,age,health,maxHealth,Math.min(100,hunger+4),bond);
        }
        public Hatchling feed(int food) { return food<=0?this:new Hatchling(id,ownerId,genome,stage,ageDays,health,maxHealth,Math.max(0,hunger-food),Math.min(100,bond+1)); }
        public boolean canBreed() { return stage==LifeStage.ADULT && health>=60 && hunger<60; }
    }

    public static Optional<DragonEgg> breed(UUID ownerId, UUID parentAId, UUID parentBId,
                                             Companion parentA, Companion parentB,
                                             DragonGenome genomeA, DragonGenome genomeB, long seed, int day) {
        if(ownerId==null || parentAId==null || parentBId==null || parentA==null || parentB==null || genomeA==null || genomeB==null
                || parentAId.equals(parentBId) || !parentA.id().equals(parentAId) || !parentB.id().equals(parentBId)
                || !parentA.dragon() || !parentB.dragon() || !parentA.alive() || !parentB.alive()
                || !ownerId.equals(parentA.ownerId()) || !ownerId.equals(parentB.ownerId())
                || parentA.level()<10 || parentB.level()<10 || day<0) return Optional.empty();
        SplittableRandom random=new SplittableRandom(seed);
        DragonGenome child=new DragonGenome(random.nextBoolean()?genomeA.element():genomeB.element(),
                inherit(genomeA.vitality(),genomeB.vitality(),random),inherit(genomeA.agility(),genomeB.agility(),random),
                inherit(genomeA.flamePower(),genomeB.flamePower(),random),inherit(genomeA.intelligence(),genomeB.intelligence(),random),
                inherit(genomeA.temperament(),genomeB.temperament(),random));
        return Optional.of(new DragonEgg(UUID.randomUUID(),parentAId,parentBId,ownerId,child,EggStatus.FERTILE,0,30,-1,0,0));
    }

    public static Optional<Hatchling> createHatchling(DragonEgg egg, UUID hatchlingId) {
        if(egg==null || hatchlingId==null || egg.status()!=EggStatus.READY_TO_HATCH) return Optional.empty();
        int maxHealth=40+egg.genome().healthBonus();
        return Optional.of(new Hatchling(hatchlingId,egg.ownerId(),egg.genome(),LifeStage.HATCHLING,0,maxHealth,maxHealth,20,10));
    }

    public static DragonIntent chooseIntent(AutonomyOrder order, int hunger, int health, boolean ownerNearby,
                                            boolean threatNearby, boolean homeAvailable, boolean targetAvailable) {
        if(hunger>=75) return DragonIntent.SEEK_FOOD;
        if(health<=30) return homeAvailable?DragonIntent.RETURN_HOME:DragonIntent.REST;
        if(threatNearby) return DragonIntent.DEFEND;
        if(order==null) order=AutonomyOrder.FOLLOW_OWNER;
        return switch(order) {
            case FOLLOW_OWNER -> ownerNearby?DragonIntent.REST:DragonIntent.FOLLOW_OWNER;
            case DEFEND_HOME -> homeAvailable?DragonIntent.DEFEND:DragonIntent.FOLLOW_OWNER;
            case PATROL -> DragonIntent.PATROL;
            case HUNT -> targetAvailable?DragonIntent.HUNT:DragonIntent.PATROL;
            case SEEK_FOOD -> DragonIntent.SEEK_FOOD;
            case REST -> DragonIntent.REST;
            case RETURN_HOME -> homeAvailable?DragonIntent.RETURN_HOME:DragonIntent.FOLLOW_OWNER;
        };
    }

    private static int inherit(int a,int b,SplittableRandom random) { return Math.max(1,Math.min(100,(a+b)/2+random.nextInt(-5,6))); }
}
