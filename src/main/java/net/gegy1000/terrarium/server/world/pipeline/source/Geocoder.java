package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

import java.io.IOException;
import java.util.List;

public interface Geocoder {
    Coordinate get(String place) throws IOException;

    List<String> suggestCommand(String place) throws IOException;

    String[] suggest(String place) throws IOException;
}
