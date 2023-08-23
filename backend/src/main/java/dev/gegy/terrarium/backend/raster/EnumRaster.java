package dev.gegy.terrarium.backend.raster;

import java.util.Arrays;

public class EnumRaster<T extends Enum<T>> implements Raster {
    private final EnumRasterType<T> type;
    private final RasterShape shape;
    private final byte[] buffer;

    private EnumRaster(final EnumRasterType<T> type, final RasterShape shape, final byte[] buffer) {
        this.type = type;
        this.shape = shape;
        this.buffer = buffer;
    }

    public static <T extends Enum<T>> RasterType<EnumRaster<T>> type(final T defaultValue) {
        return new EnumRasterType<>(defaultValue);
    }

    public void put(final int x, final int y, final T value) {
        buffer[shape.index(x, y)] = (byte) (value.ordinal() & 0xff);
    }

    public T get(final int x, final int y) {
        return type.variants[buffer[shape.index(x, y)] & 0xff];
    }

    private void checkSameType(final EnumRaster<T> raster) {
        if (!type.equals(raster.type)) {
            throw new IllegalArgumentException("Enum rasters have different types: got " + raster.type + ", but expected" + type);
        }
    }

    public void copyFrom(final EnumRaster<T> raster) {
        Raster.checkSameShape(this, raster);
        checkSameType(raster);
        System.arraycopy(raster.buffer, 0, buffer, 0, buffer.length);
    }

    public void copyFromClipped(final EnumRaster<T> raster, final int x0, final int y0) {
        if (x0 == 0 && y0 == 0 && shape().equals(raster.shape())) {
            copyFrom(raster);
            return;
        }

        checkSameType(raster);

        final int x1 = Math.min(x0 + raster.width(), width());
        final int y1 = Math.min(y0 + raster.height(), height());

        for (int y = Math.max(y0, 0); y < y1; y++) {
            for (int x = Math.max(x0, 0); x < x1; x++) {
                buffer[shape.index(x, y)] = raster.buffer[raster.shape.index(x - x0, y - y0)];
            }
        }
    }

    @Override
    public RasterType<EnumRaster<T>> type() {
        return type;
    }

    @Override
    public RasterShape shape() {
        return shape;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void copyFrom(final Raster raster) {
        if (raster instanceof final EnumRaster<?> enumRaster && type.equals(enumRaster.type)) {
            copyFrom((EnumRaster<T>) enumRaster);
        } else {
            throw new IllegalArgumentException("Cannot copy from " + raster + " into EnumRaster of type " + type);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void copyFromClipped(final Raster raster, final int x0, final int y0) {
        if (raster instanceof final EnumRaster<?> enumRaster && type.equals(enumRaster.type)) {
            copyFromClipped((EnumRaster<T>) enumRaster, x0, y0);
        } else {
            throw new IllegalArgumentException("Cannot copy from " + raster + " into EnumRaster of type " + type);
        }
    }

    private static final class EnumRasterType<T extends Enum<T>> implements RasterType<EnumRaster<T>> {
        private final T defaultValue;
        private final T[] variants;

        public EnumRasterType(final T defaultValue) {
            this.defaultValue = defaultValue;
            final Class<T> enumClass = defaultValue.getDeclaringClass();
            variants = enumClass.getEnumConstants();
            if (variants.length > 255) {
                throw new IllegalArgumentException("Cannot construct EnumRaster for " + enumClass + " with " + variants.length + " variants");
            }
        }

        @Override
        public EnumRaster<T> create(final RasterShape shape) {
            final byte[] buffer = new byte[shape.size()];
            if (defaultValue.ordinal() != 0) {
                Arrays.fill(buffer, (byte) defaultValue.ordinal());
            }
            return new EnumRaster<>(this, shape, buffer);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof final EnumRasterType<?> type) {
                return defaultValue.equals(type.defaultValue);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return defaultValue.hashCode();
        }

        @Override
        public String toString() {
            return "EnumRasterType" + Arrays.toString(variants);
        }
    }
}
