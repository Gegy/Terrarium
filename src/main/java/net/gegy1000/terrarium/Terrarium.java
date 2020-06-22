package net.gegy1000.terrarium;

import net.gegy1000.terrarium.server.ServerProxy;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.command.TerrariumCommand;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.gegy1000.terrarium.server.world.chunk.tracker.SavedColumnTracker;
import net.gegy1000.terrarium.server.world.chunk.tracker.SavedCubeTracker;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(modid = Terrarium.ID, name = "Terrarium", version = Terrarium.VERSION, acceptedMinecraftVersions = "[1.12]", dependencies = "after:cubicchunks")
public class Terrarium {
    public static final String ID = "terrarium";
    public static final String VERSION = "1.1.1";

    public static final String CLIENT_PROXY = "net.gegy1000.terrarium.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.terrarium.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Terrarium.ID);

    public static boolean serverHasMod = false;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        TerrariumCapabilities.onPreInit();

        NETWORK.registerMessage(TerrariumHandshakeMessage.Handler.class, TerrariumHandshakeMessage.class, 0, Side.SERVER);
        NETWORK.registerMessage(TerrariumHandshakeMessage.Handler.class, TerrariumHandshakeMessage.class, 1, Side.CLIENT);
        NETWORK.registerMessage(DataFailWarningMessage.Handler.class, DataFailWarningMessage.class, 3, Side.CLIENT);

        MinecraftForge.EVENT_BUS.register(SavedColumnTracker.class);

        if (Loader.isModLoaded("cubicchunks")) {
            registerCubicChunksEvents();
        }
    }

    private static void registerCubicChunksEvents() {
        MinecraftForge.EVENT_BUS.register(SavedCubeTracker.class);
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        TerrariumPresetRegistry.onInit();
    }

    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new TerrariumCommand());
    }

    @NetworkCheckHandler
    public static boolean onCheckNetwork(Map<String, String> mods, Side side) {
        if (side.isServer()) {
            serverHasMod = mods.containsKey(Terrarium.ID);
        }
        return !mods.containsKey(Terrarium.ID) || mods.get(Terrarium.ID).equals(VERSION);
    }
}
