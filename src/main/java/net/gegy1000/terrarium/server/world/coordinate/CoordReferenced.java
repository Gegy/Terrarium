package net.gegy1000.terrarium.server.world.coordinate;

public final class CoordReferenced<T> {
    public final T source;
    public final CoordinateReference crs;

    public CoordReferenced(T source, CoordinateReference crs) {
        this.source = source;
        this.crs = crs;
    }
}
