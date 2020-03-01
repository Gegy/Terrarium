package net.gegy1000.earth.server.world.ecology.soil;

public interface SoilConfig {
    static SoilConfig unary(SoilLayer layer) {
        return depth -> layer;
    }

    static SoilConfig binary(SoilLayer surface, SoilLayer subsurface) {
        return depth -> depth == 0 ? surface : subsurface;
    }

    SoilLayer forDepth(int depth);
}
