package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class OceanFillComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTileAccess> heightComponent;
    private final IBlockState block;
    private final int oceanLevel;

    public OceanFillComposer(RegionComponentType<ShortRasterTileAccess> heightComponent, IBlockState block, int oceanLevel) {
        this.heightComponent = heightComponent;
        this.block = block;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void provideSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        ShortRasterTileAccess heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightRaster.getShort(localX, localZ);
                if (height < this.oceanLevel) {
                    for (int localY = height + 1; localY <= this.oceanLevel; localY++) {
                        primer.setBlockState(localX, localY, localZ, this.block);
                    }
                }
            }
        }
    }

    public static class Parser implements InstanceObjectParser<SurfaceComposer> {
        @Override
        public SurfaceComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            IBlockState block = valueParser.parseBlockState(objectRoot, "block");
            int level = valueParser.parseInteger(objectRoot, "level");
            return new OceanFillComposer(heightComponent, block, level);
        }
    }
}
