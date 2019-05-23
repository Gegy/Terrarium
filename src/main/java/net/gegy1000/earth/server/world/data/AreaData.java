package net.gegy1000.earth.server.world.data;

import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.MergableData;

import java.awt.geom.Area;

public final class AreaData implements Data, MergableData<AreaData> {
    public static final AreaData EMPTY = new AreaData();

    private final Area area;

    public AreaData(Area area) {
        this.area = area;
    }

    public AreaData() {
        this(new Area());
    }

    public Area getArea() {
        return this.area;
    }

    @Override
    public AreaData copy() {
        return new AreaData(this.area);
    }

    @Override
    public AreaData merge(AreaData other) {
        Area area = new Area();
        area.add(this.area);
        area.add(other.area);
        return new AreaData(area);
    }
}
