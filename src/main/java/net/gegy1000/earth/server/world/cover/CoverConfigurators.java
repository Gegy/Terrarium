package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.cover.carver.Carvers;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.minecraftforge.common.BiomeDictionary;

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
        config.carve(Carvers.flooded(RegionComponentType.HEIGHT));
    };
}
