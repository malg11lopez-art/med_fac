package com.medievalfactions.network;

/** Shared bounds for the server-validated player skin catalogue. */
public final class MFPlayerSkinRules {
    public static final int SKIN_COUNT = 86;
    public static final String DATA_KEY = "mf_player_skin_index";
    public static final String CHANGE_TICK_KEY = "mf_player_skin_change_tick";

    private MFPlayerSkinRules() {}

    public static boolean isValidIndex(int index) {
        return index >= 0 && index < SKIN_COUNT;
    }
}
