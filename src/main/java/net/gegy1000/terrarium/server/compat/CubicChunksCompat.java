package net.gegy1000.terrarium.server.compat;

import net.minecraftforge.fml.common.Loader;

public class CubicChunksCompat {
    private static Proxy constructed;

    public static Proxy get() {
        if (constructed == null) {
            constructed = Loader.isModLoaded("cubicchunks") ? new Present() : new Absent();
        }
        return constructed;
    }

    public interface Proxy {
    }

    public static class Absent implements Proxy {
    }

    public static class Present implements Proxy {
    }
}
