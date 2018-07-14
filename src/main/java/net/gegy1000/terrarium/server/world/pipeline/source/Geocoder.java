package net.gegy1000.terrarium.server.world.pipeline.source;

import javax.vecmath.Vector2d;
import java.io.IOException;
import java.util.List;

public interface Geocoder {
    Vector2d get(String place) throws IOException;

    String[] suggest(String place) throws IOException;
}
