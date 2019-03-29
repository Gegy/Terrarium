package net.gegy1000.terrarium.server.world.pipeline.data.raster;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

public class ByteRaster implements Data, NumberRaster<Byte> {
    private final byte[] data;
    private final int width;
    private final int height;

    public ByteRaster(byte[] data, int width, int height) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public ByteRaster(int width, int height) {
        this.data = new byte[width * height];
        this.width = width;
        this.height = height;
    }

    public ByteRaster(DataView view) {
        this(view.getWidth(), view.getHeight());
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
    public Object getRawData() {
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

    @Override
    public void setDouble(int x, int y, double value) {
        int rounded = (int) value;
        this.setByte(x, y, (byte) MathHelper.clamp(rounded, Byte.MIN_VALUE, Byte.MAX_VALUE));
    }

    @Override
    public double getDouble(int x, int y) {
        return this.getByte(x, y);
    }

    @Override
    public ByteRaster copy() {
        return new ByteRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }
}
