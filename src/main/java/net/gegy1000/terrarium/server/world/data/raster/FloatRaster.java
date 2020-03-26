package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Optional;

public final class FloatRaster extends AbstractRaster<float[]> implements NumberRaster<float[]> {
    private FloatRaster(float[] data, int width, int height) {
        super(data, width, height);
    }

    public static FloatRaster create(int width, int height) {
        float[] array = new float[width * height];
        return new FloatRaster(array, width, height);
    }

    public static FloatRaster create(DataView view) {
        return create(view.getWidth(), view.getHeight());
    }

    public static FloatRaster create(DataView view, float value) {
        FloatRaster raster = create(view.getWidth(), view.getHeight());
        Arrays.fill(raster.data, value);
        return raster;
    }

    public static Sampler sampler(DataKey<FloatRaster> key) {
        return new Sampler(key);
    }

    public void set(int x, int y, float value) {
        this.data[this.index(x, y)] = value;
    }

    public float get(int x, int y) {
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
        this.set(x, y, (float) value);
    }

    @Override
    public double getDouble(int x, int y) {
        return this.get(x, y);
    }

    public FloatRaster copy() {
        return new FloatRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }

    public interface Transformer {
        float apply(float source, int x, int y);
    }

    public interface Iterator {
        void accept(float value, int x, int y);
    }

    public static class Sampler implements Raster.Sampler<FloatRaster> {
        private final DataKey<FloatRaster> key;
        private float defaultValue;

        Sampler(DataKey<FloatRaster> key) {
            this.key = key;
        }

        public Sampler defaultValue(float value) {
            this.defaultValue = value;
            return this;
        }

        public float sample(ColumnDataCache dataCache, int x, int z) {
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            Optional<FloatRaster> optional = dataCache.joinData(columnPos, this.key);
            if (optional.isPresent()) {
                FloatRaster raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultValue;
        }

        @Override
        public FloatRaster sample(ColumnDataCache dataCache, DataView view) {
            FloatRaster raster = FloatRaster.create(view);
            if (this.defaultValue != 0.0F) {
                Arrays.fill(raster.data, this.defaultValue);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
