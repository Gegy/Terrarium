package net.gegy1000.terrarium.server.world.pipeline.populator;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.world.World;

import java.util.Arrays;

public class StaticByteRegionPopulator implements RegionPopulator<ByteRasterTileAccess> {
    private final byte value;

    public StaticByteRegionPopulator(byte value) {
        this.value = value;
    }

    @Override
    public ByteRasterTileAccess populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        byte[] data = new byte[width * height];
        Arrays.fill(data, this.value);
        return new ByteRasterTileAccess(data, width, height);
    }

    @Override
    public Class<ByteRasterTileAccess> getType() {
        return ByteRasterTileAccess.class;
    }

    public static class Parser implements InstanceObjectParser<RegionPopulator<?>> {
        @Override
        public RegionPopulator<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            byte value = (byte) valueParser.parseInteger(objectRoot, "value");
            return new StaticByteRegionPopulator(value);
        }
    }
}
