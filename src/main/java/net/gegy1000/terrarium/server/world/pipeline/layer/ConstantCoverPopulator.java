package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConstantCoverPopulator implements DataLayer<CoverRasterTile> {
    private final CoverType value;

    public ConstantCoverPopulator(CoverType value) {
        this.value = value;
    }

    @Override
    public CoverRasterTile apply(LayerContext context, DataView view) {
        CoverType[] data = new CoverType[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new CoverRasterTile(data, view.getWidth(), view.getHeight());
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptyList();
    }
}
