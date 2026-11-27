package com.medievalfactions.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

/**
 * Shared compatibility gate for MEDIEVAL FACTIONS SavedData schemas.
 * Missing schema_version is legacy schema 0. Future versions are rejected so an
 * older mod cannot silently load and later overwrite data it does not understand.
 */
final class MFSavedDataSchema {
    private static final String VERSION_KEY = "schema_version";

    private MFSavedDataSchema() {}

    /**
     * Validates independently versioned advanced-record buckets.
     * A missing map or missing bucket entry is legacy bucket schema 0. The next save
     * writes the current version, so old data is upgraded without rewriting its records.
     * Unknown/future markers fail closed to prevent an older mod from overwriting newer data.
     */
    static void requireBucketVersions(CompoundTag tag, String key,
            Map<String, Integer> supportedVersions, String dataId) {
        if (tag == null || key == null || key.isBlank()
                || supportedVersions == null || supportedVersions.isEmpty()
                || dataId == null || dataId.isBlank()) {
            throw new IllegalArgumentException("Invalid per-bucket SavedData schema contract");
        }
        if (tag.contains(key) && !tag.contains(key, Tag.TAG_COMPOUND)) {
            throw new IllegalStateException("Invalid per-bucket schema container for " + dataId);
        }

        CompoundTag storedVersions = tag.contains(key, Tag.TAG_COMPOUND)
                ? tag.getCompound(key) : new CompoundTag();
        for (String storedBucket : storedVersions.getAllKeys()) {
            if (!supportedVersions.containsKey(storedBucket)) {
                throw new IllegalStateException("Unknown SavedData bucket schema " + storedBucket + " for " + dataId);
            }
        }

        for (Map.Entry<String, Integer> supported : supportedVersions.entrySet()) {
            String bucket = supported.getKey();
            int currentVersion = supported.getValue();
            if (bucket == null || bucket.isBlank() || currentVersion < 1) {
                throw new IllegalArgumentException("Invalid supported bucket schema for " + dataId);
            }
            int storedVersion = 0;
            if (storedVersions.contains(bucket)) {
                if (!storedVersions.contains(bucket, Tag.TAG_INT)) {
                    throw new IllegalStateException("Invalid schema version type for " + dataId + "/" + bucket);
                }
                storedVersion = storedVersions.getInt(bucket);
            }
            if (storedVersion < 0 || storedVersion > currentVersion) {
                throw new IllegalStateException("Unsupported SavedData bucket schema " + storedVersion
                        + " for " + dataId + "/" + bucket + " (supported: 0.." + currentVersion + ")");
            }
        }
    }

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
