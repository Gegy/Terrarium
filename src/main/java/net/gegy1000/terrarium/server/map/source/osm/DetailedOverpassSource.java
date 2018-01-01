package net.gegy1000.terrarium.server.map.source.osm;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class DetailedOverpassSource extends OverpassSource {
    private static final String QUERY_LOCATION = "/assets/terrarium/query/detail_overpass_query.oql";
    private static final int QUERY_VERSION = 3;

    public DetailedOverpassSource(EarthGenerationSettings settings) {
        super(settings, 0.05, "detailed", QUERY_LOCATION, QUERY_VERSION);
    }
}
