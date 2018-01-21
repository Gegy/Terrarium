package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;

public class RegionData {
    private final short[] heights;
    private final CoverType[] cover;
    private final OverpassTileAccess overpassTile;

    public RegionData(short[] heights, CoverType[] cover, OverpassTileAccess overpassTile) {
        this.heights = heights;
        this.cover = cover;
        this.overpassTile = overpassTile;
    }

    public short[] getHeights() {
        return this.heights;
    }

    public CoverType[] getCover() {
        return this.cover;
    }

    public OverpassTileAccess getOverpassTile() {
        return this.overpassTile;
    }
}
