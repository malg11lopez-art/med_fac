package com.medievalfactions.domain;

/** Anti-farming combat XP policy: diminishing rewards per creature type and a finite daily cap. */
public final class MFCombatRewardRules {
    public static final int DAILY_XP_CAP = 1_000;
    private MFCombatRewardRules() {}

    public static int calculateAward(int vanillaExperience, int previousSameTypeKills, long earnedToday) {
        if (vanillaExperience <= 0 || previousSameTypeKills < 0 || earnedToday >= DAILY_XP_CAP) return 0;
        long base = Math.min(250L, (long) vanillaExperience * 20L);
        long diminished = switch (previousSameTypeKills) {
            case 0 -> base;
            case 1 -> base / 2L;
            case 2 -> base / 4L;
            default -> 0L;
        };
        long remaining = DAILY_XP_CAP - Math.max(0L, earnedToday);
        return (int) Math.max(0L, Math.min(diminished, remaining));
    }
}
