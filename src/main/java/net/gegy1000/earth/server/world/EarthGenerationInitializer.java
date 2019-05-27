package net.gegy1000.earth.server.world;

import net.gegy1000.cubicglue.CubicGlue;
import net.gegy1000.earth.server.world.composer.BoulderDecorationComposer;
import net.gegy1000.earth.server.world.composer.EarthBiomeComposer;
import net.gegy1000.earth.server.world.composer.EarthCarvingComposer;
import net.gegy1000.earth.server.world.composer.EarthDecorationComposer;
import net.gegy1000.earth.server.world.composer.IceCoverComposer;
import net.gegy1000.earth.server.world.composer.SoilSurfaceComposer;
import net.gegy1000.earth.server.world.composer.WaterFillSurfaceComposer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.CompositeTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.VanillaEntitySpawnComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.structure.VanillaStructureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CaveSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapSurfaceComposer;
import net.minecraft.init.Blocks;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthGenerationInitializer implements TerrariumGeneratorInitializer {
    private final EarthInitContext ctx;

    EarthGenerationInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public TerrariumGenerator buildGenerator(boolean preview) {
        CompositeTerrariumGenerator.Builder builder = CompositeTerrariumGenerator.builder();

        this.addSurfaceComposers(builder, preview);
        this.addDecorationComposers(preview, builder);

        builder.setBiomeComposer(new EarthBiomeComposer(EarthDataKeys.COVER, EarthDataKeys.LANDFORM, EarthDataKeys.AVERAGE_TEMPERATURE, EarthDataKeys.MONTHLY_RAINFALL));
        builder.setSpawnPosition(new Coordinate(this.ctx.latLngCoordinates, this.ctx.settings.getDouble(SPAWN_LATITUDE), this.ctx.settings.getDouble(SPAWN_LONGITUDE)));

        return builder.build();
    }

    private void addSurfaceComposers(CompositeTerrariumGenerator.Builder builder, boolean preview) {
        int heightOrigin = this.ctx.settings.getInteger(HEIGHT_ORIGIN);

        builder.addSurfaceComposer(new HeightmapSurfaceComposer(EarthDataKeys.HEIGHT, Blocks.STONE.getDefaultState()));
        builder.addSurfaceComposer(new WaterFillSurfaceComposer(EarthDataKeys.HEIGHT, EarthDataKeys.LANDFORM, EarthDataKeys.WATER_LEVEL, Blocks.WATER.getDefaultState()));
        builder.addSurfaceComposer(new SoilSurfaceComposer(this.ctx.world, EarthDataKeys.HEIGHT, EarthDataKeys.SOIL, Blocks.STONE.getDefaultState()));

        if (!preview && this.ctx.settings.getBoolean(ENABLE_DECORATION)) {
            builder.addSurfaceComposer(new EarthCarvingComposer(EarthDataKeys.COVER));
        }

        if (!preview && this.ctx.settings.get(CAVE_GENERATION)) {
            builder.addSurfaceComposer(new CaveSurfaceComposer(this.ctx.world));
        }

        if (!CubicGlue.isCubic(this.ctx.world)) {
            builder.addSurfaceComposer(new BedrockSurfaceComposer(this.ctx.world, Blocks.BEDROCK.getDefaultState(), Math.min(heightOrigin - 1, 5)));
        }
    }

    private void addDecorationComposers(boolean preview, CompositeTerrariumGenerator.Builder builder) {
        if (this.ctx.settings.getBoolean(ENABLE_DECORATION)) {
            builder.addDecorationComposer(new EarthDecorationComposer(this.ctx.world, EarthDataKeys.COVER));
            builder.addDecorationComposer(new BoulderDecorationComposer(this.ctx.world, EarthDataKeys.SLOPE));
        }

        builder.addDecorationComposer(new IceCoverComposer());
        builder.addDecorationComposer(new VanillaEntitySpawnComposer(this.ctx.world));

        if (!preview) {
            builder.addStructureComposer(new VanillaStructureComposer(this.ctx.world));
        }
    }
}
