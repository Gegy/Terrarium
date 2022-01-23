package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;

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
        return create(view.width(), view.height());
    }

    public static FloatRaster create(DataView view, float value) {
        FloatRaster raster = create(view.width(), view.height());
        Arrays.fill(raster.rawData, value);
        return raster;
    }

    public static Sampler sampler(DataKey<FloatRaster> key) {
        return new Sampler(key);
    }

    public void set(int x, int y, float value) {
        this.rawData[this.index(x, y)] = value;
    }

    public float get(int x, int y) {
        return this.rawData[this.index(x, y)];
    }

    public void transform(Transformer transformer) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int index = this.index(x, y);
                this.rawData[index] = transformer.apply(this.rawData[index], x, y);
            }
        }
    }

    public void iterate(Iterator iterator) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                iterator.accept(this.rawData[this.index(x, y)], x, y);
            }
        }
    }

    @Override
    public void setFloat(int x, int y, float value) {
        this.set(x, y, value);
    }

    @Override
    public float getFloat(int x, int y) {
        return this.get(x, y);
    }

    public FloatRaster copy() {
        return new FloatRaster(Arrays.copyOf(this.rawData, this.rawData.length), this.width, this.height);
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
            DataSample data = dataCache.joinData(x >> 4, z >> 4);
            return this.sample(data, x & 0xF, z & 0xF);
        }

        public float sample(DataSample data, int x, int z) {
            Optional<FloatRaster> optional = data.get(this.key);
            if (optional.isPresent()) {
                FloatRaster raster = optional.get();
                return raster.get(x, z);
            }
            return this.defaultValue;
        }

        @Override
        public FloatRaster sample(ColumnDataCache dataCache, DataView view) {
            FloatRaster raster = FloatRaster.create(view);
            if (this.defaultValue != 0.0F) {
                Arrays.fill(raster.rawData, this.defaultValue);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
