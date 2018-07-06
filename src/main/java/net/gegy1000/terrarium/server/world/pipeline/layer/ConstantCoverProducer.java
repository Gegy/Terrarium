package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

import java.util.Arrays;

public class ConstantCoverProducer implements DataLayerProducer<CoverRasterTile> {
    private final CoverType value;

    public ConstantCoverProducer(CoverType value) {
        this.value = value;
    }

    @Override
    public CoverRasterTile apply(DataView view) {
        CoverType[] data = new CoverType[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new CoverRasterTile(data, view.getWidth(), view.getHeight());
    }
}
