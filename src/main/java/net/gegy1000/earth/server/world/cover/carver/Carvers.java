package net.gegy1000.earth.server.world.cover.carver;

import net.gegy1000.terrarium.server.world.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public final class Carvers {
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();

    public static CoverCarver flooded(RegionComponentType<ShortRaster> heightComponent) {
        GenLayer water = new SelectionSeedLayer(2, 2);
        water = new GenLayerFuzzyZoom(11000, water);
        water = new GenLayerVoronoiZoom(12000, water);
        water = new OutlineEdgeLayer(3, 13000, water);
        water = new GenLayerZoom(14000, water);

        GenLayer waterLayer = water;
        waterLayer.initWorldGenSeed(0);

        return (cubicPos, writer, rasters) -> {
            int[] sampledWater = CoverCarver.sampleChunk(cubicPos, waterLayer);
            ShortRaster heightRaster = rasters.get(heightComponent);

            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    int waterValue = sampledWater[localX + localZ * 16];
                    if (waterValue == 3) {
                        short height = heightRaster.getShort(localX, localZ);
                        writer.set(localX, height, localZ, WATER);
                    }
                }
            }
        };
    }
}
