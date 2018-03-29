package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverType;

public abstract class EarthCoverType implements CoverType<EarthCoverContext> {
    @Override
    public Class<EarthCoverContext> getRequiredContext() {
        return EarthCoverContext.class;
    }
}
