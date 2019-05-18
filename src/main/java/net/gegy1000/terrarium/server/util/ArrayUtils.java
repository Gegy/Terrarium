package net.gegy1000.terrarium.server.util;

import java.util.Arrays;

public class ArrayUtils {
    public static <T> T[] fill(T[] array, T with) {
        Arrays.fill(array, with);
        return array;
    }
}
