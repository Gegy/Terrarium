package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.minecraft.world.World;

public class VanillaEntitySpawnComposer implements DecorationComposer {
    private static final long SPAWN_SEED = 24933181514746343L;

    private final SpatialRandom spatialRandom;

    public VanillaEntitySpawnComposer(World world) {
        this.spatialRandom = new SpatialRandom(world, SPAWN_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.spatialRandom.setSeed(pos.getMinX(), pos.getMinY(), pos.getMinZ());
        GenGen.proxy(writer.getGlobal()).populateEntities(pos, writer, this.spatialRandom);
    }
}
