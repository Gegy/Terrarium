package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Preconditions;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;
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
        return create(view.getWidth(), view.getHeight());
    }

    public static ByteRaster wrap(byte[] data, int width, int height) {
        Preconditions.checkArgument(data.length == width * height, "invalid buffer size");
        return new ByteRaster(data, width, height);
    }

    public static Sampler sampler(DataKey<ByteRaster> key) {
        return new Sampler(key);
    }

    public void set(int x, int y, byte value) {
        this.data[this.index(x, y)] = value;
    }

    public byte get(int x, int y) {
        return this.data[this.index(x, y)];
    }

    public void fill(byte value) {
        Arrays.fill(this.data, value);
    }

    public int getUnsigned(int x, int y) {
        return this.get(x, y) & 0xFF;
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
        int rounded = (int) value;
        this.set(x, y, (byte) MathHelper.clamp(rounded, Byte.MIN_VALUE, Byte.MAX_VALUE));
    }

    @Override
    public double getDouble(int x, int y) {
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
        return new ByteRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
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
            ColumnData data = dataCache.joinData(new ChunkPos(x >> 4, z >> 4));
            return this.sample(data, x, z);
        }

        public byte sample(ColumnData data, int x, int z) {
            Optional<ByteRaster> optional = data.get(this.key);
            if (optional.isPresent()) {
                ByteRaster raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultValue;
        }

        @Override
        public ByteRaster sample(ColumnDataCache dataCache, DataView view) {
            ByteRaster raster = ByteRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.data, this.defaultValue);
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
