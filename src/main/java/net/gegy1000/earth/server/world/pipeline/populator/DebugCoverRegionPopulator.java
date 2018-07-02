package net.gegy1000.earth.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

public class DebugCoverRegionPopulator implements RegionPopulator<CoverRasterTile> {
    @Override
    public CoverRasterTile populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        CoverType[] data = new CoverType[width * height];
        int minBufferedX = pos.getMinBufferedX();
        int minBufferedZ = pos.getMinBufferedZ();
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                data[localX + localZ * width] = DebugMap.getCover(localX + minBufferedX, localZ + minBufferedZ).getCoverType();
            }
        }
        return new CoverRasterTile(data, width, height);
    }

    @Override
    public Class<CoverRasterTile> getType() {
        return CoverRasterTile.class;
    }
}
