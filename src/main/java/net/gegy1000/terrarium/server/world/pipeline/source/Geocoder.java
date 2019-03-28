package net.gegy1000.terrarium.server.world.pipeline.source;

import javax.vecmath.Vector2d;
import java.io.IOException;

public interface Geocoder {
    Geocoder VOID = new Geocoder() {
        @Override
        public Vector2d get(String place) {
            return null;
        }

        @Override
        public String[] suggest(String place) {
            return new String[0];
        }
    };

    Vector2d get(String place) throws IOException;

    String[] suggest(String place) throws IOException;
}
