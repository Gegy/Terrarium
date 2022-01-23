package net.gegy1000.terrarium.server.world.composer.surface;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.generator.GenericChunkPrimer;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.DataSample;

public class GenericSurfaceComposer implements SurfaceComposer {
    private final GenericChunkPrimer primer;

    private GenericSurfaceComposer(GenericChunkPrimer primer) {
        this.primer = primer;
    }

    public static GenericSurfaceComposer of(GenericChunkPrimer primer) {
        return new GenericSurfaceComposer(primer);
    }

    @Override
    public void composeSurface(TerrariumWorld terrarium, DataSample data, CubicPos pos, ChunkPrimeWriter writer) {
        this.primer.primeChunk(pos, writer);
    }
}
