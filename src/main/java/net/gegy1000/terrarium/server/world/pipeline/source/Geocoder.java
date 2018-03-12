package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

import java.io.IOException;

public interface Geocoder {
    Coordinate get(String place) throws IOException;
}
