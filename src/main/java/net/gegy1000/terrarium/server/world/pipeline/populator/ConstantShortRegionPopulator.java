package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

import java.util.Arrays;

public class ConstantShortRegionPopulator implements RegionPopulator<ShortRasterTile> {
    private final short value;

    public ConstantShortRegionPopulator(short value) {
        this.value = value;
    }

    @Override
    public ShortRasterTile populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        short[] data = new short[width * height];
        Arrays.fill(data, this.value);
        return new ShortRasterTile(data, width, height);
    }

    @Override
    public Class<ShortRasterTile> getType() {
        return ShortRasterTile.class;
    }
}
