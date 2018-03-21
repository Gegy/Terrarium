package net.gegy1000.terrarium.server.world.generator.customization.widget;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.world.json.InitObjectParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class WidgetConverterRegistry {
    private static final Map<ResourceLocation, InitObjectParser<WidgetPropertyConverter>> CONVERTERS = new HashMap<>();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(CONVERTERS));
    }

    @SubscribeEvent
    public static void onRegisterConverters(Event event) {
        event.register(new ResourceLocation(Terrarium.MODID, "scaled_converter"), new ScaledPropertyConverter.Parser());
        event.register(new ResourceLocation(Terrarium.MODID, "inverse_converter"), (valueParser, objectRoot) -> new InversePropertyConverter());
    }

    public static InitObjectParser<WidgetPropertyConverter> get(ResourceLocation identifier) {
        return CONVERTERS.get(identifier);
    }

    public static class Event extends TerrariumRegistryEvent<InitObjectParser<WidgetPropertyConverter>> {
        private Event(Map<ResourceLocation, InitObjectParser<WidgetPropertyConverter>> registry) {
            super(registry);
        }
    }
}
