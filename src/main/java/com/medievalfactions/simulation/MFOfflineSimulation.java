package com.medievalfactions.simulation;

/** Safe wall-clock catch-up policy for abstract world simulation after server downtime. */
public final class MFOfflineSimulation {
    /** Default maximum catch-up: six real-world hours per server restart. */
    public static final long DEFAULT_MAX_CATCH_UP_SECONDS = 6L * 60L * 60L;

    private MFOfflineSimulation() {}

    /**
     * Returns bounded elapsed seconds. Clock rollback/future timestamps produce zero;
     * long outages are capped so food, wars and prices cannot spiral while offline.
     */
    public static long boundedElapsedSeconds(long lastSaveEpochSeconds, long nowEpochSeconds) {
        return boundedElapsedSeconds(lastSaveEpochSeconds, nowEpochSeconds, DEFAULT_MAX_CATCH_UP_SECONDS);
    }

    public static long boundedElapsedSeconds(long lastSaveEpochSeconds, long nowEpochSeconds,
                                             long maxCatchUpSeconds) {
        if (maxCatchUpSeconds < 0L) throw new IllegalArgumentException("maxCatchUpSeconds must not be negative");
        if (lastSaveEpochSeconds < 0L || nowEpochSeconds < 0L || nowEpochSeconds <= lastSaveEpochSeconds)
            return 0L;
        long elapsed;
        try { elapsed = Math.subtractExact(nowEpochSeconds, lastSaveEpochSeconds); }
        catch (ArithmeticException ex) { elapsed = Long.MAX_VALUE; }
        return Math.min(elapsed, maxCatchUpSeconds);
    }

    /** Converts bounded wall-clock time to a capped number of abstract simulation steps. */
    public static long simulationSteps(long elapsedSeconds, long secondsPerStep, long maxSteps) {
        if (elapsedSeconds < 0L || secondsPerStep < 1L || maxSteps < 0L)
            throw new IllegalArgumentException("Invalid simulation step parameters");
        return Math.min(elapsedSeconds / secondsPerStep, maxSteps);
    }
}
