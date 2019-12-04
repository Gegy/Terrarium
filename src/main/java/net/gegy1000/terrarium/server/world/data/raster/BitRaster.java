package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Optional;

public final class BitRaster extends AbstractRaster<char[]> {
    private static final int WORD_SIZE_BYTES = 2;
    private static final int WORD_SIZE_BITS = WORD_SIZE_BYTES * 8;
    private static final int BIT_MASK = 15;

    private BitRaster(char[] data, int width, int height) {
        super(data, width, height);
    }

    private static int wordCount(int bits) {
        return (bits + WORD_SIZE_BITS - 1) / WORD_SIZE_BITS;
    }

    public static BitRaster create(int width, int height) {
        int bits = width * height;
        char[] array = new char[wordCount(bits)];
        return new BitRaster(array, width, height);
    }

    public static BitRaster create(DataView view) {
        return create(view.getWidth(), view.getHeight());
    }

    public static Sampler sampler(DataKey<BitRaster> key) {
        return new Sampler(key);
    }

    public void set(int x, int y, boolean value) {
        if (value) this.put(x, y);
        else this.remove(x, y);
    }

    public void put(int x, int y) {
        int index = this.index(x, y);
        int bitIndex = index & BIT_MASK;
        this.data[wordIndex(index)] |= (1 << bitIndex);
    }

    public void remove(int x, int y) {
        int index = this.index(x, y);
        int bitIndex = index & BIT_MASK;
        this.data[wordIndex(index)] &= ~(1 << bitIndex);
    }

    public boolean get(int x, int y) {
        int index = this.index(x, y);
        int bitIndex = index & BIT_MASK;
        char word = this.data[wordIndex(index)];
        return (word >> bitIndex & 1) != 0;
    }

    public void transform(Transformer transformer) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                boolean source = this.get(x, y);
                this.set(x, y, transformer.apply(source, x, y));
            }
        }
    }

    public void iterate(Iterator iterator) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                boolean value = this.get(x, y);
                iterator.accept(value, x, y);
            }
        }
    }

    public BitRaster copy() {
        return new BitRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }

    public interface Transformer {
        boolean apply(boolean source, int x, int y);
    }

    public interface Iterator {
        void accept(boolean value, int x, int y);
    }

    private static int wordIndex(int index) {
        return index / WORD_SIZE_BITS;
    }

    public static class Sampler {
        private final DataKey<BitRaster> key;
        private boolean defaultValue;

        Sampler(DataKey<BitRaster> key) {
            this.key = key;
        }

        public Sampler setDefaultValue(boolean value) {
            this.defaultValue = value;
            return this;
        }

        public boolean sample(ColumnDataCache dataCache, int x, int z) {
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            Optional<BitRaster> optional = dataCache.joinData(columnPos, this.key);
            if (optional.isPresent()) {
                BitRaster raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultValue;
        }

        public BitRaster sample(ColumnDataCache dataCache, DataView view) {
            BitRaster raster = BitRaster.create(view);
            if (this.defaultValue) {
                Arrays.fill(raster.data, Character.MAX_VALUE);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
