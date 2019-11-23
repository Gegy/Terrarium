package net.gegy1000.earth.server.world.data;

import java.awt.geom.Area;

public final class AreaData {
    public static final AreaData EMPTY = new AreaData(new Area());

    private final Area area;

    public AreaData(Area area) {
        this.area = area;
    }

    public Area getArea() {
        return this.area;
    }
}
