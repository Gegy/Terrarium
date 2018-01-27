package net.gegy1000.terrarium.server.map.system.populator;

import com.google.common.collect.ImmutableList;
import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.map.system.sampler.DataSampler;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class OverpassRegionPopulator implements RegionPopulator<OverpassTileAccess> {
    private final ImmutableList<DataSampler<OverpassTileAccess>> overpassSamplers;

    public OverpassRegionPopulator(List<DataSampler<OverpassTileAccess>> overpassSamplers) {
        this.overpassSamplers = ImmutableList.copyOf(overpassSamplers);
    }

    @Override
    public OverpassTileAccess populate(EarthGenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        Coordinate minRegionCoordinate = pos.getMinBufferedCoordinate(settings);
        Coordinate maxRegionCoordinate = pos.getMaxBufferedCoordinate(settings);

        int minSampleX = MathHelper.floor(minRegionCoordinate.getBlockX()) - 8;
        int minSampleZ = MathHelper.floor(minRegionCoordinate.getBlockZ()) - 8;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getBlockX()) + 8;
        int maxSampleZ = MathHelper.ceil(maxRegionCoordinate.getBlockZ()) + 8;

        int sampleWidth = maxSampleX - minSampleX;
        int sampleHeight = maxSampleZ - minSampleZ;

        OverpassTileAccess tile = null;
        for (DataSampler<OverpassTileAccess> sampler : this.overpassSamplers) {
            if (sampler.shouldSample()) {
                OverpassTileAccess sampled = sampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight);
                if (tile == null) {
                    tile = sampled;
                } else {
                    tile = tile.merge(sampled);
                }
            }
        }

        return tile != null ? tile : new OverpassTileAccess();
    }
}
