package net.gegy1000.earth.server.world.ecology.vegetation;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.maxent.MaxentGrowthIndicator;
import net.minecraft.util.ResourceLocation;

public final class Trees {
    public static final float RADIUS = 3.0F;

    private static final TreeGenerators GENERATORS = TreeGenerators.collect();

    public static final Vegetation ACACIA = Vegetation.builder()
            .generator(GENERATORS.acacia)
            .growthIndicator(Indicators.ACACIA)
            .build();

    public static final Vegetation BIRCH = Vegetation.builder()
            .generator(GENERATORS.birch)
            .growthIndicator(Indicators.BIRCH)
            .build();

    public static final Vegetation OAK = Vegetation.builder()
            .generator(GENERATORS.oak)
            .growthIndicator(Indicators.OAK)
            .build();

    public static final Vegetation JUNGLE = Vegetation.builder()
            .generator(GENERATORS.jungle)
            .growthIndicator(Indicators.JUNGLE_LIKE)
            .build();

    public static final Vegetation BIG_JUNGLE = Vegetation.builder()
            .generator(GENERATORS.bigJungle)
            .growthIndicator(Indicators.JUNGLE_LIKE.pow(3.0))
            .build();

    public static final Vegetation SPRUCE = Vegetation.builder()
            .generator(GENERATORS.spruce)
            .growthIndicator(Indicators.SPRUCE)
            .build();

    public static final Vegetation PINE = Vegetation.builder()
            .generator(GENERATORS.pine)
            .growthIndicator(Indicators.PINE)
            .build();

    public static class Indicators {
        public static final GrowthIndicator ACACIA = maxentIndicator("acacia");
        public static final GrowthIndicator BIRCH = maxentIndicator("birch").pow(1.0 / 1.3);
        public static final GrowthIndicator OAK = maxentIndicator("oak");
        public static final GrowthIndicator JUNGLE_LIKE = maxentIndicator("jungle_like");
        public static final GrowthIndicator SPRUCE = maxentIndicator("spruce");
        public static final GrowthIndicator PINE = maxentIndicator("pine");

        private static GrowthIndicator maxentIndicator(String path) {
            return MaxentGrowthIndicator.tryParse(new ResourceLocation(TerrariumEarth.ID, "vegetation/models/trees/" + path + ".lambdas"))
                    .orElse(GrowthIndicator.no());
        }
    }
}
