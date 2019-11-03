package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeClassifier {
    private static final float SNOW_TEMPERATURE = 5.0F;

    public static Biome classify(Context context) {
        if (context.isLand()) {
            return classifyLand(context);
        } else {
            return classifyWater(context);
        }
    }

    private static Biome classifyLand(Context context) {
        if (context.isFrozen()) {
            return context.isForested() ? Biomes.TAIGA : Biomes.ICE_PLAINS;
        }

        if (context.isFlooded()) {
            return Biomes.SWAMPLAND;
        }

        if (context.isWet()) {
            return context.isForested() ? Biomes.JUNGLE : Biomes.JUNGLE_EDGE;
        }

        if (context.isDry()) {
            return context.isBarren() ? Biomes.DESERT : Biomes.SAVANNA;
        }

        return context.isForested() ? Biomes.FOREST : Biomes.PLAINS;
    }

    private static Biome classifyWater(Context context) {
        if (context.isSea()) {
            return context.isFrozen() ? Biomes.FROZEN_OCEAN : Biomes.OCEAN;
        }

        return context.isFrozen() ? Biomes.FROZEN_RIVER : Biomes.RIVER;
    }

    public static class Context {
        public Landform landform;
        public float averageTemperature;
        public int monthlyRainfall;
        public Cover cover;

        public boolean isSea() {
            return this.landform == Landform.SEA;
        }

        public boolean isRiverOrLake() {
            return this.landform == Landform.LAKE_OR_RIVER;
        }

        public boolean isLand() {
            return this.landform == Landform.LAND;
        }

        public boolean isFrozen() {
            // TODO: Use mean min temperature rather?
            return this.averageTemperature < SNOW_TEMPERATURE || this.is(CoverMarker.FROZEN);
        }

        public boolean isForested() {
            return this.is(CoverMarker.FORESTED);
        }

        public boolean isFlooded() {
            return this.is(CoverMarker.FLOODED);
        }

        public boolean isBarren() {
            return this.is(CoverMarker.BARREN);
        }

        // TODO: These values?
        public boolean isWet() {
            return this.monthlyRainfall > 200;
        }

        public boolean isDry() {
            return this.monthlyRainfall < 30;
        }

        public boolean is(CoverMarker marker) {
            return this.cover.getConfig().markers().contains(marker);
        }
    }
}
