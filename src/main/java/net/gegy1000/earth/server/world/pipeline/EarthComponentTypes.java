package net.gegy1000.earth.server.world.pipeline;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmData;
import net.gegy1000.earth.server.world.pipeline.source.tile.SoilRaster;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.util.ResourceLocation;

public class EarthComponentTypes {
    public static final RegionComponentType<OsmData> OSM = new RegionComponentType<OsmData>(new ResourceLocation(TerrariumEarth.MODID, "osm"), OsmData.class) {
        @Override
        public OsmData createDefaultData(int width, int height) {
            return new OsmData();
        }
    };

    public static final RegionComponentType<WaterRaster> WATER = new RegionComponentType<WaterRaster>(new ResourceLocation(TerrariumEarth.MODID, "water"), WaterRaster.class) {
        @Override
        public WaterRaster createDefaultData(int width, int height) {
            return new WaterRaster(new short[width * height], width, height);
        }
    };

    public static final RegionComponentType<SoilRaster> SOIL = new RegionComponentType<SoilRaster>(new ResourceLocation(TerrariumEarth.MODID, "soil"), SoilRaster.class) {
        @Override
        public SoilRaster createDefaultData(int width, int height) {
            return new SoilRaster(width, height);
        }
    };

    public static final RegionComponentType<FloatRaster> AVERAGE_TEMPERATURE = new RegionComponentType<FloatRaster>(new ResourceLocation(TerrariumEarth.MODID, "temperature"), FloatRaster.class) {
        @Override
        public FloatRaster createDefaultData(int width, int height) {
            return new FloatRaster(width, height);
        }
    };

    public static final RegionComponentType<ShortRaster> ANNUAL_RAINFALL = new RegionComponentType<ShortRaster>(new ResourceLocation(TerrariumEarth.MODID, "rainfall"), ShortRaster.class) {
        @Override
        public ShortRaster createDefaultData(int width, int height) {
            return new ShortRaster(width, height);
        }
    };
}
