package net.gegy1000.terrarium.server.world.composer.decoration;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;

import java.util.Collection;

public final class CompositeDecorationComposer implements DecorationComposer {
    private final DecorationComposer[] composers;

    private CompositeDecorationComposer(DecorationComposer[] composers) {
        this.composers = composers;
    }

    public static CompositeDecorationComposer of(DecorationComposer... composers) {
        return new CompositeDecorationComposer(composers);
    }

    public static CompositeDecorationComposer of(Collection<DecorationComposer> composers) {
        return new CompositeDecorationComposer(composers.toArray(new DecorationComposer[0]));
    }

    @Override
    public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        for (DecorationComposer composer : this.composers) {
            composer.composeDecoration(dataCache, pos, writer);
        }
    }
}
