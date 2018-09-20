package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.cubicglue.util.VanillaBiomeDecorator;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public class VanillaBiomeDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 24933181514746343L;

    @Override
    public void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer) {
        VanillaBiomeDecorator.decorate(pos, writer, writer.getCenterBiome());

//        // TODO
//        if (TerrainGen.populate(null, world, this.horizontalRandom, pos.getX(), pos.getZ(), false, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
//            WorldEntitySpawner.performWorldGenSpawning(world, biome, globalX + 8, globalZ + 8, 16, 16, this.horizontalRandom);
//        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
