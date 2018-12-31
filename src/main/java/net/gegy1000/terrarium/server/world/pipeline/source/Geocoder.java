package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.server.util.Point2d;

import java.io.IOException;

public interface Geocoder {
    Point2d get(String place) throws IOException;

    String[] suggest(String place) throws IOException;
}
