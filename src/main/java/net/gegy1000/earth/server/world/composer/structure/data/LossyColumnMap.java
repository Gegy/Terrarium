package net.gegy1000.earth.server.world.composer.structure.data;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Arrays;

public final class LossyColumnMap<T> {
    private final long[] keys;
    private final T[] values;

    private final int capacity;
    private final int mask;

    @SuppressWarnings("unchecked")
    public LossyColumnMap(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.keys = new long[this.capacity];
        this.values = (T[]) new Object[this.capacity];
        this.clear();
    }

    private static int hash(long key) {
        return HashCommon.long2int(HashCommon.mix(key));
    }

    public void put(int x, int z, T value) {
        long key = ChunkPos.asLong(x, z);
        int idx = hash(key) & this.mask;

        this.values[idx] = value;
        this.keys[idx] = key;
    }

    @Nullable
    public T get(int x, int z) {
        long key = ChunkPos.asLong(x, z);
        int idx = hash(key) & this.mask;

        if (this.keys[idx] == key) {
            return this.values[idx];
        }

        return null;
    }

    public void clear() {
        Arrays.fill(this.keys, Long.MIN_VALUE);
        Arrays.fill(this.values, null);
    }
}
