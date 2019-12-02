package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;

import java.util.Collection;

public final class CompositeSurfaceComposer implements SurfaceComposer {
    private final SurfaceComposer[] composers;

    private CompositeSurfaceComposer(SurfaceComposer[] composers) {
        this.composers = composers;
    }

    public static CompositeSurfaceComposer of(SurfaceComposer... composers) {
        return new CompositeSurfaceComposer(composers);
    }

    public static CompositeSurfaceComposer of(Collection<SurfaceComposer> composers) {
        return new CompositeSurfaceComposer(composers.toArray(new SurfaceComposer[0]));
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        for (SurfaceComposer composer : this.composers) {
            composer.composeSurface(data, pos, writer);
        }
    }
}
