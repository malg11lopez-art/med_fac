package com.medievalfactions.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import static com.medievalfactions.domain.MFGameCatalog.Treaty;

/** Symmetric, persistent faction relation. War is a distinct state, not a relation-score band. */
public record MFDiplomacyRelation(UUID factionA, UUID factionB, int score, boolean atWar, Set<Treaty> treaties) {
    public MFDiplomacyRelation {
        if (factionA == null || factionB == null || factionA.equals(factionB)) throw new IllegalArgumentException("Two different factions are required");
        if (score < -100 || score > 100) throw new IllegalArgumentException("Relation score must be -100..100");
        LinkedHashSet<Treaty> copy = new LinkedHashSet<>();
        if (treaties != null) copy.addAll(treaties);
        if (atWar && (copy.contains(Treaty.ALLIANCE) || copy.contains(Treaty.NON_AGGRESSION) || copy.contains(Treaty.PEACE)))
            throw new IllegalArgumentException("War cannot coexist with peace/alliance/non-aggression treaties");
        treaties = Collections.unmodifiableSet(copy);
    }

    public String band() {
        if (atWar) return "WAR";
        if (score <= -61) return "HOSTILE";
        if (score <= -21) return "RIVAL";
        if (score <= 20) return "NEUTRAL";
        if (score <= 60) return "FRIENDLY";
        return "ALLY";
    }

    public MFDiplomacyRelation adjust(int delta) {
        int next = Math.max(-100, Math.min(100, score + delta));
        return new MFDiplomacyRelation(factionA, factionB, next, atWar, treaties);
    }

    public MFDiplomacyRelation declareWar() {
        LinkedHashSet<Treaty> next = new LinkedHashSet<>(treaties);
        next.remove(Treaty.ALLIANCE);
        next.remove(Treaty.NON_AGGRESSION);
        next.remove(Treaty.PEACE);
        return new MFDiplomacyRelation(factionA, factionB, score, true, next);
    }

    public MFDiplomacyRelation makePeace(int postWarScore) {
        int bounded = Math.max(-100, Math.min(100, postWarScore));
        LinkedHashSet<Treaty> next = new LinkedHashSet<>(treaties);
        next.add(Treaty.PEACE);
        return new MFDiplomacyRelation(factionA, factionB, bounded, false, next);
    }

    public MFDiplomacyRelation addTreaty(Treaty treaty) {
        if (atWar && (treaty == Treaty.ALLIANCE || treaty == Treaty.NON_AGGRESSION || treaty == Treaty.PEACE))
            throw new IllegalStateException("A peace agreement is required before this treaty");
        LinkedHashSet<Treaty> next = new LinkedHashSet<>(treaties);
        next.add(treaty);
        return new MFDiplomacyRelation(factionA, factionB, score, atWar, next);
    }
}
