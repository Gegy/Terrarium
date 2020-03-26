package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Preconditions;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;
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
        return create(view.getWidth(), view.getHeight());
    }

    public static UByteRaster create(DataView view, int value) {
        UByteRaster raster = create(view.getWidth(), view.getHeight());
        Arrays.fill(raster.data, (byte) value);
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
        UByteRaster raster = UByteRaster.create(from.getWidth(), from.getHeight());
        from.copyInto(raster);
        return raster;
    }

    public void fill(int value) {
        Arrays.fill(this.data, (byte) (value & 0xFF));
    }

    public void set(int x, int y, int value) {
        this.data[this.index(x, y)] = (byte) (value & 0xFF);
    }

    public int get(int x, int y) {
        return this.data[this.index(x, y)] & 0xFF;
    }

    public void transform(Transformer transformer) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int index = this.index(x, y);
                int source = this.data[index] & 0xFF;
                this.data[index] = (byte) (transformer.apply(source, x, y) & 0xFF);
            }
        }
    }

    public void iterate(Iterator iterator) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int value = this.data[this.index(x, y)] & 0xFF;
                iterator.accept(value, x, y);
            }
        }
    }

    @Override
    public void setDouble(int x, int y, double value) {
        int rounded = (int) value;
        this.set(x, y, (byte) MathHelper.clamp(rounded, 0, 255));
    }

    @Override
    public double getDouble(int x, int y) {
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
        return new UByteRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
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
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            Optional<UByteRaster> optional = dataCache.joinData(columnPos, this.key);
            if (optional.isPresent()) {
                UByteRaster raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultValue;
        }

        @Override
        public UByteRaster sample(ColumnDataCache dataCache, DataView view) {
            UByteRaster raster = UByteRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.data, (byte) (this.defaultValue & 0xF));
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
