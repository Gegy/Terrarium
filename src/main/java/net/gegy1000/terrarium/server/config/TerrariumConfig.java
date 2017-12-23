package net.gegy1000.terrarium.server.config;

import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Terrarium.MODID)
@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class TerrariumConfig {
    @Config.Name("enable_data_status_icon")
    @Config.LangKey("config.terrarium.enable_data_status_icon")
    @Config.Comment("If true, the current status for data streaming will be displayed in the top left corner of the screen")
    public static boolean dataStatusIcon = true;

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Terrarium.MODID)) {
            ConfigManager.sync(Terrarium.MODID, Config.Type.INSTANCE);
        }
    }
}
