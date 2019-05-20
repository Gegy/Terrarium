package net.gegy1000.earth.server.world;

import com.google.common.collect.Lists;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.world.pipeline.source.LandCoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SoilCoverSource;
import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.property.BooleanKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.NumberKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.InversePropertyConverter;
import net.gegy1000.terrarium.server.world.generator.customization.widget.SliderWidget;
import net.gegy1000.terrarium.server.world.generator.customization.widget.ToggleWidget;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Random;

public class EarthWorldType extends TerrariumWorldType {
    public static final double EARTH_CIRCUMFERENCE = 40075000.0;

    public static final double SRTM_WIDTH = 1200.0 * 360.0;

    public static final double SRTM_SCALE = EARTH_CIRCUMFERENCE / SRTM_WIDTH;
    public static final double LANDCOVER_SCALE = EARTH_CIRCUMFERENCE / LandCoverSource.GLOBAL_WIDTH;
    public static final double SOIL_SCALE = EARTH_CIRCUMFERENCE / SoilCoverSource.GLOBAL_WIDTH;
    public static final double CLIMATE_SCALE = EARTH_CIRCUMFERENCE / WorldClimateDataset.WIDTH;

    public static final int HIGHEST_POINT_METERS = 8900;

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.MODID, "earth_generator");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "earth_default");

    public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");
    public static final PropertyKey<Boolean> ENABLE_DECORATION = new BooleanKey("enable_decoration");
    public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
    public static final PropertyKey<Number> HEIGHT_SCALE = new NumberKey("height_scale");
    public static final PropertyKey<Number> NOISE_SCALE = new NumberKey("noise_scale");
    public static final PropertyKey<Number> HEIGHT_ORIGIN = new NumberKey("height_origin");
    public static final PropertyKey<Number> SEA_DEPTH = new NumberKey("ocean_depth");
    public static final PropertyKey<Number> BEACH_SIZE = new NumberKey("beach_size");
    public static final PropertyKey<Boolean> ENABLE_BUILDINGS = new BooleanKey("enable_buildings");
    public static final PropertyKey<Boolean> ENABLE_STREETS = new BooleanKey("enable_streets");

    public static final PropertyKey<Boolean> CAVE_GENERATION = new BooleanKey("cave_generation");

    public EarthWorldType() {
        super("earth", IDENTIFIER, PRESET);
    }

    @Override
    public TerrariumGeneratorInitializer createGeneratorInitializer(World world, GenerationSettings settings) {
        world.setSeaLevel(settings.getInteger(HEIGHT_ORIGIN));
        return new EarthGenerationInitializer(EarthInitContext.from(world, settings));
    }

    @Override
    public TerrariumDataInitializer createDataInitializer(World world, GenerationSettings settings) {
        return new EarthDataInitializer(EarthInitContext.from(world, settings));
    }

    @Override
    public Collection<ICapabilityProvider> createCapabilities(World world, GenerationSettings settings) {
        CoordinateState earthCoordinates = new LatLngCoordinateState(settings.getDouble(WORLD_SCALE) * SRTM_SCALE * 1200.0);
        return Lists.newArrayList(new EarthCapability.Impl(earthCoordinates));
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.builder()
                .withProperties(SPAWN_LATITUDE, SPAWN_LONGITUDE)
                .withProperties(WORLD_SCALE, HEIGHT_SCALE, NOISE_SCALE)
                .withProperties(SEA_DEPTH, HEIGHT_ORIGIN)
                .withProperties(BEACH_SIZE)
                .withProperties(ENABLE_DECORATION, ENABLE_BUILDINGS, ENABLE_STREETS)
                .withProperties(CAVE_GENERATION)
                .build();
    }

    @Override
    public TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder()
                .withCategory("world",
                        new SliderWidget(WORLD_SCALE, 1.0, 200.0, 5.0, 1.0, new InversePropertyConverter()),
                        new SliderWidget(HEIGHT_SCALE, 0.0, 10.0, 0.5, 0.1),
                        new SliderWidget(NOISE_SCALE, 0.0, 3.0, 0.5, 0.1),
                        new SliderWidget(SEA_DEPTH, 0, 32, 1, 1),
                        new SliderWidget(HEIGHT_ORIGIN, -63, 128, 1, 1),
                        new SliderWidget(BEACH_SIZE, 0, 8, 1, 1)
                )
                .withCategory("features",
                        new ToggleWidget(ENABLE_DECORATION),
                        new ToggleWidget(ENABLE_BUILDINGS).locked(),
                        new ToggleWidget(ENABLE_STREETS).locked()
                )
                .withCategory("procedural_features",
                        new ToggleWidget(CAVE_GENERATION)
                )
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(GuiCreateWorld parent, WorldType worldType, TerrariumPreset preset) {
        return new EarthCustomizationGui(parent, worldType, this, preset);
    }

    @Override
    public boolean shouldReduceSlimes(World world, Random random) {
        TerrariumWorldData worldData = TerrariumWorldData.get(world);
        if (worldData == null) {
            return false;
        }
        return worldData.getSettings().getInteger(HEIGHT_ORIGIN) < 40;
    }

    @Override
    protected int calculateMaxGenerationHeight(WorldServer world, GenerationSettings settings) {
        double globalScale = settings.getDouble(HEIGHT_SCALE) * settings.getDouble(WORLD_SCALE);
        double highestPointBlocks = HIGHEST_POINT_METERS * globalScale;
        return MathHelper.ceil(highestPointBlocks + settings.getDouble(HEIGHT_ORIGIN) + 1);
    }
}
