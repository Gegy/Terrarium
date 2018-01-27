package net.gegy1000.terrarium.server.map.source.osm;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class GeneralOverpassSource extends OverpassSource {
    private static final String QUERY_LOCATION = "/assets/terrarium/query/general_overpass_query.oql";
    private static final int QUERY_VERSION = 2;

    public GeneralOverpassSource(EarthGenerationSettings settings) {
        super(settings, 0.1, "general", QUERY_LOCATION, QUERY_VERSION);
    }
}
