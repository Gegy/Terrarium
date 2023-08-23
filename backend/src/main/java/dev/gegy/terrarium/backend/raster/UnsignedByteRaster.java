package dev.gegy.terrarium.backend.raster;

public class UnsignedByteRaster implements IntLikeRaster {
    public static final RasterType<UnsignedByteRaster> TYPE = RasterType.create(UnsignedByteRaster::create);

    protected final RasterShape shape;
    protected final byte[] buffer;

    protected UnsignedByteRaster(final RasterShape shape, final byte[] buffer) {
        this.shape = shape;
        this.buffer = buffer;
    }

    public static UnsignedByteRaster create(final RasterShape shape) {
        final byte[] buffer = new byte[shape.size()];
        return new UnsignedByteRaster(shape, buffer);
    }

    public static UnsignedByteRaster wrap(final RasterShape shape, final byte[] buffer) {
        return new UnsignedByteRaster(shape, buffer);
    }

    public void putByte(final int x, final int y, final int value) {
        buffer[shape.index(x, y)] = (byte) (value & 0xff);
    }

    public int getByte(final int x, final int y) {
        return buffer[shape.index(x, y)] & 0xff;
    }

    @Override
    public RasterType<? extends UnsignedByteRaster> type() {
        return TYPE;
    }

    @Override
    public RasterShape shape() {
        return shape;
    }

    @Override
    public void putInt(final int x, final int y, final int value) {
        putByte(x, y, value);
    }

    @Override
    public int getInt(final int x, final int y) {
        return getByte(x, y);
    }
}
