package net.gegy1000.terrarium.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import dev.gegy.gengen.core.GenGen;
import dev.gegy.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.world.World;

public class VanillaEntitySpawnComposer implements DecorationComposer {
    private static final long SPAWN_SEED = 24933181514746343L;

    private final SpatialRandom spatialRandom;

    public VanillaEntitySpawnComposer(World world) {
        this.spatialRandom = new SpatialRandom(world, SPAWN_SEED);
    }

    @Override
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        this.spatialRandom.setSeed(pos.getMinX(), pos.getMinY(), pos.getMinZ());
        GenGen.proxy(writer.getGlobal()).populateEntities(pos, writer, this.spatialRandom);
    }
}
