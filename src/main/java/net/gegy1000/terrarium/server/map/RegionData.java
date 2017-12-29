package net.gegy1000.terrarium.server.map;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;

public class RegionData {
    private final short[] heights;
    private final GlobType[] globcover;
    private final OverpassTileAccess overpassTile;

    public RegionData(short[] heights, GlobType[] globcover, OverpassTileAccess overpassTile) {
        this.heights = heights;
        this.globcover = globcover;
        this.overpassTile = overpassTile;
    }

    public short[] getHeights() {
        return this.heights;
    }

    public GlobType[] getGlobcover() {
        return this.globcover;
    }

    public OverpassTileAccess getOverpassTile() {
        return this.overpassTile;
    }
}
