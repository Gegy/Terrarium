package net.gegy1000.terrarium.server.world.pipeline.data;

import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;

public interface RasterConstructor<T extends RasterData<?>> {
    T construct(int width, int height);

    default T construct(DataView view) {
        return this.construct(view.getWidth(), view.getHeight());
    }
}
