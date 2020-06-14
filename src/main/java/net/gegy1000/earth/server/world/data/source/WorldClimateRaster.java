package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import org.tukaani.xz.SingleXZInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import static net.gegy1000.earth.server.world.EarthWorldType.CLIMATE_SCALE;

public final class WorldClimateRaster {
    public static final int WIDTH = 4320;
    public static final int HEIGHT = 2160;

    private static final float STANDARD_TEMPERATURE = 14.0F;
    private static final short STANDARD_RAINFALL = 600;

    private static final int PACK_RANGE = 255;

    private static final float TEMP_MIN = -40.0F;
    private static final float TEMP_MAX = 45.0F;
    private static final float TEMP_RANGE = TEMP_MAX - TEMP_MIN;
    private static final float TEMP_CURVE = 1.0F;

    private static final float RAIN_MIN = 0.0F;
    private static final float RAIN_RANGE = 7200.0F;
    private static final float RAIN_CURVE = 2.3F;

    private final float[] meanTemperature;
    private final float[] minTemperature;
    private final short[] annualRainfall;

    private WorldClimateRaster(float[] meanTemperature, float[] minTemperature, short[] annualRainfall) {
        this.meanTemperature = meanTemperature;
        this.minTemperature = minTemperature;
        this.annualRainfall = annualRainfall;
    }

    public static WorldClimateRaster parse(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(new SingleXZInputStream(in));

        byte[] meanTemperaturePacked = new byte[WIDTH * HEIGHT];
        data.readFully(meanTemperaturePacked);
        float[] meanTemperature = unpackTemperature(meanTemperaturePacked);

        byte[] minTemperaturePacked = new byte[WIDTH * HEIGHT];
        data.readFully(minTemperaturePacked);
        float[] minTemperature = unpackTemperature(minTemperaturePacked);

        byte[] annualRainfallPacked = new byte[WIDTH * HEIGHT];
        data.readFully(annualRainfallPacked);
        short[] annualRainfall = unpackRainfall(annualRainfallPacked);

        return new WorldClimateRaster(meanTemperature, minTemperature, annualRainfall);
    }

    private static float[] unpackTemperature(byte[] packed) {
        float[] unpacked = new float[packed.length];
        for (int i = 0; i < unpacked.length; i++) {
            unpacked[i] = unpackTemperature(packed[i]);
        }
        return unpacked;
    }

    private static float unpackTemperature(byte packed) {
        float shifted = (float) (packed & 0xFF);
        double unpacked = TEMP_RANGE * Math.pow(shifted / PACK_RANGE, TEMP_CURVE);
        return (float) (TEMP_MIN + unpacked);
    }

    private static short[] unpackRainfall(byte[] packed) {
        short[] unpacked = new short[packed.length];
        for (int i = 0; i < unpacked.length; i++) {
            unpacked[i] = unpackRainfall(packed[i]);
        }
        return unpacked;
    }

    private static short unpackRainfall(byte packed) {
        float shifted = (float) (packed & 0xFF);
        double unpacked = RAIN_RANGE * Math.pow(shifted / PACK_RANGE, RAIN_CURVE);
        return (short) (RAIN_MIN + unpacked);
    }

    public static CoordinateReference crs(double worldScale) {
        double scale = CLIMATE_SCALE / worldScale;
        return CoordinateReference.scaleAndOffset(scale, scale, -WIDTH / 2.0, -HEIGHT / 2.0);
    }

    public float getMeanTemperature(int x, int y) {
        if (outOfBounds(x, y)) return STANDARD_TEMPERATURE;
        return this.meanTemperature[index(x, y)];
    }

    public float getMinTemperature(int x, int y) {
        if (outOfBounds(x, y)) return STANDARD_TEMPERATURE;
        return this.minTemperature[index(x, y)];
    }

    public short getAnnualRainfall(int x, int y) {
        if (outOfBounds(x, y)) return STANDARD_RAINFALL;
        return this.annualRainfall[index(x, y)];
    }

    private static int index(int x, int y) {
        return x + y * WIDTH;
    }

    private static boolean outOfBounds(int x, int y) {
        return x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }
}
