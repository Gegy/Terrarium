package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeClassifier {
    public static final float FREEZE_TEMPERATURE = 0.0F;
    public static final float COLD_TEMPERATURE = 10.0F;

    public static Biome classify(Context context) {
        if (context.isLand()) {
            return classifyLand(context);
        } else {
            return classifyWater(context);
        }
    }

    private static Biome classifyLand(Context context) {
        if (context.isFrozen()) {
            return context.isForested() ? Biomes.COLD_TAIGA : Biomes.ICE_PLAINS;
        } else if (context.isCold()) {
            return Biomes.TAIGA;
        }

        if (context.isWet() || context.isFlooded()) {
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
        public int elevation;
        public Landform landform;
        public float minTemperature;
        public float meanTemperature;
        public int annualRainfall;
        public Cover cover;

        public int getElevation() {
            return this.elevation;
        }

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
            return this.minTemperature < FREEZE_TEMPERATURE || this.cover.is(CoverMarkers.FROZEN);
        }

        public boolean isCold() {
            return this.meanTemperature < COLD_TEMPERATURE || this.isFrozen();
        }

        public boolean isForested() {
            return this.cover.is(CoverMarkers.FOREST);
        }

        public boolean isFlooded() {
            return this.cover.is(CoverMarkers.FLOODED);
        }

        public boolean isBarren() {
            return this.cover.is(CoverMarkers.BARREN);
        }

        // TODO: These values?
        public boolean isWet() {
            return this.annualRainfall > 2400;
        }

        public boolean isDry() {
            return this.annualRainfall < 360;
        }
    }
}
