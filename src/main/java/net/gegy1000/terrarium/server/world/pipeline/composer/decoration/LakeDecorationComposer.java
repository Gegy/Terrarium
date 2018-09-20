package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.util.populator.VanillaLakePopulator;
import net.minecraft.world.World;

public class LakeDecorationComposer extends SimpleDecorationComposer {
    private static final long DECORATION_SEED = 1576583677480695379L;

    public LakeDecorationComposer(World world) {
        super(new VanillaLakePopulator(world, DECORATION_SEED));
    }
}
