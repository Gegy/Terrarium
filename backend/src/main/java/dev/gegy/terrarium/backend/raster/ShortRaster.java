package dev.gegy.terrarium.backend.raster;

public class ShortRaster implements IntLikeRaster {
    public static final RasterType<ShortRaster> TYPE = RasterType.create(ShortRaster::create);

    protected final RasterShape shape;
    protected final short[] buffer;

    protected ShortRaster(final RasterShape shape, final short[] buffer) {
        this.shape = shape;
        this.buffer = buffer;
    }

    public static ShortRaster create(final RasterShape shape) {
        final short[] buffer = new short[shape.size()];
        return new ShortRaster(shape, buffer);
    }

    public static ShortRaster wrap(final RasterShape shape, final short[] buffer) {
        return new ShortRaster(shape, buffer);
    }

    public void putShort(final int x, final int y, final short value) {
        buffer[shape.index(x, y)] = value;
    }

    public short getShort(final int x, final int y) {
        return buffer[shape.index(x, y)];
    }

    @Override
    public RasterType<ShortRaster> type() {
        return TYPE;
    }

    @Override
    public RasterShape shape() {
        return shape;
    }

    @Override
    public void putInt(final int x, final int y, final int value) {
        putShort(x, y, (short) (value & 0xffff));
    }

    @Override
    public int getInt(final int x, final int y) {
        return getShort(x, y);
    }
}
