package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.cover.EarthCoverBiomes;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.TerrariumCover;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.biome.Biome;

import java.util.LinkedList;
import java.util.List;

public class WaterApplyAdapter implements RegionAdapter {
    protected final CoordinateState geoCoordinateState;
    protected final RegionComponentType<WaterRasterTile> waterComponent;
    protected final RegionComponentType<ShortRasterTile> heightComponent;
    protected final RegionComponentType<BiomeRasterTile> biomeComponent;

    public WaterApplyAdapter(CoordinateState geoCoordinateState, RegionComponentType<WaterRasterTile> waterComponent, RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.geoCoordinateState = geoCoordinateState;
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.biomeComponent = biomeComponent;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        short[] heightBuffer = data.getOrExcept(this.heightComponent).getShortData();
        Biome[] biomeBuffer = data.getOrExcept(this.biomeComponent).getData();
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);

        List<FloodFill.Point> unselectedPoints = new LinkedList<>();
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int sampleType = waterTile.getWaterType(localX, localY);
                if (WaterRasterTile.isWater(sampleType)) {
                    biomeBuffer[index] = EarthCoverBiomes.WATER;
                } else {
                    Biome currentBiome = biomeBuffer[index];
                    if (currentBiome == EarthCoverBiomes.WATER) {
                        biomeBuffer[index] = TerrariumCover.NONE;
                        unselectedPoints.add(new FloodFill.Point(localX, localY));
                        heightBuffer[index] = (short) Math.max(1, heightBuffer[index]);
                    }
                }
            }
        }

        for (FloodFill.Point point : unselectedPoints) {
            CoverSelectVisitor visitor = new CoverSelectVisitor();
            FloodFill.floodVisit(biomeBuffer, width, height, point, visitor);
            biomeBuffer[point.getX() + point.getY() * width] = visitor.getResult();
        }
    }

    protected class CoverSelectVisitor implements FloodFill.Visitor<Biome> {
        protected Biome result = null;

        @Override
        public Biome visit(FloodFill.Point point, Biome sampled) {
            if (sampled != TerrariumCover.NONE) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, Biome sampled) {
            return sampled != EarthCoverBiomes.WATER;
        }

        public Biome getResult() {
            if (this.result == null) {
                return EarthCoverBiomes.RAINFED_CROPS;
            }
            return this.result;
        }
    }
}
