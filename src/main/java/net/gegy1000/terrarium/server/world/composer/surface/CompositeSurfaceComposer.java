package net.gegy1000.terrarium.server.world.composer.surface;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.DataSample;

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
    public void composeSurface(TerrariumWorld terrarium, DataSample data, CubicPos pos, ChunkPrimeWriter writer) {
        for (SurfaceComposer composer : this.composers) {
            composer.composeSurface(terrarium, data, pos, writer);
        }
    }
}
