package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.EarthCoverPriming;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;

public final class CoverCarveComposer implements SurfaceComposer {
    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        data.get(EarthDataKeys.COVER).ifPresent(coverRaster -> {
            Cover focus = coverRaster.get(8, 8);

            EarthCoverPriming.Builder builder = new EarthCoverPriming.Builder();
            focus.configurePrimer(builder);

            EarthCoverPriming primer = builder.build();
            primer.carvers.forEach(carver -> carver.carve(pos, writer, data));
        });
    }
}
