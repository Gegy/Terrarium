package net.gegy1000.earth.server.world.data;

import net.gegy1000.terrarium.server.world.pipeline.data.Data;

import java.awt.geom.Area;

public final class AreaData implements Data {
    public static final AreaData EMPTY = new AreaData(new Area());

    private final Area area;

    public AreaData(Area area) {
        this.area = area;
    }

    public Area getArea() {
        return this.area;
    }

    @Override
    public AreaData copy() {
        return new AreaData(this.area);
    }
}
