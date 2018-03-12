package net.gegy1000.terrarium.server.world.pipeline.source.tile;

public class ShortRasterTileAccess implements TiledDataAccess, RasterDataAccess<Short> {
    private final short[] data;
    private final int width;
    private final int height;

    public ShortRasterTileAccess(short[] data, int width, int height) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    @Deprecated
    public void set(int x, int z, Short value) {
        this.data[x + z * this.width] = value;
    }

    public void setShort(int x, int z, short value) {
        this.data[x + z * this.width] = value;
    }

    @Override
    @Deprecated
    public Short get(int x, int z) {
        return this.getShort(x, z);
    }

    public short getShort(int x, int z) {
        return this.data[x + z * this.width];
    }

    @Override
    public Short[] getData() {
        short[] data = this.getShortData();
        Short[] result = new Short[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    public short[] getShortData() {
        return this.data;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
