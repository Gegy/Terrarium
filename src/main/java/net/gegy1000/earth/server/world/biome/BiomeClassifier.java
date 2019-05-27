package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeClassifier {
    private static final float SNOW_TEMPERATURE = 5.0F;

    private static final ClassificationTree<Biome, Context> TREE = ClassificationTree.<Biome, Context>create()
            .when(Context::isSea).then(ocean -> ocean
                    .when(Context::isFrozen).thenYield(Biomes.FROZEN_OCEAN)
                    .otherwise().thenYield(Biomes.OCEAN)
            )
            .when(Context::isRiverOrLake).then(river -> river
                    .when(Context::isFrozen).thenYield(Biomes.FROZEN_RIVER)
                    .otherwise().thenYield(Biomes.RIVER)
            )
            .otherwise().then(land -> land
                    .when(Context::isFrozen).then(frozen -> frozen
                            .when(Context::isForested).thenYield(Biomes.TAIGA)
                            .otherwise().thenYield(Biomes.ICE_PLAINS)
                    )
                    .when(Context::isFlooded).then(flooded -> flooded
                            .yield(Biomes.SWAMPLAND)
                    )
                    .otherwise().then(warm -> warm
                            .when(Context::isWet).then(wet -> wet
                                    .when(Context::isForested).thenYield(Biomes.JUNGLE)
                                    .otherwise().thenYield(Biomes.JUNGLE_EDGE)
                            )
                            .when(Context::isDry).then(dry -> dry
                                    .when(Context::isBarren).thenYield(Biomes.DESERT)
                                    .otherwise().thenYield(Biomes.SAVANNA)
                            )
                            .otherwise().then(normal -> normal
                                    .when(Context::isForested).thenYield(Biomes.FOREST)
                                    .otherwise().thenYield(Biomes.PLAINS)
                            )
                    )
            );

    public static Biome classify(Context context) {
        return TREE.classify(context);
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
