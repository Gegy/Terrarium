package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

import java.util.Arrays;

public class ConstantByteRegionPopulator implements RegionPopulator<ByteRasterTile> {
    private final byte value;

    public ConstantByteRegionPopulator(byte value) {
        this.value = value;
    }

    @Override
    public ByteRasterTile populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        byte[] data = new byte[width * height];
        Arrays.fill(data, this.value);
        return new ByteRasterTile(data, width, height);
    }

    @Override
    public Class<ByteRasterTile> getType() {
        return ByteRasterTile.class;
    }
}
