package net.gegy1000.terrarium.server.world;

import com.google.gson.Gson;
import joptsimple.internal.Strings;
import net.gegy1000.terrarium.Terrarium;

public class EarthGenerationSettings {
    public static final double REAL_SCALE = 92.766203;

    private static final Gson GSON = new Gson();

    public double spawnLatitude = 27.988350;
    public double spawnLongitude = 86.923641;
    public boolean buildings = false;
    public boolean streets = false;
    public boolean decorate = true;
    public double worldScale = 1.0 / 35.0;
    public double terrainHeightScale = 1.0;
    public double noiseScale = 1.0;
    public int heightOffset = 5;
    public int scatterRange = 12;
    public boolean mapFeatures = false;
    public boolean caveGeneration = false;
    public boolean resourceGeneration = false;

    public EarthGenerationSettings() {
    }

    public EarthGenerationSettings(double spawnLatitude, double spawnLongitude,
                                   boolean buildings, boolean streets, boolean decorate, int scatterRange,
                                   double worldScale, double terrainHeightScale, double noiseScale, int heightOffset,
                                   boolean mapFeatures, boolean caveGeneration, boolean resourceGeneration) {
        this.spawnLatitude = spawnLatitude;
        this.spawnLongitude = spawnLongitude;
        this.buildings = buildings;
        this.streets = streets;
        this.decorate = decorate;
        this.scatterRange = scatterRange;
        this.worldScale = worldScale;
        this.terrainHeightScale = terrainHeightScale;
        this.noiseScale = noiseScale;
        this.heightOffset = heightOffset;
        this.mapFeatures = mapFeatures;
        this.caveGeneration = caveGeneration;
        this.resourceGeneration = resourceGeneration;
    }

    public static EarthGenerationSettings deserialize(String settings) {
        if (Strings.isNullOrEmpty(settings)) {
            return new EarthGenerationSettings();
        }
        try {
            return GSON.fromJson(settings, EarthGenerationSettings.class);
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to parse settings string: \"{}\"", settings, e);
        }
        return new EarthGenerationSettings();
    }

    public double getFinalScale() {
        return this.worldScale * REAL_SCALE;
    }

    public double getInverseScale() {
        return 1.0 / this.getFinalScale();
    }

    public int getOceanHeight() {
        return this.heightOffset + 1;
    }

    public String serialize() {
        return GSON.toJson(this);
    }
}
