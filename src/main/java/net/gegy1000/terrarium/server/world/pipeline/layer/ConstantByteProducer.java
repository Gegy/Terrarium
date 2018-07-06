package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;

import java.util.Arrays;

public class ConstantByteProducer implements DataLayerProducer<ByteRasterTile> {
    private final byte value;

    public ConstantByteProducer(byte value) {
        this.value = value;
    }

    @Override
    public ByteRasterTile apply(DataView view) {
        byte[] data = new byte[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new ByteRasterTile(data, view.getWidth(), view.getHeight());
    }
}
