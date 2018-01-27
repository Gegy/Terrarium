package net.gegy1000.terrarium.server.map.adapter.debug;

import net.gegy1000.terrarium.server.map.GenerationRegion;
import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.glob.CoverTileAccess;
import net.gegy1000.terrarium.server.map.system.component.TerrariumComponentTypes;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class RegionBorderAdapter implements RegionAdapter {
    @Override
    public void adapt(EarthGenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        CoverTileAccess coverTile = data.get(TerrariumComponentTypes.COVER);

        if (coverTile == null) {
            return;
        }

        CoverType[] cover = coverTile.getData();

        int minX = GenerationRegion.BUFFER;
        int minZ = GenerationRegion.BUFFER;
        int maxX = width - GenerationRegion.BUFFER;
        int maxZ = height - GenerationRegion.BUFFER;
        for (int localZ = minZ; localZ < maxZ; localZ++) {
            for (int localX = minX; localX < maxX; localX++) {
                if (localX == minX || localX == maxX - 1 || localZ == minZ || localZ == maxZ - 1) {
                    cover[localX + localZ * width] = CoverType.DEBUG;
                }
            }
        }
    }
}
