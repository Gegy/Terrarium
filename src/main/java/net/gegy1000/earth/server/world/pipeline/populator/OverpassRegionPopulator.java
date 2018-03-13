package net.gegy1000.earth.server.world.pipeline.populator;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class OverpassRegionPopulator implements RegionPopulator<OsmTileAccess> {
    private final ImmutableList<DataSampler<OsmTileAccess>> samplers;

    public OverpassRegionPopulator(List<DataSampler<OsmTileAccess>> samplers) {
        this.samplers = ImmutableList.copyOf(samplers);
    }

    @Override
    public OsmTileAccess populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        Coordinate minRegionCoordinate = pos.getMinBufferedCoordinate();
        Coordinate maxRegionCoordinate = pos.getMaxBufferedCoordinate();

        int minSampleX = MathHelper.floor(minRegionCoordinate.getBlockX()) - 8;
        int minSampleZ = MathHelper.floor(minRegionCoordinate.getBlockZ()) - 8;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getBlockX()) + 8;
        int maxSampleZ = MathHelper.ceil(maxRegionCoordinate.getBlockZ()) + 8;

        int sampleWidth = maxSampleX - minSampleX;
        int sampleHeight = maxSampleZ - minSampleZ;

        OsmTileAccess tile = null;
        for (DataSampler<OsmTileAccess> sampler : this.samplers) {
            if (sampler.shouldSample()) {
                OsmTileAccess sampled = sampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight);
                if (tile == null) {
                    tile = sampled;
                } else {
                    tile = tile.merge(sampled);
                }
            }
        }

        return tile != null ? tile : new OsmTileAccess();
    }

    @Override
    public Class<OsmTileAccess> getType() {
        return OsmTileAccess.class;
    }

    public static class Parser implements InstanceObjectParser<RegionPopulator<?>> {
        @Override
        public RegionPopulator<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            List<DataSampler<OsmTileAccess>> samplers = valueParser.parseSamplers(objectRoot, "samplers", OsmTileAccess.class);
            return new OverpassRegionPopulator(samplers);
        }
    }
}
