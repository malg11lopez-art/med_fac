package net.minecraft.nbt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Test-only in-memory stand-in for the subset of Minecraft NBT used by SavedData. */
public final class CompoundTag implements Tag {
    private final Map<String, Object> values = new HashMap<>();
    public void putUUID(String key, UUID value) { values.put(key, value); }
    public UUID getUUID(String key) { return (UUID) values.get(key); }
    public boolean hasUUID(String key) { return values.get(key) instanceof UUID; }
    public void putString(String key, String value) { values.put(key, value); }
    public String getString(String key) { Object value = values.get(key); return value instanceof String s ? s : ""; }
    public void putLong(String key, long value) { values.put(key, value); }
    public long getLong(String key) { Object value = values.get(key); return value instanceof Number n ? n.longValue() : 0L; }
    public void putInt(String key, int value) { values.put(key, value); }
    public int getInt(String key) { Object value = values.get(key); return value instanceof Number n ? n.intValue() : 0; }
    public void putBoolean(String key, boolean value) { values.put(key, value); }
    public boolean getBoolean(String key) { Object value = values.get(key); return value instanceof Boolean b && b; }
    public void put(String key, Tag value) { values.put(key, value); }
    public ListTag getList(String key, int ignoredType) { Object value = values.get(key); return value instanceof ListTag list ? list : new ListTag(); }
    public boolean contains(String key) { return values.containsKey(key); }
    public boolean contains(String key, int type) {
        Object value = values.get(key);
        return switch (type) {
            case Tag.TAG_INT -> value instanceof Integer;
            case Tag.TAG_LONG -> value instanceof Long;
            case Tag.TAG_STRING -> value instanceof String;
            case Tag.TAG_LIST -> value instanceof ListTag;
            case Tag.TAG_COMPOUND -> value instanceof CompoundTag;
            default -> values.containsKey(key);
        };
    }
}
