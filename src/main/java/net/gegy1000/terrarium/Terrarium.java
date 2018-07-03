package net.gegy1000.terrarium;

import net.gegy1000.earth.server.command.GeoTeleportCommand;
import net.gegy1000.terrarium.server.ServerProxy;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(modid = Terrarium.MODID, name = "Terrarium", version = Terrarium.VERSION, acceptedMinecraftVersions = "[1.12]")
public class Terrarium {
    public static final String MODID = "terrarium";
    public static final String VERSION = "0.1.0-dev";

    public static final String CLIENT_PROXY = "net.gegy1000.terrarium.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.terrarium.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static SimpleNetworkWrapper network;

    public static boolean serverHasMod = false;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        PROXY.onPreInit();

        network = NetworkRegistry.INSTANCE.newSimpleChannel(Terrarium.MODID);
        network.registerMessage(TerrariumHandshakeMessage.Handler.class, TerrariumHandshakeMessage.class, 0, Side.SERVER);
        network.registerMessage(TerrariumHandshakeMessage.Handler.class, TerrariumHandshakeMessage.class, 1, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        PROXY.onInit();
    }

    @Mod.EventHandler
    public static void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit();
    }

    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new GeoTeleportCommand());
    }

    @NetworkCheckHandler
    public static boolean onCheckNetwork(Map<String, String> mods, Side side) {
        if (side.isServer()) {
            serverHasMod = mods.containsKey(Terrarium.MODID);
        }
        return !mods.containsKey(Terrarium.MODID) || mods.get(Terrarium.MODID).equals(VERSION);
    }
}
