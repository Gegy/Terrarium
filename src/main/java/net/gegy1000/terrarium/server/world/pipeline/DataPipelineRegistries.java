package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.adapter.BeachAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightNoiseAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightTransformAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.OsmCoastlineAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.WaterFlattenAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugRegionBorderAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.populator.OverpassRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.ScaledByteRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.ScaledCoverRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.ScaledShortRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.sampler.ByteTileSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.CoverTileSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.OsmSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.ShortTileSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.SlopeTileSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.earth.GlobSource;
import net.gegy1000.terrarium.server.world.pipeline.source.earth.SRTMHeightSource;
import net.gegy1000.terrarium.server.world.pipeline.source.earth.osm.OverpassSource;
import net.gegy1000.terrarium.server.world.pipeline.source.earth.tile.OsmTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class DataPipelineRegistries {
    private static final Map<ResourceLocation, RegionComponentType<?>> COMPONENT_TYPES = new HashMap<>();

    private static final Map<ResourceLocation, InstanceObjectParser<TiledDataSource<?>>> SOURCES = new HashMap<>();
    private static final Map<ResourceLocation, InstanceObjectParser<DataSampler<?>>> SAMPLERS = new HashMap<>();
    private static final Map<ResourceLocation, InstanceObjectParser<RegionPopulator<?>>> POPULATORS = new HashMap<>();
    private static final Map<ResourceLocation, InstanceObjectParser<RegionAdapter>> ADAPTERS = new HashMap<>();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new ComponentTypeEvent(COMPONENT_TYPES));
        MinecraftForge.EVENT_BUS.post(new SourceEvent(SOURCES));
        MinecraftForge.EVENT_BUS.post(new SamplerEvent(SAMPLERS));
        MinecraftForge.EVENT_BUS.post(new PopulatorEvent(POPULATORS));
        MinecraftForge.EVENT_BUS.post(new AdapterEvent(ADAPTERS));
    }

    @SubscribeEvent
    public static void onRegisterComponentTypes(ComponentTypeEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "height"), new RegionComponentType<ShortRasterTileAccess>(ShortRasterTileAccess.class) {
            @Override
            public ShortRasterTileAccess createDefaultData(int width, int height) {
                short[] data = new short[width * height];
                return new ShortRasterTileAccess(data, width, height);
            }
        });

        event.register(new ResourceLocation(Terrarium.MODID, "slope"), new RegionComponentType<ByteRasterTileAccess>(ByteRasterTileAccess.class) {
            @Override
            public ByteRasterTileAccess createDefaultData(int width, int height) {
                byte[] data = new byte[width * height];
                return new ByteRasterTileAccess(data, width, height);
            }
        });

        event.register(new ResourceLocation(Terrarium.MODID, "cover"), new RegionComponentType<CoverRasterTileAccess>(CoverRasterTileAccess.class) {
            @Override
            public CoverRasterTileAccess createDefaultData(int width, int height) {
                CoverType[] data = ArrayUtils.defaulted(new CoverType[width * height], CoverType.NO_DATA);
                return new CoverRasterTileAccess(data, width, height);
            }
        });

        event.register(new ResourceLocation(Terrarium.MODID, "osm"), new RegionComponentType<OsmTileAccess>(OsmTileAccess.class) {
            @Override
            public OsmTileAccess createDefaultData(int width, int height) {
                return new OsmTileAccess();
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterSources(SourceEvent event) {
        // TODO: Move to Earth module
        event.register(new ResourceLocation(Terrarium.MODID, "srtm_raster_source"), new SRTMHeightSource.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "glob_raster_source"), new GlobSource.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "overpass_source"), new OverpassSource.Parser());
    }

    @SubscribeEvent
    public static void onRegisterSamplers(SamplerEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "cover_raster_sampler"), new CoverTileSampler.Parser());

        event.register(new ResourceLocation(Terrarium.MODID, "short_raster_sampler"), new ShortTileSampler.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "byte_raster_sampler"), new ByteTileSampler.Parser());

        event.register(new ResourceLocation(Terrarium.MODID, "slope_raster_sampler"), new SlopeTileSampler.Parser());

        event.register(new ResourceLocation(Terrarium.MODID, "osm_sampler"), new OsmSampler.Parser());
    }

    @SubscribeEvent
    public static void onRegisterPopulators(PopulatorEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "scaled_cover_populator"), new ScaledCoverRegionPopulator.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "overpass_populator"), new OverpassRegionPopulator.Parser());

        event.register(new ResourceLocation(Terrarium.MODID, "scaled_short_populator"), new ScaledShortRegionPopulator.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "scaled_byte_populator"), new ScaledByteRegionPopulator.Parser());
    }

    @SubscribeEvent
    public static void onRegisterAdapters(AdapterEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "debug_region_border"), new DebugRegionBorderAdapter.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "height_noise"), new HeightNoiseAdapter.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "transform_heights"), new HeightTransformAdapter.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "beaches"), new BeachAdapter.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "water_edge_flatten"), new WaterFlattenAdapter.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "osm_coastlines"), new OsmCoastlineAdapter.Parser());
    }

    public static RegionComponentType<?> getComponentType(ResourceLocation identifier) {
        return COMPONENT_TYPES.get(identifier);
    }

    public static InstanceObjectParser<TiledDataSource<?>> getSource(ResourceLocation identifier) {
        return SOURCES.get(identifier);
    }

    public static InstanceObjectParser<DataSampler<?>> getSampler(ResourceLocation identifier) {
        return SAMPLERS.get(identifier);
    }

    public static InstanceObjectParser<RegionPopulator<?>> getPopulator(ResourceLocation identifier) {
        return POPULATORS.get(identifier);
    }

    public static InstanceObjectParser<RegionAdapter> getAdapter(ResourceLocation identifier) {
        return ADAPTERS.get(identifier);
    }

    public static final class ComponentTypeEvent extends TerrariumRegistryEvent<RegionComponentType<?>> {
        private ComponentTypeEvent(Map<ResourceLocation, RegionComponentType<?>> registry) {
            super(registry);
        }
    }

    public static final class SourceEvent extends TerrariumRegistryEvent<InstanceObjectParser<TiledDataSource<?>>> {
        private SourceEvent(Map<ResourceLocation, InstanceObjectParser<TiledDataSource<?>>> registry) {
            super(registry);
        }
    }

    public static final class SamplerEvent extends TerrariumRegistryEvent<InstanceObjectParser<DataSampler<?>>> {
        private SamplerEvent(Map<ResourceLocation, InstanceObjectParser<DataSampler<?>>> registry) {
            super(registry);
        }
    }

    public static final class PopulatorEvent extends TerrariumRegistryEvent<InstanceObjectParser<RegionPopulator<?>>> {
        private PopulatorEvent(Map<ResourceLocation, InstanceObjectParser<RegionPopulator<?>>> registry) {
            super(registry);
        }
    }

    public static final class AdapterEvent extends TerrariumRegistryEvent<InstanceObjectParser<RegionAdapter>> {
        private AdapterEvent(Map<ResourceLocation, InstanceObjectParser<RegionAdapter>> registry) {
            super(registry);
        }
    }
}
