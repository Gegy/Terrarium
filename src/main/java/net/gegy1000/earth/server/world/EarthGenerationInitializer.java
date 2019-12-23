package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.capability.HeightmapStore;
import net.gegy1000.earth.server.world.composer.BoulderDecorationComposer;
import net.gegy1000.earth.server.world.composer.CoverDecorationComposer;
import net.gegy1000.earth.server.world.composer.EarthBiomeComposer;
import net.gegy1000.earth.server.world.composer.EarthCarvingComposer;
import net.gegy1000.earth.server.world.composer.FreezeSurfaceComposer;
import net.gegy1000.earth.server.world.composer.OreDecorationComposer;
import net.gegy1000.earth.server.world.composer.SoilSurfaceComposer;
import net.gegy1000.earth.server.world.composer.WaterFillSurfaceComposer;
import net.gegy1000.earth.server.world.ores.VanillaOres;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.gengen.core.GenGen;
import net.gegy1000.gengen.util.primer.GenericCavePrimer;
import net.gegy1000.gengen.util.primer.GenericRavinePrimer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.composer.decoration.VanillaEntitySpawnComposer;
import net.gegy1000.terrarium.server.world.composer.structure.VanillaStructureComposer;
import net.gegy1000.terrarium.server.world.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.composer.surface.GenericSurfaceComposer;
import net.gegy1000.terrarium.server.world.composer.surface.HeightmapSurfaceComposer;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.CompositeTerrariumGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthGenerationInitializer implements TerrariumGeneratorInitializer {
    private final EarthInitContext ctx;

    EarthGenerationInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void setup(CompositeTerrariumGenerator.Builder builder, boolean preview) {
        this.addSurfaceComposers(builder, preview);
        this.addDecorationComposers(preview, builder);

        builder.setBiomeComposer(new EarthBiomeComposer());
        builder.setSpawnPosition(new Coordinate(this.ctx.lngLatCrs, this.ctx.settings.getDouble(SPAWN_LONGITUDE), this.ctx.settings.getDouble(SPAWN_LATITUDE)));
    }

    private void addSurfaceComposers(CompositeTerrariumGenerator.Builder builder, boolean preview) {
        World world = this.ctx.world;
        int heightOffset = this.ctx.settings.getInteger(HEIGHT_OFFSET);
        HeightFunction surfaceFunction = HeightmapStore.global(world, heightOffset);

        builder.addSurfaceComposer(new HeightmapSurfaceComposer(EarthDataKeys.TERRAIN_HEIGHT, Blocks.STONE.getDefaultState()));
        builder.addSurfaceComposer(new WaterFillSurfaceComposer(EarthDataKeys.TERRAIN_HEIGHT, EarthDataKeys.LANDFORM, EarthDataKeys.WATER_LEVEL, Blocks.WATER.getDefaultState()));
        builder.addSurfaceComposer(new SoilSurfaceComposer(world, EarthDataKeys.TERRAIN_HEIGHT, EarthDataKeys.SLOPE, Blocks.STONE.getDefaultState()));

        if (preview) return;

        if (this.ctx.settings.getBoolean(ENABLE_DECORATION)) {
            builder.addSurfaceComposer(new EarthCarvingComposer(EarthDataKeys.COVER));
        }

        if (this.ctx.settings.get(CAVE_GENERATION)) {
            builder.addSurfaceComposer(GenericSurfaceComposer.of(new GenericCavePrimer(world)));
        }

        if (this.ctx.settings.get(RAVINE_GENERATION)) {
            builder.addSurfaceComposer(GenericSurfaceComposer.of(new GenericRavinePrimer(world, surfaceFunction)));
        }

        if (!GenGen.isCubic(world)) {
            builder.addSurfaceComposer(new BedrockSurfaceComposer(world, Blocks.BEDROCK.getDefaultState(), Math.min(heightOffset - 1, 5)));
        }
    }

    private void addDecorationComposers(boolean preview, CompositeTerrariumGenerator.Builder builder) {
        if (this.ctx.settings.getBoolean(ENABLE_DECORATION)) {
            builder.addDecorationComposer(new CoverDecorationComposer(this.ctx.world, EarthDataKeys.COVER));

            // TODO: More decorators such as this
            builder.addDecorationComposer(new BoulderDecorationComposer(this.ctx.world, EarthDataKeys.SLOPE));
        }

        if (this.ctx.settings.getBoolean(ORE_GENERATION)) {
            OreDecorationComposer oreComposer = new OreDecorationComposer(this.ctx.world, EarthDataKeys.TERRAIN_HEIGHT);
            VanillaOres.addTo(oreComposer);

            builder.addDecorationComposer(oreComposer);
        }

        builder.addDecorationComposer(new FreezeSurfaceComposer(this.ctx.world, EarthDataKeys.SLOPE));
        builder.addDecorationComposer(new VanillaEntitySpawnComposer(this.ctx.world));

        if (!preview) {
            builder.addStructureComposer(new VanillaStructureComposer(this.ctx.world));
        }
    }
}
