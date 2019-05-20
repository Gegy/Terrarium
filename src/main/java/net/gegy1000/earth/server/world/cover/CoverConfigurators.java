package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.cover.carver.Carvers;
import net.gegy1000.earth.server.world.cover.decorator.VegetationDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Grasses;
import net.gegy1000.earth.server.world.pipeline.EarthDataKeys;
import net.minecraftforge.common.BiomeDictionary;

// TODO: I believe we might want a sort of registry system for different vegetation pools, which can be extended externally
public final class CoverConfigurators {
    public static final CoverConfigurator NONE = config -> {};

    public static final CoverConfigurator WATER = config -> {
        config.classify(BiomeDictionary.Type.WET);
        config.classify(BiomeDictionary.Type.OCEAN);
    };

    public static final CoverConfigurator SNOWY = config -> {
        config.classify(BiomeDictionary.Type.SNOWY);
    };

    public static final CoverConfigurator FLOODED = config -> {
        config.classify(BiomeDictionary.Type.WET, BiomeDictionary.Type.SWAMP);
        config.carve(Carvers.flooded(EarthDataKeys.HEIGHT));
    };

    public static final CoverConfigurator HERBACEOUS = config -> {
        config.decorate(
                VegetationDecorator.builder()
                        .withVegetation(Grasses.GRASS, 10.0F)
                        .withRadius(Grasses.RADIUS)
                        .withDensity(0.2F, 0.4F)
                        .build()
        );
    };

    public static final CoverConfigurator DENSELY_HERBACEOUS = config -> {
        config.decorate(
                VegetationDecorator.builder()
                        .withVegetation(Grasses.GRASS, 10.0F)
                        .withVegetation(Grasses.FERN, 3.0F)
                        .withVegetation(Grasses.TALL_GRASS, 3.0F)
                        .withVegetation(Grasses.TALL_FERN, 0.9F)
                        .withRadius(Grasses.RADIUS)
                        .withDensity(0.5F, 0.6F)
                        .build()
        );
    };
}
