package net.gegy1000.terrarium.server.map.adapter;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public interface RegionAdapter {
    void adaptGlobcover(EarthGenerationSettings settings, OverpassTileAccess overpassTile, GlobType[] globBuffer, int x, int z, int width, int height);
}
