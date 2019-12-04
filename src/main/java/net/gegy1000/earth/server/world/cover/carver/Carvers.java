package net.gegy1000.earth.server.world.cover.carver;

import net.gegy1000.terrarium.server.world.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

public final class Carvers {
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();

    public static CoverCarver flooded(DataKey<ShortRaster> heightKey) {
        GenLayer water = new SelectionSeedLayer(2, 2);
        water = new GenLayerFuzzyZoom(11000, water);
        water = new GenLayerVoronoiZoom(12000, water);
        water = new OutlineEdgeLayer(3, 13000, water);
        water = new GenLayerZoom(14000, water);

        GenLayer waterLayer = water;
        waterLayer.initWorldGenSeed(0);

        return (cubicPos, writer, rasters) -> {
            rasters.get(heightKey).ifPresent(heightRaster -> {
                int[] sampledWater = CoverCarver.sampleChunk(cubicPos, waterLayer);

                int minY = cubicPos.getMinY();
                int maxY = cubicPos.getMaxY();

                heightRaster.iterate((height, x, z) -> {
                    if (height >= minY && height <= maxY) {
                        int waterValue = sampledWater[x + z * 16];
                        if (waterValue == 3) {
                            writer.set(x, height & 0xF, z, WATER);
                        }
                    }
                });
            });
        };
    }
}
