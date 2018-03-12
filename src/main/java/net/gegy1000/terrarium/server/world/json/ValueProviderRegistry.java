package net.gegy1000.terrarium.server.world.json;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.event.TerrariumRegistryEvent;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ValueProviderRegistry {
    private static final Map<ResourceLocation, ValueProvider<?>> PROVIDERS = new HashMap<>();

    public static void onInit() {
        MinecraftForge.EVENT_BUS.post(new Event(PROVIDERS));
    }

    @SubscribeEvent
    public static void onRegisterGenerators(Event event) {
        // TODO: Earth specific provider
        event.register(new ResourceLocation(Terrarium.MODID, "earth_interpolation_method"), (ValueProvider<String>) (valueParser, root) -> {
            double scale = 1.0 / valueParser.parseDouble(root, "world_scale");

            Interpolation.Method interpolationMethod = Interpolation.Method.CUBIC;
            if (scale >= 45.0) {
                interpolationMethod = Interpolation.Method.LINEAR;
            } else if (scale >= 20.0) {
                interpolationMethod = Interpolation.Method.COSINE;
            }

            return interpolationMethod.getKey();
        });

        event.register(new ResourceLocation(Terrarium.MODID, "add"), (DoubleOp) (l, r) -> l + r);
        event.register(new ResourceLocation(Terrarium.MODID, "sub"), (DoubleOp) (l, r) -> l - r);
        event.register(new ResourceLocation(Terrarium.MODID, "mul"), (DoubleOp) (l, r) -> l * r);
        event.register(new ResourceLocation(Terrarium.MODID, "div"), (DoubleOp) (l, r) -> l / r);
    }

    public static ValueProvider<?> get(ResourceLocation identifier) {
        return PROVIDERS.get(identifier);
    }

    public static final class Event extends TerrariumRegistryEvent<ValueProvider<?>> {
        private Event(Map<ResourceLocation, ValueProvider<?>> registry) {
            super(registry);
        }
    }

    private interface DoubleOp extends ValueProvider<Double> {
        @Override
        default Double provide(JsonValueParser valueParser, JsonObject root) {
            double left = valueParser.parseDouble(root, "left");
            double right = valueParser.parseDouble(root, "right");
            return this.perform(left, right);
        }

        double perform(double l, double r);
    }
}
