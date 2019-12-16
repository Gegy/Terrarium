package net.gegy1000.earth.server.util;

import java.io.IOException;

public interface IoSupplier<T> {
    T get() throws IOException;
}
