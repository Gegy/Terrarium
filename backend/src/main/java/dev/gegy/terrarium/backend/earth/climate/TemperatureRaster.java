package dev.gegy.terrarium.backend.earth.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.raster.RasterBufferCodecs;
import dev.gegy.terrarium.backend.raster.RasterShape;
import dev.gegy.terrarium.backend.raster.RasterType;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;
import dev.gegy.terrarium.backend.util.Util;

import java.util.Arrays;

public class TemperatureRaster extends UnsignedByteRaster {
    private static final float MIN = -40.0f;
    private static final float MAX = 45.0f;

    private static final byte DEFAULT_VALUE = pack(14.0f);
    private static final float[] UNPACK_LOOKUP = new float[256];

    public static final Codec<TemperatureRaster> CODEC = RecordCodecBuilder.create(i -> i.group(
            RasterShape.CODEC.forGetter(TemperatureRaster::shape),
            RasterBufferCodecs.BYTES.fieldOf("data").forGetter(r -> r.buffer)
    ).apply(i, TemperatureRaster::new));
    public static final RasterType<TemperatureRaster> TYPE = RasterType.create(TemperatureRaster::create, CODEC);

    static {
        for (int packed = 0; packed < UNPACK_LOOKUP.length; packed++) {
            UNPACK_LOOKUP[packed] = unpack(packed);
        }
    }

    protected TemperatureRaster(final RasterShape shape, final byte[] buffer) {
        super(shape, buffer);
    }

    public static TemperatureRaster create(final RasterShape shape) {
        final byte[] buffer = new byte[shape.size()];
        Arrays.fill(buffer, DEFAULT_VALUE);
        return new TemperatureRaster(shape, buffer);
    }

    public static TemperatureRaster wrap(final RasterShape shape, final byte[] buffer) {
        return new TemperatureRaster(shape, buffer);
    }

    public float getTemperature(final int x, final int y) {
        return UNPACK_LOOKUP[getByte(x, y)];
    }

    private static byte pack(final float value) {
        return (byte) Math.round(Util.inverseLerp(MIN, MAX, value) * 255.0f);
    }

    private static float unpack(final int packed) {
        return Util.lerp(MIN, MAX, packed / 255.0f);
    }

    @Override
    public RasterType<TemperatureRaster> type() {
        return TYPE;
    }
}
