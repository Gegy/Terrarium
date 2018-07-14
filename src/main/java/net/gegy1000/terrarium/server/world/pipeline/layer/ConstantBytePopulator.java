package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConstantBytePopulator implements DataLayer<ByteRasterTile> {
    private final byte value;

    public ConstantBytePopulator(byte value) {
        this.value = value;
    }

    @Override
    public ByteRasterTile apply(LayerContext context, DataView view) {
        byte[] data = new byte[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new ByteRasterTile(data, view.getWidth(), view.getHeight());
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptyList();
    }
}
