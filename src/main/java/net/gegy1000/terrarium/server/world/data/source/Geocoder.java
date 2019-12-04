package net.gegy1000.terrarium.server.world.data.source;

import javax.annotation.Nullable;
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
            return null;
        }
    };

    Vector2d get(String place) throws IOException;

    @Nullable
    String[] suggest(String place) throws IOException;
}
