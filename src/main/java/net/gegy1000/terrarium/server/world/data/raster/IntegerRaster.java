package net.gegy1000.terrarium.server.world.data.raster;

public interface IntegerRaster<T> extends NumberRaster<T> {
    void setInt(int x, int y, int value);

    int getInt(int x, int y);

    default void copyInto(IntegerRaster<?> raster) {
        for (int y = 0; y < this.getHeight(); y++) {
            for (int x = 0; x < this.getWidth(); x++) {
                raster.setInt(x, y, this.getInt(x, y));
            }
        }
    }
}
