package net.gegy1000.earth.server.config;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TerrariumEarth.ID)
@Mod.EventBusSubscriber(modid = TerrariumEarth.ID)
public class TerrariumEarthConfig {
    @Config.Name("street_view_zoom")
    @Config.LangKey("config.earth.street_view_zoom")
    @Config.Comment("The zoom level to load street view images at. Higher values means higher resolution")
    @Config.RangeInt(min = 0, max = 4)
    public static int streetViewZoom = 2;

    @Config.Name("osm_geocoder")
    @Config.LangKey("config.earth.osm_geocoder")
    @Config.Comment("If true, the Nominatim OpenStreetMap Geocoder will be used instead of Google's")
    public static boolean osmGeocoder = false;

    @Config.Name("accept_remote_data")
    @Config.LangKey("config.terrarium.accept_remote_data")
    @Config.Comment("If true, you will no longer be warned about remote data usage. (Automatically set through GUI)")
    public static boolean acceptedRemoteDataWarning = false;

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(TerrariumEarth.ID)) {
            ConfigManager.sync(TerrariumEarth.ID, Config.Type.INSTANCE);
        }
    }
}
