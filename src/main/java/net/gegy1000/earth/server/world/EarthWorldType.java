package net.gegy1000.earth.server.world;

import com.google.common.collect.Lists;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.client.gui.SharedInitializingGui;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.SoilCoverSource;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.property.BooleanKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.EnumKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.NumberKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CycleWidget;
import net.gegy1000.terrarium.server.world.generator.customization.widget.SliderWidget;
import net.gegy1000.terrarium.server.world.generator.customization.widget.ToggleWidget;
import net.minecraft.client.Minecraft;
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

// TODO: Code to update old terrarium world configs to new format
public class EarthWorldType extends TerrariumWorldType {
    public static final double EARTH_CIRCUMFERENCE = 40075000.0;

    public static final double SRTM_WIDTH = 1200.0 * 360.0;

    public static final double SRTM_SCALE = EARTH_CIRCUMFERENCE / SRTM_WIDTH;
    public static final double LANDCOVER_SCALE = EARTH_CIRCUMFERENCE / LandCoverSource.GLOBAL_WIDTH;
    public static final double SOIL_SCALE = EARTH_CIRCUMFERENCE / SoilCoverSource.GLOBAL_WIDTH;
    public static final double CLIMATE_SCALE = EARTH_CIRCUMFERENCE / WorldClimateRaster.WIDTH;

    public static final int HIGHEST_POINT_METERS = 8900;

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.MODID, "earth");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "global_default");

    public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");
    public static final PropertyKey<Boolean> ENABLE_DECORATION = new BooleanKey("enable_decoration");
    public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
    public static final PropertyKey<Number> HEIGHT_SCALE = new NumberKey("height_scale");
    public static final PropertyKey<Number> HEIGHT_ORIGIN = new NumberKey("height_origin");
    public static final PropertyKey<Number> SEA_DEPTH = new NumberKey("ocean_depth");
    public static final PropertyKey<Number> BEACH_SIZE = new NumberKey("beach_size");
    public static final PropertyKey<Boolean> ENABLE_BUILDINGS = new BooleanKey("enable_buildings");
    public static final PropertyKey<Boolean> ENABLE_STREETS = new BooleanKey("enable_streets");

    public static final PropertyKey<Boolean> CAVE_GENERATION = new BooleanKey("cave_generation");
    public static final PropertyKey<Season> SEASON = new EnumKey<>("season", Season.class);

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
        CoordinateState earthCoordinates = new LatLngCoordinateState((SRTM_SCALE * 1200.0) / settings.getDouble(WORLD_SCALE));
        return Lists.newArrayList(new EarthCapability.Impl(earthCoordinates));
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.builder()
                .withProperties(SPAWN_LATITUDE, SPAWN_LONGITUDE)
                .withProperties(WORLD_SCALE, HEIGHT_SCALE)
                .withProperties(SEA_DEPTH, HEIGHT_ORIGIN)
                .withProperties(BEACH_SIZE)
                .withProperties(ENABLE_DECORATION, ENABLE_BUILDINGS, ENABLE_STREETS)
                .withProperties(CAVE_GENERATION)
                .withProperties(SEASON)
                .build();
    }

    @Override
    public TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder()
                .withCategory("world",
                        new SliderWidget(WORLD_SCALE, 1.0, 250.0, 5.0, 1.0, value -> String.format("1:%.0f", value)),
                        new SliderWidget(HEIGHT_SCALE, 0.0, 10.0, 0.5, 0.1, value -> String.format("%.1fx", value)),
                        new SliderWidget(SEA_DEPTH, 0, 32, 1, 1, value -> String.format("%.0f blocks", value)),
                        new SliderWidget(HEIGHT_ORIGIN, -63, 128, 1, 1, value -> String.format("%.0f blocks", value)),
                        new SliderWidget(BEACH_SIZE, 0, 8, 1, 1, value -> String.format("%.0f blocks", value))
                )
                .withCategory("natural",
                        new CycleWidget<>(SEASON)
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
    public void onCustomize(Minecraft client, WorldType worldType, GuiCreateWorld parent) {
        if (!SharedEarthData.isInitialized()) {
            client.displayGuiScreen(new SharedInitializingGui(parent, () -> super.onCustomize(client, worldType, parent)));
            return;
        }

        super.onCustomize(client, worldType, parent);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(GuiCreateWorld parent, WorldType worldType) {
        return new EarthCustomizationGui(parent, worldType, this);
    }

    @Override
    public boolean shouldReduceSlimes(World world, Random random) {
        TerrariumWorld worldData = TerrariumWorld.get(world);
        if (worldData == null) {
            return false;
        }
        return worldData.getSettings().getInteger(HEIGHT_ORIGIN) < 40;
    }

    @Override
    protected int calculateMaxGenerationHeight(WorldServer world, GenerationSettings settings) {
        double globalScale = settings.getDouble(HEIGHT_SCALE) / settings.getDouble(WORLD_SCALE);
        double highestPointBlocks = HIGHEST_POINT_METERS * globalScale;
        return MathHelper.ceil(highestPointBlocks + settings.getDouble(HEIGHT_ORIGIN) + 1);
    }
}
