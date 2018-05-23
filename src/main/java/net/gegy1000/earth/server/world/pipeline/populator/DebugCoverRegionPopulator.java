package net.gegy1000.earth.server.world.pipeline.populator;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.world.World;

public class DebugCoverRegionPopulator implements RegionPopulator<CoverRasterTileAccess> {
    @Override
    public CoverRasterTileAccess populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        CoverType[] data = new CoverType[width * height];
        int minBufferedX = pos.getMinBufferedX();
        int minBufferedZ = pos.getMinBufferedZ();
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                data[localX + localZ * width] = DebugMap.getCover(localX + minBufferedX, localZ + minBufferedZ).getCoverType();
            }
        }
        return new CoverRasterTileAccess(data, width, height);
    }

    @Override
    public Class<CoverRasterTileAccess> getType() {
        return CoverRasterTileAccess.class;
    }

    public static class Parser implements InstanceObjectParser<RegionPopulator<?>> {
        @Override
        public RegionPopulator<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            return new DebugCoverRegionPopulator();
        }
    }
}
