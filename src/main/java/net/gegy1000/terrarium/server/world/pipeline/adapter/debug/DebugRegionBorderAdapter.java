package net.gegy1000.terrarium.server.world.pipeline.adapter.debug;

import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;

public class DebugRegionBorderAdapter implements RegionAdapter {
    private final RegionComponentType<CoverRasterTile> coverComponent;

    public DebugRegionBorderAdapter(RegionComponentType<CoverRasterTile> coverComponent) {
        this.coverComponent = coverComponent;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);

        CoverType[] cover = coverTile.getData();

        int minX = GenerationRegion.BUFFER;
        int minZ = GenerationRegion.BUFFER;
        int maxX = width - GenerationRegion.BUFFER;
        int maxZ = height - GenerationRegion.BUFFER;
        for (int localZ = minZ; localZ < maxZ; localZ++) {
            for (int localX = minX; localX < maxX; localX++) {
                if (localX == minX || localX == maxX - 1 || localZ == minZ || localZ == maxZ - 1) {
                    cover[localX + localZ * width] = TerrariumCoverTypes.DEBUG;
                }
            }
        }
    }
}
