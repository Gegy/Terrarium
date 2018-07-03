package net.gegy1000.terrarium.server.world.pipeline.source.tile;

public class ByteRasterTile implements TiledDataAccess, RasterDataAccess<Byte> {
    private final byte[] data;
    private final int width;
    private final int height;

    public ByteRasterTile(byte[] data, int width, int height) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    @Deprecated
    public void set(int x, int z, Byte value) {
        this.data[x + z * this.width] = value;
    }

    public void setByte(int x, int z, byte value) {
        this.data[x + z * this.width] = value;
    }

    @Override
    @Deprecated
    public Byte get(int x, int z) {
        return this.getByte(x, z);
    }

    public byte getByte(int x, int y) {
        return this.data[x + y * this.width];
    }

    public int getUnsigned(int x, int y) {
        return this.getByte(x, y) & 0xFF;
    }

    @Override
    public Byte[] getData() {
        byte[] data = this.getByteData();
        Byte[] result = new Byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    public byte[] getByteData() {
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
