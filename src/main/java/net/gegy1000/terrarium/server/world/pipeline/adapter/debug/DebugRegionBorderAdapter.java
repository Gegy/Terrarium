package net.gegy1000.terrarium.server.world.pipeline.adapter.debug;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;

public class DebugRegionBorderAdapter implements RegionAdapter {
    private final RegionComponentType<CoverRasterTileAccess> coverComponent;

    public DebugRegionBorderAdapter(RegionComponentType<CoverRasterTileAccess> coverComponent) {
        this.coverComponent = coverComponent;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        CoverRasterTileAccess coverTile = data.getOrExcept(this.coverComponent);

        CoverType[] cover = coverTile.getData();

        int minX = GenerationRegion.BUFFER;
        int minZ = GenerationRegion.BUFFER;
        int maxX = width - GenerationRegion.BUFFER;
        int maxZ = height - GenerationRegion.BUFFER;
        for (int localZ = minZ; localZ < maxZ; localZ++) {
            for (int localX = minX; localX < maxX; localX++) {
                if (localX == minX || localX == maxX - 1 || localZ == minZ || localZ == maxZ - 1) {
                    cover[localX + localZ * width] = CoverRegistry.DEBUG;
                }
            }
        }
    }

    public static class Parser implements InstanceObjectParser<RegionAdapter> {
        @Override
        public RegionAdapter parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            return new DebugRegionBorderAdapter(valueParser.parseComponentType(objectRoot, "component", CoverRasterTileAccess.class));
        }
    }
}
