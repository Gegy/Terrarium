package net.gegy1000.earth.server.world.pipeline;

import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;

public class EarthComponentTypes {
    public static final RegionComponentType<OsmTile> OSM = new RegionComponentType<OsmTile>(OsmTile.class) {
        @Override
        public OsmTile createDefaultData(int width, int height) {
            return new OsmTile();
        }
    };
}
