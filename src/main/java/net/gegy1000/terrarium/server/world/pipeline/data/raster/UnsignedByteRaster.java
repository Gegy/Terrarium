package net.gegy1000.terrarium.server.world.pipeline.data.raster;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

public class UnsignedByteRaster implements Data, NumberRaster<Integer> {
    private final byte[] data;
    private final int width;
    private final int height;

    public UnsignedByteRaster(byte[] data, int width, int height) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match data length!");
        }
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public UnsignedByteRaster(int width, int height) {
        this.data = new byte[width * height];
        this.width = width;
        this.height = height;
    }

    public UnsignedByteRaster(DataView view) {
        this(view.getWidth(), view.getHeight());
    }

    @Override
    @Deprecated
    public void set(int x, int z, Integer value) {
        this.setByte(x, z, value);
    }

    public void setByte(int x, int z, int value) {
        this.data[x + z * this.width] = (byte) (value & 0xFF);
    }

    @Override
    @Deprecated
    public Integer get(int x, int z) {
        return this.getByte(x, z);
    }

    public int getByte(int x, int y) {
        return this.data[x + y * this.width] & 0xFF;
    }

    @Override
    public Integer[] getData() {
        byte[] data = this.getByteData();
        Integer[] result = new Integer[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] & 0xFF;
        }
        return result;
    }

    @Override
    public Object getRawData() {
        return this.data;
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

    @Override
    public void setDouble(int x, int y, double value) {
        int rounded = (int) value;
        this.setByte(x, y, (byte) MathHelper.clamp(rounded, 0, 255));
    }

    @Override
    public double getDouble(int x, int y) {
        return this.getByte(x, y);
    }

    @Override
    public UnsignedByteRaster copy() {
        return new UnsignedByteRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }
}
