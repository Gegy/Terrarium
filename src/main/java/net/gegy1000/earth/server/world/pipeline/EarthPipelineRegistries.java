package net.gegy1000.earth.server.world.pipeline;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.adapter.OsmCoastlineAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterFlattenAdapter;
import net.gegy1000.earth.server.world.pipeline.populator.OverpassRegionPopulator;
import net.gegy1000.earth.server.world.pipeline.sampler.OsmSampler;
import net.gegy1000.earth.server.world.pipeline.source.GlobSource;
import net.gegy1000.earth.server.world.pipeline.source.SRTMHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.DataPipelineRegistries;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class EarthPipelineRegistries {
    @SubscribeEvent
    public static void onRegisterComponentTypes(DataPipelineRegistries.ComponentTypeEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "osm"), new RegionComponentType<OsmTileAccess>(OsmTileAccess.class) {
            @Override
            public OsmTileAccess createDefaultData(int width, int height) {
                return new OsmTileAccess();
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterSources(DataPipelineRegistries.SourceEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "srtm_raster_source"), new SRTMHeightSource.Parser());
        event.register(new ResourceLocation(TerrariumEarth.MODID, "glob_raster_source"), new GlobSource.Parser());
        event.register(new ResourceLocation(TerrariumEarth.MODID, "overpass_source"), new OverpassSource.Parser());
    }

    @SubscribeEvent
    public static void onRegisterSamplers(DataPipelineRegistries.SamplerEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "osm_sampler"), new OsmSampler.Parser());
    }

    @SubscribeEvent
    public static void onRegisterPopulators(DataPipelineRegistries.PopulatorEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "overpass_populator"), new OverpassRegionPopulator.Parser());
    }

    @SubscribeEvent
    public static void onRegisterAdapters(DataPipelineRegistries.AdapterEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "osm_coastlines"), new OsmCoastlineAdapter.Parser());
        event.register(new ResourceLocation(TerrariumEarth.MODID, "water_edge_flatten"), new WaterFlattenAdapter.Parser());
    }
}
