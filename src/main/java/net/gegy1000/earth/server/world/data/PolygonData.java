package net.gegy1000.earth.server.world.data;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.MergableData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PolygonData implements Data, MergableData<PolygonData> {
    public static final PolygonData EMPTY = new PolygonData(Collections.emptyList());

    private final Collection<MultiPolygon> polygons;

    public PolygonData(Collection<MultiPolygon> polygons) {
        this.polygons = polygons;
    }

    public PolygonData() {
        this(new ArrayList<>());
    }

    public Collection<MultiPolygon> getPolygons() {
        return this.polygons;
    }

    @Override
    public PolygonData copy() {
        return new PolygonData(new ArrayList<>(this.polygons));
    }

    @Override
    public PolygonData merge(PolygonData other) {
        List<MultiPolygon> merged = new ArrayList<>(this.polygons.size() + other.polygons.size());
        merged.addAll(this.polygons);
        merged.addAll(other.polygons);
        return new PolygonData(merged);
    }
}
