package net.gegy1000.earth.server.world.composer.structure.data;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

public final class LossyColumnCache {
    private final long[] table;

    private final int capacity;
    private final int mask;

    public LossyColumnCache(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;

        this.table = new long[this.capacity];
        this.clear();
    }

    private static int hash(long key) {
        return HashCommon.long2int(HashCommon.mix(key));
    }

    public boolean set(int x, int z) {
        long key = ChunkPos.asLong(x, z);

        int idx = hash(key) & this.mask;

        long existing = this.table[idx];
        this.table[idx] = key;

        return existing == key;
    }

    public void clear() {
        Arrays.fill(this.table, Long.MIN_VALUE);
    }
}
