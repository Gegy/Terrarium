package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

import java.util.Arrays;

public class ConstantShortProducer implements DataLayerProducer<ShortRasterTile> {
    private final short value;

    public ConstantShortProducer(short value) {
        this.value = value;
    }

    @Override
    public ShortRasterTile apply(DataView view) {
        short[] data = new short[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new ShortRasterTile(data, view.getWidth(), view.getHeight());
    }
}
