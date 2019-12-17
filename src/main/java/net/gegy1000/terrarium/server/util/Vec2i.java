package net.gegy1000.terrarium.server.util;

public final class Vec2i {
    public final int x;
    public final int y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vec2i{" + this.x + ";" + this.y + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Vec2i) {
            Vec2i vec = (Vec2i) obj;
            return this.x == vec.x && this.y == vec.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * this.x + this.y;
    }

    public static Vec2i min(Vec2i left, Vec2i right) {
        return new Vec2i(Math.min(left.x, right.x), Math.min(left.y, right.y));
    }

    public static Vec2i max(Vec2i left, Vec2i right) {
        return new Vec2i(Math.max(left.x, right.x), Math.max(left.y, right.y));
    }
}
