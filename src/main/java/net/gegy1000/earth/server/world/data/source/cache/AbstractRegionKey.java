package net.gegy1000.earth.server.world.data.source.cache;

import cubicchunks.regionlib.api.region.key.IKey;
import cubicchunks.regionlib.api.region.key.IKeyProvider;
import cubicchunks.regionlib.api.region.key.RegionKey;

public abstract class AbstractRegionKey<T extends IKey<T>> implements IKey<T> {
    private final int x;
    private final int z;

    protected AbstractRegionKey(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public static int bitmask(int bits) {
        return (1 << bits) - 1;
    }

    public static int entriesPerRegion(int bits) {
        return (1 << bits) * (1 << bits);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractRegionKey) {
            AbstractRegionKey key = (AbstractRegionKey) obj;
            return this.x == key.x && this.z == key.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.z;
        return result;
    }

    @Override
    public RegionKey getRegionKey() {
        int bits = this.bits();
        int regX = this.x >> bits;
        int regZ = this.z >> bits;
        return new RegionKey(regX + "." + regZ);
    }

    @Override
    public int getId() {
        int bits = this.bits();
        int bitmask = bitmask(bits);
        return ((this.x & bitmask) << bits) | (this.z & bits);
    }

    @Override
    public String toString() {
        return "RegionKey{x=" + this.x + ", z=" + this.z + "}";
    }

    protected abstract int bits();

    public static abstract class Provider<T extends AbstractRegionKey<T>> implements IKeyProvider<T> {
        @Override
        public T fromRegionAndId(RegionKey regionKey, int id) throws IllegalArgumentException {
            if (!this.isValid(regionKey)) throw new IllegalArgumentException("Invalid name " + regionKey.getName());

            String[] values = regionKey.getName().split("\\.");

            int bits = this.bits();
            int bitmask = bitmask(bits);

            int relativeX = id >>> bits;
            int relativeZ = id & bitmask;
            return this.create(
                    Integer.parseInt(values[0]) << bits | relativeX,
                    Integer.parseInt(values[1]) << bits | relativeZ
            );
        }

        @Override
        public int getKeyCount(RegionKey key) {
            return entriesPerRegion(this.bits());
        }

        @Override
        public boolean isValid(RegionKey key) {
            return key.getName().matches("-?\\d+\\.-?\\d+");
        }

        protected abstract T create(int x, int z);

        protected abstract int bits();
    }
}
