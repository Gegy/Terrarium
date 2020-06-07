package net.gegy1000.terrarium.server.world.composer.decoration;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;

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
    public void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        for (DecorationComposer composer : this.composers) {
            composer.composeDecoration(terrarium, pos, writer);
        }
    }
}
