package net.gegy1000.terrarium.server.world.composer.surface;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.generator.GenericChunkPrimer;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.world.data.ColumnData;

public class GenericSurfaceComposer implements SurfaceComposer {
    private final GenericChunkPrimer primer;

    private GenericSurfaceComposer(GenericChunkPrimer primer) {
        this.primer = primer;
    }

    public static GenericSurfaceComposer of(GenericChunkPrimer primer) {
        return new GenericSurfaceComposer(primer);
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        this.primer.primeChunk(pos, writer);
    }
}
