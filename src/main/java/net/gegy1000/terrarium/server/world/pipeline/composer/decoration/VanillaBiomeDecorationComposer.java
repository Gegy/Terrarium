package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.VanillaBiomeDecorator;
import net.gegy1000.earth.server.world.CubicIntegrationFormat;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public class VanillaBiomeDecorationComposer implements DecorationComposer {
    public VanillaBiomeDecorationComposer(CubicIntegrationFormat format) {

    }

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        VanillaBiomeDecorator.decorate(pos, writer, writer.getCenterBiome());
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
