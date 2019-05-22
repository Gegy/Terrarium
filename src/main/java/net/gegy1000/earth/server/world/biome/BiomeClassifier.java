package net.gegy1000.earth.server.world.biome;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.geography.Landform;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeClassifier {
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
                            .yield(Biomes.ICE_PLAINS)
                    )
                    .otherwise().then(normal -> normal
                            .yield(Biomes.PLAINS)
                    )
            );

    public static Biome classify(Context context) {
        return TREE.classify(context);
    }

    public static class Context {
        public Landform landform;
        public float averageTemperature;
        public int annualRainfall;
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
            return this.averageTemperature < 10.0F || this.is(CoverMarker.FROZEN);
        }

        public boolean is(CoverMarker marker) {
            return this.cover.getConfig().markers().contains(marker);
        }
    }
}
