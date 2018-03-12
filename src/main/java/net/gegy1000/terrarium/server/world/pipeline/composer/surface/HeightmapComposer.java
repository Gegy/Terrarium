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

public class HeightmapComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTileAccess> heightComponent;
    private final IBlockState block;

    private final ShortRasterTileAccess chunkBuffer = new ShortRasterTileAccess(new short[16 * 16], 16, 16);

    public HeightmapComposer(RegionComponentType<ShortRasterTileAccess> heightComponent, IBlockState block) {
        this.heightComponent = heightComponent;
        this.block = block;
    }

    @Override
    public void provideSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        regionHandler.fillRaster(this.heightComponent, this.chunkBuffer, globalX, globalZ, 16, 16);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = this.chunkBuffer.getShort(localX, localZ);
                for (int localY = 1; localY <= height; localY++) {
                    primer.setBlockState(localX, localY, localZ, this.block);
                }
            }
        }
    }

    public static class Parser implements InstanceObjectParser<SurfaceComposer> {
        @Override
        public SurfaceComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            IBlockState block = valueParser.parseBlockState(objectRoot, "block");
            return new HeightmapComposer(heightComponent, block);
        }
    }
}
