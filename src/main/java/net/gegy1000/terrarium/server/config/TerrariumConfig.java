package net.gegy1000.terrarium.server.config;

import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Terrarium.ID)
@Mod.EventBusSubscriber(modid = Terrarium.ID)
public class TerrariumConfig {
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Terrarium.ID)) {
            ConfigManager.sync(Terrarium.ID, Config.Type.INSTANCE);
        }
    }
}
