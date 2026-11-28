package net.minecraft.world.level.saveddata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Test-only stand-in. The real mod uses Minecraft's SavedData implementation. */
public abstract class SavedData {
    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider registries);
    public void setDirty() {}
    public static final class Factory<T extends SavedData> {
        public Factory(Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> loader) {}
    }
}
