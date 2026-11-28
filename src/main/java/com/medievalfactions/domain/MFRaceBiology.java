package com.medievalfactions.domain;

import java.util.EnumMap;
import java.util.Map;

import static com.medievalfactions.domain.MFGameCatalog.AgeStage;
import static com.medievalfactions.domain.MFGameCatalog.RaceFamily;

/** Tunable race-specific life-cycle timings expressed in custom world days (378 days per year). */
public final class MFRaceBiology {
    public static final int DAYS_PER_YEAR = 378;
    public record Profile(int adultYears, int elderYears, int lifeExpectancyYears, int maximumAgeYears) {
        public Profile {
            if (adultYears < 8 || elderYears <= adultYears || lifeExpectancyYears < elderYears
                    || maximumAgeYears < lifeExpectancyYears) throw new IllegalArgumentException("Invalid race biology");
        }
        public long adultAgeDays() { return (long) adultYears * DAYS_PER_YEAR; }
        public long elderAgeDays() { return (long) elderYears * DAYS_PER_YEAR; }
        public long lifeExpectancyDays() { return (long) lifeExpectancyYears * DAYS_PER_YEAR; }
        public long maximumAgeDays() { return (long) maximumAgeYears * DAYS_PER_YEAR; }
    }

    private static final Map<RaceFamily, Profile> PROFILES = new EnumMap<>(RaceFamily.class);
    static {
        put(RaceFamily.HUMANS, 18, 60, 80, 100);
        put(RaceFamily.ELVES, 50, 350, 550, 700);
        put(RaceFamily.DWARVES, 40, 170, 220, 270);
        put(RaceFamily.ORCS, 14, 45, 60, 80);
        put(RaceFamily.BEASTMEN, 16, 55, 75, 95);
        put(RaceFamily.GOBLINS, 12, 35, 50, 65);
        put(RaceFamily.DRACONIDS, 35, 250, 500, 700);
        put(RaceFamily.VAMPIRES, 30, 400, 900, 1_200);
        put(RaceFamily.UNDEAD, 15, 120, 300, 500);
        put(RaceFamily.FAE, 25, 200, 500, 700);
        put(RaceFamily.CELESTIALS, 100, 800, 2_000, 3_000);
        put(RaceFamily.DEMONS, 25, 200, 500, 800);
        put(RaceFamily.MERFOLK, 18, 80, 120, 160);
        put(RaceFamily.CENTAURS, 18, 60, 90, 120);
        put(RaceFamily.GIANTS, 70, 300, 500, 700);
        put(RaceFamily.HARPIES, 15, 45, 75, 95);
    }
    private MFRaceBiology() {}
    private static void put(RaceFamily race, int adult, int elder, int expectancy, int max) {
        PROFILES.put(race, new Profile(adult, elder, expectancy, max));
    }
    public static Profile profile(RaceFamily race) { return PROFILES.get(race); }
    public static long adultAgeDays(RaceFamily race) { return profile(race).adultAgeDays(); }

    public static AgeStage stageAt(RaceFamily race, long ageDays) {
        Profile p = profile(race);
        long age = Math.max(0L, ageDays);
        long babyLimit = 2L * DAYS_PER_YEAR;
        long childLimit = Math.max(babyLimit, p.adultAgeDays() * 2L / 3L);
        if (age < babyLimit) return AgeStage.BABY;
        if (age < childLimit) return AgeStage.CHILD;
        if (age < p.adultAgeDays()) return AgeStage.ADOLESCENT;
        if (age < p.elderAgeDays()) return AgeStage.ADULT;
        return AgeStage.ELDER;
    }
}
