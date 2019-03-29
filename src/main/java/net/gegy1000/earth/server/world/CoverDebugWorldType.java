package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.coordinate.DebugLatLngCoordinateState;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.composer.DebugSignDecorationComposer;
import net.gegy1000.earth.server.world.pipeline.data.DebugCoverPopulator;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.generator.BasicTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.CoverBiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.CoverDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CoverSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.function.ConstantRasterProducer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CoverDebugWorldType extends TerrariumWorldType {
    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.MODID, "debug_generator");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "debug_default");

    public CoverDebugWorldType() {
        super("earth_debug", IDENTIFIER, PRESET);
    }

    @Override
    public TerrariumGeneratorInitializer createInitializer(World world, GenerationSettings settings) {
        return new Initializer(world);
    }

    @Override
    public Collection<ICapabilityProvider> createCapabilities(World world, GenerationSettings settings) {
        return Collections.emptyList();
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.EMPTY;
    }

    @Override
    public TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.EMPTY;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean shouldReduceSlimes(World world, Random random) {
        return true;
    }

    private static class Initializer implements TerrariumGeneratorInitializer {
        private final World world;

        private Initializer(World world) {
            this.world = world;
        }

        @Override
        public TerrariumGenerator buildGenerator(boolean preview) {
            CoordinateState zoneGeoCoordinates = new DebugLatLngCoordinateState();
            List<ConstructedCover<?>> coverTypes = this.buildCoverTypes(zoneGeoCoordinates);
            return BasicTerrariumGenerator.builder()
                    .withSurfaceComposer(new HeightmapSurfaceComposer(RegionComponentType.HEIGHT, Blocks.QUARTZ_BLOCK.getDefaultState()))
                    .withSurfaceComposer(new CoverSurfaceComposer(this.world, RegionComponentType.HEIGHT, RegionComponentType.COVER, coverTypes, true, Blocks.QUARTZ_BLOCK.getDefaultState()))
                    .withSurfaceComposer(new BedrockSurfaceComposer(this.world, Blocks.BEDROCK.getDefaultState(), 0))
                    .withDecorationComposer(new CoverDecorationComposer(this.world, RegionComponentType.COVER, coverTypes))
                    .withDecorationComposer(new DebugSignDecorationComposer(RegionComponentType.HEIGHT))
                    .withBiomeComposer(new CoverBiomeComposer(RegionComponentType.COVER, coverTypes))
                    .withSpawnPosition(Coordinate.fromBlock(0.0, 0.0))
                    .build();
        }

        private List<ConstructedCover<?>> buildCoverTypes(CoordinateState zoneGeoCoordinates) {
            List<ConstructedCover<?>> coverTypes = new ArrayList<>();
            CoverGenerationContext.Default context = new CoverGenerationContext.Default(this.world, RegionComponentType.HEIGHT, RegionComponentType.COVER);
            EarthCoverContext earthContext = new EarthCoverContext(this.world, RegionComponentType.HEIGHT, RegionComponentType.COVER, RegionComponentType.SLOPE, zoneGeoCoordinates, false);
            coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.DEBUG, context));
            coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.PLACEHOLDER, context));
            coverTypes.addAll(EarthCoverTypes.COVER_TYPES.stream().map(type -> new ConstructedCover<>(type, earthContext)).collect(Collectors.toList()));
            return coverTypes;
        }

        @Override
        public TerrariumDataProvider buildDataProvider() {
            return TerrariumDataProvider.builder()
                    .withComponent(RegionComponentType.HEIGHT, ConstantRasterProducer.shortRaster((short) 62))
                    .withComponent(RegionComponentType.SLOPE, ConstantRasterProducer.unsignedByteRaster(0))
                    .withComponent(RegionComponentType.COVER, DebugCoverPopulator.populate())
                    .build();
        }
    }
}
