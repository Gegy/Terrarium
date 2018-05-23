package net.gegy1000.terrarium.server.world.pipeline.populator;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.world.World;

import java.util.Arrays;

public class StaticShortRegionPopulator implements RegionPopulator<ShortRasterTileAccess> {
    private final short value;

    public StaticShortRegionPopulator(short value) {
        this.value = value;
    }

    @Override
    public ShortRasterTileAccess populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        short[] data = new short[width * height];
        Arrays.fill(data, this.value);
        return new ShortRasterTileAccess(data, width, height);
    }

    @Override
    public Class<ShortRasterTileAccess> getType() {
        return ShortRasterTileAccess.class;
    }

    public static class Parser implements InstanceObjectParser<RegionPopulator<?>> {
        @Override
        public RegionPopulator<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            short value = (short) valueParser.parseInteger(objectRoot, "value");
            return new StaticShortRegionPopulator(value);
        }
    }
}
