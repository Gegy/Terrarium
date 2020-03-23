package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;
import java.util.Optional;

public final class EnumRaster<T extends Enum<T>> extends AbstractRaster<byte[]> {
    private final Class<T> type;
    private final T[] universe;

    private EnumRaster(byte[] data, int width, int height, Class<T> type) {
        super(data, width, height);
        this.type = type;
        this.universe = type.getEnumConstants();

        if (this.universe.length > 255) {
            throw new IllegalStateException("Enum has too many variants!");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumRaster<T> create(T variant, int width, int height) {
        Class<T> type = (Class<T>) variant.getClass();
        byte[] array = new byte[width * height];
        Arrays.fill(array, (byte) variant.ordinal());
        return new EnumRaster<>(array, width, height, type);
    }

    public static <T extends Enum<T>> EnumRaster<T> createSquare(T variant, int size) {
        return create(variant, size, size);
    }

    public static <T extends Enum<T>> EnumRaster<T> create(T variant, DataView view) {
        return create(variant, view.getWidth(), view.getHeight());
    }

    public static <T extends Enum<T>> Sampler<T> sampler(DataKey<EnumRaster<T>> key, T defaultVariant) {
        return new Sampler<>(key, defaultVariant);
    }

    public void set(int x, int y, T variant) {
        this.data[this.index(x, y)] = (byte) variant.ordinal();
    }

    public T get(int x, int y) {
        return this.universe[this.data[this.index(x, y)] & 0xFF];
    }

    public void transform(Transformer<T> transformer) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int index = this.index(x, y);
                T variant = this.universe[this.data[index] & 0xFF];
                this.data[index] = (byte) transformer.apply(variant, x, y).ordinal();
            }
        }
    }

    public void iterate(Iterator<T> iterator) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                T variant = this.universe[this.data[this.index(x, y)] & 0xFF];
                iterator.accept(variant, x, y);
            }
        }
    }

    public EnumRaster<T> copy() {
        return new EnumRaster<>(Arrays.copyOf(this.data, this.data.length), this.width, this.height, this.type);
    }

    public interface Transformer<T extends Enum<T>> {
        T apply(T source, int x, int y);
    }

    public interface Iterator<T extends Enum<T>> {
        void accept(T value, int x, int y);
    }

    public static class Sampler<T extends Enum<T>> implements Raster.Sampler<EnumRaster<T>> {
        private final DataKey<EnumRaster<T>> key;
        private final T defaultVariant;

        Sampler(DataKey<EnumRaster<T>> key, T defaultVariant) {
            this.key = key;
            this.defaultVariant = defaultVariant;
        }

        public T sample(ColumnDataCache dataCache, int x, int z) {
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            Optional<EnumRaster<T>> optional = dataCache.joinData(columnPos, this.key);
            if (optional.isPresent()) {
                EnumRaster<T> raster = optional.get();
                return raster.get(x & 0xF, z & 0xF);
            }
            return this.defaultVariant;
        }

        @Override
        public EnumRaster<T> sample(ColumnDataCache dataCache, DataView view) {
            EnumRaster<T> raster = EnumRaster.create(this.defaultVariant, view);
            AbstractRaster.sampleInto(raster, dataCache, view, this.key);
            return raster;
        }
    }
}
