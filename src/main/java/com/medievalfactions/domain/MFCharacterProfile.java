package com.medievalfactions.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;

import static com.medievalfactions.domain.MFGameCatalog.*;

/** Persistent player progression. Affinity and element are rolled once and cannot be rerolled. */
public record MFCharacterProfile(
        UUID playerId,
        RaceFamily race,
        Affinity affinity,
        Element element,
        CombatClass mainClass,
        Profession mainProfession,
        String subclass,
        int classLevel,
        long combatXp,
        Set<String> unlockedNodes,
        int professionLevel,
        long professionXp,
        boolean subclassTrialCompleted) {

    public static final int MAX_CLASS_LEVEL = 65;
    public static final int MAX_PROFESSION_LEVEL = 50;
    public static final int MAX_TALENT_POINTS = 32;
    public static final int MIN_SUBCLASS_LEVEL = 15;
    public static final int MIN_SUBCLASS_NODES = 1;

    public MFCharacterProfile {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(race, "race");
        Objects.requireNonNull(affinity, "affinity");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(mainClass, "mainClass");
        Objects.requireNonNull(mainProfession, "mainProfession");
        if (element.affinity() != affinity) throw new IllegalArgumentException("Element does not match affinity");
        if (classLevel < 1 || classLevel > MAX_CLASS_LEVEL || combatXp < 0L) throw new IllegalArgumentException("Invalid class progression");
        if (professionLevel < 1 || professionLevel > MAX_PROFESSION_LEVEL || professionXp < 0L) throw new IllegalArgumentException("Invalid profession progression");
        if (subclass != null && subclass.isBlank()) subclass = null;
        if (subclass != null && !subclass.matches("[a-z0-9_.-]{2,48}")) throw new IllegalArgumentException("Invalid subclass id");
        LinkedHashSet<String> copy = new LinkedHashSet<>();
        if (unlockedNodes != null) {
            for (String node : unlockedNodes) {
                if (node == null || !node.matches("[a-z0-9_.-]{2,64}")) throw new IllegalArgumentException("Invalid skill node");
                copy.add(node);
            }
        }
        long paidNodes = copy.stream().filter(node -> !MFSkillTreeCatalog.isCoreNode(node)).count();
        if (paidNodes > earnedTalentPoints(classLevel)) throw new IllegalArgumentException("More skill nodes than earned points");
        if (!copy.containsAll(MFSkillTreeCatalog.starterCoreNodes(mainClass)))
            throw new IllegalArgumentException("Character is missing mandatory core tree nodes");
        for (String nodeId : copy) {
            MFSkillTreeCatalog.Node node = MFSkillTreeCatalog.node(mainClass, nodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Node does not belong to this class: " + nodeId));
            if (!copy.containsAll(node.prerequisites())) throw new IllegalArgumentException("Stored node has missing prerequisites: " + nodeId);
            if (node.minimumClassLevel() > classLevel) throw new IllegalArgumentException("Stored node exceeds class level");
        }
        if (subclass != null) {
            MFSkillTreeCatalog.Node gate = MFSkillTreeCatalog.subclassGate(mainClass, subclass)
                    .orElseThrow(() -> new IllegalArgumentException("Stored subclass does not belong to this class"));
            if (!copy.contains(gate.id()) || !subclassTrialCompleted)
                throw new IllegalArgumentException("Stored subclass lacks its gate or trial");
        }
        unlockedNodes = Collections.unmodifiableSet(copy);
    }

    public static MFCharacterProfile create(UUID playerId, RaceFamily race, CombatClass combatClass,
                                            Profession profession, long seed) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(race, "race");
        Objects.requireNonNull(combatClass, "combatClass");
        Objects.requireNonNull(profession, "profession");
        SplittableRandom random = new SplittableRandom(seed);
        Affinity affinity = random.nextBoolean() ? Affinity.SOLAR : Affinity.LUNAR;
        var elements = elementsFor(affinity);
        Element element = elements.get(random.nextInt(elements.size()));
        return new MFCharacterProfile(playerId, race, affinity, element, combatClass, profession,
                null, 1, 0L, MFSkillTreeCatalog.starterCoreNodes(combatClass), 1, 0L, false);
    }

    public int talentPointsEarned() { return earnedTalentPoints(classLevel); }
    public int talentPointsSpent() { return (int) unlockedNodes.stream().filter(node -> !MFSkillTreeCatalog.isCoreNode(node)).count(); }
    public int talentPointsAvailable() { return talentPointsEarned() - talentPointsSpent(); }

    public static int earnedTalentPoints(int level) {
        int bounded = Math.max(1, Math.min(MAX_CLASS_LEVEL, level));
        return Math.min(MAX_TALENT_POINTS, Math.max(0, (bounded - 1) / 2));
    }

    public static long combatXpForNextLevel(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= MAX_CLASS_LEVEL) return Long.MAX_VALUE;
        return 250L + 50L * currentLevel;
    }

    public static long professionXpForNextLevel(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= MAX_PROFESSION_LEVEL) return Long.MAX_VALUE;
        return 400L + 75L * currentLevel;
    }

    public MFCharacterProfile awardCombatXp(long amount) {
        if (amount <= 0L || classLevel >= MAX_CLASS_LEVEL) return this;
        long xp = safeAdd(combatXp, amount);
        int level = classLevel;
        while (level < MAX_CLASS_LEVEL) {
            long required = combatXpForNextLevel(level);
            if (xp < required) break;
            xp -= required;
            level++;
        }
        return copy(level, xp, unlockedNodes, professionLevel, professionXp, subclass, subclassTrialCompleted);
    }

    public MFCharacterProfile awardProfessionXp(long amount) {
        if (amount <= 0L || professionLevel >= MAX_PROFESSION_LEVEL) return this;
        long xp = safeAdd(professionXp, amount);
        int level = professionLevel;
        while (level < MAX_PROFESSION_LEVEL) {
            long required = professionXpForNextLevel(level);
            if (xp < required) break;
            xp -= required;
            level++;
        }
        return copy(classLevel, combatXp, unlockedNodes, level, xp, subclass, subclassTrialCompleted);
    }

    public MFCharacterProfile unlockNode(String nodeId) {
        MFSkillTreeCatalog.Node node = MFSkillTreeCatalog.node(mainClass, nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node does not belong to " + mainClass));
        if (unlockedNodes.contains(nodeId)) throw new IllegalStateException("Node already unlocked");
        if (talentPointsAvailable() < 1) throw new IllegalStateException("No talent points available");
        if (classLevel < node.minimumClassLevel()) throw new IllegalStateException("Class level " + node.minimumClassLevel() + " required");
        if (!unlockedNodes.containsAll(node.prerequisites())) throw new IllegalStateException("Missing tree prerequisites");
        LinkedHashSet<String> next = new LinkedHashSet<>(unlockedNodes);
        next.add(nodeId);
        return copy(classLevel, combatXp, next, professionLevel, professionXp, subclass, subclassTrialCompleted);
    }

    /** Compatibility overload: caller-supplied prerequisites are ignored; canonical tree rules always win. */
    @Deprecated
    public MFCharacterProfile unlockNode(String nodeId, Set<String> ignoredPrerequisites) {
        return unlockNode(nodeId);
    }

    public MFCharacterProfile completeSubclassTrial() {
        return copy(classLevel, combatXp, unlockedNodes, professionLevel, professionXp, subclass, true);
    }

    public MFCharacterProfile chooseSubclass(String subclassId) {
        if (subclass != null) throw new IllegalStateException("Subclass already chosen; switching is not a normal respec");
        if (classLevel < MIN_SUBCLASS_LEVEL) throw new IllegalStateException("Class level 15 required");
        if (talentPointsSpent() < MIN_SUBCLASS_NODES) throw new IllegalStateException("At least one talent node in the specialization path is required");
        if (!subclassTrialCompleted) throw new IllegalStateException("Subclass trial not completed");
        MFSkillTreeCatalog.Node gate = MFSkillTreeCatalog.subclassGate(mainClass, subclassId)
                .orElseThrow(() -> new IllegalArgumentException("Subclass does not belong to " + mainClass));
        if (!unlockedNodes.contains(gate.id())) throw new IllegalStateException("Unlock the subclass node in its branch first");
        return copy(classLevel, combatXp, unlockedNodes, professionLevel, professionXp, subclassId, true);
    }

    public SolarLunarTraits traitsAtDayTime(long dayTime) {
        long time = Math.floorMod(dayTime, 24_000L);
        boolean cycleDay = time < 12_000L;
        boolean active = affinity == Affinity.SOLAR ? cycleDay : !cycleDay;
        return switch (affinity) {
            case SOLAR -> new SolarLunarTraits(active, active, false, false);
            case LUNAR -> new SolarLunarTraits(false, false, active, active);
        };
    }

    private MFCharacterProfile copy(int nextClassLevel, long nextCombatXp, Set<String> nextNodes,
                                    int nextProfessionLevel, long nextProfessionXp, String nextSubclass,
                                    boolean trialCompleted) {
        return new MFCharacterProfile(playerId, race, affinity, element, mainClass, mainProfession,
                nextSubclass, nextClassLevel, nextCombatXp, nextNodes, nextProfessionLevel,
                nextProfessionXp, trialCompleted);
    }

    private static long safeAdd(long left, long right) {
        try { return Math.addExact(left, right); }
        catch (ArithmeticException ex) { return Long.MAX_VALUE; }
    }

    /** Trait booleans map to the four affinity bonuses; final attribute magnitudes remain balance data. */
    public record SolarLunarTraits(boolean vitality, boolean energy, boolean perception, boolean spirituality) {}
}
