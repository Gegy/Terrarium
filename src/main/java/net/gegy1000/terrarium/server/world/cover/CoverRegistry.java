package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.world.cover.generator.PlaceholderCover;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class CoverRegistry {
    private static final Map<ResourceLocation, CoverType> COVER_TYPES = new HashMap<>();
    private static final Map<ResourceLocation, InstanceObjectParser<CoverGenerationContext>> CONTEXTS = new HashMap<>();

    public static final CoverType PLACEHOLDER = new PlaceholderCover();
    public static final CoverType DEBUG = new PlaceholderCover();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new ContextEvent(CONTEXTS));
        MinecraftForge.EVENT_BUS.post(new TypeEvent(COVER_TYPES));
    }

    @SubscribeEvent
    public static void onRegisterContexts(ContextEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "default_context"), new CoverGenerationContext.Default.Parser());
    }

    @SubscribeEvent
    public static void onRegisterCover(TypeEvent event) {
        event.register(new ResourceLocation(Terrarium.MODID, "placeholder"), PLACEHOLDER);
        event.register(new ResourceLocation(Terrarium.MODID, "debug"), DEBUG);
    }

    public static InstanceObjectParser<CoverGenerationContext> getContext(ResourceLocation identifier) {
        return CONTEXTS.get(identifier);
    }

    public static CoverType getCoverType(ResourceLocation identifier) {
        return COVER_TYPES.get(identifier);
    }

    public static Map<ResourceLocation, CoverType> getRegistry() {
        return Collections.unmodifiableMap(COVER_TYPES);
    }

    public static class ContextEvent extends TerrariumRegistryEvent<InstanceObjectParser<CoverGenerationContext>> {
        private ContextEvent(Map<ResourceLocation, InstanceObjectParser<CoverGenerationContext>> registry) {
            super(registry);
        }
    }

    public static class TypeEvent extends TerrariumRegistryEvent<CoverType> {
        private TypeEvent(Map<ResourceLocation, CoverType> registry) {
            super(registry);
        }
    }
}
