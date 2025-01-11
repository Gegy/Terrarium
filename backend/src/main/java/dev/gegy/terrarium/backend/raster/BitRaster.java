package dev.gegy.terrarium.backend.raster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.BitSet;

public class BitRaster implements Raster {
    public static final Codec<BitRaster> CODEC = RecordCodecBuilder.create(i -> i.group(
            RasterShape.CODEC.forGetter(BitRaster::shape),
            RasterBufferCodecs.BIT_SET.fieldOf("data").forGetter(r -> r.buffer)
    ).apply(i, BitRaster::new));
    public static final RasterType<BitRaster> TYPE = RasterType.create(BitRaster::create, CODEC);

    protected final RasterShape shape;
    protected final BitSet buffer;

    protected BitRaster(final RasterShape shape, final BitSet buffer) {
        this.shape = shape;
        this.buffer = buffer;
    }

    public static BitRaster create(final RasterShape shape) {
        final BitSet buffer = new BitSet(shape.size());
        return new BitRaster(shape, buffer);
    }

    public void putBoolean(final int x, final int y, final boolean value) {
        buffer.set(shape.index(x, y), value);
    }

    public boolean getBoolean(final int x, final int y) {
        return buffer.get(shape.index(x, y));
    }

    public void copyFrom(final BitRaster raster) {
        Raster.checkSameShape(this, raster);
        buffer.clear();
        buffer.or(raster.buffer);
    }

    @Override
    public RasterType<BitRaster> type() {
        return TYPE;
    }

    @Override
    public RasterShape shape() {
        return shape;
    }

    @Override
    public void copyFrom(final Raster raster) {
        if (raster instanceof final BitRaster bitRaster) {
            copyFrom(bitRaster);
        } else {
            throw new IllegalArgumentException("Cannot copy from " + raster + " into BitRaster");
        }
    }

    @Override
    public void copyFromClipped(final Raster raster, final int x0, final int y0) {
        if (!(raster instanceof final BitRaster bitRaster)) {
            throw new IllegalArgumentException("Cannot copy from " + raster + " into BitRaster");
        }

        if (x0 == 0 && y0 == 0 && shape().equals(raster.shape())) {
            copyFrom(raster);
            return;
        }

        final int x1 = Math.min(x0 + raster.width(), width());
        final int y1 = Math.min(y0 + raster.height(), height());

        for (int y = Math.max(y0, 0); y < y1; y++) {
            for (int x = Math.max(x0, 0); x < x1; x++) {
                putBoolean(x, y, bitRaster.getBoolean(x - x0, y - y0));
            }
        }
    }
}
