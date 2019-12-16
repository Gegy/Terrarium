package net.gegy1000.earth.server.util;

import java.io.IOException;

public interface IoFunction<T, R> {
    R apply(T key) throws IOException;
}

