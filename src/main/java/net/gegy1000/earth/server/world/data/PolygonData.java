package net.gegy1000.earth.server.world.data;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class PolygonData implements Data {
    public static final PolygonData EMPTY = new PolygonData(Collections.emptyList());

    private final Collection<MultiPolygon> polygons;

    public PolygonData(Collection<MultiPolygon> polygons) {
        this.polygons = polygons;
    }

    public Collection<MultiPolygon> getPolygons() {
        return this.polygons;
    }

    @Override
    public PolygonData copy() {
        return new PolygonData(new ArrayList<>(this.polygons));
    }
}
