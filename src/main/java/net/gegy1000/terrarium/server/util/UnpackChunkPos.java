package net.gegy1000.terrarium.server.util;

import net.minecraft.util.math.ChunkPos;

public final class UnpackChunkPos {
    private static final long MASK = 0xFFFFFFFFL;

    public static ChunkPos unpack(long key) {
        return new ChunkPos(unpackX(key), unpackZ(key));
    }

    public static int unpackX(long key) {
        return (int) (key & MASK);
    }

    public static int unpackZ(long key) {
        return (int) (key >> 32 & MASK);
    }
}
