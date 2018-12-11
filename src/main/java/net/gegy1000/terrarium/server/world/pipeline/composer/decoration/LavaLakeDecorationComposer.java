package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.util.populator.VanillaLavaLakePopulator;
import net.gegy1000.earth.server.world.CubicIntegrationFormat;
import net.minecraft.world.World;

public class LavaLakeDecorationComposer extends SimpleDecorationComposer {
    private static final long DECORATION_SEED = 21052088057241959L;

    public LavaLakeDecorationComposer(World world, CubicIntegrationFormat format) {
        super(new VanillaLavaLakePopulator(world, DECORATION_SEED));
    }
}
