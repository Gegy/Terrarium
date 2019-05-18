package net.gegy1000.terrarium.server.world.pipeline.data.raster;

import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Optional;

public final class ShortRaster extends AbstractRaster<short[]> implements NumberRaster<short[]> {
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
    public void setDouble(int x, int y, double value) {
        this.set(x, y, (short) value);
    }

    @Override
    public double getDouble(int x, int y) {
        return this.get(x, y);
    }

    @Override
    public ShortRaster copy() {
        return new ShortRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }

    public interface Transformer {
        short apply(short source, int x, int y);
    }

    public interface Iterator {
        void accept(short value, int x, int y);
    }

    public static class Sampler {
        private final DataKey<ShortRaster> key;
        private short defaultValue;

        Sampler(DataKey<ShortRaster> key) {
            this.key = key;
        }

        public Sampler setDefaultValue(short value) {
            this.defaultValue = value;
            return this;
        }

        public short sample(ColumnDataCache dataCache, int x, int z) {
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            Optional<ShortRaster> optional = dataCache.joinData(columnPos, this.key);
            if (optional.isPresent()) {
                ShortRaster raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultValue;
        }

        public ShortRaster sample(ColumnDataCache dataCache, DataView view) {
            ShortRaster raster = ShortRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.data, this.defaultValue);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
