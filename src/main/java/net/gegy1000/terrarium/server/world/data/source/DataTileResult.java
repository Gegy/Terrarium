package net.gegy1000.terrarium.server.world.data.source;

import net.gegy1000.terrarium.server.util.Vec2i;

import java.util.Optional;

public final class DataTileResult<T> {
    public final Vec2i pos;
    public final Optional<T> data;

    public DataTileResult(Vec2i pos, Optional<T> data) {
        this.pos = pos;
        this.data = data;
    }

    public static <T> DataTileResult<T> of(Vec2i pos, T data) {
        return new DataTileResult<>(pos, Optional.of(data));
    }

    public static <T> DataTileResult<T> empty(Vec2i pos) {
        return new DataTileResult<>(pos, Optional.empty());
    }
}
