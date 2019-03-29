package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.server.world.pipeline.data.Data;

public final class DataTileEntry<T extends Data> {
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
