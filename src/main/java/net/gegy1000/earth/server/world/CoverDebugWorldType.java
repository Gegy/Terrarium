package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.coordinate.DebugLatLngCoordinateState;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.composer.DebugSignDecorationComposer;
import net.gegy1000.earth.server.world.pipeline.populator.DebugCoverRegionPopulator;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.generator.BasicTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.CoverBiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.CoverDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CoverSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.populator.ConstantByteRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.ConstantShortRegionPopulator;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
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
    public TerrariumGenerator buildGenerator(World world, GenerationSettings settings) {
        CoordinateState zoneGeoCoordinates = new DebugLatLngCoordinateState();
        List<ConstructedCover<?>> coverTypes = new ArrayList<>();
        CoverGenerationContext.Default context = new CoverGenerationContext.Default(world, RegionComponentType.HEIGHT, RegionComponentType.COVER);
        EarthCoverContext earthContext = new EarthCoverContext(world, RegionComponentType.HEIGHT, RegionComponentType.COVER, RegionComponentType.SLOPE, zoneGeoCoordinates, true);
        coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.DEBUG, context));
        coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.PLACEHOLDER, context));
        coverTypes.addAll(EarthCoverTypes.COVER_TYPES.stream().map(type -> new ConstructedCover<>(type, earthContext)).collect(Collectors.toList()));
        return BasicTerrariumGenerator.builder()
                .withSurfaceComposer(new HeightmapSurfaceComposer(RegionComponentType.HEIGHT, Blocks.QUARTZ_BLOCK.getDefaultState()))
                .withSurfaceComposer(new CoverSurfaceComposer(world, RegionComponentType.COVER, coverTypes, true,Blocks.QUARTZ_BLOCK.getDefaultState()))
                .withSurfaceComposer(new BedrockSurfaceComposer(world, Blocks.BEDROCK.getDefaultState(), 0))
                .withDecorationComposer(new CoverDecorationComposer(world, RegionComponentType.COVER, coverTypes))
                .withDecorationComposer(new DebugSignDecorationComposer(RegionComponentType.HEIGHT))
                .withBiomeComposer(new CoverBiomeComposer(RegionComponentType.COVER, coverTypes))
                .withSpawnPosition(Coordinate.fromBlock(0.0, 0.0))
                .build();
    }

    @Override
    public TerrariumDataProvider buildDataProvider(World world, GenerationSettings settings) {
        return TerrariumDataProvider.builder()
                .withComponent(RegionComponentType.HEIGHT, new ConstantShortRegionPopulator((short) 62))
                .withComponent(RegionComponentType.SLOPE, new ConstantByteRegionPopulator((byte) 0))
                .withComponent(RegionComponentType.COVER, new DebugCoverRegionPopulator())
                .build();
    }

    @Override
    protected TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder().build();
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        return true;
    }
}
