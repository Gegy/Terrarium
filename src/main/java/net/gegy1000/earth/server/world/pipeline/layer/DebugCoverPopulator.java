package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;

import java.util.Collection;
import java.util.Collections;

public class DebugCoverPopulator implements DataLayer<BiomeRasterTile> {
    @Override
    public BiomeRasterTile apply(LayerContext context, DataView view) {
        BiomeRasterTile result = new BiomeRasterTile(view);
        int viewX = view.getX();
        int viewY = view.getY();
        for (int localZ = 0; localZ < view.getHeight(); localZ++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                result.set(localX, localZ, DebugMap.getCover(localX + viewX, localZ + viewY).getBiome());
            }
        }
        return result;
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptyList();
    }
}
