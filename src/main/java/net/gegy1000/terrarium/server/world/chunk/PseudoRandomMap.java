package net.gegy1000.terrarium.server.world.chunk;

import net.minecraft.world.World;

public class PseudoRandomMap {
    private static final long PRIME_1 = 6364136223846793005L;
    private static final long PRIME_2 = 1442695040888963407L;

    private final long seed;

    private long currentSeed;

    public PseudoRandomMap(long worldSeed, long localSeed) {
        this.seed = worldSeed ^ localSeed;
    }

    public PseudoRandomMap(World world, long localSeed) {
        this(world.getSeed(), localSeed);
    }

    public void initPosSeed(int x, int z) {
        this.currentSeed = this.seed;
        for (int i = 0; i < 2; i++) {
            this.currentSeed *= this.currentSeed * PRIME_1 + PRIME_2;
            this.currentSeed += x;
            this.currentSeed *= this.currentSeed * PRIME_1 + PRIME_2;
            this.currentSeed += z;
        }
    }

    public int nextInt(int bound) {
        long next = (this.next() >> 24) % bound;
        if (next < 0) {
            next += bound;
        }
        return (int) next;
    }

    public double nextDouble() {
        return (double) this.nextInt(0xFFFFFF) / 0xFFFFFF;
    }

    public long next() {
        long next = this.currentSeed;
        this.currentSeed *= this.currentSeed * PRIME_1 + PRIME_2;
        this.currentSeed += this.seed;
        return next;
    }
}
