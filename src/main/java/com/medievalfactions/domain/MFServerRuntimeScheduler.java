package com.medievalfactions.domain;

/**
 * Per-server tick cadence. Keep one instance per MinecraftServer so integrated-server
 * restarts and simultaneous server instances cannot share schedule state.
 */
public final class MFServerRuntimeScheduler {
    private long tickCount;
    private int lastMinute = -1;

    /** Advances this server's logical tick counter and returns the new value. */
    public long advanceTick() {
        tickCount = tickCount == Long.MAX_VALUE ? 1L : tickCount + 1L;
        return tickCount;
    }

    /** True once for the first observed minute and then once whenever the minute changes. */
    public boolean observeMinute(int minuteOfDay) {
        if (minuteOfDay < 0 || minuteOfDay >= 1_440)
            throw new IllegalArgumentException("minuteOfDay must be in [0, 1439]");
        if (minuteOfDay == lastMinute) return false;
        lastMinute = minuteOfDay;
        return true;
    }

    /** Checks whether the current tick is an exact interval boundary. */
    public boolean dueEvery(long interval) {
        if (interval < 1L) throw new IllegalArgumentException("interval must be positive");
        return tickCount > 0L && tickCount % interval == 0L;
    }

    public long tickCount() { return tickCount; }
    public int lastMinute() { return lastMinute; }
}
