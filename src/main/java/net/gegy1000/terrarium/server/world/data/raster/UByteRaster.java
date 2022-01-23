package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Preconditions;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Optional;

public final class UByteRaster extends AbstractRaster<byte[]> implements IntegerRaster<byte[]> {
    private UByteRaster(byte[] data, int width, int height) {
        super(data, width, height);
    }

    public static UByteRaster create(int width, int height) {
        byte[] array = new byte[width * height];
        return new UByteRaster(array, width, height);
    }

    public static UByteRaster create(DataView view) {
        return create(view.width(), view.height());
    }

    public static UByteRaster create(DataView view, int value) {
        UByteRaster raster = create(view.width(), view.height());
        Arrays.fill(raster.rawData, (byte) value);
        return raster;
    }

    public static UByteRaster wrap(byte[] data, int width, int height) {
        Preconditions.checkArgument(data.length == width * height, "invalid buffer size");
        return new UByteRaster(data, width, height);
    }

    public static Sampler sampler(DataKey<UByteRaster> key) {
        return new Sampler(key);
    }

    public static UByteRaster copyFrom(IntegerRaster<?> from) {
        UByteRaster raster = UByteRaster.create(from.width(), from.height());
        from.copyInto(raster);
        return raster;
    }

    public void fill(int value) {
        Arrays.fill(this.rawData, (byte) (value & 0xFF));
    }

    public void set(int x, int y, int value) {
        this.rawData[this.index(x, y)] = (byte) (value & 0xFF);
    }

    public int get(int x, int y) {
        return this.rawData[this.index(x, y)] & 0xFF;
    }

    public void transform(Transformer transformer) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int index = this.index(x, y);
                int source = this.rawData[index] & 0xFF;
                this.rawData[index] = (byte) (transformer.apply(source, x, y) & 0xFF);
            }
        }
    }

    public void iterate(Iterator iterator) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int value = this.rawData[this.index(x, y)] & 0xFF;
                iterator.accept(value, x, y);
            }
        }
    }

    @Override
    public void setFloat(int x, int y, float value) {
        int rounded = (int) value;
        this.set(x, y, (byte) MathHelper.clamp(rounded, 0, 255));
    }

    @Override
    public float getFloat(int x, int y) {
        return this.get(x, y);
    }

    @Override
    public void setInt(int x, int y, int value) {
        this.set(x, y, value);
    }

    @Override
    public int getInt(int x, int y) {
        return this.get(x, y);
    }

    public UByteRaster copy() {
        return new UByteRaster(Arrays.copyOf(this.rawData, this.rawData.length), this.width, this.height);
    }

    public interface Transformer {
        int apply(int source, int x, int y);
    }

    public interface Iterator {
        void accept(int value, int x, int y);
    }

    public static class Sampler implements Raster.Sampler<UByteRaster> {
        private final DataKey<UByteRaster> key;
        private int defaultValue;

        Sampler(DataKey<UByteRaster> key) {
            this.key = key;
        }

        public Sampler defaultValue(int value) {
            this.defaultValue = value;
            return this;
        }

        public int sample(ColumnDataCache dataCache, int x, int z) {
            DataSample data = dataCache.joinData(x >> 4, z >> 4);
            return this.sample(data, x & 0xF, z & 0xF);
        }

        public int sample(DataSample data, int x, int z) {
            Optional<UByteRaster> optional = data.get(this.key);
            if (optional.isPresent()) {
                UByteRaster raster = optional.get();
                return raster.get(x, z);
            }
            return this.defaultValue;
        }

        @Override
        public UByteRaster sample(ColumnDataCache dataCache, DataView view) {
            UByteRaster raster = UByteRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.rawData, (byte) (this.defaultValue & 0xF));
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
