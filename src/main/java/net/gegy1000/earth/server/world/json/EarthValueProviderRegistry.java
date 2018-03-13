package net.gegy1000.earth.server.world.json;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.json.ValueProvider;
import net.gegy1000.terrarium.server.world.json.ValueProviderRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class EarthValueProviderRegistry {
    @SubscribeEvent
    public static void onRegisterProviders(ValueProviderRegistry.Event event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "earth_interpolation_method"), (ValueProvider<String>) (valueParser, root) -> {
            double scale = 1.0 / valueParser.parseDouble(root, "world_scale");

            Interpolation.Method interpolationMethod = Interpolation.Method.CUBIC;
            if (scale >= 45.0) {
                interpolationMethod = Interpolation.Method.LINEAR;
            } else if (scale >= 20.0) {
                interpolationMethod = Interpolation.Method.COSINE;
            }

            return interpolationMethod.getKey();
        });
    }
}
