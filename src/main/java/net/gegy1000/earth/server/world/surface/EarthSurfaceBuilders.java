package net.gegy1000.earth.server.world.surface;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.surface.DynamicSurfaceBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public class EarthSurfaceBuilders {
    public static final DynamicSurfaceBuilder DYNAMIC = register("dynamic", new DynamicSurfaceBuilder());

    public static final PatchedSurfaceConfig BARE_CONFIG = PatchedSurfaceConfig.builder()
            .withEntry(SurfaceBuilder.GRAVEL_CONFIG, 2)
            .withEntry(SurfaceBuilder.DIRT_CONFIG, 10)
            .withEntry(SurfaceBuilder.SAND_CONFIG, 5)
            .build();

    public static final TernarySurfaceConfig SNOW_CONFIG = new TernarySurfaceConfig(
            Blocks.SNOW_BLOCK.getDefaultState(),
            Blocks.SNOW_BLOCK.getDefaultState(),
            Blocks.SNOW_BLOCK.getDefaultState()
    );

    private static <T extends SurfaceBuilder<?>> T register(String id, T builder) {
        return Registry.register(Registry.SURFACE_BUILDER, new Identifier(TerrariumEarth.MODID, id), builder);
    }
}
