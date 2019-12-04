package net.gegy1000.terrarium.server.world.data.raster;

public interface Raster<T> {
    int getWidth();

    int getHeight();

    T getData();

    default int index(int x, int y) {
        return x + y * this.getWidth();
    }
}
