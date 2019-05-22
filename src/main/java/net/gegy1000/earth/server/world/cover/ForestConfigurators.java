package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.biome.CoverMarker;
import net.gegy1000.earth.server.world.cover.decorator.VegetationDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.terrarium.server.util.WeightedPool;

public final class ForestConfigurators {
    private static final CoverConfigurator FOREST = config -> config.mark(CoverMarker.FORESTED);

    public static final CoverConfigurator BROADLEAF_EVERGREEN = forest(0.15F, 0.6F, Trees.BROADLEAVED_EVERGREEN);
    public static final CoverConfigurator BROADLEAF_DECIDUOUS = forest(0.15F, 0.6F, Trees.BROADLEAVED_DECIDUOUS);
    public static final CoverConfigurator BROADLEAF_DECIDUOUS_CLOSED = forest(0.4F, 0.8F, Trees.BROADLEAVED_DECIDUOUS);
    public static final CoverConfigurator BROADLEAF_DECIDUOUS_OPEN = forest(0.15F, 0.4F, Trees.BROADLEAVED_DECIDUOUS);
    public static final CoverConfigurator NEEDLELEAF_EVERGREEN = forest(0.15F, 0.6F, Trees.NEEDLELEAVED_EVERGREEN);
    public static final CoverConfigurator NEEDLELEAF_EVERGREEN_CLOSED = forest(0.4F, 0.8F, Trees.NEEDLELEAVED_EVERGREEN);
    public static final CoverConfigurator NEEDLELEAF_EVERGREEN_OPEN = forest(0.15F, 0.4F, Trees.NEEDLELEAVED_EVERGREEN);
    public static final CoverConfigurator NEEDLELEAF_DECIDUOUS = forest(0.15F, 0.6F, Trees.NEEDLELEAVED_DECIDUOUS);
    public static final CoverConfigurator NEEDLELEAF_DECIDUOUS_CLOSED = forest(0.4F, 0.8F, Trees.NEEDLELEAVED_DECIDUOUS);
    public static final CoverConfigurator NEEDLELEAF_DECIDUOUS_OPEN = forest(0.15F, 0.4F, Trees.NEEDLELEAVED_DECIDUOUS);
    public static final CoverConfigurator MIXED_LEAF_TYPE = forest(0.15F, 0.6F,
            Trees.BROADLEAVED_EVERGREEN, Trees.BROADLEAVED_DECIDUOUS,
            Trees.NEEDLELEAVED_EVERGREEN, Trees.NEEDLELEAVED_DECIDUOUS
    );

    @SafeVarargs
    private static CoverConfigurator forest(float minDensity, float maxDensity, WeightedPool<Vegetation>... pools) {
        return config -> {
            ForestConfigurators.FOREST.configure(config);
            CoverConfigurators.DENSELY_HERBACEOUS.configure(config);

            VegetationDecorator.Builder builder = VegetationDecorator.builder()
                    .withRadius(Trees.RADIUS)
                    .withDensity(minDensity, maxDensity);

            for (WeightedPool<Vegetation> pool : pools) {
                builder = builder.withVegetation(pool);
            }

            config.decorate(builder.build());
        };
    }
}
