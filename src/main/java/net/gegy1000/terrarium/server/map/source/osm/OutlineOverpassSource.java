package net.gegy1000.terrarium.server.map.source.osm;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class OutlineOverpassSource extends OverpassSource {
    private static final String QUERY_LOCATION = "/assets/terrarium/query/outline_overpass_query.oql";
    private static final int QUERY_VERSION = 1;

    public OutlineOverpassSource(EarthGenerationSettings settings) {
        super(settings, 0.3, "outline", QUERY_LOCATION, QUERY_VERSION);
    }
}
