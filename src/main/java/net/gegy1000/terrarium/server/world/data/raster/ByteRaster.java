package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Preconditions;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Optional;

public final class ByteRaster extends AbstractRaster<byte[]> implements IntegerRaster<byte[]> {
    private ByteRaster(byte[] data, int width, int height) {
        super(data, width, height);
    }

    public static ByteRaster create(int width, int height) {
        byte[] array = new byte[width * height];
        return new ByteRaster(array, width, height);
    }

    public static ByteRaster create(DataView view) {
        return create(view.width(), view.height());
    }

    public static ByteRaster wrap(byte[] data, int width, int height) {
        Preconditions.checkArgument(data.length == width * height, "invalid buffer size");
        return new ByteRaster(data, width, height);
    }

    public static Sampler sampler(DataKey<ByteRaster> key) {
        return new Sampler(key);
    }

    public void set(int x, int y, byte value) {
        this.rawData[this.index(x, y)] = value;
    }

    public byte get(int x, int y) {
        return this.rawData[this.index(x, y)];
    }

    public void fill(byte value) {
        Arrays.fill(this.rawData, value);
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
        int rounded = (int) value;
        this.set(x, y, (byte) MathHelper.clamp(rounded, Byte.MIN_VALUE, Byte.MAX_VALUE));
    }

    @Override
    public float getFloat(int x, int y) {
        return this.get(x, y);
    }

    @Override
    public void setInt(int x, int y, int value) {
        this.set(x, y, (byte) MathHelper.clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE));
    }

    @Override
    public int getInt(int x, int y) {
        return this.get(x, y);
    }

    public ByteRaster copy() {
        return new ByteRaster(Arrays.copyOf(this.rawData, this.rawData.length), this.width, this.height);
    }

    public interface Transformer {
        byte apply(byte source, int x, int y);
    }

    public interface Iterator {
        void accept(byte value, int x, int y);
    }

    public static class Sampler implements Raster.Sampler<ByteRaster> {
        private final DataKey<ByteRaster> key;
        private byte defaultValue;

        Sampler(DataKey<ByteRaster> key) {
            this.key = key;
        }

        public Sampler setDefaultValue(byte value) {
            this.defaultValue = value;
            return this;
        }

        public byte sample(ColumnDataCache dataCache, int x, int z) {
            DataSample data = dataCache.joinData(x >> 4, z >> 4);
            return this.sample(data, x & 0xF, z & 0xF);
        }

        public byte sample(DataSample data, int x, int z) {
            Optional<ByteRaster> optional = data.get(this.key);
            if (optional.isPresent()) {
                ByteRaster raster = optional.get();
                return raster.get(x, z);
            }
            return this.defaultValue;
        }

        @Override
        public ByteRaster sample(ColumnDataCache dataCache, DataView view) {
            ByteRaster raster = ByteRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.rawData, this.defaultValue);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
