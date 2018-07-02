package net.gegy1000.earth.server.world.pipeline.populator;

import com.google.common.collect.ImmutableList;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.util.math.MathHelper;

public class OsmRegionPopulator implements RegionPopulator<OsmTile> {
    private final ImmutableList<DataSampler<OsmTile>> samplers;

    @SafeVarargs
    public OsmRegionPopulator(DataSampler<OsmTile>... samplers) {
        this.samplers = ImmutableList.copyOf(samplers);
    }

    @Override
    public OsmTile populate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        Coordinate minRegionCoordinate = pos.getMinBufferedCoordinate();
        Coordinate maxRegionCoordinate = pos.getMaxBufferedCoordinate();

        int minSampleX = MathHelper.floor(minRegionCoordinate.getBlockX()) - 8;
        int minSampleZ = MathHelper.floor(minRegionCoordinate.getBlockZ()) - 8;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getBlockX()) + 8;
        int maxSampleZ = MathHelper.ceil(maxRegionCoordinate.getBlockZ()) + 8;

        int sampleWidth = maxSampleX - minSampleX;
        int sampleHeight = maxSampleZ - minSampleZ;

        OsmTile tile = null;
        for (DataSampler<OsmTile> sampler : this.samplers) {
            if (sampler.shouldSample()) {
                OsmTile sampled = sampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight);
                if (tile == null) {
                    tile = sampled;
                } else {
                    tile = tile.merge(sampled);
                }
            }
        }

        return tile != null ? tile : new OsmTile();
    }

    @Override
    public Class<OsmTile> getType() {
        return OsmTile.class;
    }
}
