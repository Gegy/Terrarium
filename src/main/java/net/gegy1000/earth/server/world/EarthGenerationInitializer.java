package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.capability.HeightmapStore;
import net.gegy1000.earth.server.world.composer.EarthBiomeComposer;
import net.gegy1000.earth.server.world.composer.EarthCactusComposer;
import net.gegy1000.earth.server.world.composer.EarthGrassComposer;
import net.gegy1000.earth.server.world.composer.EarthTreeComposer;
import net.gegy1000.earth.server.world.composer.FloodedSurfaceComposer;
import net.gegy1000.earth.server.world.composer.FreezeSurfaceComposer;
import net.gegy1000.earth.server.world.composer.OreDecorationComposer;
import net.gegy1000.earth.server.world.composer.TerrainSurfaceComposer;
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
    private final World world;

    EarthGenerationInitializer(EarthInitContext ctx, World world) {
        this.ctx = ctx;
        this.world = world;
    }

    @Override
    public void setup(CompositeTerrariumGenerator.Builder builder) {
        this.addSurfaceComposers(builder);
        this.addDecorationComposers(builder);

        builder.addStructureComposer(new VanillaStructureComposer(this.world));

        builder.setBiomeComposer(new EarthBiomeComposer());
        builder.setSpawnPosition(new Coordinate(this.ctx.lngLatCrs, this.ctx.settings.getDouble(SPAWN_LONGITUDE), this.ctx.settings.getDouble(SPAWN_LATITUDE)));
    }

    private void addSurfaceComposers(CompositeTerrariumGenerator.Builder builder) {
        int heightOffset = this.ctx.settings.getInteger(HEIGHT_OFFSET);
        HeightFunction surfaceFunction = HeightmapStore.global(this.world, heightOffset);

        builder.addSurfaceComposer(new HeightmapSurfaceComposer(EarthDataKeys.TERRAIN_HEIGHT, Blocks.STONE.getDefaultState()));
        builder.addSurfaceComposer(new WaterFillSurfaceComposer(Blocks.WATER.getDefaultState()));
        builder.addSurfaceComposer(new TerrainSurfaceComposer(this.world, Blocks.STONE.getDefaultState()));

        builder.addSurfaceComposer(new FloodedSurfaceComposer());

        if (this.ctx.settings.get(CAVE_GENERATION)) {
            builder.addSurfaceComposer(GenericSurfaceComposer.of(new GenericCavePrimer(this.world)));
        }

        if (this.ctx.settings.get(RAVINE_GENERATION)) {
            builder.addSurfaceComposer(GenericSurfaceComposer.of(new GenericRavinePrimer(this.world, surfaceFunction)));
        }

        if (!GenGen.isCubic(this.world)) {
            builder.addSurfaceComposer(new BedrockSurfaceComposer(this.world, Blocks.BEDROCK.getDefaultState(), Math.min(heightOffset - 1, 5)));
        }
    }

    private void addDecorationComposers(CompositeTerrariumGenerator.Builder builder) {
        if (this.ctx.settings.getBoolean(ADD_TREES)) {
            builder.addDecorationComposer(new EarthTreeComposer(this.world));
        }

        if (this.ctx.settings.getBoolean(ADD_GRASS)) {
            builder.addDecorationComposer(new EarthGrassComposer(this.world));
        }

        if (this.ctx.settings.getBoolean(ADD_CACTI)) {
            builder.addDecorationComposer(new EarthCactusComposer(this.world));
        }

        if (this.ctx.settings.getBoolean(ORE_GENERATION)) {
            OreDecorationComposer oreComposer = new OreDecorationComposer(this.world);
            VanillaOres.addTo(oreComposer);

            builder.addDecorationComposer(oreComposer);
        }

        builder.addDecorationComposer(new FreezeSurfaceComposer(this.world));
        builder.addDecorationComposer(new VanillaEntitySpawnComposer(this.world));
    }
}
