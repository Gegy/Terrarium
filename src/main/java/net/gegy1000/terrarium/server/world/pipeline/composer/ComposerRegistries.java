package net.gegy1000.terrarium.server.world.pipeline.composer;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.CoverBiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.CoverDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.OceanFillComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CoverSurfaceComposer;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ComposerRegistries {
    private static final Map<ResourceLocation, InstanceObjectParser<BiomeComposer>> BIOME_COMPOSERS = new HashMap<>();
    private static final Map<ResourceLocation, InstanceObjectParser<DecorationComposer>> DECORATION_COMPOSERS = new HashMap<>();
    private static final Map<ResourceLocation, InstanceObjectParser<SurfaceComposer>> SURFACE_COMPOSERS = new HashMap<>();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new BiomeEvent(BIOME_COMPOSERS));
        MinecraftForge.EVENT_BUS.post(new DecorationEvent(DECORATION_COMPOSERS));
        MinecraftForge.EVENT_BUS.post(new SurfaceEvent(SURFACE_COMPOSERS));
    }

    @SubscribeEvent
    public static void onRegisterBiomeComposers(BiomeEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "biome_cover"), new CoverBiomeComposer.Parser());
    }

    @SubscribeEvent
    public static void onRegisterDecorationComposers(DecorationEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "cover_decorator"), new CoverDecorationComposer.Parser());
    }

    @SubscribeEvent
    public static void onRegisterSurfaceComposers(SurfaceEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "heightmap_composer"), new HeightmapComposer.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "fill_ocean"), new OceanFillComposer.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "cover_surface"), new CoverSurfaceComposer.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "add_bedrock"), new BedrockComposer.Parser());
    }

    public static InstanceObjectParser<SurfaceComposer> getSurfaceComposer(ResourceLocation identifier) {
        return SURFACE_COMPOSERS.get(identifier);
    }

    public static InstanceObjectParser<DecorationComposer> getDecorationComposer(ResourceLocation identifer) {
        return DECORATION_COMPOSERS.get(identifer);
    }

    public static InstanceObjectParser<BiomeComposer> getBiomeComposer(ResourceLocation identifier) {
        return BIOME_COMPOSERS.get(identifier);
    }

    public static final class BiomeEvent extends TerrariumRegistryEvent<InstanceObjectParser<BiomeComposer>> {
        private BiomeEvent(Map<ResourceLocation, InstanceObjectParser<BiomeComposer>> registry) {
            super(registry);
        }
    }

    public static final class DecorationEvent extends TerrariumRegistryEvent<InstanceObjectParser<DecorationComposer>> {
        private DecorationEvent(Map<ResourceLocation, InstanceObjectParser<DecorationComposer>> registry) {
            super(registry);
        }
    }

    public static final class SurfaceEvent extends TerrariumRegistryEvent<InstanceObjectParser<SurfaceComposer>> {
        private SurfaceEvent(Map<ResourceLocation, InstanceObjectParser<SurfaceComposer>> registry) {
            super(registry);
        }
    }
}
