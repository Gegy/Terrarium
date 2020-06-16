package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.HashCommon;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;

import java.util.Arrays;
import java.util.Optional;

public final class ShortRaster extends AbstractRaster<short[]> implements IntegerRaster<short[]> {
    private ShortRaster(short[] data, int width, int height) {
        super(data, width, height);
    }

    public static ShortRaster create(int width, int height) {
        short[] array = new short[width * height];
        return new ShortRaster(array, width, height);
    }

    public static ShortRaster createSquare(int size) {
        return create(size, size);
    }

    public static ShortRaster create(DataView view) {
        return create(view.getWidth(), view.getHeight());
    }

    public static ShortRaster create(DataView view, int value) {
        ShortRaster raster = create(view.getWidth(), view.getHeight());
        Arrays.fill(raster.data, (short) value);
        return raster;
    }

    public static ShortRaster wrap(short[] data, int width, int height) {
        Preconditions.checkArgument(data.length == width * height, "invalid buffer size");
        return new ShortRaster(data, width, height);
    }

    public static Sampler sampler(DataKey<ShortRaster> key) {
        return new Sampler(key);
    }

    public void set(int x, int y, short value) {
        this.data[this.index(x, y)] = value;
    }

    public short get(int x, int y) {
        return this.data[this.index(x, y)];
    }

    public void transform(Transformer transformer) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int index = this.index(x, y);
                this.data[index] = transformer.apply(this.data[index], x, y);
            }
        }
    }

    public void iterate(Iterator iterator) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                iterator.accept(this.data[this.index(x, y)], x, y);
            }
        }
    }

    @Override
    public void setFloat(int x, int y, float value) {
        this.set(x, y, (short) value);
    }

    @Override
    public float getFloat(int x, int y) {
        return this.get(x, y);
    }

    @Override
    public void setInt(int x, int y, int value) {
        this.set(x, y, (short) value);
    }

    @Override
    public int getInt(int x, int y) {
        return this.get(x, y);
    }

    public ShortRaster copy() {
        return new ShortRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }

    public interface Transformer {
        short apply(short source, int x, int y);
    }

    public interface Iterator {
        void accept(short value, int x, int y);
    }

    public static class Sampler implements Raster.Sampler<ShortRaster> {
        private final DataKey<ShortRaster> key;
        private short defaultValue;

        private SampleCache cache;

        Sampler(DataKey<ShortRaster> key) {
            this.key = key;
        }

        public Sampler defaultValue(int value) {
            this.defaultValue = (short) value;
            return this;
        }

        public Sampler cached() {
            this.cache = new SampleCache();
            return this;
        }

        public short sample(ColumnDataCache dataCache, int x, int z) {
            if (this.cache == null) {
                ColumnData data = dataCache.joinData(x >> 4, z >> 4);
                return this.sample(data, x & 0xF, z & 0xF);
            }

            short cached = this.cache.get(x, z);
            if (cached != SampleCache.NULL) {
                return cached;
            }

            ColumnData data = dataCache.joinData(x >> 4, z >> 4);
            short sample = this.sample(data, x & 0xF, z & 0xF);
            this.cache.set(x, z, sample);

            return sample;
        }

        public short sample(ColumnData data, int x, int z) {
            Optional<ShortRaster> optional = data.get(this.key);
            if (optional.isPresent()) {
                ShortRaster raster = optional.get();
                return raster.get(x, z);
            }
            return this.defaultValue;
        }

        @Override
        public ShortRaster sample(ColumnDataCache dataCache, DataView view) {
            ShortRaster raster = ShortRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.data, this.defaultValue);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }

    static class SampleCache {
        private static final int CAPACITY = 64;
        private static final int MASK = CAPACITY - 1;

        static final short NULL = Short.MIN_VALUE;

        private final long[] keys;
        private final short[] values;

        SampleCache() {
            this.keys = new long[CAPACITY];
            Arrays.fill(this.keys, Long.MIN_VALUE);
            this.values = new short[CAPACITY];
        }

        private static long key(int x, int z) {
            return (long) x << 32 | (long) z;
        }

        private static int hash(long key) {
            return HashCommon.long2int(HashCommon.mix(key));
        }

        void set(int x, int z, short value) {
            long key = key(x, z);
            int idx = hash(key) & MASK;

            this.values[idx] = value;
            this.keys[idx] = key;
        }

        short get(int x, int z) {
            long key = key(x, z);
            int idx = hash(key) & MASK;

            if (this.keys[idx] == key) {
                return this.values[idx];
            }

            return NULL;
        }
    }
}
