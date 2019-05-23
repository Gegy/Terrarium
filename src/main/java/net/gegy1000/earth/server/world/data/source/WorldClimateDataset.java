package net.gegy1000.earth.server.world.data.source;

import org.tukaani.xz.SingleXZInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class WorldClimateDataset {
    public static final int WIDTH = 4320;
    public static final int HEIGHT = 2160;

    private static final int OFFSET_X = WorldClimateDataset.WIDTH / 2;
    private static final int OFFSET_Y = WorldClimateDataset.HEIGHT / 2;

    private static final int NO_DATA = 0;

    private static final float STANDARD_TEMPERATURE = 14.0F;
    private static final short STANDARD_RAINFALL = 990;

    private static final int PACK_MIN = 1;
    private static final int PACK_MAX = 255;
    private static final int PACK_RANGE = PACK_MAX - PACK_MIN;

    private final float tempMin;
    private final float tempRange;
    private final float tempCurve;

    private final float rainMin;
    private final float rainRange;
    private final float rainCurve;

    private final byte[] temperature;
    private final byte[] rainfall;

    private WorldClimateDataset(
            float tempMin, float tempMax, float tempCurve,
            float rainMin, float rainMax, float rainCurve,
            byte[] temperature, byte[] rainfall
    ) {
        this.tempMin = tempMin;
        this.tempRange = tempMax - tempMin;
        this.tempCurve = 1.0F / tempCurve;
        this.rainMin = rainMin;
        this.rainRange = rainMax - rainMin;
        this.rainCurve = 1.0F / rainCurve;
        this.temperature = temperature;
        this.rainfall = rainfall;
    }

    public static WorldClimateDataset parse(InputStream in) throws IOException {
        if (in == null) {
            throw new IOException("Climate Dataset stream was null");
        }

        try (DataInputStream data = new DataInputStream(new SingleXZInputStream(in))) {
            float tempMin = data.readFloat();
            float tempMax = data.readFloat();
            float tempCurve = data.readFloat();

            byte[] temperature = new byte[WIDTH * HEIGHT];
            data.readFully(temperature);

            float rainMin = data.readFloat();
            float rainMax = data.readFloat();
            float rainCurve = data.readFloat();

            byte[] rainfall = new byte[WIDTH * HEIGHT];
            data.readFully(rainfall);

            return new WorldClimateDataset(
                    tempMin, tempMax, tempCurve,
                    rainMin, rainMax, rainCurve,
                    temperature, rainfall
            );
        }
    }

    public float getAverageTemperature(int x, int y) {
        if (outOfBounds(x, y)) {
            return STANDARD_TEMPERATURE;
        }

        byte packed = this.temperature[index(x, y)];
        if (packed == NO_DATA) {
            return STANDARD_TEMPERATURE;
        }

        return this.unpackTemperature(packed);
    }

    public short getAnnualRainfall(int x, int y) {
        if (outOfBounds(x, y)) {
            return STANDARD_RAINFALL;
        }

        byte packed = this.rainfall[index(x, y)];
        if (packed == NO_DATA) {
            return STANDARD_RAINFALL;
        }

        return this.unpackRainfall(packed);
    }

    private float unpackTemperature(byte packed) {
        float shifted = (float) ((packed & 0xFF) - PACK_MIN);
        double unpacked = this.tempRange * Math.pow(shifted / PACK_RANGE, this.tempCurve);
        return (float) (this.tempMin + unpacked);
    }

    private short unpackRainfall(byte packed) {
        float shifted = (float) ((packed & 0xFF) - PACK_MIN);
        double unpacked = this.rainRange * Math.pow(shifted / PACK_RANGE, this.rainCurve);
        return (short) (this.rainMin + unpacked);
    }

    private static int index(int x, int y) {
        return (x + OFFSET_X) + (y + OFFSET_Y) * WIDTH;
    }

    private static boolean outOfBounds(int x, int y) {
        x += OFFSET_X;
        y += OFFSET_Y;
        return x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }
}
