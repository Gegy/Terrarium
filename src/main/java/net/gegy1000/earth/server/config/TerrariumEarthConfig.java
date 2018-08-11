package net.gegy1000.earth.server.config;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TerrariumEarth.MODID)
@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class TerrariumEarthConfig {
    @Config.Name("street_view_zoom")
    @Config.LangKey("config.earth.street_view_zoom")
    @Config.Comment("The zoom level to load street view images at. Higher values means higher resolution")
    @Config.RangeInt(min = 0, max = 4)
    public static int streetViewZoom = 2;

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(TerrariumEarth.MODID)) {
            ConfigManager.sync(TerrariumEarth.MODID, Config.Type.INSTANCE);
        }
    }
}
