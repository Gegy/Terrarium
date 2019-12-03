package net.gegy1000.earth.server.world.data.source.cache;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TileCache<T> {
    @Nullable
    InputStream in(T key) throws IOException;

    @Nullable
    OutputStream out(T key) throws IOException;

    void delete(T key) throws IOException;
}
