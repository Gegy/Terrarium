package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;

import java.util.concurrent.CompletableFuture;

public final class DebugCoverPopulator {
    public static DataFuture<CoverRaster> populate() {
        return DataFuture.of((engine, view) -> {
            CoverRaster result = new CoverRaster(view);
            int viewX = view.getX();
            int viewY = view.getY();
            for (int localZ = 0; localZ < view.getHeight(); localZ++) {
                for (int localX = 0; localX < view.getWidth(); localX++) {
                    result.set(localX, localZ, DebugMap.getCover(localX + viewX, localZ + viewY).getCoverType());
                }
            }

            return CompletableFuture.completedFuture(result);
        });
    }
}
