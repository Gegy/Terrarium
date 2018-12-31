package net.gegy1000.earth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.commands.CommandRegistry;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.gegy1000.earth.server.command.GeoTeleportCommand;
import net.gegy1000.earth.server.command.GeoToolCommand;
import net.gegy1000.earth.server.message.EarthMapGuiMessage;
import net.gegy1000.earth.server.message.EarthPanoramaMessage;
import net.gegy1000.earth.server.world.CoverDebugGeneratorType;
import net.gegy1000.earth.server.world.EarthGeneratorType;
import net.gegy1000.earth.server.world.pipeline.source.EarthRemoteData;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.minecraft.world.level.LevelGeneratorType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TerrariumEarth implements ModInitializer, EarthProxy {
    public static final String MODID = "earth";
    public static final String VERSION = "2.0.0-dev";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static EarthProxy proxy;

    public static final LevelGeneratorType EARTH_TYPE = new EarthGeneratorType().create();
    public static final LevelGeneratorType COVER_DEBUG_TYPE = new CoverDebugGeneratorType().create();

    public TerrariumEarth() {
        proxy = this;
    }

    @Override
    public void onInitialize() {
        Thread thread = new Thread(() -> {
            EarthRemoteData.loadInfo();
            SrtmHeightSource.loadValidTiles();
        }, "Terrarium Remote Load");
        thread.setDaemon(true);
        thread.start();

        EarthMapGuiMessage.registerTo(CustomPayloadPacketRegistry.CLIENT);
        EarthPanoramaMessage.registerTo(CustomPayloadPacketRegistry.CLIENT);

        CommandRegistry.INSTANCE.register(false, GeoTeleportCommand::register);
        CommandRegistry.INSTANCE.register(false, GeoToolCommand::register);
    }
}
