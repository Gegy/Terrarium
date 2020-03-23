package net.gegy1000.earth.server.world;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.PrepareTerrarium;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.property.BooleanKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.NumberKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.SliderScale;
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
import java.util.function.Consumer;

// TODO: Upgrade old terrarium worlds
public class EarthWorldType extends TerrariumWorldType {
    public static final double LANDCOVER_SCALE = EarthWorld.EQUATOR_CIRCUMFERENCE / LandCoverSource.GLOBAL_WIDTH;
    public static final double CLIMATE_SCALE = EarthWorld.EQUATOR_CIRCUMFERENCE / WorldClimateRaster.WIDTH;

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.ID, "earth");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.ID, "global_scenic");

    public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");
    public static final PropertyKey<Boolean> ADD_TREES = new BooleanKey("add_trees");
    public static final PropertyKey<Boolean> ADD_GRASS = new BooleanKey("add_grass");
    public static final PropertyKey<Boolean> ADD_FLOWERS = new BooleanKey("add_flowers");
    public static final PropertyKey<Boolean> ADD_CACTI = new BooleanKey("add_cacti");
    public static final PropertyKey<Boolean> ADD_SUGAR_CANE = new BooleanKey("add_sugar_cane");
    public static final PropertyKey<Boolean> ADD_GOURDS = new BooleanKey("add_gourds");
    public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
    public static final PropertyKey<Number> TERRESTRIAL_HEIGHT_SCALE = new NumberKey("terrestrial_height_scale");
    public static final PropertyKey<Number> OCEANIC_HEIGHT_SCALE = new NumberKey("oceanic_height_scale");
    public static final PropertyKey<Number> HEIGHT_OFFSET = new NumberKey("height_offset");

    public static final PropertyKey<Boolean> CAVE_GENERATION = new BooleanKey("cave_generation");
    public static final PropertyKey<Boolean> RAVINE_GENERATION = new BooleanKey("ravine_generation");
    public static final PropertyKey<Boolean> ORE_GENERATION = new BooleanKey("ore_generation");

    public EarthWorldType() {
        super("earth", IDENTIFIER, PRESET);
    }

    @Override
    public ComposableChunkGenerator createGenerator(World world) {
        return new ComposableChunkGenerator(world);
    }

    @Override
    public TerrariumGeneratorInitializer createGeneratorInitializer(World world, GenerationSettings settings, ColumnDataCache dataCache) {
        world.setSeaLevel(settings.getInteger(HEIGHT_OFFSET));
        return new EarthGenerationInitializer(EarthInitContext.from(settings), world, dataCache);
    }

    @Override
    public TerrariumDataInitializer createDataInitializer(GenerationSettings settings) {
        return new EarthDataInitializer(EarthInitContext.from(settings));
    }

    @Override
    public Collection<ICapabilityProvider> createCapabilities(GenerationSettings settings) {
        CoordinateReference crs = EarthInitContext.from(settings).lngLatCrs;
        return Lists.newArrayList(new EarthWorld.Impl(crs));
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.builder()
                .withProperties(SPAWN_LATITUDE, SPAWN_LONGITUDE)
                .withProperties(WORLD_SCALE, TERRESTRIAL_HEIGHT_SCALE, OCEANIC_HEIGHT_SCALE)
                .withProperties(HEIGHT_OFFSET)
                .withProperties(ADD_TREES, ADD_GRASS, ADD_FLOWERS, ADD_CACTI, ADD_SUGAR_CANE, ADD_GOURDS)
                .withProperties(CAVE_GENERATION, RAVINE_GENERATION, ORE_GENERATION)
                .build();
    }

    @Override
    public TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder()
                .withCategory("world",
                        new SliderWidget(WORLD_SCALE)
                                .range(1.0, 8000.0).step(5.0, 1.0)
                                .scale(SliderScale.power(3.0))
                                .display(value -> String.format("1:%.0f", value)),
                        new SliderWidget(TERRESTRIAL_HEIGHT_SCALE)
                                .range(0.0, 10.0).step(0.5, 0.1)
                                .display(value -> String.format("%.1fx", value)),
                        new SliderWidget(OCEANIC_HEIGHT_SCALE)
                                .range(0.0, 10.0).step(0.5, 0.1)
                                .display(value -> String.format("%.1fx", value)),
                        new SliderWidget(HEIGHT_OFFSET)
                                .range(-63, 128)
                                .display(value -> String.format("%.0f blocks", value))
                )
                .withCategory("ecological",
                        new ToggleWidget(ADD_TREES),
                        new ToggleWidget(ADD_GRASS),
                        new ToggleWidget(ADD_FLOWERS),
                        new ToggleWidget(ADD_CACTI),
                        new ToggleWidget(ADD_SUGAR_CANE),
                        new ToggleWidget(ADD_GOURDS)
                )
                .withCategory("geological",
                        new ToggleWidget(CAVE_GENERATION),
                        new ToggleWidget(RAVINE_GENERATION),
                        new ToggleWidget(ORE_GENERATION)
                )
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomize(Minecraft client, WorldType worldType, GuiCreateWorld parent) {
        if (!SharedEarthData.isInitialized()) {
            client.displayGuiScreen(PrepareTerrarium.prepareScreen(parent, () -> this.onCustomize(client, worldType, parent)));
            return;
        }

        EarthCustomizationGui customizationGui = new EarthCustomizationGui(parent, this);

        if (Strings.isNullOrEmpty(parent.chunkProviderSettingsJson)) {
            Consumer<TerrariumPreset> acceptPreset = preset -> {
                customizationGui.applyPreset(preset);
                client.displayGuiScreen(customizationGui);
            };
            client.displayGuiScreen(new SelectPresetGui(acceptPreset, parent, this));
        } else {
            client.displayGuiScreen(customizationGui);
        }
    }

    @Override
    public boolean shouldReduceSlimes(World world, Random random) {
        TerrariumWorld terrarium = TerrariumWorld.get(world);
        if (terrarium == null) return false;
        return terrarium.getSettings().getInteger(HEIGHT_OFFSET) < 40;
    }

    @Override
    public double getHorizon(World world) {
        GenerationSettings settings = TerrariumUserTracker.getProvidedSettings();
        if (settings == null) return 63.0;
        return settings.getInteger(HEIGHT_OFFSET);
    }

    @Override
    public float getCloudHeight() {
        GenerationSettings settings = TerrariumUserTracker.getProvidedSettings();
        if (settings == null) return 128.0F;

        double cloudHeight = this.transformHeight(settings, 2000.0);
        return (float) Math.max(cloudHeight, settings.getDouble(HEIGHT_OFFSET) + 64.0);
    }

    @Override
    protected int calculateMaxGenerationHeight(WorldServer world, GenerationSettings settings) {
        double height = this.transformHeight(settings, EarthWorld.HIGHEST_POINT_METERS);
        return MathHelper.ceil(height) + 1;
    }

    private double transformHeight(GenerationSettings settings, double height) {
        double scale = settings.getDouble(TERRESTRIAL_HEIGHT_SCALE) / settings.getDouble(WORLD_SCALE);
        double offset = settings.getDouble(HEIGHT_OFFSET);
        return height * scale + offset;
    }
}
