package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConstantUnsignedBytePopulator implements DataLayer<UnsignedByteRasterTile> {
    private final byte value;

    public ConstantUnsignedBytePopulator(int value) {
        this.value = (byte) (value & 0xFF);
    }

    @Override
    public UnsignedByteRasterTile apply(LayerContext context, DataView view) {
        byte[] data = new byte[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new UnsignedByteRasterTile(data, view.getWidth(), view.getHeight());
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptyList();
    }
}
