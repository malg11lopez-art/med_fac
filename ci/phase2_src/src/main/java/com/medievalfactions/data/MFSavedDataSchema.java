package com.medievalfactions.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Shared compatibility gate for MEDIEVAL FACTIONS SavedData schemas.
 * Missing schema_version is legacy schema 0. Future versions are rejected so an
 * older mod cannot silently load and later overwrite data it does not understand.
 */
final class MFSavedDataSchema {
    private static final String VERSION_KEY = "schema_version";

    private MFSavedDataSchema() {}

    static void requireSupported(CompoundTag tag, int currentVersion, String dataId) {
        if (tag == null || currentVersion < 1 || dataId == null || dataId.isBlank()) {
            throw new IllegalArgumentException("Invalid SavedData schema contract");
        }
        if (tag.contains(VERSION_KEY) && !tag.contains(VERSION_KEY, Tag.TAG_INT)) {
            throw new IllegalStateException("Invalid schema_version type for " + dataId);
        }
        int storedVersion = tag.contains(VERSION_KEY, Tag.TAG_INT) ? tag.getInt(VERSION_KEY) : 0;
        if (storedVersion < 0 || storedVersion > currentVersion) {
            throw new IllegalStateException("Unsupported SavedData schema " + storedVersion
                    + " for " + dataId + " (supported: 0.." + currentVersion + ")");
        }
    }
}
