package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverType;

import java.awt.Color;

public abstract class EarthCoverType implements CoverType<EarthCoverContext> {
    private final Color approximateColor;

    protected EarthCoverType(Color approximateColor) {
        this.approximateColor = approximateColor;
    }

    @Override
    public Color getApproximateColor() {
        return this.approximateColor;
    }
}
