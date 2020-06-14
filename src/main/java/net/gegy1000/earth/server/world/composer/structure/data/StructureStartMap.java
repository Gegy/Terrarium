package net.gegy1000.earth.server.world.composer.structure.data;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

// Implementation assumes entries will never be removed
public final class StructureStartMap implements Iterable<StructureStart> {
    private static final int MIX = 662976773;

    private final float loadFactor;

    private StructureStart[] table;
    private int capacity;
    private int mask;

    private int size;
    private int growThreshold;

    public StructureStartMap(int capacity, float loadFactor) {
        this.loadFactor = loadFactor;
        this.setupTable(capacity);
    }

    private void setupTable(int capacity) {
        this.capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = this.capacity - 1;
        this.growThreshold = MathHelper.floor(this.capacity * this.loadFactor) - 1;

        StructureStart[] previous = this.table;
        this.table = new StructureStart[this.capacity];

        if (previous != null) {
            this.copyEntriesFrom(previous);
        }
    }

    private void copyEntriesFrom(StructureStart[] table) {
        for (StructureStart entry : table) {
            if (entry != null) this.put(entry);
        }
    }

    public void clear() {
        Arrays.fill(this.table, null);
        this.size = 0;
    }

    @Nullable
    public StructureStart put(StructureStart start) {
        int x = start.getChunkPosX();
        int z = start.getChunkPosZ();
        int index = this.index(x, z);

        // assumes entries will never be removed and that there is always a free entry slot
        StructureStart entry;
        while ((entry = this.table[index]) != null) {
            if (entry.getChunkPosX() == x && entry.getChunkPosZ() == z) {
                this.table[index] = start;
                return entry;
            }
            index = (index + 1) & this.mask;
        }

        // we found an empty slot
        this.table[index] = start;

        if (++this.size > this.growThreshold) {
            this.setupTable(this.capacity * 2);
        }

        return null;
    }

    @Nullable
    public StructureStart get(int x, int z) {
        int index = this.index(x, z);

        // assumes entries will never be removed
        StructureStart entry;
        while ((entry = this.table[index]) != null) {
            if (entry.getChunkPosX() == x && entry.getChunkPosZ() == z) {
                return entry;
            }
            index = (index + 1) & this.mask;
        }

        return null;
    }

    public boolean contains(int x, int z) {
        return this.get(x, z) != null;
    }

    private int index(int x, int z) {
        return hash(x, z) & this.mask;
    }

    private static int hash(int x, int z) {
        int hash = MIX;
        hash += x;
        hash *= MIX;
        hash += z;
        hash *= MIX;
        return hash;
    }

    @Override
    public Iterator<StructureStart> iterator() {
        return new AbstractIterator<StructureStart>() {
            private int index;

            @Override
            protected StructureStart computeNext() {
                while (this.index < StructureStartMap.this.table.length) {
                    StructureStart entry = StructureStartMap.this.table[this.index++];
                    if (entry != null) {
                        return entry;
                    }
                }
                return this.endOfData();
            }
        };
    }
}
