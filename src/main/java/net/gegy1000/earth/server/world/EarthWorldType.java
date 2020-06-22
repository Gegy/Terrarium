package net.gegy1000.earth.server.world;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.PrepareTerrarium;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertySchema;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
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

import static net.gegy1000.earth.server.world.EarthProperties.*;

public class EarthWorldType extends TerrariumWorldType {
    public static final double CLIMATE_SCALE = EarthWorld.EQUATOR_CIRCUMFERENCE / WorldClimateRaster.WIDTH;

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.ID, "earth");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.ID, "scenic");

    public EarthWorldType() {
        super("earth", IDENTIFIER, PRESET);
    }

    @Override
    public ComposableChunkGenerator createGenerator(World world) {
        return new ComposableChunkGenerator(world);
    }

    @Override
    public TerrariumGeneratorInitializer createGeneratorInitializer(WorldServer world, GenerationSettings settings, ColumnDataCache dataCache) {
        world.setSeaLevel(settings.getInteger(HEIGHT_OFFSET) + 2);
        return new EarthGenerationInitializer(EarthInitContext.from(settings), world, dataCache);
    }

    @Override
    public TerrariumDataInitializer createDataInitializer(GenerationSettings settings) {
        return new EarthDataInitializer(EarthInitContext.from(settings));
    }

    @Override
    public Collection<ICapabilityProvider> createCapabilities(World world, GenerationSettings settings) {
        return Lists.newArrayList(new EarthWorld.Impl(settings));
    }

    @Override
    public PropertySchema buildPropertySchema() {
        return EarthPropertySchema.INSTANCE;
    }

    @Override
    public TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder()
                .withCategory("world",
                        new SliderWidget(WORLD_SCALE)
                                .range(1.0, 40000.0).step(5.0, 1.0)
                                .scale(SliderScale.power(3.0))
                                .display(value -> {
                                    if (value < 1000.0) {
                                        return String.format("1:%.0fm", value);
                                    } else {
                                        return String.format("1:%.1fkm", value / 1000.0);
                                    }
                                }),
                        new SliderWidget(TERRESTRIAL_HEIGHT_SCALE)
                                .range(0.0, 50.0).step(0.5, 0.1)
                                .scale(SliderScale.power(3.0))
                                .display(value -> String.format("%.1fx", value)),
                        new SliderWidget(OCEANIC_HEIGHT_SCALE)
                                .range(0.0, 50.0).step(0.5, 0.1)
                                .scale(SliderScale.power(3.0))
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
                .withCategory("structure",
                        new ToggleWidget(ADD_STRONGHOLDS),
                        new ToggleWidget(ADD_VILLAGES),
                        new ToggleWidget(ADD_MINESHAFTS),
                        new ToggleWidget(ADD_TEMPLES),
                        new ToggleWidget(ADD_OCEAN_MONUMENTS),
                        new ToggleWidget(ADD_WOODLAND_MANSIONS)
                )
                .withCategory("compatibility",
                        new ToggleWidget(COMPATIBILITY_MODE),
                        new ToggleWidget(BOP_INTEGRATION).locked(!TerrariumEarth.hasBiomesOPlenty)
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
    protected int getMinGenerationHeight(WorldServer world, GenerationSettings settings) {
        double height = this.transformHeight(settings, EarthWorld.LOWEST_POINT_METERS);
        return MathHelper.floor(height) - 1;
    }

    @Override
    protected int getMaxGenerationHeight(WorldServer world, GenerationSettings settings) {
        double height = this.transformHeight(settings, EarthWorld.HIGHEST_POINT_METERS);
        return MathHelper.ceil(height) + 1;
    }

    @Override
    public int getMinSpawnHeight(World world) {
        return world.getSeaLevel() - 1;
    }

    private double transformHeight(GenerationSettings settings, double height) {
        double heightScale = height >= 0.0 ? settings.getDouble(TERRESTRIAL_HEIGHT_SCALE) : settings.getDouble(OCEANIC_HEIGHT_SCALE);

        double scale = heightScale / settings.getDouble(WORLD_SCALE);
        double offset = settings.getDouble(HEIGHT_OFFSET);
        return height * scale + offset;
    }
}
