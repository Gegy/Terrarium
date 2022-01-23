package net.gegy1000.earth.server.world.composer.surface;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.layer.SelectionSeedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;

public final class FloodedSurfaceComposer implements SurfaceComposer {
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();

    private final GenLayer water;

    public FloodedSurfaceComposer() {
        GenLayer water = new SelectionSeedLayer(2, 2);
        water = new GenLayerFuzzyZoom(11000, water);
        water = new GenLayerVoronoiZoom(12000, water);
        water = new OutlineEdgeLayer(3, 13000, water);
        water = new GenLayerZoom(14000, water);

        this.water = water;
        this.water.initWorldGenSeed(0);
    }

    @Override
    public void composeSurface(TerrariumWorld terrarium, DataSample data, CubicPos pos, ChunkPrimeWriter writer) {
        data.with(EarthData.TERRAIN_HEIGHT, EarthData.COVER).ifPresent(with -> {
            ShortRaster heightRaster = with.get(EarthData.TERRAIN_HEIGHT);
            EnumRaster<Cover> coverRaster = with.get(EarthData.COVER);

            Cover cover = coverRaster.get(8, 8);
            if (cover.is(CoverMarkers.FLOODED)) {
                IntCache.resetIntCache();
                int[] sampledWater = this.water.getInts(pos.getMinX(), pos.getMinZ(), 16, 16);

                int minY = pos.getMinY();
                int maxY = pos.getMaxY();

                heightRaster.iterate((height, x, z) -> {
                    if (height >= minY && height <= maxY) {
                        int waterValue = sampledWater[x + z * 16];
                        if (waterValue == 3) {
                            writer.set(x, height & 0xF, z, WATER);
                        }
                    }
                });
            }
        });
    }
}
