package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

import java.util.Arrays;

public class ConstantCoverRegionPopulator implements RegionPopulator<CoverRasterTile> {
    private final CoverType value;

    public ConstantCoverRegionPopulator(CoverType value) {
        this.value = value;
    }

    @Override
    public CoverRasterTile populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        CoverType[] data = new CoverType[width * height];
        Arrays.fill(data, this.value);
        return new CoverRasterTile(data, width, height);
    }

    @Override
    public Class<CoverRasterTile> getType() {
        return CoverRasterTile.class;
    }
}
