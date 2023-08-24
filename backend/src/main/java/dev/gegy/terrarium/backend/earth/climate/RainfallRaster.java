package dev.gegy.terrarium.backend.earth.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.raster.RasterBufferCodecs;
import dev.gegy.terrarium.backend.raster.RasterShape;
import dev.gegy.terrarium.backend.raster.RasterType;
import dev.gegy.terrarium.backend.raster.UnsignedByteRaster;
import dev.gegy.terrarium.backend.util.Util;

import java.util.Arrays;

public class RainfallRaster extends UnsignedByteRaster {
    private static final float MIN = 0.0f;
    private static final float MAX = 7200.0f;
    private static final float CURVE = 2.3f;

    private static final byte DEFAULT_VALUE = (byte) pack(600);
    private static final short[] UNPACK_LOOKUP = new short[256];

    public static final Codec<RainfallRaster> CODEC = RecordCodecBuilder.create(i -> i.group(
            RasterShape.CODEC.forGetter(RainfallRaster::shape),
            RasterBufferCodecs.BYTES.fieldOf("data").forGetter(r -> r.buffer)
    ).apply(i, RainfallRaster::new));
    public static final RasterType<RainfallRaster> TYPE = RasterType.create(RainfallRaster::create, CODEC);

    static {
        for (int packed = 0; packed < UNPACK_LOOKUP.length; packed++) {
            UNPACK_LOOKUP[packed] = unpack(packed);
        }
    }

    protected RainfallRaster(final RasterShape shape, final byte[] buffer) {
        super(shape, buffer);
    }

    public static RainfallRaster create(final RasterShape shape) {
        final byte[] buffer = new byte[shape.size()];
        Arrays.fill(buffer, DEFAULT_VALUE);
        return new RainfallRaster(shape, buffer);
    }

    public static RainfallRaster wrap(final RasterShape shape, final byte[] buffer) {
        return new RainfallRaster(shape, buffer);
    }

    public short getRainfall(final int x, final int y) {
        return UNPACK_LOOKUP[getByte(x, y)];
    }

    private static int pack(final int value) {
        return (int) Math.round((Math.pow(Util.inverseLerp(MIN, MAX, value), 1.0f / CURVE)) * 255.0f);
    }

    private static short unpack(final int packed) {
        return (short) Util.lerp(MIN, MAX, (float) Math.pow(packed / 255.0f, CURVE));
    }

    @Override
    public RasterType<? extends UnsignedByteRaster> type() {
        return TYPE;
    }
}
