package net.gegy1000.earth.server.world.pipeline;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.minecraft.util.ResourceLocation;

public class EarthComponentTypes {
    public static final RegionComponentType<OsmTile> OSM = new RegionComponentType<OsmTile>(new ResourceLocation(TerrariumEarth.MODID, "osm"), OsmTile.class) {
        @Override
        public OsmTile createDefaultData(int width, int height) {
            return new OsmTile();
        }
    };

    public static final RegionComponentType<WaterRasterTile> WATER = new RegionComponentType<WaterRasterTile>(new ResourceLocation(TerrariumEarth.MODID, "water"), WaterRasterTile.class) {
        @Override
        public WaterRasterTile createDefaultData(int width, int height) {
            return new WaterRasterTile(new short[width * height], width, height);
        }
    };
}
