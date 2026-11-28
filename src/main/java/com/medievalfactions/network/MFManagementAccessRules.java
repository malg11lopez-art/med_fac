package com.medievalfactions.network;

/** Access and request-budget rules for the server-wide management dashboard. */
public final class MFManagementAccessRules {
    public static final String LAST_REQUEST_TICK_KEY = "mf_management_last_request_tick";
    public static final long REQUEST_COOLDOWN_TICKS = 5L;

    private MFManagementAccessRules() {}

    /** The current dashboard contains world-wide records and is therefore operator-only. */
    public static boolean canViewGlobalDashboard(boolean operator) {
        return operator;
    }

    /** Allows one snapshot every five ticks; a world-time rollback resets the cooldown safely. */
    public static boolean mayRequest(long now, long previous) {
        return now < previous || now - previous >= REQUEST_COOLDOWN_TICKS;
    }
}
