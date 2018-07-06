package net.gegy1000.earth.server.world.pipeline;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;

public class EarthComponentTypes {
    public static final RegionComponentType<OsmTile> OSM = new RegionComponentType<OsmTile>(OsmTile.class) {
        @Override
        public OsmTile createDefaultData(int width, int height) {
            return new OsmTile();
        }
    };

    public static final RegionComponentType<ByteRasterTile> WATER = new RegionComponentType<ByteRasterTile>(ByteRasterTile.class) {
        @Override
        public ByteRasterTile createDefaultData(int width, int height) {
            return new ByteRasterTile(new byte[width * height], width, height);
        }
    };
}
