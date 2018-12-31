package net.gegy1000.earth.server.world.surface;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class EarthSurfaceBuilders {
    public static final BareSurfaceBuilder BARE = register("bare", new BareSurfaceBuilder());

    private static <T extends SurfaceBuilder<?>> T register(String id, T builder) {
        return Registry.register(Registry.SURFACE_BUILDER, new Identifier(TerrariumEarth.MODID, id), builder);
    }
}
