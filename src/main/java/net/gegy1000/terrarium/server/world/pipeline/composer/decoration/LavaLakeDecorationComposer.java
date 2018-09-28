package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.util.populator.VanillaLavaLakePopulator;
import net.gegy1000.earth.server.world.CubicGenerationFormat;
import net.gegy1000.earth.server.world.FeatureGenerationFormat;
import net.minecraft.world.World;

public class LavaLakeDecorationComposer extends SimpleDecorationComposer {
    private static final long DECORATION_SEED = 21052088057241959L;

    public LavaLakeDecorationComposer(World world, CubicGenerationFormat format) {
        super(new VanillaLavaLakePopulator(world, DECORATION_SEED));
    }
}
