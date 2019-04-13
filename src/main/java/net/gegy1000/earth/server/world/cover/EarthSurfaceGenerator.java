package net.gegy1000.earth.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;

public abstract class EarthSurfaceGenerator extends CoverSurfaceGenerator<EarthCoverContext> {
    protected EarthSurfaceGenerator(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
        super(context, coverType);
    }
}
