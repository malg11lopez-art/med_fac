package net.minecraft.nbt;
import java.util.ArrayList;
public final class ListTag extends ArrayList<CompoundTag> implements Tag {
    private static final long serialVersionUID = 1L;
    public CompoundTag getCompound(int index) { return get(index); }
}
