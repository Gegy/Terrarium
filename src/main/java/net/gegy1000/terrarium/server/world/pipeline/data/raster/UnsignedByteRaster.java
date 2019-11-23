package net.gegy1000.terrarium.server.world.pipeline.data.raster;

import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Optional;

public final class UnsignedByteRaster extends AbstractRaster<byte[]> implements NumberRaster<byte[]> {
    private UnsignedByteRaster(byte[] data, int width, int height) {
        super(data, width, height);
    }

    public static UnsignedByteRaster create(int width, int height) {
        byte[] array = new byte[width * height];
        return new UnsignedByteRaster(array, width, height);
    }

    public static UnsignedByteRaster create(DataView view) {
        return create(view.getWidth(), view.getHeight());
    }

    public static Sampler sampler(DataKey<UnsignedByteRaster> key) {
        return new Sampler(key);
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

    public UnsignedByteRaster copy() {
        return new UnsignedByteRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }

    public interface Transformer {
        int apply(int source, int x, int y);
    }

    public interface Iterator {
        void accept(int value, int x, int y);
    }

    public static class Sampler {
        private final DataKey<UnsignedByteRaster> key;
        private int defaultValue;

        Sampler(DataKey<UnsignedByteRaster> key) {
            this.key = key;
        }

        public Sampler setDefaultValue(int value) {
            this.defaultValue = value;
            return this;
        }

        public int sample(ColumnDataCache dataCache, int x, int z) {
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            Optional<UnsignedByteRaster> optional = dataCache.joinData(columnPos, this.key);
            if (optional.isPresent()) {
                UnsignedByteRaster raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultValue;
        }

        public UnsignedByteRaster sample(ColumnDataCache dataCache, DataView view) {
            UnsignedByteRaster raster = UnsignedByteRaster.create(view);
            if (this.defaultValue != 0) {
                Arrays.fill(raster.data, (byte) (this.defaultValue & 0xF));
            }
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
