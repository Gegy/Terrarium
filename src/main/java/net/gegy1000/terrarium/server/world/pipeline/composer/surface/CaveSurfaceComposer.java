package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.gengen.api.ChunkPrimeWriter;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.GenericChunkPrimer;
import net.gegy1000.gengen.util.primer.GenericCavePrimer;
import net.gegy1000.gengen.util.primer.GenericRavinePrimer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.minecraft.world.World;

public class CaveSurfaceComposer implements SurfaceComposer {
    private final GenericChunkPrimer caveGenerator;
    private final GenericChunkPrimer ravineGenerator;

    public CaveSurfaceComposer(World world) {
        this.caveGenerator = new GenericCavePrimer(world);
        this.ravineGenerator = new GenericRavinePrimer(world, world.getSeaLevel());
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        this.caveGenerator.prime(pos, writer);
        this.ravineGenerator.prime(pos, writer);
    }
}
