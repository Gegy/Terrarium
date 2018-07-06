package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

public class DebugCoverPopulator implements DataLayerProducer<CoverRasterTile> {
    @Override
    public CoverRasterTile apply(DataView view) {
        CoverRasterTile result = new CoverRasterTile(view);
        int viewX = view.getX();
        int viewY = view.getY();
        for (int localZ = 0; localZ < view.getHeight(); localZ++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                result.set(localX, localZ, DebugMap.getCover(localX + viewX, localZ + viewY).getCoverType());
            }
        }
        return result;
    }
}
