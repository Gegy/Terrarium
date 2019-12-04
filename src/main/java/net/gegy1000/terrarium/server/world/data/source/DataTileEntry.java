package net.gegy1000.terrarium.server.world.data.source;

public final class DataTileEntry<T> {
    private final DataTilePos pos;
    private final T data;

    public DataTileEntry(DataTilePos pos, T data) {
        this.pos = pos;
        this.data = data;
    }

    public DataTilePos getPos() {
        return this.pos;
    }

    public T getData() {
        return this.data;
    }
}
