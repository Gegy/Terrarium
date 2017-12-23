package net.gegy1000.terrarium.server.util;

import java.util.Arrays;

public class ArrayUtils {
    public static <T> T[] defaulted(T[] array, T defaultEntry) {
        Arrays.fill(array, defaultEntry);
        return array;
    }
}
