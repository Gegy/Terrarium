package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.gengen.api.ChunkPopulationWriter;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.minecraft.world.World;

import java.util.Random;

public class VanillaEntitySpawnComposer implements DecorationComposer {
    private static final long SPAWN_SEED = 24933181514746343L;

    private final SpatialRandom randomMap;
    private final Random random = new Random(0);

    public VanillaEntitySpawnComposer(World world) {
        this.randomMap = new SpatialRandom(world, SPAWN_SEED);
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.randomMap.initPosSeed(pos.getMinX(), pos.getMinY(), pos.getMinZ());
        this.random.setSeed(this.randomMap.next());

        GenGen.proxy(writer.getGlobal()).populateEntities(pos, writer, this.random);
    }
}
