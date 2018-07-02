package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.generator.PlaceholderCover;

public class TerrariumCoverTypes {
    public static final CoverType<CoverGenerationContext> PLACEHOLDER = new PlaceholderCover();
    public static final CoverType<CoverGenerationContext> DEBUG = new PlaceholderCover();
}
