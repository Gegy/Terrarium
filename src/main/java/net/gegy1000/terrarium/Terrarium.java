package net.gegy1000.terrarium;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.gegy1000.terrarium.server.ServerEventHandler;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.gegy1000.terrarium.server.message.LoadingStateMessage;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingStateHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Terrarium implements ModInitializer {
    public static final String MODID = "terrarium";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static boolean serverHasMod = false;

    @Override
    public void onInitialize() {
        if (!TiledDataSource.GLOBAL_CACHE_ROOT.exists()) {
            TiledDataSource.GLOBAL_CACHE_ROOT.mkdirs();
        }

        ServerEventHandler.register();
        LoadingStateHandler.register();
        TerrariumHandshakeTracker.register();

        TerrariumHandshakeMessage.registerTo(CustomPayloadPacketRegistry.SERVER);
        TerrariumHandshakeMessage.registerTo(CustomPayloadPacketRegistry.CLIENT);
        LoadingStateMessage.registerTo(CustomPayloadPacketRegistry.CLIENT);
        DataFailWarningMessage.registerTo(CustomPayloadPacketRegistry.CLIENT);
    }
}
